package frontend;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

public final class Musica {
    private Musica() { }
    private static volatile Clip clipMusica;
    private static volatile SourceDataLine lineaStreaming;
    private static volatile Thread hiloMusica;
    private static volatile String musicaRutaActual = "";
    private static volatile String ultimaMusicaRuta = "";
    private static volatile boolean musicaActiva = true;
    private static final AtomicLong MUSICA_TOKEN = new AtomicLong(0);

    public static synchronized void reproducirMusica(String rutaRelativa) {
        if (!musicaActiva || rutaRelativa == null || rutaRelativa.isBlank()) return;
        String limpia = rutaRelativa.replace('\\', '/').replaceAll("^/+", "");
        if (limpia.startsWith("assets/")) limpia = limpia.substring("assets/".length());
        if (limpia.equals(musicaRutaActual) && hiloMusica != null && hiloMusica.isAlive()) return;

        detenerMusicaInterno();
        final String rutaAudio = limpia;
        final long token = MUSICA_TOKEN.incrementAndGet();
        musicaRutaActual = rutaAudio;
        ultimaMusicaRuta = rutaAudio;
        Thread hilo = new Thread(() -> reproducirEnBucle(rutaAudio, token), "Mazmorras-Audio");
        hilo.setDaemon(true);
        hiloMusica = hilo;
        hilo.start();
    }

    private static URL buscarRecursoURL(String ruta) {
        return Recursos.urlRecurso(ruta);
    }

    private static void reproducirEnBucle(String rutaAudio, long token) {
        URL url = buscarRecursoURL(rutaAudio);
        if (url == null) return;
        try {
            // Primer intento: Clip de Java Sound, compatible con WAV PCM estándar.
            try (AudioInputStream entrada = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()))) {
                AudioFormat formato = entrada.getFormat();
                validarFormatoAudio(formato);
                Clip clip = AudioSystem.getClip();
                clip.open(entrada);
                if (token != MUSICA_TOKEN.get() || !musicaActiva) { clip.close(); return; }
                try {
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl volumen = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        volumen.setValue(Math.max(volumen.getMinimum(), Math.min(volumen.getMaximum(), -19.0f)));
                    }
                } catch (Exception ignored) { }
                clipMusica = clip;
                System.out.println("[Audio] OK Clip: " + rutaAudio + " | " + formato);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                while (token == MUSICA_TOKEN.get() && musicaActiva && clip.isOpen()) {
                    try { Thread.sleep(200); }
                    catch (InterruptedException ex) { Thread.currentThread().interrupt(); break; }
                }
                clip.stop();
                clip.close();
            }
        } catch (Exception clipError) {
            if (token != MUSICA_TOKEN.get() || !musicaActiva) return;
            System.err.println("[Audio] Clip no disponible; usando streaming: " + clipError.getMessage());
            try {
                reproducirStreaming(url, rutaAudio, token);
            } catch (Exception streamError) {
                if (token == MUSICA_TOKEN.get()) {
                    System.err.println("[Audio] ERROR DE REPRODUCCIÓN: " + streamError.getClass().getSimpleName()
                            + " - " + streamError.getMessage());
                    System.err.println("[Audio] Verifica el volumen de Windows y que exista un dispositivo de salida.");
                }
            }
        } finally {
            limpiarAudioSiCorresponde(token);
        }
    }

    private static void validarFormatoAudio(AudioFormat formato) {
        if (!AudioFormat.Encoding.PCM_SIGNED.equals(formato.getEncoding())
                || formato.getSampleSizeInBits() != 16
                || formato.getChannels() != 1
                || Math.abs(formato.getSampleRate() - 22050f) > 1f) {
            throw new IllegalArgumentException("Formato WAV no compatible: " + formato
                    + ". Se requiere PCM 16-bit, mono, 22050 Hz.");
        }
    }

    private static void reproducirStreaming(URL url, String rutaAudio, long token) throws Exception {
        byte[] buffer = new byte[8192];
        while (token == MUSICA_TOKEN.get() && musicaActiva) {
            try (AudioInputStream entrada = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()))) {
                AudioFormat formato = entrada.getFormat();
                validarFormatoAudio(formato);
                SourceDataLine linea = AudioSystem.getSourceDataLine(formato);
                linea.open(formato, 32768);
                try {
                    if (linea.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl volumen = (FloatControl) linea.getControl(FloatControl.Type.MASTER_GAIN);
                        volumen.setValue(Math.max(volumen.getMinimum(), Math.min(volumen.getMaximum(), -19.0f)));
                    }
                } catch (Exception ignored) { }
                lineaStreaming = linea;
                linea.start();
                System.out.println("[Audio] OK SourceDataLine: " + rutaAudio + " | " + formato);
                int leidos;
                while (token == MUSICA_TOKEN.get() && musicaActiva
                        && (leidos = entrada.read(buffer, 0, buffer.length)) != -1) {
                    int offset = 0;
                    while (offset < leidos && token == MUSICA_TOKEN.get() && musicaActiva) {
                        offset += linea.write(buffer, offset, leidos - offset);
                    }
                }
                if (lineaStreaming == linea) lineaStreaming = null;
                linea.drain();
                linea.stop();
                linea.close();
            }
        }
    }

    private static synchronized void limpiarAudioSiCorresponde(long token) {
        if (token == MUSICA_TOKEN.get()) {
            if (clipMusica != null) { try { clipMusica.stop(); clipMusica.close(); } catch (Exception ignored) { } clipMusica = null; }
            if (lineaStreaming != null) { try { lineaStreaming.stop(); lineaStreaming.close(); } catch (Exception ignored) { } lineaStreaming = null; }
            musicaRutaActual = "";
            hiloMusica = null;
        }
    }

    public static synchronized void detenerMusica() {
        MUSICA_TOKEN.incrementAndGet();
        detenerMusicaInterno();
        musicaRutaActual = "";
        ultimaMusicaRuta = "";
    }

    private static void detenerMusicaInterno() {
        Clip clip = clipMusica;
        clipMusica = null;
        if (clip != null) { try { clip.stop(); clip.flush(); clip.close(); } catch (Exception ignored) { } }
        SourceDataLine linea = lineaStreaming;
        lineaStreaming = null;
        if (linea != null) { try { linea.stop(); linea.flush(); linea.close(); } catch (Exception ignored) { } }
        Thread hilo = hiloMusica;
        hiloMusica = null;
        if (hilo != null && hilo != Thread.currentThread()) hilo.interrupt();
        musicaRutaActual = "";
    }

    public static synchronized void setMusicaActiva(boolean activa) {
        if (musicaActiva == activa) return;
        musicaActiva = activa;
        if (!activa) {
            MUSICA_TOKEN.incrementAndGet();
            detenerMusicaInterno();
        } else if (!ultimaMusicaRuta.isEmpty()) {
            reproducirMusica(ultimaMusicaRuta);
        }
    }

    public static boolean isMusicaActiva() { return musicaActiva; }

}
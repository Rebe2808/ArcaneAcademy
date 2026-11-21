package frontend;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Recursos {
    private static final ConcurrentHashMap<String, BufferedImage> CACHE_IMAGENES = new ConcurrentHashMap<>();

    private static final Set<Path> RAICES_RECURSOS = buscarRaicesRecursos();

    /** Classpath al ejecutar el JAR; proyecto detectado al ejecutar Main desde un IDE. */
    private static Set<Path> buscarRaicesRecursos() {
        Set<Path> bases = new LinkedHashSet<>();
        bases.add(new File(System.getProperty("user.dir", ".")).toPath().toAbsolutePath().normalize());
        try {
            Path clases = java.nio.file.Paths.get(Recursos.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            bases.add(Files.isDirectory(clases) ? clases : clases.getParent());
        } catch (Exception ex) {
            System.err.println("[Recursos] No se pudo resolver la ubicacion de clases: " + ex.getMessage());
        }
        Set<Path> raices = new LinkedHashSet<>();
        for (Path base : bases) {
            Path cursor = base;
            for (int nivel = 0; cursor != null && nivel < 6; nivel++, cursor = cursor.getParent()) {
                raices.add(cursor.resolve("assets"));
                raices.add(cursor.resolve("src").resolve("assets"));
                // Un IDE puede abrir el contenedor del ZIP y compilar en su propio bin/.
                // Solo inspeccionamos hijos inmediatos de las dos bases cercanas.
                if (nivel < 2 && Files.isDirectory(cursor)) {
                    try (java.nio.file.DirectoryStream<Path> hijos = Files.newDirectoryStream(cursor)) {
                        for (Path hijo : hijos) {
                            if (Files.isRegularFile(hijo.resolve("src").resolve("Main.java"))
                                    && Files.isRegularFile(hijo.resolve("src/frontend/Recursos.java"))) {
                                raices.add(hijo.resolve("assets"));
                            }
                        }
                    } catch (java.io.IOException ex) {
                        System.err.println("[Recursos] No se pudo inspeccionar " + cursor + ": " + ex.getMessage());
                    }
                }
            }
        }
        return raices;
    }

    private static java.io.InputStream abrirRecurso(String ruta) throws java.io.IOException {
        java.io.InputStream in = Recursos.class.getResourceAsStream("/assets/" + ruta);
        if (in != null) return in;
        for (Path raiz : RAICES_RECURSOS) {
            Path candidato = raiz.resolve(ruta).normalize();
            if (candidato.startsWith(raiz) && Files.isRegularFile(candidato)) return Files.newInputStream(candidato);
        }
        return null;
    }

    public static BufferedImage cargarImagen(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.trim().isEmpty()) return null;
        String limpia = rutaRelativa.replace('\\', '/').replaceAll("^/+", "");
        if (limpia.startsWith("assets/")) limpia = limpia.substring("assets/".length());
        BufferedImage cacheada = CACHE_IMAGENES.get(limpia);
        if (cacheada != null) return cacheada;
        try (java.io.InputStream in = abrirRecurso(limpia)) {
            if (in != null) {
                BufferedImage imagen = ImageIO.read(in);
                if (imagen != null) {
                    CACHE_IMAGENES.put(limpia, imagen);
                    return imagen;
                }
            }
        } catch (java.io.IOException ex) {
            System.err.println("[Recursos] No se pudo leer " + limpia + ": " + ex.getMessage());
        }
        System.err.println("[Recursos] NO ENCONTRADO O INVALIDO: " + limpia
            + " | user.dir=" + System.getProperty("user.dir"));
        return null;
    }

    /** Evita iniciar un juego invisible si se extrae o copia un proyecto incompleto. */
    public static void verificarSprites() {
        String[] rutas = {"imagenes/fondo-unsa.png", "imagenes/fondo_laboratorio.png",
            "imagenes/nivel3-observatorio.jpg", "colisiones/nivel3-muros.png",
            "sprites/jugador-sheet.png", "sprites/jugador-cafe-sheet.png", "sprites/bug-morado-sheet.png",
            "sprites/enemigo-libro-exacto.png", "sprites/enemigo-murcielago-exacto.png",
            "sprites/enemigo-golem-exacto.png", "sprites/enemigo_laboratorio.png", "sprites/mini-jefe-exacto.png"};
        StringBuilder faltantes = new StringBuilder();
        for (String ruta : rutas) if (cargarImagen(ruta) == null) faltantes.append("\n- ").append(ruta);
        if (faltantes.length() > 0) throw new IllegalStateException(
            "No se pudieron cargar los recursos del juego:" + faltantes
            + "\nExtrae el ZIP completo y abre la carpeta que contiene src y assets."
            + "\nDirectorio de ejecucion: " + System.getProperty("user.dir"));
    }

    public static ImageIcon cargarIcono(String rutaRelativa, int w, int h) {
        BufferedImage img = cargarImagen(rutaRelativa);
        if (img == null) return null;
        BufferedImage escalada = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = escalada.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        double escala = Math.min((double) w / img.getWidth(), (double) h / img.getHeight());
        int ancho = Math.max(1, (int) Math.round(img.getWidth() * escala));
        int alto = Math.max(1, (int) Math.round(img.getHeight() * escala));
        int x = (w - ancho) / 2;
        int y = (h - alto) / 2;
        g2.drawImage(img, x, y, ancho, alto, null);
        g2.dispose();
        return new ImageIcon(escalada);
    }

    // Fuente local exclusiva del menu: no altera la geometria de los niveles.
    private static final Font FUENTE_MENU = cargarFuenteMenu();

    private static Font cargarFuenteMenu() {
        try (java.io.InputStream in = abrirRecurso("fuentes/PressStart2P-Regular.ttf")) {
            if (in != null) return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception ex) {
            System.err.println("[Recursos] No se pudo cargar la fuente: " + ex.getMessage());
        }
        return new Font(Font.MONOSPACED, Font.PLAIN, 12);
    }

    public static Font fuenteMenu(float size) {
        return FUENTE_MENU.deriveFont(Font.PLAIN, size);
    }

    public static Font fuentePixel(int style, float size) {
        String[] candidatos = {"Press Start 2P", "Courier New", "Monospaced"};
        for (String nombre : candidatos) {
            Font f = new Font(nombre, style, (int) size);
            if (f.getFamily().equalsIgnoreCase(nombre) || nombre.equals("Monospaced")) {
                return f.deriveFont(size);
            }
        }
        return new Font(Font.MONOSPACED, style, (int) size);
    }

    // Sonidos 8-bit generados directamente con Java (onda cuadrada/triangulo/diente de sierra)
    public static boolean sonidoActivo = true;

    public static void playTone(double freqStart, double freqEnd, int durationMs, String wave, double volume) {
        if (!sonidoActivo) return;
        new Thread(() -> {
            try {
                float sampleRate = 22050f;
                int samples = (int) ((durationMs / 1000.0) * sampleRate);
                byte[] buf = new byte[samples];
                double phase = 0;
                for (int i = 0; i < samples; i++) {
                    double t = (double) i / samples;
                    double freq = freqStart + (freqEnd - freqStart) * t;
                    phase += (2.0 * Math.PI * freq) / sampleRate;
                    double val;
                    if ("triangle".equals(wave)) {
                        val = (2.0 / Math.PI) * Math.asin(Math.sin(phase));
                    } else if ("sawtooth".equals(wave)) {
                        val = 2.0 * ((phase / (2.0 * Math.PI)) % 1.0) - 1.0;
                    } else {
                        val = Math.sin(phase) >= 0 ? 1.0 : -1.0;
                    }
                    double env = Math.max(0.01, 1.0 - t);
                    buf[i] = (byte) (val * volume * 0.65 * env * 127);
                }
                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
                try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
                    sdl.open(af);
                    sdl.start();
                    sdl.write(buf, 0, buf.length);
                    sdl.drain();
                }
            } catch (Exception ignored) {}
        }).start();
    }

    public static void playSelect() { playTone(440, 440, 50, "square", 0.25); }
    public static void playConfirm() {
        playTone(523.25, 523.25, 80, "square", 0.25);
        new Timer(80, e -> { ((Timer) e.getSource()).stop(); playTone(659.25, 659.25, 120, "square", 0.25); }).start();
    }
    public static void playJump() { playTone(150, 420, 120, "square", 0.25); }
    public static void playCoin() {
        playTone(523, 523, 80, "triangle", 0.3);
        new Timer(80, e -> { ((Timer) e.getSource()).stop(); playTone(659, 659, 120, "triangle", 0.3); }).start();
    }
    public static void playHit() { playTone(120, 40, 150, "sawtooth", 0.35); }
    public static void playAttack() { playTone(300, 600, 100, "sine", 0.25); }
    public static void playBossDie() { playTone(400, 50, 600, "sawtooth", 0.4); }

    public static java.net.URL urlRecurso(String ruta) {
        java.net.URL url = Recursos.class.getResource("/assets/" + ruta);
        if (url != null) return url;
        for (java.nio.file.Path raiz : RAICES_RECURSOS) {
            java.nio.file.Path candidato = raiz.resolve(ruta).normalize();
            if (candidato.startsWith(raiz) && java.nio.file.Files.isRegularFile(candidato)) {
                try { return candidato.toUri().toURL(); }
                catch (java.net.MalformedURLException ex) { throw new IllegalStateException(ex); }
            }
        }
        return null;
    }
}

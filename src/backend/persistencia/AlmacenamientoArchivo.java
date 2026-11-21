package backend.persistencia;
import backend.Partida;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public final class AlmacenamientoArchivo {
    private static final Path CARPETA = Path.of(System.getProperty("user.home"), ".mazmorras-unsa-poo");
    private static final Path PARTIDA = CARPETA.resolve("partida-v1.dat");
    private AlmacenamientoArchivo() { }
    public static void guardar(byte[] datos) throws IOException {
        Partida p = CodecPartida.decodificar(datos);
        Files.createDirectories(CARPETA);
        Path temporal = CARPETA.resolve("partida-v1.tmp");
        Files.write(temporal, datos);
        try { Files.move(temporal, PARTIDA, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException ex) { Files.move(temporal, PARTIDA, StandardCopyOption.REPLACE_EXISTING); }
        // Resumen legible para la competencia de archivos de texto. No reemplaza al guardado binario.
        Properties resumen = new Properties();
        resumen.setProperty("nivel", "" + p.getNivelActual());
        resumen.setProperty("vida", "" + p.getJugador().getHp());
        resumen.setProperty("mana", "" + p.getJugador().getMp());
        resumen.setProperty("monedas", "" + p.getMonedero().getMonedas());
        try (OutputStream out = Files.newOutputStream(CARPETA.resolve("resumen.properties"))) {
            resumen.store(out, "Resumen de la ultima partida guardada");
        }
    }
    public static Partida cargar() throws IOException {
        if (!Files.isRegularFile(PARTIDA)) throw new IOException("Todavia no hay una partida guardada");
        if (Files.size(PARTIDA) > 4_000_000) throw new IOException("Archivo demasiado grande");
        return CodecPartida.decodificar(Files.readAllBytes(PARTIDA));
    }
}
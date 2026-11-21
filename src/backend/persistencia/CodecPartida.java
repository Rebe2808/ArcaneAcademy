package backend.persistencia;
import backend.Partida;
import java.io.*;

public final class CodecPartida {
    private CodecPartida() { }
    public static byte[] codificar(Partida partida) throws IOException {
        if (partida == null || !partida.getJugador().estaVivo() || partida.isCompletada())
            throw new IOException("Guarda una partida activa con el jugador vivo.");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeInt(1); out.writeObject(partida);
        }
        return bytes.toByteArray();
    }
    public static Partida decodificar(byte[] datos) throws IOException {
        if (datos == null || datos.length > 4_000_000) throw new IOException("Archivo demasiado grande o vacio");
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(datos))) {
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                "maxdepth=32;maxrefs=20000;maxarray=10000;maxbytes=4000000;backend.*;java.util.*;java.lang.*;!*"));
            if (in.readInt() != 1) throw new IOException("Version de guardado incompatible");
            Object objeto = in.readObject();
            if (!(objeto instanceof Partida)) throw new IOException("No es una partida");
            Partida p = (Partida)objeto;
            if (p.getNivelActual() < 1 || p.getNivelActual() > 2 || !p.getJugador().estaVivo() || p.isCompletada())
                throw new IOException("Estado de partida invalido");
            return p;
        } catch (ClassNotFoundException ex) { throw new IOException("Clases incompatibles", ex); }
    }
}

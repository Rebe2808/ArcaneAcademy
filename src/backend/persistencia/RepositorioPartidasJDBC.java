package backend.persistencia;
import backend.Partida;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

public final class RepositorioPartidasJDBC {
    private RepositorioPartidasJDBC() { }
    private static Connection conectar() throws IOException, SQLException {
        Properties p = new Properties();
        Path archivo = Path.of("config", "database.properties");
        if (!Files.isRegularFile(archivo)) throw new IOException("Falta config/database.properties");
        try (InputStream in = Files.newInputStream(archivo)) { p.load(in); }
        String url = p.getProperty("url", "").trim();
        if (url.isEmpty()) throw new IOException("Falta URL de PostgreSQL");
        DriverManager.setLoginTimeout(5);
        return DriverManager.getConnection(url, p.getProperty("usuario", ""), p.getProperty("clave", ""));
    }
    public static void guardar(byte[] datos) throws IOException, SQLException {
        CodecPartida.decodificar(datos);
        String sql = "INSERT INTO partida_poo(id,datos) VALUES(1,?) "
            + "ON CONFLICT(id) DO UPDATE SET datos=EXCLUDED.datos, actualizado_en=CURRENT_TIMESTAMP";
        try (Connection c = conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setQueryTimeout(5); s.setBytes(1, datos); s.executeUpdate();
        }
    }
    public static Partida cargar() throws IOException, SQLException {
        try (Connection c = conectar(); PreparedStatement s = c.prepareStatement("SELECT datos FROM partida_poo WHERE id=1")) {
            s.setQueryTimeout(5);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new IOException("No hay respaldo en PostgreSQL");
                return CodecPartida.decodificar(r.getBytes(1));
            }
        }
    }
}
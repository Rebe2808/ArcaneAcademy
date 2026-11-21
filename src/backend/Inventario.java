package backend;
import java.util.*;

/** Compuesto del jugador: cantidades, llaves y libros tienen una sola fuente de verdad. */
public class Inventario implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<TipoItem, Integer> consumibles = new HashMap<>();
    private final Set<String> llaves = new LinkedHashSet<>();
    private final Set<String> libros = new LinkedHashSet<>();
    public Inventario() {
        consumibles.put(TipoItem.CAFE, 3);
        consumibles.put(TipoItem.ENERGIZANTE, 2);
    }
    public int cantidad(TipoItem tipo) { return consumibles.getOrDefault(tipo, 0); }
    boolean consumir(TipoItem tipo) {
        int cantidad = cantidad(tipo);
        if (cantidad <= 0) return false;
        consumibles.put(tipo, cantidad - 1); return true;
    }
    public void agregarLlave(String llave) {
        if (llave != null && !llave.trim().isEmpty()) llaves.add(llave);
    }
    public boolean tieneLlave(String llave) { return llaves.contains(llave); }
    public boolean agregarLibro(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return false;
        return libros.add(nombre);
    }
    public boolean tieneLibro(String nombre) { return libros.contains(nombre); }
    public int getCantidadLibros() { return libros.size(); }
    public Set<String> getLlaves() { return Collections.unmodifiableSet(llaves); }
    public Set<String> getLibros() { return Collections.unmodifiableSet(libros); }

    public void agregar(TipoItem tipo, int cantidad) {
        if (tipo == null || cantidad <= 0) throw new IllegalArgumentException("Cantidad invalida");
        consumibles.put(tipo, Math.addExact(cantidad(tipo), cantidad));
    }
}

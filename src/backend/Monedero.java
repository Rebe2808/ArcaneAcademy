package backend;

public final class Monedero implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private int monedas;
    public int getMonedas() { return monedas; }
    public void sumar(int cantidad) {
        if (cantidad > 0) monedas = Math.addExact(monedas, cantidad);
    }
    public boolean gastar(int cantidad) {
        if (cantidad <= 0 || monedas < cantidad) return false;
        monedas -= cantidad;
        return true;
    }
}
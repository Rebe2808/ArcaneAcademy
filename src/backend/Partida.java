package backend;

/**
 * La ventana conserva esta partida al cambiar de pantalla; los niveles reciben
 * la misma instancia.
 */
public class Partida implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final Jugador jugador = new Jugador("Kai");
    private final Enemigo enemigoLaboratorio = Enemigo.crearEnemigoLaboratorio();
    private final java.util.List<Enemigo> enemigosBiblioteca = java.util.Arrays.asList(
            enemigoBiblioteca("Libro guardian 1", 35), enemigoBiblioteca("Libro guardian 2", 35),
            enemigoBiblioteca("Libro guardian 3", 35), enemigoBiblioteca("Murcielago 1", 20),
            enemigoBiblioteca("Murcielago 2", 20), enemigoBiblioteca("Murcielago 3", 20),
            enemigoBiblioteca("Golem 1", 55), enemigoBiblioteca("Golem 2", 55));
    private final Enemigo guardianBiblioteca = new Enemigo(
            "Guardian de la Biblioteca", "mini-jefe-exacto.png", 240, 30, 5, 2, null, 35, true);

    private static Enemigo enemigoBiblioteca(String nombre, int hp) {
        return new Enemigo(nombre, "", hp, 22, 0, 2, null, 5, false);
    }

    public java.util.List<Enemigo> getEnemigosBiblioteca() {
        return java.util.Collections.unmodifiableList(enemigosBiblioteca);
    }

    public Enemigo getGuardianBiblioteca() {
        return guardianBiblioteca;
    }

    private final Monedero monedero = new Monedero();
    private final java.util.Set<String> premios = new java.util.HashSet<>();
    private double puntoX = 80, puntoY = 380;

    public Monedero getMonedero() {
        return monedero;
    }

    public boolean premiar(String id, int cantidad) {
        if (id == null || cantidad <= 0 || !premios.add(id))
            return false;
        monedero.sumar(cantidad);
        return true;
    }

    public boolean tienePremio(String id) {
        return premios.contains(id);
    }

    public double getPuntoX() {
        return puntoX;
    }

    public double getPuntoY() {
        return puntoY;
    }

    public void guardarPuntoSeguro(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y))
            throw new IllegalArgumentException("Punto invalido");
        puntoX = x;
        puntoY = y;
    }

    private int nivelActual = 1;
    private boolean completada;

    public Jugador getJugador() {
        return jugador;
    }

    public Enemigo getEnemigoLaboratorio() {
        return enemigoLaboratorio;
    }

    public int getNivelActual() {
        return nivelActual;
    }

    public void entrarBiblioteca() {
        if (enemigoLaboratorio.estaVivo())
            throw new IllegalStateException("Completa el laboratorio primero");
        enemigoLaboratorio.entregarRecompensa(jugador);
        nivelActual = 2;
    }

    public void recogerLibro(String nombre) {
        jugador.getInventario().agregarLibro(nombre);
        if (jugador.getInventario().getCantidadLibros() >= 5)
            jugador.getInventario().agregarLlave("llave2");
    }

    public boolean puedeAbrirPortal() {
        return jugador.getInventario().tieneLlave("llave2") && !guardianBiblioteca.estaVivo();
    }

    public void completarBiblioteca() {
        if (!puedeAbrirPortal())
            throw new IllegalStateException("Portal bloqueado");
        guardianBiblioteca.entregarRecompensa(jugador);
        premiar("victoria-biblioteca", 100);
        completada = true;
    }

    public boolean isCompletada() {
        return completada;
    }

    public int precio(TipoItem tipo) {
        if (tipo == null)
            throw new IllegalArgumentException("Objeto desconocido");
        switch (tipo) {
            case CAFE:
                return 15;
            case ENERGIZANTE:
                return 15;
            case POCION_VIDA:
                return 25;
            case POCION_MANA:
                return 25;
            default:
                throw new IllegalArgumentException("Objeto desconocido");
        }
    }

    public boolean comprar(TipoItem tipo) {
        if (!jugador.estaVivo() || completada || jugador.getInventario().cantidad(tipo) >= 99)
            return false;
        if (!monedero.gastar(precio(tipo)))
            return false;
        jugador.getInventario().agregar(tipo, 1);
        return true;
    }
}

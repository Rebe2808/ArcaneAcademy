package backend;
public class Enemigo extends EntidadJuego {

    private static final long serialVersionUID = 1L;
    private int nivelMazmorra;
    private String llaveRecompensa;
    private int expRecompensa;
    private boolean esJefeFinal;

    public Enemigo(String nombre, String sprite, int hp, int atk, int def, int nivelMazmorra, String llaveRecompensa, int expRecompensa, boolean esJefeFinal) {
        super(nombre, hp, atk, def, sprite);
        this.nivelMazmorra = nivelMazmorra;
        this.llaveRecompensa = llaveRecompensa;
        this.expRecompensa = expRecompensa;
        this.esJefeFinal = esJefeFinal;
    }

    private boolean recompensaEntregada;
    public boolean entregarRecompensa(Jugador jugador) {
        if (estaVivo() || recompensaEntregada) return false;
        recompensaEntregada = true;
        jugador.getInventario().agregarLlave(llaveRecompensa);
        jugador.ganarExperiencia(expRecompensa);
        return true;
    }

    // Métodos fábrica
    public static Enemigo crearEnemigoLaboratorio() {
        return new Enemigo("Bug Morado", "enemigo_laboratorio.png", 45, 18, 5, 1, "llave1", 20, false);
    }

    public static Enemigo crearEnemigoBiblioteca() {
        return new Enemigo("Bibliotecaria", "enemigo_bibliotecario.png", 75, 13, 6, 2, "llave2", 35, false);
    }

    public static Enemigo crearEnemigoOficina() {
        return new Enemigo("Profe Decanato", "enemigo_oficina.png", 105, 18, 10, 3, "llave3", 50, false);
    }

    public static Enemigo crearEnemigoExamenFinal() {
        return new Enemigo("Profe Examen Final", "enemigo_profesor.png", 160, 24, 13, 4, "victoria", 100, true);
    }

    public int getNivelMazmorra() { return nivelMazmorra; }
    public String getLlaveRecompensa() { return llaveRecompensa; }
    public int getExpRecompensa() { return expRecompensa; }
    public boolean isEsJefeFinal() { return esJefeFinal; }
}
package backend;

/** Cada consumible define su propio efecto: polimorfismo usado por Jugador. */
public enum TipoItem {
    CAFE("Cafe Expreso") {
        int aplicar(Jugador j) { return j.curar(40); }
    },
    ENERGIZANTE("Energizante") {
        int aplicar(Jugador j) { return j.recuperarMp(35); }
    },
    POCION_VIDA("Pocion de Vida") {
        int aplicar(Jugador j) { return j.curar(55); }
    },
    POCION_MANA("Pocion de Mana") {
        int aplicar(Jugador j) { return j.recuperarMp(50); }
    };
    private final String nombre;
    TipoItem(String nombre) { this.nombre = nombre; }
    public String getNombre() { return nombre; }
    abstract int aplicar(Jugador jugador);
}

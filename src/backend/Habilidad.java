package backend;

/** Las reglas de mana y efecto no dependen de Swing. */
public abstract class Habilidad implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public enum Tipo { RAYO, REFACTORIZAR, DORMIR }
    private final Tipo tipo;
    private final String nombre;
    private final int costoMp;
    protected Habilidad(Tipo tipo, String nombre, int costo) {
        this.tipo = tipo; this.nombre = nombre; costoMp = costo;
    }
    public final int aplicar(Jugador jugador, Enemigo enemigo) {
        if (!jugador.estaVivo() || jugador.getMp() < costoMp) return -1;
        if (tipo == Tipo.DORMIR && jugador.getHp() == jugador.getMaxHp()) return -1;
        if (tipo != Tipo.DORMIR && (enemigo == null || !enemigo.estaVivo())) return -1;
        jugador.usarMana(costoMp);
        return ejecutarEfecto(jugador, enemigo);
    }
    protected abstract int ejecutarEfecto(Jugador jugador, Enemigo enemigo);
    public static Habilidad crearRayoUNSA() {
        return new Habilidad(Tipo.RAYO, "Rayo UNSA", 25) {
            protected int ejecutarEfecto(Jugador j, Enemigo e) {
                return e.recibirDanio((int)(j.getAtk() * 2.2));
            }
        };
    }
    public static Habilidad crearRefactorizar() {
        return new Habilidad(Tipo.REFACTORIZAR, "Refactorizar", 30) {
            protected int ejecutarEfecto(Jugador j, Enemigo e) {
                return e.recibirDanioDirecto((int)(j.getAtk() * 1.8));
            }
        };
    }
    public static Habilidad crearDormir() {
        return new Habilidad(Tipo.DORMIR, "Siesta EPIS", 10) {
            protected int ejecutarEfecto(Jugador j, Enemigo e) { return j.curar(30); }
        };
    }
    public Tipo getTipo() { return tipo; }
    public String getNombre() { return nombre; }
    public int getCostoMp() { return costoMp; }
}

package backend;

/** Estado comun de las entidades. La identidad de cada objeto se conserva por referencia. */
public abstract class EntidadJuego implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private int hp, maxHp, atk, def;
    private String sprite;
    public EntidadJuego(String nombre) { this(nombre, 100, 10, 5, "desconocido.png"); }
    public EntidadJuego(String nombre, int hp, int atk, int def, String sprite) {
        setNombre(nombre); this.maxHp = Math.max(1, hp); this.hp = this.maxHp;
        setAtk(atk); setDef(def); this.sprite = sprite;
    }
    public int recibirDanio(int potencia) {
        return potencia <= 0 ? 0 : recibirDanioDirecto(Math.max(1, potencia - def));
    }
    public int recibirDanioDirecto(int cantidad) {
        int antes = hp;
        hp -= Math.min(hp, Math.max(0, cantidad));
        return antes - hp;
    }
    public int atacar(EntidadJuego objetivo) {
        if (!estaVivo() || objetivo == null || !objetivo.estaVivo()) return 0;
        return objetivo.recibirDanio(atk);
    }
    public boolean estaVivo() { return hp > 0; }
    public double getPorcentajeHp() { return 100.0 * hp / maxHp; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { nombre = n == null || n.trim().isEmpty() ? "Entidad" : n.trim(); }
    public int getHp() { return hp; }
    public void setHp(int valor) { hp = Math.max(0, Math.min(maxHp, valor)); }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int valor) { maxHp = Math.max(1, valor); hp = Math.min(hp, maxHp); }
    public int getAtk() { return atk; }
    public void setAtk(int valor) { atk = Math.max(0, valor); }
    public int getDef() { return def; }
    public void setDef(int valor) { def = Math.max(0, valor); }
    public String getSprite() { return sprite; }
    public void setSprite(String valor) { sprite = valor; }
}

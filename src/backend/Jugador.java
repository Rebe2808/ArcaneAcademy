package backend;
import java.util.ArrayList;
import java.util.List;

public class Jugador extends EntidadJuego {

    private static final long serialVersionUID = 1L;

    // Constantes estáticas (Tema 03.3)
    public static final int EXP_BASE_NIVEL = 100;
    public static final int MAX_POCIONES_PERMITIDAS = 3;

    private int mp;
    private int maxMp;
    private int nivel;
    private int exp;
    private int expParaSiguienteNivel;
    
    // COMPOSICIÓN (Tema 04): El Jugador POSEE un Inventario
    private Inventario inventario;
    
    // Lista Dinámica (Tema 02)
    private List<Habilidad> habilidades;

    // Arreglo Unidimensional para registro de acciones (Tema 01)
    private String[] historialCombate;
    private int elementosIngresados; // Seguimiento de elementos en arreglo

    public Jugador(String nombre) {
        super(nombre, 100, 20, 10, "jugador.png"); // Llamada a super(...)
        this.maxMp = 100;
        this.mp = 100;
        this.nivel = 1;
        this.exp = 0;
        this.expParaSiguienteNivel = EXP_BASE_NIVEL;

        this.inventario = new Inventario(); // Instanciación por Composición
        this.habilidades = new ArrayList<>();
        this.habilidades.add(Habilidad.crearRayoUNSA());
        this.habilidades.add(Habilidad.crearRefactorizar());
        this.habilidades.add(Habilidad.crearDormir());

        this.historialCombate = new String[10];
        this.elementosIngresados = 0;
    }

    // Agregar registro controlando desbordamiento (Tema 01)
    public void registrarAccion(String accion) {
        if (elementosIngresados < historialCombate.length) {
            historialCombate[elementosIngresados] = accion;
            elementosIngresados++;
        } else {
            // Desplazamiento usando System.arraycopy (Tema 01)
            System.arraycopy(historialCombate, 1, historialCombate, 0, historialCombate.length - 1);
            historialCombate[historialCombate.length - 1] = accion;
        }
    }

    // Búsqueda Lineal de Habilidad por nombre (Tema 01)
    public Habilidad buscarHabilidadPorNombre(String nombre) {
        for (Habilidad h : habilidades) {
            if (h.getNombre().equalsIgnoreCase(nombre)) {
                return h;
            }
        }
        return null;
    }

    public boolean usarMana(int cantidad) {
        if (cantidad < 0 || cantidad > mp) return false;
        mp -= cantidad; return true;
    }
    public int recuperarMp(int cantidad) {
        int antes = mp;
        mp += Math.min(maxMp - mp, Math.max(0, cantidad));
        return mp - antes;
    }
    public int curar(int cantidad) {
        if (!estaVivo()) return 0;
        int antes = getHp();
        setHp(antes + Math.min(getMaxHp() - antes, Math.max(0, cantidad)));
        return getHp() - antes;
    }
    public int usarItem(TipoItem tipo) {
        if (tipo == null || !estaVivo() || inventario.cantidad(tipo) == 0) return -1;
        int recuperado = tipo.aplicar(this);
        if (recuperado == 0) return 0; // No gastar si la barra ya esta llena.
        inventario.consumir(tipo);
        registrarAccion(tipo.getNombre() + ": +" + recuperado);
        return recuperado;
    }
    public Habilidad getHabilidad(Habilidad.Tipo tipo) {
        for (Habilidad h : habilidades) if (h.getTipo() == tipo) return h;
        throw new IllegalArgumentException("Habilidad desconocida");
    }
    public int usarHabilidad(Habilidad.Tipo tipo, Enemigo enemigo) {
        Habilidad habilidad = getHabilidad(tipo);
        int resultado = habilidad.aplicar(this, enemigo);
        if (resultado >= 0) registrarAccion(habilidad.getNombre() + ": " + resultado);
        return resultado;
    }
    @Override public int atacar(EntidadJuego objetivo) {
        int danio = super.atacar(objetivo);
        if (danio > 0) registrarAccion("Ataque: " + danio);
        return danio;
    }
    public void ganarExperiencia(int cantidad) {
        exp += Math.max(0, cantidad);
        while (exp >= expParaSiguienteNivel) {
            exp -= expParaSiguienteNivel; nivel++;
            expParaSiguienteNivel = nivel * EXP_BASE_NIVEL;
        }
    }
    public int getNivel() { return nivel; }
    public int getExp() { return exp; }
    public String[] getHistorialCombate() { return java.util.Arrays.copyOf(historialCombate, elementosIngresados); }
    public int getMp() { return mp; }
    public int getMaxMp() { return maxMp; }
    public Inventario getInventario() { return inventario; }
    public List<Habilidad> getHabilidades() { return java.util.Collections.unmodifiableList(habilidades); }
}

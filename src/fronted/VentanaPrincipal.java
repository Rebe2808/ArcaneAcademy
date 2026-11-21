package frontend;

import backend.*;
import backend.persistencia.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Callable;

public class VentanaPrincipal extends JFrame {
    private Partida partida;
    private PanelJuego nivelActual;
    private boolean ocupada;

    public VentanaPrincipal() {
        Recursos.verificarSprites();
        setTitle("Mazmorras UNSA");
        setSize(1200, 740);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (ocupada) {
                    aviso("Espera a que termine el guardado o la carga.");
                    return;
                }
                Musica.detenerMusica();
                if (nivelActual != null)
                    nivelActual.detener();
                dispose();
                System.exit(0);
            }
        });
        JMenuBar barra = new JMenuBar();
        JMenu sesion = new JMenu("Sesion");
        barra.add(sesion);
        agregar(sesion, "Guardar archivo", () -> guardar(false));
        agregar(sesion, "Cargar archivo", () -> cargar(false));
        agregar(sesion, "Tienda", this::abrirTienda);
        sesion.addSeparator();
        agregar(sesion, "Guardar respaldo en BD", () -> guardar(true));
        agregar(sesion, "Cargar respaldo de BD", () -> cargar(true));
        setJMenuBar(barra);
        mostrarMenuPrincipal();
    }

    private void agregar(JMenu menu, String texto, Runnable accion) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(e -> accion.run());
        menu.add(item);
    }

    private void aviso(String texto) {
        JOptionPane.showMessageDialog(this, texto);
    }

    private boolean desdeMenu() {
        if (ocupada) {
            aviso("Hay una operacion en curso.");
            return false;
        }
        if (!(getContentPane() instanceof MenuPrincipalPanel)) {
            aviso("Vuelve al menu para guardar, cargar o comprar.");
            return false;
        }
        return true;
    }

    public Partida getPartida() {
        if (partida == null)
            throw new IllegalStateException("No hay partida activa");
        return partida;
    }

    public boolean tienePartida() {
        return partida != null && !partida.isCompletada();
    }

    private void mostrar(JPanel panel) {
        setContentPane(panel);
        revalidate();
        repaint();
        setTitle("Mazmorras UNSA" + (partida == null ? "" : " | Monedas: " + partida.getMonedero().getMonedas()));
        if (panel instanceof Nivel1Panel)
            Musica.reproducirMusica("audio/nivel1-cortis-redred.wav");
        else if (panel instanceof Nivel2Panel)
            Musica.reproducirMusica("audio/nivel2-seru-giran-eiti-leda.wav");
        else if (panel instanceof Nivel3Panel)
            Musica.reproducirMusica("audio/nivel3-hold-on-tight-instrumental.wav");
        else
            Musica.detenerMusica();
        SwingUtilities.invokeLater(panel::requestFocusInWindow);
    }

    public void mostrarMenuPrincipal() {
        mostrar(new MenuPrincipalPanel(this));
    }

    public void nuevaPartida() {
        if (ocupada)
            return;
        if (nivelActual != null)
            nivelActual.detener();
        partida = new Partida();
        mostrarNivel1();
    }

    public void mostrarNivel1() {
        if (partida == null)
            partida = new Partida();
        if (nivelActual != null)
            nivelActual.detener();
        nivelActual = new Nivel1Panel(this, partida);
        mostrar(nivelActual);
    }

    public void mostrarNivel2() {
        partida.entrarBiblioteca();
        if (nivelActual != null)
            nivelActual.detener();
        nivelActual = new Nivel2Panel(this, partida);
        mostrar(nivelActual);
    }

    /** Abre el tercer nivel sin alterar el modelo ni el esquema de persistencia. */
    public void mostrarNivel3() {
        if (partida == null)
            return;
        if (nivelActual != null)
            nivelActual.detener();
        nivelActual = new Nivel3Panel(this, partida);
        mostrar(nivelActual);
    }

    public void continuarPartida() {
        if (!ocupada && tienePartida() && nivelActual != null)
            mostrar(nivelActual);
    }

    private void guardar(boolean bd) {
        if (!desdeMenu())
            return;
        if (!tienePartida()) {
            aviso("No hay partida activa para guardar.");
            return;
        }
        try {
            // Captura consistente del modelo pausado, antes del trabajo en segundo plano.
            byte[] datos = CodecPartida.codificar(partida);
            ejecutar(() -> {
                if (bd)
                    RepositorioPartidasJDBC.guardar(datos);
                else
                    AlmacenamientoArchivo.guardar(datos);
                return null;
            }, bd ? "Respaldo guardado en PostgreSQL." : "Partida guardada en archivo.");
        } catch (Exception ex) {
            aviso(ex.getMessage());
        }
    }

    private void cargar(boolean bd) {
        if (!desdeMenu())
            return;
        if (tienePartida() && JOptionPane.showConfirmDialog(this,
                "Cargar reemplazara la sesion actual. ¿Continuar?", "Cargar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        ejecutar(() -> bd ? RepositorioPartidasJDBC.cargar() : AlmacenamientoArchivo.cargar(), "Partida cargada.");
    }

    private void ejecutar(Callable<Partida> tarea, String mensaje) {
        ocupada = true;
        new SwingWorker<Partida, Void>() {
            protected Partida doInBackground() throws Exception {
                return tarea.call();
            }

            protected void done() {
                try {
                    Partida leida = get();
                    if (leida != null) {
                        if (nivelActual != null)
                            nivelActual.detener();
                        partida = leida;
                        nivelActual = partida.getNivelActual() == 1
                                ? new Nivel1Panel(VentanaPrincipal.this, partida)
                                : new Nivel2Panel(VentanaPrincipal.this, partida);
                        mostrar(nivelActual);
                    }
                    if (leida == null)
                        aviso(mensaje);
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    aviso("No se completo la operacion: " + causa.getMessage());
                } finally {
                    ocupada = false;
                }
            }
        }.execute();
    }

    private void abrirTienda() {
        if (!desdeMenu())
            return;
        if (!tienePartida()) {
            aviso("Inicia una partida primero.");
            return;
        }
        TipoItem[] tipos = TipoItem.values();
        String[] opciones = new String[tipos.length];
        for (int i = 0; i < tipos.length; i++)
            opciones[i] = tipos[i].getNombre() + " - " + partida.precio(tipos[i]);
        int elegido = JOptionPane.showOptionDialog(this,
                "Saldo: " + partida.getMonedero().getMonedas() + " monedas. Compra una unidad:",
                "Tienda", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (elegido >= 0) {
            aviso(partida.comprar(tipos[elegido]) ? "Compra realizada." : "Saldo insuficiente o inventario lleno.");
            setTitle("Mazmorras UNSA | Monedas: " + partida.getMonedero().getMonedas());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}

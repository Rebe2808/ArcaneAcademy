import javax.swing.SwingUtilities;
import frontend.VentanaPrincipal;

/**
 * Punto de entrada unico de Mazmorras UNSA.
 * Ejecuta la aplicacion de escritorio construida con Java Swing.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                VentanaPrincipal ventana = new VentanaPrincipal();
                ventana.setVisible(true);
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage(),
                        "Recursos del juego incompletos", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

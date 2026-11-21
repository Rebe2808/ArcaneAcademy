package frontend;
import backend.*;
import javax.swing.*;
import java.awt.*;

public final class DialogoInventario {
    private DialogoInventario() { }
    public static void mostrar(Component padre, Jugador jugador, Runnable refrescar) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(padre), "Inventario", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(15, 23, 42));
        JLabel estado = new JLabel(), mensaje = new JLabel("Selecciona un objeto.");
        estado.setForeground(Color.WHITE); mensaje.setForeground(Color.YELLOW);
        panel.add(estado);
        TipoItem[] tipos = TipoItem.values();
        JButton[] botones = new JButton[tipos.length];
        Runnable actualizar = () -> {
            estado.setText("HP " + jugador.getHp() + "/" + jugador.getMaxHp()
                + " | MP " + jugador.getMp() + "/" + jugador.getMaxMp());
            for (int i=0; i<tipos.length; i++) {
                botones[i].setText(tipos[i].getNombre() + " x" + jugador.getInventario().cantidad(tipos[i]));
                botones[i].setEnabled(jugador.getInventario().cantidad(tipos[i]) > 0);
            }
            refrescar.run();
        };
        for (int i=0; i<tipos.length; i++) {
            final TipoItem tipo = tipos[i];
            botones[i] = new JButton();
            botones[i].addActionListener(e -> {
                int efecto = jugador.usarItem(tipo);
                mensaje.setText(efecto > 0 ? "Recuperaste " + efecto + "." : "Barra llena o sin existencias: no se gasto.");
                actualizar.run();
            });
            panel.add(botones[i]);
        }
        JButton cerrar = new JButton("Volver"); cerrar.addActionListener(e -> dlg.dispose());
        panel.add(mensaje); panel.add(cerrar);
        actualizar.run(); dlg.setContentPane(panel); dlg.pack(); dlg.setLocationRelativeTo(padre); dlg.setVisible(true);
    }
}
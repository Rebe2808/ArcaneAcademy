package frontend;

import javax.swing.*;
import java.awt.*;

public class MarcoRetroPanel extends JPanel {
    private Color bordeExterno;
    private Color bordeInterno;
    private Color fondoTop;
    private Color fondoBottom;
    private boolean remachesDorados;

    public MarcoRetroPanel(Color bordeExterno, Color bordeInterno, Color fondoTop, Color fondoBottom, boolean remachesDorados) {
        this.bordeExterno = bordeExterno;
        this.bordeInterno = bordeInterno;
        this.fondoTop = fondoTop;
        this.fondoBottom = fondoBottom;
        this.remachesDorados = remachesDorados;
        setOpaque(false);
    }

    public void setColoresBorde(Color ext, Color in) {
        this.bordeExterno = ext;
        this.bordeInterno = in;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        // Fondo degradado oscuro translúcido
        GradientPaint gp = new GradientPaint(0, 0, fondoTop, 0, h, fondoBottom);
        g2.setPaint(gp);
        g2.fillRect(4, 4, w - 8, h - 8);

        // Outline negro exterior (2px)
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(1, 1, w - 3, h - 3);

        // Borde principal (4px)
        g2.setColor(bordeExterno);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(4, 4, w - 8, h - 8);

        // Borde interior brillante (2px)
        g2.setColor(bordeInterno);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(7, 7, w - 14, h - 14);

        // Remaches dorados decorativos en las esquinas
        if (remachesDorados) {
            g2.setColor(new Color(250, 204, 21));
            g2.fillRect(0, 0, 8, 8);
            g2.fillRect(w - 8, h - 8, 8, 8);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

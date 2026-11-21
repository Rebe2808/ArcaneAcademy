package frontend;

import javax.swing.*;
import java.awt.*;

public class BarraEstadoPanel extends JPanel {
    private int valor = 100;
    private int maximo = 100;
    private final Color colorTop;
    private final Color colorMid;
    private final Color colorBot;
    private final Color colorBorde;
    private boolean parpadeo = false;

    public BarraEstadoPanel(Color top, Color mid, Color bot, Color borde) {
        this.colorTop = top;
        this.colorMid = mid;
        this.colorBot = bot;
        this.colorBorde = borde;
        setPreferredSize(new Dimension(180, 18));
        setOpaque(false);
    }

    public void setValores(int valor, int maximo) {
        this.valor = Math.max(0, Math.min(maximo, valor));
        this.maximo = Math.max(1, maximo);
        repaint();
    }

    public void setParpadeo(boolean p) {
        this.parpadeo = p;
        repaint();
    }

    public double getPorcentaje() {
        return (valor * 100.0) / maximo;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        // Fondo oscuro #05070f
        g2.setColor(new Color(5, 7, 15));
        g2.fillRect(0, 0, w, h);

        // Relleno proporcional
        int fillW = (int) Math.round(((double) valor / maximo) * (w - 4));
        if (fillW > 0) {
            GradientPaint gp = new GradientPaint(0, 2, colorTop, 0, h - 2, colorBot);
            g2.setPaint(gp);
            g2.fillRect(2, 2, fillW, h - 4);

            // Brillo superior (40%)
            g2.setColor(new Color(255, 255, 255, parpadeo ? 150 : 72));
            g2.fillRect(2, 2, fillW, (int) ((h - 4) * 0.4));
        }

        // Marcas (ticks) cada 10%
        g2.setColor(new Color(0, 0, 0, 140));
        for (int i = 1; i < 10; i++) {
            int tx = (w * i) / 10;
            g2.fillRect(tx, 2, 2, h - 4);
        }

        // Borde 2px
        g2.setColor(colorBorde);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(1, 1, w - 2, h - 2);
        g2.dispose();
    }
}

package frontend;

import java.awt.*;
import javax.swing.*;

/** Equivalente Swing de .menu-hint y kbd del CSS. Son indicadores, no botones. */
public class AyudaTecladoPanel extends JPanel {
    public AyudaTecladoPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(330, 44));
        setMinimumSize(new Dimension(280, 44));
        setMaximumSize(new Dimension(330, 44));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        getAccessibleContext().setAccessibleName("Flechas arriba y abajo para mover; Enter para elegir");
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setColor(new Color(148, 163, 184, 64));
        g.fillRect(0, 0, getWidth(), 2);
        g.setFont(Recursos.fuenteMenu(8f));
        int x = (getWidth() - 264) / 2;
        tecla(g, x, 12, 24, "", -1);
        tecla(g, x + 28, 12, 24, "", 1);
        g.setColor(new Color(148, 163, 184));
        g.drawString("mover", x + 62, 29);
        tecla(g, x + 128, 12, 56, "ENTER", 0);
        g.setColor(new Color(148, 163, 184));
        g.drawString("elegir", x + 194, 29);
        g.dispose();
    }

    private void tecla(Graphics2D g, int x, int y, int w, String texto, int direccion) {
        g.setColor(new Color(100, 116, 139));
        g.fillRoundRect(x, y, w, 28, 5, 5);
        g.setColor(new Color(30, 41, 59));
        g.fillRoundRect(x + 2, y + 2, w - 4, 22, 3, 3);
        g.setColor(Color.WHITE);
        if (direccion == 0) {
            g.drawString(texto, x + (w - g.getFontMetrics().stringWidth(texto)) / 2, y + 17);
        } else {
            // Vector: no depende de que la fuente contenga las flechas Unicode.
            int cx = x + w / 2, cy = y + 13;
            g.setStroke(new BasicStroke(2));
            g.drawLine(cx, cy - 5, cx, cy + 5);
            int punta = cy + direccion * 5;
            g.drawLine(cx, punta, cx - 4, punta - direccion * 4);
            g.drawLine(cx, punta, cx + 4, punta - direccion * 4);
        }
    }
}

package frontend;
import java.awt.*;
import javax.swing.Icon;

/** Iconos pequenos dibujados sin depender de fuentes emoji del sistema. */
public class IconoMenu implements Icon {
    private final int tipo;
    public IconoMenu(int tipo) { this.tipo = tipo; }
    public int getIconWidth() { return 16; }
    public int getIconHeight() { return 16; }
    public void paintIcon(Component c, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        g.setColor(c.getForeground());
        g.setStroke(new BasicStroke(2));
        if (tipo == 0) {
            g.drawLine(3, 2, 13, 13); g.drawLine(13, 2, 3, 13);
            g.drawLine(1, 10, 6, 15); g.drawLine(10, 15, 15, 10);
        } else if (tipo == 1) {
            g.drawRect(2, 1, 12, 13); g.drawRect(5, 1, 6, 5);
            g.drawRect(5, 9, 6, 5);
        } else {
            g.drawLine(6, 6, 13, 13); g.drawOval(11, 11, 3, 3);
            g.drawArc(1, 1, 7, 7, 80, 280);
        }
        g.dispose();
    }
}

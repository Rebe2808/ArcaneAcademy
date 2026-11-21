package frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BotonRetro extends JButton {
    private boolean hover = false;
    private boolean activo = false;
    private Color bordeNormal = new Color(29, 78, 216);
    private Color bordeHover = new Color(147, 197, 253);
    private Color topNormal = new Color(22, 42, 92);
    private Color botNormal = new Color(10, 19, 48);
    private Color topHover = new Color(37, 99, 235);
    private Color botHover = new Color(30, 58, 138);

    public BotonRetro(String texto) {
        super(texto);
        setFont(Recursos.fuentePixel(Font.BOLD, 10f));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.LEFT);
        setHorizontalTextPosition(SwingConstants.RIGHT);
        setIconTextGap(12);
        setMargin(new Insets(10, 12, 10, 12));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) { hover = true; repaint(); }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hover = false; repaint();
            }
        });
    }

    public void setEstiloMorado() {
        bordeNormal = new Color(196, 181, 253);
        bordeHover = Color.WHITE;
        topNormal = new Color(124, 58, 237);
        botNormal = new Color(91, 33, 182);
        topHover = new Color(139, 92, 246);
        botHover = new Color(109, 40, 217);
    }

    public void setEstiloDorado() {
        bordeNormal = new Color(254, 243, 199);
        bordeHover = Color.WHITE;
        topNormal = new Color(252, 211, 77);
        botNormal = new Color(217, 119, 6);
        topHover = new Color(253, 224, 71);
        botHover = new Color(245, 158, 11);
        setForeground(new Color(26, 15, 5));
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        boolean destacado = isEnabled() && (hover || activo || isFocusOwner());
        Color cTop = destacado ? topHover : topNormal;
        Color cBot = destacado ? botHover : botNormal;
        if (!isEnabled()) {
            cTop = new Color(35, 40, 55);
            cBot = new Color(20, 24, 34);
        }

        // Sombra inferior 4px
        g2.setColor(new Color(11, 27, 77));
        g2.fillRect(2, 4, w - 4, h - 4);

        int offsetY = getModel().isPressed() ? 2 : (destacado ? -1 : 0);
        GradientPaint gp = new GradientPaint(0, 2 + offsetY, cTop, 0, h - 4 + offsetY, cBot);
        g2.setPaint(gp);
        g2.fillRect(2, 2 + offsetY, w - 4, h - 6);

        // Borde exterior
        g2.setColor(destacado ? bordeHover : bordeNormal);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(2, 2 + offsetY, w - 5, h - 7);

        g2.dispose();
        super.paintComponent(g);
    }
}

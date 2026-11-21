package frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MenuPrincipalPanel extends JPanel {
    private final VentanaPrincipal ventana;
    private final BufferedImage fondoUnsa;
    private final JLabel lblStatusPill;
    private final JButton btnSoundToggle;
    private final JLabel lblTitle;
    private final JLabel lblSubtitle;
    private final JLabel lblLocation;
    private final JButton[] menuButtons = new JButton[3];
    private final JLabel lblPressStart;
    private final JLabel lblCopyright;
    private int currentIndex = 0;

    // Animacion de fondo y particulas dibujadas con Java2D
    private double breathPhase = 0;
    private double titleShineX = -200;
    private final List<Mota> motas = new ArrayList<>();
    private final Random rnd = new Random();
    private Timer animTimer;

    private static class Mota {
        double x, y, r, vy, vx, fase;
        boolean doradoClaro;
    }

    public MenuPrincipalPanel(VentanaPrincipal ventana) {
        this.ventana = ventana;
        this.fondoUnsa = Recursos.cargarImagen("imagenes/fondo-unsa.png");
        setLayout(new BorderLayout());
        setBackground(new Color(5, 6, 15));
        setFocusable(true);

        for (int i = 0; i < 55; i++) {
            Mota m = new Mota();
            m.x = rnd.nextDouble() * 1280;
            m.y = rnd.nextDouble() * 720;
            m.r = rnd.nextDouble() * 2.5 + 1.5;
            m.vy = -(rnd.nextDouble() * 0.6 + 0.2);
            m.vx = (rnd.nextDouble() - 0.5) * 0.35;
            m.fase = rnd.nextDouble() * Math.PI * 2;
            m.doradoClaro = rnd.nextDouble() >= 0.7;
            motas.add(m);
        }

        // --- BARRA SUPERIOR ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(16, 28, 8, 28));

        lblStatusPill = new JLabel("  ESTADO: LISTO  ");
        lblStatusPill.setFont(Recursos.fuenteMenu(10f));
        lblStatusPill.setForeground(new Color(203, 213, 225));
        lblStatusPill.setOpaque(true);
        lblStatusPill.setBackground(new Color(8, 12, 26, 210));
        lblStatusPill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 60), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        btnSoundToggle = new JButton("SONIDO: ON");
        btnSoundToggle.setFont(Recursos.fuenteMenu(10f));
        btnSoundToggle.setForeground(new Color(203, 213, 225));
        btnSoundToggle.setBackground(new Color(8, 12, 26, 210));
        btnSoundToggle.setFocusPainted(false);
        btnSoundToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSoundToggle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 60), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        btnSoundToggle.addActionListener(e -> {
            Recursos.sonidoActivo = !Recursos.sonidoActivo;
            Musica.setMusicaActiva(Recursos.sonidoActivo);
            btnSoundToggle.setText(Recursos.sonidoActivo ? "SONIDO: ON" : "MUTE SONIDO: OFF");
            if (Recursos.sonidoActivo)
                Recursos.playSelect();
            requestFocusInWindow();
        });

        topBar.add(lblStatusPill, BorderLayout.WEST);
        topBar.add(btnSoundToggle, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // --- CENTRO: TITULO + MENU ---
        JPanel centerWrap = new JPanel();
        centerWrap.setOpaque(false);
        centerWrap.setLayout(new BoxLayout(centerWrap, BoxLayout.Y_AXIS));

        JLabel lblCrest = new JLabel("✦  ⚔  ✦", SwingConstants.CENTER);
        lblCrest.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 30));
        lblCrest.setForeground(new Color(253, 230, 138));
        lblCrest.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTitle = new JLabel("MAZMORRAS UNSA", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                // Sombra pixelada #1a0f05
                g2.setColor(new Color(26, 15, 5));
                g2.drawString(getText(), x + 4, y + 4);
                g2.drawString(getText(), x - 2, y - 2);
                g2.drawString(getText(), x + 2, y - 2);
                g2.drawString(getText(), x - 2, y + 2);
                // Degradado dorado #fff7d1 -> #fcd34d -> #d97706
                LinearGradientPaint gp = new LinearGradientPaint(0, y - fm.getAscent(), 0, y,
                        new float[] { 0f, .4f, .75f, 1f }, new Color[] { new Color(255, 247, 209),
                                new Color(252, 211, 77), new Color(217, 119, 6), new Color(146, 64, 14) });
                g2.setPaint(gp);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        lblTitle.setFont(Recursos.fuenteMenu(56f));
        lblTitle.setPreferredSize(new Dimension(900, 82));
        lblTitle.setMaximumSize(new Dimension(900, 82));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel subtitleBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(185, 28, 28), 0, getHeight(),
                        new Color(127, 29, 29));
                g2.setPaint(gp);
                g2.fillRect(3, 3, getWidth() - 6, getHeight() - 6);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
                g2.setColor(new Color(253, 230, 138));
                g2.drawRect(4, 4, getWidth() - 8, getHeight() - 8);
                g2.dispose();
            }
        };
        subtitleBox.setOpaque(false);
        subtitleBox.setMaximumSize(new Dimension(440, 44));
        lblSubtitle = new JLabel("EL DESAFÍO DE EPIS");
        lblSubtitle.setFont(Recursos.fuenteMenu(14f));
        lblSubtitle.setForeground(Color.WHITE);
        subtitleBox.add(lblSubtitle);
        subtitleBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblLocation = new JLabel("  AREQUIPA · PERÚ  ");
        lblLocation.setFont(Recursos.fuenteMenu(9f));
        lblLocation.setForeground(new Color(226, 232, 240));
        lblLocation.setOpaque(true);
        lblLocation.setBackground(new Color(0, 0, 0, 165));
        lblLocation.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 60), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        lblLocation.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Caja del Menu Principal (pixel-box menu-box)
        MarcoRetroPanel menuBox = new MarcoRetroPanel(
                new Color(226, 232, 240),
                new Color(30, 41, 59),
                new Color(20, 28, 56, 225),
                new Color(10, 14, 28, 240),
                false);
        menuBox.setLayout(new BoxLayout(menuBox, BoxLayout.Y_AXIS));
        menuBox.setBorder(BorderFactory.createEmptyBorder(18, 22, 16, 22));
        menuBox.setMaximumSize(new Dimension(380, 290));
        menuBox.setPreferredSize(new Dimension(380, 290));
        menuBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMenuTitle = new JLabel("MENÚ PRINCIPAL", SwingConstants.CENTER);
        lblMenuTitle.setFont(Recursos.fuenteMenu(12f));
        lblMenuTitle.setForeground(Color.WHITE);
        lblMenuTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuBox.add(lblMenuTitle);
        menuBox.add(Box.createVerticalStrut(10));
        JPanel divisor = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(250, 204, 21, 140));
                for (int x = 0; x < getWidth(); x += 12)
                    g.fillRect(x, 0, 8, 4);
            }
        };
        divisor.setOpaque(false);
        divisor.setMaximumSize(new Dimension(330, 4));
        divisor.setPreferredSize(new Dimension(330, 4));
        menuBox.add(divisor);
        menuBox.add(Box.createVerticalStrut(12));

        String[] labels = { "NUEVA PARTIDA", "CONTINUAR", "OPCIONES" };

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            JButton item = new JButton();
            item.setFont(Recursos.fuenteMenu(11f));
            item.setFocusPainted(false);
            item.setContentAreaFilled(false);
            item.setOpaque(true);
            item.setHorizontalAlignment(SwingConstants.LEFT);
            item.setMaximumSize(new Dimension(330, 42));
            item.setPreferredSize(new Dimension(330, 42));
            item.setAlignmentX(Component.CENTER_ALIGNMENT);
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            item.setText("  " + labels[i]);
            String[] rutasIconos = { "ui/icono-personaje.png", "ui/icono-control.png", "ui/icono-tuerca.png" };
            item.setIcon(Recursos.cargarIcono(rutasIconos[i], 26, 26));
            item.setIconTextGap(10);
            item.setFocusable(false);

            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (currentIndex != idx)
                        updateMenuSelection(idx);
                }
            });
            item.addActionListener(e -> triggerOption(idx));
            menuButtons[i] = item;
            menuBox.add(item);
            menuBox.add(Box.createVerticalStrut(8));
        }

        menuBox.add(Box.createVerticalStrut(6));
        menuBox.add(new AyudaTecladoPanel());

        lblPressStart = new JLabel("— PRESIONA ENTER PARA COMENZAR —", SwingConstants.CENTER);
        lblPressStart.setFont(Recursos.fuenteMenu(9f));
        lblPressStart.setForeground(Color.WHITE);
        lblPressStart.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerWrap.add(Box.createVerticalGlue());
        centerWrap.add(lblCrest);
        centerWrap.add(Box.createVerticalStrut(4));
        centerWrap.add(lblTitle);
        centerWrap.add(Box.createVerticalStrut(4));
        centerWrap.add(subtitleBox);
        centerWrap.add(Box.createVerticalStrut(10));
        centerWrap.add(lblLocation);
        centerWrap.add(Box.createVerticalStrut(18));
        centerWrap.add(menuBox);
        centerWrap.add(Box.createVerticalStrut(12));
        centerWrap.add(lblPressStart);
        centerWrap.add(Box.createVerticalGlue());

        add(centerWrap, BorderLayout.CENTER);

        // --- PIE ---
        lblCopyright = new JLabel("© 2026 DEPARTAMENTO DE INGENIERÍA DE SISTEMAS / UNSA", SwingConstants.CENTER);
        lblCopyright.setFont(Recursos.fuenteMenu(9f));
        lblCopyright.setForeground(new Color(255, 255, 255, 220));
        lblCopyright.setBorder(BorderFactory.createEmptyBorder(8, 10, 14, 10));
        add(lblCopyright, BorderLayout.SOUTH);

        updateMenuSelection(0);
        configurarTeclado();

        animTimer = new Timer(33, e -> {
            breathPhase += 0.02;
            titleShineX += 14;
            if (titleShineX > 1200)
                titleShineX = -200;
            for (Mota m : motas) {
                m.x += m.vx + Math.sin(breathPhase * 2 + m.fase) * 0.25;
                m.y += m.vy;
                if (m.y < -10) {
                    m.y = Math.max(600, getHeight()) + 10;
                    m.x = rnd.nextDouble() * Math.max(800, getWidth());
                }
            }
            lblPressStart.setVisible(((int) (breathPhase * 3)) % 2 == 0);
            repaint();
        });
        animTimer.start();
    }

    @Override
    public void removeNotify() {
        if (animTimer != null)
            animTimer.stop();
        super.removeNotify();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (animTimer != null)
            animTimer.start();
    }

    private void configurarTeclado() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    updateMenuSelection(currentIndex - 1);
                } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                    updateMenuSelection(currentIndex + 1);
                } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                    triggerOption(currentIndex);
                }
            }
        });
    }

    private void updateMenuSelection(int newIndex) {
        currentIndex = (newIndex + menuButtons.length) % menuButtons.length;
        String[] labels = { "NUEVA PARTIDA", "CONTINUAR", "OPCIONES" };

        for (int i = 0; i < menuButtons.length; i++) {
            JButton btn = menuButtons[i];
            if (i == currentIndex) {
                btn.setText("> " + labels[i]);
                btn.setForeground(new Color(250, 204, 21));
                btn.setBackground(new Color(65, 55, 22));
                btn.setOpaque(true);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(2, 5, 2, 2, new Color(250, 204, 21)),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            } else {
                btn.setText("  " + labels[i]);
                btn.setForeground(new Color(203, 213, 225));
                btn.setBackground(new Color(15, 23, 42));
                btn.setOpaque(false);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(2, 5, 2, 2),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        }
        Recursos.playSelect();
    }

    private void triggerOption(int index) {
        Recursos.playConfirm();
        if (index == 0) {
            ventana.nuevaPartida();
        } else if (index == 1) {
            if (ventana.tienePartida())
                ventana.continuarPartida();
            else
                mostrarModalRetro("CONTINUAR",
                        "No hay una sesion activa. Inicia NUEVA PARTIDA o usa Sesion > Cargar archivo para recuperar tu guardado.");
        } else if (index == 2) {
            mostrarModalRetro("OPCIONES", "Configuración de Video: Renderizado Pixelated [ACTIVADO]. Sonido 8-bit ["
                    + (Recursos.sonidoActivo ? "ACTIVADO" : "DESACTIVADO") + "].");
        }
    }

    private void mostrarModalRetro(String titulo, String msg) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), titulo,
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        MarcoRetroPanel panel = new MarcoRetroPanel(
                new Color(226, 232, 240), new Color(30, 41, 59),
                new Color(20, 28, 56), new Color(10, 14, 28), false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        panel.setPreferredSize(new Dimension(440, 210));

        JLabel lblT = new JLabel(titulo, SwingConstants.CENTER);
        lblT.setFont(Recursos.fuenteMenu(13f));
        lblT.setForeground(new Color(250, 204, 21));
        lblT.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblM = new JLabel("<html><center>" + msg + "</center></html>", SwingConstants.CENTER);
        lblM.setFont(Recursos.fuenteMenu(9f));
        lblM.setForeground(new Color(203, 213, 225));
        lblM.setAlignmentX(Component.CENTER_ALIGNMENT);

        BotonRetro btnOk = new BotonRetro("ENTENDIDO");
        btnOk.setEstiloDorado();
        btnOk.setHorizontalAlignment(SwingConstants.CENTER);
        btnOk.setMaximumSize(new Dimension(180, 40));
        btnOk.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOk.addActionListener(e -> {
            Recursos.playTone(300, 300, 50, "square", 0.2);
            dlg.dispose();
            requestFocusInWindow();
        });

        panel.add(lblT);
        panel.add(Box.createVerticalStrut(16));
        panel.add(lblM);
        panel.add(Box.createVerticalGlue());
        panel.add(btnOk);

        dlg.setContentPane(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        if (fondoUnsa != null) {
            double scale = 1.03 + 0.03 * Math.sin(breathPhase);
            int dw = (int) (w * scale);
            int dh = (int) (h * scale);
            int dx = (w - dw) / 2;
            int dy = (h - dh) / 2 - (int) (4 * Math.sin(breathPhase));
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(fondoUnsa, dx, dy, dw, dh, null);
        } else {
            g2.setColor(new Color(12, 16, 36));
            g2.fillRect(0, 0, w, h);
        }

        // Vineta oscura y brillo solar
        g2.setColor(new Color(4, 6, 20, 110));
        g2.fillRect(0, 0, w, h);

        // Motas flotantes
        for (Mota m : motas) {
            int alpha = (int) (130 + 90 * Math.sin(breathPhase * 3 + m.fase));
            alpha = Math.max(25, Math.min(240, alpha));
            g2.setColor(m.doradoClaro ? new Color(255, 240, 170, alpha) : new Color(255, 190, 90, alpha));
            g2.fillRect((int) m.x, (int) m.y, (int) m.r, (int) m.r);
        }
        g2.dispose();
    }
}

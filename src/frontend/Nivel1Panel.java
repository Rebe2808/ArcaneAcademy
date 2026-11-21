package frontend;

import backend.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Nivel1Panel extends PanelJuego {
    private final VentanaPrincipal ventana;

    // Estado y reglas del combate implementados en Java
    private boolean enTurnoJugador = true;
    private final Partida partida;
    private final Jugador jugador;
    private final Enemigo enemigo;

    // Posicion y animacion spritesheet 4x4 (jugador-sheet.png)
    private double jugadorPosX = 0.18; // 18%
    private int jugadorPosY = 15; // bottom 15px
    private boolean mirandoDerecha = true;
    private int animRow = 0; // 0: idle, 1: walk/run, 2: attack/cast, 3: hit
    private int animCol = 0;
    private int animTick = 0;
    private int animDuracionTicks = 0;
    private boolean enSalto = false;
    private int saltoTick = 0;

    // Efectos de combate
    private int lungeJugadorOffset = 0;
    private int lungeEnemigoOffset = 0;
    private int hitJugadorFlash = 0;
    private int hitEnemigoFlash = 0;
    private int shakeTicks = 0;
    private boolean mostrarCorte = false;
    private boolean corteMagico = false;
    private int corteTicks = 0;

    private static class TextoFlotante {
        String texto;
        int x, y, vida;
        Color color;
    }

    private final List<TextoFlotante> flotantes = new ArrayList<>();

    private static class Polvo {
        double x, y, vy, vx;
        int alpha;
    }

    private final List<Polvo> polvos = new ArrayList<>();
    private final Random rnd = new Random();

    // Imagenes
    private final BufferedImage fondoLab;
    private final BufferedImage jugadorSheet;
    private final BufferedImage jugadorCafeSheet;
    private final BufferedImage enemigoAnimadoSheet;
    private int cafeAnimTicks = 0;

    // Componentes Swing HUD e interfaz inferior
    private final BarraEstadoPanel hpBar;
    private final BarraEstadoPanel mpBar;
    private final BarraEstadoPanel enemyHpBar;
    private final JLabel lblHpText;
    private final JLabel lblMpText;
    private final JLabel lblEnemyHpText;
    private final JLabel lblTurnIndicator;
    private final JLabel lblDialogText;

    private final JPanel panelAccionesCard;
    private final CardLayout cardAccionesLayout;
    private final BotonRetro[] botonesPrincipales = new BotonRetro[4];
    private final BotonRetro[] botonesSkills = new BotonRetro[4];
    private final BotonRetro[] botonesItems = new BotonRetro[5];

    private Timer gameTimer;

    public Nivel1Panel(VentanaPrincipal ventana) {
        this(ventana, ventana == null ? new Partida() : ventana.getPartida());
    }

    public Nivel1Panel(VentanaPrincipal ventana, Partida partida) {
        this.ventana = ventana;
        this.partida = partida;
        this.jugador = partida.getJugador();
        this.enemigo = partida.getEnemigoLaboratorio();
        this.fondoLab = Recursos.cargarImagen("imagenes/fondo_laboratorio.png");
        this.jugadorSheet = Recursos.cargarImagen("sprites/jugador-sheet.png");
        this.jugadorCafeSheet = Recursos.cargarImagen("sprites/jugador-cafe-sheet.png");
        this.enemigoAnimadoSheet = Recursos.cargarImagen("sprites/bug-morado-sheet.png");

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 28, 18, 28));
        setFocusable(true);

        for (int i = 0; i < 12; i++) {
            Polvo p = new Polvo();
            p.x = rnd.nextDouble() * 1200;
            p.y = rnd.nextDouble() * 700;
            p.vy = -(0.6 + rnd.nextDouble() * 0.8);
            p.vx = 0.25;
            p.alpha = 140;
            polvos.add(p);
        }

        // =====================================================================
        // 1. HUD SUPERIOR (TARJETA JUGADOR | CENTRO | TARJETA ENEMIGO)
        // =====================================================================
        JPanel hudOverlay = new JPanel(new BorderLayout(20, 0));
        hudOverlay.setOpaque(false);

        // --- PLAYER CARD ---
        MarcoRetroPanel playerCard = new MarcoRetroPanel(
                new Color(37, 99, 235), new Color(59, 130, 246),
                new Color(20, 30, 70, 240), new Color(7, 10, 24, 245), true);
        playerCard.setLayout(new BorderLayout(14, 0));
        playerCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        playerCard.setPreferredSize(new Dimension(450, 114));

        ImageIcon iconJugador = Recursos.cargarIcono("sprites/avatar-jugador.png", 74, 74);
        if (iconJugador == null)
            iconJugador = Recursos.cargarIcono("sprites/jugador-avatar.png", 74, 74);
        JLabel lblAvatarJug = new JLabel(iconJugador);
        lblAvatarJug.setPreferredSize(new Dimension(78, 78));
        lblAvatarJug.setOpaque(true);
        lblAvatarJug.setBackground(Color.BLACK);
        lblAvatarJug.setBorder(BorderFactory.createLineBorder(new Color(59, 130, 246), 3));

        JPanel playerStats = new JPanel();
        playerStats.setOpaque(false);
        playerStats.setLayout(new BoxLayout(playerStats, BoxLayout.Y_AXIS));

        JPanel titleJugRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleJugRow.setOpaque(false);
        JLabel lblNombreJug = new JLabel("EPIS");
        lblNombreJug.setFont(Recursos.fuentePixel(Font.BOLD, 14f));
        lblNombreJug.setForeground(Color.WHITE);
        JLabel lblLvJug = new JLabel(" LV.1 ");
        lblLvJug.setFont(Recursos.fuentePixel(Font.BOLD, 8f));
        lblLvJug.setForeground(new Color(26, 18, 5));
        lblLvJug.setOpaque(true);
        lblLvJug.setBackground(new Color(250, 204, 21));
        titleJugRow.add(lblNombreJug);
        titleJugRow.add(lblLvJug);

        hpBar = new BarraEstadoPanel(new Color(251, 113, 133), new Color(220, 38, 38), new Color(153, 27, 27),
                new Color(11, 27, 77));
        mpBar = new BarraEstadoPanel(new Color(125, 211, 252), new Color(37, 99, 235), new Color(30, 58, 138),
                new Color(11, 27, 77));
        lblHpText = new JLabel("100/100", SwingConstants.RIGHT);
        lblHpText.setFont(Recursos.fuentePixel(Font.BOLD, 10f));
        lblHpText.setForeground(Color.WHITE);
        lblHpText.setPreferredSize(new Dimension(82, 16));

        lblMpText = new JLabel("100/100", SwingConstants.RIGHT);
        lblMpText.setFont(Recursos.fuentePixel(Font.BOLD, 10f));
        lblMpText.setForeground(Color.WHITE);
        lblMpText.setPreferredSize(new Dimension(82, 16));

        playerStats.add(titleJugRow);
        playerStats.add(Box.createVerticalStrut(6));
        playerStats.add(crearFilaStat("HP", hpBar, lblHpText));
        playerStats.add(Box.createVerticalStrut(6));
        playerStats.add(crearFilaStat("MP", mpBar, lblMpText));

        playerCard.add(lblAvatarJug, BorderLayout.WEST);
        playerCard.add(playerStats, BorderLayout.CENTER);

        // --- CENTER BADGE ---
        JPanel centerBadge = new JPanel();
        centerBadge.setOpaque(false);
        centerBadge.setLayout(new BoxLayout(centerBadge, BoxLayout.Y_AXIS));

        JLabel lblLevelTag = new JLabel("  NIVEL 1  ");
        lblLevelTag.setFont(Recursos.fuentePixel(Font.BOLD, 13f));
        lblLevelTag.setForeground(new Color(26, 18, 5));
        lblLevelTag.setOpaque(true);
        lblLevelTag.setBackground(new Color(253, 224, 71));
        lblLevelTag.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 243, 199), 3),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        lblLevelTag.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLevelName = new JLabel("LABORATORIO UNSA");
        lblLevelName.setFont(Recursos.fuentePixel(Font.BOLD, 8f));
        lblLevelName.setForeground(new Color(226, 232, 240));
        lblLevelName.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTurnIndicator = new JLabel("  ▶ TU TURNO  ");
        lblTurnIndicator.setFont(Recursos.fuentePixel(Font.BOLD, 9f));
        lblTurnIndicator.setForeground(new Color(187, 247, 208));
        lblTurnIndicator.setOpaque(true);
        lblTurnIndicator.setBackground(new Color(5, 46, 22, 220));
        lblTurnIndicator.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(34, 197, 94), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        lblTurnIndicator.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerBadge.add(lblLevelTag);
        centerBadge.add(Box.createVerticalStrut(6));
        centerBadge.add(lblLevelName);
        centerBadge.add(Box.createVerticalStrut(8));
        centerBadge.add(lblTurnIndicator);

        // --- ENEMY CARD ---
        MarcoRetroPanel enemyCard = new MarcoRetroPanel(
                new Color(126, 34, 206), new Color(168, 85, 247),
                new Color(20, 30, 70, 240), new Color(7, 10, 24, 245), true);
        enemyCard.setLayout(new BorderLayout(14, 0));
        enemyCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        enemyCard.setPreferredSize(new Dimension(430, 114));

        ImageIcon iconEnemigo = Recursos.cargarIcono("sprites/enemigo_laboratorio.png", 74, 74);
        JLabel lblAvatarEn = new JLabel(iconEnemigo);
        lblAvatarEn.setPreferredSize(new Dimension(78, 78));
        lblAvatarEn.setOpaque(true);
        lblAvatarEn.setBackground(Color.BLACK);
        lblAvatarEn.setBorder(BorderFactory.createLineBorder(new Color(168, 85, 247), 3));

        JPanel enemyStats = new JPanel();
        enemyStats.setOpaque(false);
        enemyStats.setLayout(new BoxLayout(enemyStats, BoxLayout.Y_AXIS));

        JPanel titleEnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        titleEnRow.setOpaque(false);
        JLabel lblNombreEn = new JLabel("BUG MORADO");
        lblNombreEn.setFont(Recursos.fuentePixel(Font.BOLD, 14f));
        lblNombreEn.setForeground(Color.WHITE);
        JLabel lblBossTag = new JLabel(" JEFE ");
        lblBossTag.setFont(Recursos.fuentePixel(Font.BOLD, 8f));
        lblBossTag.setForeground(Color.WHITE);
        lblBossTag.setOpaque(true);
        lblBossTag.setBackground(new Color(239, 68, 68));
        titleEnRow.add(lblNombreEn);
        titleEnRow.add(lblBossTag);

        enemyHpBar = new BarraEstadoPanel(new Color(134, 239, 172), new Color(22, 163, 74), new Color(22, 101, 52),
                new Color(11, 27, 77));
        lblEnemyHpText = new JLabel("45/45", SwingConstants.RIGHT);
        lblEnemyHpText.setFont(Recursos.fuentePixel(Font.BOLD, 10f));
        lblEnemyHpText.setForeground(Color.WHITE);
        lblEnemyHpText.setPreferredSize(new Dimension(82, 16));

        enemyStats.add(titleEnRow);
        enemyStats.add(Box.createVerticalStrut(10));
        enemyStats.add(crearFilaStat("HP", enemyHpBar, lblEnemyHpText));

        enemyCard.add(enemyStats, BorderLayout.CENTER);
        enemyCard.add(lblAvatarEn, BorderLayout.EAST);

        hudOverlay.add(playerCard, BorderLayout.WEST);
        hudOverlay.add(centerBadge, BorderLayout.CENTER);
        hudOverlay.add(enemyCard, BorderLayout.EAST);

        add(hudOverlay, BorderLayout.NORTH);

        // =====================================================================
        // 2. ESCENARIO DE BATALLA (SPRITES DIBUJADOS EN EL PANEL CENTRAL)
        // =====================================================================
        JPanel battleArena = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarArena(g, getWidth(), getHeight());
            }
        };
        battleArena.setOpaque(false);
        add(battleArena, BorderLayout.CENTER);

        // =====================================================================
        // 3. PANEL INFERIOR DE ACCIONES Y DIÁLOGOS
        // =====================================================================
        MarcoRetroPanel bottomInterface = new MarcoRetroPanel(
                new Color(29, 78, 216), new Color(59, 130, 246),
                new Color(20, 30, 70, 240), new Color(7, 10, 24, 245), true);
        bottomInterface.setLayout(new BorderLayout(0, 8));
        bottomInterface.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));

        JPanel gridPrincipal = new JPanel(new GridLayout(1, 2, 18, 0));
        gridPrincipal.setOpaque(false);

        // Dialog box a la izquierda
        MarcoRetroPanel dialogPanel = new MarcoRetroPanel(
                new Color(37, 99, 235), new Color(59, 130, 246),
                new Color(6, 10, 24, 248), new Color(12, 20, 48, 245), false);
        dialogPanel.setLayout(new BorderLayout());
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        dialogPanel.setPreferredSize(new Dimension(560, 118));

        lblDialogText = new JLabel(
                "<html><font color='#facc15'>▶</font> ¡Nivel 1: Laboratorio UNSA!<br>Un <font color='#facc15'><b>Bug Morado</b></font> salvaje ha colapsado tu servidor de matrícula.<br>¡Debes compilar una solución!</html>");
        lblDialogText.setFont(Recursos.fuentePixel(Font.PLAIN, 10f));
        lblDialogText.setForeground(Color.WHITE);
        dialogPanel.add(lblDialogText, BorderLayout.CENTER);

        // Menus de botones a la derecha (CardLayout: mainActions, skillsMenu,
        // itemsMenu)
        cardAccionesLayout = new CardLayout();
        panelAccionesCard = new JPanel(cardAccionesLayout);
        panelAccionesCard.setOpaque(false);

        JPanel mainActions = new JPanel(new GridLayout(2, 2, 12, 10));
        mainActions.setOpaque(false);
        botonesPrincipales[0] = new BotonRetro("[1] ⚔️ DEPURAR");
        botonesPrincipales[0].setActivo(true);
        botonesPrincipales[0].addActionListener(e -> ejecutarAtaque());

        botonesPrincipales[1] = new BotonRetro("[2] ⚡ HABILIDADES");
        botonesPrincipales[1].addActionListener(e -> abrirSubmenu("skills"));

        botonesPrincipales[2] = new BotonRetro("[3] ☕ ÍTEMS");
        botonesPrincipales[2].addActionListener(e -> abrirSubmenu("items"));

        botonesPrincipales[3] = new BotonRetro("[4] 🏃 HUIR");
        botonesPrincipales[3].addActionListener(e -> huirAlMapa());

        String[] textosAccion = { "[1] DEPURAR", "[2] HABILIDADES", "[3] ITEMS", "[4] HUIR" };
        String[] iconosAccion = { "ui/icono-calavera.png", "ui/icono-mapa.png", "ui/icono-mochila.png",
                "ui/icono-puerta.png" };
        for (int i = 0; i < botonesPrincipales.length; i++) {
            botonesPrincipales[i].setText(textosAccion[i]);
            botonesPrincipales[i].setIcon(Recursos.cargarIcono(iconosAccion[i], 36, 36));
            mainActions.add(botonesPrincipales[i]);
        }

        JPanel skillsMenu = new JPanel(new GridLayout(2, 2, 12, 10));
        skillsMenu.setOpaque(false);
        botonesSkills[0] = new BotonRetro("RAYO UNSA");
        botonesSkills[0].setIcon(Recursos.cargarIcono("sprites/items/rayo.png", 34, 34));
        botonesSkills[0].addActionListener(e -> usarHabilidad("rayo"));
        botonesSkills[1] = new BotonRetro("REFACTOR");
        botonesSkills[1].addActionListener(e -> usarHabilidad("refactor"));
        botonesSkills[2] = new BotonRetro("CURACION");
        botonesSkills[2].setIcon(Recursos.cargarIcono("sprites/items/pergamino-curacion.png", 34, 34));
        botonesSkills[2].addActionListener(e -> usarHabilidad("curar"));
        botonesSkills[3] = new BotonRetro("VOLVER");
        botonesSkills[3].addActionListener(e -> cerrarSubmenus());
        for (BotonRetro boton : botonesSkills)
            skillsMenu.add(boton);

        JPanel itemsMenu = new JPanel(new GridLayout(3, 2, 12, 8));
        itemsMenu.setOpaque(false);
        botonesItems[0] = new BotonRetro("CAFE EXPRESO");
        botonesItems[0].addActionListener(e -> usarItem("cafe"));
        botonesItems[1] = new BotonRetro("ENERGIZANTE");
        botonesItems[1].setIcon(Recursos.cargarIcono("sprites/items/rayo.png", 34, 34));
        botonesItems[1].addActionListener(e -> usarItem("energizante"));
        botonesItems[2] = new BotonRetro("POCION VIDA");
        botonesItems[2].addActionListener(e -> usarItem("pocionVida"));
        botonesItems[3] = new BotonRetro("POCION MANA");
        botonesItems[3].addActionListener(e -> usarItem("pocionMana"));
        botonesItems[4] = new BotonRetro("VOLVER");
        botonesItems[4].addActionListener(e -> cerrarSubmenus());
        for (BotonRetro boton : botonesItems)
            itemsMenu.add(boton);
        JPanel hueco = new JPanel();
        hueco.setOpaque(false);
        itemsMenu.add(hueco);

        panelAccionesCard.add(mainActions, "main");
        panelAccionesCard.add(skillsMenu, "skills");
        panelAccionesCard.add(itemsMenu, "items");

        gridPrincipal.add(dialogPanel);
        gridPrincipal.add(panelAccionesCard);

        JLabel lblKeyboardHelp = new JLabel(
                "[1] Atacar   [2] Habilidades   [3] Ítems   [4] Huir   [Esc] Volver   [WASD] Mover   [Espacio] Saltar",
                SwingConstants.CENTER);
        lblKeyboardHelp.setFont(new Font("Arial", Font.BOLD, 11));
        lblKeyboardHelp.setForeground(new Color(203, 213, 225));

        bottomInterface.add(gridPrincipal, BorderLayout.CENTER);
        bottomInterface.add(lblKeyboardHelp, BorderLayout.SOUTH);

        add(bottomInterface, BorderLayout.SOUTH);

        actualizarUI();
        configurarTeclado();

        gameTimer = temporizador(45, e -> actualizarAnimaciones());
        gameTimer.start();
    }

    private JPanel crearFilaStat(String etiqueta, BarraEstadoPanel barra, JLabel lblVal) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(Recursos.fuentePixel(Font.BOLD, 10f));
        lbl.setForeground(new Color(147, 197, 253));
        lbl.setPreferredSize(new Dimension(28, 16));
        row.add(lbl, BorderLayout.WEST);
        row.add(barra, BorderLayout.CENTER);
        row.add(lblVal, BorderLayout.EAST);
        return row;
    }

    private void actualizarAnimaciones() {
        animTick++;
        if (cafeAnimTicks > 0)
            cafeAnimTicks--;

        if (animDuracionTicks > 0) {
            animDuracionTicks--;
            if (animDuracionTicks == 0 && !enSalto) {
                animRow = 0;
            }
        }

        if (enSalto) {
            saltoTick++;
            if (saltoTick >= 14) {
                enSalto = false;
                saltoTick = 0;
                animRow = 0;
            }
        }

        int velocidadFrame = (animRow == 0) ? 5 : 3;
        if (animTick % velocidadFrame == 0) {
            animCol = (animCol + 1) % 4;
        }

        if (lungeJugadorOffset > 0)
            lungeJugadorOffset = Math.max(0, lungeJugadorOffset - 12);
        if (lungeEnemigoOffset < 0)
            lungeEnemigoOffset = Math.min(0, lungeEnemigoOffset + 12);
        if (hitJugadorFlash > 0)
            hitJugadorFlash--;
        if (hitEnemigoFlash > 0)
            hitEnemigoFlash--;
        if (shakeTicks > 0)
            shakeTicks--;
        if (corteTicks > 0) {
            corteTicks--;
            if (corteTicks == 0)
                mostrarCorte = false;
        }

        for (int i = flotantes.size() - 1; i >= 0; i--) {
            TextoFlotante tf = flotantes.get(i);
            tf.y -= 3;
            tf.vida--;
            if (tf.vida <= 0)
                flotantes.remove(i);
        }

        for (Polvo p : polvos) {
            p.x += p.vx;
            p.y += p.vy;
            if (p.y < 0) {
                p.y = Math.max(600, getHeight());
                p.x = rnd.nextDouble() * Math.max(900, getWidth());
            }
        }

        // Sincronizar indicador de turno
        if (enTurnoJugador) {
            lblTurnIndicator.setText("  ▶ TU TURNO  ");
            lblTurnIndicator.setForeground(new Color(187, 247, 208));
            lblTurnIndicator.setBackground(new Color(5, 46, 22, 220));
            lblTurnIndicator.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(34, 197, 94), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        } else {
            lblTurnIndicator.setText("  … TURNO ENEMIGO  ");
            lblTurnIndicator.setForeground(new Color(254, 202, 202));
            lblTurnIndicator.setBackground(new Color(69, 10, 10, 220));
            lblTurnIndicator.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(239, 68, 68), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        }

        for (BotonRetro b : botonesPrincipales)
            b.setEnabled(enTurnoJugador);
        for (BotonRetro b : botonesSkills)
            b.setEnabled(enTurnoJugador);
        TipoItem[] tiposItems = TipoItem.values();
        for (int i = 0; i < tiposItems.length; i++) {
            botonesItems[i].setEnabled(enTurnoJugador && jugador.getInventario().cantidad(tiposItems[i]) > 0);
        }
        botonesItems[4].setEnabled(enTurnoJugador);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int sx = shakeTicks > 0 ? (rnd.nextInt(11) - 5) : 0;
        int sy = shakeTicks > 0 ? (rnd.nextInt(7) - 3) : 0;
        g2.translate(sx, sy);

        int w = getWidth();
        int h = getHeight();
        if (fondoLab != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(fondoLab, 0, 0, w, h, null);
        } else {
            g2.setColor(new Color(10, 15, 30));
            g2.fillRect(0, 0, w, h);
        }

        // Ambiente morado flotante
        for (Polvo p : polvos) {
            g2.setColor(new Color(196, 181, 253, p.alpha));
            g2.fillRect((int) p.x, (int) p.y, 4, 4);
        }
        g2.dispose();
    }

    private void dibujarArena(Graphics g, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Sombra general del suelo
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillOval((int) (w * 0.08), h - 24, (int) (w * 0.84), 22);

        // --- SPRITE JUGADOR (Spritesheet 4x4) ---
        int saltoOffset = enSalto ? (int) (Math.sin((saltoTick / 14.0) * Math.PI) * 58) : 0;
        int jW = 280;
        int jH = 210;
        int jX = (int) (w * jugadorPosX) + lungeJugadorOffset;
        int jY = h - jH - jugadorPosY - saltoOffset;

        // Sombra bajo jugador
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillOval(jX + 55, h - jugadorPosY - 10, jW - 110, 16);

        BufferedImage sheetJugadorActual = (cafeAnimTicks > 0 && jugadorCafeSheet != null)
                ? jugadorCafeSheet
                : jugadorSheet;
        if (sheetJugadorActual != null) {
            int fw = sheetJugadorActual.getWidth() / 4;
            int fh = sheetJugadorActual.getHeight() / 4;
            int columnaJugador = (cafeAnimTicks > 0) ? ((animTick / 4) % 4) : animCol;
            int filaJugador = (cafeAnimTicks > 0) ? 0 : animRow;
            int srcX = columnaJugador * fw;
            int srcY = filaJugador * fh;

            Graphics2D gj = (Graphics2D) g2.create();
            if (!mirandoDerecha) {
                gj.translate(jX + jW, jY);
                gj.scale(-1, 1);
                gj.drawImage(sheetJugadorActual, 0, 0, jW, jH, srcX, srcY, srcX + fw, srcY + fh, null);
            } else {
                gj.drawImage(sheetJugadorActual, jX, jY, jX + jW, jY + jH, srcX, srcY, srcX + fw, srcY + fh, null);
            }
            if (hitJugadorFlash > 0 && hitJugadorFlash % 2 == 0) {
                gj.setColor(new Color(239, 68, 68, 130));
                gj.fillRect(jX + 50, jY + 20, jW - 100, jH - 30);
            }
            gj.dispose();
        }

        // --- SPRITE ENEMIGO (Bug Morado) ---
        int floatY = (int) (Math.sin(animTick * 0.14) * 10);
        int eW = 200;
        int eH = 200;
        int eX = (int) (w * 0.80) - eW + lungeEnemigoOffset;
        int eY = h - eH - 25 + floatY;

        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillOval(eX + 25, h - 22, eW - 50, 16);

        if (enemigoAnimadoSheet != null) {
            int fw = enemigoAnimadoSheet.getWidth() / 4;
            int fh = enemigoAnimadoSheet.getHeight() / 4;
            int frame = (animTick / 5) % 16;
            int srcX = (frame % 4) * fw;
            int srcY = (frame / 4) * fh;
            g2.drawImage(enemigoAnimadoSheet, eX, eY, eX + eW, eY + eH,
                    srcX, srcY, srcX + fw, srcY + fh, null);
            if (hitEnemigoFlash > 0 && hitEnemigoFlash % 2 == 0) {
                g2.setColor(new Color(255, 255, 255, 160));
                g2.fillRect(eX + 20, eY + 20, eW - 40, eH - 40);
            }
        }

        // Efecto de corte o rayo
        if (mostrarCorte) {
            g2.setFont(new Font("Arial", Font.BOLD, 96));
            g2.setColor(corteMagico ? new Color(191, 219, 254) : new Color(255, 247, 204));
            g2.drawString(corteMagico ? "✦" : "╱", eX + 35, eY + 120);
        }

        // Numeros flotantes de danio / curacion
        g2.setFont(Recursos.fuentePixel(Font.BOLD, 24f));
        for (TextoFlotante tf : flotantes) {
            g2.setColor(Color.BLACK);
            g2.drawString(tf.texto, tf.x + 3, tf.y + 3);
            g2.setColor(tf.color);
            g2.drawString(tf.texto, tf.x, tf.y);
        }

        g2.dispose();
    }

    private void actualizarUI() {
        botonesItems[0].setText("CAFE x" + jugador.getInventario().cantidad(TipoItem.CAFE));
        botonesItems[1].setText("ENERGIZANTE x" + jugador.getInventario().cantidad(TipoItem.ENERGIZANTE));
        botonesItems[0].setEnabled(jugador.getInventario().cantidad(TipoItem.CAFE) > 0);
        botonesItems[1].setEnabled(jugador.getInventario().cantidad(TipoItem.ENERGIZANTE) > 0);
        botonesItems[2].setText("VIDA x" + jugador.getInventario().cantidad(TipoItem.POCION_VIDA));
        botonesItems[3].setText("MANA x" + jugador.getInventario().cantidad(TipoItem.POCION_MANA));
        Habilidad.Tipo[] tipos = Habilidad.Tipo.values();
        for (int i = 0; i < tipos.length; i++) {
            Habilidad h = jugador.getHabilidad(tipos[i]);
            botonesSkills[i].setToolTipText(h.getNombre() + " - " + h.getCostoMp() + " MP");
        }

        hpBar.setValores(jugador.getHp(), jugador.getMaxHp());
        mpBar.setValores(jugador.getMp(), jugador.getMaxMp());
        enemyHpBar.setValores(enemigo.getHp(), enemigo.getMaxHp());

        lblHpText.setText(jugador.getHp() + "/" + jugador.getMaxHp());
        lblMpText.setText(jugador.getMp() + "/" + jugador.getMaxMp());
        lblEnemyHpText.setText(enemigo.getHp() + "/" + enemigo.getMaxHp());

        hpBar.setParpadeo(hpBar.getPorcentaje() <= 30);
        enemyHpBar.setParpadeo(enemyHpBar.getPorcentaje() <= 30);
    }

    private void mostrarDanioFlotante(String texto, boolean enEnemigo, Color color) {
        int w = Math.max(960, getWidth());
        int h = Math.max(400, getHeight() - 250);
        TextoFlotante tf = new TextoFlotante();
        tf.texto = texto;
        tf.x = enEnemigo ? (int) (w * 0.72) : (int) (w * 0.24);
        tf.y = h - 190;
        tf.vida = 22;
        tf.color = color;
        flotantes.add(tf);
    }

    private void mostrarMensaje(String htmlTexto) {
        lblDialogText.setText("<html><font color='#facc15'>▶</font> " + htmlTexto + "</html>");
    }

    private void abrirSubmenu(String menu) {
        if (!enTurnoJugador)
            return;
        cardAccionesLayout.show(panelAccionesCard, menu);
        requestFocusInWindow();
    }

    private void cerrarSubmenus() {
        cardAccionesLayout.show(panelAccionesCard, "main");
        requestFocusInWindow();
    }

    // 1. DEPURAR (Ataque basico = 15 danio)
    private void ejecutarAtaque() {
        if (!enTurnoJugador)
            return;
        enTurnoJugador = false;
        lungeJugadorOffset = 80;
        animRow = 2;
        animCol = 0;
        animDuracionTicks = 12;
        mostrarCorte = true;
        corteMagico = false;
        corteTicks = 10;
        Recursos.playAttack();

        Timer t = temporizador(400, e -> {
            ((Timer) e.getSource()).stop();
            int danio = jugador.atacar(enemigo);
            hitEnemigoFlash = 8;
            shakeTicks = 6;
            Recursos.playHit();
            actualizarUI();
            mostrarDanioFlotante("-" + danio, true, new Color(239, 68, 68));
            mostrarMensaje(
                    "¡Ejecutaste ⚔️ <font color='#facc15'><b>DEPURAR</b></font>! Infligiste <font color='#facc15'><b>"
                            + danio + "</b></font> de daño.");
            verificarTurnoEnemigo();
        });
        t.setRepeats(false);
        t.start();
    }

    // 2. HABILIDADES (rayo: 25 MP, refactor: 30 MP, curar: 10 MP -> +30 HP)
    private void usarHabilidad(String tipo) {
        if (!enTurnoJugador)
            return;
        Habilidad.Tipo seleccion = "rayo".equals(tipo) ? Habilidad.Tipo.RAYO
                : "refactor".equals(tipo) ? Habilidad.Tipo.REFACTORIZAR : Habilidad.Tipo.DORMIR;
        Habilidad habilidad = jugador.getHabilidad(seleccion);
        if (jugador.getMp() < habilidad.getCostoMp()) {
            mostrarMensaje("MP insuficiente. Requieres " + habilidad.getCostoMp() + " MP.");
            return;
        }
        if (seleccion == Habilidad.Tipo.DORMIR && jugador.getHp() == jugador.getMaxHp()) {
            mostrarMensaje("Tu vida ya esta completa.");
            return;
        }

        enTurnoJugador = false;
        cerrarSubmenus();

        if ("curar".equals(tipo)) {
            int recuperado = jugador.usarHabilidad(seleccion, enemigo);
            Recursos.playCoin();
            actualizarUI();
            mostrarDanioFlotante("+" + recuperado, false, new Color(34, 197, 94));
            mostrarMensaje("Usaste 💤 <font color='#facc15'><b>Siesta EPIS</b></font>. Recibes salud.");
            verificarTurnoEnemigo();
        } else {
            animRow = 2;
            animCol = 0;
            animDuracionTicks = 14;
            mostrarCorte = true;
            corteMagico = true;
            corteTicks = 12;
            Recursos.playAttack();

            Timer t = temporizador(480, e -> {
                ((Timer) e.getSource()).stop();
                int danio = jugador.usarHabilidad(seleccion, enemigo);
                hitEnemigoFlash = 8;
                shakeTicks = 6;
                Recursos.playHit();
                actualizarUI();
                mostrarDanioFlotante("-" + danio, true, new Color(239, 68, 68));
                mostrarMensaje("Lanzaste la habilidad e infligiste <font color='#facc15'><b>" + danio
                        + "</b></font> de daño.");
                verificarTurnoEnemigo();
            });
            t.setRepeats(false);
            t.start();
        }
    }

    // 3. ITEMS (cafe: +40 HP, energizante: +35 MP)
    private void usarItem(String tipo) {
        if (!enTurnoJugador)
            return;
        TipoItem item;
        switch (tipo) {
            case "cafe":
                item = TipoItem.CAFE;
                break;
            case "energizante":
                item = TipoItem.ENERGIZANTE;
                break;
            case "pocionVida":
                item = TipoItem.POCION_VIDA;
                break;
            case "pocionMana":
                item = TipoItem.POCION_MANA;
                break;
            default:
                throw new IllegalArgumentException("Item desconocido: " + tipo);
        }
        int recuperado = jugador.usarItem(item);
        if (recuperado <= 0) {
            mostrarMensaje(recuperado < 0 ? "No quedan unidades de " + item.getNombre() + "."
                    : "La barra ya esta llena. No se consumio el objeto.");
            return;
        }
        enTurnoJugador = false;
        cerrarSubmenus();
        Recursos.playCoin();
        if (item == TipoItem.CAFE) {
            cafeAnimTicks = 54;
            animCol = 0;
        }
        mostrarDanioFlotante("+" + recuperado, false,
                item == TipoItem.CAFE ? new Color(34, 197, 94) : new Color(59, 130, 246));
        mostrarMensaje("Usaste " + item.getNombre() + ". Quedan " + jugador.getInventario().cantidad(item) + ".");
        actualizarUI();
        verificarTurnoEnemigo();
    }

    // RESPUESTA DEL ENEMIGO (8 danio)
    private void verificarTurnoEnemigo() {
        if (enemigo.getHp() <= 0) {
            enemigo.entregarRecompensa(jugador);
            partida.premiar("victoria-laboratorio", 50);
            mostrarMensaje("🎉 ¡VICTORIA! Has depurado el Bug Morado.");
            Timer winT = temporizador(1100, e -> {
                ((Timer) e.getSource()).stop();
                mostrarPantallaFin(true,
                        "Depuraste el <b>Bug Morado</b> y el servidor de matrícula vuelve a la vida.<br><br>¡Nivel 1 completado!",
                        () -> ventana.mostrarNivel2());
            });
            winT.setRepeats(false);
            winT.start();
            return;
        }

        Timer enemyDelay = temporizador(650, e -> {
            ((Timer) e.getSource()).stop();
            lungeEnemigoOffset = -66;
            Timer hitT = temporizador(380, ev -> {
                ((Timer) ev.getSource()).stop();
                int danioEnemigo = enemigo.atacar(jugador);
                hitJugadorFlash = 8;
                shakeTicks = 6;
                animRow = 3;
                animCol = 0;
                animDuracionTicks = 11;
                Recursos.playHit();
                actualizarUI();
                mostrarDanioFlotante("-" + danioEnemigo, false, new Color(255, 83, 100));
                mostrarMensaje(
                        "El 👾 <font color='#facc15'><b>Bug Morado</b></font> contraatacó e infligió <font color='#facc15'><b>"
                                + danioEnemigo + "</b></font> de daño.");

                if (jugador.getHp() <= 0) {
                    Timer loseT = temporizador(900, ev2 -> {
                        ((Timer) ev2.getSource()).stop();
                        mostrarPantallaFin(false,
                                "El Bug Morado colapsó tu sesión.<br><br>Compila otra vez y vuelve a intentarlo.",
                                () -> ventana.nuevaPartida());
                    });
                    loseT.setRepeats(false);
                    loseT.start();
                } else {
                    enTurnoJugador = true;
                }
            });
            hitT.setRepeats(false);
            hitT.start();
        });
        enemyDelay.setRepeats(false);
        enemyDelay.start();
    }

    private void huirAlMapa() {
        if (!enTurnoJugador)
            return;
        int r = JOptionPane.showConfirmDialog(this, "¿Deseas huir al mapa general?", "Huir", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            ventana.mostrarMenuPrincipal();
        }
        requestFocusInWindow();
    }

    private void mostrarPantallaFin(boolean victoria, String mensajeHtml, Runnable alContinuar) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), victoria ? "¡VICTORIA!" : "HAS CAÍDO",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);

        Color bExt = victoria ? new Color(29, 78, 216) : new Color(185, 28, 28);
        Color bInt = victoria ? new Color(250, 204, 21) : new Color(239, 68, 68);
        MarcoRetroPanel box = new MarcoRetroPanel(bExt, bInt, new Color(20, 30, 70), new Color(7, 10, 24), true);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createEmptyBorder(28, 26, 26, 26));
        box.setPreferredSize(new Dimension(460, 290));

        JLabel lblIcon = new JLabel(victoria ? "🏆" : "💀", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(victoria ? "¡VICTORIA!" : "HAS CAÍDO", SwingConstants.CENTER);
        lblTitle.setFont(Recursos.fuentePixel(Font.BOLD, 22f));
        lblTitle.setForeground(victoria ? new Color(250, 204, 21) : new Color(248, 113, 113));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMsg = new JLabel("<html><center>" + mensajeHtml + "</center></html>", SwingConstants.CENTER);
        lblMsg.setFont(Recursos.fuentePixel(Font.PLAIN, 10f));
        lblMsg.setForeground(new Color(203, 213, 225));
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        BotonRetro btnEnd = new BotonRetro(victoria ? "CONTINUAR ▶" : "REINTENTAR ↻");
        btnEnd.setHorizontalAlignment(SwingConstants.CENTER);
        btnEnd.setMaximumSize(new Dimension(380, 44));
        btnEnd.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEnd.addActionListener(e -> {
            dlg.dispose();
            alContinuar.run();
        });

        box.add(lblIcon);
        box.add(Box.createVerticalStrut(10));
        box.add(lblTitle);
        box.add(Box.createVerticalStrut(14));
        box.add(lblMsg);
        box.add(Box.createVerticalGlue());
        box.add(btnEnd);

        dlg.setContentPane(box);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void configurarTeclado() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                // Movimiento libre WASD / Flechas + Espacio saltar
                if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                    moverJugador(-0.022, 0, e.isShiftDown());
                    return;
                }
                if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                    moverJugador(0.022, 0, e.isShiftDown());
                    return;
                }
                if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                    moverJugador(0, 9, e.isShiftDown());
                    return;
                }
                if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                    moverJugador(0, -9, e.isShiftDown());
                    return;
                }
                if (code == KeyEvent.VK_SPACE) {
                    if (enTurnoJugador && !enSalto) {
                        enSalto = true;
                        saltoTick = 0;
                        animRow = 1;
                    }
                    return;
                }
                if (code == KeyEvent.VK_ESCAPE) {
                    cerrarSubmenus();
                    return;
                }
                if (code == KeyEvent.VK_1 || code == KeyEvent.VK_NUMPAD1)
                    ejecutarAtaque();
                if (code == KeyEvent.VK_2 || code == KeyEvent.VK_NUMPAD2)
                    abrirSubmenu("skills");
                if (code == KeyEvent.VK_3 || code == KeyEvent.VK_NUMPAD3)
                    abrirSubmenu("items");
                if (code == KeyEvent.VK_4 || code == KeyEvent.VK_NUMPAD4)
                    huirAlMapa();
            }
        });
    }

    private void moverJugador(double dx, int dy, boolean correr) {
        jugadorPosX = Math.max(0.02, Math.min(0.65, jugadorPosX + dx));
        jugadorPosY = Math.max(5, Math.min(42, jugadorPosY + dy));
        if (dx < 0)
            mirandoDerecha = false;
        if (dx > 0)
            mirandoDerecha = true;
        animRow = 1;
        animDuracionTicks = correr ? 9 : 11;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        actualizarUI();
    }

}

package frontend;

import backend.Partida;
import backend.Jugador;
import backend.TipoItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class Nivel2Panel extends PanelJuego {
    private final VentanaPrincipal ventana;

    private static final int WORLD_WIDTH = 5200;
    private static final int WORLD_HEIGHT = 540;
    private static final int VIEW_W = 960;
    private static final int VIEW_H = 540;

    // Sprites y animaciones del segundo nivel
    private final BufferedImage kaiSheet;
    private final BufferedImage bookSheet;
    private final BufferedImage batSheet;
    private final BufferedImage golemSheet;
    private final BufferedImage bossSheet;

    // Estado del jugador exacto
    private double px = 80, py = 380;
    private final int pw = 42, ph = 58;
    private double vx = 0, vy = 0;
    private final double baseSpeed = 4.5, runSpeed = 6.8, jumpForce = -13.5, gravity = 0.58;
    private boolean isGrounded = false;
    private boolean dirRight = true;
    private String animState = "idle";
    private int animFrame = 0, animTimer = 0;
    private final Partida partida;
    private final Jugador jugador;
    private final int totalBooks = 5;
    private boolean isAttacking = false;
    private int attackTimer = 0;
    private int invulnerableTimer = 0;
    private double checkpointX = 80, checkpointY = 380;

    private double camX = 0;
    private int tick = 0;
    private int shakeTimer = 0;
    private boolean juegoPausado = false;
    private final Set<Integer> keysDown = new HashSet<>();

    // HUD Swing
    private final BarraEstadoPanel hpBar;
    private final JLabel lblHpText;
    private final JLabel[] bookSlots = new JLabel[5];
    private final JLabel lblBooksCount;
    private final JLabel lblZoneText;
    private final JLabel lblKeyStatus;

    private final MarcoRetroPanel bossHudPanel;
    private final BarraEstadoPanel bossHpBar;
    private final MarcoRetroPanel dialogOverlay;
    private final JLabel lblDialogText;
    private Timer dialogFadeTimer;
    private Timer gameLoopTimer;

    private static class Zona {
        int startX, endX;
        String name;

        Zona(int s, int e, String n) {
            startX = s;
            endX = e;
            name = n;
        }
    }

    private static class Plataforma {
        int x, y, w, h;
        String type;

        Plataforma(int x, int y, int w, int h, String t) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.type = t;
        }
    }

    private static class Libro {
        int x, y;
        String name;

        Libro(int x, int y, String n) {
            this.x = x;
            this.y = y;
            this.name = n;
        }
    }

    private static class EnemigoVista {
        final backend.Enemigo modelo;
        String type;
        BufferedImage sheet;
        double x, y, baseY, speed;
        int minX, maxX, dir, w, h, frame, animTimer, totalFrames, frameSpeed, shootTimer;
        double angle;

        EnemigoVista(String type, BufferedImage sheet, double x, double y, int minX, int maxX, double speed, int dir,
                int w, int h, backend.Enemigo modelo, int totalFrames, int frameSpeed) {
            this.type = type;
            this.sheet = sheet;
            this.x = x;
            this.y = y;
            this.baseY = y;
            this.minX = minX;
            this.maxX = maxX;
            this.speed = speed;
            this.dir = dir;
            this.w = w;
            this.h = h;
            this.modelo = modelo;
            this.totalFrames = totalFrames;
            this.frameSpeed = frameSpeed;
        }
    }

    private static class Proyectil {
        double x, y, vx, vy;
        Color color;
        int radius;

        Proyectil(double x, double y, double vx, double vy, Color c, int r) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = c;
            this.radius = r;
        }
    }

    private static class Particula {
        double x, y, vx, vy, size;
        Color color;
        int life;
    }

    private static class Floater {
        double x, y;
        String text;
        Color color;
        int life;
    }

    private final List<Zona> zones = Arrays.asList(
            new Zona(0, 1000, "1. Gran Vestíbulo de Entrada"),
            new Zona(1000, 2100, "2. Laberinto de Estanterías"),
            new Zona(2100, 3200, "3. Archivo Prohibido de Algoritmos"),
            new Zona(3200, 4200, "4. Puentes Elevados de la UNSA"),
            new Zona(4200, 5200, "5. Sanctum del Guardián Ancestral"));

    private final List<Plataforma> platforms = Arrays.asList(
            new Plataforma(0, 460, 950, 80, "stone"),
            new Plataforma(260, 360, 140, 20, "wood"),
            new Plataforma(480, 280, 160, 20, "wood"),
            new Plataforma(720, 210, 150, 20, "wood"),
            new Plataforma(1050, 460, 980, 80, "stone"),
            new Plataforma(1150, 370, 180, 20, "bookshelf"),
            new Plataforma(1400, 290, 200, 20, "bookshelf"),
            new Plataforma(1680, 220, 180, 20, "bookshelf"),
            new Plataforma(1380, 150, 160, 20, "wood"),
            new Plataforma(1880, 340, 140, 20, "wood"),
            new Plataforma(2150, 460, 420, 80, "stone"),
            new Plataforma(2700, 460, 450, 80, "stone"),
            new Plataforma(2280, 360, 140, 20, "wood"),
            new Plataforma(2480, 280, 150, 20, "wood"),
            new Plataforma(2750, 210, 160, 20, "wood"),
            new Plataforma(2950, 330, 150, 20, "wood"),
            new Plataforma(3250, 460, 880, 80, "stone"),
            new Plataforma(3350, 370, 140, 18, "bridge"),
            new Plataforma(3560, 300, 150, 18, "bridge"),
            new Plataforma(3780, 230, 160, 18, "bridge"),
            new Plataforma(4000, 170, 140, 18, "bridge"),
            new Plataforma(4280, 460, 920, 80, "stone"),
            new Plataforma(4420, 350, 180, 20, "stone"),
            new Plataforma(4800, 350, 180, 20, "stone"));

    private final List<Libro> books = Arrays.asList(
            new Libro(520, 240, "Algoritmos y Complejidad"),
            new Libro(1480, 110, "Estructuras de Datos UNSA"),
            new Libro(2520, 240, "Bases de Datos Avanzadas"),
            new Libro(3820, 190, "Sistemas Distribuidos"),
            new Libro(4880, 310, "Tesis Final EPIS"));

    private final Rectangle exitPortal = new Rectangle(5080, 370, 60, 90);
    private final List<EnemigoVista> enemies = new ArrayList<>();

    // Mini Jefe exacto
    private boolean bossActive = false;
    private double bossX = 4700, bossY = 310;
    private final int bossW = 120, bossH = 150;
    private final double bossSpeed = 3.0;
    private int bossDir = -1;
    private final backend.Enemigo guardian;
    private int bossAttackTimer = 0, bossFrame = 0, bossAnimTimer = 0;

    private final List<Proyectil> projectiles = new ArrayList<>();
    private final List<Particula> particles = new ArrayList<>();
    private final List<Floater> floaters = new ArrayList<>();
    private final Random rnd = new Random();
    private final FondoBiblioteca fondo = new FondoBiblioteca();

    public Nivel2Panel(VentanaPrincipal ventana) {
        this(ventana, ventana == null ? new Partida() : ventana.getPartida());
    }

    public Nivel2Panel(VentanaPrincipal ventana, Partida partida) {
        this.ventana = ventana;
        this.partida = partida;
        this.jugador = partida.getJugador();
        this.guardian = partida.getGuardianBiblioteca();
        checkpointX = partida.getPuntoX();
        checkpointY = partida.getPuntoY();
        px = checkpointX;
        py = checkpointY;
        camX = Math.max(0, Math.min(WORLD_WIDTH - VIEW_W, px - VIEW_W / 2.0));
        this.kaiSheet = Recursos.cargarImagen("sprites/jugador-sheet.png");
        this.bookSheet = Recursos.cargarImagen("sprites/enemigo-libro-exacto.png");
        this.batSheet = Recursos.cargarImagen("sprites/enemigo-murcielago-exacto.png");
        this.golemSheet = Recursos.cargarImagen("sprites/enemigo-golem-exacto.png");
        this.bossSheet = Recursos.cargarImagen("sprites/mini-jefe-exacto.png");

        enemies.add(new EnemigoVista("book_guardian", bookSheet, 550, 396, 450, 850, 1.8, 1, 64, 64,
                partida.getEnemigosBiblioteca().get(0), 3, 8));
        enemies.add(new EnemigoVista("book_guardian", bookSheet, 1450, 226, 1400, 1580, 2.0, 1, 64, 64,
                partida.getEnemigosBiblioteca().get(1), 3, 8));
        enemies.add(new EnemigoVista("book_guardian", bookSheet, 3400, 396, 3300, 3650, 2.2, -1, 64, 64,
                partida.getEnemigosBiblioteca().get(2), 3, 8));

        enemies.add(new EnemigoVista("ink_bat", batSheet, 1250, 200, 0, 0, 2.4, 1, 68, 52,
                partida.getEnemigosBiblioteca().get(3), 3, 6));
        enemies.add(new EnemigoVista("ink_bat", batSheet, 2600, 180, 0, 0, 2.6, -1, 68, 52,
                partida.getEnemigosBiblioteca().get(4), 3, 6));
        enemies.add(new EnemigoVista("ink_bat", batSheet, 3650, 170, 0, 0, 2.8, 1, 68, 52,
                partida.getEnemigosBiblioteca().get(5), 3, 6));

        enemies.add(new EnemigoVista("paper_golem", golemSheet, 2300, 386, 2200, 2420, 0.9, 1, 68, 74,
                partida.getEnemigosBiblioteca().get(6), 2, 12));
        enemies.add(new EnemigoVista("paper_golem", golemSheet, 3800, 386, 3750, 3950, 0.9, -1, 68, 74,
                partida.getEnemigosBiblioteca().get(7), 2, 12));

        setLayout(new BorderLayout(0, 10));
        setBackground(new Color(11, 9, 20));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        setFocusable(true);

        // =====================================================================
        // 1. HUD SUPERIOR (5 TARJETAS)
        // =====================================================================
        MarcoRetroPanel hudBar = new MarcoRetroPanel(
                new Color(91, 33, 182), new Color(139, 92, 246),
                new Color(40, 24, 90, 235), new Color(15, 12, 30, 245), false);
        hudBar.setLayout(new GridLayout(1, 5, 10, 0));
        hudBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // Card 1: Heroe
        JPanel cardHero = new JPanel(new BorderLayout(8, 0));
        cardHero.setOpaque(false);
        JLabel lblHeroAvatar = new JLabel(Recursos.cargarIcono("sprites/avatar-jugador.png", 38, 38));
        lblHeroAvatar.setBorder(BorderFactory.createLineBorder(new Color(250, 204, 21), 2));
        JPanel heroTxt = new JPanel(new GridLayout(2, 1));
        heroTxt.setOpaque(false);
        JLabel l1 = new JLabel("HÉROE EPIS");
        l1.setFont(Recursos.fuentePixel(Font.PLAIN, 7f));
        l1.setForeground(new Color(196, 181, 253));
        JLabel l2 = new JLabel("KAI");
        l2.setFont(Recursos.fuentePixel(Font.BOLD, 10f));
        l2.setForeground(new Color(245, 158, 11));
        heroTxt.add(l1);
        heroTxt.add(l2);
        cardHero.add(lblHeroAvatar, BorderLayout.WEST);
        cardHero.add(heroTxt, BorderLayout.CENTER);

        // Card 2: Vida
        JPanel cardHp = new JPanel(new BorderLayout(0, 4));
        cardHp.setOpaque(false);
        JPanel hpTop = new JPanel(new BorderLayout());
        hpTop.setOpaque(false);
        JLabel lblVida = new JLabel("❤ VIDA");
        lblVida.setFont(Recursos.fuentePixel(Font.BOLD, 8f));
        lblVida.setForeground(new Color(196, 181, 253));
        lblHpText = new JLabel("100/100");
        lblHpText.setFont(Recursos.fuentePixel(Font.BOLD, 8f));
        lblHpText.setForeground(Color.WHITE);
        hpTop.add(lblVida, BorderLayout.WEST);
        hpTop.add(lblHpText, BorderLayout.EAST);
        hpBar = new BarraEstadoPanel(new Color(251, 113, 133), new Color(220, 38, 38), new Color(153, 27, 27),
                new Color(76, 29, 149));
        cardHp.add(hpTop, BorderLayout.NORTH);
        cardHp.add(hpBar, BorderLayout.CENTER);

        // Card 3: Libros Sagrados
        JPanel cardBooks = new JPanel(new GridLayout(3, 1, 0, 2));
        cardBooks.setOpaque(false);
        JLabel lblLibTitle = new JLabel("LIBROS SAGRADOS");
        lblLibTitle.setFont(Recursos.fuentePixel(Font.PLAIN, 7f));
        lblLibTitle.setForeground(new Color(196, 181, 253));
        JPanel slotsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        slotsRow.setOpaque(false);
        for (int i = 0; i < 5; i++) {
            bookSlots[i] = new JLabel();
            bookSlots[i].setPreferredSize(new Dimension(20, 20));
            bookSlots[i].setOpaque(true);
            bookSlots[i].setBackground(new Color(8, 47, 73));
            bookSlots[i].setBorder(BorderFactory.createLineBorder(new Color(22, 78, 99), 2));
            slotsRow.add(bookSlots[i]);
        }
        lblBooksCount = new JLabel("0 / 5");
        lblBooksCount.setFont(Recursos.fuentePixel(Font.BOLD, 8f));
        lblBooksCount.setForeground(new Color(34, 211, 238));
        cardBooks.add(lblLibTitle);
        cardBooks.add(slotsRow);
        cardBooks.add(lblBooksCount);

        // Card 4: Zona
        JPanel cardZone = new JPanel(new GridLayout(2, 1));
        cardZone.setOpaque(false);
        JLabel lblZTitle = new JLabel("ZONA");
        lblZTitle.setFont(Recursos.fuentePixel(Font.PLAIN, 7f));
        lblZTitle.setForeground(new Color(196, 181, 253));
        lblZoneText = new JLabel("1. Gran Vestíbulo de Entrada");
        lblZoneText.setFont(Recursos.fuentePixel(Font.BOLD, 8f));
        lblZoneText.setForeground(new Color(216, 180, 254));
        cardZone.add(lblZTitle);
        cardZone.add(lblZoneText);

        // Card 5: Portal
        JPanel cardPortal = new JPanel(new GridLayout(2, 1));
        cardPortal.setOpaque(false);
        JLabel lblPTitle = new JLabel("PORTAL");
        lblPTitle.setFont(Recursos.fuentePixel(Font.PLAIN, 7f));
        lblPTitle.setForeground(new Color(196, 181, 253));
        lblKeyStatus = new JLabel("SELLADO", Recursos.cargarIcono("sprites/items/llave-f.png", 22, 22),
                SwingConstants.LEFT);
        lblKeyStatus.setFont(Recursos.fuentePixel(Font.BOLD, 9f));
        lblKeyStatus.setForeground(new Color(248, 113, 113));
        cardPortal.add(lblPTitle);
        cardPortal.add(lblKeyStatus);

        hudBar.add(cardHero);
        hudBar.add(cardHp);
        hudBar.add(cardBooks);
        hudBar.add(cardZone);
        hudBar.add(cardPortal);

        add(hudBar, BorderLayout.NORTH);

        // =====================================================================
        // 2. CANVAS PRINCIPAL DEL JUEGO + BOSS HUD + DIALOGO SUPERPUESTO
        // =====================================================================
        JPanel canvasWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderMundo(g, getWidth(), getHeight());
            }
        };
        canvasWrapper.setBackground(new Color(5, 4, 10));
        canvasWrapper.setBorder(BorderFactory.createLineBorder(new Color(91, 33, 182), 3));

        // Boss HUD
        bossHudPanel = new MarcoRetroPanel(
                new Color(236, 72, 153), new Color(157, 23, 77),
                new Color(20, 8, 38, 235), new Color(20, 8, 38, 235), false);
        bossHudPanel.setLayout(new BorderLayout(0, 6));
        bossHudPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        bossHudPanel.setVisible(false);

        JLabel lblBossTitle = new JLabel("👾 GUARDIÁN ANCESTRAL DE LA UNSA", SwingConstants.CENTER);
        lblBossTitle.setFont(Recursos.fuentePixel(Font.BOLD, 9f));
        lblBossTitle.setForeground(new Color(249, 168, 212));
        bossHpBar = new BarraEstadoPanel(new Color(240, 171, 252), new Color(192, 38, 211), new Color(126, 34, 206),
                new Color(157, 23, 77));
        bossHudPanel.add(lblBossTitle, BorderLayout.NORTH);
        bossHudPanel.add(bossHpBar, BorderLayout.CENTER);

        JPanel topOverlay = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
        topOverlay.setOpaque(false);
        bossHudPanel.setPreferredSize(new Dimension(520, 56));
        topOverlay.add(bossHudPanel);
        canvasWrapper.add(topOverlay, BorderLayout.NORTH);

        // Dialog Overlay
        dialogOverlay = new MarcoRetroPanel(
                new Color(139, 92, 246), new Color(30, 18, 69),
                new Color(25, 15, 55, 240), new Color(10, 7, 24, 240), false);
        dialogOverlay.setLayout(new BorderLayout());
        dialogOverlay.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        lblDialogText = new JLabel(
                "<html>📖 <font color='#facc15'><b>BIENVENIDO A LA BIBLIOTECA PERDIDA DE LA UNSA</b></font><br>Usa <b>A/D</b> o <b>flechas</b> para correr, <b>W/Espacio</b> para saltar y <b>Shift</b> para acelerar. ¡Encuentra los 5 tomos de código y vence al Guardián!</html>");
        lblDialogText.setFont(Recursos.fuentePixel(Font.PLAIN, 9f));
        lblDialogText.setForeground(new Color(233, 213, 255));
        dialogOverlay.add(lblDialogText, BorderLayout.CENTER);

        JPanel bottomOverlay = new JPanel(new BorderLayout());
        bottomOverlay.setOpaque(false);
        bottomOverlay.setBorder(BorderFactory.createEmptyBorder(0, 20, 14, 20));
        bottomOverlay.add(dialogOverlay, BorderLayout.CENTER);
        canvasWrapper.add(bottomOverlay, BorderLayout.SOUTH);

        add(canvasWrapper, BorderLayout.CENTER);

        // =====================================================================
        // 3. BARRA INFERIOR DE CONTROLES
        // =====================================================================
        MarcoRetroPanel controlsBar = new MarcoRetroPanel(
                new Color(76, 29, 149), new Color(124, 58, 237),
                new Color(26, 18, 56), new Color(14, 10, 32), false);
        controlsBar.setLayout(new BorderLayout());
        controlsBar.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        JLabel lblKeys = new JLabel("[A/D] Mover  [Espacio] Saltar  [Shift] Correr  [J/Z] Atacar  [I] Inventario");
        lblKeys.setFont(Recursos.fuentePixel(Font.PLAIN, 8f));
        lblKeys.setForeground(new Color(203, 213, 225));

        BotonRetro btnMenu = new BotonRetro("🏰 MENÚ");
        btnMenu.setEstiloMorado();
        btnMenu.setHorizontalAlignment(SwingConstants.CENTER);
        btnMenu.addActionListener(e -> {
            keysDown.clear();
            ventana.mostrarMenuPrincipal();
        });

        controlsBar.add(lblKeys, BorderLayout.WEST);
        lblMonedas.setForeground(new Color(250, 204, 21));
        lblMonedas.setHorizontalAlignment(SwingConstants.CENTER);
        controlsBar.add(lblMonedas, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        BotonRetro btnInventario = new BotonRetro("INVENTARIO [I]");
        btnInventario.addActionListener(e -> abrirInventario());
        acciones.add(btnInventario);
        acciones.add(btnMenu);
        controlsBar.add(acciones, BorderLayout.EAST);

        add(controlsBar, BorderLayout.SOUTH);

        configurarTeclado();
        iniciarTimerDialogo();

        gameLoopTimer = temporizador(16, e -> {
            update();
            repaint();
        });
        actualizarHUD();
        gameLoopTimer.start();
    }

    private void iniciarTimerDialogo() {
        if (dialogFadeTimer != null)
            dialogFadeTimer.stop();
        dialogFadeTimer = temporizador(6000, e -> dialogOverlay.setVisible(false));
        dialogFadeTimer.setRepeats(false);
        dialogFadeTimer.start();
    }

    private void mostrarDialogo(String html) {
        lblDialogText.setText("<html>" + html + "</html>");
        dialogOverlay.setVisible(true);
        iniciarTimerDialogo();
    }

    private void configurarTeclado() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (juegoPausado)
                    return;
                if (code == KeyEvent.VK_I) {
                    abrirInventario();
                    return;
                }
                keysDown.add(code);

                if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    if (isGrounded && !juegoPausado) {
                        vy = jumpForce;
                        isGrounded = false;
                        Recursos.playJump();
                        addParticles(px + pw / 2.0, py + ph, new Color(167, 139, 250), 6);
                    }
                }

                if ((code == KeyEvent.VK_J || code == KeyEvent.VK_Z) && !isAttacking && !juegoPausado) {
                    isAttacking = true;
                    attackTimer = 18;
                    Recursos.playAttack();
                    Rectangle attackBox = new Rectangle(
                            dirRight ? (int) (px + pw) : (int) (px - 45),
                            (int) py, 45, ph);
                    for (EnemigoVista en : enemies) {
                        if (en.modelo.getHp() > 0
                                && attackBox.intersects(new Rectangle((int) en.x, (int) en.y, en.w, en.h))) {
                            int danio = jugador.atacar(en.modelo);
                            en.modelo.entregarRecompensa(jugador);
                            addFloater(en.x + en.w / 2.0, en.y - 12,
                                    en.modelo.getHp() <= 0 ? "¡DERROTADO!" : "-" + danio, new Color(253, 224, 71), 36);
                            addParticles(en.x + en.w / 2.0, en.y + en.h / 2.0, new Color(239, 68, 68), 10);
                            Recursos.playHit();
                        }
                    }
                    if (bossActive && guardian.estaVivo()) {
                        if (attackBox.intersects(new Rectangle((int) bossX, (int) bossY, bossW, bossH))) {
                            int danio = jugador.atacar(guardian);
                            addFloater(bossX + bossW / 2.0, bossY - 12, "-" + danio, new Color(236, 72, 153), 36);
                            addParticles(bossX + bossW / 2.0, bossY + bossH / 2.0, new Color(236, 72, 153), 14);
                            Recursos.playHit();
                            actualizarHUD();
                            if (guardian.getHp() <= 0) {
                                guardian.entregarRecompensa(jugador);
                                bossActive = false;
                                Recursos.playBossDie();
                                bossHudPanel.setVisible(false);
                                mostrarDialogo(
                                        "🎉 ¡Derrotaste al Guardián! Recupera los 5 libros para abrir el portal de salida.");
                            }
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keysDown.remove(e.getKeyCode());
            }
        });
    }

    private void abrirInventario() {
        if (juegoPausado || !jugador.estaVivo())
            return;
        juegoPausado = true;
        keysDown.clear();
        try {
            DialogoInventario.mostrar(this, jugador, this::actualizarHUD);
        } finally {
            keysDown.clear();
            juegoPausado = false;
            requestFocusInWindow();
        }
    }

    @Override
    public void removeNotify() {
        keysDown.clear();
        super.removeNotify();
    }

    private void addParticles(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            Particula pt = new Particula();
            pt.x = x;
            pt.y = y;
            pt.vx = (rnd.nextDouble() - 0.5) * 6;
            pt.vy = (rnd.nextDouble() - 0.5) * 6;
            pt.size = rnd.nextDouble() * 4 + 2;
            pt.color = color;
            pt.life = 25;
            particles.add(pt);
        }
    }

    private void addFloater(double x, double y, String text, Color color, int life) {
        Floater f = new Floater();
        f.x = x;
        f.y = y;
        f.text = text;
        f.color = color;
        f.life = life;
        floaters.add(f);
    }

    private void update() {
        if (juegoPausado)
            return;
        tick++;
        if (shakeTimer > 0)
            shakeTimer--;
        double moveSpeed = keysDown.contains(KeyEvent.VK_SHIFT) ? runSpeed : baseSpeed;
        vx = 0;
        if (keysDown.contains(KeyEvent.VK_A) || keysDown.contains(KeyEvent.VK_LEFT)) {
            vx = -moveSpeed;
            dirRight = false;
        }
        if (keysDown.contains(KeyEvent.VK_D) || keysDown.contains(KeyEvent.VK_RIGHT)) {
            vx = moveSpeed;
            dirRight = true;
        }

        vy += gravity;
        px += vx;
        px = Math.max(0, Math.min(WORLD_WIDTH - pw, px));
        py += vy;
        isGrounded = false;

        for (Plataforma plat : platforms) {
            if (px + pw > plat.x && px < plat.x + plat.w &&
                    py + ph >= plat.y && py + ph - vy <= plat.y + 12) {
                py = plat.y - ph;
                vy = 0;
                isGrounded = true;
            }
        }

        if (py > WORLD_HEIGHT + 60) {
            recibirDanio(25);
            px = checkpointX;
            py = checkpointY;
            vy = 0;
            mostrarDialogo("⚠️ ¡Caíste al vacío! Regresas al último punto seguro.");
        }

        if (isAttacking) {
            attackTimer--;
            if (attackTimer <= 0)
                isAttacking = false;
        }
        if (invulnerableTimer > 0)
            invulnerableTimer--;

        if (!isGrounded)
            animState = "jump";
        else if (isAttacking)
            animState = "attack";
        else if (Math.abs(vx) > 4.8)
            animState = "run";
        else if (Math.abs(vx) > 0)
            animState = "walk";
        else
            animState = "idle";

        animTimer++;
        if (animTimer % 6 == 0)
            animFrame = (animFrame + 1) % 4;

        for (Zona z : zones) {
            if (px >= z.startX && px < z.endX) {
                lblZoneText.setText(z.name);
                if (z.startX > checkpointX) {
                    checkpointX = z.startX + 50;
                    checkpointY = 380;
                    partida.guardarPuntoSeguro(checkpointX, checkpointY);
                }
            }
        }

        if (px > 4300 && guardian.estaVivo() && !bossActive) {
            bossActive = true;
            bossHudPanel.setVisible(true);
            mostrarDialogo("🚨 ¡ALERTA! El Guardián de la Biblioteca ha despertado.");
        }

        for (int i = 0; i < MONEDAS.length; i++) {
            int[] m = MONEDAS[i];
            if (new Rectangle((int) px, (int) py, pw, ph).intersects(new Rectangle(m[0], m[1], 24, 24))
                    && partida.premiar("moneda-biblioteca-" + i, m[2])) {
                Recursos.playCoin();
                addFloater(m[0], m[1], "+" + m[2], Color.YELLOW, 40);
                actualizarHUD();
            }
        }

        for (Libro b : books) {
            if (!jugador.getInventario().tieneLibro(b.name) && px + pw > b.x && px < b.x + 24 && py + ph > b.y
                    && py < b.y + 28) {
                partida.recogerLibro(b.name);
                Recursos.playCoin();
                addParticles(b.x + 12, b.y + 14, new Color(6, 182, 212), 12);
                addFloater(b.x + 12, b.y - 10, "+1 LIBRO", new Color(103, 232, 249), 50);
                actualizarHUD();
                mostrarDialogo("📘 ¡Recuperaste: <font color='#facc15'><b>" + b.name + "</b></font>! ("
                        + jugador.getInventario().getCantidadLibros() + "/5)");
                if (jugador.getInventario().getCantidadLibros() == totalBooks) {
                    lblKeyStatus.setText("ABIERTO");
                    lblKeyStatus.setIcon(Recursos.cargarIcono("sprites/items/llave-medalla.png", 22, 22));
                    lblKeyStatus.setForeground(new Color(74, 222, 128));
                }
            }
        }

        for (EnemigoVista en : enemies) {
            if (en.modelo.getHp() <= 0)
                continue;
            en.animTimer++;
            if (en.animTimer % en.frameSpeed == 0)
                en.frame = (en.frame + 1) % en.totalFrames;

            if ("book_guardian".equals(en.type) || "paper_golem".equals(en.type)) {
                en.x += en.speed * en.dir;
                if (en.x > en.maxX || en.x < en.minX)
                    en.dir *= -1;
            } else if ("ink_bat".equals(en.type)) {
                en.x += en.speed * en.dir;
                en.angle += 0.09;
                en.y = en.baseY + Math.sin(en.angle) * 38;
                if (en.x > px + 450 || en.x < px - 450)
                    en.dir *= -1;
            }

            if ("paper_golem".equals(en.type)) {
                en.shootTimer++;
                if (en.shootTimer % 120 == 0 && Math.abs(px - en.x) < 480) {
                    projectiles.add(new Proyectil(en.x + en.w / 2.0, en.y + en.h / 2.0, px > en.x ? 4.8 : -4.8, -1.5,
                            new Color(248, 250, 252), 7));
                }
            }

            if (invulnerableTimer == 0 &&
                    px + pw > en.x + 10 && px < en.x + en.w - 10 &&
                    py + ph > en.y + 10 && py < en.y + en.h - 10) {
                mostrarImpacto(en.modelo.atacar(jugador));
            }
        }

        if (bossActive && guardian.estaVivo()) {
            bossAnimTimer++;
            if (bossAnimTimer % 8 == 0)
                bossFrame = (bossFrame + 1) % 3;
            bossX += bossSpeed * bossDir;
            if (bossX < 4350 || bossX > 4950)
                bossDir *= -1;
            bossAttackTimer++;
            if (bossAttackTimer % 80 == 0) {
                projectiles.add(new Proyectil(bossX + bossW / 2.0, bossY + 45, px > bossX ? 6.5 : -6.5, 0,
                        new Color(236, 72, 153), 10));
            }
            if (invulnerableTimer == 0 &&
                    px + pw > bossX + 15 && px < bossX + bossW - 15 &&
                    py + ph > bossY + 15 && py < bossY + bossH - 15) {
                mostrarImpacto(guardian.atacar(jugador));
            }
        }

        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Proyectil p = projectiles.get(i);
            p.x += p.vx;
            p.y += p.vy;
            if (invulnerableTimer == 0 &&
                    px + pw > p.x - p.radius && px < p.x + p.radius &&
                    py + ph > p.y - p.radius && py < p.y + p.radius) {
                recibirDanio(15);
                projectiles.remove(i);
                continue;
            }
            if (p.x < camX - 100 || p.x > camX + VIEW_W + 100)
                projectiles.remove(i);
        }

        for (int i = particles.size() - 1; i >= 0; i--) {
            Particula pt = particles.get(i);
            pt.x += pt.vx;
            pt.y += pt.vy;
            pt.life--;
            if (pt.life <= 0)
                particles.remove(i);
        }

        for (int i = floaters.size() - 1; i >= 0; i--) {
            Floater f = floaters.get(i);
            f.y -= 0.7;
            f.life--;
            if (f.life <= 0)
                floaters.remove(i);
        }

        if (px + pw > exitPortal.x && px < exitPortal.x + exitPortal.width && py + ph > exitPortal.y) {
            if (partida.puedeAbrirPortal()) {
                partida.completarBiblioteca();
                mostrarFin(true,
                        "Recuperaste los 5 tomos y derrotaste al Guardián.<br><br>Completaste la Biblioteca Perdida.",
                        () -> {
                            if (gameLoopTimer != null)
                                gameLoopTimer.stop();
                            ventana.mostrarNivel3();
                        });
            } else {
                mostrarDialogo("🔒 El Portal está bloqueado. Necesitas los 5 Libros y vencer al Guardián.");
            }
        }

        actualizarCamara();
    }

    private void actualizarCamara() {
        double centroKai = px + pw / 2.0;
        double targetX = centroKai - VIEW_W * 0.42;
        targetX = Math.max(0.0, Math.min(WORLD_WIDTH - VIEW_W, targetX));
        // Seguimiento directo: al cambiar de dirección la cámara responde en el mismo tick.
        camX = targetX;
    }

    private void recibirDanio(int cantidad) {
        mostrarImpacto(jugador.recibirDanioDirecto(cantidad));
    }

    private void mostrarImpacto(int cantidad) {
        invulnerableTimer = 40;
        shakeTimer = 10;
        addFloater(px + pw / 2.0, py - 6, "-" + cantidad, new Color(248, 113, 113), 40);
        Recursos.playHit();
        addParticles(px + pw / 2.0, py + ph / 2.0, new Color(239, 68, 68), 12);
        actualizarHUD();

        if (!jugador.estaVivo()) {
            mostrarFin(false,
                    "Los guardianes de la biblioteca te vencieron.<br><br>Volverás al último punto seguro.",
                    () -> {
                        jugador.setHp(jugador.getMaxHp());
                        vy = 0;
                        invulnerableTimer = 90;
                        px = checkpointX;
                        py = checkpointY;
                        actualizarHUD();
                    });
        }
    }

    private void actualizarHUD() {
        lblMonedas.setText("MONEDAS: " + partida.getMonedero().getMonedas());
        hpBar.setValores(jugador.getHp(), jugador.getMaxHp());
        hpBar.setParpadeo(jugador.getPorcentajeHp() <= 30);
        lblHpText.setText(jugador.getHp() + "/" + jugador.getMaxHp());
        if (jugador.getInventario().tieneLlave("llave2")) {
            lblKeyStatus.setText("ABIERTO");
            lblKeyStatus.setIcon(Recursos.cargarIcono("sprites/items/llave-medalla.png", 22, 22));
            lblKeyStatus.setForeground(new Color(74, 222, 128));
        }
        lblBooksCount.setText(jugador.getInventario().getCantidadLibros() + " / " + totalBooks);
        for (int i = 0; i < 5; i++) {
            if (i < jugador.getInventario().getCantidadLibros()) {
                bookSlots[i].setIcon(Recursos.cargarIcono("sprites/items/libro.png", 18, 18));
                bookSlots[i].setBackground(new Color(34, 211, 238));
                bookSlots[i].setBorder(BorderFactory.createLineBorder(new Color(207, 250, 254), 2));
            } else {
                bookSlots[i].setIcon(null);
                bookSlots[i].setBackground(new Color(8, 47, 73));
                bookSlots[i].setBorder(BorderFactory.createLineBorder(new Color(22, 78, 99), 2));
            }
        }
        if (bossActive) {
            bossHpBar.setValores(guardian.getHp(), guardian.getMaxHp());
        }
    }

    private void mostrarFin(boolean victoria, String mensajeHtml, Runnable alAceptar) {
        if (juegoPausado)
            return;
        juegoPausado = true;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), victoria ? "¡VICTORIA!" : "HAS CAÍDO",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        MarcoRetroPanel box = new MarcoRetroPanel(
                victoria ? new Color(250, 204, 21) : new Color(239, 68, 68),
                new Color(109, 40, 217),
                new Color(28, 18, 69), new Color(11, 8, 24), false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createEmptyBorder(26, 22, 24, 22));
        box.setPreferredSize(new Dimension(420, 265));

        JLabel lblIcon = new JLabel(victoria ? "🏆" : "💀", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(victoria ? "¡VICTORIA!" : "HAS CAÍDO", SwingConstants.CENTER);
        lblTitle.setFont(Recursos.fuentePixel(Font.BOLD, 18f));
        lblTitle.setForeground(victoria ? new Color(250, 204, 21) : new Color(248, 113, 113));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMsg = new JLabel("<html><center>" + mensajeHtml + "</center></html>", SwingConstants.CENTER);
        lblMsg.setFont(Recursos.fuentePixel(Font.PLAIN, 9f));
        lblMsg.setForeground(new Color(212, 196, 247));
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        BotonRetro btn = new BotonRetro(victoria ? "CONTINUAR ▶" : "REINTENTAR ↻");
        btn.setEstiloMorado();
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setMaximumSize(new Dimension(260, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            dlg.dispose();
            juegoPausado = false;
            alAceptar.run();
            requestFocusInWindow();
        });

        box.add(lblIcon);
        box.add(Box.createVerticalStrut(8));
        box.add(lblTitle);
        box.add(Box.createVerticalStrut(12));
        box.add(lblMsg);
        box.add(Box.createVerticalGlue());
        box.add(btn);

        dlg.setContentPane(box);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void renderMundo(Graphics g, int panelW, int panelH) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Escala proporcional del mundo de juego 960x540
        double scaleX = panelW / (double) VIEW_W;
        double scaleY = panelH / (double) VIEW_H;
        g2.scale(scaleX, scaleY);

        int sx = shakeTimer > 0 ? (rnd.nextInt(9) - 4) : 0;
        int sy = shakeTimer > 0 ? (rnd.nextInt(7) - 3) : 0;
        g2.translate(sx, sy);

        // Fondo original de la Biblioteca Perdida: capas con parallax y ventanales.
        fondo.dibujarCapas(g2, camX, tick);

        // 2. Mundo desplazado por camara
        int cX = (int) Math.round(camX);
        g2.translate(-cX, 0);

        // Plataformas
        for (Plataforma p : platforms) {
            if (p.x + p.w < cX - 20 || p.x > cX + VIEW_W + 20)
                continue;
            if ("stone".equals(p.type)) {
                g2.setColor(new Color(76, 29, 149));
                g2.fillRect(p.x, p.y, p.w, p.h);
                g2.setColor(new Color(167, 139, 250));
                g2.fillRect(p.x, p.y, p.w, 4);
                g2.setColor(new Color(124, 58, 237));
                g2.fillRect(p.x, p.y + 4, p.w, 3);
            } else if ("wood".equals(p.type)) {
                g2.setColor(new Color(91, 51, 23));
                g2.fillRect(p.x, p.y, p.w, p.h);
                g2.setColor(new Color(124, 74, 34));
                g2.fillRect(p.x, p.y, p.w, 6);
            } else if ("bookshelf".equals(p.type)) {
                g2.setColor(new Color(120, 53, 15));
                g2.fillRect(p.x, p.y, p.w, p.h);
                g2.setColor(new Color(161, 98, 7));
                g2.fillRect(p.x, p.y, p.w, 4);
            } else {
                g2.setColor(new Color(122, 82, 48));
                g2.fillRect(p.x, p.y, p.w, p.h);
                g2.setColor(new Color(196, 181, 253));
                g2.fillRect(p.x, p.y - 12, p.w, 2);
            }
        }

        BufferedImage imagenMoneda = Recursos.cargarImagen("sprites/items/moneda.png");
        for (int i = 0; i < MONEDAS.length; i++) {
            if (!partida.tienePremio("moneda-biblioteca-" + i)) {
                int[] m = MONEDAS[i];
                g2.drawImage(imagenMoneda, m[0], m[1], 24, 24, null);
            }
        }

        // Portal salida
        boolean portalOpen = partida.puedeAbrirPortal();
        g2.setColor(new Color(42, 29, 87));
        g2.fillRect(exitPortal.x - 10, exitPortal.y - 12, exitPortal.width + 20, exitPortal.height + 12);
        g2.setColor(portalOpen ? new Color(16, 185, 129) : new Color(5, 3, 13));
        g2.fillRect(exitPortal.x, exitPortal.y, exitPortal.width, exitPortal.height);
        if (!portalOpen) {
            g2.setColor(new Color(239, 68, 68));
            g2.fillRect(exitPortal.x + 21, exitPortal.y + 40, 18, 14);
        }

        // Libros Sagrados

        for (Libro b : books) {
            if (jugador.getInventario().tieneLibro(b.name))
                continue;
            int bob = (int) (Math.sin(tick * 0.06 + b.x) * 4);
            int cy = b.y + 14 + bob;
            g2.setColor(new Color(14, 116, 144));
            g2.fillRect(b.x - 1, cy - 16, 24, 32);
            g2.setColor(new Color(34, 211, 238));
            g2.fillRect(b.x + 3, cy - 16, 19, 32);
            g2.setColor(new Color(248, 250, 252));
            g2.fillRect(b.x + 6, cy - 12, 14, 24);
        }

        // Enemigos
        for (EnemigoVista en : enemies) {
            if (en.modelo.getHp() <= 0)
                continue;
            if (en.sheet != null) {
                int fw = en.sheet.getWidth() / en.totalFrames;
                int fh = en.sheet.getHeight();
                Graphics2D ge = (Graphics2D) g2.create();
                if (en.dir == -1) {
                    ge.translate(en.x + en.w, en.y);
                    ge.scale(-1, 1);
                    ge.drawImage(en.sheet, 0, 0, en.w, en.h, en.frame * fw, 0, (en.frame + 1) * fw, fh, null);
                } else {
                    ge.drawImage(en.sheet, (int) en.x, (int) en.y, (int) en.x + en.w, (int) en.y + en.h, en.frame * fw,
                            0, (en.frame + 1) * fw, fh, null);
                }
                ge.dispose();
            }
            if (en.modelo.getHp() < en.modelo.getMaxHp()) {
                int bw = (int) (en.w * 0.8);
                int bx = (int) (en.x + (en.w - bw) / 2.0);
                int by = (int) (en.y - 10);
                g2.setColor(Color.BLACK);
                g2.fillRect(bx - 1, by - 1, bw + 2, 7);
                g2.setColor(new Color(74, 222, 128));
                g2.fillRect(bx, by, (int) (bw * ((double) en.modelo.getHp() / en.modelo.getMaxHp())), 5);
            }
        }

        // Mini Jefe
        if (bossActive && guardian.estaVivo() && bossSheet != null) {
            int fw = bossSheet.getWidth() / 3;
            int fh = bossSheet.getHeight();
            Graphics2D gb = (Graphics2D) g2.create();
            if (bossDir == 1) {
                gb.translate(bossX + bossW, bossY);
                gb.scale(-1, 1);
                gb.drawImage(bossSheet, 0, 0, bossW, bossH, bossFrame * fw, 0, (bossFrame + 1) * fw, fh, null);
            } else {
                gb.drawImage(bossSheet, (int) bossX, (int) bossY, (int) bossX + bossW, (int) bossY + bossH,
                        bossFrame * fw, 0, (bossFrame + 1) * fw, fh, null);
            }
            gb.dispose();
        }

        // Proyectiles
        for (Proyectil p : projectiles) {
            g2.setColor(p.color);
            g2.fillOval((int) (p.x - p.radius), (int) (p.y - p.radius), p.radius * 2, p.radius * 2);
            g2.setColor(Color.WHITE);
            g2.fillOval((int) (p.x - p.radius / 2.0), (int) (p.y - p.radius / 2.0), p.radius, p.radius);
        }

        // Jugador Kai (spritesheet 4x4)
        Graphics2D gk = (Graphics2D) g2.create();
        double cx = px + pw / 2.0;
        gk.translate(cx, 0);
        if (!dirRight)
            gk.scale(-1, 1);

        if (kaiSheet != null) {
            int fw = kaiSheet.getWidth() / 4;
            int fh = kaiSheet.getHeight() / 4;
            int row = 0, col = animFrame;
            if (invulnerableTimer > 26) {
                row = 3;
                col = 1;
            } else if ("attack".equals(animState)) {
                row = 2;
                col = Math.min(3, (18 - attackTimer) / 5);
            } else if ("jump".equals(animState)) {
                row = 1;
                col = vy < 0 ? 1 : 2;
            } else if ("walk".equals(animState) || "run".equals(animState)) {
                row = 1;
            }

            int D = 74;
            int dy = (int) (py + ph - D + 5);
            gk.drawImage(kaiSheet, -D / 2 + 4, dy, D / 2 + 4, dy + D, col * fw + 64, row * fh, col * fw + 320,
                    row * fh + 256, null);
        } else {
            gk.setColor(new Color(245, 158, 11));
            gk.fillRect(-pw / 2, (int) py, pw, ph);
        }
        gk.dispose();

        // Arco de corte al atacar
        if (isAttacking) {
            g2.setColor(new Color(255, 247, 204));
            g2.setStroke(new BasicStroke(4f));
            int arcX = dirRight ? (int) (px + pw) : (int) (px - 35);
            g2.drawArc(arcX, (int) py, 35, ph, dirRight ? -60 : 120, 120);
        }

        // Particulas
        for (Particula pt : particles) {
            g2.setColor(pt.color);
            g2.fillRect((int) pt.x, (int) pt.y, (int) pt.size, (int) pt.size);
        }

        // Textos flotantes
        g2.setFont(Recursos.fuentePixel(Font.BOLD, 10f));
        for (Floater f : floaters) {
            g2.setColor(Color.BLACK);
            g2.drawString(f.text, (int) f.x + 2, (int) f.y + 2);
            g2.setColor(f.color);
            g2.drawString(f.text, (int) f.x, (int) f.y);
        }

        g2.translate(cX, 0);

        // Barra de progreso superior del nivel
        double pr = Math.min(1.0, px / (WORLD_WIDTH - 100.0));
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(16, 8, VIEW_W - 32, 6);
        g2.setColor(new Color(34, 211, 238));
        g2.fillRect(16, 8, (int) ((VIEW_W - 32) * pr), 6);
        g2.setColor(new Color(250, 204, 21));
        g2.fillRect(14 + (int) ((VIEW_W - 32) * pr), 6, 8, 10);

        g2.dispose();
    }

    // Matriz: cada fila contiene x, y y valor. El indice sirve de identificador
    // estable.
    private static final int[][] MONEDAS = {
            { 320, 300, 10 }, { 535, 225, 10 }, { 760, 165, 10 }, { 1480, 100, 15 },
            { 2520, 230, 15 }, { 3820, 180, 20 }, { 4880, 290, 20 }
    };
    private final JLabel lblMonedas = new JLabel();

    @Override
    public void addNotify() {
        super.addNotify();
        actualizarHUD();
    }
}

package frontend;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Fondo del Nivel 2 (Biblioteca Perdida de la UNSA).
 *
 * Las capas estáticas se pre-renderizan una sola vez en imágenes "tileables" y luego se desplazan
 * con parallax (cada capa a distinta velocidad). Encima se dibujan los efectos dinámicos:
 * candelabros con llama parpadeante, niebla, polvo flotante, viñeta y un tinte de color por zona.
 *
 *   capa 0  muro base + estrellas          (fija)
 *   capa 1  ventanales góticos con luna y rayos de luz   (parallax 0.12)
 *   capa 2  estanterías del muro del fondo               (parallax 0.25)
 *   capa 3  columnas y arcos de piedra                   (parallax 0.40)
 *   capa 4  candelabros colgantes (animados)             (parallax 0.55)
 *   capa 5  niebla + polvo + viñeta + tinte de zona      (pantalla)
 */
public final class FondoBiblioteca {
    private static final int W = 960;
    private static final int H = 540;

    private static final int T_VENTANAS = 1200;
    private static final int T_ESTANTES = 640;
    private static final int T_COLUMNAS = 800;
    private static final int T_LAMPARAS = 900;

    private static final double P_VENTANAS = 0.12;
    private static final double P_ESTANTES = 0.25;
    private static final double P_COLUMNAS = 0.40;
    private static final double P_LAMPARAS = 0.55;

    private final BufferedImage muro;
    private final BufferedImage ventanales;
    private final BufferedImage estanterias;
    private final BufferedImage columnas;
    private final BufferedImage vineta;

    // Polvo: x, y, velocidad x, fase, tamaño
    private final double[][] polvo = new double[46][5];

    // Tinte de color según la posición en el mundo (centros de las 5 zonas del nivel)
    private static final double[] ZONA_X = { 500, 1550, 2650, 3700, 4700 };
    private static final int[][] ZONA_RGBA = {
        { 80, 120, 255, 16 },   // 1. Vestíbulo: azul lunar
        { 245, 158, 11, 26 },   // 2. Laberinto: ámbar de velas
        { 16, 185, 129, 30 },   // 3. Archivo prohibido: verde espectral
        { 56, 189, 248, 24 },   // 4. Puentes: cian frío
        { 236, 72, 153, 44 }    // 5. Sanctum del guardián: magenta
    };

    public FondoBiblioteca() {
        muro = crearMuro();
        ventanales = crearVentanales();
        estanterias = crearEstanterias();
        columnas = crearColumnas();
        vineta = crearVineta();

        Random r = new Random(7);
        for (double[] p : polvo) {
            p[0] = r.nextDouble() * W;
            p[1] = 40 + r.nextDouble() * 440;
            p[2] = 0.08 + r.nextDouble() * 0.22;
            p[3] = r.nextDouble() * Math.PI * 2;
            p[4] = 1 + r.nextInt(3);
        }
    }

    // =====================================================================
    //  API
    // =====================================================================

    /** Capas del fondo (se dibujan ANTES del mundo, en coordenadas de pantalla 960x540). */
    public void dibujarCapas(Graphics2D g, double camX, int tick) {
        g.drawImage(muro, 0, 0, null);
        capa(g, ventanales, T_VENTANAS, camX * P_VENTANAS);
        capa(g, estanterias, T_ESTANTES, camX * P_ESTANTES);
        capa(g, columnas, T_COLUMNAS, camX * P_COLUMNAS);
        dibujarCandelabros(g, camX, tick);
    }

    /** Efectos frontales (se dibujan DESPUÉS del mundo, en coordenadas de pantalla). */
    public void dibujarAtmosfera(Graphics2D g, double camX, int tick) {
        Composite original = g.getComposite();

        // Niebla baja: degradado + bancos de niebla que se desplazan lentamente
        g.setPaint(new GradientPaint(0, 380, new Color(120, 90, 200, 0), 0, H, new Color(120, 90, 200, 70)));
        g.fillRect(0, 380, W, H - 380);
        for (int i = 0; i < 5; i++) {
            double base = i * 260 - (camX * 0.8) % 1300 + tick * (0.25 + i * 0.07);
            double x = ((base % 1300) + 1300) % 1300 - 260;
            int y = 438 + (i % 3) * 22;
            g.setPaint(new GradientPaint((float) x, y, new Color(170, 150, 235, 0),
                                            (float) x + 120, y, new Color(170, 150, 235, 26), true));
            g.fill(new Ellipse2D.Double(x, y, 300, 46));
        }

        // Polvo flotante con brillo intermitente
        for (double[] p : polvo) {
            double x = (((p[0] - camX * 0.6 + tick * p[2]) % W) + W) % W;
            double y = p[1] + Math.sin(tick * 0.02 + p[3]) * 12;
            double brillo = 0.5 + 0.5 * Math.sin(tick * 0.05 + p[3] * 3);
            g.setColor(new Color(255, 236, 190, (int) (40 + 110 * brillo)));
            g.fillRect((int) x, (int) y, (int) p[4], (int) p[4]);
        }

        // Tinte de color de la zona actual (interpolado entre zonas)
        Color tinte = tinteEnMundo(camX + W / 2.0);
        g.setColor(tinte);
        g.fillRect(0, 0, W, H);

        // Viñeta
        g.drawImage(vineta, 0, 0, null);
        g.setComposite(original);
    }

    // =====================================================================
    //  Capas dinámicas
    // =====================================================================

    private void dibujarCandelabros(Graphics2D g, double camX, int tick) {
        int off = (int) Math.floorMod(Math.round(camX * P_LAMPARAS), (long) T_LAMPARAS);
        int idx = 0;
        for (int x = 450 - off - T_LAMPARAS; x < W + 160; x += T_LAMPARAS, idx++) {
            if (x < -160) continue;
            double balanceo = Math.sin(tick * 0.015 + x * 0.01) * 3;
            int cy = 118 + (int) Math.round(balanceo);
            candelabro(g, x + (int) balanceo, cy, tick, x);
        }
    }

    private void candelabro(Graphics2D g, int cx, int cy, int tick, int semilla) {
        // Cadena
        g.setColor(new Color(70, 62, 90));
        g.fillRect(cx - 1, 0, 3, cy - 6);
        g.setColor(new Color(120, 108, 150));
        for (int y = 4; y < cy - 8; y += 8) g.fillRect(cx - 2, y, 5, 3);

        // Halo de luz cálida (parpadea)
        double flick = 0.85 + 0.15 * Math.sin(tick * 0.21 + semilla) * Math.sin(tick * 0.13 + semilla * 0.3);
        int alphaHalo = (int) (78 * flick);
        RadialGradientPaint halo = new RadialGradientPaint(new Point2D.Float(cx, cy - 22), 150f,
            new float[] { 0f, 0.45f, 1f },
            new Color[] { new Color(255, 196, 96, alphaHalo), new Color(255, 150, 60, alphaHalo / 3), new Color(255, 120, 40, 0) });
        g.setPaint(halo);
        g.fill(new Ellipse2D.Float(cx - 150, cy - 172, 300, 300));

        // Aro de hierro
        g.setColor(new Color(58, 46, 70));
        g.fillRoundRect(cx - 52, cy - 2, 104, 10, 8, 8);
        g.setColor(new Color(196, 160, 82));
        g.fillRect(cx - 50, cy - 2, 100, 3);
        g.setColor(new Color(58, 46, 70));
        g.fillRect(cx - 6, cy + 8, 12, 10);   // remate inferior
        g.fillRect(cx - 3, cy + 18, 6, 6);

        // Velas y llamas
        for (int i = -2; i <= 2; i++) {
            int vx = cx + i * 21 - 3;
            g.setColor(new Color(236, 226, 196));
            g.fillRect(vx, cy - 16, 6, 14);
            g.setColor(new Color(176, 160, 128));
            g.fillRect(vx + 4, cy - 16, 2, 14);
            double f = Math.sin(tick * 0.28 + i * 1.7 + semilla);
            int alto = 9 + (int) Math.round(f * 2);
            g.setColor(new Color(255, 140, 30));
            g.fill(new Ellipse2D.Double(vx, cy - 16 - alto, 6, alto + 2));
            g.setColor(new Color(255, 236, 140));
            g.fill(new Ellipse2D.Double(vx + 1.5, cy - 14 - alto + 2, 3, alto - 2));
        }
    }

    // =====================================================================
    //  Utilidades
    // =====================================================================

    private static void capa(Graphics2D g, BufferedImage img, int periodo, double desplazamiento) {
        int off = (int) Math.floorMod(Math.round(desplazamiento), (long) periodo);
        for (int x = -off; x < W; x += periodo) g.drawImage(img, x, 0, null);
    }

    private static Color tinteEnMundo(double mundoX) {
        if (mundoX <= ZONA_X[0]) return rgba(ZONA_RGBA[0]);
        for (int i = 1; i < ZONA_X.length; i++) {
            if (mundoX <= ZONA_X[i]) {
                double t = (mundoX - ZONA_X[i - 1]) / (ZONA_X[i] - ZONA_X[i - 1]);
                int[] a = ZONA_RGBA[i - 1], b = ZONA_RGBA[i];
                return new Color(mezcla(a[0], b[0], t), mezcla(a[1], b[1], t), mezcla(a[2], b[2], t), mezcla(a[3], b[3], t));
            }
        }
        return rgba(ZONA_RGBA[ZONA_RGBA.length - 1]);
    }

    private static Color rgba(int[] c) { return new Color(c[0], c[1], c[2], c[3]); }
    private static int mezcla(int a, int b, double t) { return (int) Math.round(a + (b - a) * t); }

    private static BufferedImage nuevaImagen(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    /** Arco ojival (punta suave): x,y = esquina superior izquierda del recuadro que lo contiene. */
    private static Path2D arco(double x, double y, double w, double h) {
        double r = w / 2;
        Path2D p = new Path2D.Double();
        p.moveTo(x, y + h);
        p.lineTo(x, y + r * 1.15);
        p.curveTo(x, y + r * 0.45, x + r * 0.62, y, x + r, y);
        p.curveTo(x + r * 1.38, y, x + w, y + r * 0.45, x + w, y + r * 1.15);
        p.lineTo(x + w, y + h);
        p.closePath();
        return p;
    }

    // =====================================================================
    //  Pre-render de capas
    // =====================================================================

    /** Muro profundo con degradado violeta y estrellas lejanas. */
    private BufferedImage crearMuro() {
        BufferedImage img = nuevaImagen(W, H);
        Graphics2D g = img.createGraphics();
        g.setPaint(new GradientPaint(0, 0, new Color(8, 5, 26), 0, H, new Color(40, 22, 78)));
        g.fillRect(0, 0, W, H);
        // Resplandor central tenue
        g.setPaint(new RadialGradientPaint(new Point2D.Float(W / 2f, 230), 520f,
            new float[] { 0f, 1f }, new Color[] { new Color(90, 60, 170, 70), new Color(90, 60, 170, 0) }));
        g.fillRect(0, 0, W, H);
        Random r = new Random(11);
        for (int i = 0; i < 70; i++) {
            int a = 60 + r.nextInt(120);
            g.setColor(new Color(220, 220, 255, a));
            g.fillRect(r.nextInt(W), r.nextInt(300), 1 + r.nextInt(2), 1 + r.nextInt(2));
        }
        g.dispose();
        return img;
    }

    /** Ventanales góticos con cielo nocturno, luna, estrellas, tracería y rayos de luz. */
    private BufferedImage crearVentanales() {
        BufferedImage img = nuevaImagen(T_VENTANAS, H);
        Graphics2D g = img.createGraphics();
        Random r = new Random(23);
        final int ancho = 118, alto = 290, y0 = 26;

        for (int i = 0; i < 3; i++) {
            int wx = 140 + i * 400;

            // Rayo de luz lunar cayendo en diagonal hacia el suelo
            Path2D rayo = new Path2D.Double();
            rayo.moveTo(wx + 14, y0 + alto - 20);
            rayo.lineTo(wx + ancho - 14, y0 + alto - 20);
            rayo.lineTo(wx + ancho + 170, 470);
            rayo.lineTo(wx + 40, 470);
            rayo.closePath();
            g.setPaint(new GradientPaint(0, y0 + alto - 20, new Color(170, 205, 255, 62), 0, 470, new Color(170, 205, 255, 0)));
            g.fill(rayo);

            // Marco de piedra (arco exterior)
            g.setColor(new Color(14, 9, 38));
            g.fill(arco(wx - 16, y0 - 16, ancho + 32, alto + 40));
            g.setColor(new Color(74, 58, 140));
            g.setStroke(new BasicStroke(3f));
            g.draw(arco(wx - 16, y0 - 16, ancho + 32, alto + 40));

            // Vidrio con cielo nocturno
            Path2D vidrio = arco(wx, y0, ancho, alto);
            g.setPaint(new GradientPaint(0, y0, new Color(10, 16, 58), 0, y0 + alto, new Color(52, 86, 160)));
            g.fill(vidrio);

            Shape clipPrev = g.getClip();
            g.setClip(vidrio);
            // Estrellas dentro del vidrio
            for (int s = 0; s < 14; s++) {
                g.setColor(new Color(235, 240, 255, 110 + r.nextInt(120)));
                g.fillRect(wx + 8 + r.nextInt(ancho - 16), y0 + 10 + r.nextInt(alto - 90), 2, 2);
            }
            // Luna con halo (sólo en dos de los tres ventanales; posición distinta)
            if (i != 1) {
                int lx = wx + 38 + i * 12, ly = y0 + 62 + i * 14;
                g.setPaint(new RadialGradientPaint(new Point2D.Float(lx + 15, ly + 15), 70f,
                    new float[] { 0f, 1f }, new Color[] { new Color(240, 245, 255, 150), new Color(240, 245, 255, 0) }));
                g.fillRect(lx - 60, ly - 60, 150, 150);
                Area creciente = new Area(new Ellipse2D.Double(lx, ly, 32, 32));   // luna menos un círculo desplazado
                creciente.subtract(new Area(new Ellipse2D.Double(lx + 10, ly - 4, 30, 30)));
                g.setColor(new Color(255, 250, 215));
                g.fill(creciente);
            }
            // Siluetas de torres lejanas al pie del vidrio
            g.setColor(new Color(16, 20, 56));
            g.fillRect(wx, y0 + alto - 46, 22, 46);
            g.fillRect(wx + 30, y0 + alto - 70, 16, 70);
            g.fillRect(wx + 56, y0 + alto - 38, 26, 38);
            g.fillRect(wx + 88, y0 + alto - 58, 30, 58);
            g.setClip(clipPrev);

            // Tracería (parteluces)
            g.setColor(new Color(26, 18, 62));
            g.setStroke(new BasicStroke(4f));
            g.drawLine(wx + ancho / 2, y0 + 8, wx + ancho / 2, y0 + alto);
            g.drawLine(wx, y0 + 110, wx + ancho, y0 + 110);
            g.drawLine(wx, y0 + 200, wx + ancho, y0 + 200);
            g.drawOval(wx + ancho / 2 - 18, y0 + 32, 36, 36);
            g.setStroke(new BasicStroke(1f));
            g.setColor(new Color(110, 92, 190, 150));
            g.draw(arco(wx, y0, ancho, alto));

            // Alféizar
            g.setColor(new Color(34, 24, 74));
            g.fillRect(wx - 20, y0 + alto, ancho + 40, 12);
            g.setColor(new Color(96, 78, 168));
            g.fillRect(wx - 20, y0 + alto, ancho + 40, 3);
        }
        g.dispose();
        return img;
    }

    /** Estanterías del muro del fondo con lomos de libros de colores. */
    private BufferedImage crearEstanterias() {
        BufferedImage img = nuevaImagen(T_ESTANTES, H);
        Graphics2D g = img.createGraphics();
        Random r = new Random(41);
        final int top = 318, bottom = 466, filas = 4;
        final int filaAlto = (bottom - top) / filas;
        final int modulo = 160;
        final Color[] lomos = {
            new Color(168, 48, 72), new Color(40, 128, 150), new Color(196, 148, 40), new Color(112, 64, 170),
            new Color(52, 130, 84), new Color(190, 92, 52), new Color(70, 84, 170), new Color(150, 150, 168)
        };

        for (int m = 0; m < T_ESTANTES / modulo; m++) {
            int mx = m * modulo;
            // Fondo del mueble
            g.setColor(new Color(24, 15, 36));
            g.fillRect(mx, top, modulo, bottom - top);

            for (int f = 0; f < filas; f++) {
                int baseY = top + (f + 1) * filaAlto;
                int x = mx + 8;
                while (x < mx + modulo - 12) {
                    if (r.nextInt(14) == 0) { x += 7 + r.nextInt(10); continue; }   // hueco vacío
                    int bw = 6 + r.nextInt(7);
                    int bh = filaAlto - 8 - r.nextInt(10);
                    if (x + bw > mx + modulo - 8) break;
                    Color c = lomos[r.nextInt(lomos.length)];
                    // Oscurecidos para que el fondo no compita con el gameplay
                    Color c2 = new Color((int) (c.getRed() * 0.50), (int) (c.getGreen() * 0.50), (int) (c.getBlue() * 0.55));
                    g.setColor(c2);
                    g.fillRect(x, baseY - bh, bw, bh);
                    g.setColor(new Color(0, 0, 0, 90));
                    g.fillRect(x + bw - 2, baseY - bh, 2, bh);
                    g.setColor(new Color(255, 235, 170, 70));       // franja dorada del lomo
                    g.fillRect(x + 1, baseY - bh + 5, bw - 3, 2);
                    x += bw + 1;
                }
                // Tabla de la repisa
                g.setColor(new Color(56, 34, 40));
                g.fillRect(mx, baseY, modulo, 5);
                g.setColor(new Color(92, 58, 52));
                g.fillRect(mx, baseY, modulo, 1);
            }
            // Marco del mueble
            g.setColor(new Color(60, 38, 46));
            g.fillRect(mx, top - 8, modulo, 8);
            g.fillRect(mx, top, 6, bottom - top);
            g.fillRect(mx + modulo - 6, top, 6, bottom - top);
            g.setColor(new Color(98, 64, 62));
            g.fillRect(mx, top - 8, modulo, 2);
        }

        // Oscurecer ligeramente toda la capa (profundidad)
        g.setPaint(new GradientPaint(0, top - 8, new Color(8, 4, 24, 20), 0, bottom, new Color(8, 4, 24, 120)));
        g.fillRect(0, top - 8, T_ESTANTES, bottom - top + 8);
        g.dispose();
        return img;
    }

    /** Columnas y arcos ojivales en primer plano medio. Las "bahías" quedan transparentes. */
    private BufferedImage crearColumnas() {
        BufferedImage img = nuevaImagen(T_COLUMNAS, H);
        Graphics2D g = img.createGraphics();

        final int bahia = 400, anchoCol = 74, techo = 0, suelo = 470;
        Area muroPiedra = new Area(new Rectangle(0, techo, T_COLUMNAS, suelo - techo));
        for (int b = 0; b < T_COLUMNAS / bahia; b++) {
            int ox = b * bahia + anchoCol / 2;
            muroPiedra.subtract(new Area(arco(ox, 36, bahia - anchoCol, suelo - 36)));
        }

        g.setPaint(new GradientPaint(0, 0, new Color(18, 12, 40), 0, suelo, new Color(34, 22, 66)));
        g.fill(muroPiedra);

        // Juntas de sillería (sólo dentro de la piedra)
        Shape clipPrev = g.getClip();
        g.setClip(muroPiedra);
        g.setColor(new Color(8, 4, 24, 130));
        for (int y = 14, fila = 0; y < suelo; y += 30, fila++) {
            g.fillRect(0, y, T_COLUMNAS, 2);
            for (int x = (fila % 2) * 30; x < T_COLUMNAS; x += 60) g.fillRect(x, y, 2, 30);
        }
        // Luz fría desde la izquierda de cada columna
        for (int b = 0; b < T_COLUMNAS / bahia; b++) {
            int cx = b * bahia - anchoCol / 2;
            g.setPaint(new GradientPaint(cx, 0, new Color(150, 130, 255, 0), cx + anchoCol, 0, new Color(150, 130, 255, 46)));
            g.fillRect(cx + (b == 0 ? anchoCol / 2 : 0), 0, anchoCol, suelo);
            g.fillRect(cx + T_COLUMNAS, 0, anchoCol, suelo);
        }
        g.setClip(clipPrev);

        // Contorno iluminado del arco
        g.setColor(new Color(112, 92, 196, 150));
        g.setStroke(new BasicStroke(2f));
        g.draw(muroPiedra);

        // Capiteles y bases
        for (int b = 0; b < T_COLUMNAS / bahia; b++) {
            int cx = b * bahia - anchoCol / 2;
            for (int copia = 0; copia < 2; copia++) {
                int px = cx + copia * T_COLUMNAS;
                g.setColor(new Color(40, 28, 82));
                g.fillRect(px - 6, 150, anchoCol + 12, 12);           // capitel
                g.fillRect(px - 8, suelo - 24, anchoCol + 16, 24);   // base
                g.setColor(new Color(120, 100, 200, 160));
                g.fillRect(px - 6, 150, anchoCol + 12, 2);
                g.fillRect(px - 8, suelo - 24, anchoCol + 16, 2);
            }
        }
        g.dispose();
        return img;
    }

    private BufferedImage crearVineta() {
        BufferedImage img = nuevaImagen(W, H);
        Graphics2D g = img.createGraphics();
        g.translate(W / 2.0, H / 2.0);
        g.scale(1.0, 0.82);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(0, 0), W * 0.64f,
            new float[] { 0f, 0.58f, 1f },
            new Color[] { new Color(3, 2, 12, 0), new Color(3, 2, 12, 0), new Color(3, 2, 12, 165) }));
        g.fillRect(-W, -H * 2, W * 2, H * 4);
        g.dispose();
        return img;
    }
}

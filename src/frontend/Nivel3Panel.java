package frontend;

import backend.Enemigo;
import backend.Jugador;
import backend.Partida;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/** Nivel 3 nuevo: Distrito de la Lluvia. Plataformas y combate arcade lateral. */
public final class Nivel3Panel extends PanelJuego {
    private static final int WORLD_W = 6200, FLOOR = 500;
    private final VentanaPrincipal ventana;
    private final Partida partida;
    private final Jugador jugador;
    private final Lienzo lienzo = new Lienzo();
    private final Set<Integer> keys = new HashSet<>();
    private final List<Rectangle> platforms = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Crate> crates = new ArrayList<>();
    private final List<Bomb> bombs = new ArrayList<>();
    private final List<Ladder> ladders = new ArrayList<>();
    private int animFrame;
    private double x=90, y=420, vx, vy, camera;
    private boolean grounded, facingRight=true, completed, paused, climbing;
    private int tick, shootCooldown, hurtCooldown, checkpoint=90, defeated;
    private String notice="CRUZA EL DISTRITO · LLEGA A LA TORRE DE CONTROL";
    private JLabel status;
    private final javax.swing.Timer loop;

    private static final class Enemy {
        double x,y,vx; int hp=3, min,max, fire=80; boolean alive=true;
        Enemy(int x,int min,int max,double speed){this.x=x;this.y=456;this.min=min;this.max=max;vx=speed;}
    }
    private static final class Shot { double x,y,vx; int life=70; Shot(double x,double y,double vx){this.x=x;this.y=y;this.vx=vx;} }
    private static final class Particle { double x,y,vx,vy; int life; Color color; Particle(double x,double y,double vx,double vy,int life,Color c){this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.life=life;color=c;} }
    private static final class Crate { int x,y; boolean broken; Crate(int x,int y){this.x=x;this.y=y;} }
    private static final class Bomb { double x,y,vx,vy; int fuse=95; Bomb(double x,double y,double vx){this.x=x;this.y=y;this.vx=vx;this.vy=-5.2;} }
    private static final class Ladder { int x,top,bottom; Ladder(int x,int top,int bottom){this.x=x;this.top=top;this.bottom=bottom;} }

    public Nivel3Panel(VentanaPrincipal ventana, Partida partida) {
        this.ventana=ventana; this.partida=partida; this.jugador=partida.getJugador();
        setLayout(new BorderLayout()); setBackground(new Color(8,13,27));
        JPanel top=new JPanel(new BorderLayout()); top.setBackground(new Color(9,18,34)); top.setBorder(BorderFactory.createEmptyBorder(9,14,9,14));
        JLabel title=new JLabel("MAZMORRAS UNSA  /  NIVEL 3: DISTRITO DE LA LLUVIA"); title.setForeground(new Color(146,226,255)); title.setFont(Recursos.fuentePixel(Font.BOLD,10));
        status=new JLabel(); status.setForeground(new Color(255,220,140)); status.setFont(Recursos.fuentePixel(Font.BOLD,9)); top.add(title,BorderLayout.WEST);top.add(status,BorderLayout.EAST); actualizarEstado();
        add(top,BorderLayout.NORTH); add(lienzo,BorderLayout.CENTER);
        JLabel controls=new JLabel("A/D o ←/→ MOVER   ESPACIO/W/↑ SALTAR   J o X DISPARAR   ↑/W SUBIR ESCALERAS   P PAUSA   ESC MENÚ",SwingConstants.CENTER);
        controls.setOpaque(true);controls.setBackground(new Color(10,18,34));controls.setForeground(Color.WHITE);controls.setFont(Recursos.fuentePixel(Font.BOLD,8));controls.setBorder(BorderFactory.createEmptyBorder(9,4,9,4));add(controls,BorderLayout.SOUTH);
        buildWorld(); bindKeys(); loop=temporizador(16,e->{if(!paused&&!completed)updateGame();lienzo.repaint();});loop.start();
    }
    private void buildWorld(){
        platforms.add(new Rectangle(0,FLOOR,WORLD_W,180));
        int[][] ledges={{360,405,150,18},{650,350,180,18},{960,420,130,18},{1240,365,190,18},{1590,405,180,18},{1900,335,160,18},{2240,405,210,18},{2600,350,150,18},{2920,410,190,18},{3260,345,190,18},{3610,405,170,18},{3950,335,200,18},{4310,400,170,18},{4660,350,190,18},{5030,410,180,18},{5400,350,180,18}};
        for(int[] a:ledges)platforms.add(new Rectangle(a[0],a[1],a[2],a[3]));
        int[][] specs={{520,440,740},{1040,930,1200},{1720,1580,1870},{2380,2200,2530},{3050,2900,3210},{3740,3600,3900},{4470,4300,4620},{5180,5000,5340},{5700,5500,6000}};
        for(int[] a:specs)enemies.add(new Enemy(a[0],a[1],a[2],1.0+(a[0]%3)*.25));
        for(int a=280;a<5600;a+=510)crates.add(new Crate(a,468));
        int[][] stairData={{820,330,500},{1460,315,500},{2130,300,500},{2780,320,500},{3440,305,500},{4110,300,500},{4780,315,500},{5350,305,500}};
        for(int[] a:stairData){ladders.add(new Ladder(a[0],a[1],a[2]));for(int yy=a[1]+35;yy<a[2];yy+=35)platforms.add(new Rectangle(a[0]-4,yy,58,9));}
    }
    private void bindKeys(){
        // KeyListener dependia de que el lienzo conservara el foco. Al cambiar de
        // pantalla Swing podia dejar el foco en el panel padre y congelar los controles.
        // WHEN_IN_FOCUSED_WINDOW recibe las teclas aunque el foco este en otro hijo.
        lienzo.setFocusable(true);
        lienzo.setFocusTraversalKeysEnabled(false);
        int[] controls = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                KeyEvent.VK_SPACE, KeyEvent.VK_J, KeyEvent.VK_X};
        for (int key : controls) {
            String name = "nivel3-pressed-" + key;
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0, false), name);
            getActionMap().put(name, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    boolean firstPress = keys.add(key);
                    if (!firstPress || paused || completed) return;
                    if (key == KeyEvent.VK_P) { paused = !paused; return; }
                    if ((key == KeyEvent.VK_SPACE || key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
                            && grounded && !climbing) {
                        vy = -11.5;
                        grounded = false;
                    }
                    if ((key == KeyEvent.VK_J || key == KeyEvent.VK_X) && shootCooldown <= 0) shoot();
                }
            });
            String release = "nivel3-released-" + key;
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0, true), release);
            getActionMap().put(release, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { keys.remove(key); }
            });
        }
        String pause = "nivel3-pause";
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), pause);
        getActionMap().put(pause, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { paused = !paused; }
        });
        String escape = "nivel3-exit";
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escape);
        getActionMap().put(escape, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { salir(); }
        });
        addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { keys.clear(); }
        });
        SwingUtilities.invokeLater(() -> { requestFocusInWindow(); lienzo.requestFocusInWindow(); });
    }
    private void shoot(){shots.add(new Shot(x+(facingRight?31:-5),y+20,facingRight?10:-10));shootCooldown=17;for(int i=0;i<5;i++)particles.add(new Particle(x+(facingRight?34:0),y+22,(facingRight?1:-1)*(Math.random()*3+1),(Math.random()-.5)*2,12,new Color(255,205,91)));}
    private void updateGame(){tick++;if(shootCooldown>0)shootCooldown--;if(hurtCooldown>0)hurtCooldown--;
        boolean left=keys.contains(KeyEvent.VK_A)||keys.contains(KeyEvent.VK_LEFT),right=keys.contains(KeyEvent.VK_D)||keys.contains(KeyEvent.VK_RIGHT);
        vx=left?-4.1:right?4.1:0;if(vx!=0)facingRight=vx>0;x+=vx;x=Math.max(0,Math.min(WORLD_W-35,x));
        double oldY=y;boolean up=keys.contains(KeyEvent.VK_W)||keys.contains(KeyEvent.VK_UP),down=keys.contains(KeyEvent.VK_S)||keys.contains(KeyEvent.VK_DOWN);
        Ladder near=null;for(Ladder l:ladders)if(x+17>=l.x-9&&x+17<=l.x+48&&y+40>=l.top&&y<=l.bottom){near=l;break;}
        climbing=near!=null&&(up||down);
        if(climbing){
            vy=0; y+=up?-3.6:3.6;
            y=Math.max(near.top-40,Math.min(near.bottom-40,y));
            grounded=false;
        } else { vy=Math.min(13,vy+.58); y+=vy; }
        grounded=false;
        for(Rectangle p:platforms){Rectangle2D.Double feet=new Rectangle2D.Double(x+3,y+35,26,5);if(vy>=0&&oldY+40<=p.y+5&&feet.intersects(p)){y=p.y-40;vy=0;grounded=true;}}
        if(y>720){x=checkpoint;y=420;vy=0;hurtPlayer();notice="TE REAGRUPASTE EN EL ÚLTIMO PUNTO DE CONTROL";}
        for(Enemy e:enemies)if(e.alive){e.x+=e.vx;if(e.x<e.min||e.x>e.max)e.vx=-e.vx;if(Math.abs(e.x-x)<38&&Math.abs(e.y-y)<50&&hurtCooldown==0)hurtPlayer();
            if(--e.fire<=0){if(Math.abs(e.x-x)<520){bombs.add(new Bomb(e.x+12,e.y+12,(x<e.x?-1:1)*(2.0+Math.random())));}e.fire=105+(int)(Math.random()*65);}}
        Iterator<Bomb> bi=bombs.iterator();while(bi.hasNext()){Bomb b=bi.next();b.x+=b.vx;b.y+=b.vy;b.vy+=.19;b.fuse--;if(b.fuse<=0||b.y>FLOOR-5){burst(b.x,b.y,new Color(255,139,65),24);if(Math.abs(b.x-x)<68&&Math.abs(b.y-y)<55&&hurtCooldown==0)hurtPlayer();bi.remove();}}
        Iterator<Shot> si=shots.iterator();while(si.hasNext()){Shot s=si.next();s.x+=s.vx;s.life--;boolean remove=s.life<=0;
            for(Enemy e:enemies)if(!remove&&e.alive&&Math.abs(s.x-e.x)<27&&Math.abs(s.y-(e.y+20))<30){e.hp--;burst(s.x,s.y,new Color(255,160,80),12);remove=true;if(e.hp<=0){e.alive=false;defeated++;burst(e.x,e.y,new Color(92,220,255),28);notice="SECTOR DESPEJADO · CONTINÚA AVANZANDO";}break;}
            for(Crate c:crates)if(!remove&&!c.broken&&Math.abs(s.x-c.x)<23&&Math.abs(s.y-480)<27){c.broken=true;remove=true;burst(c.x,470,new Color(255,185,85),20);}
            if(remove)si.remove();}
        for(Particle p:particles){p.x+=p.vx;p.y+=p.vy;p.vy+=.12;p.life--; }particles.removeIf(p->p.life<=0);
        if(x>WORLD_W-130){completed=true;loop.stop();jugador.ganarExperiencia(80);JOptionPane.showMessageDialog(this,"¡NIVEL 3 COMPLETADO!\n\nAtravesaste el Distrito de la Lluvia y alcanzaste la torre de control.","Mazmorras UNSA",JOptionPane.INFORMATION_MESSAGE);ventana.mostrarMenuPrincipal();}
        camera=Math.max(0,Math.min(WORLD_W-lienzo.getWidth(),x-lienzo.getWidth()*.38));if(x>1500)checkpoint=1500;if(x>3200)checkpoint=3200;actualizarEstado();
    }
    private void hurtPlayer(){hurtCooldown=65;jugador.recibirDanioDirecto(5);if(jugador.getHp()<=0){jugador.setHp(Math.max(1,jugador.getMaxHp()/2));x=checkpoint;y=420;notice="RECUPERASTE EL ALIENTO EN EL PUNTO DE CONTROL";}actualizarEstado();}
    private void burst(double bx,double by,Color c,int n){for(int i=0;i<n;i++){double a=Math.random()*Math.PI*2,s=Math.random()*4+1;particles.add(new Particle(bx,by,Math.cos(a)*s,Math.sin(a)*s,18+(int)(Math.random()*20),c));}}
    private void actualizarEstado(){if(status!=null)status.setText("HP "+jugador.getHp()+"/"+jugador.getMaxHp()+"   ·   SECTORES "+defeated+"/"+enemies.size());}
    private void salir(){detener();ventana.mostrarMenuPrincipal();}
    @Override public void detener(){if(loop!=null)loop.stop();super.detener();}

    private final class Lienzo extends JPanel{
        Lienzo(){setPreferredSize(new Dimension(1100,620));setBackground(new Color(8,15,28));}
        protected void paintComponent(Graphics gg){super.paintComponent(gg);Graphics2D g=(Graphics2D)gg.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);g.translate(-camera,0);drawBackground(g);drawWorld(g);drawLadders(g);drawEntities(g);g.translate(camera,0);drawOverlay(g);g.dispose();}
        private void drawBackground(Graphics2D g){g.setPaint(new GradientPaint(0,0,new Color(9,22,43),0,500,new Color(33,43,58)));g.fillRect((int)camera,0,getWidth()+1,620);
            for(int layer=0;layer<3;layer++){int base=360+layer*45;for(int bx=(int)(camera/ (1.0+layer*.25)/110)*110-110;bx<camera+getWidth()+220;bx+=110){int h=90+(int)((Math.abs(bx*31L+layer*73)%150));g.setColor(new Color(12+layer*5,28+layer*5,47+layer*6));g.fillRect(bx,base-h,78,h);g.setColor(new Color(44,102,125));for(int wy=base-h+15;wy<base-15;wy+=24)for(int wx=bx+12;wx<bx+68;wx+=22){g.setColor((wx+wy+tick/8)%4==0?new Color(255,190,91):new Color(42,119,145));g.fillRect(wx,wy,8,11);}}}
            // luna velada y reflejos de neón
            g.setColor(new Color(174,205,216,150));g.fillOval((int)camera+getWidth()-190,58,58,58);g.setColor(new Color(9,22,43));g.fillOval((int)camera+getWidth()-174,48,58,58);
            g.setColor(new Color(92,167,196,70));for(int i=0;i<95;i++){int rx=(int)((i*137L+tick*3)%(WORLD_W)),ry=(i*67+tick*7)%510;g.drawLine(rx,ry,rx-5,ry+13);}}
        private void drawWorld(Graphics2D g){g.setColor(new Color(20,29,43));g.fillRect(0,FLOOR,WORLD_W,180);g.setColor(new Color(52,75,88));g.fillRect(0,FLOOR,WORLD_W,8);g.setColor(new Color(31,47,59));for(int i=0;i<WORLD_W;i+=64){g.fillRect(i,520,42,5);g.setColor(new Color(72,91,98));g.drawLine(i,500,i+18,520);g.setColor(new Color(31,47,59));}
            for(Rectangle p:platforms)if(p.y<FLOOR){g.setColor(new Color(39,62,76));g.fillRect(p.x,p.y,p.width,p.height);g.setColor(new Color(93,166,177));g.fillRect(p.x,p.y,p.width,4);}
            for(int i=0;i< WORLD_W;i+=420){g.setColor(new Color(22,34,47));g.fillRect(i+25,445,12,55);g.setColor(new Color(83,184,191));g.fillRect(i+27,450,8,5);}
            for(int i=0;i<WORLD_W;i+=700){g.setColor(new Color(9,19,31));g.fillRect(i+90,390,22,110);g.setColor(new Color(75,108,118));g.fillRect(i+96,400,4,90);g.setColor(new Color(246,174,75,170));g.fillRect(i+92,420,10,5);}
            g.setColor(new Color(15,25,40));g.fillRect(5850,215,220,285);g.setColor(new Color(55,112,133));g.fillRect(5870,235,180,265);g.setColor(new Color(12,27,45));g.fillRect(5900,265,120,235);g.setColor(new Color(255,190,90));g.fillRect(5948,305,24,8);}
        private void drawLadders(Graphics2D g){for(Ladder l:ladders){g.setColor(new Color(24,38,49));g.fillRect(l.x-5,l.top,7,l.bottom-l.top);g.fillRect(l.x+39,l.top,7,l.bottom-l.top);g.setColor(new Color(190,139,76));g.fillRect(l.x,l.top,4,l.bottom-l.top);g.fillRect(l.x+35,l.top,4,l.bottom-l.top);for(int yy=l.top+8;yy<l.bottom;yy+=13){g.setColor(new Color(226,176,101));g.fillRect(l.x,yy,39,4);g.setColor(new Color(104,83,65));g.drawLine(l.x,yy+4,l.x+38,yy+4);}}}
        private void drawEntities(Graphics2D g){for(Bomb b:bombs){g.setColor(new Color(40,43,48));g.fillOval((int)b.x,(int)b.y,15,15);g.setColor(new Color(255,116,63));g.fillRect((int)b.x+9,(int)b.y-3,5,5);g.setColor(new Color(255,211,116));g.fillRect((int)b.x+11,(int)b.y-5,3,3);}
            for(Crate c:crates)if(!c.broken){g.setColor(new Color(116,77,47));g.fillRect(c.x,c.y,30,30);g.setColor(new Color(220,164,89));g.drawRect(c.x,c.y,29,29);g.drawLine(c.x,c.y,c.x+30,c.y+30);g.drawLine(c.x+30,c.y,c.x,c.y+30);}
            for(Enemy e:enemies)if(e.alive){int ex=(int)e.x,ey=(int)e.y;g.setColor(new Color(83,42,102));g.fillRect(ex,ey,31,36);g.setColor(new Color(186,93,220));g.fillRect(ex+4,ey+5,23,18);g.setColor(new Color(255,220,120));g.fillRect(ex+7,ey+11,5,5);g.fillRect(ex+20,ey+11,5,5);g.setColor(Color.BLACK);g.fillRect(ex+2,ey-8,28,4);g.setColor(new Color(255,90,110));g.fillRect(ex+2,ey-8,Math.max(0,26*e.hp/3),4);g.setColor(new Color(140,87,177));g.fillRect(ex+5,ey+36,7,8);g.fillRect(ex+20,ey+36,7,8);}
            drawPlayer(g);
            for(Shot s:shots){g.setColor(new Color(255,220,125));g.fillRect((int)s.x,(int)s.y,13,4);g.setColor(new Color(255,130,70,110));g.fillRect((int)s.x-5,(int)s.y-3,20,10);}for(Particle p:particles){g.setColor(new Color(p.color.getRed(),p.color.getGreen(),p.color.getBlue(),Math.min(255,p.life*10)));g.fillRect((int)p.x,(int)p.y,4,4);}}

        private void drawPlayer(Graphics2D g){int px=(int)x,py=(int)y;int step=(int)(Math.abs(vx)>.1?(tick/5)%2:0);boolean blink=hurtCooldown>0&&tick%8<4;if(blink)return;
            // mochila y silueta
            g.setColor(new Color(30,53,69));g.fillRect(px+(facingRight?1:25),py+16,7,16);
            g.setColor(new Color(46,78,99));g.fillRect(px+5,py+15,23,20);g.setColor(new Color(39,111,137));g.fillRect(px+7,py+17,19,14);
            g.setColor(new Color(225,174,119));g.fillRect(px+9,py+3,15,15);g.setColor(new Color(48,35,33));g.fillRect(px+8,py+1,17,5);g.fillRect(px+7,py+5,4,6);
            g.setColor(new Color(255,226,171));g.fillRect(px+(facingRight?19:9),py+9,4,4);g.setColor(new Color(35,39,49));g.fillRect(px+(facingRight?21:10),py+10,2,2);
            g.setColor(new Color(197,73,59));g.fillRect(px+10,py+18,13,4);g.setColor(new Color(220,190,116));g.fillRect(px+13,py+22,6,3);
            g.setColor(new Color(28,38,52));int legY=py+33;g.fillRect(px+8,legY+(step==1?2:0),7,8);g.fillRect(px+19,legY+(step==0?2:0),7,8);g.setColor(new Color(96,133,147));g.fillRect(px+7,py+39+(step==1?2:0),9,3);g.fillRect(px+18,py+39+(step==0?2:0),9,3);
            g.setColor(new Color(40,48,60));g.fillRect(px+(facingRight?24:0),py+20,9,5);g.setColor(new Color(157,177,180));g.fillRect(px+(facingRight?29:0),py+19,5,3);
        }
        private void drawOverlay(Graphics2D g){g.setColor(new Color(5,12,23,190));g.fillRoundRect(18,16,Math.min(520,getWidth()-36),56,10,10);g.setColor(new Color(143,218,240));g.setFont(Recursos.fuentePixel(Font.BOLD,9));g.drawString(notice,32,39);g.setColor(new Color(220,230,235));g.setFont(Recursos.fuentePixel(Font.PLAIN,8));g.drawString("PROGRESO "+(int)(100*x/WORLD_W)+"%   ·   PUNTO DE CONTROL "+checkpoint,32,58);
            if(paused){g.setColor(new Color(0,0,0,170));g.fillRect(0,0,getWidth(),getHeight());g.setColor(Color.WHITE);g.setFont(Recursos.fuentePixel(Font.BOLD,22));g.drawString("PAUSA",getWidth()/2-45,getHeight()/2);}}
    }
}

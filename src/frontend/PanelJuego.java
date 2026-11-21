package frontend;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/** Pausa los temporizadores al ocultar el nivel: ninguna pantalla oculta modifica la partida. */
public abstract class PanelJuego extends JPanel {
    private final List<Timer> timers = new ArrayList<>();
    private final List<Timer> pausados = new ArrayList<>();
    protected Timer temporizador(int intervalo, ActionListener accion) {
        timers.removeIf(t -> !t.isRunning() && !pausados.contains(t));
        Timer timer = new Timer(intervalo, null);
        timer.addActionListener(e -> {
            if (!timer.isRepeats()) timers.remove(timer);
            accion.actionPerformed(e);
        });
        timers.add(timer);
        return timer;
    }
    @Override public void removeNotify() {
        pausados.clear();
        for (Timer timer : timers) {
            if (timer.isRunning()) pausados.add(timer);
            timer.stop();
        }
        super.removeNotify();
    }
    @Override public void addNotify() {
        super.addNotify();
        for (Timer timer : pausados) timer.start();
        pausados.clear();
    }
    public void detener() {
        for (Timer timer : timers) timer.stop();
        pausados.clear();
    }
}

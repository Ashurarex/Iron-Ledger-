package ui.animation;

import javax.swing.Timer;
import javax.swing.SwingUtilities;
import java.util.function.Consumer;

public class Animator {
    public interface Easing {
        float ease(float t);
    }

    public static final Easing LINEAR = t -> t;
    public static final Easing EASE_OUT_CUBIC = Animator::easeOutCubic;
    public static final Easing EASE_IN_OUT = Animator::easeInOut;

    public static final boolean ANIMATIONS_ENABLED =
        Boolean.parseBoolean(System.getProperty("ironledger.animations", "true"));

    private static final int TIMER_DELAY_MS = 16;

    private final int durationMs;
    private final Easing easing;
    private final Consumer<Float> updateCallback;
    private final Runnable completionCallback;
    private final Timer timer;

    private long startTimeMs;
    private boolean running;

    public Animator(int durationMs, Easing easing, Consumer<Float> updateCallback) {
        this(durationMs, easing, updateCallback, null);
    }

    public Animator(int durationMs, Easing easing, Consumer<Float> updateCallback, Runnable completionCallback) {
        this.durationMs = Math.max(1, durationMs);
        this.easing = easing == null ? LINEAR : easing;
        this.updateCallback = updateCallback;
        this.completionCallback = completionCallback;
        this.timer = new Timer(TIMER_DELAY_MS, e -> onTick());
        this.timer.setCoalesce(true);
    }

    public void start() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::start);
            return;
        }

        if (!ANIMATIONS_ENABLED) {
            running = false;
            timer.stop();
            if (updateCallback != null) {
                updateCallback.accept(1f);
            }
            if (completionCallback != null) {
                completionCallback.run();
            }
            return;
        }

        startTimeMs = System.currentTimeMillis();
        running = true;
        if (updateCallback != null) {
            updateCallback.accept(0f);
        }
        timer.start();
    }

    public void stop() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::stop);
            return;
        }

        if (!running && !timer.isRunning()) {
            return;
        }
        running = false;
        timer.stop();
    }

    public boolean isRunning() {
        return running;
    }

    private void onTick() {
        float raw = Math.min(1f, (float) (System.currentTimeMillis() - startTimeMs) / durationMs);
        float eased = easing.ease(clamp(raw));

        if (updateCallback != null) {
            updateCallback.accept(eased);
        }

        if (raw >= 1f) {
            timer.stop();
            running = false;
            if (completionCallback != null) {
                completionCallback.run();
            }
        }
    }

    public static float easeOutCubic(float t) {
        float clamped = clamp(t);
        float inverse = 1f - clamped;
        return 1f - inverse * inverse * inverse;
    }

    public static float easeInOut(float t) {
        float clamped = clamp(t);
        if (clamped < 0.5f) {
            return 4f * clamped * clamped * clamped;
        }
        float inverse = (float) Math.pow(-2f * clamped + 2f, 3);
        return 1f - inverse / 2f;
    }

    public static float clamp(float value) {
        if (value < 0f) {
            return 0f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }
}

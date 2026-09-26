package it.ld.libgdx.utils;

import com.badlogic.gdx.utils.Timer;

public class Debouncer {
    private Timer.Task pending;
    
    public void debounce(final float delaySeconds, final Runnable action) {
        if (pending != null) {
            pending.cancel();
            pending = null;
        }

        pending = new Timer.Task() {
            @Override
            public void run() {
                action.run();
            }
        };
        
        Timer.schedule(pending, delaySeconds);
    }

    public void cancel() {
        if (pending != null) {
            pending.cancel();
            pending = null;
        }
    }
}
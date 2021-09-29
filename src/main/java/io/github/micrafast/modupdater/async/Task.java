package io.github.micrafast.modupdater.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Task<P, E extends Throwable> extends Thread {
    private static final Log log = LogFactory.getLog(Task.class);
    private P progressValue;
    private E exception = null;
    private boolean hadStarted = false;

    public void startExecute() {
        if (!this.hadStarted) {
            this.start();
        }
    }

    @Override
    public void run() {
        if (!hadStarted) {
            hadStarted = true;
        }
        try {
            this.execute();
        } catch (Throwable e) {
            try {
                exception = (E) e;
            } catch (ClassCastException e2) {
                log.error(e2);
            }
        }
    }

    protected abstract void execute() throws E;
    public double getPercent() {
        return (this.hadStarted && (!this.isAlive())) ? 100 : 0;
    }
    public boolean completed() {
        return this.getPercent() >= 100.0;
    }
    protected void setProgress(P progress) {
        this.progressValue = progress;
    }
    public P getProgress() {
        return this.progressValue;
    }
    public boolean hasRunIntoException() {
        return exception != null;
    }
    public E getException() {
        return exception;
    }

}

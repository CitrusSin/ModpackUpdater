package io.github.citrussin.modupdater.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Task<P> extends Thread {
    private static final Log log = LogFactory.getLog(Task.class);
    private P progressValue;
    private Throwable exception = null;
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
            exception = e;
        }
    }

    protected abstract void execute() throws Throwable;
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
    public Throwable getException() {
        return exception;
    }

}

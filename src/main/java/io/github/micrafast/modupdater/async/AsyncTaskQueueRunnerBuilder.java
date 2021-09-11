package io.github.micrafast.modupdater.async;

import java.util.LinkedList;
import java.util.Queue;

public class AsyncTaskQueueRunnerBuilder<T extends Task<? extends P,? extends E>, P, E extends Throwable> {
    private Queue<T> taskQueue = new LinkedList<>();

    private int maxThreadCount = 10;
    private int watchDelayMillis = 10;

    public AsyncTaskQueueRunnerBuilder<T,P,E> addTask(T task) {
        taskQueue.add(task);
        return this;
    }

    public AsyncTaskQueueRunnerBuilder<T,P,E> setMaxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
        return this;
    }

    public AsyncTaskQueueRunnerBuilder<T,P,E> setWatchDelayMillis(int watchDelayMillis) {
        this.watchDelayMillis = watchDelayMillis;
        return this;
    }

    public AsyncTaskQueueRunner<T,P,E> build() {
        return new AsyncTaskQueueRunner<>(maxThreadCount, watchDelayMillis, taskQueue);
    }
}

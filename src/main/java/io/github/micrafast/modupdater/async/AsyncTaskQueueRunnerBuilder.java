package io.github.micrafast.modupdater.async;

import java.util.LinkedList;
import java.util.Queue;

public class AsyncTaskQueueRunnerBuilder<T extends Task<? extends P>, P> {
    private Queue<T> taskQueue = new LinkedList<>();

    private int maxThreadCount = 10;
    private int watchDelayMillis = 10;

    public AsyncTaskQueueRunnerBuilder<T,P> addTask(T task) {
        taskQueue.add(task);
        return this;
    }

    public AsyncTaskQueueRunnerBuilder<T,P> setMaxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
        return this;
    }

    public AsyncTaskQueueRunnerBuilder<T,P> setWatchDelayMillis(int watchDelayMillis) {
        this.watchDelayMillis = watchDelayMillis;
        return this;
    }

    public AsyncTaskQueueRunner<T,P> build() {
        return new AsyncTaskQueueRunner<>(maxThreadCount, watchDelayMillis, taskQueue);
    }
}

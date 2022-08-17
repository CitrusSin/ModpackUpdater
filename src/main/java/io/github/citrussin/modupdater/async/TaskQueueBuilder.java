package io.github.citrussin.modupdater.async;

import java.util.LinkedList;
import java.util.Queue;

public class TaskQueueBuilder<T extends Task<? extends P>, P> {
    private Queue<T> taskQueue = new LinkedList<>();

    private int maxThreadCount = 10;
    private int watchDelayMillis = 10;

    public TaskQueueBuilder<T,P> addTask(T task) {
        taskQueue.add(task);
        return this;
    }

    public TaskQueueBuilder<T,P> setMaxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
        return this;
    }

    public TaskQueueBuilder<T,P> setWatchDelayMillis(int watchDelayMillis) {
        this.watchDelayMillis = watchDelayMillis;
        return this;
    }

    public TaskQueue<T,P> build() {
        return new TaskQueue<>(maxThreadCount, watchDelayMillis, taskQueue);
    }
}

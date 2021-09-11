package io.github.micrafast.modupdater.async;

import java.util.LinkedList;
import java.util.Queue;

public class AsyncTaskQueueRunnerBuilder<T extends Task<? extends P,? extends E>, P, E extends Throwable> {
    private Queue<T> taskQueue = new LinkedList<>();

    public AsyncTaskQueueRunnerBuilder<T,P,E> addTask(T task) {
        taskQueue.add(task);
        return this;
    }

    public AsyncTaskQueueRunner<T,P,E> build() {
        return new AsyncTaskQueueRunner<>(taskQueue);
    }
}

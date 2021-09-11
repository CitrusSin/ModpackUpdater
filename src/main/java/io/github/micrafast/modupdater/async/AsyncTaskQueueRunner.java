package io.github.micrafast.modupdater.async;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AsyncTaskQueueRunner<T extends Task<? extends P,? extends E>, P, E extends Throwable> {
    protected int maxThreadCount;
    protected final Queue<T> waitingTasksQueue;
    protected final Set<T> runningTasksSet = new HashSet<>();
    protected final Set<T> finishedTasksSet = new HashSet<>();
    protected WatchThread watchThread;
    protected boolean isRunning = false;

    private final int watchDelayMillis;

    public AsyncTaskQueueRunner() {
        this(10, 10);
    }

    public AsyncTaskQueueRunner(Queue<T> waitingTasksQueue) {
        this(10,10, waitingTasksQueue);
    }

    public AsyncTaskQueueRunner(int maxThreadCount, int watchDelayMillis) {
        this(maxThreadCount, watchDelayMillis, new LinkedList<>());
    }

    public AsyncTaskQueueRunner(int maxThreadCount, int watchDelayMillis, Queue<T> waitingTasksQueue) {
        this.maxThreadCount = maxThreadCount;
        this.watchDelayMillis = watchDelayMillis;
        this.waitingTasksQueue = waitingTasksQueue;
    }

    public int getMaxThreadCount() {
        return maxThreadCount;
    }

    public void setMaxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
    }

    public boolean running() {
        return isRunning;
    }

    public synchronized boolean hasRemainingTasks() {
        return !(waitingTasksQueue.isEmpty() && runningTasksSet.isEmpty());
    }

    public synchronized int getTotalTasksCount() {
        return waitingTasksQueue.size() + runningTasksSet.size() + finishedTasksSet.size();
    }

    public synchronized void forEachRunning(Consumer<? super T> consumer) {
        runningTasksSet.forEach(consumer);
    }

    public synchronized void forEachExceptionThrown(BiConsumer<? super T, ? super E> consumer) {
        for (T task : finishedTasksSet) {
            if (task.hasRunIntoException()) {
                E exception = task.getException();
                consumer.accept(task, exception);
            }
        }
    }

    public boolean hasExceptionThrown() {
        AtomicBoolean bool = new AtomicBoolean(false);
        this.forEachExceptionThrown((t, e) -> bool.set(true));
        return bool.get();
    }

    public synchronized boolean addTask(T task) {
        return waitingTasksQueue.offer(task);
    }

    public synchronized double getPercent() {
        double totalPctValue = 0;
        for (T task : runningTasksSet) {
            totalPctValue += task.getPercent();
        }
        totalPctValue += finishedTasksSet.size() * 100.0;
        totalPctValue /= this.getTotalTasksCount();
        return totalPctValue;
    }

    public void asyncRunTaskQueue() {
        if (!this.running()) {
            synchronized (this) {
                this.isRunning = true;
                watchThread = new WatchThread();
                watchThread.start();
            }
        }
    }

    public void interrupt() {
        if (this.running()) {
            synchronized (this) {
                watchThread.interrupt();
            }
        }
    }

    protected void runTaskQueue() {
        this.isRunning = true;
        while (this.hasRemainingTasks()) {
            synchronized (this) {
                while (runningTasksSet.size() < maxThreadCount && (!waitingTasksQueue.isEmpty())) {
                    T task = waitingTasksQueue.poll();
                    task.startExecute();
                    runningTasksSet.add(task);
                }
                runningTasksSet.removeIf(task -> (!task.isAlive()) || (task.completed()));
            }
            try {
                Thread.sleep(watchDelayMillis);
            } catch (InterruptedException e) {
                break;
            }
        }
        synchronized (this) {
            this.isRunning = false;
        }
    }

    class WatchThread extends Thread {
        @Override
        public void run() {
            AsyncTaskQueueRunner.this.runTaskQueue();
        }
    }
}

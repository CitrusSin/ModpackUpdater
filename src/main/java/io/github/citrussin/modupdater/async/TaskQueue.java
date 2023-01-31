package io.github.citrussin.modupdater.async;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TaskQueue<T extends Task<? extends P>, P> {
    protected int maxThreadCount;
    protected final Queue<T> waitingTasksQueue;
    protected final Set<T> runningTasksSet = new HashSet<>();
    protected final Set<T> finishedTasksSet = new HashSet<>();
    protected final List<Consumer<TaskQueue<T,P>>> watchingCallbacks = new LinkedList<>();
    protected final List<BiConsumer<? super T, ? super Throwable>> exceptionCallbacks = new LinkedList<>();
    protected Thread watchThread;
    protected boolean isRunning = false;

    private final int watchDelayMs;

    public TaskQueue(int maxThreadCount, int watchDelayMillis, Queue<T> waitingTasksQueue) {
        this.maxThreadCount = maxThreadCount;
        this.watchDelayMs = watchDelayMillis;
        this.waitingTasksQueue = waitingTasksQueue;
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

    public void addWatchCallback(Consumer<TaskQueue<T,P>> callback) {
        synchronized (watchingCallbacks) {
            watchingCallbacks.add(callback);
        }
    }

    public void addExceptionCallback(BiConsumer<? super T, ? super Throwable> callback) {
        synchronized (exceptionCallbacks) {
            exceptionCallbacks.add(callback);
        }
    }

    public synchronized void forEachRunning(Consumer<? super T> consumer) {
        runningTasksSet.forEach(consumer);
    }

    public synchronized void forEachFinished(Consumer<? super T> consumer) {
        finishedTasksSet.forEach(consumer);
    }

    public synchronized void forEachExceptionThrown(BiConsumer<? super T, ? super Throwable> consumer) {
        for (T task : finishedTasksSet) {
            if (task.hasRunIntoException()) {
                Throwable exception = task.getException();
                consumer.accept(task, exception);
            }
        }
    }

    public Throwable[] getExceptionsThrown() {
        List<Throwable> list = new LinkedList<>();
        this.forEachExceptionThrown((t, e) -> list.add(e));
        Throwable[] array = new Throwable[list.size()];
        return list.toArray(array);
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
        double totalPctValue = 0d;
        for (T task : runningTasksSet) {
            totalPctValue += task.getPercent();
        }
        totalPctValue += finishedTasksSet.size() * 100.0;
        totalPctValue /= this.getTotalTasksCount();
        return totalPctValue;
    }

    public void runTaskQueue() {
        if (!this.running()) {
            synchronized (this) {
                this.isRunning = true;
                watchThread = new Thread(() -> {
                    try {
                        this.runTaskQueueWithThreadBlock();
                    } catch (InterruptedException ignored) {}
                });
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

    public void runTaskQueueWithThreadBlock() throws InterruptedException {
        this.isRunning = true;
        while (this.hasRemainingTasks()) {
            synchronized (this) {
                while (runningTasksSet.size() < maxThreadCount && (!waitingTasksQueue.isEmpty())) {
                    T task = waitingTasksQueue.poll();
                    task.startExecute();
                    runningTasksSet.add(task);
                }
                // Clean finished tasks and crashed tasks
                final Iterator<T> iterator = runningTasksSet.iterator();
                while (iterator.hasNext()) {
                    T task = iterator.next();
                    if (task.hasRunIntoException()) {
                        synchronized (exceptionCallbacks) {
                            exceptionCallbacks.forEach(callback -> callback.accept(task, task.getException()));
                        }
                        finishedTasksSet.add(task);
                        iterator.remove();
                    } else if ((!task.isAlive()) || (task.completed())) {
                        finishedTasksSet.add(task);
                        iterator.remove();
                    }
                }
                // Regularly call registered callbacks
                synchronized (this.watchingCallbacks) {
                    for (Consumer<TaskQueue<T,P>> callback : watchingCallbacks) {
                        callback.accept(this);
                    }
                }
            }
            Thread.sleep(watchDelayMs);
        }
        synchronized (this) {
            this.isRunning = false;
        }
    }
}

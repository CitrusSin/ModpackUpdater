package io.github.micrafast.modupdater.async;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AsyncTaskQueueRunner<T extends Task<? extends P,? extends E>, P, E extends Throwable> {
    protected int maxThreadCount;
    protected final Queue<T> waitingTasksQueue;
    protected final Set<T> runningTasksSet = new HashSet<>();
    protected final Set<T> finishedTasksSet = new HashSet<>();
    protected final List<Consumer<AsyncTaskQueueRunner<T,P,E>>> watchingCallbacks = new LinkedList<>();
    protected final List<BiConsumer<T,E>> exceptionCallbacks = new LinkedList<>();
    protected Thread watchThread;
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

    public boolean addWatchCallback(Consumer<AsyncTaskQueueRunner<T,P,E>> callback) {
        synchronized (watchingCallbacks) {
            return watchingCallbacks.add(callback);
        }
    }

    public void removeWatchCallbackIf(Predicate<Consumer<AsyncTaskQueueRunner<T,P,E>>> predicate) {
        synchronized (watchingCallbacks) {
            watchingCallbacks.removeIf(predicate);
        }
    }

    public boolean addExceptionCallback(BiConsumer<T,E> callback) {
        synchronized (exceptionCallbacks) {
            return exceptionCallbacks.add(callback);
        }
    }

    public void removeExceptionCallbackIf(Predicate<BiConsumer<T,E>> predicate) {
        synchronized (exceptionCallbacks) {
            exceptionCallbacks.removeIf(predicate);
        }
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
        double totalPctValue = 0d;
        for (T task : runningTasksSet) {
            totalPctValue += task.getPercent();
        }
        totalPctValue += finishedTasksSet.size() * 100.0;
        totalPctValue /= (double)this.getTotalTasksCount();
        return totalPctValue;
    }

    public void runTaskQueue() {
        if (!this.running()) {
            synchronized (this) {
                this.isRunning = true;
                watchThread = new Thread(this::runTaskQueueWithThreadBlock);
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

    public void runTaskQueueWithThreadBlock() {
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
                    for (Consumer<AsyncTaskQueueRunner<T,P,E>> callback : watchingCallbacks) {
                        callback.accept(this);
                    }
                }
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
}

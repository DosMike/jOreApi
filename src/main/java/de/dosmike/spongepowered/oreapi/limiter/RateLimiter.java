package de.dosmike.spongepowered.oreapi.limiter;

import de.dosmike.spongepowered.oreapi.utility.TracingThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

/** if you're concerned about dosing the ore servers, take this.
 * This is a management thread for the tasks, that'll wait for a limiter
 * to approve the next task to be executed in a async executor. */
public class RateLimiter extends Thread {

    boolean running = true;
    List<CompletableTask<?>> tasks = new LinkedList<>();
    private final Object taskMutex = new Object();
    private List<Runnable> onIdleCallbacks=new LinkedList<>();
    private List<Runnable> onIdleOnceCallbacks=new LinkedList<>();
    private final Object idleMutex = new Object();
    private Limiter limit;
    private ExecutorService exec = Executors.newFixedThreadPool(1);

    public RateLimiter(Limiter limiter) {
        this.limit = limiter;
        try {
            setName("Ore Query Limiter");
            setUncaughtExceptionHandler(TracingThreadFactory.exceptionTracePrinter);
        } catch (SecurityException ignore) {}
    }
    public RateLimiter() {
        this(new BucketLimiter(2,80)); //bucket is faster than averaging
    }

    @Override
    public void run() {
        boolean nowIdle;
        while(running) {
            CompletableTask<?> task = null;
            synchronized (taskMutex) {
                if (!tasks.isEmpty())
                    task = tasks.remove(0);
            }
            if (task != null) { //execute next task
                Future<?> currentTask = null;
                try {
                    currentTask = exec.submit(task);
                    currentTask.get();
                    task.notifyOwner();
                } catch (InterruptedException i) {
                    currentTask.cancel(true);
                    task.cancel();
                    halt(); break;
                } catch (ExecutionException wrapped) {
                    task.notifyOwnerExceptional(wrapped.getCause());
                }
                //on idle callback
                synchronized (taskMutex) {
                    nowIdle = tasks.isEmpty();
                }
                if (nowIdle) {
                    onIdleNotify();
                }
            } else nowIdle = true;
            // after a task (!canRequest) definitively wait for the next to be requestable
            try {
                if (!limit.canRequest()) {
                    limit.waitForNext();
                } else if (nowIdle) { //if we did no yield otherwise, let it do so here
                    Thread.sleep(100);
                }
            } catch (InterruptedException interrupt) {
                halt();
                break;
            }
        }
        Logger.getLogger(getName()).fine("Rate Limiter terminated");
    }

    public <T> CompletableFuture<T> enqueue(Supplier<T> task) {
        if (!isAlive()) throw new IllegalStateException("The rate limiter has already terminated");
        CompletableTask<T> future = new CompletableTask<>(task);
        synchronized (taskMutex) {
            tasks.add(future);
        }
        return future.getFuture();
    }

    /** Shut down this RateLimiter after the current task finished */
    public void halt() {
        running = false;
        synchronized (taskMutex) {
            tasks.forEach(CompletableTask::cancel);
            tasks.clear();
        }
        exec.shutdownNow(); //notify running task
    }

    /** specify a runnable that gets executed after the last task ran. Does not execute if the limiter starts idle */
    public void addOnIdleListener(Runnable whenIdle, boolean once) {
        synchronized (idleMutex) {
            (once ? onIdleOnceCallbacks : onIdleCallbacks).add(whenIdle);
        }
    }
    public void removeOnIdleListener(Runnable listener) {
        synchronized (idleMutex) {
            onIdleCallbacks.remove(listener);
            onIdleOnceCallbacks.remove(listener);
        }
    }
    private void onIdleNotify() {
        // unlock before execution to allow modification of the callback lists from within the callbacks
        List<Runnable> collective = new LinkedList<>();
        synchronized (idleMutex) {
            collective.addAll(onIdleCallbacks);
            collective.addAll(onIdleOnceCallbacks);
            onIdleOnceCallbacks.clear();
        }
        collective.forEach(r->{try{r.run();}catch (Throwable t){/**/}});
    }

    public synchronized void takeRequest() {
        limit.takeRequest();
    }

}

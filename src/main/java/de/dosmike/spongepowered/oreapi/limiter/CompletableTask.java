package de.dosmike.spongepowered.oreapi.limiter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/** The idea behind CompletableTask is, that the execution can be delayed arbitrarily.
 * In contrast to CompletableFuture supplyAsync that tries to immediately execute a
 * time heavy task */
public class CompletableTask<T> implements Runnable {

    private CompletableFuture<T> userExposed;
    private Supplier<T> rawTask;
    private T result;

    public CompletableTask(Supplier<T> task) {
        rawTask = task;
        userExposed = new CompletableFuture<>();
    }

    @Override
    public void run() {
        result = rawTask.get();
    }

    public void cancel() {
        userExposed.cancel(true);
    }

    public void notifyOwner() {
        userExposed.complete(result);
    }
    public void notifyOwnerExceptional(Throwable t) {
        userExposed.completeExceptionally(t);
    }

    public CompletableFuture<T> getFuture() {
        return userExposed;
    }

}

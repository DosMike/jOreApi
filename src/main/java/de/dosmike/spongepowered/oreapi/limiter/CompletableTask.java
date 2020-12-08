package de.dosmike.spongepowered.oreapi.limiter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * The idea behind CompletableTask is, that the execution can be delayed arbitrarily.
 * In contrast to CompletableFuture supplyAsync that tries to immediately execute a
 * time heavy task
 */
public class CompletableTask<T> implements Runnable {

    /** This {@link CompletableFuture} will be handed over to the end user.
     * Through that, the user can decide whether they want to use async completion stages or just join in the data.*/
    private CompletableFuture<T> userExposed;
    /**
     * The raw task to be executed.
     */
    private Supplier<T> rawTask;
    /**
     * Delayed storage for computation result, because {@link RateLimiter} is supposed to fist
     * do some internal error handling.
     */
    private T result;

    public CompletableTask(Supplier<T> task) {
        rawTask = task;
        userExposed = new CompletableFuture<>();
    }

    @Override
    public void run() {
        result = rawTask.get();
    }

    /** Notify user that the task was canceled and that there won't be a result */
    public void cancel() {
        userExposed.cancel(true);
    }

    /**
     * Hands over the internal result to the user
     */
    public void notifyOwner() {
        userExposed.complete(result);
    }

    /**
     * Sends away an exception that occurred during execution.
     */
    public void notifyOwnerExceptional(Throwable t) {
        userExposed.completeExceptionally(t);
    }

    /**
     * get the "user notification" object out of here
     *
     * @return the CompletableFuture for user notification
     */
    public CompletableFuture<T> getFuture() {
        return userExposed;
    }

}

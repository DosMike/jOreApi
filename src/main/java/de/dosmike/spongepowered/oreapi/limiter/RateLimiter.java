package de.dosmike.spongepowered.oreapi.limiter;

import de.dosmike.spongepowered.oreapi.utility.TracingThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * If you're concerned about dosing the ore service, take this.
 * This is a management thread for the tasks, that'll wait for a limiter
 * to approve the next task to be executed in a async executor.
 * Since this is about preventing
 */
public class RateLimiter extends Thread {

	private boolean running = true;
	private final List<CompletableTask<?>> tasks = new LinkedList<>();
	private final Object taskMutex = new Object();
	private final List<Runnable> onIdleCallbacks = new LinkedList<>();
	private final List<Runnable> onIdleOnceCallbacks = new LinkedList<>();
	private final Object idleMutex = new Object();
	private final Limiter limit;
	private final ExecutorService exec = Executors.newFixedThreadPool(1, new TracingThreadFactory());

	public RateLimiter(Limiter limiter) {
		this.limit = limiter;
		try {
			setName("Ore Query Limiter");
			setUncaughtExceptionHandler(TracingThreadFactory.exceptionTracePrinter);
		} catch (SecurityException ignore) {
		}
	}

	public RateLimiter() {
		this(new BucketLimiter(2, 80)); //bucket is faster than averaging
	}

	@Override
	public void run() {
		boolean nowIdle;
		while (running) {
			CompletableTask<?> task = null;
			//retrieve next task
			synchronized (taskMutex) {
				if (!tasks.isEmpty())
					task = tasks.remove(0);
			}
			//execute next task outside of mutex
			if (task != null) {
				Future<?> currentTask = null;
				try {
					currentTask = exec.submit(task);
					currentTask.get();
					// success
					task.notifyOwner();
				} catch (InterruptedException i) {
					// termination
					currentTask.cancel(true);
					task.cancel();
					halt();
					break;
				} catch (ExecutionException wrapped) {
					// failure
					task.notifyOwnerExceptional(wrapped.getCause());
				}
				//on idle callback
				synchronized (taskMutex) {
					nowIdle = tasks.isEmpty();
				}
				//again do actual work outside of mutex
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
				//termination
				halt();
				break;
			}
		}
		Logger.getLogger(getName()).fine("Rate Limiter terminated");
	}

	/**
	 * Enqueues a task as CompletableTask that awaits execution.
	 * Returns the CompletableFuture that will receive results.
	 */
	public <T> CompletableFuture<T> enqueue(Supplier<T> task) {
		if (!isAlive()) throw new IllegalStateException("The rate limiter has already terminated");
		CompletableTask<T> future = new CompletableTask<>(task);
		synchronized (taskMutex) {
			tasks.add(future);
		}
		return future.getFuture();
	}

	/**
	 * Shut down this RateLimiter after the current task finished.
	 * All enqueues tasks will be canceled as well.
	 */
	public void halt() {
		running = false;
		synchronized (taskMutex) {
			tasks.forEach(CompletableTask::cancel);
			tasks.clear();
		}
		exec.shutdownNow(); //notify running task
	}

	/**
	 * Specify a runnable that gets executed after the last task ran.
	 * Does not execute if the limiter starts idle.
	 *
	 * @param whenIdle the task to execute when the this {@link RateLimiter} runs out of things to do
	 * @param once     set true if you only want the task to execute once
	 */
	public void addOnIdleListener(Runnable whenIdle, boolean once) {
		synchronized (idleMutex) {
			(once ? onIdleOnceCallbacks : onIdleCallbacks).add(whenIdle);
		}
	}

	/**
	 * Remove the idle listener again.
	 *
	 * @param listener listener to remove.
	 */
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
		collective.forEach(r -> {
			try {
				r.run();
			} catch (Throwable t) {/**/}
		});
	}

	/**
	 * Proxy to this limiters {@link Limiter#takeRequest()}
	 */
	public synchronized void takeRequest() {
		limit.takeRequest();
	}

}

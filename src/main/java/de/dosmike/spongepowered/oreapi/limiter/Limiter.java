package de.dosmike.spongepowered.oreapi.limiter;

/**
 * Interface for request limiters
 */
public interface Limiter {

	/**
	 * @return true if a request can currently be made without delay
	 */
	boolean canRequest();

	/**
	 * @return the unix timestamp, where the next request can be made
	 */
	long nextRequestAt();

	/**
	 * blocks until a request can be made
	 */
	default void waitForNext() throws InterruptedException {
		long nextAt = nextRequestAt();
		while (nextAt - System.currentTimeMillis() > 0) {
			Thread.sleep(nextAt - System.currentTimeMillis());
		}
	}

	/**
	 * notifies the limiter that a request is going out basically now
	 */
	void takeRequest();

}

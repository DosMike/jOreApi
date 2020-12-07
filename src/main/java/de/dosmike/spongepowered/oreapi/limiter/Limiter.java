package de.dosmike.spongepowered.oreapi.limiter;

public interface Limiter {

    boolean canRequest();

    /** @return the unix timestamp, where the next request can be made */
    long nextRequestAt();

    /** blocks until a request can be made */
    default void waitForNext() throws InterruptedException {
        long nextAt = nextRequestAt();
        while (nextAt-System.currentTimeMillis() > 0) {
            Thread.sleep(nextAt-System.currentTimeMillis());
        }
    }

    void takeRequest();

}

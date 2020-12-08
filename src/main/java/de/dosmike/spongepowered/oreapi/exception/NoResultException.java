package de.dosmike.spongepowered.oreapi.exception;

/**
 * This exception wraps reasons for no results.
 * If this exception has a cause it was triggered through that.
 * Otherwise the message will most likely contain the http response-message.
 */
public class NoResultException extends RuntimeException {
    public NoResultException() {
    }

    public NoResultException(String message) {
        super(message);
    }

    public NoResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoResultException(Throwable cause) {
        super(cause);
    }
}

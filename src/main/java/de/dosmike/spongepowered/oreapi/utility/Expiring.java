package de.dosmike.spongepowered.oreapi.utility;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Object with lifetime
 */
public class Expiring<T> implements Serializable {
	protected T value;
	protected long expirationDate;

	protected Expiring() {
		value = null;
		expirationDate = 0L;
	}

	protected Expiring(T object, long lifespan) {
		value = object;
		expirationDate = System.currentTimeMillis() + lifespan;
	}

	public boolean isExpired() {
		return System.currentTimeMillis() > expirationDate;
	}

	public boolean isAlive() {
		return System.currentTimeMillis() <= expirationDate;
	}

	/**
	 * get this object, if it has not yet expired. otherwise throws exception
	 *
	 * @return the object
	 * @throws IllegalStateException if the object expired
	 */
	public T get() {
		if (isExpired())
			throw new IllegalStateException("This object expired");
		return value;
	}

	/**
	 * get this object, if it has not yet expired. otherwise other is returned
	 *
	 * @param other the value to return if the actual value expired
	 * @return a value
	 */
	public T orElse(T other) {
		return isExpired() ? other : value;
	}

	/**
	 * get this object, if it has not yet expired. otherwise other is supplied
	 *
	 * @param other the supplier to execute if the actual value expired
	 * @return a value
	 */
	public T orElseGet(Supplier<T> other) {
		return isExpired() ? other.get() : value;
	}

	/**
	 * applies consumer to object only if the object has not yet expired
	 *
	 * @param consumer a function consuming this value, if alive
	 */
	public void ifAlive(Consumer<T> consumer) {
		if (isAlive()) consumer.accept(value);
	}

	/**
	 * tries to get the object
	 *
	 * @return the object wrapped in a optional
	 */
	public Optional<T> poll() {
		return isExpired() ? Optional.empty() : Optional.of(value);
	}

	/**
	 * It is not recommended to use expired values. If you insist on using them
	 * anyways you can use this method.
	 *
	 * @return the value, regardless of it's expiration state
	 */
	public T getAnyways() {
		return value;
	}

	/**
	 * Makes this object expire at the specified time
	 *
	 * @param object a time limited object
	 * @param timeAt the unix timestamp in ms at which this object expires
	 * @param <Y>    wrapped element type
	 * @return the new expiring value
	 */
	public static <Y> Expiring<Y> expireAt(Y object, long timeAt) {
		return new Expiring<>(object, timeAt - System.currentTimeMillis());
	}

	/**
	 * Makes this object expire after some specified time
	 *
	 * @param object   a time limited object
	 * @param lifespan the time in ms after which this object expires
	 * @param <Y>      wrapped element type
	 * @return the new expiring value
	 */
	public static <Y> Expiring<Y> expireIn(Y object, long lifespan) {
		return new Expiring<>(object, lifespan);
	}

	/**
	 * Create an empty auto expired value.
	 *
	 * @param <Y> auto element type
	 * @return an expired Expiring
	 */
	public static <Y> Expiring<Y> expired() {
		return new Expiring<>();
	}

	/**
	 * @return the exact moment this value expires in epoch ms
	 */
	public long getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @return the remaining lifetime in ms
	 */
	public long getRemainingLifespan() {
		return Math.min(0, expirationDate - System.currentTimeMillis());
	}

	@Override
	public String toString() {
		return "Expiring<" + value + ">";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Expiring<?> expiring = (Expiring<?>) o;
		return expirationDate == expiring.expirationDate &&
				Objects.equals(value, expiring.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, expirationDate);
	}
}

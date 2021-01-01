package de.dosmike.spongepowered.oreapi.utility;

public class Pair<K, V> {
	K k;
	V v;

	public Pair(K first, V second) {
		k = first;
		v = second;
	}

	public K getFirst() {
		return k;
	}

	public void setFirst(K value) {
		k = value;
	}

	public V getSecond() {
		return v;
	}

	public void setSecond(V value) {
		v = value;
	}

}

package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.utility.Expiring;
import de.dosmike.spongepowered.oreapi.utility.RepositoryTimestamp;

import javax.net.ssl.HttpsURLConnection;
import java.io.Serializable;

/**
 * Represents the current session token. Should be destroyed with
 * {@link OreApiV2#destroySession()}
 */
public class OreSession extends Expiring<String> implements Serializable {

	public OreSession(JsonObject json) {
		try {
			value = json.get("session").getAsString();
			expirationDate = RepositoryTimestamp.toNative(json.get("expires").getAsString());
		} catch (Throwable e) {
			value = null;
			expirationDate = 0L;
		}
	}

	public OreSession() {
	}

	/**
	 * @param connection to authorize
	 * @return connection for piping
	 */
	public HttpsURLConnection authenticate(HttpsURLConnection connection) {
		if (isAlive())
			connection.setRequestProperty("Authorization", "OreApi session=" + value);
		return connection;
	}

	/**
	 * Should not be called manually.
	 */
	public void destroy() {
		value = null;
		expirationDate = 0L;
	}

}

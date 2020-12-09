package de.dosmike.spongepowered.oreapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.dosmike.spongepowered.oreapi.limiter.RateLimiter;
import de.dosmike.spongepowered.oreapi.netobject.OreSession;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

/**
 * This is the "business in the back" class of {@link OreApiV2}. This class does the basic work nobody else wants to do.
 * <br>
 * Please do me a favour and call {@link #terminate} when you're exiting, this class hosts a static thread that needs to
 * go at some point.<br>
 * <ul><li>{@link OreApiV2} Presents a nice interface with only the necessary methods. It utilizes the cache and, if
 * necessary calls into {@link NetTasks} for live data.</li>
 * <li>{@link NetTasks} Is the actual API implementation. Providing suppliers that can be scheduled in the Limiter held
 * by the {@link ConnectionManager}</li>
 * <li>{@link ConnectionManager} holds all the local API data including cache and session. It contains all sorts of
 * utility from building connection objects to destroying sessions</li></ul>
 */
public class ConnectionManager {

	//    public static final String baseUrl = "https://ore.spongepowered.org/";
	public static final String baseUrl = "https://staging-ore-vue.spongeproject.net/";
	private static final JsonParser parser = new JsonParser();
	final String application;
	private String apiKey = null;
	private static boolean verboseNetworkLogging = false;

	/**
	 * rate limiter is static, because we don't want connection spam through
	 * different plugins
	 */
	static RateLimiter limiter = null;
	private static final List<OreApiV2> instances = new LinkedList<>();
	OreSession session;
	ObjectCache cache;

	private ConnectionManager(String application) {
		verboseNetworkLogging = System.getProperty("verboseNetTrafficLogging") != null;
		this.application = application;
		session = new OreSession();
		if (limiter == null) {
			limiter = new RateLimiter();
			limiter.start();
		}
		cache = new ObjectCache();
	}

	/**
	 * Don't call this unless all connections are complete.
	 * This function will terminate the limiter, effectively preventing further calls through {@link OreApiV2}
	 */
	public static void terminate() {
		instances.forEach(OreApiV2::close);
		limiter.halt();
		System.out.println("Closed Connection Manager");
	}

	static void notifyClosed(OreApiV2 instance) {
		if (!instances.remove(instance))
			throw new IllegalStateException("This instance was already closed!");
	}

	public void withApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void clearApiKey() {
		apiKey = null;
	}

	public HttpsURLConnection createConnection(String method, String endpoint) throws IOException {
		if (verboseNetworkLogging) System.out.println(method + " " + endpoint);
		HttpsURLConnection connection = (HttpsURLConnection) new URL(ConnectionManager.baseUrl + "api/v2" + endpoint).openConnection();
		connection.setRequestMethod(method);
		connection.setInstanceFollowRedirects(true);
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		connection.setRequestProperty("User-Agent", application);
		connection.setRequestProperty("Content-Type", "application/json");
		return connection;
	}

	static JsonObject parseJson(HttpsURLConnection connection) throws IOException {
		JsonObject jobj = parser.parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
		if (verboseNetworkLogging) System.out.println("< " + jobj.toString());
		return jobj;
	}

	static HttpsURLConnection postData(HttpsURLConnection connection, String rawBody) throws IOException {
		if (verboseNetworkLogging) System.out.println("> " + rawBody);
		connection.setDoOutput(true);
		try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
			w.write(rawBody);
			w.flush();
		}
		return connection;
	}

	public OreSession getSession() {
		return session;
	}

	public ObjectCache getCache() {
		return cache;
	}

	public boolean authenticate() {
		if (session.isAlive()) return true;
		try {
			HttpsURLConnection connection = createConnection("POST", "/authenticate");
			if (apiKey != null)
				connection.setRequestProperty("Authorization", "OreApi apikey=" + URLEncoder.encode(apiKey, "UTF-8"));
			connection.setDoInput(true);
			if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
				tryPrintErrorBody(connection);
				return false;
			}
			session = new OreSession(parseJson(connection));
			return session.isAlive();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @return true if the session was confirmed as deleted by the api
	 */
	public boolean destroySession() {
		String sid = session.getAnyways();
		if (sid == null || sid.isEmpty()) return false; //session already nuked from orbit
		try {
			HttpsURLConnection connection = createConnection("POST", "/authenticate");
			if (apiKey != null)
				connection.setRequestProperty("Authorization", "OreApi apikey=" + URLEncoder.encode(apiKey, "UTF-8"));
			connection.setDoInput(true);
			if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
				tryPrintErrorBody(connection);
				return false;
			}
			session.destroy();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void tryPrintErrorBody(HttpsURLConnection connection) {
		try {
			System.err.println("Error Body for response " + connection.getResponseCode() + ": " + connection.getResponseMessage());
			InputStream in = connection.getErrorStream();
			if (in != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				String line;
				while ((line = br.readLine()) != null)
					System.err.println(line);
				br.close();
			}
		} catch (IOException ignore) {
		}
	}


	//Region builder
	public static class Builder {
		private String apiKey = null;
		private String application = "jOreApi/1.2 (by DosMike; Ore API V2)";

		private Builder() {
		}

		/**
		 * Every application and every plugin should use a separate user agent.
		 * Do NOT use a browser string!
		 * The API will ignore requests that don't use a valid user agent.
		 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent
		 */
		public Builder setApplication(String userAgent) {
			this.application = userAgent;
			return Builder.this;
		}

		/**
		 * Use the provided key for authentication and session creation.
		 * You can change this at a later points with {@link #withApiKey}
		 */
		public Builder setApiKey(String key) {
			this.apiKey = key;
			return Builder.this;
		}

		public OreApiV2 build() {
			ConnectionManager connection = new ConnectionManager(application);
			connection.withApiKey(apiKey);
			OreApiV2 instance = new OreApiV2(connection);
			instances.add(instance);
			return instance;
		}
	}

	static Builder builder() {
		return new Builder();
	}
	//endregion


}

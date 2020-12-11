package de.dosmike.spongepowered.oreapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.dosmike.spongepowered.oreapi.exception.MissingPermissionException;
import de.dosmike.spongepowered.oreapi.exception.NoResultException;
import de.dosmike.spongepowered.oreapi.netobject.*;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.dosmike.spongepowered.oreapi.ConnectionManager.tryPrintErrorBody;
import static de.dosmike.spongepowered.oreapi.utility.ReflectionHelper.friendField;

/**
 * If {@link OreApiV2} is "party in the front" and {@link ConnectionManager} is "business in the back", then this is?
 * This is the poor fella that has to carry the whole team.<br>
 * <ul><li>{@link OreApiV2} Presents a nice interface with only the necessary methods. It utilizes the cache and, if
 * necessary calls into {@link NetTasks} for live data.</li>
 * <li>{@link NetTasks} Is the actual API implementation. Providing suppliers that can be scheduled in the Limiter held
 * by the {@link ConnectionManager}</li>
 * <li>{@link ConnectionManager} holds all the local API data including cache and session. It contains all sorts of
 * utility from building connection objects to destroying sessions</li></ul>
 */
class NetTasks {

	//region util
	private static void auth(ConnectionManager cm) {
		if (!cm.authenticate())
			throw new IllegalStateException("Could not create API session");
	}

	private static String urlencoded(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static HttpsURLConnection connect(ConnectionManager connection, String method, String queryURI) throws IOException {
		auth(connection);
		ConnectionManager.limiter.takeRequest();
		return connection.session.authenticate(connection.createConnection(method, queryURI));
	}

	private static void checkResponseCode(HttpsURLConnection connection, OrePermission endPointPermission) throws IOException {
		int rc = connection.getResponseCode();
		if (rc < 200 || rc >= 400) {
			if (rc == 403 && endPointPermission != null)
				throw new MissingPermissionException(endPointPermission);
			else if (rc != 404) // not 403 && not 404 -> print
				tryPrintErrorBody(connection);
			throw new NoResultException(connection.getResponseMessage());
		}
		//otherwise we good
	}
	//endregion

	//region project
	static Supplier<OreProjectList> projectSearch(ConnectionManager cm, OreProjectFilter filter) {
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "GET", "/projects?" + filter.toString());
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				OreProjectList resultList = new OreProjectList(ConnectionManager.parseJsonObject(connection), OreProject.class, filter);
				for (OreProject p : resultList.getResult())
					cm.cache.cacheProject(p);
				return resultList;
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreProject> getProject(ConnectionManager cm, OreNamespace namespace) {
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "GET", "/projects/" + namespace.toURLEncode());
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				return cm.cache.cacheProject(new OreProject(ConnectionManager.parseJsonObject(connection)));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreProject> findByPluginId(ConnectionManager cm, String pluginId) {
		return () -> {
			OreProjectFilter filter = new OreProjectFilter();
			filter.setQuery(pluginId);
			OreProjectList result;
			do {
				result = projectSearch(cm, filter).get();
				Optional<OreProject> project = result.getResult().stream().filter(p -> p.getPluginId().equals(pluginId)).findAny();
				if (project.isPresent())
					return project.get();
				filter = result.getPagination().getQueryNext();
			} while (result.getPagination().hasMorePages());
			throw new NoResultException("All results checked");
		};
	}

	/**
	 * Use an OreProject.Builder builder instead of calling this manually.
	 * It also provides nice setters.
	 *
	 * @param cm      the api instance to send this through
	 * @param request request data
	 * @return the task requesting the project to be created
	 */
	static Supplier<OreProject> createProject(ConnectionManager cm, OreProjectTemplate request) {
		//prevent modification after validation until the task executes
		final String requestBody = JsonUtil.buildJson(request).toString();
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "POST", "/projects");
				connection.setDoInput(true);
				ConnectionManager.postData(connection, requestBody);
				checkResponseCode(connection, OrePermission.Create_Project);
				return cm.cache.cacheProject(new OreProject(ConnectionManager.parseJsonObject(connection)));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreProject> updateProject(ConnectionManager cm, OreProject project) {
		//get shadow namespace
		final OreNamespace ns = friendField(project, "shadowNamespace");
		final String requestBody = JsonUtil.buildJson(project, "patchProject").toString();
		return () -> {
			cm.cache.untrack(project);
			try {
				HttpsURLConnection connection = connect(cm, "PATCH", "/projects/" + ns.toURLEncode());
				connection.setDoInput(true);
				ConnectionManager.postData(connection, requestBody);
				checkResponseCode(connection, OrePermission.Edit_Subject_Settings);
				return cm.cache.cacheProject(new OreProject(ConnectionManager.parseJsonObject(connection)));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<Void> deleteProject(ConnectionManager cm, OreProjectReference project) {
		return () -> {
			try {
				cm.cache.untrack(project);
				HttpsURLConnection connection = connect(cm, "DELETE", "/projects/" + project.getNamespace().toURLEncode());
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.Hard_Delete_Project);
				return null;
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreMemberList> getMembers(ConnectionManager cm, OreProjectReference project) {
		return () -> {
			try {
				//i'll just just bump the limit since this endpoint is not implementing proper pagination
				// don't think there will ever be more that 100 members in a project
				HttpsURLConnection connection = connect(cm, "GET", "/projects/" + project.getNamespace().toURLEncode() + "/members?limit=100");
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				return new OreMemberList(ConnectionManager.parseJsonArray(connection));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<Void> setMembers(ConnectionManager cm, OreProjectReference project, Map<String, OreRole> roles) {
		//build post body
		JsonArray array = new JsonArray();
		roles.entrySet().stream().map(e -> {
			JsonObject entry = new JsonObject();
			entry.addProperty("name", e.getKey());
			entry.addProperty("role", e.getValue().name());
			return entry;
		}).forEach(array::add);
		String requestBody = array.toString();

		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "POST", "/projects/" + project.getNamespace().toURLEncode() + "/members");
				connection.setDoInput(true);
				ConnectionManager.postData(connection, requestBody);
				checkResponseCode(connection, OrePermission.Manage_Subject_Members);
				return null;
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	/**
	 * The api docs say this endpoint requires is_subject_member, but the permission
	 * is not listed in the NamedPermissions enum. don't know what's going on with that
	 */
	static Supplier<Map<Date, OreProjectStatsDay>> getProjectStats(ConnectionManager cm, OreProjectReference project, Date from, Date to) {
		final SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "GET",
						"/projects/" + project.getNamespace().toURLEncode() +
								"/stats?fromDate=" + urlencoded(dateOnly.format(from) + "&toDate=" + urlencoded(dateOnly.format(to))));
				connection.setDoInput(true);
				checkResponseCode(connection, null);
				JsonObject container = ConnectionManager.parseJsonObject(connection);
				Map<Date, OreProjectStatsDay> map = new HashMap<>();
				for (Map.Entry<String, JsonElement> entry : container.entrySet()) {
					map.put(dateOnly.parse(entry.getKey()), new OreProjectStatsDay(entry.getValue().getAsJsonObject()));
				}
				return map;
			} catch (IOException | ParseException e) {
				throw new NoResultException(e);
			}
		};
	}
	//endregion

	//region version
	static Supplier<OreVersionList> listVersions(ConnectionManager cm, OreProjectReference project, @Nullable OrePaginationFilter pagination) {
		return () -> {
			try {
				String totalQuery = "/projects/" + project.getNamespace().toURLEncode() + "/versions";
				if (pagination != null) {
					totalQuery += "?" + pagination.toString();
				}
				HttpsURLConnection connection = connect(cm, "GET", totalQuery);
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				OreVersionList resultList = new OreVersionList(ConnectionManager.parseJsonObject(connection), project, OreVersion.class, pagination);
				for (OreVersion v : resultList.getResult())
					cm.cache.cacheVersion(project.getPluginId().toLowerCase(), v);
				return resultList;
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreVersion> getVersion(ConnectionManager cm, OreProjectReference project, String versionName) {
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "GET", "/projects/" + project.getNamespace().toURLEncode() + "/versions/" + URLEncoder.encode(versionName, "UTF-8"));
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				return cm.cache.cacheVersion(project.getPluginId(), new OreVersion(project.toReference(), ConnectionManager.parseJsonObject(connection)));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<String> getVerionChangelog(ConnectionManager cm, OreVersion version) {
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "GET", "/projects/" + version.getProjectRef().getNamespace().toURLEncode() + "/versions/" + version.getURLSafeName() + "/changelog");
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				String changelog = ConnectionManager.parseJsonObject(connection).get("changelog").getAsString();
				System.out.println(changelog);
				version.updateChangelog(changelog);
				return changelog;
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<URL> getDownloadURL(ConnectionManager cm, OreVersion version) {
		return () -> {
			auth(cm);
			try {
				//if the plugin was reviewed, the url is static
				if (version.getReviewState().equals(OreReviewState.Reviewed))
					return new URL(ConnectionManager.baseUrl +
							version.getProjectRef().getNamespace().toURLEncode() + "/versions/" +
							version.getURLSafeName() + "/download");

				// If the plugin was not approved, you are supposed to be prompted
				// that the plugin might be risky to use.
				// Since the api alone can't do that, I'll just query the target URL
				URL requestUrl = new URL(ConnectionManager.baseUrl +
						version.getProjectRef().getNamespace().toURLEncode() + "/versions/" +
						version.getURLSafeName() + "/confirm?api=true");

				HttpsURLConnection connection = cm.session.authenticate((HttpsURLConnection) requestUrl.openConnection());
				connection.setInstanceFollowRedirects(true);
				connection.setRequestProperty("User-Agent", cm.application);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
					tryPrintErrorBody(connection);
					throw new NoResultException(connection.getResponseMessage());
				}
				JsonObject response = new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
				String string = response.get("url").getAsString();
				return new URL(string);
			} catch (Exception e) {
				throw new NoResultException(e);
			}
		};
	}
	//endregion

	//region permission
	static Supplier<OrePermissionGrant> getPermissions(ConnectionManager cm, String query) {
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "GET", "/permissions?" + query);
				connection.setDoInput(true);
				checkResponseCode(connection, null);
				return new OrePermissionGrant(ConnectionManager.parseJsonObject(connection));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<Boolean> checkPermissions(ConnectionManager cm, String query, Collection<OrePermission> perms, boolean anyEnough) {
		return () -> {
			try {
				String fullQuery = "/permissions/" + (anyEnough ? "hasAny" : "hasAll") + "?" + query;
				if (!query.isEmpty()) fullQuery += "&";
				fullQuery += perms.stream().map(p -> "permissions=" + urlencoded(p.name().toLowerCase(Locale.ROOT))).collect(Collectors.joining("&"));
				HttpsURLConnection connection = connect(cm, "GET", fullQuery);
				connection.setDoInput(true);
				checkResponseCode(connection, null);
				return ConnectionManager.parseJsonObject(connection).get("result").getAsBoolean();
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}
	//endregion
}

package de.dosmike.spongepowered.oreapi.routes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.dosmike.spongepowered.oreapi.ConnectionManager;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.exception.MissingPermissionException;
import de.dosmike.spongepowered.oreapi.exception.NoResultException;
import de.dosmike.spongepowered.oreapi.netobject.*;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import de.dosmike.spongepowered.oreapi.utility.MultiPartFormData;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.dosmike.spongepowered.oreapi.ConnectionManager.*;
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

	/**
	 * Creates a authenticated connection to the api to the requested endpoint.
	 * Uses the default content type "application/json"
	 */
	private static HttpsURLConnection connect(ConnectionManager connection, String method, String queryURI) throws IOException {
		return connect(connection, method, "application/json", queryURI);
	}

	/**
	 * Creates a authenticated connection to the api to the requested endpoint.
	 * If you're not passing a content type here, you'll have to set it through other means
	 */
	private static HttpsURLConnection connect(ConnectionManager connection, String method, @Nullable String contentType, String queryURI) throws IOException {
		auth(connection);
		ConnectionManager.takeRequest();
		HttpsURLConnection https = connection.getSession().authenticate(connection.createConnection(method, queryURI));
		if (contentType != null) https.setRequestProperty("Content-Type", contentType);
		return https;
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
					cm.getCache().cacheProject(p);
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
				return cm.getCache().cacheProject(new OreProject(ConnectionManager.parseJsonObject(connection)));
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
				return cm.getCache().cacheProject(new OreProject(ConnectionManager.parseJsonObject(connection)));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreProject> updateProject(ConnectionManager cm, OreProject project) {
		//get hidden values
		final OreNamespace ns = friendField(project, "shadowNamespace");
		final String requestBody = JsonUtil.buildJson(project, "patchProject").toString();
		return () -> {
			cm.getCache().untrack(project);
			try {
				HttpsURLConnection connection = connect(cm, "PATCH", "/projects/" + ns.toURLEncode());
				connection.setDoInput(true);
				ConnectionManager.postData(connection, requestBody);
				checkResponseCode(connection, OrePermission.Edit_Subject_Settings);
				return cm.getCache().cacheProject(new OreProject(ConnectionManager.parseJsonObject(connection)));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static <T extends OreProjectReference> Supplier<T> updateProjectVisibility(ConnectionManager cm, T project, OreVisibility visibility, String comment) {
		JsonObject requestJson = new JsonObject();
		requestJson.addProperty("visibility", visibility.toString());
		requestJson.addProperty("comment", comment != null ? comment : "");
		final String update = requestJson.toString();

		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "POST", "/projects/" + project.getNamespace().toURLEncode() + "/visibility");
				postData(connection, update);
				checkResponseCode(connection, null);

				//try to update and return the cached reference, if possible
				Optional<OreProject> cacheproject = cm.getCache().project(project);
				cacheproject.ifPresent(p -> friendField(p, "visibility", visibility));
				if (project instanceof OreProject) {
					return (T) cacheproject.orElse((OreProject) project);
				} else {
					return (T) project;
				}
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<Void> deleteProject(ConnectionManager cm, OreProjectReference project) {
		return () -> {
			try {
				cm.getCache().untrack(project);
				HttpsURLConnection connection = connect(cm, "DELETE", "/projects/" + project.getNamespace().toURLEncode());
				checkResponseCode(connection, OrePermission.Hard_Delete_Project);
				return null;
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreMemberList> getProjectMembers(ConnectionManager cm, OreProjectReference project) {
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

	static Supplier<Void> setProjectMembers(ConnectionManager cm, OreProjectReference project, Map<String, OreRole> roles) {
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
					cm.getCache().cacheVersion(project.getPluginId().toLowerCase(), v);
				return resultList;
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreVersion> getVersion(ConnectionManager cm, OreVersionReference version) {
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "GET", "/projects/" + version.getProjectRef().getNamespace().toURLEncode() + "/versions/" + version.getURLSafeName());
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				return cm.getCache().cacheVersion(version.getProjectRef().getPluginId(), new OreVersion(version.getProjectRef(), ConnectionManager.parseJsonObject(connection)));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreVersion> createVersion(ConnectionManager cm, OreDeployVersionInfo info) {
		return () -> {
			final OreProjectReference pref = info.getProjectRef();
			try {
				//content type null - will be set through MultiPartFormData#write
				HttpsURLConnection connection = connect(cm, "POST", null, "/projects/" + pref.getNamespace().toURLEncode() + "/versions");
				connection.setDoInput(true);
				MultiPartFormData mpfd = new MultiPartFormData();
				mpfd.addProperty("plugin-info", JsonUtil.buildJson(info));
				mpfd.addAsset("plugin-file", info.getReleaseAsset());
				mpfd.write(connection);
				checkResponseCode(connection, OrePermission.Create_Version);
				return cm.getCache().cacheVersion(pref.getPluginId(), new OreVersion(pref, parseJsonObject(connection)));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	/**
	 * uncached
	 */
	static Supplier<OreVersion> scanVersion(ConnectionManager cm, OreProjectReference project, Path file) {
		return () -> {
			try {
				if (!"application/java-archive".equals(Files.probeContentType(file))) {
					throw new IllegalArgumentException("The file type is not supported");
				}
			} catch (IOException e) {
				throw new IllegalArgumentException("The supplied file is invalid", e);
			}
			try {
				//content type null - will be set through MultiPartFormData#write
				HttpsURLConnection connection = connect(cm, "PUT", null, "/projects/" + project.getNamespace().toURLEncode() + "/versions/scan");
				connection.setDoInput(true);
				MultiPartFormData mpfd = new MultiPartFormData();
				mpfd.addAsset("plugin-file", file);
				mpfd.write(connection);
				checkResponseCode(connection, OrePermission.Create_Version);
				return new OreVersion(project, parseJsonObject(connection));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreVersion> updateVersion(ConnectionManager cm, OreVersion version) {
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "PATCH", "/projects/" + version.getProjectRef().getNamespace().toURLEncode() + "/versions");
				connection.setDoInput(true);
				postData(connection, JsonUtil.buildJson(version.getTags(), "patchVersion").toString());
				checkResponseCode(connection, OrePermission.Edit_Version);
				return cm.getCache().cacheVersion(version.getProjectRef().getPluginId(), new OreVersion(version.getProjectRef(), parseJsonObject(connection)));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<Void> deleteVersion(ConnectionManager cm, OreVersionReference version) {
		return () -> {
			try {
				cm.getCache().untrack(version);
				HttpsURLConnection connection = connect(cm, "DELETE", "/projects/" + version.getProjectRef().getNamespace().toURLEncode() + "/versions/" + version.getURLSafeName());
				checkResponseCode(connection, OrePermission.Hard_Delete_Version);
				return null;
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static <T extends OreVersionReference> Supplier<T> updateVersionVisibility(ConnectionManager cm, T version, OreVisibility visibility, String comment) {
		JsonObject requestJson = new JsonObject();
		requestJson.addProperty("visibility", visibility.toString());
		requestJson.addProperty("comment", comment != null ? comment : "");
		final String update = requestJson.toString();

		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "POST", "/projects/" + version.getProjectRef().getNamespace().toURLEncode() + "/versions/" + version.getURLSafeName() + "/visibility");
				postData(connection, update);
				checkResponseCode(connection, OrePermission.Edit_Version);

				//try to update and return the cached reference, if possible
				Optional<OreVersion> cacheversion = cm.getCache().version(version);
				cacheversion.ifPresent(v -> friendField(v, "visibility", visibility));
				if (version instanceof OreVersion) {
					return (T) cacheversion.orElse((OreVersion) version);
				} else {
					return (T) version;
				}
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

	/**
	 * The api docs say this endpoint requires is_subject_member, but the permission
	 * is not listed in the NamedPermissions enum. don't know what's going on with that
	 */
	static Supplier<Map<Date, OreVersionStatsDay>> getVersionStats(ConnectionManager cm, OreVersionReference version, Date from, Date to) {
		final SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "GET",
						"/projects/" + version.getProjectRef().getNamespace().toURLEncode() +
								"/versions/" + version.getURLSafeName() +
								"/stats?fromDate=" + urlencoded(dateOnly.format(from) + "&toDate=" + urlencoded(dateOnly.format(to))));
				connection.setDoInput(true);
				checkResponseCode(connection, null);
				JsonObject container = ConnectionManager.parseJsonObject(connection);
				Map<Date, OreVersionStatsDay> map = new HashMap<>();
				for (Map.Entry<String, JsonElement> entry : container.entrySet()) {
					map.put(dateOnly.parse(entry.getKey()), new OreVersionStatsDay(entry.getValue().getAsJsonObject()));
				}
				return map;
			} catch (IOException | ParseException e) {
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

				HttpsURLConnection connection = cm.getSession().authenticate((HttpsURLConnection) requestUrl.openConnection());
				connection.setInstanceFollowRedirects(true);
				connection.setRequestProperty("User-Agent", cm.getUserAgent());
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
	//region user
	static Supplier<OreUserList> userSearch(ConnectionManager cm, OreUserFilter filter) {
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "GET", "/users?" + filter.toString());
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				OreUserList resultList = new OreUserList(ConnectionManager.parseJsonObject(connection), OreUser.class, filter);
				for (OreUser u : resultList.getResult())
					cm.getCache().cacheUser(u);
				return resultList;
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	static Supplier<OreUser> getUser(ConnectionManager cm, String name, boolean self) {
		return () -> {
			final String queryName;
			if (self)
				queryName = "@me";
			else if (name == null || !name.matches("\\w+"))
				throw new NoResultException("The supplied username is invalid");
			else
				queryName = name;
			try {
				HttpsURLConnection connection = connect(cm, "GET", "/users/" + urlencoded(name));
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				return cm.getCache().cacheUser(new OreUser(ConnectionManager.parseJsonObject(connection)));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}
	//endregion

	//region organization
	static Supplier<OreOrganization> getOrganization(ConnectionManager cm, String organizaiton) {
		return () -> {
			try {
				HttpsURLConnection connection = connect(cm, "GET", "/organizations/" + urlencoded(organizaiton));
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				return new OreOrganization(parseJsonObject(connection));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	/**
	 * this is basically a duplicate to getProjectMembers. Might do something about that later
	 */
	static Supplier<OreMemberList> getOrganizationMembers(ConnectionManager cm, String organization) {
		return () -> {
			try {
				//i'll just just bump the limit since this endpoint is not implementing proper pagination
				// don't think there will ever be more that 100 members in a project
				HttpsURLConnection connection = connect(cm, "GET", "/organizations/" + urlencoded(organization) + "/members?limit=100");
				connection.setDoInput(true);
				checkResponseCode(connection, OrePermission.View_Public_Info);
				return new OreMemberList(ConnectionManager.parseJsonArray(connection));
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}

	/**
	 * this is basically a duplicate to setProjectMembers. Might do something about that later
	 */
	static Supplier<Void> setOrganizationMembers(ConnectionManager cm, String organization, Map<String, OreRole> roles) {
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
				HttpsURLConnection connection = connect(cm, "POST", "/organizations/" + urlencoded(organization) + "/members");
				connection.setDoInput(true);
				ConnectionManager.postData(connection, requestBody);
				checkResponseCode(connection, OrePermission.Manage_Subject_Members);
				return null;
			} catch (IOException e) {
				throw new NoResultException(e);
			}
		};
	}
	//endregion

}

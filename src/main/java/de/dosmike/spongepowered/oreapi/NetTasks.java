package de.dosmike.spongepowered.oreapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.dosmike.spongepowered.oreapi.exception.MissingPermissionException;
import de.dosmike.spongepowered.oreapi.exception.NoResultException;
import de.dosmike.spongepowered.oreapi.limiter.RateLimiter;
import de.dosmike.spongepowered.oreapi.netobject.*;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.dosmike.spongepowered.oreapi.ConnectionManager.tryPrintErrorBody;

/**
 * If {@link OreApiV2} is "party in the front" and {@link ConnectionManager} is "business in the back", then this is?
 * This is the poor fella that has to carry the whole team.<br>
 * <li>{@link OreApiV2} Presents a nice interface with only the necessary methods. It utilizes the cache and, if
 * necessary calls into {@link NetTasks} for live data.</li>
 * <li>{@link NetTasks} Is the actual API implementation. Providing suppliers that can be scheduled in the Limiter held
 * by the {@link ConnectionManager}</li>
 * <li>{@link ConnectionManager} holds all the local API data including cache and session. It contains all sorts of
 * utility from building connection objects to destroying sessions</li>
 */
class NetTasks {

    private static void auth(ConnectionManager cm) {
        if (!cm.authenticate())
            throw new IllegalStateException("Could not create API session");
    }
    private static String urlencoded(String s) { try { return URLEncoder.encode(s, "UTF-8"); } catch (Throwable e) { throw new RuntimeException(e); } }

    private static HttpsURLConnection connect(ConnectionManager connection, String queryURI) throws IOException {
        auth(connection);
        ConnectionManager.limiter.takeRequest();
        return connection.session.authenticate(connection.createConnection(queryURI));
    }

    static Supplier<OreProjectList> projectSearch(ConnectionManager cm, OreProjectFilter filter) {
        return ()->{try {
            HttpsURLConnection connection = connect(cm, "/projects?"+filter.toString());
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                tryPrintErrorBody(connection);
                throw connection.getResponseCode()==403?new MissingPermissionException(OrePermission.View_Public_Info):new NoResultException(connection.getResponseMessage());
            }
            OreProjectList resultList = new OreProjectList(ConnectionManager.parseJson(connection), OreProject.class, filter);
            for (OreProject p : resultList.getResult())
                cm.cache.cacheProject(p);
            return resultList;
        } catch (IOException e) {
            throw new NoResultException(e);
        }};
    }

    static Supplier<OreProject> getProject(ConnectionManager cm, OreNamespace namespace) {
        return ()->{try {
            HttpsURLConnection connection = connect(cm, "/projects/"+ namespace.toURLEncode());
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                if (connection.getResponseCode()!=404) tryPrintErrorBody(connection);
                throw connection.getResponseCode()==403?new MissingPermissionException(OrePermission.View_Public_Info):new NoResultException(connection.getResponseMessage());
            }
            return cm.cache.cacheProject(new OreProject(ConnectionManager.parseJson(connection)));
        } catch (IOException e) {
            throw new NoResultException(e);
        }};
    }

    static Supplier<OreProject> findByPluginId(ConnectionManager cm, String pluginId) {
        return ()-> {
            OreProjectFilter filter = new OreProjectFilter();
            filter.setQuery(pluginId);
            OreProjectList result;
            do {
                result = projectSearch(cm,filter).get();
                Optional<OreProject> project = result.getResult().stream().filter(p -> p.getPluginId().equals(pluginId)).findAny();
                if (project.isPresent())
                    return project.get();
                filter = result.getPagination().getQueryNext();
            } while (result.getPagination().hasMorePages());
            throw new NoResultException("All results checked");
        };
    }

    static Supplier<OreVersionList> listVersions(ConnectionManager cm, OreProjectReference project, @Nullable OrePaginationFilter pagination) {
        return ()->{try {
            String totalQuery = "/projects/"+ project.getNamespace().toURLEncode()+"/versions";
            if (pagination != null) {
                totalQuery += "?"+pagination.toString();
            }
            HttpsURLConnection connection = connect(cm,totalQuery);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                tryPrintErrorBody(connection);
                throw connection.getResponseCode()==403?new MissingPermissionException(OrePermission.View_Public_Info):new NoResultException(connection.getResponseMessage());
            }
            OreVersionList resultList = new OreVersionList(ConnectionManager.parseJson(connection), project, OreVersion.class, pagination);
            for (OreVersion v : resultList.getResult())
                cm.cache.cacheVersion(project.getPluginId().toLowerCase(), v);
            return resultList;
        } catch (IOException e) {
            throw new NoResultException(e);
        }};
    }

    static Supplier<OreVersion> getVersion(ConnectionManager cm, OreProjectReference project, String versionName) {
        return ()->{try {
            HttpsURLConnection connection = connect(cm,"/projects/"+ project.getNamespace().toURLEncode() +"/versions/"+ URLEncoder.encode(versionName, "UTF-8"));
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                if (connection.getResponseCode()!=404) tryPrintErrorBody(connection);
                throw connection.getResponseCode()==403?new MissingPermissionException(OrePermission.View_Public_Info):new NoResultException(connection.getResponseMessage());
            }
            return cm.cache.cacheVersion(project.getPluginId(), new OreVersion(project.toReference(), ConnectionManager.parseJson(connection)));
        } catch (IOException e) {
            throw new NoResultException(e);
        }};
    }

    static Supplier<String> getVerionChangelog(ConnectionManager cm, OreVersion version) {
        return ()->{try {
            HttpsURLConnection connection = connect(cm,"/projects/"+ version.getProjectRef().getNamespace().toURLEncode() +"/versions/"+ version.getURLSafeName()+"/changelog");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                if (connection.getResponseCode()!=404) tryPrintErrorBody(connection);
                throw connection.getResponseCode()==403?new MissingPermissionException(OrePermission.View_Public_Info):new NoResultException(connection.getResponseMessage());
            }
            String changelog = ConnectionManager.parseJson(connection).get("changelog").getAsString();
            System.out.println(changelog);
            version.updateChangelog(changelog);
            return changelog;
        } catch (IOException e) {
            throw new NoResultException(e);
        }};
    }

    static Supplier<URL> getDownloadURL(ConnectionManager cm, OreVersion version) {
        return ()->{
            auth(cm);
            try {
                //if the plugin was reviewed, the url is static
                if (version.getReviewState().equals(OreReviewState.Reviewed))
                    return new URL(ConnectionManager.baseUrl+
                            version.getProjectRef().getNamespace().toURLEncode()+"/versions/"+
                            version.getURLSafeName()+"/download");

                // If the plugin was not approved, you are supposed to be prompted
                // that the plugin might be risky to use.
                // Since the api alone can't do that, I'll just query the target URL
                URL requestUrl = new URL(ConnectionManager.baseUrl+
                        version.getProjectRef().getNamespace().toURLEncode()+"/versions/"+
                        version.getURLSafeName()+"/confirm?api=true");

                HttpsURLConnection connection = cm.session.authenticate((HttpsURLConnection) requestUrl.openConnection());
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("User-Agent", cm.application);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                    tryPrintErrorBody(connection);
                    throw connection.getResponseCode()==403?new MissingPermissionException():new NoResultException(connection.getResponseMessage());
                }
                JsonObject response = new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
                String string = response.get("url").getAsString();
                return new URL(string);
            } catch (Exception e) {
                throw new NoResultException(e);
            }
        };
    }

    static Supplier<OrePermissionGrant> getPermissions(ConnectionManager cm, String query) {
        return ()->{try {
            HttpsURLConnection connection = connect(cm,"/permissions?"+query);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                if (connection.getResponseCode()!=404) tryPrintErrorBody(connection);
                throw new NoResultException(connection.getResponseMessage());
            }
            return new OrePermissionGrant(ConnectionManager.parseJson(connection));
        } catch (IOException e) {
            throw new NoResultException(e);
        }};
    }

    static Supplier<Boolean> checkPermissions(ConnectionManager cm, String query, Collection<OrePermission> perms, boolean anyEnough) {
        return ()->{try {
            String fullQuery = "/permissions/"+(anyEnough?"hasAny":"hasAll")+"?"+query;
            if (!query.isEmpty()) fullQuery+="&";
            fullQuery += perms.stream().map(p->"permissions="+urlencoded(p.name().toLowerCase(Locale.ROOT))).collect(Collectors.joining("&"));
            HttpsURLConnection connection = connect(cm,fullQuery);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                if (connection.getResponseCode()!=404) tryPrintErrorBody(connection);
                throw new NoResultException(connection.getResponseMessage());
            }
            return ConnectionManager.parseJson(connection).get("result").getAsBoolean();
        } catch (IOException e) {
            throw new NoResultException(e);
        }};
    }
}

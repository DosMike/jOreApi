package de.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.routes.Organizations;
import de.dosmike.spongepowered.oreapi.routes.Permissions;
import de.dosmike.spongepowered.oreapi.routes.Projects;
import de.dosmike.spongepowered.oreapi.routes.Users;

/**
 * This class is the "party in the front" - enjoy a class with clean API<br>
 * <ul><li>{@link OreApiV2} Presents a nice interface with only the necessary methods. It utilizes the cache and, if
 * necessary calls into NetTasks for live data.</li>
 * <li>NetTasks Is the actual API implementation. Providing suppliers that can be scheduled in the Limiter held
 * by the ConnectionManager</li>
 * <li>ConnectionManager holds all the local API data including cache and session. It contains all sorts of
 * utility from building connection objects to destroying sessions</li></ul>
 */
public class OreApiV2 implements AutoCloseable {

	private ConnectionManager instance;

	OreApiV2(ConnectionManager connectionManager) {
		instance = connectionManager;
	}

	//region NON_API - Utility

	public ConnectionManager getConnectionManager() {
		return instance;
	}

	/**
	 * @return true if the termination of this session was confirmed by api. will be false if already destroyed
	 */
	public boolean destroySession() {
		if (instance == null) throw new IllegalStateException("This API instance was closed");
		return instance.destroySession();
	}

	@Override
	public void close() {
		if (instance == null) throw new IllegalStateException("This API instance was closed");
		destroySession();
		ConnectionManager.notifyClosed(this);
		instance = null;
	}
	//endregion

	/**
	 * This returns a permission route for global permission checking, equal to
	 * Permissions#global(OreApiV2).
	 * If you want to check permissions for a Namespaces object (Projects) please
	 * use projects()#permissions(?) or OreProjectReference#with(OreApiV2, Permissions.class).
	 * Use an analogous method for Organisations.
	 *
	 * @return Permission route for global permissions
	 */
	public Permissions permissions() {
		return Permissions.global(this);
	}

	/**
	 * This returns the projects route. While most endpoints only require a OreProjectReference
	 * or even OreNamespace. Some endpoints deviate though, so no project information can be passed
	 * to the route here.
	 *
	 * @return Projects route
	 */
	public Projects projects() {
		return new Projects(this);
	}

	/**
	 * This returns the users route. User information is read only but can provide information
	 * about roles, permissions, membership in organizations and projects.
	 *
	 * @return Users route
	 */
	public Users users() {
		return new Users(this);
	}

	/**
	 * This returns the organizations route. Organizations can not be created or deleted through
	 * the API, but you are able to fetch and manipulate the organizations member list, given you
	 * have the right permission.
	 *
	 * @return Organizations route
	 */
	public Organizations organizations() {
		return new Organizations(this);
	}

	public static ConnectionManager.Builder builder() {
		return ConnectionManager.builder();
	}

}

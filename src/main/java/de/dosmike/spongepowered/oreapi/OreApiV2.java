package de.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.routes.Organizations;
import de.dosmike.spongepowered.oreapi.routes.Permissions;
import de.dosmike.spongepowered.oreapi.routes.Projects;
import de.dosmike.spongepowered.oreapi.routes.Users;

/**
 * The main interface for the Ore Api. Build your instance, pick some route and go.<br>
 * If your application does not terminate properly you can try to terminate the connection manager at the very end.
 */
public class OreApiV2 implements AutoCloseable {

	private ConnectionManager instance;

	OreApiV2(ConnectionManager connectionManager) {
		instance = connectionManager;
	}

	//region NON_API - Utility

	/**
	 * The connection manager should not be of any particular use for you.<br>
	 * One thing you might want to do, if you're holding a long running instance of the api is to poke the cache every
	 * now and then, but that's optional as well.<br>
	 * Once the application using the api is about to terminate you should call {@link ConnectionManager#terminate()}
	 * but that's really it and can probably ignored as well.
	 *
	 * @return the ConnectionManager hiding https connections and stuff
	 */
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

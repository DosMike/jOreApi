package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.routes.*;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonTags;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;
import java.util.Locale;
import java.util.function.BiFunction;

/**
 * This equals the Membership Project structure but is used internally as a value-less
 * reference to projects, giving minimal unique projects identifiers.
 * This object can be used to fetch all project relevant data from the remote.
 */
public class OreProjectReference implements Serializable {

	@FromJson("plugin_id")
	String pluginId;
	@FromJson("namespace")
	@JsonTags("patchProject")
	OreNamespace namespace;

	protected OreProjectReference() {
	}

	/**
	 * Create the project reference from a JsonObject. This is used for JsonUtil#fillSelf.
	 *
	 * @param object the json scoped into namespace information
	 */
	public OreProjectReference(JsonObject object) {
		JsonUtil.fillSelf(this, object);
	}

	private OreProjectReference(OreProjectReference project) {
		this.pluginId = project.pluginId;
		this.namespace = new OreNamespace(project.namespace.owner, project.namespace.slug);
	}

	/**
	 * @return the project reference from this object
	 */
	public OreProjectReference toReference() {
		return new OreProjectReference(this);
	}

	/**
	 * Note that the project id is phased out as identifier. While a plugin id will still be uniquely held by
	 * a user in the future, the plan is to allow multiple plugins withing one project (jar).
	 *
	 * @return the unique project id
	 */
	public String getPluginId() {
		return pluginId.toLowerCase(Locale.ROOT);
	}

	/**
	 * @return the projects namespace on remote
	 */
	public OreNamespace getNamespace() {
		return namespace;
	}

	/**
	 * Access a different route from this project.
	 * Most interesting uses are #with(api, Permissions.class) and #with(api, Members.class)
	 *
	 * @return a the target route with project information, if applicable
	 */
	public <T extends AbstractRoute> T with(OreApiV2 apiInstance, Class<T> route) {
		if (Projects.class.isAssignableFrom(route)) {
			return (T) apiInstance.projects();
		} else if (Permissions.class.isAssignableFrom(route)) {
			return (T) Permissions.namespace(apiInstance, namespace);
		} else if (Members.class.isAssignableFrom(route)) {
			return (T) apiInstance.projects().members(this);
		} else if (Versions.class.isAssignableFrom(route)) {
			return (T) apiInstance.projects().versions();
		}
		throw new IllegalArgumentException("The supplied Route is not supported by with");
	}

	/**
	 * Access a different route from this project and applies the project to the functor.
	 * Could be used like this #with(api, Projects.class, Projects::fetch)
	 *
	 * @return the result object for the specified route and functor
	 */
	public <T extends AbstractRoute, R> R with(OreApiV2 apiInstance, Class<T> route, BiFunction<T, OreProjectReference, R> function) {
		return function.apply(with(apiInstance, route), this);
	}

	/**
	 * Access a different route from this project.
	 * Most interesting uses are #with(api, Permissions.class) and #with(api, Members.class)
	 *
	 * @return a the target route with project information, if applicable
	 */
	public <R> R with(OreApiV2 apiInstance, BiFunction<Projects, OreProjectReference, R> function) {
		return function.apply(apiInstance.projects(), this);
	}

	//Region builder
	public static class Builder {
		String a = null;
		OreNamespace b = null;

		private Builder() {

		}

		public Builder projectId(String id) {
			if (id == null || id.isEmpty())
				throw new IllegalArgumentException("Owner can't be empty");
			a = id;
			return Builder.this;
		}

		public Builder namespace(OreNamespace namespace) {
			if (namespace == null || namespace.owner.isEmpty() || namespace.slug.isEmpty())
				throw new IllegalArgumentException("Namespace can't be empty");
			b = namespace;
			return Builder.this;
		}

		public OreProjectReference build() {
			if (a == null)
				throw new IllegalArgumentException("Owner has to be set");
			if (b == null)
				throw new IllegalArgumentException("Namespace has to be set");
			OreProjectReference opr = new OreProjectReference();
			opr.pluginId = a;
			opr.namespace = b;
			return opr;
		}
	}

	public static Builder builder() {
		return new Builder();
	}
	//endregion


	@Override
	public String toString() {
		return pluginId + "(" + namespace.toString() + ")";
	}
}

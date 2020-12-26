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
import java.util.function.Function;

/**
 * internal minimal project reference
 */
public class OreProjectReference implements Serializable {

	@FromJson("plugin_id")
	String pluginId;
	@FromJson("namespace")
	@JsonTags("patchProject")
	OreNamespace namespace;

	protected OreProjectReference() {
	}

	protected OreProjectReference(JsonObject object) {
		JsonUtil.fillSelf(this, object);
	}

	private OreProjectReference(OreProjectReference project) {
		this.pluginId = project.pluginId;
		this.namespace = new OreNamespace(project.namespace.owner, project.namespace.slug);
	}

	public OreProjectReference toReference() {
		return new OreProjectReference(this);
	}

	public String getPluginId() {
		return pluginId.toLowerCase(Locale.ROOT);
	}

	public OreNamespace getNamespace() {
		return namespace;
	}

	public <T extends AbstractRoute> T with(OreApiV2 apiInstance, Class<T> route) {
		if (Projects.class.isAssignableFrom(route)) {
			return (T) apiInstance.projects();
		} else if (Permissions.class.isAssignableFrom(route)) {
			return (T) Permissions.namespace(apiInstance, namespace);
		} else if (Members.class.isAssignableFrom(route)) {
			return (T) apiInstance.projects().members(this);
		} else if (Versions.class.isAssignableFrom(route)) {
			return (T) apiInstance.projects().versions(this);
		}
		throw new IllegalArgumentException("The supplied Route is not supported by with");
	}

	public <T extends AbstractRoute, R> R with(OreApiV2 apiInstance, Class<T> route, BiFunction<T, OreProjectReference, R> function) {
		return function.apply(with(apiInstance, route), this);
	}

	public <T extends AbstractRoute, R> R with(OreApiV2 apiInstance, Class<T> route, Function<T, R> function) {
		return function.apply(with(apiInstance, route));
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

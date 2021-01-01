package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.routes.Projects;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonTags;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class OreNamespace implements Serializable {

	@FromJson("owner")
	@JsonTags("patchProject")
	String owner;
	@FromJson("slug")
	String slug;

	/**
	 * Create the namespace from a JsonObject. This is used for JsonUtil#fillSelf.
	 *
	 * @param object the json scoped into namespace information
	 */
	public OreNamespace(JsonObject object) {
		JsonUtil.fillSelf(this, object);
	}

	/**
	 * Constructor for manually creating a namespace.
	 * A factory method would just not have any benefits.
	 * While names of owners seem to be restricted to word characters, this is not enforced here.
	 *
	 * @param projectOwner the project owner. Can be a user or organization name.
	 * @param projectSlug  this is basically a url-safe version of the project name.
	 */
	public OreNamespace(String projectOwner, String projectSlug) {
		owner = projectOwner != null ? projectOwner : "";
		slug = projectSlug != null ? projectSlug : "";
	}

	//region getter

	/**
	 * Get the owner of this project.
	 * The name does not provide information on whether this is a user or organization.
	 *
	 * @return the owner of this project.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * The slug of the project is basically a url-safe version of the project name.
	 * Non-word characters seem to be replaced with dashes.
	 *
	 * @return the project slug
	 */
	public String getSlug() {
		return slug;
	}
	//endregion

	//region setter

	/**
	 * Effectively transfers ownership of the object referenced by this namespace.
	 * If you want to commit changes to this value you'll have to update the owning object on the remote.
	 *
	 * @param owner the owner to transfer this project to
	 * @see Projects#update(OreProject)
	 * @see OreProject#update
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	//endregion

	/**
	 * Builds the namespace representation as used for queries with owner/slug
	 * where both owner and slug are url encoded to prevent malformed requests.
	 *
	 * @return the namespace for use in queries
	 */
	public String toURLEncode() {
		try {
			return URLEncoder.encode(owner, "UTF-8") + "/" + URLEncoder.encode(slug, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a the namespace similar to {@link #toURLEncode()}, but does not
	 * escape owner or slug for readability.
	 *
	 * @return the string representation of this namespace.
	 * @see #toURLEncode()
	 */
	@Override
	public String toString() {
		return owner + "/" + slug;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OreNamespace that = (OreNamespace) o;

		if (!owner.equals(that.owner)) return false;
		return slug.equals(that.slug);
	}

	@Override
	public int hashCode() {
		int result = owner.hashCode();
		result = 31 * result + slug.hashCode();
		return result;
	}
}

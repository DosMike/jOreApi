package de.dosmike.spongepowered.oreapi.netobject;

import de.dosmike.spongepowered.oreapi.routes.Projects;
import de.dosmike.spongepowered.oreapi.utility.FromJson;

import java.io.Serializable;

/**
 * The project template is used to create new projects on ore.
 *
 * @see Projects#create(OreProjectTemplate)
 * @see #builder()
 */
public class OreProjectTemplate implements Serializable {

	@FromJson("name")
	private final String name;
	@FromJson("plugin_id")
	private final String pluginId;
	@FromJson("category")
	private final OreCategory category;
	@FromJson("description")
	private final String description;
	@FromJson("owner_name")
	private final String ownerName;

	private OreProjectTemplate(String a, String b, OreCategory c, String d, String e) {
		if (a == null) throw new NullPointerException("name can't be null");
		if (b == null) throw new NullPointerException("pluginId can't be null");
		if (c == null) throw new NullPointerException("category can't be null");
		if (e == null) throw new NullPointerException("ownerName can't be null");
		name = a;
		pluginId = b;
		category = c;
		description = d;
		ownerName = e;
	}

	//region getter

	/**
	 * @return the name for this project
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the project id
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * @return the category for this project
	 */
	public OreCategory getCategory() {
		return category;
	}

	/**
	 * @return the description. This is most likely the 120 character summary.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the owner name for this project. can be either a user or organization name.
	 */
	public String getOwnerName() {
		return ownerName;
	}
	//endregion

	//Region builder
	public static class Builder { // this is called ApiV2ProjectTemplate withing ore
		String name = null;
		String pluginId = null;
		OreCategory category = null;
		String description = "";
		String ownerName = null;

		private Builder() {
		}

		/**
		 * Set the name for this project. Although you can change the name later
		 * you cannot update the slug after creation (The slug being a url-safe representation
		 * of the project name).
		 *
		 * @param name the name of this project
		 * @return this builder for chaining
		 */
		public Builder setName(String name) {
			if (name == null) throw new NullPointerException("name can't be null");
			Builder.this.name = name;
			return Builder.this;
		}

		/**
		 * The id for this project. Project ids can't be changed but "given up" allowing other devs
		 * to pick the project up again if desired.
		 *
		 * @param pluginId the id of this project
		 * @return this builder for chaining
		 */
		public Builder setPluginId(String pluginId) {
			if (pluginId == null) throw new NullPointerException("pluginId can't be null");
			Builder.this.pluginId = pluginId;
			return Builder.this;
		}

		/**
		 * Set the primary category for this project.
		 *
		 * @param category the category
		 * @return this builder for chaining
		 */
		public Builder setCategory(OreCategory category) {
			if (category == null) throw new NullPointerException("category can't be null");
			Builder.this.category = category;
			return Builder.this;
		}

		/**
		 * Optional project summary. Yes, it's called description in this object.
		 * The summary must not exceed 120 Characters.
		 *
		 * @param description the value for the description
		 * @return this builder for chaining
		 */
		public Builder setDescription(String description) {
			Builder.this.description = description;
			return Builder.this;
		}

		/**
		 * Set the owner for this project. Can either be a a user name or an organization name.
		 * You'll require the corresponding permission to be able to create projects as organization.
		 *
		 * @param ownerName name of the project owner
		 * @return this builder for chaining
		 */
		public Builder setOwner(String ownerName) {
			if (ownerName == null) throw new NullPointerException("ownerName can't be null");
			Builder.this.ownerName = ownerName;
			return Builder.this;
		}

		/**
		 * Build the project template
		 *
		 * @return the built instance
		 */
		public OreProjectTemplate build() {
			return new OreProjectTemplate(name, pluginId, category, description, ownerName);
		}
	}

	/**
	 * Create a new Project Template in order to create a project on Ore
	 *
	 * @return a new builder
	 */
	public static Builder builder() {
		return new Builder();
	}
	//endregion

}

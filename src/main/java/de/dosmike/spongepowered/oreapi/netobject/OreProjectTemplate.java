package de.dosmike.spongepowered.oreapi.netobject;

import de.dosmike.spongepowered.oreapi.utility.FromJson;

public class OreProjectTemplate {

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
	public String getName() {
		return name;
	}

	public String getPluginId() {
		return pluginId;
	}

	public OreCategory getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

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

		public Builder setName(String name) {
			if (name == null) throw new NullPointerException("name can't be null");
			Builder.this.name = name;
			return Builder.this;
		}

		public Builder setPluginId(String pluginId) {
			if (pluginId == null) throw new NullPointerException("pluginId can't be null");
			Builder.this.pluginId = pluginId;
			return Builder.this;
		}

		public Builder setCategory(OreCategory category) {
			if (category == null) throw new NullPointerException("category can't be null");
			Builder.this.category = category;
			return Builder.this;
		}

		/**
		 * Optional project summary. Yes, it's called description in this object.
		 */
		public Builder setDescription(String description) {
			Builder.this.description = description;
			return Builder.this;
		}

		public Builder setOwner(String ownerName) {
			if (ownerName == null) throw new NullPointerException("ownerName can't be null");
			Builder.this.ownerName = ownerName;
			return Builder.this;
		}

		public OreProjectTemplate build() {
			return new OreProjectTemplate(name, pluginId, category, description, ownerName);
		}
	}

	public static Builder builder() {
		return new Builder();
	}
	//endregion

}

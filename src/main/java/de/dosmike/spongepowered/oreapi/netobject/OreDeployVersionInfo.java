package de.dosmike.spongepowered.oreapi.netobject;

import de.dosmike.spongepowered.oreapi.utility.FromJson;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This object is used to upload a new version to Ore.
 * Please keep in mind that a version name can only be used once per project,
 * even if a previous version with the same name was deleted.
 */
public class OreDeployVersionInfo implements Serializable {

    OreProjectReference projectReference;

    Path wrapped;
    @FromJson("create_forum_post")
    boolean createForumPost;
    @FromJson("description")
    String description;
    @FromJson("stability")
    OreStability stability;
    @FromJson(value = "release_type", optional = true)
    OreReleaseType releaseType;

    private OreDeployVersionInfo(Path a, boolean b, String c, OreStability d, OreReleaseType e, OreProjectReference f) {
        wrapped = a;
        createForumPost = b;
        description = c;
        stability = d;
        releaseType = e;
        projectReference = f.toReference();
    }

    //region getter

    /**
     * @return a reference to the project this version should be created in
     */
    public OreProjectReference getProjectRef() {
        return projectReference;
    }

    /**
     * @return a path to the file that will be uploaded as the new version
     */
    public Path getReleaseAsset() {
        return wrapped;
    }

    /**
     * @return true if the upload of this version will create a comment on the projects discussion forum thread
     */
    public boolean isCreateForumPost() {
        return createForumPost;
    }

    /**
     * @return the description for this version. can contain unparsed markdown
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return this versions initial stability
     */
    public OreStability getStability() {
        return stability;
    }

    /**
     * @return this versions release type
     */
    public OreReleaseType getReleaseType() {
        return releaseType;
    }
    //endregion

    //Region builder
    public static class Builder {
        Path wrapped;
        boolean createForumPost;
        String description;
        OreStability stability = OreStability.Stable;
        OreReleaseType releaseType = null;

        private Builder() {
        }

        /**
         * Ore requires you to upload a jar or zip archive with a release.
         * This implementation only allows jar archives.
         *
         * @param asset the Path to a jar archive
         * @return this builder for chaining
         * @throws IllegalArgumentException if the file cannot be read or is not of mime-type application/java-archive
         */
        public Builder asset(Path asset) {
            if (asset == null || !Files.isRegularFile(asset))
                throw new IllegalArgumentException("The provided asset is invalid");
            try {
                if (!"application/java-archive".equals(Files.probeContentType(asset)))
                    throw new IllegalArgumentException("The file type is not supported");
            } catch (IOException e) {
                throw new IllegalArgumentException("The supplied file is invalid", e);
            }
            this.wrapped = asset;
            return Builder.this;
        }

        /**
         * Whether to create a forum post with this update or not.
         * The forum post will be a comment in the plugins discussion thread with the following content:<br>
         * <tt>A new version has been released for PLUGIN, it is available here.<br>
         * -----<br>
         * Changelog with parsed markdown</tt>
         *
         * @param doSo set true if you want a form post to be created
         * @return this builder for chaining
         */
        public Builder createForumPost(boolean doSo) {
            this.createForumPost = doSo;
            return Builder.this;
        }

        /**
         * The version description can be read on the versions ore page and, if set, on the projects
         * discussion thread in the forums.
         * The description does support markdown syntax
         *
         * @param versionDescription description for this version
         * @return this builder for chaining
         */
        public Builder description(String versionDescription) {
            this.description = versionDescription == null ? "" : versionDescription;
            return Builder.this;
        }

        /**
         * Set the stability for this version. This used to be the release channel.
         *
         * @param stability the stability
         * @return this builder for chaining
         */
        public Builder stability(OreStability stability) {
            if (stability == null)
                throw new IllegalArgumentException("Stability cannot be null");
            this.stability = stability;
            return Builder.this;
        }

        /**
         * Set the type of release. You might want this to match the version part that changed in this version
         * if you're using semantic versioning as recommended by sponge:<br>
         * Major changes are breaking changes and increase the fist version digit;
         * Minor changes introduce new features but don't break and increase the second version digit;
         * Patches are usually maintenance and fix bugs, increasing the third version digit;
         * Hotfixes are urgent updates that fix breaking bugs and are expressed in versions as anything but the first three digits.
         * <b>Note: Semantic versions are not enforced, do as you please</b>
         *
         * @param releaseType the type of release
         * @return this builder for chaining
         */
        public Builder releaseType(OreReleaseType releaseType) {
            if (releaseType == null)
                throw new IllegalArgumentException("Release Type cannot be null");
            this.releaseType = releaseType;
            return Builder.this;
        }

        /**
         * Build this version object for the specified project.
         * Please note that your version will be rejected if the project id extracted from the jar archive does
         * not match the project id.<br>
         * You should submit this using <tt>api.projects().versions().create()</tt>.
         *
         * @param project the project to create this version in
         * @return the version info for creation
         */
        public OreDeployVersionInfo build(OreProjectReference project) {
            return new OreDeployVersionInfo(wrapped, createForumPost, description, stability, releaseType, project);
        }
    }

    /**
     * Create the data container for a new version to be added to a project<br>
     * You should submit this using <tt>api.projects().versions().create()</tt>.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }
    //endregion

}

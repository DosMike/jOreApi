package de.dosmike.spongepowered.oreapi.netobject;

import de.dosmike.spongepowered.oreapi.utility.FromJson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OreDeployVersionInfo {

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

    public OreProjectReference getProjectRef() {
        return projectReference;
    }

    public Path getReleaseAsset() {
        return wrapped;
    }

    public boolean isCreateForumPost() {
        return createForumPost;
    }

    public String getDescription() {
        return description;
    }

    public OreStability getStability() {
        return stability;
    }

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

        public Builder createForumPost(boolean doSo) {
            this.createForumPost = doSo;
            return Builder.this;
        }

        public Builder description(String versionDescription) {
            this.description = versionDescription == null ? "" : versionDescription;
            return Builder.this;
        }

        public Builder stability(OreStability stability) {
            if (stability == null)
                throw new IllegalArgumentException("Stability cannot be null");
            this.stability = stability;
            return Builder.this;
        }

        public Builder releaseType(OreReleaseType releaseType) {
            if (releaseType == null)
                throw new IllegalArgumentException("Release Type cannot be null");
            this.releaseType = releaseType;
            return Builder.this;
        }

        public OreDeployVersionInfo build(OreProjectReference project) {
            return new OreDeployVersionInfo(wrapped, createForumPost, description, stability, releaseType, project);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion

}

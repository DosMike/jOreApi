package de.dosmike.spongepowered.oreapi.netobject;

import de.dosmike.spongepowered.oreapi.utility.FromJson;

import java.nio.file.Path;

public class OreDeployVersionInfo {

    Path wrapped;
    @FromJson("create_forum_post")
    boolean createForumPost;
    @FromJson("description")
    String description;
    @FromJson("stability")
    OreStability stability;
    @FromJson(value = "release_type", optional = true)
    OreReleaseType releaseType;

    private OreDeployVersionInfo(Path a, boolean b, String c, OreStability d, OreReleaseType e) {
        wrapped = a;
        createForumPost = b;
        description = c;
        stability = d;
        releaseType = e;
    }

    //Region builder
    public static class Builder {
        Path wrapped;
        boolean createForumPost;
        String description;
        OreStability stability = OreStability.Stable;
        OreReleaseType releaseType = null;

        private Builder() {
        }

        public OreDeployVersionInfo build() {
            return null;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion

}

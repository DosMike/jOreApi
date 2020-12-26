package de.dosmike.spongepowered.oreapi.netobject;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.routes.Versions;
import de.dosmike.spongepowered.oreapi.utility.FromJson;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.function.BiFunction;

public class OreVersionReference implements Serializable {

    /**
     * This allows requests for version without having to specify the project again.
     * Also prevents accidentally specifying in the wrong project.
     */
    protected OreProjectReference project;
    @FromJson("name")
    protected String name;

    OreVersionReference() {
    }

    OreVersionReference(OreVersionReference version) {
        project = version.getProjectRef();
        name = getName();
    }

    private OreVersionReference(OreProjectReference p, String v) {
        project = p.toReference();
        name = v;
    }

    //region getter
    public OreProjectReference getProjectRef() {
        return project;
    }

    public String getName() {
        return name;
    }
    //endregion


    public <T> T with(OreApiV2 api, BiFunction<Versions, OreVersionReference, T> function) {
        return function.apply(api.projects().versions(project), this);
    }

    public String getURLSafeName() {
        try {
            return URLEncoder.encode(name, "UTF-8");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public OreVersionReference toReference() {
        return new OreVersionReference(this);
    }

    //Region builder
    public static class Builder {
        String a;
        OreProjectReference b;

        private Builder() {

        }

        public Builder project(OreProjectReference project) {
            if (project == null)
                throw new IllegalArgumentException("Project cannot be null");
            b = project;
            return Builder.this;
        }

        public Builder versionName(String versionName) {
            if (versionName == null)
                throw new IllegalArgumentException("Version Name cannot be null");
            a = versionName;
            return Builder.this;
        }

        public OreVersionReference build() {
            return new OreVersionReference(b, a);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion

    @Override
    public String toString() {
        return name + "@" + getProjectRef().toString();
    }

}

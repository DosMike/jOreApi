package de.dosmike.spongepowered.oreapi.netobject;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.routes.*;
import de.dosmike.spongepowered.oreapi.utility.FromJson;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OreVersionReference implements Serializable {

    /**
     * This allows requests for version without having to specify the project again.
     * Also prevents accidentally specifying in the wrong project.
     */
    protected OreProjectReference project;
    @FromJson("name")
    protected String name;

    protected OreVersionReference() {
    }

    private OreVersionReference(OreVersionReference version) {
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

    public <T extends AbstractRoute> T with(OreApiV2 apiInstance, Class<T> route) {
        if (Projects.class.isAssignableFrom(route)) {
            return (T) apiInstance.projects();
        } else if (Permissions.class.isAssignableFrom(route)) {
            return (T) Permissions.namespace(apiInstance, getProjectRef().getNamespace());
        } else if (Members.class.isAssignableFrom(route)) {
            return (T) apiInstance.projects().members(getProjectRef());
        } else if (Versions.class.isAssignableFrom(route)) {
            return (T) apiInstance.projects().versions();
        }
        throw new IllegalArgumentException("The supplied Route is not supported by with");
    }

    public <T extends AbstractRoute, R> R with(OreApiV2 api, Class<T> route, BiFunction<T, OreVersionReference, R> function) {
        return function.apply(with(api, route), this);
    }

    public <R> R with(OreApiV2 api, BiFunction<Versions, OreVersionReference, R> function) {
        return function.apply(api.projects().versions(), this);
    }

    public <T extends AbstractRoute, R> R with(OreApiV2 api, Class<T> route, Function<T, R> function) {
        return function.apply(with(api, route));
    }

    public <R> R with(OreApiV2 api, Function<Versions, R> function) {
        return function.apply(api.projects().versions());
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

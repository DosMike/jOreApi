package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
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

    public OreNamespace(JsonObject object) {
        JsonUtil.fillSelf(this, object);
    }

    public OreNamespace(String projectOwner, String projectSlug) {
        owner = projectOwner != null ? projectOwner : "";
        slug = projectSlug != null ? projectSlug : "";
    }

    public String getOwner() {
        return owner;
    }

    public String getSlug() {
        return slug;
    }

    public String toURLEncode() {
        try {
            return URLEncoder.encode(owner, "UTF-8")+"/"+URLEncoder.encode(slug, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return owner+"/"+slug;
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

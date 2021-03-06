package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

/**
 * parses both SimplePlatform and VersionPlatform.
 * Keep in mind, that SimplePlatform will not provide a display platform version or minecraft version.
 * platform version is optional.
 */
public class OrePlatform implements Serializable {

    @FromJson("platform")
    String platform;
    @FromJson(value = "platform_version", optional = true)
    String platformVersion;
    @FromJson(value = "display_platform_version", optional = true)
    String displayPlatformVersion;
    @FromJson(value = "minecraft_version", optional = true)
    String minecraftVersion;

    public OrePlatform(JsonObject object) {
        JsonUtil.fillSelf(this, object);
    }

    public String getName() {
        return platform;
    }
    public String getPlatformVersion() {
        return platformVersion;
    }
    public String getDisplayPlatformVersion() {
        return displayPlatformVersion;
    }
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public String toString() {
        String val = platform + " (";
        if (displayPlatformVersion != null) val+=displayPlatformVersion;
        else if (platformVersion != null) val+=platformVersion;
        else val += "v?";
        if (minecraftVersion != null) val += ", " + minecraftVersion;
        return val + ")";
    }
}

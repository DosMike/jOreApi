package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

public class OreFileInfo implements Serializable {

    @FromJson("name")
    String name;
    @FromJson("size_bytes")
    long byteSize;
    @FromJson("md5_hash")
    String md5;

    public OreFileInfo(JsonObject object) {
        JsonUtil.fillSelf(this, object);
    }

    public String getName() {
        return name;
    }

    public long getByteSize() {
        return byteSize;
    }

    public String getMd5() {
        return md5;
    }
}

package de.dosmike.spongepowered.oreapi.utility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This list of tags can be used to filter what field are serialized into json.
 * When JsonUtils.buildJson is call with a list of tags, every field with @FromJson
 * has to have a @JsonTags will all mention tags in order for it to be serialized.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonTags {
    String[] value();
}

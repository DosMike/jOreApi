package de.dosmike.spongepowered.oreapi.utility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this field as to be constructed from Json with JsonUtil#fillSelf.
 * The type of the field is examined to determine further strategies:
 * String, int and long fields are trivially parsed.
 * Enums require a static fromString(String) method that ignores case.
 * Other objects need to support a (JsonObject) constructor.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FromJson {
    /**
     * The json key to read from
     */
    String value();

    /**
     * if this is marked optional, any absence of the key does not trigger exceptions.
     * parse errors may still occur
     */
    boolean optional() default false;

    /**
     * is barely checked, will try to read the json as input type and assign as output type
     */
    Class<? extends TypeMapper<?,?>> mapper() default TypeMappers.IdentityMapper.class;

}

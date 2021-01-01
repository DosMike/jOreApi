package de.dosmike.spongepowered.oreapi.utility;

import java.lang.annotation.*;

/**
 * This annotation is supposed to mark, that this method is not meant to be visible for a user of the library.
 * This means the way this field or method is currently used is probably a hack.
 * Access to these members is done through reflection.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
//noinspection unused
public @interface ReflectiveUse {

}

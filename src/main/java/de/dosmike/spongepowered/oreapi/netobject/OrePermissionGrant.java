package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This is a resolved set of permissions, to be seen as a set of permissions that has explicitly been confirmed
 * by the remote Ore API. While you can build the set yourself, no actually backend checks are performed through
 * this object and as a result things might break!
 */
public class OrePermissionGrant extends HashSet<OrePermission> {

    public OrePermissionGrant() {
        super();
    }

    /**
     * Construct a permission grant from an arbitrary collection of permissions.
     * All passed permissions will be assumed to be granted.
     *
     * @param c the collection of permissions granted
     */
    public OrePermissionGrant(@NotNull Collection<? extends OrePermission> c) {
        super(c);
    }

    /**
     * Create the premission grant from a JsonObject. This is used for JsonUtil#fillSelf.
     *
     * @param object the json scoped into namespace information
     */
    public OrePermissionGrant(JsonObject object) {
        List<OrePermission> perms = new LinkedList<>();
        object.get("permissions").getAsJsonArray().forEach(e -> perms.add(OrePermission.fromString(e.getAsString())));
        addAll(perms);
    }

    /**
     * Run some code if and only if all permissions passed to this method are granted with this permission grant
     *
     * @param r        the code to execute
     * @param required the permission required for execution
     */
    public void ifContainsAll(Runnable r, OrePermission... required) {
        if (containsAll(Arrays.asList(required))) r.run();
    }

    /**
     * Run some code if and only if all permissions passed to this method are granted with this permission grant
     *
     * @param s        the code to execute
     * @param required the permission required for execution
     * @param <T> type of your codes return value
     * @return the return value, if the code executed
     */
    public <T> Optional<T> ifContainsAll(Supplier<T> s, OrePermission... required) {
        if (containsAll(Arrays.asList(required))) return Optional.of(s.get());
        return Optional.empty();
    }

    /**
     * Run some code if this permission grants at least one permission in the passed set of permissions
     *
     * @param r        the code to run
     * @param required the set of permissions to find in this grant
     */
    public void ifContainsAny(Runnable r, OrePermission... required) {
        for (OrePermission p : required)
            if (contains(p))
                r.run();
    }

    /**
     * Run some code if this permission grants at least one permission in the passed set of permissions
     *
     * @param s        the code to run
     * @param required the set of permissions to find in this grant
     * @param <T> type of your codes return value
     * @return the return value, if the code executed
     */
    public <T> Optional<T> ifContainsAny(Supplier<T> s, OrePermission... required) {
        for (OrePermission p : required)
            if (contains(p))
                return Optional.of(s.get());
        return Optional.empty();
    }

    /**
     * Will prevent code form continuing execution if one or more permissions
     * are missing from this set.
     * The thrown exceptions message will contain a user readable list of all missing permissions.
     *
     * @param required the permissions required in order to continue execution.
     * @throws IllegalStateException if one or more permission from required are not in this grant.
     */
    public void assertAllPermissions(OrePermission... required) {
        Set<OrePermission> missing = new HashSet<>(Arrays.asList(required));
        missing.removeAll(this);
        if (!missing.isEmpty())
            throw new IllegalStateException("Missing Permission: " + missing.stream().map(OrePermission::name).collect(Collectors.joining(", ")));
    }

    /**
     * Will prevent code form continuing execution if none of the requested
     * permissions are contained in this set.
     * The thrown exceptions message will contain a user readable list of possible missing permissions.
     *
     * @param required the permissions required in order to continue execution.
     * @throws IllegalStateException if all permission from required are missing in this grant.
     */
    public void assertAnyPermissions(OrePermission... required) {
        for (OrePermission p : required)
            if (contains(p))
                return;
        throw new IllegalStateException("Missing Permission: " + Arrays.stream(required).map(OrePermission::name).collect(Collectors.joining(", ")));
    }

}

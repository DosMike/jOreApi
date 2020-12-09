package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OrePermissionGrant extends HashSet<OrePermission> {

	public OrePermissionGrant() {
		super();
	}

	public OrePermissionGrant(@NotNull Collection<? extends OrePermission> c) {
		super(c);
	}

	public OrePermissionGrant(JsonObject object) {
		List<OrePermission> perms = new LinkedList<>();
		object.get("permissions").getAsJsonArray().forEach(e -> perms.add(OrePermission.fromString(e.getAsString())));
		addAll(perms);
	}

	public void ifContainsAll(Runnable r, OrePermission... required) {
		if (containsAll(Arrays.asList(required))) r.run();
	}

	public <T> Optional<T> ifContainsAll(Supplier<T> s, OrePermission... required) {
		if (containsAll(Arrays.asList(required))) return Optional.of(s.get());
		return Optional.empty();
	}

	public void ifContainsAny(Runnable r, OrePermission... required) {
		for (OrePermission p : required)
			if (contains(p))
				r.run();
	}

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

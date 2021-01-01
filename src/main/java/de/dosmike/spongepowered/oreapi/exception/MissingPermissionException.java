package de.dosmike.spongepowered.oreapi.exception;

import de.dosmike.spongepowered.oreapi.netobject.OrePermission;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Emitted when a request returned RC 403 - Forbidden.
 * This means that your current session lacks some permission.
 */
public class MissingPermissionException extends RuntimeException {
	public MissingPermissionException() {
	}

	public MissingPermissionException(String message) {
		super(message);
	}

	public MissingPermissionException(OrePermission... perms) {
		super("Missing Permission for endpoint: " + Arrays.stream(perms).map(Enum::name).collect(Collectors.joining(", ")));
	}

	public MissingPermissionException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingPermissionException(Throwable cause) {
		super(cause);
	}
}

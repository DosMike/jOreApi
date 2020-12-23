package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.OreNamespace;
import de.dosmike.spongepowered.oreapi.netobject.OrePermission;
import de.dosmike.spongepowered.oreapi.netobject.OrePermissionGrant;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public abstract class Permissions extends AbstractRoute {

    private Permissions(OreApiV2 api, String query) {
        super(api);
        this.query = query;
    }

    private String query;

    public CompletableFuture<OrePermissionGrant> get() {
        return enqueue(NetTasks.getPermissions(cm(), query));
    }

    public CompletableFuture<Boolean> hasAll(Collection<OrePermission> perms) {
        return enqueue(NetTasks.checkPermissions(cm(), query, perms, false));
    }

    public CompletableFuture<Boolean> hasAny(Collection<OrePermission> perms) {
        return enqueue(NetTasks.checkPermissions(cm(), query, perms, true));
    }

    public static class Global extends Permissions {
        private Global(OreApiV2 api) {
            super(api, "");
        }
    }

    public static class Namespaced extends Permissions {
        private Namespaced(OreApiV2 api, OreNamespace namespace) {
            super(api, "projectOwner=" + urlencoded(namespace.getOwner()) + "&projectSlug=" + urlencoded(namespace.getSlug()));
        }
    }

    public static class Organisation extends Permissions {
        private Organisation(OreApiV2 api, String organisation) {
            super(api, "organizationName=" + urlencoded(organisation));
        }
    }

    public static Global global(OreApiV2 api) {
        return new Global(api);
    }

    public static Namespaced namespace(OreApiV2 api, OreNamespace namespace) {
        return new Namespaced(api, namespace);
    }

    public static Organisation organisation(OreApiV2 api, String organisation) {
        return new Organisation(api, organisation);
    }

}

package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.ConnectionManager;
import de.dosmike.spongepowered.oreapi.ObjectCache;
import de.dosmike.spongepowered.oreapi.OreApiV2;

import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility for the Route classes to get the api instance, cache and connection manager
 */
public abstract class AbstractRoute {

    protected OreApiV2 api;

    public AbstractRoute(OreApiV2 api) {
        this.api = api;
    }

    protected static String urlencoded(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> CompletableFuture<T> enqueue(Supplier<T> task) {
        return ConnectionManager.enqueue(task);
    }

    protected ConnectionManager cm() {
        ConnectionManager cm = api.getConnectionManager();
        if (cm == null)
            throw new IllegalStateException("The api instance was already closed");
        return cm;
    }

    protected ObjectCache cache() {
        return cm().getCache();
    }

}

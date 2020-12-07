package de.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.netobject.*;
import de.dosmike.spongepowered.oreapi.utility.CachingCollection;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ObjectCache {

    private CachingCollection<OreProject> oreProjectCache = new CachingCollection<>(5, TimeUnit.MINUTES);
    public OreProject cacheProject(OreProject project) {
        oreProjectCache.add(project);
        return project;
    }
    public Optional<OreProject> project(OreProjectReference reference) {
        if (reference == null)
            return Optional.empty();
        else if (reference.getNamespace() == null)
            return oreProjectCache.stream().filter(e->e.getPluginId().equalsIgnoreCase(reference.getPluginId())).findFirst();
        else
            return oreProjectCache.stream().filter(e->e.getNamespace().equals(reference.getNamespace())).findFirst();
    }
    public Optional<OreProject> project(String pluginId) {
        return oreProjectCache.stream().filter(e->e.getPluginId().equalsIgnoreCase(pluginId)).findFirst();
    }
    public Optional<OreProject> project(OreNamespace namespace) {
        return oreProjectCache.stream().filter(e->e.getNamespace().equals(namespace)).findFirst();
    }
    private Map<String, CachingCollection<OreVersion>> oreVersionCache = new HashMap<>();
    public OreVersion cacheVersion(String pluginId, OreVersion version) {
        CachingCollection<OreVersion> cache = oreVersionCache.get(pluginId.toLowerCase());
        if (cache == null) {
            cache = new CachingCollection<>(5, TimeUnit.MINUTES);
            oreVersionCache.put(pluginId.toLowerCase(), cache);
        }
        cache.add(version);
        return version;
    }
    public Optional<OreVersion> version(String pluginId, String versionName) {
        CachingCollection<OreVersion> collection = oreVersionCache.get(pluginId.toLowerCase());
        if (collection == null) return Optional.empty();
        else return collection.stream()
                .filter(v -> v.getName().equalsIgnoreCase(versionName))
                .findFirst();
    }

//    public void exportState(OutputStream outputStream) throws IOException {
//        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
//        oos.writeObject(oreProjectCache);
//        oos.writeObject(oreVersionCache);
//        oos.writeObject(ConnectionManager.get().session);
//        oos.flush();
//    }
//    public void importState(InputStream inputStream) throws IOException, ClassNotFoundException {
//        ObjectInputStream ois = new ObjectInputStream(inputStream);
//        oreProjectCache = (CachingCollection<OreProject>) ois.readObject();
//        oreVersionCache = (Map<String, CachingCollection<OreVersion>>) ois.readObject();
//        ConnectionManager.get().session = (OreSession) ois.readObject();
//    }

    public void poke() {
        // CachingCollection.size() performs timeout check and returns the remaining size

        //noinspection ResultOfMethodCallIgnored
        oreProjectCache.size();
        Set<String> vkeys = new HashSet<>();
        oreVersionCache.forEach((k,v)->{if (vkeys.size()==0) vkeys.add(k);});
        for (String k : vkeys) oreVersionCache.remove(k);

    }
}

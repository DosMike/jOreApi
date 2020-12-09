package de.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.netobject.OreNamespace;
import de.dosmike.spongepowered.oreapi.netobject.OreProject;
import de.dosmike.spongepowered.oreapi.netobject.OreProjectReference;
import de.dosmike.spongepowered.oreapi.netobject.OreVersion;
import de.dosmike.spongepowered.oreapi.utility.CachingCollection;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static de.dosmike.spongepowered.oreapi.utility.ReflectionHelper.friendField;

public class ObjectCache {

	private final CachingCollection<OreProject> oreProjectCache = new CachingCollection<>(5, TimeUnit.MINUTES);

	public OreProject cacheProject(OreProject project) {
		oreProjectCache.add(project);
		return project;
	}

	public Optional<OreProject> project(OreProjectReference reference) {
		if (reference == null)
			return Optional.empty();
		else if (reference.getNamespace() == null)
			return oreProjectCache.stream().filter(e -> e.getPluginId().equalsIgnoreCase(reference.getPluginId())).findFirst();
		else
			return oreProjectCache.stream().filter(e -> e.getNamespace().equals(reference.getNamespace())).findFirst();
	}

	public Optional<OreProject> project(String pluginId) {
		return oreProjectCache.stream().filter(e -> e.getPluginId().equalsIgnoreCase(pluginId)).findFirst();
	}

	public Optional<OreProject> project(OreNamespace namespace) {
		return oreProjectCache.stream().filter(e -> e.getNamespace().equals(namespace)).findFirst();
	}

	private final Map<String, CachingCollection<OreVersion>> oreVersionCache = new HashMap<>();

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
		oreVersionCache.forEach((k, v) -> {
			if (vkeys.size() == 0) vkeys.add(k);
		});
		for (String k : vkeys) oreVersionCache.remove(k);

	}

	public void untrack(OreNamespace ns) {
		//from versions
		Set<String> invalidatedKeys = new HashSet<>();
		for (Map.Entry<String, CachingCollection<OreVersion>> entry : oreVersionCache.entrySet()) {
			if (entry.getValue().stream().anyMatch(v -> v.getProjectRef().getNamespace().equals(ns)))
				invalidatedKeys.add(entry.getKey());
		}
		for (String k : invalidatedKeys) oreVersionCache.remove(k);
		//from project
		oreProjectCache.removeIf(p -> friendField(p, "shadowNamespace").equals(ns));
	}

	public void untrack(String pluginId) {
		//from versions
		oreVersionCache.remove(pluginId.toLowerCase(Locale.ROOT));
		//from plugins
		oreProjectCache.removeIf(p -> p.getPluginId().equalsIgnoreCase(pluginId));
	}

	public void untrack(OreProjectReference project) {
		untrack(project.getPluginId());
	}

	public void untrack(OreVersion version) {
		CachingCollection<OreVersion> vcache = oreVersionCache.get(version.getProjectRef().getPluginId().toLowerCase(Locale.ROOT));
		if (vcache == null) return;
		vcache.removeIf(v -> v.getName().equals(version.getName()));
	}
}

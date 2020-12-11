package test.dosmike.spongepowered.oreapi.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.dosmike.spongepowered.oreapi.netobject.OreProject;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonUtilities {

	private static OreProject projectInstance;

	@BeforeAll
	public void prepareProject() {
		projectInstance = new OreProject(new JsonParser().parse("{\n" +
				"  \"created_at\": \"2020-11-21T09:51:14.631Z\",\n" +
				"  \"plugin_id\": \"vshop\",\n" +
				"  \"name\": \"VillagerShops\",\n" +
				"  \"namespace\": {\n" +
				"    \"owner\": \"DosMike\",\n" +
				"    \"slug\": \"villagershops\"\n" +
				"  },\n" +
				"  \"promoted_versions\": [\n" +
				"    {\n" +
				"      \"version\": \"2.8\",\n" +
				"      \"platforms\": [\n" +
				"        {\n" +
				"          \"platform\": \"spongeapi\",\n" +
				"          \"platform_version\": \"[7.0.0,)\",\n" +
				"          \"display_platform_version\": \"7.0\",\n" +
				"          \"minecraft_version\": null\n" +
				"        }\n" +
				"      ]\n" +
				"    }\n" +
				"  ],\n" +
				"  \"stats\": {\n" +
				"    \"views\": 0,\n" +
				"    \"downloads\": 0,\n" +
				"    \"recent_views\": 0,\n" +
				"    \"recent_downloads\": 0,\n" +
				"    \"stars\": 0,\n" +
				"    \"watchers\": 0\n" +
				"  },\n" +
				"  \"category\": \"economy\",\n" +
				"  \"summary\": \"Set up Mobs of any type as admin shop. Shops will use a Inventory-Menu to buy/sell items\",\n" +
				"  \"last_updated\": \"2020-11-21T09:51:14.631Z\",\n" +
				"  \"visibility\": \"public\",\n" +
				"  \"user_actions\": {\n" +
				"    \"starred\": false,\n" +
				"    \"watching\": false\n" +
				"  },\n" +
				"  \"settings\": {\n" +
				"    \"keywords\": [],\n" +
				"    \"homepage\": null,\n" +
				"    \"issues\": null,\n" +
				"    \"sources\": null,\n" +
				"    \"support\": null,\n" +
				"    \"license\": {\n" +
				"      \"name\": null,\n" +
				"      \"url\": null\n" +
				"    },\n" +
				"    \"forum_sync\": true\n" +
				"  },\n" +
				"  \"icon_url\": \"https://staging-auth.spongeproject.net/avatar/DosMike?size=120x120\",\n" +
				"  \"external\": {\n" +
				"    \"discourse\": {\n" +
				"      \"topic_id\": null,\n" +
				"      \"post_id\": null\n" +
				"    }\n" +
				"  }\n" +
				"}").getAsJsonObject());
	}

	@Test
	@Order(1)
	public void serializeObjects() {
		JsonObject wholeProject = JsonUtil.buildJson(projectInstance);
		Set<String> expectedKeysTLWhole = new HashSet<>(Arrays.asList(
				"created_at", "plugin_id",
				"name", "namespace",
				"promoted_versions", "stats",
				"category", "summary",
				"last_updated", "visibility",
				"settings", "icon_url"
		));
		Set<String> presentKeysTLWhole = wholeProject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());

		System.out.println("Present Keys:  " + presentKeysTLWhole.stream().sorted().collect(Collectors.joining(", ")));
		System.out.println("Expected Keys: " + expectedKeysTLWhole.stream().sorted().collect(Collectors.joining(", ")));

		Set<String> keysMissing = new HashSet<>(expectedKeysTLWhole);
		keysMissing.removeAll(presentKeysTLWhole);
		if (!keysMissing.isEmpty())
			System.out.println("Missing Keys (" + keysMissing.size() + "): " + String.join(", ", keysMissing));
		assertTrue(keysMissing.isEmpty(), "There are keys missing");

		Set<String> keysRemaining = new HashSet<>(presentKeysTLWhole);
		keysRemaining.removeAll(expectedKeysTLWhole);
		if (!keysRemaining.isEmpty())
			System.out.println("Remaining Keys (" + keysRemaining.size() + "): " + String.join(", ", keysRemaining));
		assertTrue(keysRemaining.isEmpty(), "Too many fields were serialized");

		assertEquals(wholeProject.getAsJsonObject("namespace").getAsJsonPrimitive("owner").getAsString(), "DosMike");
		assertEquals(wholeProject.getAsJsonObject("namespace").getAsJsonPrimitive("owner").getAsString(), projectInstance.getNamespace().getOwner());
	}

	@Test
	@Order(2)
	public void serializeObjectsFiltered() {
		//JsonObject wholeProject = JsonUtil.buildJson(projectInstance);
		JsonObject patchProject = JsonUtil.buildJson(projectInstance, "patchProject");
//		assertNotEquals(patchProject, wholeProject);

		Set<String> expectedKeysTLPatch = new HashSet<>(Arrays.asList(
				"name", "namespace",
				"category", "summary",
				"settings"
		));
		Set<String> presentKeysTLPatch = patchProject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());

		System.out.println("Present Keys:  " + presentKeysTLPatch.stream().sorted().collect(Collectors.joining(", ")));
		System.out.println("Expected Keys: " + expectedKeysTLPatch.stream().sorted().collect(Collectors.joining(", ")));

		Set<String> keysMissing = new HashSet<>(expectedKeysTLPatch);
		keysMissing.removeAll(presentKeysTLPatch);
		if (!keysMissing.isEmpty())
			System.out.println("Missing Keys (" + keysMissing.size() + "): " + String.join(", ", keysMissing));
		assertTrue(keysMissing.isEmpty(), "There are keys missing");

		Set<String> keysRemaining = new HashSet<>(presentKeysTLPatch);
		keysRemaining.removeAll(expectedKeysTLPatch);
		if (!keysRemaining.isEmpty())
			System.out.println("Remaining Keys (" + keysRemaining.size() + "): " + String.join(", ", keysRemaining));
		assertTrue(keysRemaining.isEmpty(), "Too many fields were serialized");
	}

}

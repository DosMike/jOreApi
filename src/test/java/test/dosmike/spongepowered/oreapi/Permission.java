package test.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.ConnectionManager;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.exception.MissingPermissionException;
import de.dosmike.spongepowered.oreapi.netobject.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Permission {

	private OreApiV2 api;

	@BeforeAll
	public void prepareConnectionManager() {
		System.setProperty("verboseNetTrafficLogging", "true");
		api = OreApiV2.builder()
				.setApplication("jOreApi/1.2 (by DosMike; Ore API V2) / JUnit Test")
				.build();
	}

	@Test
	@Order(1)
	public void viewPublicInfo() {
		//this permission should be granted on all public session endpoins
		OrePermissionGrant granted = api.getPermissions().join();
		System.out.println("Has Perms: " + granted.stream().map(Enum::name).collect(Collectors.joining(", ")));
		granted.assertAllPermissions(OrePermission.View_Public_Info);
		api.getPermissions(new OreNamespace("DosMike", "villagershops")).join().assertAllPermissions(OrePermission.View_Public_Info);
	}

	@Test
	@Order(2)
	public void askHasPermission() {
		assertTrue(api.hasAllPermissions(new OreNamespace("DosMike", "villagershops"), Collections.singleton(OrePermission.View_Public_Info)).join());
		assertFalse(api.hasAllPermissions(new OreNamespace("DosMike", "villagershops"), Collections.singleton(OrePermission.Delete_Project)).join());
	}

	@Test
	@Order(3)
	public void violatePermission() {
		assertFalse(api.hasAllPermissions(Collections.singleton(OrePermission.Create_Project)).join());
		assertThrows(MissingPermissionException.class, () -> api.createProject(OreProjectTemplate.builder()
				.setName("Test Plugin")
				.setCategory(OreCategory.Misc)
				.setPluginId("testplugin197h5z86")
				.setOwner("DosMike")
				.setDescription("This plugin should not exist")
				.build()).join());
	}

	@AfterAll
	public void reset() {
		System.out.println("Reset...");
		ConnectionManager.terminate();
		//this hack allows restarting
		try {
			Field f = ConnectionManager.class.getDeclaredField("limiter");
			f.setAccessible(true);
			f.set(null, null);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

}

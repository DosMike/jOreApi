package test.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.ConnectionManager;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.OreNamespace;
import de.dosmike.spongepowered.oreapi.netobject.OreVersionList;
import de.dosmike.spongepowered.oreapi.routes.Versions;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PublicVersions {

	private OreApiV2 api;
	private OreVersionList versions;
	private final OreNamespace ns = new OreNamespace("DosMike", "villagershops");

	@BeforeAll
	public void prepareConnectionManager() {
		System.setProperty("verboseNetTrafficLogging", "true");
		api = OreApiV2.builder()
				.setApplication("jOreApi/1.2 (by DosMike; Ore API V2) / JUnit Test")
				.build();
	}

	@Test
	@Order(1)
	public void listVersions() {
		versions = api.projects().get(ns).thenCompose(p -> api.projects().versions(p).list(null)).join();
		assertFalse(versions.getResult().isEmpty());
	}

	@Test
	@Order(2)
	public void getChangelog() {
		String changeLog = versions.getResult().get(0).with(api, Versions::changelog).join();
		assertFalse(changeLog.isEmpty());
		assertTrue(versions.getResult().get(0).getChangelog().isPresent());
	}

	@Test
	@Order(3)
	public void getVersionDownload() {
		System.out.println(versions.getResult().get(0).with(api, Versions::getDownloadURL).join());
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

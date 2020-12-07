package test.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.ConnectionManager;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.OreNamespace;
import de.dosmike.spongepowered.oreapi.netobject.OreProjectFilter;
import de.dosmike.spongepowered.oreapi.netobject.OreProjectList;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PublicProjects {

    private OreApiV2 api;

    @BeforeAll
    public void prepareConnectionManager() {
        api = OreApiV2.builder()
                .setApplication("jOreApi/1.2 (by DosMike; Ore API V2) / JUnit Test")
                .build();
    }

    @Test
    @Order(1)
    public void search() {
        System.out.println("Using search term 'cubeengine'");
        OreProjectFilter search = new OreProjectFilter("cubeengine");
        OreProjectList result = api.projectSearch(search).join();
        result.getResult().forEach(p-> System.out.println(p.getName()+": "+p.getPluginId()+" @ "+p.getNamespace().toString()));
        assertTrue(result.getResult().stream().anyMatch(p->p.getPluginId().equals("cubeengine-chat")), "Search does not contain most relevant");
    }

    @Test
    @Order(2)
    public void findPluginById() {
        System.out.println("Finding plugin 'vshop'");
        System.out.println(api.findProjectByPluginId("vshop").join().getName());
    }

    @Test
    @Order(3)
    public void getByNamespace() {
        OreNamespace ns = new OreNamespace("DosMike", "villagershops");
        System.out.println(api.getProject(ns).join().getName());
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

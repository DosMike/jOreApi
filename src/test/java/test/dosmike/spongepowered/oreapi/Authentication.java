package test.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.ConnectionManager;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Authentication {

    private OreApiV2 api;

    private ConnectionManager con() {
        try {
            Field f = OreApiV2.class.getDeclaredField("instance");
            f.setAccessible(true);
            return (ConnectionManager) f.get(api);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @BeforeAll
    public void prepareConnectionManager() {
        System.setProperty("verboseNetTrafficLogging", "true");
        api = OreApiV2.builder()
                .setApplication("jOreApi/1.2 (by DosMike; Ore API V2) / JUnit Test")
                .build();
    }

    @Test
    @Order(1)
    public void authenticate() {
        ConnectionManager con = con();
        assertTrue(con.authenticate(), "Getting public session failed");
        String session = con.getSession().get();
        assertTrue(con.authenticate(), "Validating public session failed");
        assertEquals(session, con.getSession().get(), "Existing session was not reused");
    }

    @Test
    @Order(2)
    public void destroySession() {
        ConnectionManager con = con();
        String session = con.getSession().get();
        assertTrue(api.destroySession());
        assertTrue(con.getSession().isExpired());

        assertTrue(con.authenticate());
        assertTrue(con.getSession().isAlive());
        assertNotEquals(session, con.getSession().get());
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

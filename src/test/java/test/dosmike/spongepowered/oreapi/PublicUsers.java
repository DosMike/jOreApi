package test.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.OreMembership;
import de.dosmike.spongepowered.oreapi.netobject.OreMembershipList;
import de.dosmike.spongepowered.oreapi.netobject.OreNamespace;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PublicUsers {

    private OreApiV2 api;
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
    public void listMemberships() {
        OreMembershipList ml = api.users().memberships("DosMike").join();
        for (OreMembership m : ml.getResult())
            System.out.println(m.getScope());
    }

}

package com.glisterbyte.singingmonsters;

import com.glisterbyte.singingmonsters.main.catalog.MonsterCatalog;
import com.glisterbyte.singingmonsters.main.catalog.MonsterSpecies;
import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.main.common.HasTimer;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.main.island.IslandType;
import com.glisterbyte.singingmonsters.main.monster.Monster;
import com.glisterbyte.singingmonsters.main.structure.breeding.BreedingStructure;
import com.glisterbyte.singingmonsters.main.structure.nursery.Nursery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// 16 July 2026, 4:24 AM CST - all tests passing

@ExtendWith(FailOnceExtension.class)
public class BreedingStructureTests extends ClientBoundTests {

    private static BreedingStructure breeder;

    @BeforeAll
    public static void init() throws ClientException, InterruptedException {
        initClient();
        Island island = client.getIsland(IslandType.PLANT_ISLAND);
        breeder = island.getBreedingStructures().stream().filter(HasTimer::isIdle).findFirst().orElse(null);
    }

    @Test
    @DisplayName("Breed a noggin")
    public void breedNoggin() throws ClientException, InterruptedException {

        assumeTrue(breeder != null, "No idle breeder on Plant Island");

        Island plantIsland = client.getIsland(IslandType.PLANT_ISLAND);
        MonsterCatalog catalog = client.getMonsterCatalog();
        Monster noggin = plantIsland.getMonsterOfSpecies(catalog.noggin());
        Monster mate = plantIsland.getMonsters().stream()
                .filter(m -> m.getSpecies().getElements().size() >= 3)
                .filter(m -> !catalog.bowgart().matches(m.getSpecies()))
                .max(Comparator.comparing(m -> m.getSpecies().getElements().size()))
                .orElse(null);

        assumeTrue(noggin != null, "No noggin on Plant Island");
        assumeTrue(mate != null, "No mate found for noggin on Plant Island");

        breeder.breed(noggin, mate);

        assertTrue(breeder.getUserBreedingId().isPresent());
        assertTrue(breeder.isPending());

        breeder.waitUntilDone();

        assertTrue(breeder.getUserBreedingId().isPresent());
        assertTrue(breeder.isDone());

        MonsterSpecies species = breeder.getResultingSpecies();
        assertNotNull(species);

        Nursery nursery = breeder.collectEgg();
        assertNotNull(nursery);
        assertEquals(species, nursery.getEggSpecies());
        assertTrue(nursery.isPending());

        // cleanup
        nursery.waitUntilDone();
        nursery.sellEgg();


    }

}

package com.glisterbyte.singingmonsters;

import com.glisterbyte.singingmonsters.main.catalog.MonsterCatalog;
import com.glisterbyte.singingmonsters.main.catalog.MonsterSpecies;
import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.main.common.CurrencyType;
import com.glisterbyte.singingmonsters.main.common.MultiCurrencyValue;
import com.glisterbyte.singingmonsters.main.common.Position;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.main.island.IslandType;
import com.glisterbyte.singingmonsters.main.monster.Monster;
import com.glisterbyte.singingmonsters.main.monster.MonsterPlacement;
import com.glisterbyte.singingmonsters.main.monster.eggbox.EggBoxMonster;
import com.glisterbyte.singingmonsters.main.monster.eggbox.ReadableEggBoxMonster;
import com.glisterbyte.singingmonsters.main.monster.eggbox.ZappedEggData;
import com.glisterbyte.singingmonsters.main.structure.breeding.BreedingStructure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

// 16 July 2026, 4:22 AM CST - all tests passing

@ExtendWith(FailOnceExtension.class)
public class WublinMonsterTests extends ClientBoundTests {

    private static final Logger logger = LoggerFactory.getLogger(WublinMonsterTests.class);

    private static Island island;

    private static MonsterCatalog catalog;

    @BeforeAll
    public static void init() throws ClientException, InterruptedException {

        initClient();
        island = client.getIsland(IslandType.WUBLIN_ISLAND);
        catalog = client.getMonsterCatalog();

    }

    @Test
    @DisplayName("Buy and sell wublin statue")
    public void buyAndSellWublinStatue() throws ClientException, InterruptedException {

        MonsterSpecies zynthSpecies = catalog.zynth().getSpecies(island);
        Position position = getNewPosition(island, zynthSpecies.getSize(), null);
        EggBoxMonster monster = (EggBoxMonster)island.buyMonster(
                zynthSpecies, new MonsterPlacement(position, true)
        );

        assertNotNull(monster);
        assertEquals(position, monster.getPosition());
        assertTrue(monster.isFlipped());
        assertFalse(monster.isActivated());

        client.refresh();

        assertEquals(position, monster.getPosition());
        assertTrue(monster.isFlipped());
        assertFalse(monster.isActivated());

        monster.sell();

        assertFalse(island.getMonsters().contains(monster));
        long userMonsterId = monster.getUserMonsterId();

        client.refresh();

        assertFalse(island.getMonsters().contains(monster));
        assertTrue(island.getMonsters().stream().noneMatch(m -> m.getUserMonsterId() == userMonsterId));

    }

    @Test
    @DisplayName("Get required eggs")
    public void getRequiredEggs() throws ClientException, InterruptedException {

        EggBoxMonster brump = (EggBoxMonster)island.buyMonster(catalog.brump());
        try {

            assertEquals(2, brump.getRequiredEggCount(catalog.fwog()));
            assertEquals(2, brump.getRemainingEggCount(catalog.fwog()));

            assertEquals(6, brump.getRequiredEggCount(catalog.furcorn()));
            assertEquals(6, brump.getRemainingEggCount(catalog.furcorn()));

        }
        finally {
            brump.sell();
        }

    }

    @Test
    @DisplayName("Zap egg (noggin into dwumrohl)")
    public void zapEgg() throws ClientException, InterruptedException {

        Island plantIsland = client.getIsland(IslandType.PLANT_ISLAND);
        Monster noggin = plantIsland.getMonsterOfSpecies(catalog.noggin());
        Monster mate = plantIsland.getMonsters().stream()
                .filter(m -> m.getSpecies().getElements().size() >= 3)
                .filter(m -> !catalog.bowgart().matches(m.getSpecies()))
                .max(Comparator.comparing(m -> m.getSpecies().getElements().size()))
                .orElse(null);

        assumeTrue(noggin != null, "No noggin on Plant Island (to breed a new noggin)");
        assumeTrue(mate != null, "No mate found for noggin on Plant Island (to breed a noggin)");

        BreedingStructure breeder = plantIsland.breed(noggin, mate);
        logger.info("Bred {} and {}, waiting for {}", noggin, mate, breeder.getRemainingTime());
        breeder.waitUntilDone();

        EggBoxMonster dwumrohl = (EggBoxMonster)island.buyMonster(catalog.dwumrohl());
        try {

            breeder.zapEgg(dwumrohl);

            assertEquals(1, dwumrohl.getZappedEggCount(catalog.noggin()));
            assertEquals(7, dwumrohl.getRemainingEggCount(catalog.noggin()));
            assertEquals(8, dwumrohl.getRequiredEggCount(catalog.noggin()));

            client.refresh();

            assertEquals(1, dwumrohl.getZappedEggCount(catalog.noggin()));
            assertEquals(7, dwumrohl.getRemainingEggCount(catalog.noggin()));
            assertEquals(8, dwumrohl.getRequiredEggCount(catalog.noggin()));

            dwumrohl.sellEggs();

        }
        finally {
            dwumrohl.sell();
        }

    }

    @Test
    @DisplayName("Collect from active wublin")
    public void collectFromActiveWublin() throws ClientException, InterruptedException {

        EggBoxMonster monster = island.getMonsters().stream()
                .map(m -> (EggBoxMonster)m)
                .filter(ReadableEggBoxMonster::isActivated)
                .filter(ReadableEggBoxMonster::isReadyToCollect)
                .findFirst().orElse(null);

        assumeTrue(
                monster != null,
                "No active and ready (to collect) wublin is present on Wublin Island"
        );

        CurrencyType currencyType = monster.getNextCollectionType();
        logger.info("Collecting from wublin {} (expecting {})", monster, currencyType);
        MultiCurrencyValue currency = monster.collect();
        logger.info("Collected {}", currency);
        assertFalse(currency.isZero());
        assertEquals(currencyType, currency.getCurrencyType());

    }

    @Test
    @DisplayName("Activate wublin")
    public void activateWublin() throws ClientException, InterruptedException {

        EggBoxMonster monster = island.getMonsters().stream()
                .map(m -> (EggBoxMonster)m)
                .filter(ReadableEggBoxMonster::isReadyToActivate)
                .findFirst().orElse(null);

        assumeTrue(
                monster != null,
                "No ready-to-activate wublin is present on Wublin Island"
        );

        assertFalse(monster.isActivated());
        assertNull(monster.getFillTimer());

        monster.activate();

        assertTrue(monster.isActivated());
        for (ZappedEggData data : monster.getEggData()) {
            assertEquals(0, data.remainingCount());
        }

        client.refresh();

        assertTrue(monster.isActivated());
        for (ZappedEggData data : monster.getEggData()) {
            assertEquals(0, data.remainingCount());
        }

    }

}
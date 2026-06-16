package com.glisterbyte.singingmonsters;

import com.glisterbyte.singingmonsters.main.catalog.MonsterSpecies;
import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.main.common.HasTimer;
import com.glisterbyte.singingmonsters.main.common.MultiCurrencyValue;
import com.glisterbyte.singingmonsters.main.common.Position;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.main.island.IslandType;
import com.glisterbyte.singingmonsters.main.monster.Monster;
import com.glisterbyte.singingmonsters.main.monster.MonsterPlacement;
import com.glisterbyte.singingmonsters.main.structure.nursery.Nursery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

// 16 July 2026, 4:28 AM CST - all tests passing

@ExtendWith(FailOnceExtension.class)
public class NurseryTests extends ClientBoundTests {

    private static Nursery nursery;
    private static Island island;
    private static MonsterSpecies species;

    private static boolean hasNoggin(IslandType islandType) {
        return switch (islandType) {
            case PLANT_ISLAND, AIR_ISLAND, WATER_ISLAND, EARTH_ISLAND -> true;
            default -> false;
        };
    }

    @BeforeAll
    public static void init() throws ClientException, InterruptedException {

        initClient();

        nursery = client.getIslands().stream().filter(i -> hasNoggin(i.getIslandType()))
                .flatMap(i -> i.getNurseries().stream())
                .filter(HasTimer::isIdle)
                .findFirst().orElse(null);

        if (nursery != null) {
            island = nursery.getIsland();
            species = client.getMonsterCatalog().noggin().getSpecies(island);
        }

    }

    private void buyNoggin() throws ClientException, InterruptedException {

        MultiCurrencyValue currencyBefore = client.getCurrency();
        MultiCurrencyValue cost = species.getCost();

        nursery.buyEgg(species);

        assertEquals(currencyBefore.subtract(cost), client.getCurrency());
        assertTrue(nursery.getUserEggId().isPresent());
        assertEquals(nursery.getEggSpecies(), species);

        client.refresh();

        assertEquals(currencyBefore.subtract(cost), client.getCurrency());
        assertTrue(nursery.getUserEggId().isPresent());
        assertEquals(nursery.getEggSpecies(), species);

    }

    @Test
    @DisplayName("Buy, place, and sell a noggin")
    public void buyPlaceSell() throws ClientException, InterruptedException {

        assumeTrue(nursery != null, "Could not find an available nursery");

        buyNoggin();
        nursery.waitUntilDone();

        Position position = getNewPosition(island, species.getSize(), null);
        Monster monster = nursery.hatchEgg(new MonsterPlacement(position, false));

        assertNotNull(monster);
        assertEquals(position, monster.getPosition());
        assertFalse(monster.isFlipped());

        client.refresh();

        assertNotNull(monster);
        assertEquals(position, monster.getPosition());
        assertFalse(monster.isFlipped());

        monster.sell();

    }

    @Test
    @DisplayName("Buy, hatch, and immediately sell a noggin")
    public void buyHatchSell() throws ClientException, InterruptedException {

        assumeTrue(nursery != null, "Could not find an available nursery");

        buyNoggin();
        nursery.waitUntilDone();

        long coinsBefore = client.getCoins();

        nursery.sellEgg();

        assertTrue(nursery.isIdle());
        assertTrue(coinsBefore < client.getCoins());

        client.refresh();

        assertTrue(nursery.isIdle());
        assertTrue(coinsBefore < client.getCoins());

    }

}
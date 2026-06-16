package com.glisterbyte.singingmonsters;

import com.glisterbyte.singingmonsters.main.catalog.BakeryRecipe;
import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.main.island.IslandType;
import com.glisterbyte.singingmonsters.main.structure.bakery.Bakery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

// 16 July 2026, 4:24 AM CST - all tests passing

@ExtendWith(FailOnceExtension.class)
public class BakeryTests extends ClientBoundTests {

    private static Bakery bakery;

    @BeforeAll
    public static void init() throws ClientException, InterruptedException {

        initClient();

        Island island = client.getIsland(IslandType.PLANT_ISLAND);

        bakery = island.getBakeries().stream().findFirst().orElse(null);

        if (bakery == null) bakery = (Bakery)island.buyStructure(client.getStructureCatalog().smallBakery());

        assertNotNull(bakery);

    }

    @Test
    @DisplayName("Bake cupcakes")
    public void bakeCupcakes() throws ClientException, InterruptedException {

        BakeryRecipe recipe = client.getBakingCatalog().cupcakes();

        long foodBefore = client.getFood();
        long foodAfter = foodBefore + (long)recipe.getAmount();

        bakery.bake(recipe);

        assertTrue(bakery.getUserBakingId().isPresent());
        assertEquals(recipe, bakery.getCurrentRecipe());
        assertTrue(bakery.isCurrentlyBaking());

        client.refresh();

        assertTrue(bakery.getUserBakingId().isPresent());
        assertEquals(recipe, bakery.getCurrentRecipe());
        assertTrue(bakery.isCurrentlyBaking());

        bakery.waitUntilDone();

        assertTrue(bakery.isDone());

        bakery.collect();

        assertTrue(bakery.getUserBakingId().isEmpty());
        assertNull(bakery.getCurrentRecipe());
        assertFalse(bakery.isCurrentlyBaking());
        assertEquals(foodAfter, client.getFood());

        client.refresh();

        assertTrue(bakery.getUserBakingId().isEmpty());
        assertNull(bakery.getCurrentRecipe());
        assertFalse(bakery.isCurrentlyBaking());
        assertEquals(foodAfter, client.getFood());

    }

}

package com.glisterbyte.singingmonsters;

import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.main.common.CurrencyType;
import com.glisterbyte.singingmonsters.main.common.MultiCurrencyValue;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.main.structure.obstacle.Obstacle;
import com.glisterbyte.singingmonsters.main.structure.obstacle.ReadableObstacle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

// 16 July 2026, 4:29 AM CST - all tests passing

@ExtendWith(FailOnceExtension.class)
public class ObstacleTests extends ClientBoundTests {

    @BeforeAll
    public static void init() throws ClientException, InterruptedException {
        initClient();
    }

    @Test
    @DisplayName("Start removal of an obstacle")
    public void startRemoval() throws ClientException, InterruptedException {

        Obstacle obstacle = client.getIslands().stream()
                .filter(i -> i.getCollectionCurrencyType() == CurrencyType.COINS)
                .flatMap(i -> i.getObstacles().stream())
                .filter(o -> !o.isBeingRemoved())
                .min(Comparator.comparing(o -> o.getRemovalCost().coins())).orElse(null);

        assumeTrue(obstacle != null, "Could not find an obstacle to start removing");

        MultiCurrencyValue currencyBefore = client.getCurrency();
        MultiCurrencyValue removalCost = obstacle.getRemovalCost();

        obstacle.startRemoval();

        assertTrue(obstacle.isBeingRemoved());
        assertNotNull(obstacle.getRemovalTimer());
        assertEquals(currencyBefore.subtract(removalCost), client.getCurrency());

        client.refresh();

        assertTrue(obstacle.isBeingRemoved());
        assertNotNull(obstacle.getRemovalTimer());
        assertEquals(currencyBefore.subtract(removalCost), client.getCurrency());

    }

    @Test
    @DisplayName("Finish removal of an obstacle")
    public void finishRemoval() throws ClientException, InterruptedException {

        Obstacle obstacle = client.getIslands().stream()
                .flatMap(i -> i.getObstacles().stream())
                .filter(ReadableObstacle::isRemovalDone)
                .findFirst().orElse(null);

        assumeTrue(obstacle != null, "Could not find an obstacle to finish removing");

        obstacle.finishRemoval();

        Island island = obstacle.getIsland();
        assertFalse(island.getStructures().contains(obstacle));
        long userStructureId = obstacle.getUserStructureId();

        client.refresh();

        assertFalse(island.getStructures().contains(obstacle));
        assertTrue(island.getStructures().stream().noneMatch(m -> m.getUserStructureId() == userStructureId));

    }

}
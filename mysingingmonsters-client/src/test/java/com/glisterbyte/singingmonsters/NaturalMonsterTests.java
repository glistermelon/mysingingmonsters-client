package com.glisterbyte.singingmonsters;

import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.main.common.MultiCurrencyValue;
import com.glisterbyte.singingmonsters.main.common.Position;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.main.island.IslandType;
import com.glisterbyte.singingmonsters.main.monster.Monster;
import com.glisterbyte.singingmonsters.main.monster.MonsterPlacement;
import com.glisterbyte.singingmonsters.main.structure.nursery.Nursery;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// 16 July 2026, 4:22 AM CST - all tests passing

@ExtendWith(FailOnceExtension.class)
public class NaturalMonsterTests extends ClientBoundTests {

    private static Island island;
    private static Monster monster;

    @BeforeAll
    public static void init() throws ClientException, InterruptedException {

        initClient();

        island = client.getIsland(IslandType.PLANT_ISLAND);

        Position position = getNewPosition(
                island, client.getMonsterCatalog().noggin().getSpecies(island).getSize(), null
        );
        monster = buyNoggin(new MonsterPlacement(position, true));

        assertNotNull(monster);
        assertEquals(position, monster.getPosition());
        assertTrue(monster.isFlipped());

        client.refresh();

        assertEquals(position, monster.getPosition());
        assertTrue(monster.isFlipped());
        assertEquals(monster.getSpecies(), client.getMonsterCatalog().noggin().getSpecies(island));
        assertTrue(client.getMonsterCatalog().noggin().matches(monster.getSpecies()));
        assertTrue(monster.isActivated());

    }

    @AfterAll
    public static void cleanup() throws ClientException, InterruptedException {

        monster.sell();

        assertFalse(island.getMonsters().contains(monster));
        long userMonsterId = monster.getUserMonsterId();

        client.refresh();

        assertFalse(island.getMonsters().contains(monster));
        assertTrue(island.getMonsters().stream().noneMatch(m -> m.getUserMonsterId() == userMonsterId));

    }

    private static String generateRandomName() {
        String chars = "abcdefghijklmnopqrstuvwxyz123456789";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            builder.append(chars.charAt((int)(chars.length() * Math.random())));
        }
        return builder.toString();
    }

    private static Monster buyNoggin(MonsterPlacement placement) throws ClientException, InterruptedException {
        Nursery nursery = island.buyMonsterEgg(client.getMonsterCatalog().noggin());
        assertNotNull(nursery);
        nursery.waitUntilDone();
        return nursery.hatchEgg(placement);
    }

    @Test
    @DisplayName("Feed monster")
    public void feedMonster() throws ClientException, InterruptedException {

        for (int timesFed = 0; timesFed < 4; timesFed++) {
            assertEquals(timesFed, monster.getTimesFed());
            long foodBefore = client.getFood();
            monster.feed();
            assertEquals((timesFed + 1) % 4, monster.getTimesFed());
            long foodAfter = client.getFood();
            assertTrue(foodAfter < foodBefore);
        }

        int level = monster.getLevel();
        monster.feedToNextLevel();
        assertEquals(level + 1, monster.getLevel());

        client.refresh();

        assertEquals(level + 1, monster.getLevel());

        level = 4;
        monster.feedToLevel(level);
        assertEquals(level, monster.getLevel());

        client.refresh();
        assertEquals(level, monster.getLevel());

        monster.feedToLevel(1);
        assertEquals(level, monster.getLevel());

        monster.feedToLevel(level);
        assertEquals(level, monster.getLevel());

    }

    @Test
    @DisplayName("Collect monster")
    public void collectMonster() throws ClientException, InterruptedException {

        double coinsPerSec = (double)monster.getSpecies().getMonsterLevelData(monster.getLevel()).coinsPerMinute() / 60;
        int secondsPerCoin = (int)Math.ceil(1.0 / coinsPerSec);
        Thread.sleep(Duration.ofSeconds(secondsPerCoin + 1));

        MultiCurrencyValue first = monster.collect();
        MultiCurrencyValue second = monster.collect();

        assertFalse(first.isZero());
        assertTrue(second.isZero());
        assertSame(first.getCurrencyType(), monster.getCollectionCurrencyType());

    }

    @Test
    @DisplayName("Move monster")
    public void moveMonster() throws ClientException, InterruptedException {

        Position position = getNewPosition(island, monster.getSize(), monster.getPosition());
        MonsterPlacement placement = MonsterPlacement.createDefault().withPosition(position);

        monster.move(placement);

        assertEquals(placement.position(), monster.getPosition());
        assertEquals(placement.flipped(), monster.isFlipped());
        assertEquals(placement, monster.getPlacement());

        client.refresh();

        assertEquals(placement.position(), monster.getPosition());
        assertEquals(placement.flipped(), monster.isFlipped());
        assertEquals(placement, monster.getPlacement());

    }

    @Test
    @DisplayName("Flip monster")
    public void flipMonster() throws ClientException, InterruptedException {
        boolean flipped = monster.isFlipped();
        monster.flip();
        assertEquals(!flipped, monster.isFlipped());
        client.refresh();
        assertEquals(!flipped, monster.isFlipped());
        monster.flip();
        assertEquals(flipped, monster.isFlipped());
        client.refresh();
        assertEquals(flipped, monster.isFlipped());
    }

    @Test
    @DisplayName("Change monster volume")
    public void changeMonsterVolume() throws ClientException, InterruptedException {

        for (double volume : List.of(-1.0, 0.0, 0.5, 1.0, 1.5)) {
            monster.setVolume(volume);
            assertEquals(volume, monster.getVolume(), 0.01);
        }

        double volume = 0.5 + 0.3 * Math.random();
        monster.setVolume(volume);
        assertEquals(volume, monster.getVolume(), 0.01);
        client.refresh();
        assertEquals(volume, monster.getVolume(), 0.01);

    }

    @Test
    @DisplayName("Mute monster")
    public void muteMonster() throws ClientException, InterruptedException {
        boolean muted = monster.isMuted();
        monster.setMuted(!muted);
        assertEquals(!muted, monster.isMuted());
        client.refresh();
        assertEquals(!muted, monster.isMuted());
        monster.setMuted(muted);
        assertEquals(muted, monster.isMuted());
        client.refresh();
        assertEquals(muted, monster.isMuted());
    }

    @Test
    @DisplayName("Change monster name")
    public void nameMonster() throws ClientException, InterruptedException {
        String name = generateRandomName();
        monster.setName(name);
        assertEquals(name, monster.getName());
        client.refresh();
        assertEquals(name, monster.getName());
    }

}
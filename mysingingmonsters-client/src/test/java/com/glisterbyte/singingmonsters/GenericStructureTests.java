package com.glisterbyte.singingmonsters;

import com.glisterbyte.singingmonsters.main.catalog.StructureType;
import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.main.common.Position;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.main.island.IslandType;
import com.glisterbyte.singingmonsters.main.structure.Structure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

// 16 July 2026, 4:25 AM CST - all tests passing

@ExtendWith(FailOnceExtension.class)
public class GenericStructureTests extends ClientBoundTests {

    private static Island island;
    private static Structure structure;

    @BeforeAll
    public static void init() throws ClientException, InterruptedException {

        initClient();

        StructureType structureType = client.getStructureCatalog().flappyFlag();
        island = client.getIsland(IslandType.PLANT_ISLAND);

        Position position = getNewPosition(island, structureType.getSize(), null);
        structure = island.buyStructure(structureType, position);

        assertNotNull(structure);
        assertEquals(position, structure.getPosition());
        assertEquals(structureType, structure.getStructureType());

        client.refresh();

        assertEquals(position, structure.getPosition());
        assertEquals(structureType, structure.getStructureType());

    }

    @AfterAll
    public static void cleanup() throws ClientException, InterruptedException {

        structure.sell();

        assertFalse(island.getStructures().contains(structure));
        long userStructureId = structure.getUserStructureId();

        client.refresh();

        assertFalse(island.getStructures().contains(structure));
        assertTrue(island.getStructures().stream().noneMatch(m -> m.getUserStructureId() == userStructureId));

    }

    @Test
    @DisplayName("Move structure")
    public void moveStructure() throws ClientException, InterruptedException {

        Position position = getNewPosition(island, structure.getSize(), structure.getPosition());
        double scale = structure.getScale() == 0.8 ? 0.9 : 0.8;

        structure.move(position, scale);

        assertEquals(position, structure.getPosition());
        assertEquals(scale, structure.getScale(), 0.001);

        client.refresh();

        assertEquals(position, structure.getPosition());
        assertEquals(scale, structure.getScale(), 0.001);

    }

    @Test
    @DisplayName("Flip structure")
    public void flipStructure() throws ClientException, InterruptedException {
        boolean flipped = structure.isFlipped();
        structure.flip();
        assertEquals(!flipped, structure.isFlipped());
        client.refresh();
        assertEquals(!flipped, structure.isFlipped());
        structure.flip();
        assertEquals(flipped, structure.isFlipped());
        client.refresh();
        assertEquals(flipped, structure.isFlipped());
    }

    @Test
    @DisplayName("Scale structure")
    @SuppressWarnings("ConstantConditions")
    public void scaleStructure() throws ClientException, InterruptedException {

        double scale;

        scale = 0.5;
        assertTrue(scale < Structure.MIN_SCALE);
        structure.setScale(scale);
        assertEquals(Structure.MIN_SCALE, structure.getScale(), 0.001);
        client.refresh();
        assertEquals(Structure.MIN_SCALE, structure.getScale(), 0.001);

        scale = 0.87;
        structure.setScale(scale);
        assertEquals(scale, structure.getScale(), 0.001);
        client.refresh();
        assertEquals(scale, structure.getScale(), 0.001);

        scale = 1.2;
        structure.setScale(scale);
        assertTrue(scale > Structure.MAX_SCALE);
        assertEquals(Structure.MAX_SCALE, structure.getScale(), 0.001);
        client.refresh();
        assertEquals(Structure.MAX_SCALE, structure.getScale(), 0.001);

    }

}
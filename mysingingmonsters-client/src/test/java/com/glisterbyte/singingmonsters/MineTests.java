package com.glisterbyte.singingmonsters;

import com.glisterbyte.singingmonsters.exceptions.ClientException;
import com.glisterbyte.singingmonsters.main.island.Island;
import com.glisterbyte.singingmonsters.main.structure.mine.Mine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

// 16 July 2026, 4:25 AM CST - all tests passing

@ExtendWith(FailOnceExtension.class)
public class MineTests extends ClientBoundTests {

    @BeforeAll
    public static void init() throws ClientException, InterruptedException {
        initClient();
    }

    @Test
    @DisplayName("Collect mine")
    public void collectMine() throws ClientException, InterruptedException {

        Mine mine = client.getIslands().stream().map(Island::getMine)
                .filter(m -> m != null && m.isReady()).findFirst().orElse(null);

        assumeTrue(mine != null, "No ready-to-collect mine found");

        assertTrue(mine.isReady());

        long diamondsBefore = client.getDiamonds();

        mine.collect();

        assertFalse(mine.isReady());
        assertEquals(diamondsBefore + 1, client.getDiamonds());

    }

}

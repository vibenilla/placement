package rocks.minestom.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CoverageTest {
    @BeforeAll
    static void initServer() {
        MinecraftServer.init();
        var blockManager = MinecraftServer.getBlockManager();
        for (var block : NonSpecialPlacementBlocks.BLOCKS) {
            blockManager.registerBlockPlacementRule(new DummyPlacementRule(block));
        }
        Registrations.registerAllVanilla(blockManager);
    }

    @Test
    void everyBlockHasPlacementRule() {
        var blockManager = MinecraftServer.getBlockManager();
        var missing = new ArrayList<Block>();

        for (var block : Block.values()) {
            if (blockManager.getBlockPlacementRule(block) == null) {
                missing.add(block);
            }
        }

        assertTrue(missing.isEmpty(), "Blocks without a registered placement rule: " + missing);
    }
}

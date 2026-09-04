package rocks.minestom.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SupportPlacementTest {
    @BeforeAll
    static void initServer() {
        MinecraftServer.init();
    }

    @Test
    void redstoneWireRequiresSupportOnPlacement() {
        var rule = new RedstoneWirePlacementRule(Block.REDSTONE_WIRE);
        var position = new BlockVec(0, 0, 0);
        var state = placementState(rule.getBlock(), blocksAt(Map.of()), position, BlockFace.TOP);

        assertNull(rule.blockPlace(state));

        var supportedState = placementState(rule.getBlock(),
                blocksAt(Map.of(new BlockVec(0, -1, 0), Block.STONE)), position, BlockFace.TOP);

        assertNotNull(rule.blockPlace(supportedState));
    }

    @Test
    void redstoneWireIsRemovedWhenSupportIsRemoved() {
        var rule = new RedstoneWirePlacementRule(Block.REDSTONE_WIRE);
        var position = new BlockVec(0, 0, 0);
        var getter = blocksAt(Map.of());
        var update = new BlockPlacementRule.UpdateState(getter, position, Block.REDSTONE_WIRE, BlockFace.BOTTOM);

        assertEquals(Block.AIR, rule.blockUpdate(update));
    }

    @Test
    void shelfIsRemovedWhenItsWallSupportIsRemoved() {
        var rule = new ShelfPlacementRule(Block.OAK_SHELF);
        var position = new BlockVec(0, 0, 0);
        var shelf = Block.OAK_SHELF.withProperty("facing", "north").withProperty("powered", "false");
        var update = new BlockPlacementRule.UpdateState(blocksAt(Map.of()), position, shelf, BlockFace.SOUTH);

        assertEquals(Block.AIR, rule.blockUpdate(update));
    }

    @Test
    void lilyPadPlacementMatchesItsSupportUpdateRule() {
        var rule = new LilyPadPlacementRule(Block.LILY_PAD);
        var position = new BlockVec(0, 0, 0);
        var state = placementState(rule.getBlock(),
                blocksAt(Map.of(new BlockVec(0, -1, 0), Block.ICE)), position, BlockFace.TOP);

        assertNotNull(rule.blockPlace(state));
    }

    @Test
    void centeredSupportsAreAcceptedForCenteredAttachments() {
        assertTrue(Utility.canSupportCenter(Block.OAK_FENCE, BlockFace.TOP));
        assertTrue(Utility.canSupportCenter(Block.STONE, BlockFace.TOP));
    }

    private static BlockPlacementRule.PlacementState placementState(
            Block block, Block.Getter getter, BlockVec position, BlockFace blockFace) {
        return new BlockPlacementRule.PlacementState(
                getter, block, blockFace, position, position, new Pos(position), ItemStack.AIR, false);
    }

    private static Block.Getter blocksAt(Map<BlockVec, Block> blocks) {
        return (x, y, z, condition) -> blocks.getOrDefault(new BlockVec(x, y, z), Block.AIR);
    }
}

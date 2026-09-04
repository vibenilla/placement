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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    void chestPairingUpdatesBothHalves() {
        var rule = new ChestPlacementRule(Block.CHEST);
        var position = new BlockVec(0, 0, 0);
        var neighbor = Block.CHEST.withProperty("facing", "north").withProperty("type", "right");
        var current = Block.CHEST.withProperty("facing", "north").withProperty("type", "single");
        var getter = blocksAt(Map.of(new BlockVec(1, 0, 0), neighbor));
        var update = new BlockPlacementRule.UpdateState(getter, position, current, BlockFace.EAST);

        assertEquals("left", rule.blockUpdate(update).getProperty("type"));

        var facing = BlockFace.fromYaw(0.0F).getOppositeFace();
        var placementNeighbor = position.relative(clockwise(facing));
        var placementState = placementState(
                rule.getBlock(),
                blocksAt(Map.of(placementNeighbor, Block.CHEST.withProperty("facing", facing.name().toLowerCase())
                        .withProperty("type", "single"))),
                position,
                BlockFace.TOP);

        assertEquals("left", rule.blockPlace(placementState).getProperty("type"));

        var paired = current.withProperty("type", "left");
        var brokenUpdate = new BlockPlacementRule.UpdateState(
                blocksAt(Map.of()), position, paired, BlockFace.EAST);

        assertEquals("single", rule.blockUpdate(brokenUpdate).getProperty("type"));
    }

    @Test
    void redstoneConnectionsRecomputeAfterNeighborRemoval() {
        var rule = new RedstoneWirePlacementRule(Block.REDSTONE_WIRE);
        var position = new BlockVec(0, 0, 0);
        var current = Block.REDSTONE_WIRE
                .withProperty("north", "side")
                .withProperty("east", "none")
                .withProperty("south", "side")
                .withProperty("west", "none")
                .withProperty("power", "0");
        var update = new BlockPlacementRule.UpdateState(
                blocksAt(Map.of(new BlockVec(0, -1, 0), Block.STONE)), position, current, BlockFace.SOUTH);

        var updated = rule.blockUpdate(update);

        assertNotEquals(current, updated);
        assertEquals("side", updated.getProperty("east"));
        assertEquals("side", updated.getProperty("west"));
    }

    @Test
    void railsRecomputeShapeAfterNeighborRemoval() {
        var rule = new RailPlacementRule(Block.RAIL);
        var position = new BlockVec(0, 0, 0);
        var current = Block.RAIL.withProperty("shape", "south_east").withProperty("waterlogged", "false");
        var update = new BlockPlacementRule.UpdateState(
                blocksAt(Map.of(new BlockVec(0, -1, 0), Block.STONE)), position, current, BlockFace.EAST);

        assertEquals("north_south", rule.blockUpdate(update).getProperty("shape"));
    }

    @Test
    void hugeMushroomCapRecomputesAfterNeighborRemoval() {
        var rule = new HugeMushroomPlacementRule(Block.BROWN_MUSHROOM_BLOCK);
        var position = new BlockVec(0, 0, 0);
        var current = Block.BROWN_MUSHROOM_BLOCK.withProperty("up", "false");
        var getter = blocksAt(Map.of(new BlockVec(0, 1, 0), Block.BROWN_MUSHROOM_BLOCK));
        var update = new BlockPlacementRule.UpdateState(getter, position, current, BlockFace.TOP);

        assertEquals("false", rule.blockUpdate(update).getProperty("up"));
        assertEquals("true", rule.blockUpdate(new BlockPlacementRule.UpdateState(
                blocksAt(Map.of()), position, current, BlockFace.TOP)).getProperty("up"));
    }

    @Test
    void tripwireHookRecomputesLineState() {
        var rule = new TripWireHookPlacementRule(Block.TRIPWIRE_HOOK);
        var position = new BlockVec(0, 0, 0);
        var current = Block.TRIPWIRE_HOOK.withProperty("facing", "east")
                .withProperty("attached", "false").withProperty("powered", "false");
        var wire = Block.TRIPWIRE.withProperty("powered", "false");
        var otherHook = Block.TRIPWIRE_HOOK.withProperty("facing", "west");
        var getter = blocksAt(Map.of(
                new BlockVec(-1, 0, 0), Block.STONE,
                new BlockVec(1, 0, 0), wire,
                new BlockVec(2, 0, 0), otherHook));
        var update = new BlockPlacementRule.UpdateState(getter, position, current, BlockFace.EAST);

        assertEquals("true", rule.blockUpdate(update).getProperty("attached"));
        assertEquals("false", rule.blockUpdate(new BlockPlacementRule.UpdateState(
                blocksAt(Map.of(new BlockVec(-1, 0, 0), Block.STONE)), position, current, BlockFace.EAST))
                .getProperty("attached"));
    }

    @Test
    void centeredSupportsAreAcceptedForCenteredAttachments() {
        assertTrue(Utility.canSupportCenter(Block.OAK_FENCE, BlockFace.TOP));
        assertTrue(Utility.canSupportCenter(Block.STONE, BlockFace.TOP));
    }

    @Test
    void waterloggableBlocksAcceptFlowingWater() {
        var position = new BlockVec(0, 0, 0);
        var flowingWater = Block.WATER.withProperty("level", "1");
        var rule = new WaterloggedAxisPlacementRule(Block.IRON_CHAIN);
        var state = placementState(rule.getBlock(), blocksAt(Map.of(position, flowingWater)), position, BlockFace.TOP);

        assertEquals("true", rule.blockPlace(state).getProperty("waterlogged"));
    }

    @Test
    void conduitRequiresSourceWater() {
        var position = new BlockVec(0, 0, 0);
        var rule = new WaterloggedDummyPlacementRule(Block.CONDUIT, true);
        var flowingState = placementState(
                rule.getBlock(), blocksAt(Map.of(position, Block.WATER.withProperty("level", "1"))), position, BlockFace.TOP);
        var sourceState = placementState(
                rule.getBlock(), blocksAt(Map.of(position, Block.WATER.withProperty("level", "0"))), position, BlockFace.TOP);

        assertEquals("false", rule.blockPlace(flowingState).getProperty("waterlogged"));
        assertEquals("true", rule.blockPlace(sourceState).getProperty("waterlogged"));
    }

    private static BlockPlacementRule.PlacementState placementState(
            Block block, Block.Getter getter, BlockVec position, BlockFace blockFace) {
        return new BlockPlacementRule.PlacementState(
                getter, block, blockFace, position, position, new Pos(position), ItemStack.AIR, false);
    }

    private static Block.Getter blocksAt(Map<BlockVec, Block> blocks) {
        return (x, y, z, condition) -> blocks.getOrDefault(new BlockVec(x, y, z), Block.AIR);
    }

    private static BlockFace clockwise(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> face;
        };
    }
}

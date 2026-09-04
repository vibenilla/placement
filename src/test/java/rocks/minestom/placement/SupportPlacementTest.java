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
    void fireRequiresSupportOrFuel() {
        var rule = new FirePlacementRule(Block.FIRE);
        var position = new BlockVec(0, 0, 0);
        var unsupported = placementState(rule.getBlock(), blocksAt(Map.of()), position, BlockFace.TOP);
        var sideFuel = blocksAt(Map.of(position.relative(BlockFace.NORTH), Block.OAK_PLANKS));

        assertNull(rule.blockPlace(unsupported));
        assertEquals("true", rule.blockPlace(
                placementState(rule.getBlock(), sideFuel, position, BlockFace.TOP)).getProperty("north"));
    }

    @Test
    void fireSelectsSoulFireAndPreservesItsAgeOnUpdates() {
        var rule = new FirePlacementRule(Block.FIRE);
        var position = new BlockVec(0, 0, 0);
        var soulSand = blocksAt(Map.of(position.relative(BlockFace.BOTTOM), Block.SOUL_SAND));

        assertEquals(Block.SOUL_FIRE, rule.blockPlace(
                placementState(rule.getBlock(), soulSand, position, BlockFace.TOP)));

        var fire = Block.FIRE.withProperty("age", "7");
        var supported = blocksAt(Map.of(position.relative(BlockFace.BOTTOM), Block.STONE));
        var update = new BlockPlacementRule.UpdateState(supported, position, fire, BlockFace.BOTTOM);

        assertEquals("7", rule.blockUpdate(update).getProperty("age"));
    }

    @Test
    void noteBlocksDeriveTheirInstrumentFromNeighbors() {
        var rule = new NoteBlockPlacementRule(Block.NOTE_BLOCK);
        var position = new BlockVec(0, 0, 0);
        var below = position.relative(BlockFace.BOTTOM);
        var above = position.relative(BlockFace.TOP);

        assertEquals("basedrum", rule.blockPlace(placementState(
                rule.getBlock(), blocksAt(Map.of(below, Block.STONE)), position, BlockFace.TOP)).getProperty("instrument"));
        assertEquals("bell", rule.blockPlace(placementState(
                rule.getBlock(), blocksAt(Map.of(below, Block.GOLD_BLOCK)), position, BlockFace.TOP)).getProperty("instrument"));
        assertEquals("zombie", rule.blockPlace(placementState(
                rule.getBlock(), blocksAt(Map.of(below, Block.GOLD_BLOCK, above, Block.ZOMBIE_HEAD)),
                position, BlockFace.TOP)).getProperty("instrument"));
        assertEquals("harp", rule.blockPlace(placementState(
                rule.getBlock(), blocksAt(Map.of(below, Block.ZOMBIE_HEAD)), position, BlockFace.TOP)).getProperty("instrument"));
    }

    @Test
    void noteBlocksUpdateTheirInstrumentVertically() {
        var rule = new NoteBlockPlacementRule(Block.NOTE_BLOCK);
        var position = new BlockVec(0, 0, 0);
        var current = Block.NOTE_BLOCK.withProperty("instrument", "harp");
        var update = new BlockPlacementRule.UpdateState(
                blocksAt(Map.of(position.relative(BlockFace.BOTTOM), Block.PACKED_ICE)),
                position, current, BlockFace.BOTTOM);

        assertEquals("chime", rule.blockUpdate(update).getProperty("instrument"));
        assertEquals(NoteBlockHandler.INSTANCE, rule.blockUpdate(update).handler());
    }

    @Test
    void leavesTrackTheirDistanceFromLogs() {
        var rule = new LeavesPlacementRule(Block.OAK_LEAVES);
        var position = new BlockVec(0, 0, 0);
        var adjacentLog = blocksAt(Map.of(position.relative(BlockFace.EAST), Block.OAK_LOG));
        var adjacentLeaves = blocksAt(Map.of(
                position.relative(BlockFace.WEST), Block.OAK_LEAVES.withProperty("distance", "3")));

        assertEquals("1", rule.blockPlace(placementState(
                rule.getBlock(), adjacentLog, position, BlockFace.TOP)).getProperty("distance"));
        assertEquals("4", rule.blockUpdate(new BlockPlacementRule.UpdateState(
                adjacentLeaves, position, Block.OAK_LEAVES, BlockFace.WEST)).getProperty("distance"));
    }

    @Test
    void creakingHeartsActivateBetweenAlignedPaleOakLogs() {
        var rule = new CreakingHeartPlacementRule(Block.CREAKING_HEART);
        var position = new BlockVec(0, 0, 0);
        var logs = blocksAt(Map.of(
                position.relative(BlockFace.WEST), Block.PALE_OAK_LOG.withProperty("axis", "x"),
                position.relative(BlockFace.EAST), Block.PALE_OAK_LOG.withProperty("axis", "x")));

        var placed = rule.blockPlace(placementState(rule.getBlock(), logs, position, BlockFace.EAST));

        assertEquals("x", placed.getProperty("axis"));
        assertEquals("dormant", placed.getProperty("creaking_heart_state"));
    }

    @Test
    void unsupportedScaffoldingHasABottomOverPartialBlocks() {
        var rule = new ScaffoldingPlacementRule(Block.SCAFFOLDING);
        var position = new BlockVec(0, 0, 0);
        var side = position.relative(BlockFace.EAST);
        var below = position.relative(BlockFace.BOTTOM);
        var blocks = blocksAt(Map.of(
                side, Block.SCAFFOLDING.withProperty("distance", "0"),
                below, Block.TORCH));

        var placed = rule.blockPlace(placementState(rule.getBlock(), blocks, position, BlockFace.TOP));

        assertEquals("1", placed.getProperty("distance"));
        assertEquals("true", placed.getProperty("bottom"));
    }

    @Test
    void speleothemsUseVanillaThicknessTransitions() {
        var rule = new PointedDripstonePlacementRule(Block.POINTED_DRIPSTONE);
        var position = new BlockVec(0, 0, 0);
        var above = position.relative(BlockFace.TOP);
        var below = position.relative(BlockFace.BOTTOM);
        var support = Block.STONE;
        var downwardTip = Block.POINTED_DRIPSTONE
                .withProperty("vertical_direction", "down")
                .withProperty("thickness", "tip");
        var upwardFrustum = Block.POINTED_DRIPSTONE
                .withProperty("vertical_direction", "up")
                .withProperty("thickness", "frustum");

        var merged = rule.blockPlace(placementState(
                rule.getBlock(), blocksAt(Map.of(above, downwardTip, below, support)), position, BlockFace.TOP));
        var base = rule.blockPlace(placementState(
                rule.getBlock(), blocksAt(Map.of(above, upwardFrustum, below, support)), position, BlockFace.TOP));
        var middle = rule.blockPlace(placementState(rule.getBlock(), blocksAt(Map.of(
                above, upwardFrustum,
                below, Block.POINTED_DRIPSTONE.withProperty("vertical_direction", "up"))), position, BlockFace.TOP));

        assertEquals("tip_merge", merged.getProperty("thickness"));
        assertEquals("base", base.getProperty("thickness"));
        assertEquals("middle", middle.getProperty("thickness"));
    }

    @Test
    void bambooCannotBePlacedInAnyFluid() {
        var rule = new BambooStalkPlacementRule(Block.BAMBOO);
        var position = new BlockVec(0, 0, 0);
        var blocks = blocksAt(Map.of(
                position, Block.LAVA,
                position.relative(BlockFace.BOTTOM), Block.DIRT));

        assertNull(rule.blockPlace(placementState(rule.getBlock(), blocks, position, BlockFace.TOP)));
    }

    @Test
    void kelpRequiresFullWaterAtItsPlacementPosition() {
        var rule = new GrowingPlantHeadPlacementRule(Block.KELP);
        var position = new BlockVec(0, 0, 0);
        var support = position.relative(BlockFace.BOTTOM);

        assertNull(rule.blockPlace(placementState(rule.getBlock(), blocksAt(Map.of(
                support, Block.STONE)), position, BlockFace.TOP)));
    }

    @Test
    void straightWallsDropTheirPostWithoutCover() {
        var rule = new WallPlacementRule(Block.COBBLESTONE_WALL);
        var position = new BlockVec(0, 0, 0);
        var blocks = blocksAt(Map.of(
                position.relative(BlockFace.NORTH), Block.STONE,
                position.relative(BlockFace.SOUTH), Block.STONE));

        var placed = rule.blockPlace(placementState(rule.getBlock(), blocks, position, BlockFace.TOP));

        assertEquals("low", placed.getProperty("north"));
        assertEquals("low", placed.getProperty("south"));
        assertEquals("false", placed.getProperty("up"));
    }

    @Test
    void blocksAboveWallsCoverEachSideIndependently() {
        var rule = new WallPlacementRule(Block.COBBLESTONE_WALL);
        var position = new BlockVec(0, 0, 0);
        var blocks = blocksAt(Map.of(
                position.relative(BlockFace.NORTH), Block.STONE,
                position.relative(BlockFace.SOUTH), Block.STONE,
                position.relative(BlockFace.TOP), Block.STONE));

        var placed = rule.blockPlace(placementState(rule.getBlock(), blocks, position, BlockFace.TOP));

        assertEquals("tall", placed.getProperty("north"));
        assertEquals("tall", placed.getProperty("south"));
        assertEquals("false", placed.getProperty("up"));
    }

    @Test
    void poweredBlocksReadNeighborSignalsOnPlacement() {
        var position = new BlockVec(0, 0, 0);
        var signal = blocksAt(Map.of(position.relative(BlockFace.EAST), Block.REDSTONE_BLOCK));
        var trapdoor = new TrapdoorPlacementRule(Block.OAK_TRAPDOOR).blockPlace(
                placementState(Block.OAK_TRAPDOOR, signal, position, BlockFace.TOP));
        var shelf = new ShelfPlacementRule(Block.OAK_SHELF).blockPlace(
                placementState(Block.OAK_SHELF, signal, position, BlockFace.TOP));
        var skull = new SkullPlacementRule(Block.ZOMBIE_HEAD).blockPlace(
                placementState(Block.ZOMBIE_HEAD, signal, position, BlockFace.TOP));
        var lamp = new RedstoneLampPlacementRule(Block.REDSTONE_LAMP).blockPlace(
                placementState(Block.REDSTONE_LAMP, signal, position, BlockFace.TOP));

        assertEquals("true", trapdoor.getProperty("powered"));
        assertEquals("true", trapdoor.getProperty("open"));
        assertEquals("true", shelf.getProperty("powered"));
        assertEquals("true", skull.getProperty("powered"));
        assertEquals("true", lamp.getProperty("lit"));
    }

    @Test
    void neighborSignalsAreDetectedAtEitherDoorHalf() {
        var position = new BlockVec(0, 0, 0);
        var blocks = blocksAt(Map.of(
                position.relative(BlockFace.TOP).relative(BlockFace.EAST), Block.REDSTONE_BLOCK));

        assertTrue(VanillaPlacementUtils.hasNeighborSignal(blocks, position.relative(BlockFace.TOP)));
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
    void shelvesDoNotRequireWallSupport() {
        var rule = new ShelfPlacementRule(Block.OAK_SHELF);
        var position = new BlockVec(0, 0, 0);
        var state = placementState(rule.getBlock(), blocksAt(Map.of()), position, BlockFace.TOP);

        assertNotNull(rule.blockPlace(state));
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
    void isolatedRedstoneDotsStayDotsOnUpdates() {
        var rule = new RedstoneWirePlacementRule(Block.REDSTONE_WIRE);
        var position = new BlockVec(0, 0, 0);
        var dot = Block.REDSTONE_WIRE
                .withProperty("north", "none")
                .withProperty("east", "none")
                .withProperty("south", "none")
                .withProperty("west", "none");
        var blocks = blocksAt(Map.of(position.relative(BlockFace.BOTTOM), Block.STONE));
        var update = new BlockPlacementRule.UpdateState(blocks, position, dot, BlockFace.EAST);

        assertTrue(RedstoneWirePlacementRule.isDot(rule.blockUpdate(update)));
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
    void lightPlacementPreservesItsSelectedLevel() {
        var position = new BlockVec(0, 0, 0);
        var light = Block.LIGHT.withProperty("level", "4");
        var state = placementState(
                light, blocksAt(Map.of(position, Block.WATER.withProperty("level", "1"))), position, BlockFace.TOP);
        var placed = new LightPlacementRule(Block.LIGHT).blockPlace(state);

        assertEquals("4", placed.getProperty("level"));
        assertEquals("true", placed.getProperty("waterlogged"));
        assertEquals(LightBlockHandler.INSTANCE, placed.handler());
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

    @Test
    void snowyBlocksTrackSnowAbove() {
        var position = new BlockVec(0, 0, 0);
        var rule = new SnowyPlacementRule(Block.GRASS_BLOCK);
        var state = placementState(
                rule.getBlock(), blocksAt(Map.of(position.relative(BlockFace.TOP), Block.SNOW)), position, BlockFace.TOP);

        assertEquals("true", rule.blockPlace(state).getProperty("snowy"));

        var current = Block.GRASS_BLOCK.withProperty("snowy", "true");
        var update = new BlockPlacementRule.UpdateState(blocksAt(Map.of()), position, current, BlockFace.TOP);

        assertEquals("false", rule.blockUpdate(update).getProperty("snowy"));
    }

    @Test
    void farmlandAndDirtPathConvertUnderSolidBlocks() {
        var position = new BlockVec(0, 0, 0);
        var blocks = blocksAt(Map.of(position.relative(BlockFace.TOP), Block.STONE));
        var farmlandRule = new FarmlandPlacementRule(Block.FARMLAND);
        var pathRule = new DirtPathPlacementRule(Block.DIRT_PATH);

        assertEquals(Block.DIRT, farmlandRule.blockPlace(
                placementState(Block.FARMLAND, blocks, position, BlockFace.TOP)));
        assertEquals(Block.DIRT, pathRule.blockPlace(
                placementState(Block.DIRT_PATH, blocks, position, BlockFace.TOP)));
    }

    @Test
    void barrelUsesVerticalLookDirection() {
        var position = new BlockVec(0, 0, 0);
        var rule = new DirectionalPlacementRule(Block.BARREL, ConsumeInteractionBlockHandler.INSTANCE);
        var state = new BlockPlacementRule.PlacementState(
                blocksAt(Map.of()), rule.getBlock(), BlockFace.TOP, position, position,
                new Pos(0.0D, 0.0D, 0.0D, 0.0F, 90.0F), ItemStack.AIR, false);

        assertEquals("up", rule.blockPlace(state).getProperty("facing"));
    }

    @Test
    void observerFacesTowardPlayerView() {
        var position = new BlockVec(0, 0, 0);
        var state = placementState(Block.OBSERVER, blocksAt(Map.of()), position, BlockFace.TOP);
        var observer = new DirectionalPlacementRule(Block.OBSERVER, true).blockPlace(state);
        var piston = new DirectionalPlacementRule(Block.PISTON).blockPlace(state);

        assertEquals(opposite(piston.getProperty("facing")), observer.getProperty("facing"));
    }

    @Test
    void driedGhastAndCalibratedSensorAreWaterlogged() {
        var position = new BlockVec(0, 0, 0);
        var blocks = blocksAt(Map.of(position, Block.WATER.withProperty("level", "1")));
        var state = placementState(Block.DRIED_GHAST, blocks, position, BlockFace.TOP);
        var driedGhast = new WaterloggedHorizontalFacingPlacementRule(Block.DRIED_GHAST, false, null).blockPlace(state);
        var sensor = new WaterloggedHorizontalFacingPlacementRule(
                Block.CALIBRATED_SCULK_SENSOR, true, ConsumeInteractionBlockHandler.INSTANCE).blockPlace(state);

        assertEquals("true", driedGhast.getProperty("waterlogged"));
        assertEquals("true", sensor.getProperty("waterlogged"));
        assertEquals(opposite(driedGhast.getProperty("facing")), sensor.getProperty("facing"));
    }

    @Test
    void bannersAndSignsRequireSolidBlocks() {
        var position = new BlockVec(0, 0, 0);
        var blocks = blocksAt(Map.of(position.relative(BlockFace.BOTTOM), Block.AZALEA));

        assertNull(new BannerPlacementRule(Block.WHITE_BANNER).blockPlace(
                placementState(Block.WHITE_BANNER, blocks, position, BlockFace.TOP)));
        assertNull(new StandingSignPlacementRule(Block.OAK_SIGN).blockPlace(
                placementState(Block.OAK_SIGN, blocks, position, BlockFace.TOP)));
    }

    @Test
    void pressurePlatesAcceptCenteredSupports() {
        var position = new BlockVec(0, 0, 0);
        var blocks = blocksAt(Map.of(position.relative(BlockFace.BOTTOM), Block.OAK_FENCE));
        var rule = new PressurePlatePlacementRule(Block.OAK_PRESSURE_PLATE);

        assertNotNull(rule.blockPlace(placementState(rule.getBlock(), blocks, position, BlockFace.TOP)));
    }

    @Test
    void wallBellFallsBackToFloorSupport() {
        var position = new BlockVec(0, 0, 0);
        var blocks = blocksAt(Map.of(position.relative(BlockFace.BOTTOM), Block.STONE));
        var rule = new BellPlacementRule(Block.BELL);

        var placed = rule.blockPlace(placementState(rule.getBlock(), blocks, position, BlockFace.EAST));

        assertNotNull(placed);
        assertEquals("floor", placed.getProperty("attachment"));
    }

    @Test
    void doubleWallBellKeepsRemainingSupport() {
        var position = new BlockVec(0, 0, 0);
        var current = Block.BELL.withProperty("attachment", "double_wall").withProperty("facing", "west");
        var blocks = blocksAt(Map.of(position.relative(BlockFace.WEST), Block.STONE));
        var update = new BlockPlacementRule.UpdateState(blocks, position, current, BlockFace.EAST);
        var rule = new BellPlacementRule(Block.BELL);

        var updated = rule.blockUpdate(update);

        assertEquals("single_wall", updated.getProperty("attachment"));
        assertEquals("west", updated.getProperty("facing"));
    }

    @Test
    void buttonsRequireFullAttachmentFaces() {
        var position = new BlockVec(0, 0, 0);
        var blocks = blocksAt(Map.of(position.relative(BlockFace.BOTTOM), Block.AZALEA));
        var rule = new FaceAttachedPlacementRule(Block.STONE_BUTTON);

        assertNull(rule.blockPlace(placementState(rule.getBlock(), blocks, position, BlockFace.TOP)));
    }

    @Test
    void tallPlantsRequireVegetationSupport() {
        var position = new BlockVec(0, 0, 0);
        var rule = new TallPlantPlacementRule(Block.SUNFLOWER);
        var unsupported = placementState(rule.getBlock(), blocksAt(Map.of()), position, BlockFace.TOP);

        assertNull(rule.blockPlace(unsupported));
    }

    @Test
    void pitcherCropStartsAsSingleLowerBlock() {
        var position = new BlockVec(0, 0, 0);
        var below = position.relative(BlockFace.BOTTOM);
        var rule = new PitcherCropPlacementRule(Block.PITCHER_CROP);
        var state = placementState(rule.getBlock(), blocksAt(Map.of(below, Block.FARMLAND)), position, BlockFace.TOP);

        var placed = rule.blockPlace(state);

        assertNotNull(placed);
        assertEquals("0", placed.getProperty("age"));
        assertEquals("lower", placed.getProperty("half"));
    }

    @Test
    void berryPlantsAttachTheirHarvestHandler() {
        var position = new BlockVec(0, 0, 0);
        var bush = new CropPlacementRule(Block.SWEET_BERRY_BUSH, BerryBlockHandler.INSTANCE).blockPlace(
                placementState(Block.SWEET_BERRY_BUSH,
                        blocksAt(Map.of(position.relative(BlockFace.BOTTOM), Block.FARMLAND)),
                        position, BlockFace.TOP));
        var vines = new GrowingPlantHeadPlacementRule(Block.CAVE_VINES, BerryBlockHandler.INSTANCE).blockPlace(
                placementState(Block.CAVE_VINES,
                        blocksAt(Map.of(position.relative(BlockFace.TOP), Block.STONE)),
                        position, BlockFace.BOTTOM));

        assertEquals(BerryBlockHandler.INSTANCE, bush.handler());
        assertEquals(BerryBlockHandler.INSTANCE, vines.handler());
    }

    @Test
    void potentSulfurTracksWaterAbove() {
        var position = new BlockVec(0, 0, 0);
        var rule = new PotentSulfurPlacementRule(Block.POTENT_SULFUR);
        var waterAbove = blocksAt(Map.of(position.relative(BlockFace.TOP), Block.WATER));

        assertEquals("wet", rule.blockPlace(
                placementState(rule.getBlock(), waterAbove, position, BlockFace.TOP)).getProperty("potent_sulfur_state"));
        assertEquals("dry", rule.blockPlace(
                placementState(rule.getBlock(), blocksAt(Map.of()), position, BlockFace.TOP)).getProperty("potent_sulfur_state"));
    }

    @Test
    void itemSpecificBlocksDoNotConsumeEveryInteraction() {
        var position = new BlockVec(0, 0, 0);
        var state = placementState(Block.BEEHIVE, blocksAt(Map.of()), position, BlockFace.TOP);
        var sensorState = placementState(Block.CALIBRATED_SCULK_SENSOR, blocksAt(Map.of()), position, BlockFace.TOP);

        assertNull(new HorizontalFacingPlacementRule(Block.BEEHIVE).blockPlace(state).handler());
        assertNull(new WaterloggedHorizontalFacingPlacementRule(
                Block.CALIBRATED_SCULK_SENSOR, true, null).blockPlace(sensorState).handler());
    }

    @Test
    void gameMasterBlocksUseConditionalInteractionHandler() {
        var position = new BlockVec(0, 0, 0);
        var state = placementState(Block.COMMAND_BLOCK, blocksAt(Map.of()), position, BlockFace.TOP);
        var placed = new DirectionalPlacementRule(
                Block.COMMAND_BLOCK, GameMasterInteractionBlockHandler.INSTANCE).blockPlace(state);

        assertEquals(GameMasterInteractionBlockHandler.INSTANCE, placed.handler());
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

    private static String opposite(String face) {
        return switch (face) {
            case "north" -> "south";
            case "east" -> "west";
            case "south" -> "north";
            case "west" -> "east";
            case "up" -> "down";
            case "down" -> "up";
            default -> throw new IllegalArgumentException(face);
        };
    }
}

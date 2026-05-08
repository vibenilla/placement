package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CoverageTest {
    @BeforeAll
    static void initServer() {
        MinecraftServer.init();
        registerAll();
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

    private static void registerAll() {
        registerByTag(AxisPlacementRule::new, "minecraft:logs");
        registerByBlocks(AxisPlacementRule::new,
                Block.CREAKING_HEART,
                Block.HAY_BLOCK,
                Block.DEEPSLATE,
                Block.INFESTED_DEEPSLATE,
                Block.MUDDY_MANGROVE_ROOTS,
                Block.BAMBOO_BLOCK,
                Block.STRIPPED_BAMBOO_BLOCK,
                Block.BASALT,
                Block.POLISHED_BASALT,
                Block.QUARTZ_PILLAR,
                Block.PURPUR_PILLAR,
                Block.BONE_BLOCK,
                Block.OCHRE_FROGLIGHT,
                Block.VERDANT_FROGLIGHT,
                Block.PEARLESCENT_FROGLIGHT);

        registerByTag(SlabPlacementRule::new, "minecraft:slabs");
        registerByTag(StairPlacementRule::new, "minecraft:stairs");
        registerByTag(TrapdoorPlacementRule::new, "minecraft:trapdoors");
        registerByTag(DoorPlacementRule::new, "minecraft:doors");
        registerByTag(FencePlacementRule::new, "minecraft:fences");
        registerByTag(FenceGatePlacementRule::new, "minecraft:fence_gates");
        registerByTag(WallPlacementRule::new, "minecraft:walls");
        registerByTag(FaceAttachedPlacementRule::new, "minecraft:buttons");
        registerByTag(RailPlacementRule::new, "minecraft:rails");
        registerByTag(BedPlacementRule::new, "minecraft:beds");
        registerByTag(BannerPlacementRule::new, "minecraft:banners");
        registerByTag(StandingSignPlacementRule::new, "minecraft:standing_signs");
        registerByTag(WallMountedPlacementRule::new, "minecraft:wall_signs");
        registerByTag(WallMountedPlacementRule::new, "minecraft:wall_banners");
        registerByTag(CeilingHangingSignPlacementRule::new, "minecraft:ceiling_hanging_signs");
        registerByTag(WallHangingSignPlacementRule::new, "minecraft:wall_hanging_signs");
        registerByTag(PlantPlacementRule::new, "minecraft:saplings");
        registerByTag(PlantPlacementRule::new, "minecraft:small_flowers");
        registerByTag(TallPlantPlacementRule::new, "minecraft:tall_flowers");
        registerByTag(CropPlacementRule::new, "minecraft:crops");
        registerByTag(LeavesPlacementRule::new, "minecraft:leaves");
        registerByTag(ShulkerBoxPlacementRule::new, "minecraft:shulker_boxes");
        registerByTag(SegmentedPlacementRule::new, "minecraft:wool_carpets");
        registerByTag(CoralPlantPlacementRule::new, "minecraft:corals");
        registerByTag(CoralWallFanPlacementRule::new, "minecraft:wall_corals");

        registerByBlocks(ChorusPlantPlacementRule::new, Block.CHORUS_PLANT);
        registerByBlocks(MossyCarpetPlacementRule::new, Block.PALE_MOSS_CARPET);
        registerByBlocks(PointedDripstonePlacementRule::new, Block.POINTED_DRIPSTONE);
        registerByBlocks(CopperGolemStatuePlacementRule::new, Block.COPPER_GOLEM_STATUE);
        registerByBlocks(TripWirePlacementRule::new, Block.TRIPWIRE);
        registerByBlocks(RedstoneWirePlacementRule::new, Block.REDSTONE_WIRE);
        registerByBlocks(FirePlacementRule::new, Block.FIRE);
        registerByBlocks(WallMountedPlacementRule::new, Block.LADDER);
        registerByBlocks(TallPlantPlacementRule::new, Block.TALL_SEAGRASS);
        registerByBlocks(block -> new HorizontalFacingPlacementRule(block, true), Block.END_PORTAL_FRAME);
        registerByBlocks(ShelfPlacementRule::new,
                Block.ACACIA_SHELF,
                Block.BAMBOO_SHELF,
                Block.BIRCH_SHELF,
                Block.CHERRY_SHELF,
                Block.CRIMSON_SHELF,
                Block.DARK_OAK_SHELF,
                Block.JUNGLE_SHELF,
                Block.MANGROVE_SHELF,
                Block.OAK_SHELF,
                Block.PALE_OAK_SHELF,
                Block.SPRUCE_SHELF,
                Block.WARPED_SHELF);

        var blockManager = MinecraftServer.getBlockManager();
        registerConcretePowders(blockManager);

        for (var block : Block.values()) {
            if (blockManager.getBlockPlacementRule(block) == null) {
                blockManager.registerBlockPlacementRule(new DummyPlacementRule(block));
            }
        }
    }

    private static void registerByTag(Function<Block, ? extends BlockPlacementRule> factory, String tagKey) {
        var registry = MinecraftServer.process().blocks();
        var tag = registry.getTag(Key.key(tagKey));

        if (tag == null) {
            return;
        }

        var blockManager = MinecraftServer.getBlockManager();

        for (var entry : tag) {
            blockManager.registerBlockPlacementRule(factory.apply(Block.fromKey(entry.key())));
        }
    }

    private static void registerByBlocks(Function<Block, ? extends BlockPlacementRule> factory, Block... blocks) {
        var blockManager = MinecraftServer.getBlockManager();

        for (var block : blocks) {
            blockManager.registerBlockPlacementRule(factory.apply(block));
        }
    }

    private static void registerConcretePowders(net.minestom.server.instance.block.BlockManager blockManager) {
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.WHITE_CONCRETE_POWDER, Block.WHITE_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.ORANGE_CONCRETE_POWDER, Block.ORANGE_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.MAGENTA_CONCRETE_POWDER, Block.MAGENTA_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.LIGHT_BLUE_CONCRETE_POWDER, Block.LIGHT_BLUE_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.YELLOW_CONCRETE_POWDER, Block.YELLOW_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.LIME_CONCRETE_POWDER, Block.LIME_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.PINK_CONCRETE_POWDER, Block.PINK_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.GRAY_CONCRETE_POWDER, Block.GRAY_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.LIGHT_GRAY_CONCRETE_POWDER, Block.LIGHT_GRAY_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.CYAN_CONCRETE_POWDER, Block.CYAN_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.PURPLE_CONCRETE_POWDER, Block.PURPLE_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.BLUE_CONCRETE_POWDER, Block.BLUE_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.BROWN_CONCRETE_POWDER, Block.BROWN_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.GREEN_CONCRETE_POWDER, Block.GREEN_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.RED_CONCRETE_POWDER, Block.RED_CONCRETE));
        blockManager.registerBlockPlacementRule(new ConcretePowderPlacementRule(Block.BLACK_CONCRETE_POWDER, Block.BLACK_CONCRETE));
    }
}

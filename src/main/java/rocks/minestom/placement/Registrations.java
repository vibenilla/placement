package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import org.jetbrains.annotations.NotNull;

public final class Registrations {
    private Registrations() {

    }

    public static void registerAllVanilla(@NotNull BlockManager blockManager) {
        registerHandlers(blockManager);
        registerByTag(AxisPlacementRule::new, "minecraft:logs");
        Utility.registerPlacementRules(
                AxisPlacementRule::new,
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
        registerByTag(block -> new FaceAttachedPlacementRule(block, ButtonBlockHandler.INSTANCE), "minecraft:buttons");
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

        Utility.registerPlacementRules(
                HorizontalFacingPlacementRule::new,
                Block.FURNACE,
                Block.BLAST_FURNACE,
                Block.SMOKER,
                Block.LECTERN,
                Block.LOOM,
                Block.STONECUTTER,
                Block.CHISELED_BOOKSHELF,
                Block.CARVED_PUMPKIN,
                Block.JACK_O_LANTERN,
                Block.BEEHIVE,
                Block.BEE_NEST,
                Block.BARREL,
                Block.REPEATER,
                Block.COMPARATOR,
                Block.CALIBRATED_SCULK_SENSOR,
                Block.DRIED_GHAST,
                Block.VAULT,
                Block.WHITE_GLAZED_TERRACOTTA,
                Block.LIGHT_GRAY_GLAZED_TERRACOTTA,
                Block.GRAY_GLAZED_TERRACOTTA,
                Block.BLACK_GLAZED_TERRACOTTA,
                Block.BROWN_GLAZED_TERRACOTTA,
                Block.RED_GLAZED_TERRACOTTA,
                Block.ORANGE_GLAZED_TERRACOTTA,
                Block.YELLOW_GLAZED_TERRACOTTA,
                Block.LIME_GLAZED_TERRACOTTA,
                Block.GREEN_GLAZED_TERRACOTTA,
                Block.CYAN_GLAZED_TERRACOTTA,
                Block.LIGHT_BLUE_GLAZED_TERRACOTTA,
                Block.BLUE_GLAZED_TERRACOTTA,
                Block.PURPLE_GLAZED_TERRACOTTA,
                Block.MAGENTA_GLAZED_TERRACOTTA,
                Block.PINK_GLAZED_TERRACOTTA);

        Utility.registerPlacementRules(
                DirectionalPlacementRule::new,
                Block.PISTON,
                Block.STICKY_PISTON,
                Block.OBSERVER,
                Block.DISPENSER,
                Block.DROPPER,
                Block.COMMAND_BLOCK,
                Block.CHAIN_COMMAND_BLOCK,
                Block.REPEATING_COMMAND_BLOCK);

        Utility.registerPlacementRules(
                ChestPlacementRule::new,
                Block.CHEST,
                Block.TRAPPED_CHEST,
                Block.COPPER_CHEST,
                Block.EXPOSED_COPPER_CHEST,
                Block.WEATHERED_COPPER_CHEST,
                Block.OXIDIZED_COPPER_CHEST,
                Block.WAXED_COPPER_CHEST,
                Block.WAXED_EXPOSED_COPPER_CHEST,
                Block.WAXED_WEATHERED_COPPER_CHEST,
                Block.WAXED_OXIDIZED_COPPER_CHEST);

        Utility.registerPlacementRules(
                CrossConnectingPlacementRule::new,
                Block.IRON_BARS,
                Block.COPPER_BARS,
                Block.EXPOSED_COPPER_BARS,
                Block.WEATHERED_COPPER_BARS,
                Block.OXIDIZED_COPPER_BARS,
                Block.WAXED_COPPER_BARS,
                Block.WAXED_EXPOSED_COPPER_BARS,
                Block.WAXED_WEATHERED_COPPER_BARS,
                Block.WAXED_OXIDIZED_COPPER_BARS,
                Block.GLASS_PANE,
                Block.WHITE_STAINED_GLASS_PANE,
                Block.LIGHT_GRAY_STAINED_GLASS_PANE,
                Block.GRAY_STAINED_GLASS_PANE,
                Block.BLACK_STAINED_GLASS_PANE,
                Block.BROWN_STAINED_GLASS_PANE,
                Block.RED_STAINED_GLASS_PANE,
                Block.ORANGE_STAINED_GLASS_PANE,
                Block.YELLOW_STAINED_GLASS_PANE,
                Block.LIME_STAINED_GLASS_PANE,
                Block.GREEN_STAINED_GLASS_PANE,
                Block.CYAN_STAINED_GLASS_PANE,
                Block.LIGHT_BLUE_STAINED_GLASS_PANE,
                Block.BLUE_STAINED_GLASS_PANE,
                Block.PURPLE_STAINED_GLASS_PANE,
                Block.MAGENTA_STAINED_GLASS_PANE,
                Block.PINK_STAINED_GLASS_PANE);

        Utility.registerPlacementRules(block -> new FaceAttachedPlacementRule(block, LeverBlockHandler.INSTANCE), Block.LEVER);
        blockManager.registerBlockPlacementRule(new HandlerAttachingPlacementRule(Block.CAKE, CakeBlockHandler.INSTANCE));
        blockManager.registerBlockPlacementRule(new HandlerAttachingPlacementRule(Block.JUKEBOX, JukeboxBlockHandler.INSTANCE));
        Utility.registerPlacementRules(CactusPlacementRule::new, Block.CACTUS);
        Utility.registerPlacementRules(CactusFlowerPlacementRule::new, Block.CACTUS_FLOWER);
        Utility.registerPlacementRules(SugarCanePlacementRule::new, Block.SUGAR_CANE);
        Utility.registerPlacementRules(MushroomPlacementRule::new, Block.RED_MUSHROOM, Block.BROWN_MUSHROOM);

        Utility.registerPlacementRules(SkullPlacementRule::new,
                Block.SKELETON_SKULL,
                Block.WITHER_SKELETON_SKULL,
                Block.ZOMBIE_HEAD,
                Block.PLAYER_HEAD,
                Block.CREEPER_HEAD,
                Block.DRAGON_HEAD,
                Block.PIGLIN_HEAD);
        Utility.registerPlacementRules(WallSkullPlacementRule::new,
                Block.SKELETON_WALL_SKULL,
                Block.WITHER_SKELETON_WALL_SKULL,
                Block.ZOMBIE_WALL_HEAD,
                Block.PLAYER_WALL_HEAD,
                Block.CREEPER_WALL_HEAD,
                Block.DRAGON_WALL_HEAD,
                Block.PIGLIN_WALL_HEAD);
        Utility.registerPlacementRules(WallTorchPlacementRule::new,
                Block.WALL_TORCH,
                Block.SOUL_WALL_TORCH,
                Block.REDSTONE_WALL_TORCH,
                Block.COPPER_WALL_TORCH);
        Utility.registerPlacementRules(CocoaPlacementRule::new, Block.COCOA);
        Utility.registerPlacementRules(LanternPlacementRule::new,
                Block.LANTERN,
                Block.SOUL_LANTERN,
                Block.COPPER_LANTERN,
                Block.EXPOSED_COPPER_LANTERN,
                Block.WEATHERED_COPPER_LANTERN,
                Block.OXIDIZED_COPPER_LANTERN,
                Block.WAXED_COPPER_LANTERN,
                Block.WAXED_EXPOSED_COPPER_LANTERN,
                Block.WAXED_WEATHERED_COPPER_LANTERN,
                Block.WAXED_OXIDIZED_COPPER_LANTERN);
        Utility.registerPlacementRules(CandlePlacementRule::new,
                Block.CANDLE,
                Block.WHITE_CANDLE,
                Block.LIGHT_GRAY_CANDLE,
                Block.GRAY_CANDLE,
                Block.BLACK_CANDLE,
                Block.BROWN_CANDLE,
                Block.RED_CANDLE,
                Block.ORANGE_CANDLE,
                Block.YELLOW_CANDLE,
                Block.LIME_CANDLE,
                Block.GREEN_CANDLE,
                Block.CYAN_CANDLE,
                Block.LIGHT_BLUE_CANDLE,
                Block.BLUE_CANDLE,
                Block.PURPLE_CANDLE,
                Block.MAGENTA_CANDLE,
                Block.PINK_CANDLE);
        Utility.registerPlacementRules(SnowLayerPlacementRule::new, Block.SNOW);
        Utility.registerPlacementRules(SeaPicklePlacementRule::new, Block.SEA_PICKLE);
        Utility.registerPlacementRules(TurtleEggPlacementRule::new, Block.TURTLE_EGG);
        Utility.registerPlacementRules(AnvilPlacementRule::new,
                Block.ANVIL,
                Block.CHIPPED_ANVIL,
                Block.DAMAGED_ANVIL);
        Utility.registerPlacementRules(HopperPlacementRule::new, Block.HOPPER);
        Utility.registerPlacementRules(BellPlacementRule::new, Block.BELL);
        Utility.registerPlacementRules(MultifacePlacementRule::new,
                Block.VINE,
                Block.GLOW_LICHEN,
                Block.SCULK_VEIN,
                Block.RESIN_CLUMP);
        Utility.registerPlacementRules(SegmentedPlacementRule::new,
                Block.PINK_PETALS,
                Block.LEAF_LITTER,
                Block.WILDFLOWERS);
        Utility.registerPlacementRules(ScaffoldingPlacementRule::new, Block.SCAFFOLDING);

        Utility.registerPlacementRules(LightningRodPlacementRule::new,
                Block.LIGHTNING_ROD,
                Block.EXPOSED_LIGHTNING_ROD,
                Block.WEATHERED_LIGHTNING_ROD,
                Block.OXIDIZED_LIGHTNING_ROD,
                Block.WAXED_LIGHTNING_ROD,
                Block.WAXED_EXPOSED_LIGHTNING_ROD,
                Block.WAXED_WEATHERED_LIGHTNING_ROD,
                Block.WAXED_OXIDIZED_LIGHTNING_ROD);
        Utility.registerPlacementRules(AmethystClusterPlacementRule::new,
                Block.SMALL_AMETHYST_BUD,
                Block.MEDIUM_AMETHYST_BUD,
                Block.LARGE_AMETHYST_BUD,
                Block.AMETHYST_CLUSTER);
        Utility.registerPlacementRules(EndRodPlacementRule::new, Block.END_ROD);
        Utility.registerPlacementRules(TripWireHookPlacementRule::new, Block.TRIPWIRE_HOOK);
        Utility.registerPlacementRules(BigDripleafPlacementRule::new, Block.BIG_DRIPLEAF);
        Utility.registerPlacementRules(SmallDripleafPlacementRule::new, Block.SMALL_DRIPLEAF);
        Utility.registerPlacementRules(PitcherCropPlacementRule::new, Block.PITCHER_CROP);

        Utility.registerPlacementRules(JigsawPlacementRule::new, Block.JIGSAW);
        Utility.registerPlacementRules(CrafterPlacementRule::new, Block.CRAFTER);
        Utility.registerPlacementRules(DecoratedPotPlacementRule::new, Block.DECORATED_POT);
        Utility.registerPlacementRules(EnderChestPlacementRule::new, Block.ENDER_CHEST);
        Utility.registerPlacementRules(BambooStalkPlacementRule::new, Block.BAMBOO);
        Utility.registerPlacementRules(GrowingPlantHeadPlacementRule::new,
                Block.KELP,
                Block.WEEPING_VINES,
                Block.TWISTING_VINES,
                Block.CAVE_VINES);
        Utility.registerPlacementRules(SeagrassPlacementRule::new, Block.SEAGRASS);
        Utility.registerPlacementRules(MangrovePropagulePlacementRule::new, Block.MANGROVE_PROPAGULE);
        Utility.registerPlacementRules(HangingRootsPlacementRule::new, Block.HANGING_ROOTS);
        Utility.registerPlacementRules(
                WaterloggedAxisPlacementRule::new,
                Block.IRON_CHAIN,
                Block.COPPER_CHAIN,
                Block.EXPOSED_COPPER_CHAIN,
                Block.WEATHERED_COPPER_CHAIN,
                Block.OXIDIZED_COPPER_CHAIN,
                Block.WAXED_COPPER_CHAIN,
                Block.WAXED_EXPOSED_COPPER_CHAIN,
                Block.WAXED_WEATHERED_COPPER_CHAIN,
                Block.WAXED_OXIDIZED_COPPER_CHAIN);
        Utility.registerPlacementRules(WaterloggedDummyPlacementRule::new,
                Block.MANGROVE_ROOTS,
                Block.HEAVY_CORE);
        Utility.registerPlacementRules(HugeMushroomPlacementRule::new,
                Block.RED_MUSHROOM_BLOCK,
                Block.BROWN_MUSHROOM_BLOCK,
                Block.MUSHROOM_STEM);
        Utility.registerPlacementRules(CampfirePlacementRule::new, Block.CAMPFIRE, Block.SOUL_CAMPFIRE);

        Utility.registerPlacementRules(ChorusPlantPlacementRule::new, Block.CHORUS_PLANT);
        Utility.registerPlacementRules(MossyCarpetPlacementRule::new, Block.PALE_MOSS_CARPET);
        Utility.registerPlacementRules(PointedDripstonePlacementRule::new, Block.POINTED_DRIPSTONE);
        Utility.registerPlacementRules(
                CopperGolemStatuePlacementRule::new,
                Block.COPPER_GOLEM_STATUE,
                Block.EXPOSED_COPPER_GOLEM_STATUE,
                Block.WEATHERED_COPPER_GOLEM_STATUE,
                Block.OXIDIZED_COPPER_GOLEM_STATUE,
                Block.WAXED_COPPER_GOLEM_STATUE,
                Block.WAXED_EXPOSED_COPPER_GOLEM_STATUE,
                Block.WAXED_WEATHERED_COPPER_GOLEM_STATUE,
                Block.WAXED_OXIDIZED_COPPER_GOLEM_STATUE);
        Utility.registerPlacementRules(
                WaterloggedDummyPlacementRule::new,
                Block.COPPER_GRATE,
                Block.EXPOSED_COPPER_GRATE,
                Block.WEATHERED_COPPER_GRATE,
                Block.OXIDIZED_COPPER_GRATE,
                Block.WAXED_COPPER_GRATE,
                Block.WAXED_EXPOSED_COPPER_GRATE,
                Block.WAXED_WEATHERED_COPPER_GRATE,
                Block.WAXED_OXIDIZED_COPPER_GRATE);
        Utility.registerPlacementRules(TallPlantPlacementRule::new, Block.PITCHER_PLANT);
        Utility.registerPlacementRules(TripWirePlacementRule::new, Block.TRIPWIRE);
        Utility.registerPlacementRules(RedstoneWirePlacementRule::new, Block.REDSTONE_WIRE);
        Utility.registerPlacementRules(FirePlacementRule::new, Block.FIRE);
        registerConcretePowders(blockManager);

        Utility.registerPlacementRules(block -> new HorizontalFacingPlacementRule(block, true), Block.END_PORTAL_FRAME);
        Utility.registerPlacementRules(WallMountedPlacementRule::new, Block.LADDER);
        Utility.registerPlacementRules(TallPlantPlacementRule::new,
                Block.TALL_SEAGRASS,
                Block.TALL_GRASS,
                Block.LARGE_FERN);
        Utility.registerPlacementRules(ShelfPlacementRule::new,
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
    }

    private static void registerHandlers(@NotNull BlockManager blockManager) {
        blockManager.registerHandler(DoorBlockHandler.INSTANCE.getKey(), () -> DoorBlockHandler.INSTANCE);
        blockManager.registerHandler(TrapdoorBlockHandler.INSTANCE.getKey(), () -> TrapdoorBlockHandler.INSTANCE);
        blockManager.registerHandler(FenceGateBlockHandler.INSTANCE.getKey(), () -> FenceGateBlockHandler.INSTANCE);
        blockManager.registerHandler(LeverBlockHandler.INSTANCE.getKey(), () -> LeverBlockHandler.INSTANCE);
        blockManager.registerHandler(ButtonBlockHandler.INSTANCE.getKey(), () -> ButtonBlockHandler.INSTANCE);
        blockManager.registerHandler(CandleBlockHandler.INSTANCE.getKey(), () -> CandleBlockHandler.INSTANCE);
        blockManager.registerHandler(CakeBlockHandler.INSTANCE.getKey(), () -> CakeBlockHandler.INSTANCE);
        blockManager.registerHandler(JukeboxBlockHandler.INSTANCE.getKey(), () -> JukeboxBlockHandler.INSTANCE);
        blockManager.registerHandler(BedBlockHandler.INSTANCE.getKey(), () -> BedBlockHandler.INSTANCE);
    }

    private static void registerByTag(@NotNull java.util.function.Function<Block, ? extends net.minestom.server.instance.block.rule.BlockPlacementRule> factory, @NotNull String tagKey) {
        Utility.registerPlacementRules(factory::apply, Key.key(tagKey));
    }

    private static void registerConcretePowders(@NotNull BlockManager blockManager) {
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

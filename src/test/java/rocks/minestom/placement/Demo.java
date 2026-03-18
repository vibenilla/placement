import net.kyori.adventure.key.Key;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import rocks.minestom.placement.*;

private static InstanceContainer createInstance() {
    var instance = MinecraftServer.getInstanceManager().createInstanceContainer();
    instance.setGenerator(unit -> unit.modifier().fillHeight(-64, 0, Block.STONE));
    instance.setTime(6000);
    instance.setTimeRate(0);
    return instance;
}

private static void registerPlacementRules() {
    Utility.registerPlacementRules(
            AxisPlacementRule::new,
            Block.CREAKING_HEART,
            Block.HAY_BLOCK,
            Block.IRON_CHAIN,
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

    Utility.registerPlacementRules(AxisPlacementRule::new, Key.key("minecraft:logs"));
    Utility.registerPlacementRules(StairPlacementRule::new, StairPlacementRule.KEY);
    Utility.registerPlacementRules(SlabPlacementRule::new, SlabPlacementRule.KEY);
    Utility.registerPlacementRules(FencePlacementRule::new, FencePlacementRule.KEY);
    Utility.registerPlacementRules(FenceGatePlacementRule::new, FenceGatePlacementRule.KEY);
    Utility.registerPlacementRules(WallPlacementRule::new, WallPlacementRule.KEY);
    Utility.registerPlacementRules(GlassPanePlacementRule::new,
            Block.GLASS_PANE,
            Block.IRON_BARS,
            Block.WHITE_STAINED_GLASS_PANE,
            Block.ORANGE_STAINED_GLASS_PANE,
            Block.MAGENTA_STAINED_GLASS_PANE,
            Block.LIGHT_BLUE_STAINED_GLASS_PANE,
            Block.YELLOW_STAINED_GLASS_PANE,
            Block.LIME_STAINED_GLASS_PANE,
            Block.PINK_STAINED_GLASS_PANE,
            Block.GRAY_STAINED_GLASS_PANE,
            Block.LIGHT_GRAY_STAINED_GLASS_PANE,
            Block.CYAN_STAINED_GLASS_PANE,
            Block.PURPLE_STAINED_GLASS_PANE,
            Block.BLUE_STAINED_GLASS_PANE,
            Block.BROWN_STAINED_GLASS_PANE,
            Block.GREEN_STAINED_GLASS_PANE,
            Block.RED_STAINED_GLASS_PANE,
            Block.BLACK_STAINED_GLASS_PANE);

    Utility.registerPlacementRules(DoorPlacementRule::new, DoorPlacementRule.KEY);
    Utility.registerPlacementRules(BedPlacementRule::new, BedPlacementRule.KEY);
    Utility.registerPlacementRules(ButtonPlacementRule::new, ButtonPlacementRule.KEY);
    Utility.registerPlacementRules(TrapdoorPlacementRule::new, TrapdoorPlacementRule.KEY);
    Utility.registerPlacementRules(StandingSignPlacementRule::new, StandingSignPlacementRule.KEY);
    Utility.registerPlacementRules(WallSignPlacementRule::new, WallSignPlacementRule.KEY);
    Utility.registerPlacementRules(CeilingHangingSignPlacementRule::new, CeilingHangingSignPlacementRule.KEY);
    Utility.registerPlacementRules(WallHangingSignPlacementRule::new, WallHangingSignPlacementRule.KEY);
    Utility.registerPlacementRules(BannerPlacementRule::new, BannerPlacementRule.KEY);
    Utility.registerPlacementRules(HorizontalFacingPlacementRule::new, Block.FURNACE, Block.BLAST_FURNACE, Block.SMOKER, Block.STONECUTTER);
    Utility.registerPlacementRules(ChestPlacementRule::new, Block.CHEST);
    Utility.registerPlacementRules(PlantPlacementRule::new, PlantPlacementRule.KEY);
    Utility.registerPlacementRules(PlantPlacementRule::new, Key.key("minecraft:saplings"));
    Utility.registerPlacementRules(CropPlacementRule::new, CropPlacementRule.KEY);
    Utility.registerPlacementRules(TallPlantPlacementRule::new,
            Block.SUNFLOWER,
            Block.LILAC,
            Block.PEONY,
            Block.ROSE_BUSH,
            Block.TALL_GRASS,
            Block.LARGE_FERN,
            Block.TALL_SEAGRASS,
            Block.PITCHER_PLANT);

    Utility.registerPlacementRules(MushroomPlacementRule::new, Block.BROWN_MUSHROOM, Block.RED_MUSHROOM);
    Utility.registerPlacementRules(SugarCanePlacementRule::new, Block.SUGAR_CANE);
    Utility.registerPlacementRules(CactusPlacementRule::new, Block.CACTUS);
    Utility.registerPlacementRules(CactusFlowerPlacementRule::new, Block.CACTUS_FLOWER);
    Utility.registerPlacementRules(RailPlacementRule::new, RailPlacementRule.KEY);
}

void main() {
    var server = MinecraftServer.init(new Auth.Online());
    var instance = createInstance();

    MinecraftServer.getGlobalEventHandler()
            .addListener(AsyncPlayerConfigurationEvent.class, event -> event.setSpawningInstance(instance))
            .addListener(PlayerSpawnEvent.class, event -> event.getPlayer().setGameMode(GameMode.CREATIVE));

    registerPlacementRules();
    server.start("0.0.0.0", 25565);
}

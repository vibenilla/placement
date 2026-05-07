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

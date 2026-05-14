import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import rocks.minestom.placement.Registrations;

import java.util.Objects;

private static InstanceContainer createInstance() {
    var instance = MinecraftServer.getInstanceManager().createInstanceContainer();
    instance.setGenerator(unit -> unit.modifier().fillHeight(-64, 0, Block.STONE));
    Objects.requireNonNull(instance.defaultClock()).rate(0.0F);
    instance.setTime(6000);
    return instance;
}

void main() {
    var server = MinecraftServer.init(new Auth.Online());
    var instance = createInstance();

    MinecraftServer.getGlobalEventHandler()
            .addListener(AsyncPlayerConfigurationEvent.class, event -> event.setSpawningInstance(instance))
            .addListener(PlayerSpawnEvent.class, event -> event.getPlayer().setGameMode(GameMode.CREATIVE));

    Registrations.registerAllVanilla(MinecraftServer.getBlockManager());
    server.start("0.0.0.0", 25565);
}

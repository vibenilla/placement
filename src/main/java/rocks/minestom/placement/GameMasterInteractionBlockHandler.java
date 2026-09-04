package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.block.BlockHandler;

public final class GameMasterInteractionBlockHandler implements BlockHandler {
    public static final GameMasterInteractionBlockHandler INSTANCE = new GameMasterInteractionBlockHandler();
    private static final Key KEY = Key.key("placement:game_master_interaction");

    private GameMasterInteractionBlockHandler() {

    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        var player = interaction.getPlayer();
        return player.getGameMode() != GameMode.CREATIVE || player.getPermissionLevel() < 2;
    }
}

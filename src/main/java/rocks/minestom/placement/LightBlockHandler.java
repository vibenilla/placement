package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.block.BlockHandler;

public final class LightBlockHandler implements BlockHandler {
    public static final LightBlockHandler INSTANCE = new LightBlockHandler();
    private static final Key KEY = Key.key("placement:light");

    private LightBlockHandler() {

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

        if (player.getGameMode() == GameMode.CREATIVE && player.getPermissionLevel() >= 2) {
            var block = interaction.getBlock();
            var level = Integer.parseInt(block.getProperty("level"));
            interaction.getInstance().setBlock(
                    interaction.getBlockPosition(), block.withProperty("level", String.valueOf((level + 1) % 16)));
        }

        return false;
    }
}

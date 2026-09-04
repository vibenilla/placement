package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;

public final class RedstoneWireBlockHandler implements BlockHandler {
    public static final RedstoneWireBlockHandler INSTANCE = new RedstoneWireBlockHandler();
    private static final Key KEY = Key.key("placement:redstone_wire");

    private RedstoneWireBlockHandler() {

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

        var gameMode = interaction.getPlayer().getGameMode();

        if (gameMode == GameMode.ADVENTURE || gameMode == GameMode.SPECTATOR) {
            return true;
        }

        var block = interaction.getBlock();

        if (!RedstoneWirePlacementRule.isCross(block) && !RedstoneWirePlacementRule.isDot(block)) {
            return true;
        }

        var position = interaction.getBlockPosition();
        var rule = new RedstoneWirePlacementRule(Block.REDSTONE_WIRE);
        var updated = rule.withConnections(
                block, interaction.getInstance(), position, RedstoneWirePlacementRule.isCross(block));

        if (updated.equals(block)) {
            return true;
        }

        interaction.getInstance().setBlock(position, updated.withHandler(INSTANCE));
        return false;
    }
}

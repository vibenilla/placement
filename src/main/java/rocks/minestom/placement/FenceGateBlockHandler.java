package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public final class FenceGateBlockHandler implements BlockHandler {
    public static final FenceGateBlockHandler INSTANCE = new FenceGateBlockHandler();
    private static final Key KEY = Key.key("placement:fence_gate");

    private FenceGateBlockHandler() {

    }

    @Override
    public @NotNull Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        var block = interaction.getBlock();
        var currentOpen = "true".equals(block.getProperty("open"));
        var updatedBlock = block;

        if (currentOpen) {
            updatedBlock = updatedBlock.withProperty("open", "false");
        } else {
            var facingName = block.getProperty("facing");
            var currentFacing = facingName == null ? BlockFace.NORTH : BlockFace.valueOf(facingName.toUpperCase());
            var playerYaw = interaction.getPlayer().getPosition().yaw();
            var playerFacing = BlockFace.fromYaw(playerYaw);

            if (currentFacing == playerFacing.getOppositeFace()) {
                updatedBlock = updatedBlock.withProperty("facing", playerFacing.name().toLowerCase());
            }

            updatedBlock = updatedBlock.withProperty("open", "true");
        }

        interaction.getInstance().setBlock(interaction.getBlockPosition(), updatedBlock);

        // TODO: vanilla plays a per-material fence gate sound; not implemented
        return false;
    }
}

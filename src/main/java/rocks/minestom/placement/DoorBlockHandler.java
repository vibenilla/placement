package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public final class DoorBlockHandler implements BlockHandler {
    public static final DoorBlockHandler INSTANCE = new DoorBlockHandler();
    private static final Key KEY = Key.key("placement:door");

    private DoorBlockHandler() {

    }

    @Override
    public @NotNull Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        if (Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        var block = interaction.getBlock();

        if (block.compare(Block.IRON_DOOR)) {
            return true;
        }

        var half = block.getProperty("half");

        if (half == null) {
            return true;
        }

        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();
        var currentOpen = "true".equals(block.getProperty("open"));
        var newOpen = String.valueOf(!currentOpen);
        var updatedBlock = block.withProperty("open", newOpen);

        instance.setBlock(blockPosition, updatedBlock);

        var partnerFace = "lower".equals(half) ? BlockFace.TOP : BlockFace.BOTTOM;
        var partnerPosition = blockPosition.relative(partnerFace);
        var partnerBlock = instance.getBlock(partnerPosition);

        if (partnerBlock.compare(block)) {
            instance.setBlock(partnerPosition, partnerBlock.withProperty("open", newOpen));
        }

        // TODO: vanilla plays a per-material door sound; not implemented
        return false;
    }
}

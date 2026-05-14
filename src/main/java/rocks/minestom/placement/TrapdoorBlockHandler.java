package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public final class TrapdoorBlockHandler implements BlockHandler {
    public static final TrapdoorBlockHandler INSTANCE = new TrapdoorBlockHandler();
    private static final Key KEY = Key.key("placement:trapdoor");

    private TrapdoorBlockHandler() {

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

        if (block.compare(Block.IRON_TRAPDOOR)) {
            return true;
        }

        var currentOpen = "true".equals(block.getProperty("open"));
        var newOpen = String.valueOf(!currentOpen);
        var updatedBlock = block.withProperty("open", newOpen);

        interaction.getInstance().setBlock(interaction.getBlockPosition(), updatedBlock);

        // TODO: vanilla plays a per-material trapdoor sound; not implemented
        return false;
    }
}

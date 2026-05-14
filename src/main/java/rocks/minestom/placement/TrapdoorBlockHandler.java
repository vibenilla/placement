package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
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
        var block = interaction.getBlock();
        var currentOpen = "true".equals(block.getProperty("open"));
        var newOpen = String.valueOf(!currentOpen);
        var updatedBlock = block.withProperty("open", newOpen);

        interaction.getInstance().setBlock(interaction.getBlockPosition(), updatedBlock);

        // TODO: vanilla plays a per-material trapdoor sound; not implemented
        // TODO: iron and copper trapdoors should not open by hand; requires Block.IRON_TRAPDOOR / Block.COPPER_TRAPDOOR / oxidized copper trapdoor enumeration to skip
        return false;
    }
}

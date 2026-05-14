package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public final class LeverBlockHandler implements BlockHandler {
    public static final LeverBlockHandler INSTANCE = new LeverBlockHandler();
    private static final Key KEY = Key.key("placement:lever");

    private LeverBlockHandler() {

    }

    @Override
    public @NotNull Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        var block = interaction.getBlock();
        var currentPowered = "true".equals(block.getProperty("powered"));
        var newPowered = String.valueOf(!currentPowered);
        var updatedBlock = block.withProperty("powered", newPowered);

        interaction.getInstance().setBlock(interaction.getBlockPosition(), updatedBlock);

        // TODO: vanilla plays a lever click sound at pitch 0.6F when powered, 0.5F when unpowered
        // TODO: vanilla emits a redstone signal on the connected face; redstone is not simulated here
        return false;
    }
}

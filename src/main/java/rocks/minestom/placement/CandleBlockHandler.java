package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public final class CandleBlockHandler implements BlockHandler {
    public static final CandleBlockHandler INSTANCE = new CandleBlockHandler();
    private static final Key KEY = Key.key("placement:candle");

    private CandleBlockHandler() {

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
        var lit = block.getProperty("lit");

        if ("true".equals(lit)) {
            var instance = interaction.getInstance();
            var blockPosition = interaction.getBlockPosition();

            instance.setBlock(blockPosition, block.withProperty("lit", "false"));

            // TODO: vanilla plays an extinguish sound and emits smoke particles
            return false;
        }

        // TODO: lighting an unlit candle requires flint and steel / fire charge detection on the held item
        return true;
    }
}

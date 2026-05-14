package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public final class RepeaterBlockHandler implements BlockHandler {
    public static final RepeaterBlockHandler INSTANCE = new RepeaterBlockHandler();
    private static final Key KEY = Key.key("placement:repeater");

    private RepeaterBlockHandler() {

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
        var delayProperty = block.getProperty("delay");
        var delay = delayProperty == null ? 1 : Integer.parseInt(delayProperty);
        var nextDelay = delay >= 4 ? 1 : delay + 1;
        var updatedBlock = block.withProperty("delay", Integer.toString(nextDelay));

        interaction.getInstance().setBlock(interaction.getBlockPosition(), updatedBlock);
        return false;
    }
}

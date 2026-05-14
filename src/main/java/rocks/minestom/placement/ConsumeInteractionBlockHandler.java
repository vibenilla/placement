package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public final class ConsumeInteractionBlockHandler implements BlockHandler {
    public static final ConsumeInteractionBlockHandler INSTANCE = new ConsumeInteractionBlockHandler();
    private static final Key KEY = Key.key("placement:consume_interaction");

    private ConsumeInteractionBlockHandler() {

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

        // TODO: vanilla opens the corresponding UI (container, workbench, etc.); we just consume the click to mirror vanilla's "no placement" behavior
        return false;
    }
}

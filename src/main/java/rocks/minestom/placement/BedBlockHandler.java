package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public final class BedBlockHandler implements BlockHandler {
    public static final BedBlockHandler INSTANCE = new BedBlockHandler();
    private static final Key KEY = Key.key("placement:bed");

    private BedBlockHandler() {

    }

    @Override
    public @NotNull Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        // TODO: vanilla puts the player to sleep at night, sets respawn, etc.
        return false;
    }
}

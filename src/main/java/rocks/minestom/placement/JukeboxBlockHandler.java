package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public final class JukeboxBlockHandler implements BlockHandler {
    public static final JukeboxBlockHandler INSTANCE = new JukeboxBlockHandler();
    private static final Key KEY = Key.key("placement:jukebox");

    private JukeboxBlockHandler() {

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
        var hasRecord = block.getProperty("has_record");

        if ("true".equals(hasRecord)) {
            var instance = interaction.getInstance();
            var blockPosition = interaction.getBlockPosition();

            instance.setBlock(blockPosition, block.withProperty("has_record", "false"));

            // TODO: vanilla pops the disc item out as an ItemEntity and stops the music
            return false;
        }

        // TODO: vanilla inserts the held music disc into the jukebox and starts playing it
        return true;
    }
}

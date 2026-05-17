package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;

public final class DaylightDetectorBlockHandler implements BlockHandler {
    public static final DaylightDetectorBlockHandler INSTANCE = new DaylightDetectorBlockHandler();
    private static final Key KEY = Key.key("placement:daylight_detector");

    private DaylightDetectorBlockHandler() {

    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        var block = interaction.getBlock();
        var currentInverted = "true".equals(block.getProperty("inverted"));
        var updatedBlock = block.withProperty("inverted", String.valueOf(!currentInverted));

        interaction.getInstance().setBlock(interaction.getBlockPosition(), updatedBlock);
        return false;
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public final class CakeBlockHandler implements BlockHandler {
    public static final CakeBlockHandler INSTANCE = new CakeBlockHandler();
    private static final Key KEY = Key.key("placement:cake");

    private CakeBlockHandler() {

    }

    @Override
    public @NotNull Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        var block = interaction.getBlock();
        var bitesProperty = block.getProperty("bites");
        var bites = bitesProperty == null ? 0 : Integer.parseInt(bitesProperty);
        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();

        if (bites >= 6) {
            instance.setBlock(blockPosition, Block.AIR);
        } else {
            instance.setBlock(blockPosition, block.withProperty("bites", String.valueOf(bites + 1)));
        }

        // TODO: vanilla feeds the player (2 hunger, 0.1 saturation), plays an eat sound, and emits an EAT game event
        return false;
    }
}

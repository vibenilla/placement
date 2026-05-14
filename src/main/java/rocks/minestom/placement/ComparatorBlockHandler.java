package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public final class ComparatorBlockHandler implements BlockHandler {
    public static final ComparatorBlockHandler INSTANCE = new ComparatorBlockHandler();
    private static final Key KEY = Key.key("placement:comparator");

    private ComparatorBlockHandler() {

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
        var mode = block.getProperty("mode");
        var newMode = "compare".equals(mode) ? "subtract" : "compare";
        var updatedBlock = block.withProperty("mode", newMode);
        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();

        instance.setBlock(blockPosition, updatedBlock);

        var pitch = "subtract".equals(newMode) ? 0.55F : 0.5F;
        var sound = Sound.sound(SoundEvent.BLOCK_COMPARATOR_CLICK, Sound.Source.BLOCK, 0.3F, pitch);
        instance.playSound(sound, blockPosition.add(0.5D, 0.5D, 0.5D));
        return false;
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

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

        if (!"true".equals(lit)) {
            // TODO: lighting an unlit candle requires flint and steel / fire charge detection on the held item
            return true;
        }

        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();
        instance.setBlock(blockPosition, block.withProperty("lit", "false"));

        var pitch = ThreadLocalRandom.current().nextFloat() * 0.4F + 0.8F;
        var sound = Sound.sound(SoundEvent.BLOCK_CANDLE_EXTINGUISH, Sound.Source.BLOCK, 1.0F, pitch);
        instance.playSound(sound, blockPosition.add(0.5D, 0.5D, 0.5D));
        return false;
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.sound.SoundEvent;

public final class LeverBlockHandler implements BlockHandler {
    public static final LeverBlockHandler INSTANCE = new LeverBlockHandler();
    private static final Key KEY = Key.key("placement:lever");

    private LeverBlockHandler() {

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
        var currentPowered = "true".equals(block.getProperty("powered"));
        var newPowered = !currentPowered;
        var updatedBlock = block.withProperty("powered", String.valueOf(newPowered));
        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();

        instance.setBlock(blockPosition, updatedBlock);

        var pitch = newPowered ? 0.6F : 0.5F;
        var sound = Sound.sound(SoundEvent.BLOCK_LEVER_CLICK, Sound.Source.BLOCK, 0.3F, pitch);
        instance.playSoundExcept(interaction.getPlayer(), sound, blockPosition.add(0.5D, 0.5D, 0.5D));

        // TODO: vanilla emits a redstone signal on the connected face; redstone is not simulated here
        return false;
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class DoorBlockHandler implements BlockHandler {
    public static final DoorBlockHandler INSTANCE = new DoorBlockHandler();
    private static final Key KEY = Key.key("placement:door");

    private DoorBlockHandler() {

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

        if (block.compare(Block.IRON_DOOR)) {
            return true;
        }

        var half = block.getProperty("half");

        if (half == null) {
            return true;
        }

        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();
        var currentOpen = "true".equals(block.getProperty("open"));
        var newOpen = String.valueOf(!currentOpen);
        var updatedBlock = block.withProperty("open", newOpen);

        instance.setBlock(blockPosition, updatedBlock);

        var partnerFace = "lower".equals(half) ? BlockFace.TOP : BlockFace.BOTTOM;
        var partnerPosition = blockPosition.relative(partnerFace);
        var partnerBlock = instance.getBlock(partnerPosition);

        if (partnerBlock.compare(block)) {
            instance.setBlock(partnerPosition, partnerBlock.withProperty("open", newOpen));
        }

        var pitch = ThreadLocalRandom.current().nextFloat() * 0.1F + 0.9F;
        var sound = Sound.sound(soundEvent(block, !currentOpen), Sound.Source.BLOCK, 1.0F, pitch);
        instance.playSound(sound, blockPosition.add(0.5D, 0.5D, 0.5D));
        return false;
    }

    private static SoundEvent soundEvent(@NotNull Block block, boolean opening) {

        if (block.compare(Block.IRON_DOOR)) {
            return opening ? SoundEvent.BLOCK_IRON_DOOR_OPEN : SoundEvent.BLOCK_IRON_DOOR_CLOSE;
        }

        if (Utility.hasTag(block, Key.key("minecraft:copper_doors"))) {
            return opening ? SoundEvent.BLOCK_COPPER_DOOR_OPEN : SoundEvent.BLOCK_COPPER_DOOR_CLOSE;
        }

        if (block.compare(Block.BAMBOO_DOOR)) {
            return opening ? SoundEvent.BLOCK_BAMBOO_WOOD_DOOR_OPEN : SoundEvent.BLOCK_BAMBOO_WOOD_DOOR_CLOSE;
        }

        if (block.compare(Block.CHERRY_DOOR)) {
            return opening ? SoundEvent.BLOCK_CHERRY_WOOD_DOOR_OPEN : SoundEvent.BLOCK_CHERRY_WOOD_DOOR_CLOSE;
        }

        if (block.compare(Block.CRIMSON_DOOR) || block.compare(Block.WARPED_DOOR)) {
            return opening ? SoundEvent.BLOCK_NETHER_WOOD_DOOR_OPEN : SoundEvent.BLOCK_NETHER_WOOD_DOOR_CLOSE;
        }
        return opening ? SoundEvent.BLOCK_WOODEN_DOOR_OPEN : SoundEvent.BLOCK_WOODEN_DOOR_CLOSE;
    }
}

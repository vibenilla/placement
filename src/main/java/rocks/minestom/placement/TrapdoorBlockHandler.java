package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class TrapdoorBlockHandler implements BlockHandler {
    public static final TrapdoorBlockHandler INSTANCE = new TrapdoorBlockHandler();
    private static final Key KEY = Key.key("placement:trapdoor");

    private TrapdoorBlockHandler() {

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

        if (block.compare(Block.IRON_TRAPDOOR)) {
            return true;
        }

        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();
        var currentOpen = "true".equals(block.getProperty("open"));
        var newOpen = String.valueOf(!currentOpen);
        var updatedBlock = block.withProperty("open", newOpen);

        instance.setBlock(blockPosition, updatedBlock);

        var pitch = ThreadLocalRandom.current().nextFloat() * 0.1F + 0.9F;
        var sound = Sound.sound(soundEvent(block, !currentOpen), Sound.Source.BLOCK, 1.0F, pitch);
        instance.playSound(sound, blockPosition.add(0.5D, 0.5D, 0.5D));
        return false;
    }

    private static SoundEvent soundEvent(@NotNull Block block, boolean opening) {

        if (block.compare(Block.IRON_TRAPDOOR)) {
            return opening ? SoundEvent.BLOCK_IRON_TRAPDOOR_OPEN : SoundEvent.BLOCK_IRON_TRAPDOOR_CLOSE;
        }

        if (Utility.hasTag(block, Key.key("minecraft:copper_trapdoors"))) {
            return opening ? SoundEvent.BLOCK_COPPER_TRAPDOOR_OPEN : SoundEvent.BLOCK_COPPER_TRAPDOOR_CLOSE;
        }

        if (block.compare(Block.BAMBOO_TRAPDOOR)) {
            return opening ? SoundEvent.BLOCK_BAMBOO_WOOD_TRAPDOOR_OPEN : SoundEvent.BLOCK_BAMBOO_WOOD_TRAPDOOR_CLOSE;
        }

        if (block.compare(Block.CHERRY_TRAPDOOR)) {
            return opening ? SoundEvent.BLOCK_CHERRY_WOOD_TRAPDOOR_OPEN : SoundEvent.BLOCK_CHERRY_WOOD_TRAPDOOR_CLOSE;
        }

        if (block.compare(Block.CRIMSON_TRAPDOOR) || block.compare(Block.WARPED_TRAPDOOR)) {
            return opening ? SoundEvent.BLOCK_NETHER_WOOD_TRAPDOOR_OPEN : SoundEvent.BLOCK_NETHER_WOOD_TRAPDOOR_CLOSE;
        }
        return opening ? SoundEvent.BLOCK_WOODEN_TRAPDOOR_OPEN : SoundEvent.BLOCK_WOODEN_TRAPDOOR_CLOSE;
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class FenceGateBlockHandler implements BlockHandler {
    public static final FenceGateBlockHandler INSTANCE = new FenceGateBlockHandler();
    private static final Key KEY = Key.key("placement:fence_gate");

    private FenceGateBlockHandler() {

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
        var currentOpen = "true".equals(block.getProperty("open"));
        var updatedBlock = block;

        if (currentOpen) {
            updatedBlock = updatedBlock.withProperty("open", "false");
        } else {
            var facingName = block.getProperty("facing");
            var currentFacing = facingName == null ? BlockFace.NORTH : BlockFace.valueOf(facingName.toUpperCase());
            var playerYaw = interaction.getPlayer().getPosition().yaw();
            var playerFacing = BlockFace.fromYaw(playerYaw);

            if (currentFacing == playerFacing.getOppositeFace()) {
                updatedBlock = updatedBlock.withProperty("facing", playerFacing.name().toLowerCase());
            }

            updatedBlock = updatedBlock.withProperty("open", "true");
        }

        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();
        instance.setBlock(blockPosition, updatedBlock);

        var pitch = ThreadLocalRandom.current().nextFloat() * 0.1F + 0.9F;
        var sound = Sound.sound(soundEvent(block, !currentOpen), Sound.Source.BLOCK, 1.0F, pitch);
        instance.playSound(sound, blockPosition.add(0.5D, 0.5D, 0.5D));
        return false;
    }

    private static SoundEvent soundEvent(@NotNull Block block, boolean opening) {

        if (block.compare(Block.BAMBOO_FENCE_GATE)) {
            return opening ? SoundEvent.BLOCK_BAMBOO_WOOD_FENCE_GATE_OPEN : SoundEvent.BLOCK_BAMBOO_WOOD_FENCE_GATE_CLOSE;
        }

        if (block.compare(Block.CHERRY_FENCE_GATE)) {
            return opening ? SoundEvent.BLOCK_CHERRY_WOOD_FENCE_GATE_OPEN : SoundEvent.BLOCK_CHERRY_WOOD_FENCE_GATE_CLOSE;
        }

        if (block.compare(Block.CRIMSON_FENCE_GATE) || block.compare(Block.WARPED_FENCE_GATE)) {
            return opening ? SoundEvent.BLOCK_NETHER_WOOD_FENCE_GATE_OPEN : SoundEvent.BLOCK_NETHER_WOOD_FENCE_GATE_CLOSE;
        }
        return opening ? SoundEvent.BLOCK_FENCE_GATE_OPEN : SoundEvent.BLOCK_FENCE_GATE_CLOSE;
    }
}

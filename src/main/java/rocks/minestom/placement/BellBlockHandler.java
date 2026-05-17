package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.sound.SoundEvent;

public final class BellBlockHandler implements BlockHandler {
    public static final BellBlockHandler INSTANCE = new BellBlockHandler();
    private static final Key KEY = Key.key("placement:bell");

    private BellBlockHandler() {

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

        var clickedFace = interaction.getBlockFace();

        if (clickedFace == BlockFace.TOP || clickedFace == BlockFace.BOTTOM) {
            return false;
        }

        var block = interaction.getBlock();
        var attachment = block.getProperty("attachment");
        var facingName = block.getProperty("facing");

        if (facingName == null) {
            return false;
        }

        var facing = BlockFace.valueOf(facingName.toUpperCase());

        if (!canRingFrom(attachment, facing, clickedFace)) {
            return false;
        }

        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();
        var sound = Sound.sound(SoundEvent.BLOCK_BELL_USE, Sound.Source.BLOCK, 2.0F, 1.0F);

        instance.playSound(sound, position.add(0.5D, 0.5D, 0.5D));
        return false;
    }

    private static boolean canRingFrom(String attachment, BlockFace facing, BlockFace clickedFace) {
        if ("ceiling".equals(attachment)) {
            return true;
        }

        return perpendicular(facing, clickedFace);
    }

    private static boolean perpendicular(BlockFace a, BlockFace b) {
        var aZ = a == BlockFace.NORTH || a == BlockFace.SOUTH;
        var bZ = b == BlockFace.NORTH || b == BlockFace.SOUTH;
        return aZ != bZ;
    }
}

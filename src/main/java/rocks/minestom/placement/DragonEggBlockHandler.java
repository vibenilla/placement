package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;

import java.util.concurrent.ThreadLocalRandom;

public final class DragonEggBlockHandler implements BlockHandler {
    public static final DragonEggBlockHandler INSTANCE = new DragonEggBlockHandler();
    private static final Key KEY = Key.key("placement:dragon_egg");

    private DragonEggBlockHandler() {

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

        var instance = interaction.getInstance();
        var origin = interaction.getBlockPosition();
        var random = ThreadLocalRandom.current();
        var dimension = instance.getCachedDimensionType();

        for (var attempt = 0; attempt < 1000; attempt++) {
            var candidate = new BlockVec(
                    origin.blockX() + random.nextInt(-15, 16),
                    origin.blockY() + random.nextInt(-7, 8),
                    origin.blockZ() + random.nextInt(-15, 16));

            if (candidate.blockY() < dimension.minY() || candidate.blockY() >= dimension.maxY()
                    || !instance.getWorldBorder().inBounds(candidate)
                    || !instance.getBlock(candidate).isAir()
                    || instance.getBlock(candidate.relative(BlockFace.BOTTOM)).isAir()) {
                continue;
            }

            instance.setBlock(candidate, interaction.getBlock().withHandler(INSTANCE));
            instance.setBlock(origin, Block.AIR);
            return false;
        }

        return false;
    }
}

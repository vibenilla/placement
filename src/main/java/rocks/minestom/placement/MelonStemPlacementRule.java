package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class MelonStemPlacementRule extends BlockPlacementRule {
    public MelonStemPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placement) {
        return block;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState update) {
        var instance = update.instance();
        var block = update.currentBlock();
        var fromBlock = instance.getBlock(update.blockPosition().relative(update.fromFace()));

        if (block.id() == Block.MELON_STEM.id()) {
            if (!isFullyGrown(block) || fromBlock.id() != Block.MELON.id()) return block;
            return Block.ATTACHED_MELON_STEM.withProperty("facing", update.fromFace().name().toLowerCase(Locale.ROOT));
        }

        var stemFacing = BlockFace.valueOf(block.getProperty("facing").toUpperCase(Locale.ROOT));
        if (update.fromFace() != stemFacing || fromBlock.id() == Block.MELON.id()) return block;

        return Block.MELON_STEM.withProperty("age", "7");
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private boolean isFullyGrown(@NotNull Block block) {
        return Integer.parseInt(block.getProperty("age")) == 7;
    }
}

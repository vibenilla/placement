package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import rocks.minestom.placement.utils.PlacementUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ScaffoldingPlacementRule extends BlockPlacementRule {
    public ScaffoldingPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placement) {
        Block block = PlacementUtils.waterlogged(placement);
        return getState(placement.instance(), placement.placePosition(), block);
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState update) {
        if (update.fromFace() != BlockFace.BOTTOM) return update.currentBlock();
        return getState(update.instance(), update.blockPosition(), update.currentBlock());
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private @NotNull Block getState(@NotNull Block.Getter instance, @NotNull Point blockPosition, @NotNull Block current) {
        var blockBelow = instance.getBlock(blockPosition.add(0, -1, 0));
        var isBottom = blockBelow.id() != Block.SCAFFOLDING.id() && !blockBelow.registry().collisionShape().isFaceFull(BlockFace.TOP);
        return current.withProperty("bottom", String.valueOf(isBottom));
    }
}

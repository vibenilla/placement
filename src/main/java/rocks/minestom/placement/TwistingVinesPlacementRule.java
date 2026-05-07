package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TwistingVinesPlacementRule extends BlockPlacementRule {
    public TwistingVinesPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placement) {
        var above = placement.instance().getBlock(placement.placePosition().add(0, 1, 0));
        if (above.id() == Block.TWISTING_VINES.id()) return Block.TWISTING_VINES_PLANT;
        return block;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var above = updateState.instance().getBlock(updateState.blockPosition().add(0, 1, 0));
        if (above.id() == Block.TWISTING_VINES.id()) return Block.TWISTING_VINES_PLANT;
        return updateState.currentBlock();
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }
}

package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

/**
 * Wraps another placement rule so that a {@code null} result from {@code blockPlace} (typically a
 * survival or support rejection) is replaced with the bare item block, and so that
 * {@code blockUpdate} never pops the block to air. Intended for builder modes where the player
 * wants placement to succeed regardless of the world constraints vanilla would enforce.
 */
public final class PermissivePlacementRule extends BlockPlacementRule {
    private final BlockPlacementRule wrapped;

    public PermissivePlacementRule(Block block, BlockPlacementRule wrapped) {
        super(block);
        this.wrapped = wrapped;
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var result = wrapped.blockPlace(placementState);
        return result == null ? placementState.block() : result;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var result = wrapped.blockUpdate(updateState);
        return result.isAir() ? updateState.currentBlock() : result;
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        return wrapped.isSelfReplaceable(replacement);
    }

    @Override
    public int maxUpdateDistance() {
        return wrapped.maxUpdateDistance();
    }
}

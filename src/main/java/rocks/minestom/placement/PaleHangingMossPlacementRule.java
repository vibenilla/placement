package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class PaleHangingMossPlacementRule extends BlockPlacementRule {
    public PaleHangingMossPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        return computeState(placementState.instance(), placementState.placePosition(), this.block);
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        return computeState(updateState.instance(), updateState.blockPosition(), updateState.currentBlock());
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private @NotNull Block computeState(@NotNull Block.Getter instance, @NotNull Point blockPosition, @NotNull Block block) {
        int x = blockPosition.blockX();
        int y = blockPosition.blockY();
        int z = blockPosition.blockZ();
        var below = instance.getBlock(x, y - 1, z);
        if (below.id() == this.block.id()) return block.withProperty("tip", "false");
        return block;
    }
}

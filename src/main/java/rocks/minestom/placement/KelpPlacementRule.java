package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class KelpPlacementRule extends BlockPlacementRule {
    public KelpPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        if (updateState.fromFace() != BlockFace.TOP) return currentBlock;

        var blockAbove = updateState.instance().getBlock(updateState.blockPosition().add(0, 1, 0));
        if (blockAbove.id() == Block.KELP.id() || blockAbove.id() == Block.KELP_PLANT.id()) {
            return Block.KELP_PLANT;
        }

        return currentBlock;
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockAbove = placementState.instance().getBlock(placementState.placePosition().add(0, 1, 0));
        if (blockAbove.id() == Block.KELP.id() || blockAbove.id() == Block.KELP_PLANT.id()) {
            return Block.KELP_PLANT;
        }

        var blockBelow = placementState.instance().getBlock(placementState.placePosition().add(0, -1, 0));
        if (blockBelow.id() == Block.KELP.id() || blockBelow.id() == Block.KELP_PLANT.id()) {
            return Block.KELP_PLANT;
        }

        return this.block;
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }
}

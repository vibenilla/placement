package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class CarpetPlacementRule extends BlockPlacementRule {
    public CarpetPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var below = placementState.instance().getBlock(placementState.placePosition().relative(BlockFace.BOTTOM));
        return below.isAir() ? null : this.block;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {

        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }
        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
        return below.isAir() ? Block.AIR : updateState.currentBlock();
    }
}

package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class TorchPlacementRule extends BlockPlacementRule {
    public TorchPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var below = placementState.instance().getBlock(placementState.placePosition().relative(BlockFace.BOTTOM));

        if (!Utility.canSupportCenter(below, BlockFace.TOP)) {
            return null;
        }

        return placementState.block();
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
        return Utility.canSupportCenter(below, BlockFace.TOP) ? updateState.currentBlock() : Block.AIR;
    }
}

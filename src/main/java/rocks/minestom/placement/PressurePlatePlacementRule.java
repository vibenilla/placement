package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class PressurePlatePlacementRule extends BlockPlacementRule {
    public PressurePlatePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var instance = placementState.instance();
        var below = instance.getBlock(placementState.placePosition().relative(BlockFace.BOTTOM));

        if (!canSurvive(below)) {
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
        return canSurvive(below) ? updateState.currentBlock() : Block.AIR;
    }

    private static boolean canSurvive(Block below) {
        return Utility.canSupportRigidBlock(below, BlockFace.TOP)
                || Utility.canSupportCenter(below, BlockFace.TOP);
    }
}

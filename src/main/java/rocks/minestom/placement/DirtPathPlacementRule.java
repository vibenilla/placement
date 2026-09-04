package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class DirtPathPlacementRule extends BlockPlacementRule {
    private static final Key FENCE_GATES_TAG = Key.key("minecraft:fence_gates");

    public DirtPathPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var above = placementState.instance().getBlock(placementState.placePosition().relative(BlockFace.TOP));
        return canSurvive(above) ? placementState.block() : Block.DIRT;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.TOP) {
            return updateState.currentBlock();
        }

        var above = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.TOP));
        return canSurvive(above) ? updateState.currentBlock() : Block.DIRT;
    }

    private static boolean canSurvive(Block above) {
        return !above.isSolid() || Utility.hasTag(above, FENCE_GATES_TAG);
    }
}

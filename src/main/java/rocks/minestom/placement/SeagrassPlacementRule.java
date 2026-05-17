package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class SeagrassPlacementRule extends BlockPlacementRule {
    public SeagrassPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var replacedBlock = instance.getBlock(placePosition);

        if (!replacedBlock.compare(Block.WATER)) {
            return null;
        }

        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!belowBlock.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return null;
        }

        return placementState.block();
    }
}

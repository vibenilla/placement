package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class SeaPicklePlacementRule extends BlockPlacementRule {
    public SeaPicklePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var existingBlock = instance.getBlock(placePosition);

        if (existingBlock.compare(placementState.block())) {
            var picklesProperty = existingBlock.getProperty("pickles");
            var pickles = picklesProperty == null ? 1 : Integer.parseInt(picklesProperty);

            if (pickles < 4) {
                return existingBlock.withProperty("pickles", Integer.toString(pickles + 1));
            }
        }

        var supportPosition = placePosition.relative(BlockFace.BOTTOM);
        var supportBlock = instance.getBlock(supportPosition);

        if (!Utility.canSupportCenter(supportBlock, BlockFace.TOP)) {
            return null;
        }

        var waterlogged = existingBlock.compare(Block.WATER);

        return placementState.block()
                .withProperty("pickles", "1")
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));

        if (!Utility.canSupportCenter(below, BlockFace.TOP)) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        if (!replacement.block().compare(this.block)) {
            return false;
        }

        if (replacement.material() != this.block.registry().material()) {
            return false;
        }

        var picklesProperty = replacement.block().getProperty("pickles");
        var pickles = picklesProperty == null ? 1 : Integer.parseInt(picklesProperty);
        return pickles < 4;
    }
}

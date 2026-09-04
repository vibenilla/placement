package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class StandingSignPlacementRule extends BlockPlacementRule {
    public StandingSignPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!Utility.canSupportCenter(belowBlock, BlockFace.TOP)) {
            return null;
        }

        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var rotation = Math.round((yaw + 180.0F) * 16.0F / 360.0F) & 15;
        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER);

        return placementState.block()
                .withHandler(SignBlockHandler.INSTANCE)
                .withProperty("rotation", Integer.toString(rotation))
                .withProperty("waterlogged", String.valueOf(waterlogged));
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
}

package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class LilyPadPlacementRule extends BlockPlacementRule {
    public LilyPadPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var below = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!supports(below)) {
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
        return supports(below) ? updateState.currentBlock() : Block.AIR;
    }

    private static boolean supports(Block below) {
        return below.compare(Block.ICE)
                || below.compare(Block.WATER) && "0".equals(below.getProperty("level"));
    }
}

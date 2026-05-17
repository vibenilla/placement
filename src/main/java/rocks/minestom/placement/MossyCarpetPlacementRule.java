package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class MossyCarpetPlacementRule extends BlockPlacementRule {
    public MossyCarpetPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var hasBase = isSturdyAbove(blockGetter, placePosition.relative(BlockFace.BOTTOM));

        return placementState.block()
                .withProperty("base", String.valueOf(hasBase))
                .withProperty("north", side(blockGetter, placePosition, BlockFace.NORTH))
                .withProperty("east", side(blockGetter, placePosition, BlockFace.EAST))
                .withProperty("south", side(blockGetter, placePosition, BlockFace.SOUTH))
                .withProperty("west", side(blockGetter, placePosition, BlockFace.WEST));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));

        if (!below.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    private static String side(Block.Getter blockGetter, Point placePosition, BlockFace face) {
        var neighbor = blockGetter.getBlock(placePosition.relative(face));

        if (neighbor.registry().collisionShape().isFaceFull(face.getOppositeFace())) {
            return "low";
        }

        return "none";
    }

    private static boolean isSturdyAbove(Block.Getter blockGetter, Point belowPosition) {
        var below = blockGetter.getBlock(belowPosition);
        return below.registry().collisionShape().isFaceFull(BlockFace.TOP);
    }
}

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
        var north = side(blockGetter, placePosition, BlockFace.NORTH);
        var east = side(blockGetter, placePosition, BlockFace.EAST);
        var south = side(blockGetter, placePosition, BlockFace.SOUTH);
        var west = side(blockGetter, placePosition, BlockFace.WEST);

        if (!hasBase && "none".equals(north) && "none".equals(east)
                && "none".equals(south) && "none".equals(west)) {
            return null;
        }

        return placementState.block()
                .withProperty("base", String.valueOf(hasBase))
                .withProperty("north", north)
                .withProperty("east", east)
                .withProperty("south", south)
                .withProperty("west", west);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.BOTTOM && !isHorizontal(updateState.fromFace())) {
            return updateState.currentBlock();
        }

        var blockGetter = updateState.instance();
        var blockPosition = updateState.blockPosition();
        var hasBase = isSturdyAbove(blockGetter, blockPosition.relative(BlockFace.BOTTOM));
        var north = side(blockGetter, blockPosition, BlockFace.NORTH);
        var east = side(blockGetter, blockPosition, BlockFace.EAST);
        var south = side(blockGetter, blockPosition, BlockFace.SOUTH);
        var west = side(blockGetter, blockPosition, BlockFace.WEST);

        if (!hasBase && "none".equals(north) && "none".equals(east)
                && "none".equals(south) && "none".equals(west)) {
            return Block.AIR;
        }

        return updateState.currentBlock()
                .withProperty("base", String.valueOf(hasBase))
                .withProperty("north", north)
                .withProperty("east", east)
                .withProperty("south", south)
                .withProperty("west", west);
    }

    private static String side(Block.Getter blockGetter, Point placePosition, BlockFace face) {
        var neighbor = blockGetter.getBlock(placePosition.relative(face));

        if (Utility.canSupportCenter(neighbor, face.getOppositeFace())) {
            return "low";
        }

        return "none";
    }

    private static boolean isSturdyAbove(Block.Getter blockGetter, Point belowPosition) {
        var below = blockGetter.getBlock(belowPosition);
        return Utility.canSupportCenter(below, BlockFace.TOP);
    }

    private static boolean isHorizontal(BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH
                || face == BlockFace.EAST || face == BlockFace.WEST;
    }
}

package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class RedstoneWirePlacementRule extends BlockPlacementRule {
    public RedstoneWirePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var below = blockGetter.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!Utility.canSupportCenter(below, BlockFace.TOP)) {
            return null;
        }

        var north = computeSide(blockGetter, placePosition, BlockFace.NORTH);
        var east = computeSide(blockGetter, placePosition, BlockFace.EAST);
        var south = computeSide(blockGetter, placePosition, BlockFace.SOUTH);
        var west = computeSide(blockGetter, placePosition, BlockFace.WEST);
        var northConnected = !"none".equals(north);
        var southConnected = !"none".equals(south);
        var eastConnected = !"none".equals(east);
        var westConnected = !"none".equals(west);
        var northSouthEmpty = !northConnected && !southConnected;
        var eastWestEmpty = !eastConnected && !westConnected;
        var resolvedNorth = !northConnected && eastWestEmpty ? "side" : north;
        var resolvedSouth = !southConnected && eastWestEmpty ? "side" : south;
        var resolvedEast = !eastConnected && northSouthEmpty ? "side" : east;
        var resolvedWest = !westConnected && northSouthEmpty ? "side" : west;

        return placementState.block()
                .withProperty("north", resolvedNorth)
                .withProperty("east", resolvedEast)
                .withProperty("south", resolvedSouth)
                .withProperty("west", resolvedWest)
                .withProperty("power", "0");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
        return Utility.canSupportCenter(below, BlockFace.TOP) ? updateState.currentBlock() : Block.AIR;
    }

    private String computeSide(Block.Getter blockGetter, Point placePosition, BlockFace face) {
        var sidePosition = placePosition.relative(face);
        var sideBlock = blockGetter.getBlock(sidePosition);

        if (sideBlock.compare(this.block)) {
            return "side";
        }

        var abovePosition = placePosition.relative(BlockFace.TOP);
        var aboveBlock = blockGetter.getBlock(abovePosition);
        var aboveSidePosition = sidePosition.relative(BlockFace.TOP);
        var aboveSideBlock = blockGetter.getBlock(aboveSidePosition);
        var sideSturdyTop = sideBlock.registry().collisionShape().isFaceFull(BlockFace.TOP);
        var aboveSolid = aboveBlock.registry().collisionShape().isFaceFull(BlockFace.BOTTOM);

        if (sideSturdyTop && !aboveSolid && aboveSideBlock.compare(this.block)) {
            return "up";
        }

        var belowSidePosition = sidePosition.relative(BlockFace.BOTTOM);
        var belowSideBlock = blockGetter.getBlock(belowSidePosition);

        if (!sideSturdyTop && belowSideBlock.compare(this.block)) {
            return "side";
        }

        return "none";
    }
}

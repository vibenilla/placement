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

        return this.withConnections(placementState.block(), blockGetter, placePosition, false)
                .withHandler(RedstoneWireBlockHandler.INSTANCE)
                .withProperty("power", "0");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
        if (!Utility.canSupportCenter(below, BlockFace.TOP)) {
            return Block.AIR;
        }

        var current = updateState.currentBlock();
        return this.withConnections(current, updateState.instance(), updateState.blockPosition(), isDot(current));
    }

    Block withConnections(Block base, Block.Getter blockGetter, Point placePosition, boolean preserveDot) {
        var north = this.computeSide(blockGetter, placePosition, BlockFace.NORTH);
        var east = this.computeSide(blockGetter, placePosition, BlockFace.EAST);
        var south = this.computeSide(blockGetter, placePosition, BlockFace.SOUTH);
        var west = this.computeSide(blockGetter, placePosition, BlockFace.WEST);
        var northConnected = !"none".equals(north);
        var southConnected = !"none".equals(south);
        var eastConnected = !"none".equals(east);
        var westConnected = !"none".equals(west);

        if (preserveDot && !northConnected && !southConnected && !eastConnected && !westConnected) {
            return base
                    .withProperty("north", "none")
                    .withProperty("east", "none")
                    .withProperty("south", "none")
                    .withProperty("west", "none");
        }

        var northSouthEmpty = !northConnected && !southConnected;
        var eastWestEmpty = !eastConnected && !westConnected;
        var resolvedNorth = !northConnected && eastWestEmpty ? "side" : north;
        var resolvedSouth = !southConnected && eastWestEmpty ? "side" : south;
        var resolvedEast = !eastConnected && northSouthEmpty ? "side" : east;
        var resolvedWest = !westConnected && northSouthEmpty ? "side" : west;

        return base
                .withProperty("north", resolvedNorth)
                .withProperty("east", resolvedEast)
                .withProperty("south", resolvedSouth)
                .withProperty("west", resolvedWest);
    }

    static boolean isCross(Block block) {
        return connected(block, "north")
                && connected(block, "east")
                && connected(block, "south")
                && connected(block, "west");
    }

    static boolean isDot(Block block) {
        return !connected(block, "north")
                && !connected(block, "east")
                && !connected(block, "south")
                && !connected(block, "west");
    }

    private static boolean connected(Block block, String property) {
        return !"none".equals(block.getProperty(property));
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

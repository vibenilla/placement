package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RailPlacementRule extends BlockPlacementRule {
    public RailPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!(placementState.instance() instanceof Instance instance)) {
            return null;
        }

        var placePosition = placementState.placePosition();
        var below = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!Utility.canSupportRigidBlock(below, BlockFace.TOP)) {
            return null;
        }

        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var horizontal = BlockFace.fromYaw(yaw);
        var defaultShape = (horizontal == BlockFace.EAST || horizontal == BlockFace.WEST) ? "east_west" : "north_south";
        var initialBlock = withShape(placementState.block(), defaultShape, waterlogged);
        return placeRail(instance, placePosition, initialBlock, defaultShape);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var instance = updateState.instance();
        var blockPosition = updateState.blockPosition();
        var currentBlock = updateState.currentBlock();
        var below = instance.getBlock(blockPosition.relative(BlockFace.BOTTOM));

        if (!Utility.canSupportRigidBlock(below, BlockFace.TOP)) {
            return Block.AIR;
        }

        var north = blockPosition.relative(BlockFace.NORTH);
        var south = blockPosition.relative(BlockFace.SOUTH);
        var west = blockPosition.relative(BlockFace.WEST);
        var east = blockPosition.relative(BlockFace.EAST);
        var n = hasNeighborRail(instance, north, blockPosition);
        var s = hasNeighborRail(instance, south, blockPosition);
        var w = hasNeighborRail(instance, west, blockPosition);
        var e = hasNeighborRail(instance, east, blockPosition);
        var defaultShape = defaultShape(currentBlock.getProperty("shape"));
        var shape = computeShape(instance, north, south, west, east, n, s, w, e,
                isStraight(currentBlock), defaultShape, null);
        var ascendingFace = ascendingFace(shape);

        if (ascendingFace != null) {
            var ascendingSupport = instance.getBlock(blockPosition.relative(ascendingFace));

            if (!Utility.canSupportRigidBlock(ascendingSupport, BlockFace.TOP)) {
                return Block.AIR;
            }
        }

        return currentBlock.withProperty("shape", shape);
    }

    private static @Nullable BlockFace ascendingFace(@Nullable String shape) {
        if (shape == null) {
            return null;
        }

        return switch (shape) {
            case "ascending_east" -> BlockFace.EAST;
            case "ascending_west" -> BlockFace.WEST;
            case "ascending_north" -> BlockFace.NORTH;
            case "ascending_south" -> BlockFace.SOUTH;
            default -> null;
        };
    }

    private static String defaultShape(@Nullable String shape) {
        return switch (shape) {
            case "east_west", "ascending_east", "ascending_west" -> "east_west";
            case null, default -> "north_south";
        };
    }

    private Block placeRail(Instance instance, Point pos, Block initial, String defaultShape) {
        var straight = isStraight(initial);
        var north = pos.relative(BlockFace.NORTH);
        var south = pos.relative(BlockFace.SOUTH);
        var west = pos.relative(BlockFace.WEST);
        var east = pos.relative(BlockFace.EAST);
        var n = hasNeighborRail(instance, north, pos);
        var s = hasNeighborRail(instance, south, pos);
        var w = hasNeighborRail(instance, west, pos);
        var e = hasNeighborRail(instance, east, pos);
        var shape = computeShape(instance, north, south, west, east, n, s, w, e, straight, defaultShape, null);
        var resolved = initial.withProperty("shape", shape);

        for (var connection : connectionsFor(pos, shape)) {
            var neighbor = findRail(instance, connection);

            if (neighbor == null) {
                continue;
            }

            var live = liveConnections(instance, neighbor.position, neighbor.block);

            if (canConnectTo(live, pos)) {
                connectToNeighbor(instance, neighbor.position, neighbor.block, live, pos);
            }
        }

        return resolved;
    }

    private static String computeShape(
            Block.Getter getter,
            Point north, Point south, Point west, Point east,
            boolean n, boolean s, boolean w, boolean e,
            boolean straight,
            String defaultShape,
            @Nullable Point newRailPos
    ) {
        var northOrSouth = n || s;
        var westOrEast = w || e;
        String shape = null;

        if (northOrSouth && !westOrEast) {
            shape = "north_south";
        }

        if (westOrEast && !northOrSouth) {
            shape = "east_west";
        }

        var southAndEast = s && e;
        var southAndWest = s && w;
        var northAndEast = n && e;
        var northAndWest = n && w;

        if (!straight) {
            if (southAndEast && !n && !w) {
                shape = "south_east";
            }

            if (southAndWest && !n && !e) {
                shape = "south_west";
            }

            if (northAndWest && !s && !e) {
                shape = "north_west";
            }

            if (northAndEast && !s && !w) {
                shape = "north_east";
            }
        }

        if (shape == null) {
            if (northOrSouth && westOrEast) {
                shape = defaultShape;
            } else if (northOrSouth) {
                shape = "north_south";
            } else if (westOrEast) {
                shape = "east_west";
            }

            if (!straight) {
                if (northAndWest) {
                    shape = "north_west";
                }

                if (northAndEast) {
                    shape = "north_east";
                }

                if (southAndWest) {
                    shape = "south_west";
                }

                if (southAndEast) {
                    shape = "south_east";
                }
            }
        }

        if ("north_south".equals(shape)) {
            if (railAt(getter, north.relative(BlockFace.TOP), newRailPos)) {
                shape = "ascending_north";
            }

            if (railAt(getter, south.relative(BlockFace.TOP), newRailPos)) {
                shape = "ascending_south";
            }
        }

        if ("east_west".equals(shape)) {
            if (railAt(getter, east.relative(BlockFace.TOP), newRailPos)) {
                shape = "ascending_east";
            }

            if (railAt(getter, west.relative(BlockFace.TOP), newRailPos)) {
                shape = "ascending_west";
            }
        }

        return shape == null ? defaultShape : shape;
    }

    private void connectToNeighbor(Instance instance, Point neighborPos, Block neighborBlock,
                                   List<Point> liveConns, Point newRailPos) {
        var straight = isStraight(neighborBlock);
        var north = neighborPos.relative(BlockFace.NORTH);
        var south = neighborPos.relative(BlockFace.SOUTH);
        var west = neighborPos.relative(BlockFace.WEST);
        var east = neighborPos.relative(BlockFace.EAST);

        var allConns = new ArrayList<Point>(liveConns.size() + 1);
        allConns.addAll(liveConns);
        allConns.add(newRailPos);

        var n = matchesColumn(allConns, north);
        var s = matchesColumn(allConns, south);
        var w = matchesColumn(allConns, west);
        var e = matchesColumn(allConns, east);
        var shape = computeShape(instance, north, south, west, east, n, s, w, e, straight, "north_south", newRailPos);
        var currentShape = neighborBlock.getProperty("shape");

        if (shape.equals(currentShape)) {
            return;
        }

        instance.setBlock(neighborPos, neighborBlock.withProperty("shape", shape));
    }

    /**
     * Vanilla RailState.removeSoftConnections: yields the neighbor's connection columns pruned to
     * those whose own current shape connects back. A neighbor whose connections don't include this
     * rail's column doesn't reciprocate, so it shouldn't be counted when deciding our shape.
     */
    private static List<Point> liveConnections(Block.Getter getter, Point pos, Block block) {
        var shape = block.getProperty("shape");

        if (shape == null) {
            return List.of();
        }

        var raw = connectionsFor(pos, shape);
        var live = new ArrayList<Point>(raw.size());

        for (var conn : raw) {
            var neighbor = findRail(getter, conn);

            if (neighbor == null) {
                continue;
            }

            var neighborShape = neighbor.block.getProperty("shape");

            if (neighborShape == null) {
                continue;
            }

            if (matchesColumn(connectionsFor(neighbor.position, neighborShape), pos)) {
                live.add(neighbor.position);
            }
        }

        return live;
    }

    private static boolean matchesColumn(List<Point> conns, Point col) {
        for (var c : conns) {
            if (c.blockX() == col.blockX() && c.blockZ() == col.blockZ()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Vanilla RailState.canConnectTo: a neighbor accepts a connection from {@code ourPos} if its
     * pruned connection list already includes ourPos's column, or it still has room (< 2 conns).
     */
    private static boolean canConnectTo(List<Point> neighborLiveConns, Point ourPos) {
        return matchesColumn(neighborLiveConns, ourPos) || neighborLiveConns.size() != 2;
    }

    /**
     * Vanilla RailState.hasNeighborRail: a rail exists in the column (or ±1Y) and its pruned
     * connections still leave room to point back at us.
     */
    private static boolean hasNeighborRail(Block.Getter getter, Point neighborSearchPos, Point ourPos) {
        var found = findRail(getter, neighborSearchPos);

        if (found == null) {
            return false;
        }

        return canConnectTo(liveConnections(getter, found.position, found.block), ourPos);
    }

    /**
     * Whether {@code probePos} hosts a rail. During placement reshape the just-placed rail isn't in
     * the world yet, so {@code newRailPos} stands in for its position when probing ascending
     * neighbours that would otherwise miss it.
     */
    private static boolean railAt(Block.Getter getter, Point probePos, @Nullable Point newRailPos) {
        if (newRailPos != null
                && newRailPos.blockX() == probePos.blockX()
                && newRailPos.blockY() == probePos.blockY()
                && newRailPos.blockZ() == probePos.blockZ()) {
            return true;
        }

        return isRail(getter, probePos);
    }

    private static @Nullable NeighborRail findRail(Block.Getter getter, Point pos) {
        var here = getter.getBlock(pos);

        if (isRail(here)) {
            return new NeighborRail(pos, here);
        }

        var above = pos.relative(BlockFace.TOP);
        var aboveBlock = getter.getBlock(above);

        if (isRail(aboveBlock)) {
            return new NeighborRail(above, aboveBlock);
        }

        var below = pos.relative(BlockFace.BOTTOM);
        var belowBlock = getter.getBlock(below);

        if (isRail(belowBlock)) {
            return new NeighborRail(below, belowBlock);
        }

        return null;
    }

    private static boolean isRail(Block.Getter getter, Point pos) {
        return isRail(getter.getBlock(pos));
    }

    private static boolean isRail(Block block) {
        return block.compare(Block.RAIL)
                || block.compare(Block.POWERED_RAIL)
                || block.compare(Block.DETECTOR_RAIL)
                || block.compare(Block.ACTIVATOR_RAIL);
    }

    private static boolean isStraight(Block block) {
        return block.compare(Block.POWERED_RAIL)
                || block.compare(Block.DETECTOR_RAIL)
                || block.compare(Block.ACTIVATOR_RAIL);
    }

    private static Block withShape(Block block, String shape, boolean waterlogged) {
        return block
                .withProperty("shape", shape)
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }

    private static List<Point> connectionsFor(Point pos, String shape) {
        var result = new ArrayList<Point>(2);

        switch (shape) {
            case "north_south" -> {
                result.add(pos.relative(BlockFace.NORTH));
                result.add(pos.relative(BlockFace.SOUTH));
            }
            case "east_west" -> {
                result.add(pos.relative(BlockFace.WEST));
                result.add(pos.relative(BlockFace.EAST));
            }
            case "ascending_east" -> {
                result.add(pos.relative(BlockFace.WEST));
                result.add(pos.relative(BlockFace.EAST).relative(BlockFace.TOP));
            }
            case "ascending_west" -> {
                result.add(pos.relative(BlockFace.WEST).relative(BlockFace.TOP));
                result.add(pos.relative(BlockFace.EAST));
            }
            case "ascending_north" -> {
                result.add(pos.relative(BlockFace.NORTH).relative(BlockFace.TOP));
                result.add(pos.relative(BlockFace.SOUTH));
            }
            case "ascending_south" -> {
                result.add(pos.relative(BlockFace.NORTH));
                result.add(pos.relative(BlockFace.SOUTH).relative(BlockFace.TOP));
            }
            case "south_east" -> {
                result.add(pos.relative(BlockFace.EAST));
                result.add(pos.relative(BlockFace.SOUTH));
            }
            case "south_west" -> {
                result.add(pos.relative(BlockFace.WEST));
                result.add(pos.relative(BlockFace.SOUTH));
            }
            case "north_west" -> {
                result.add(pos.relative(BlockFace.WEST));
                result.add(pos.relative(BlockFace.NORTH));
            }
            case "north_east" -> {
                result.add(pos.relative(BlockFace.EAST));
                result.add(pos.relative(BlockFace.NORTH));
            }
        }
        return result;
    }

    private record NeighborRail(Point position, Block block) {

    }
}

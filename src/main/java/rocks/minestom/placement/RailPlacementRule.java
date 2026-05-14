package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RailPlacementRule extends BlockPlacementRule {
    public RailPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {

        if (!(placementState.instance() instanceof Instance instance)) {
            return null;
        }
        var placePosition = placementState.placePosition();
        var below = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!below.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return null;
        }

        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var horizontal = BlockFace.fromYaw(yaw);
        var defaultShape = (horizontal == BlockFace.EAST || horizontal == BlockFace.WEST) ? "east_west" : "north_south";
        var initialBlock = withShape(this.block, defaultShape, waterlogged);
        return placeRail(instance, placePosition, initialBlock, defaultShape);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var fromFace = updateState.fromFace();

        if (fromFace != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }
        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));

        if (!below.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return Block.AIR;
        }
        return updateState.currentBlock();
    }

    private @NotNull Block placeRail(@NotNull Instance instance, @NotNull Point pos, @NotNull Block initial, @NotNull String defaultShape) {
        var straight = isStraight(initial);
        var north = pos.relative(BlockFace.NORTH);
        var south = pos.relative(BlockFace.SOUTH);
        var west = pos.relative(BlockFace.WEST);
        var east = pos.relative(BlockFace.EAST);
        var n = hasNeighborRail(instance, north);
        var s = hasNeighborRail(instance, south);
        var w = hasNeighborRail(instance, west);
        var e = hasNeighborRail(instance, east);
        var shape = computeShape(instance, north, south, west, east, n, s, w, e, straight, defaultShape);
        var resolved = initial.withProperty("shape", shape);

        for (var connection : connectionsFor(pos, shape)) {
            var neighbor = findRail(instance, connection);

            if (neighbor == null) {
                continue;
            }
            reshapeNeighbor(instance, neighbor.position, neighbor.block, pos);
        }
        return resolved;
    }

    private static @NotNull String computeShape(
            @NotNull Block.Getter getter,
            @NotNull Point north, @NotNull Point south, @NotNull Point west, @NotNull Point east,
            boolean n, boolean s, boolean w, boolean e,
            boolean straight,
            @NotNull String defaultShape
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

            if (isRail(getter, north.relative(BlockFace.TOP))) {
                shape = "ascending_north";
            }

            if (isRail(getter, south.relative(BlockFace.TOP))) {
                shape = "ascending_south";
            }
        }

        if ("east_west".equals(shape)) {

            if (isRail(getter, east.relative(BlockFace.TOP))) {
                shape = "ascending_east";
            }

            if (isRail(getter, west.relative(BlockFace.TOP))) {
                shape = "ascending_west";
            }
        }

        return shape == null ? defaultShape : shape;
    }

    private void reshapeNeighbor(@NotNull Instance instance, @NotNull Point neighborPos, @NotNull Block neighborBlock, @NotNull Point newRailPos) {
        var straight = isStraight(neighborBlock);
        var north = neighborPos.relative(BlockFace.NORTH);
        var south = neighborPos.relative(BlockFace.SOUTH);
        var west = neighborPos.relative(BlockFace.WEST);
        var east = neighborPos.relative(BlockFace.EAST);
        var n = hasNeighborRail(instance, north) || sameColumn(north, newRailPos);
        var s = hasNeighborRail(instance, south) || sameColumn(south, newRailPos);
        var w = hasNeighborRail(instance, west) || sameColumn(west, newRailPos);
        var e = hasNeighborRail(instance, east) || sameColumn(east, newRailPos);
        var currentShape = neighborBlock.getProperty("shape");
        var defaultShape = currentShape == null ? "north_south" : currentShape;
        var connectionCount = (n ? 1 : 0) + (s ? 1 : 0) + (w ? 1 : 0) + (e ? 1 : 0);

        if (connectionCount > 2 && !canConnectTo(neighborBlock)) {
            return;
        }
        var shape = computeShape(instance, north, south, west, east, n, s, w, e, straight, defaultShape);

        if (shape.equals(currentShape)) {
            return;
        }
        instance.setBlock(neighborPos, neighborBlock.withProperty("shape", shape));
    }

    private static boolean sameColumn(@NotNull Point a, @NotNull Point b) {
        return a.blockX() == b.blockX() && a.blockZ() == b.blockZ();
    }

    private static boolean canConnectTo(@NotNull Block neighborBlock) {
        return neighborBlock.getProperty("shape") == null;
    }

    private static boolean hasNeighborRail(@NotNull Block.Getter getter, @NotNull Point neighborPos) {

        if (isRail(getter, neighborPos)) {
            return true;
        }

        if (isRail(getter, neighborPos.relative(BlockFace.TOP))) {
            return true;
        }
        return isRail(getter, neighborPos.relative(BlockFace.BOTTOM));
    }

    private static @Nullable NeighborRail findRail(@NotNull Block.Getter getter, @NotNull Point pos) {
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

    private static boolean isRail(@NotNull Block.Getter getter, @NotNull Point pos) {
        return isRail(getter.getBlock(pos));
    }

    private static boolean isRail(@NotNull Block block) {
        return block.compare(Block.RAIL)
                || block.compare(Block.POWERED_RAIL)
                || block.compare(Block.DETECTOR_RAIL)
                || block.compare(Block.ACTIVATOR_RAIL);
    }

    private static boolean isStraight(@NotNull Block block) {
        return !block.compare(Block.RAIL);
    }

    private static @NotNull Block withShape(@NotNull Block block, @NotNull String shape, boolean waterlogged) {
        return block
                .withProperty("shape", shape)
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }

    private static @NotNull List<Point> connectionsFor(@NotNull Point pos, @NotNull String shape) {
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

package rocks.minestom.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

final class VanillaPlacementUtils {
    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    private VanillaPlacementUtils() {

    }

    static void scheduleHorizontalNeighborRuleUpdates(Block.Getter blockGetter, Point centerPosition) {
        if (!(blockGetter instanceof Instance instance)) {
            return;
        }

        instance.scheduleNextTick(currentInstance -> {
            for (var neighborFace : HORIZONTAL_FACES) {
                var neighborPosition = centerPosition.relative(neighborFace);
                var neighborBlock = currentInstance.getBlock(neighborPosition);
                var placementRule = MinecraftServer.getBlockManager().getBlockPlacementRule(neighborBlock);

                if (placementRule == null) {
                    continue;
                }

                var updateState = new BlockPlacementRule.UpdateState(
                        currentInstance,
                        neighborPosition,
                        neighborBlock,
                        neighborFace.getOppositeFace()
                );

                var updatedNeighbor = placementRule.blockUpdate(updateState);
                currentInstance.setBlock(neighborPosition, updatedNeighbor, false);
            }
        });
    }

    static boolean hasNeighborSignal(Block.Getter blockGetter, Point position) {
        for (var face : BlockFace.values()) {
            var neighborPosition = position.relative(face);
            var neighbor = blockGetter.getBlock(neighborPosition);

            if (weakSignal(neighbor, face) > 0) {
                return true;
            }

            if (neighbor.redstoneConductor() && hasDirectSignal(blockGetter, neighborPosition)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasDirectSignal(Block.Getter blockGetter, Point position) {
        for (var face : BlockFace.values()) {
            var source = blockGetter.getBlock(position.relative(face));

            if (directSignal(source, face) > 0) {
                return true;
            }
        }

        return false;
    }

    private static int weakSignal(Block block, BlockFace direction) {
        if (!block.signalSource()) {
            return 0;
        }

        if (block.compare(Block.REDSTONE_BLOCK)) {
            return 15;
        }

        if (block.compare(Block.REDSTONE_WIRE)) {
            var power = property(block, "power");

            if (power == 0 || direction == BlockFace.BOTTOM) {
                return 0;
            }

            if (direction == BlockFace.TOP) {
                return power;
            }

            var connection = block.getProperty(direction.getOppositeFace().name().toLowerCase());
            return "none".equals(connection) ? 0 : power;
        }

        if (block.compare(Block.REDSTONE_TORCH)) {
            return "true".equals(block.getProperty("lit")) && direction != BlockFace.TOP ? 15 : 0;
        }

        if (block.compare(Block.REDSTONE_WALL_TORCH)) {
            return "true".equals(block.getProperty("lit"))
                    && !faces(direction, block.getProperty("facing")) ? 15 : 0;
        }

        if (block.compare(Block.REPEATER)
                || block.compare(Block.COMPARATOR)
                || block.compare(Block.OBSERVER)) {
            return "true".equals(block.getProperty("powered"))
                    && faces(direction, block.getProperty("facing")) ? 15 : 0;
        }

        if (block.compare(Block.CALIBRATED_SCULK_SENSOR)
                && faces(direction, block.getProperty("facing"))) {
            return 0;
        }

        var power = Math.max(property(block, "power"), property(block, "output_power"));

        if (power > 0) {
            return power;
        }

        return "true".equals(block.getProperty("powered")) ? 15 : 0;
    }

    private static int directSignal(Block block, BlockFace direction) {
        if (block.compare(Block.REDSTONE_BLOCK)) {
            return 15;
        }

        if (block.compare(Block.REDSTONE_WIRE)) {
            return weakSignal(block, direction);
        }

        if (block.compare(Block.REDSTONE_TORCH) || block.compare(Block.REDSTONE_WALL_TORCH)) {
            return direction == BlockFace.BOTTOM && "true".equals(block.getProperty("lit")) ? 15 : 0;
        }

        if (block.compare(Block.REPEATER)
                || block.compare(Block.COMPARATOR)
                || block.compare(Block.OBSERVER)
                || block.compare(Block.TRIPWIRE_HOOK)
                || block.key().value().endsWith("lightning_rod")) {
            return "true".equals(block.getProperty("powered"))
                    && faces(direction, block.getProperty("facing")) ? 15 : 0;
        }

        var path = block.key().value();

        if (block.compare(Block.LEVER) || path.endsWith("_button")) {
            return "true".equals(block.getProperty("powered"))
                    && faces(direction, connectedDirection(block)) ? 15 : 0;
        }

        if (path.endsWith("_pressure_plate")
                || block.compare(Block.DETECTOR_RAIL)
                || block.compare(Block.LECTERN)
                || block.compare(Block.SCULK_SENSOR)
                || block.compare(Block.CALIBRATED_SCULK_SENSOR)) {
            return direction == BlockFace.TOP ? weakSignal(block, direction) : 0;
        }

        return 0;
    }

    private static String connectedDirection(Block block) {
        return switch (block.getProperty("face")) {
            case "floor" -> "up";
            case "ceiling" -> "down";
            default -> block.getProperty("facing");
        };
    }

    private static boolean faces(BlockFace face, String facing) {
        return face.name().equalsIgnoreCase(facing);
    }

    private static int property(Block block, String name) {
        var value = block.getProperty(name);
        return value == null ? 0 : Integer.parseInt(value);
    }
}

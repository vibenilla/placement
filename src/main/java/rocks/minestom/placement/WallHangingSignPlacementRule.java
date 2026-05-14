package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class WallHangingSignPlacementRule extends BlockPlacementRule {
    public WallHangingSignPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var pitch = playerPosition == null ? 0.0F : playerPosition.pitch();
        var nearest = playerPosition == null
                ? new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.TOP, BlockFace.BOTTOM}
                : orderedByNearest(yaw, pitch);
        var clickedFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        BlockFace facing = null;

        for (var direction : nearest) {

            if (!isHorizontal(direction) || sameAxis(direction, clickedFace)) {
                continue;
            }
            var candidate = direction.getOppositeFace();
            var clockwise = clockwise(candidate);
            var counterClockwise = counterClockwise(candidate);
            var clockwiseBlock = instance.getBlock(placePosition.relative(clockwise));
            var counterClockwiseBlock = instance.getBlock(placePosition.relative(counterClockwise));

            if (clockwiseBlock.registry().collisionShape().isFaceFull(counterClockwise)
                    && counterClockwiseBlock.registry().collisionShape().isFaceFull(clockwise)) {
                facing = candidate;
                break;
            }
        }

        if (facing == null) {
            return null;
        }

        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));

        return this.block
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var facing = parseFacing(currentBlock.getProperty("facing"));

        if (facing == null) {
            return currentBlock;
        }
        var clockwise = clockwise(facing);
        var counterClockwise = counterClockwise(facing);
        var fromFace = updateState.fromFace();

        if (fromFace != clockwise && fromFace != counterClockwise) {
            return currentBlock;
        }
        var instance = updateState.instance();
        var blockPosition = updateState.blockPosition();
        var clockwiseBlock = instance.getBlock(blockPosition.relative(clockwise));
        var counterClockwiseBlock = instance.getBlock(blockPosition.relative(counterClockwise));
        var clockwiseSupports = clockwiseBlock.registry().collisionShape().isFaceFull(counterClockwise);
        var counterClockwiseSupports = counterClockwiseBlock.registry().collisionShape().isFaceFull(clockwise);

        if (!clockwiseSupports || !counterClockwiseSupports) {
            return Block.AIR;
        }
        return currentBlock;
    }

    private static BlockFace parseFacing(String facingName) {
        return switch (facingName) {
            case "north" -> BlockFace.NORTH;
            case "east" -> BlockFace.EAST;
            case "south" -> BlockFace.SOUTH;
            case "west" -> BlockFace.WEST;
            case null, default -> null;
        };
    }

    private static BlockFace clockwise(@NotNull BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> face;
        };
    }

    private static BlockFace counterClockwise(@NotNull BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.WEST;
            case WEST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.EAST;
            case EAST -> BlockFace.NORTH;
            default -> face;
        };
    }

    private static boolean isHorizontal(@NotNull BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.EAST || face == BlockFace.WEST;
    }

    private static boolean sameAxis(@NotNull BlockFace first, @NotNull BlockFace second) {
        return switch (first) {
            case NORTH, SOUTH -> second == BlockFace.NORTH || second == BlockFace.SOUTH;
            case EAST, WEST -> second == BlockFace.EAST || second == BlockFace.WEST;
            case TOP, BOTTOM -> second == BlockFace.TOP || second == BlockFace.BOTTOM;
        };
    }

    private static BlockFace[] orderedByNearest(float yawDegrees, float pitchDegrees) {
        var pitchRadians = pitchDegrees * (float) (Math.PI / 180.0D);
        var yawRadians = -yawDegrees * (float) (Math.PI / 180.0D);
        var pitchSin = (float) Math.sin(pitchRadians);
        var pitchCos = (float) Math.cos(pitchRadians);
        var yawSin = (float) Math.sin(yawRadians);
        var yawCos = (float) Math.cos(yawRadians);
        var xPositive = yawSin > 0.0F;
        var yPositive = pitchSin < 0.0F;
        var zPositive = yawCos > 0.0F;
        var xYaw = xPositive ? yawSin : -yawSin;
        var yMagnitude = yPositive ? -pitchSin : pitchSin;
        var zYaw = zPositive ? yawCos : -yawCos;
        var xMagnitude = xYaw * pitchCos;
        var zMagnitude = zYaw * pitchCos;
        var axisX = xPositive ? BlockFace.EAST : BlockFace.WEST;
        var axisY = yPositive ? BlockFace.TOP : BlockFace.BOTTOM;
        var axisZ = zPositive ? BlockFace.SOUTH : BlockFace.NORTH;

        if (xYaw > zYaw) {
            if (yMagnitude > xMagnitude) {
                return makeDirectionArray(axisY, axisX, axisZ);
            }

            if (zMagnitude > yMagnitude) {
                return makeDirectionArray(axisX, axisZ, axisY);
            }
            return makeDirectionArray(axisX, axisY, axisZ);
        }

        if (yMagnitude > zMagnitude) {
            return makeDirectionArray(axisY, axisZ, axisX);
        }

        if (xMagnitude > yMagnitude) {
            return makeDirectionArray(axisZ, axisX, axisY);
        }
        return makeDirectionArray(axisZ, axisY, axisX);
    }

    private static BlockFace[] makeDirectionArray(@NotNull BlockFace first, @NotNull BlockFace second, @NotNull BlockFace third) {
        return new BlockFace[]{first, second, third, third.getOppositeFace(), second.getOppositeFace(), first.getOppositeFace()};
    }
}

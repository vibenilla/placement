package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class ShelfPlacementRule extends BlockPlacementRule {
    public ShelfPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var nearest = playerPosition == null
                ? new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.TOP, BlockFace.BOTTOM}
                : orderedByNearest(playerPosition.yaw(), playerPosition.pitch());
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        BlockFace facing = null;

        for (var direction : nearest) {
            if (!isHorizontal(direction)) {
                continue;
            }

            var supportBlock = instance.getBlock(placePosition.relative(direction));

            if (supportBlock.registry().collisionShape().isFaceFull(direction.getOppositeFace())) {
                facing = direction.getOppositeFace();
                break;
            }
        }

        if (facing == null) {
            return null;
        }

        // TODO: vanilla checks neighbor signal via Level.hasNeighborSignal; not available from PlacementState.
        return this.block
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("powered", "false");
    }

    private static boolean isHorizontal(@NotNull BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.EAST || face == BlockFace.WEST;
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

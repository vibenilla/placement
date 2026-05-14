package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class CrafterPlacementRule extends BlockPlacementRule {
    public CrafterPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var nearestLooking = playerPosition == null
                ? BlockFace.NORTH
                : nearestLookingDirection(playerPosition.yaw(), playerPosition.pitch()).getOppositeFace();
        var horizontal = playerPosition == null ? BlockFace.NORTH : BlockFace.fromYaw(playerPosition.yaw());
        var verticalDirection = switch (nearestLooking) {
            case BOTTOM -> horizontal.getOppositeFace();
            case TOP -> horizontal;
            default -> BlockFace.TOP;
        };

        // TODO: vanilla sets triggered = level.hasNeighborSignal(placePosition); we don't have a signal API here.
        return this.block
                .withHandler(ConsumeInteractionBlockHandler.INSTANCE)
                .withProperty("orientation", orientationName(nearestLooking) + "_" + orientationName(verticalDirection))
                .withProperty("triggered", "false");
    }

    private static String orientationName(@NotNull BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }

    private static BlockFace nearestLookingDirection(float yawDegrees, float pitchDegrees) {
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
                return axisY;
            }

            if (zMagnitude > yMagnitude) {
                return axisX;
            }
            return axisX;
        }

        if (yMagnitude > zMagnitude) {
            return axisY;
        }

        if (xMagnitude > yMagnitude) {
            return axisZ;
        }
        return axisZ;
    }
}

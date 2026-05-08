package rocks.minestom.placement;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LanternPlacementRule extends BlockPlacementRule {
    public LanternPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var existingBlock = instance.getBlock(placePosition);
        var waterlogged = existingBlock.compare(Block.WATER);
        var orderedFaces = nearestLookingDirections(placementState.playerPosition());

        for (var face : orderedFaces) {
            if (face != BlockFace.TOP && face != BlockFace.BOTTOM) {
                continue;
            }

            var hanging = face == BlockFace.TOP;
            var supportFace = hanging ? BlockFace.TOP : BlockFace.BOTTOM;
            var supportPosition = placePosition.relative(supportFace);
            var supportBlock = instance.getBlock(supportPosition);

            if (supportBlock.registry().collisionShape().isFaceFull(supportFace.getOppositeFace())) {
                return this.block
                        .withProperty("hanging", hanging ? "true" : "false")
                        .withProperty("waterlogged", waterlogged ? "true" : "false");
            }
        }

        return null;
    }

    private static BlockFace[] nearestLookingDirections(@Nullable Pos playerPosition) {
        if (playerPosition == null) {
            return new BlockFace[]{BlockFace.BOTTOM, BlockFace.TOP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        }

        var pitch = playerPosition.pitch() * (float) (Math.PI / 180.0);
        var yaw = -playerPosition.yaw() * (float) (Math.PI / 180.0);
        var pitchSin = (float) Math.sin(pitch);
        var pitchCos = (float) Math.cos(pitch);
        var yawSin = (float) Math.sin(yaw);
        var yawCos = (float) Math.cos(yaw);
        var xPos = yawSin > 0.0F;
        var yPos = pitchSin < 0.0F;
        var zPos = yawCos > 0.0F;
        var xYaw = xPos ? yawSin : -yawSin;
        var yMag = yPos ? -pitchSin : pitchSin;
        var zYaw = zPos ? yawCos : -yawCos;
        var xMag = xYaw * pitchCos;
        var zMag = zYaw * pitchCos;
        var axisX = xPos ? BlockFace.EAST : BlockFace.WEST;
        var axisY = yPos ? BlockFace.TOP : BlockFace.BOTTOM;
        var axisZ = zPos ? BlockFace.SOUTH : BlockFace.NORTH;

        if (xYaw > zYaw) {
            if (yMag > xMag) {
                return makeDirectionArray(axisY, axisX, axisZ);
            }
            return zMag > yMag ? makeDirectionArray(axisX, axisZ, axisY) : makeDirectionArray(axisX, axisY, axisZ);
        }

        if (yMag > zMag) {
            return makeDirectionArray(axisY, axisZ, axisX);
        }
        return xMag > yMag ? makeDirectionArray(axisZ, axisX, axisY) : makeDirectionArray(axisZ, axisY, axisX);
    }

    private static BlockFace[] makeDirectionArray(@NotNull BlockFace first, @NotNull BlockFace second, @NotNull BlockFace third) {
        return new BlockFace[]{first, second, third, third.getOppositeFace(), second.getOppositeFace(), first.getOppositeFace()};
    }
}

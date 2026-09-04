package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.Nullable;

public final class FaceAttachedPlacementRule extends BlockPlacementRule {
    private final @Nullable BlockHandler handler;

    public FaceAttachedPlacementRule(Block block) {
        this(block, null);
    }

    public FaceAttachedPlacementRule(Block block, @Nullable BlockHandler handler) {
        super(block);
        this.handler = handler;
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var horizontalFacing = BlockFace.fromYaw(yaw);
        var nearestDirections = nearestLookingDirections(playerPosition);

        for (var direction : nearestDirections) {
            String face;
            BlockFace facing;

            if (direction == BlockFace.TOP || direction == BlockFace.BOTTOM) {
                face = direction == BlockFace.TOP ? "ceiling" : "floor";
                facing = horizontalFacing;
            } else {
                face = "wall";
                facing = direction.getOppositeFace();
            }

            if (canAttach(placementState.instance(), placementState.placePosition(), direction)) {
                var result = this.handler == null ? placementState.block() : placementState.block().withHandler(this.handler);
                return result
                        .withProperty("face", face)
                        .withProperty("facing", facing.name().toLowerCase())
                        .withProperty("powered", "false");
            }
        }

        return null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var supportDirection = supportDirection(currentBlock);

        if (supportDirection == null || updateState.fromFace() != supportDirection) {
            return currentBlock;
        }

        return canAttach(updateState.instance(), updateState.blockPosition(), supportDirection)
                ? currentBlock
                : Block.AIR;
    }

    private static boolean canAttach(Block.Getter blockGetter, Point position, BlockFace connectedDirection) {
        var supportPosition = position.relative(connectedDirection);
        var supportBlock = blockGetter.getBlock(supportPosition);
        return Utility.canSupportCenter(supportBlock, connectedDirection.getOppositeFace());
    }

    private static BlockFace supportDirection(Block block) {
        var face = block.getProperty("face");

        if ("floor".equals(face)) {
            return BlockFace.BOTTOM;
        }

        if ("ceiling".equals(face)) {
            return BlockFace.TOP;
        }

        if (!"wall".equals(face)) {
            return null;
        }

        var facing = block.getProperty("facing");

        return switch (facing) {
            case "north" -> BlockFace.SOUTH;
            case "east" -> BlockFace.WEST;
            case "south" -> BlockFace.NORTH;
            case "west" -> BlockFace.EAST;
            case null, default -> null;
        };
    }

    private static BlockFace[] nearestLookingDirections(@Nullable Pos playerPosition) {
        if (playerPosition == null) {
            return new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.TOP, BlockFace.BOTTOM};
        }

        var pitch = playerPosition.pitch() * (float) (Math.PI / 180.0D);
        var yaw = -playerPosition.yaw() * (float) (Math.PI / 180.0D);
        var pitchSin = (float) Math.sin(pitch);
        var pitchCos = (float) Math.cos(pitch);
        var yawSin = (float) Math.sin(yaw);
        var yawCos = (float) Math.cos(yaw);
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
            return zMagnitude > yMagnitude ? makeDirectionArray(axisX, axisZ, axisY) : makeDirectionArray(axisX, axisY, axisZ);
        }

        if (yMagnitude > zMagnitude) {
            return makeDirectionArray(axisY, axisZ, axisX);
        }

        return xMagnitude > yMagnitude ? makeDirectionArray(axisZ, axisX, axisY) : makeDirectionArray(axisZ, axisY, axisX);
    }

    private static BlockFace[] makeDirectionArray(BlockFace first, BlockFace second, BlockFace third) {
        return new BlockFace[]{first, second, third, third.getOppositeFace(), second.getOppositeFace(), first.getOppositeFace()};
    }
}

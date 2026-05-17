package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.Nullable;

public final class WallMountedPlacementRule extends BlockPlacementRule {
    private final boolean waterloggable;
    private final @Nullable BlockHandler handler;

    public WallMountedPlacementRule(Block block) {
        this(block, true, null);
    }

    public WallMountedPlacementRule(Block block, boolean waterloggable) {
        this(block, waterloggable, null);
    }

    public WallMountedPlacementRule(Block block, @Nullable BlockHandler handler) {
        this(block, true, handler);
    }

    public WallMountedPlacementRule(Block block, boolean waterloggable, @Nullable BlockHandler handler) {
        super(block);
        this.waterloggable = waterloggable;
        this.handler = handler;
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var pitch = playerPosition == null ? 0.0F : playerPosition.pitch();
        var nearest = playerPosition == null
                ? new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}
                : orderedByNearest(yaw, pitch);
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

        var configured = this.handler == null ? this.block : this.block.withHandler(this.handler);

        if (this.waterloggable) {
            var replaced = instance.getBlock(placePosition);
            var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
            return configured
                    .withProperty("facing", facing.name().toLowerCase())
                    .withProperty("waterlogged", String.valueOf(waterlogged));
        }

        return configured.withProperty("facing", facing.name().toLowerCase());
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var facing = parseFacing(currentBlock.getProperty("facing"));

        if (facing == null) {
            return currentBlock;
        }

        var supportFace = facing.getOppositeFace();

        if (updateState.fromFace() != supportFace) {
            return currentBlock;
        }

        var supportBlock = updateState.instance().getBlock(updateState.blockPosition().relative(supportFace));

        if (!supportBlock.registry().collisionShape().isFaceFull(facing)) {
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

    private static boolean isHorizontal(BlockFace face) {
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

    private static BlockFace[] makeDirectionArray(BlockFace first, BlockFace second, BlockFace third) {
        return new BlockFace[]{first, second, third, third.getOppositeFace(), second.getOppositeFace(), first.getOppositeFace()};
    }
}

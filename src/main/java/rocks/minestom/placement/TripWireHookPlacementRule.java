package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class TripWireHookPlacementRule extends BlockPlacementRule {
    private static final int MAX_WIRE_LENGTH = 42;

    public TripWireHookPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
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

            if (Utility.canSupportCenter(supportBlock, direction.getOppositeFace())) {
                facing = direction.getOppositeFace();
                break;
            }
        }

        if (facing == null) {
            return null;
        }

        var result = placementState.block()
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("powered", "false")
                .withProperty("attached", "false");
        return this.withLineState(result, instance, placePosition, facing);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var facing = parseFacing(currentBlock.getProperty("facing"));

        if (facing == null) {
            return currentBlock;
        }

        var support = updateState.instance().getBlock(
                updateState.blockPosition().relative(facing.getOppositeFace()));

        if (!Utility.canSupportCenter(support, facing)) {
            return Block.AIR;
        }

        if (!isHorizontal(updateState.fromFace())) {
            return currentBlock;
        }

        return this.withLineState(currentBlock, updateState.instance(), updateState.blockPosition(), facing);
    }

    private Block withLineState(Block base, Block.Getter blockGetter, Point position, BlockFace facing) {
        var linePosition = position.relative(facing);
        var hasWire = false;
        var powered = false;

        for (var distance = 0; distance < MAX_WIRE_LENGTH; distance++) {
            var lineBlock = blockGetter.getBlock(linePosition);

            if (lineBlock.compare(Block.TRIPWIRE)) {
                hasWire = true;
                powered |= "true".equals(lineBlock.getProperty("powered"));
                linePosition = linePosition.relative(facing);
                continue;
            }

            var oppositeHook = lineBlock.compare(Block.TRIPWIRE_HOOK)
                    && facing.getOppositeFace().name().toLowerCase().equals(lineBlock.getProperty("facing"));
            return base
                    .withProperty("attached", String.valueOf(hasWire && oppositeHook))
                    .withProperty("powered", String.valueOf(hasWire && oppositeHook && powered));
        }

        return base.withProperty("attached", "false").withProperty("powered", "false");
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

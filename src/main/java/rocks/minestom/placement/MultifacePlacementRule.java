package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class MultifacePlacementRule extends BlockPlacementRule {
    private static final BlockFace[] DEFAULT_ORDER = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.TOP,
            BlockFace.BOTTOM
    };

    private final boolean waterloggable;

    public MultifacePlacementRule(@NotNull Block block) {
        this(block, true);
    }

    public MultifacePlacementRule(@NotNull Block block, boolean waterloggable) {
        super(block);
        this.waterloggable = waterloggable;
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var existingBlock = instance.getBlock(placePosition);
        var playerPosition = placementState.playerPosition();
        var nearest = playerPosition == null
                ? DEFAULT_ORDER
                : orderedByNearest(playerPosition.yaw(), playerPosition.pitch());

        for (var direction : nearest) {
            var supportPosition = placePosition.relative(direction);
            var supportBlock = instance.getBlock(supportPosition);

            if (!supportBlock.registry().collisionShape().isFaceFull(direction.getOppositeFace())) {
                continue;
            }

            var faceName = faceProperty(direction);

            if (existingBlock.compare(this.block) && "true".equals(existingBlock.getProperty(faceName))) {
                continue;
            }

            Block result;

            if (existingBlock.compare(this.block)) {
                result = existingBlock;
            } else if (this.waterloggable && existingBlock.compare(Block.WATER) && isWaterSource(existingBlock)) {
                result = this.block.withProperty("waterlogged", "true");
            } else {
                result = this.block;
            }

            return result.withProperty(faceName, "true");
        }
        return null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var instance = updateState.instance();
        var blockPosition = updateState.blockPosition();
        var result = currentBlock;
        var anyFace = false;

        for (var direction : DEFAULT_ORDER) {
            var faceName = faceProperty(direction);
            var faceValue = currentBlock.getProperty(faceName);

            if (!"true".equals(faceValue)) {
                continue;
            }
            var supportBlock = instance.getBlock(blockPosition.relative(direction));

            if (supportBlock.registry().collisionShape().isFaceFull(direction.getOppositeFace())) {
                anyFace = true;
                continue;
            }
            result = result.withProperty(faceName, "false");
        }

        if (!anyFace) {
            return Block.AIR;
        }
        return result;
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        if (!replacement.block().compare(this.block)) {
            return false;
        }

        if (replacement.material() != this.block.registry().material()) {
            return false;
        }

        var blockFace = replacement.blockFace();

        if (blockFace == null) {
            return false;
        }

        var faceName = faceProperty(blockFace);
        var current = replacement.block().getProperty(faceName);
        return current == null || "false".equals(current);
    }

    private static boolean isWaterSource(@NotNull Block water) {
        var level = water.getProperty("level");
        return level == null || "0".equals(level);
    }

    private static String faceProperty(@NotNull BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            case NORTH -> "north";
            case SOUTH -> "south";
            case WEST -> "west";
            case EAST -> "east";
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

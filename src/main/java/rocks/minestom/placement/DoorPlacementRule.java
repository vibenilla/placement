package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see <a href="https://mcsrc.dev/#1/1.21.11_unobfuscated/net/minecraft/world/level/block/DoorBlock">Minecraft source code</a>
 */
public final class DoorPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:doors");

    public DoorPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var instance = (Instance) placementState.instance();
        var playerPosition = placementState.playerPosition();
        var facing = getFacingDirection(playerPosition);
        var placePosition = placementState.placePosition();

        var dimension = MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType());
        assert dimension != null;

        var baseX = placePosition.blockX();
        var baseY = placePosition.blockY();
        var baseZ = placePosition.blockZ();

        // check if there's space for both door halves within the world bounds
        if (baseY <= dimension.minY() || baseY + 1 >= dimension.maxY()) {
            return null;
        }

        var targetBlock = instance.getBlock(baseX, baseY, baseZ);

        if (!isReplaceable(targetBlock)) {
            return null;
        }

        var upperBlock = instance.getBlock(baseX, baseY + 1, baseZ);

        if (!isReplaceable(upperBlock)) {
            return null;
        }

        var supportBlock = instance.getBlock(baseX, baseY - 1, baseZ);

        if (!isSupporting(supportBlock)) {
            return null;
        }

        var hinge = getHinge(instance, facing, placementState, baseX, baseY, baseZ);
        var configured = placementState.block()
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("open", "false")
                .withProperty("hinge", hinge.name().toLowerCase())
                .withProperty("powered", "false");

        var upperPosition = placePosition.add(0, 1, 0);
        instance.setBlock(upperPosition, configured.withProperty("half", "upper"));
        return configured.withProperty("half", "lower");
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var half = currentBlock.getProperty("half");

        if (half == null) {
            return currentBlock;
        }

        var otherHalfY = "lower".equals(half) ? 1 : -1;
        var blockPosition = updateState.blockPosition();
        var otherHalfBlock = updateState.instance().getBlock(
                blockPosition.blockX(),
                blockPosition.blockY() + otherHalfY,
                blockPosition.blockZ());

        if (!isDoorHalf(otherHalfBlock, getOppositeHalf(half))) {
            return Block.AIR;
        }

        return currentBlock;
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private static boolean isDoorHalf(Block block, String expectedHalf) {
        if (!Utility.hasTag(block, KEY)) {
            return false;
        }

        var half = block.getProperty("half");
        return expectedHalf.equals(half);
    }

    private static String getOppositeHalf(String half) {
        return "lower".equals(half) ? "upper" : "lower";
    }

    private static Direction getFacingDirection(@Nullable Pos position) {
        if (position == null) {
            return Direction.NORTH;
        }

        // convert yaw to horizontal direction
        var yaw = (position.yaw() % 360.0F + 360.0F) % 360.0F;

        if (yaw < 45.0F || yaw >= 315.0F) {
            return Direction.SOUTH;
        } else if (yaw < 135.0F) {
            return Direction.WEST;
        } else if (yaw < 225.0F) {
            return Direction.NORTH;
        } else {
            return Direction.EAST;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isReplaceable(Block block) {
        return block.isAir() || block.registry().isReplaceable();
    }

    private static boolean isSupporting(Block block) {
        return !block.isAir() && block.registry().isSolid();
    }

    private static Hinge getHinge(Instance instance, Direction facing, PlacementState placementState, int baseX, int baseY, int baseZ) {
        var leftDirection = rotateCounterClockwise(facing);
        var rightDirection = rotateClockwise(facing);

        var leftLower = instance.getBlock(baseX + leftDirection.normalX(), baseY, baseZ + leftDirection.normalZ());
        var leftUpper = instance.getBlock(baseX + leftDirection.normalX(), baseY + 1, baseZ + leftDirection.normalZ());
        var rightLower = instance.getBlock(baseX + rightDirection.normalX(), baseY, baseZ + rightDirection.normalZ());
        var rightUpper = instance.getBlock(baseX + rightDirection.normalX(), baseY + 1, baseZ + rightDirection.normalZ());

        // calculate solidity score: negative favors left hinge, positive favors right hinge
        var solidityScore = (isFullBlock(leftLower) ? -1 : 0)
                + (isFullBlock(leftUpper) ? -1 : 0)
                + (isFullBlock(rightLower) ? 1 : 0)
                + (isFullBlock(rightUpper) ? 1 : 0);

        var leftDoor = isLowerDoor(leftLower);
        var rightDoor = isLowerDoor(rightLower);

        // determine hinge based on neighboring doors and solidity
        if ((!leftDoor || rightDoor) && solidityScore <= 0) {
            if ((!rightDoor || leftDoor) && solidityScore == 0) {
                // no clear preference, use cursor position to decide
                var relativeCursor = getRelativeCursor(placementState);
                var stepX = facing.normalX();
                var stepZ = facing.normalZ();

                var placeLeft = (stepX >= 0 || !(relativeCursor.z() < 0.5D))
                        && (stepX <= 0 || !(relativeCursor.z() > 0.5D))
                        && (stepZ >= 0 || !(relativeCursor.x() > 0.5D))
                        && (stepZ <= 0 || !(relativeCursor.x() < 0.5D));

                return placeLeft ? Hinge.LEFT : Hinge.RIGHT;
            }

            return Hinge.LEFT;
        }

        return Hinge.RIGHT;
    }

    private static boolean isFullBlock(Block block) {
        return !block.isAir() && block.registry().occludes() && block.registry().isSolid();
    }

    private static boolean isLowerDoor(Block block) {
        if (!Utility.hasTag(block, KEY)) {
            return false;
        }

        return "lower".equals(block.getProperty("half"));
    }

    private static Direction rotateClockwise(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> direction;
        };
    }

    private static Direction rotateCounterClockwise(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            default -> direction;
        };
    }

    private static Cursor getRelativeCursor(PlacementState placementState) {
        var cursorPosition = placementState.cursorPosition();
        var localX = 0.5D;
        var localZ = 0.5D;

        if (cursorPosition != null) {
            localX = cursorPosition.x();
            localZ = cursorPosition.z();
        }

        var offsetX = 0;
        var offsetZ = 0;
        var blockFace = placementState.blockFace();

        if (blockFace != null) {
            var direction = blockFace.toDirection();
            offsetX = direction.normalX();
            offsetZ = direction.normalZ();
        }

        return new Cursor(localX - offsetX, localZ - offsetZ);
    }

    private record Cursor(double x, double z) {

    }

    public enum Hinge {
        LEFT,
        RIGHT
    }
}

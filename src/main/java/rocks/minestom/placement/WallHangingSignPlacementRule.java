package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WallHangingSignPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:wall_hanging_signs");

    public WallHangingSignPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var blockFace = placementState.blockFace();
        if (blockFace == null) {
            return null;
        }

        var playerPosition = placementState.playerPosition();
        var placePosition = placementState.placePosition();
        var instance = placementState.instance();

        var blockX = placePosition.blockX();
        var blockY = placePosition.blockY();
        var blockZ = placePosition.blockZ();

        Direction facing = null;
        if (blockFace == BlockFace.NORTH || blockFace == BlockFace.SOUTH) {
            if (playerPosition != null) {
                double yaw = playerPosition.yaw();
                yaw = (yaw % 360.0 + 360.0) % 360.0;
                facing = (yaw >= 45.0 && yaw < 135.0) || (yaw >= 225.0 && yaw < 315.0) ? Direction.WEST : Direction.EAST;
            } else {
                facing = Direction.EAST;
            }
        } else if (blockFace == BlockFace.EAST || blockFace == BlockFace.WEST) {
            if (playerPosition != null) {
                double yaw = playerPosition.yaw();
                yaw = (yaw % 360.0 + 360.0) % 360.0;
                facing = (yaw >= 135.0 && yaw < 315.0) ? Direction.NORTH : Direction.SOUTH;
            } else {
                facing = Direction.SOUTH;
            }
        }

        if (facing == null) {
            return null;
        }

        if (!canPlace(instance, facing, blockX, blockY, blockZ)) {
            return null;
        }

        return this.block.withProperty("facing", facing.name().toLowerCase());
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var facing = currentBlock.getProperty("facing");

        if (facing == null) {
            return Block.AIR;
        }

        var direction = Direction.valueOf(facing.toUpperCase());
        var blockPosition = updateState.blockPosition();
        var instance = updateState.instance();

        var blockX = blockPosition.blockX();
        var blockY = blockPosition.blockY();
        var blockZ = blockPosition.blockZ();

        if (!canPlace(instance, direction, blockX, blockY, blockZ)) {
            return Block.AIR;
        }

        return currentBlock;
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private static boolean canPlace(Block.Getter instance, Direction facing, int x, int y, int z) {
        var clockwise = getClockwise(facing);
        var counterClockwise = getCounterClockwise(facing);

        return canAttachTo(instance, facing, clockwise, x, y, z)
                || canAttachTo(instance, facing, counterClockwise, x, y, z);
    }

    private static boolean canAttachTo(Block.Getter instance, Direction facing, Direction checkDirection, int x, int y, int z) {
        var blockToCheck = instance.getBlock(
                x + checkDirection.normalX(),
                y,
                z + checkDirection.normalZ()
        );

        if (Utility.hasTag(blockToCheck, KEY)) {
            var otherFacing = blockToCheck.getProperty("facing");
            if (otherFacing != null) {
                var otherDirection = Direction.valueOf(otherFacing.toUpperCase());
                return (otherDirection.normalX() != 0 && facing.normalX() != 0)
                        || (otherDirection.normalZ() != 0 && facing.normalZ() != 0);
            }
        }

        return blockToCheck.registry().isSolid();
    }

    private static Direction getClockwise(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> direction;
        };
    }

    private static Direction getCounterClockwise(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            default -> direction;
        };
    }
}

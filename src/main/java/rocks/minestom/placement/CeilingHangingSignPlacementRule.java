package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CeilingHangingSignPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:ceiling_hanging_signs");

    public CeilingHangingSignPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var placePosition = placementState.placePosition();
        var instance = placementState.instance();

        var blockX = placePosition.blockX();
        var blockY = placePosition.blockY();
        var blockZ = placePosition.blockZ();

        var blockAbove = instance.getBlock(blockX, blockY + 1, blockZ);
        if (!isSturdy(blockAbove, instance, blockX, blockY + 1, blockZ)) {
            return null;
        }

        var direction = getDirection(playerPosition);
        var attached = shouldAttach(blockAbove, direction);
        var rotation = getRotation(playerPosition, direction, attached);

        return this.block
                .withProperty("rotation", String.valueOf(rotation))
                .withProperty("attached", String.valueOf(attached));
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var blockPosition = updateState.blockPosition();
        var instance = updateState.instance();

        var blockX = blockPosition.blockX();
        var blockY = blockPosition.blockY();
        var blockZ = blockPosition.blockZ();

        var blockAbove = instance.getBlock(blockX, blockY + 1, blockZ);
        if (!isSturdy(blockAbove, instance, blockX, blockY + 1, blockZ)) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private static boolean isSturdy(Block block, Block.Getter instance, int x, int y, int z) {
        return block.registry().isSolid();
    }

    private static Direction getDirection(@Nullable Pos playerPosition) {
        if (playerPosition == null) {
            return Direction.NORTH;
        }

        var yaw = (float) playerPosition.yaw();
        yaw = (yaw % 360.0F + 360.0F) % 360.0F;

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

    private static boolean shouldAttach(Block blockAbove, Direction direction) {
        if (Utility.hasTag(blockAbove, Key.key("minecraft:wall_hanging_signs"))) {
            var facing = blockAbove.getProperty("facing");
            if (facing != null) {
                var facingDirection = Direction.valueOf(facing.toUpperCase());
                return facingDirection.normalX() != direction.normalX()
                        && facingDirection.normalZ() != direction.normalZ();
            }
        } else if (Utility.hasTag(blockAbove, KEY)) {
            var rotation = blockAbove.getProperty("rotation");
            if (rotation != null) {
                try {
                    var rotationValue = Integer.parseInt(rotation);
                    var rotationDirection = getDirectionFromRotation(rotationValue);
                    if (rotationDirection != null) {
                        return rotationDirection.normalX() != direction.normalX()
                                && rotationDirection.normalZ() != direction.normalZ();
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return hasFullFaceBelow(blockAbove);
    }

    private static @Nullable Direction getDirectionFromRotation(int rotation) {
        if (rotation == 0 || rotation == 8) {
            return Direction.SOUTH;
        } else if (rotation == 4 || rotation == 12) {
            return Direction.NORTH;
        } else if (rotation == 2 || rotation == 10) {
            return Direction.WEST;
        } else if (rotation == 6 || rotation == 14) {
            return Direction.EAST;
        }
        return null;
    }

    private static boolean hasFullFaceBelow(Block block) {
        return block.registry().isSolid();
    }

    private static int getRotation(@Nullable Pos playerPosition, Direction direction, boolean attached) {
        if (!attached) {
            return getRotationFromDirection(direction.opposite());
        }

        if (playerPosition == null) {
            return 0;
        }

        var rotation = (float) (playerPosition.yaw() + 180.0F);
        return (int) Math.floor((double) (rotation * 16.0F / 360.0F) + 0.5D) & 15;
    }

    private static int getRotationFromDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> 4;
            case SOUTH -> 12;
            case WEST -> 2;
            case EAST -> 10;
            default -> 0;
        };
    }
}

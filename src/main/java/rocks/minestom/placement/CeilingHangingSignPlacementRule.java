package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class CeilingHangingSignPlacementRule extends BlockPlacementRule {
    public CeilingHangingSignPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var direction = BlockFace.fromYaw(yaw);
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var abovePosition = placePosition.relative(BlockFace.TOP);
        var blockAbove = instance.getBlock(abovePosition);
        var aboveFaceFull = blockAbove.registry().collisionShape().isFaceFull(BlockFace.BOTTOM);
        var shifting = placementState.isPlayerShifting();
        var attachedToMiddle = !aboveFaceFull || shifting;
        var hangingFromSign = false;

        if (isHangingSign(blockAbove) && !shifting) {
            var aboveFacing = blockAbove.getProperty("facing");

            if (aboveFacing != null) {
                if (sameHorizontalAxis(aboveFacing, direction)) {
                    attachedToMiddle = false;
                    hangingFromSign = true;
                }
            } else {
                var aboveRotationProperty = blockAbove.getProperty("rotation");

                if (aboveRotationProperty != null) {
                    var aboveDirection = segmentToDirection(Integer.parseInt(aboveRotationProperty));

                    if (aboveDirection != null && sameHorizontalAxis(aboveDirection, direction)) {
                        attachedToMiddle = false;
                        hangingFromSign = true;
                    }
                }
            }
        }

        if (!aboveFaceFull && !hangingFromSign) {
            return null;
        }

        var rotation = !attachedToMiddle
                ? directionToSegment(direction.getOppositeFace())
                : Math.round((yaw + 180.0F) * 16.0F / 360.0F) & 15;
        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));

        return this.block
                .withProperty("attached", String.valueOf(attachedToMiddle))
                .withProperty("rotation", Integer.toString(rotation))
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {

        if (updateState.fromFace() != BlockFace.TOP) {
            return updateState.currentBlock();
        }
        var currentBlock = updateState.currentBlock();
        var aboveBlock = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.TOP));
        var aboveFaceFull = aboveBlock.registry().collisionShape().isFaceFull(BlockFace.BOTTOM);

        if (aboveFaceFull) {
            return currentBlock;
        }

        if (!isHangingSign(aboveBlock)) {
            return Block.AIR;
        }
        var rotationProperty = currentBlock.getProperty("rotation");

        if (rotationProperty == null) {
            return Block.AIR;
        }
        var direction = segmentToDirection(Integer.parseInt(rotationProperty));

        if (direction == null) {
            return Block.AIR;
        }
        var aboveFacing = aboveBlock.getProperty("facing");

        if (aboveFacing != null) {
            return sameHorizontalAxis(aboveFacing, direction) ? currentBlock : Block.AIR;
        }
        var aboveRotationProperty = aboveBlock.getProperty("rotation");

        if (aboveRotationProperty == null) {
            return Block.AIR;
        }
        var aboveDirection = segmentToDirection(Integer.parseInt(aboveRotationProperty));

        if (aboveDirection == null) {
            return Block.AIR;
        }
        return sameHorizontalAxis(aboveDirection, direction) ? currentBlock : Block.AIR;
    }

    private static boolean isHangingSign(@NotNull Block block) {
        var tag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:all_hanging_signs"));
        return tag != null && tag.contains(block);
    }

    private static boolean sameHorizontalAxis(@NotNull String facingName, @NotNull BlockFace other) {
        return switch (facingName) {
            case "north", "south" -> other == BlockFace.NORTH || other == BlockFace.SOUTH;
            case "east", "west" -> other == BlockFace.EAST || other == BlockFace.WEST;
            default -> false;
        };
    }

    private static boolean sameHorizontalAxis(@NotNull BlockFace face, @NotNull BlockFace other) {
        return switch (face) {
            case NORTH, SOUTH -> other == BlockFace.NORTH || other == BlockFace.SOUTH;
            case EAST, WEST -> other == BlockFace.EAST || other == BlockFace.WEST;
            default -> false;
        };
    }

    private static BlockFace segmentToDirection(int segment) {
        return switch (segment) {
            case 0 -> BlockFace.NORTH;
            case 4 -> BlockFace.EAST;
            case 8 -> BlockFace.SOUTH;
            case 12 -> BlockFace.WEST;
            default -> null;
        };
    }

    private static int directionToSegment(@NotNull BlockFace face) {
        return switch (face) {
            case SOUTH -> 0;
            case WEST -> 4;
            case NORTH -> 8;
            case EAST -> 12;
            default -> 0;
        };
    }
}

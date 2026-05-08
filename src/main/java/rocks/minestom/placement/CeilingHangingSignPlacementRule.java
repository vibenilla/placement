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

        if (isHangingSign(blockAbove) && !shifting) {
            var aboveFacing = blockAbove.getProperty("facing");

            if (aboveFacing != null) {
                if (sameHorizontalAxis(aboveFacing, direction)) {
                    attachedToMiddle = false;
                }
            } else {
                var aboveRotationProperty = blockAbove.getProperty("rotation");

                if (aboveRotationProperty != null) {
                    var aboveDirection = segmentToDirection(Integer.parseInt(aboveRotationProperty));

                    if (aboveDirection != null && sameHorizontalAxis(aboveDirection, direction)) {
                        attachedToMiddle = false;
                    }
                }
            }
        }

        var rotation = !attachedToMiddle
                ? directionToSegment(direction.getOppositeFace())
                : Math.round((yaw + 180.0F) * 16.0F / 360.0F) & 15;
        var waterlogged = instance.getBlock(placePosition).compare(Block.WATER);

        return this.block
                .withProperty("attached", String.valueOf(attachedToMiddle))
                .withProperty("rotation", Integer.toString(rotation))
                .withProperty("waterlogged", String.valueOf(waterlogged));
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

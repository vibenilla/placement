package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DoorPlacementRule extends BlockPlacementRule {
    public DoorPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        if (!(placementState.instance() instanceof Instance instance)) {
            return null;
        }

        var placePosition = placementState.placePosition();
        var upperPosition = placePosition.relative(BlockFace.TOP);
        var maxY = instance.getCachedDimensionType().maxY();

        if (upperPosition.blockY() >= maxY) {
            return null;
        }

        var existingUpperBlock = instance.getBlock(upperPosition);

        if (!existingUpperBlock.registry().isReplaceable()) {
            return null;
        }

        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw);
        var hinge = computeHinge(instance, placePosition, facing, placementState.cursorPosition());
        var facingName = facing.name().toLowerCase();
        // TODO: vanilla checks neighbor signal - not implemented

        var lowerBlock = this.block
                .withProperty("facing", facingName)
                .withProperty("hinge", hinge)
                .withProperty("half", "lower")
                .withProperty("powered", "false")
                .withProperty("open", "false");
        var upperBlock = this.block
                .withProperty("facing", facingName)
                .withProperty("hinge", hinge)
                .withProperty("half", "upper")
                .withProperty("powered", "false")
                .withProperty("open", "false");

        if (intersectsAnyEntity(instance, placePosition, lowerBlock)) {
            return null;
        }

        instance.setBlock(upperPosition, upperBlock, false);
        return lowerBlock;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var fromFace = updateState.fromFace();
        var currentBlock = updateState.currentBlock();
        var half = currentBlock.getProperty("half");

        if (fromFace == BlockFace.TOP && "lower".equals(half)) {
            var aboveBlock = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.TOP));
            return aboveBlock.compare(this.block) && "upper".equals(aboveBlock.getProperty("half")) ? currentBlock : Block.AIR;
        }

        if (fromFace == BlockFace.BOTTOM && "upper".equals(half)) {
            var belowBlock = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
            return belowBlock.compare(this.block) && "lower".equals(belowBlock.getProperty("half")) ? currentBlock : Block.AIR;
        }
        return currentBlock;
    }

    private static boolean intersectsAnyEntity(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Block block) {
        var collisionShape = block.registry().collisionShape();

        for (var entity : instance.getNearbyEntities(blockPosition, 3.0D)) {

            if (!entity.preventBlockPlacement()) {
                continue;
            }

            var entityPosition = entity.getPosition();

            if (collisionShape.intersectBox(entityPosition.sub(blockPosition), entity.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    private static String computeHinge(@NotNull Instance instance, @NotNull Point placePosition,
                                       @NotNull BlockFace facing, @Nullable Point cursorPosition) {
        var doorsTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:doors"));
        var leftDirection = counterClockwise(facing);
        var rightDirection = clockwise(facing);
        var upperPosition = placePosition.relative(BlockFace.TOP);
        var leftLowerPosition = placePosition.relative(leftDirection);
        var leftUpperPosition = upperPosition.relative(leftDirection);
        var rightLowerPosition = placePosition.relative(rightDirection);
        var rightUpperPosition = upperPosition.relative(rightDirection);
        var leftLowerBlock = instance.getBlock(leftLowerPosition);
        var leftUpperBlock = instance.getBlock(leftUpperPosition);
        var rightLowerBlock = instance.getBlock(rightLowerPosition);
        var rightUpperBlock = instance.getBlock(rightUpperPosition);
        var solidBlockBalance = (isFullCube(leftLowerBlock) ? -1 : 0)
                + (isFullCube(leftUpperBlock) ? -1 : 0)
                + (isFullCube(rightLowerBlock) ? 1 : 0)
                + (isFullCube(rightUpperBlock) ? 1 : 0);
        var doorLeft = isLowerDoor(leftLowerBlock, doorsTag);
        var doorRight = isLowerDoor(rightLowerBlock, doorsTag);

        if ((!doorLeft || doorRight) && solidBlockBalance <= 0) {
            if ((!doorRight || doorLeft) && solidBlockBalance >= 0) {
                if (cursorPosition == null) {
                    return "left";
                }
                var stepX = facing.toDirection().normalX();
                var stepZ = facing.toDirection().normalZ();
                var clickX = cursorPosition.x();
                var clickZ = cursorPosition.z();
                var leftHinge = (stepX >= 0 || !(clickZ < 0.5D))
                        && (stepX <= 0 || !(clickZ > 0.5D))
                        && (stepZ >= 0 || !(clickX > 0.5D))
                        && (stepZ <= 0 || !(clickX < 0.5D));
                return leftHinge ? "left" : "right";
            }
            return "left";
        }
        return "right";
    }

    private static boolean isFullCube(@NotNull Block block) {
        return block.registry().collisionShape().isFaceFull(BlockFace.TOP);
    }

    private static boolean isLowerDoor(@NotNull Block block, @Nullable RegistryTag<Block> doorsTag) {
        return doorsTag != null && doorsTag.contains(block) && "lower".equals(block.getProperty("half"));
    }

    private static BlockFace counterClockwise(@NotNull BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.WEST;
            case WEST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.EAST;
            case EAST -> BlockFace.NORTH;
            default -> face;
        };
    }

    private static BlockFace clockwise(@NotNull BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> face;
        };
    }
}

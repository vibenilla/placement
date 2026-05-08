package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;

public final class StairPlacementRule extends BlockPlacementRule {
    public StairPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw);
        var clickedFace = placementState.blockFace();
        var cursorPosition = placementState.cursorPosition();
        var cursorY = cursorPosition == null ? 0.0D : cursorPosition.y();
        var bottom = clickedFace != BlockFace.BOTTOM && (clickedFace == BlockFace.TOP || cursorY <= 0.5D);
        var half = bottom ? "bottom" : "top";
        var waterlogged = placementState.instance().getBlock(placementState.placePosition()).compare(Block.WATER);
        var shape = computeShape(placementState.instance(), placementState.placePosition(), facing, half, stairsTag());

        return this.block
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("half", half)
                .withProperty("shape", shape)
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var fromFace = updateState.fromFace();

        if (fromFace == BlockFace.TOP || fromFace == BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var currentBlock = updateState.currentBlock();
        var facingName = currentBlock.getProperty("facing");
        var half = currentBlock.getProperty("half");

        if (facingName == null || half == null) {
            return currentBlock;
        }

        var facing = BlockFace.valueOf(facingName.toUpperCase());
        var shape = computeShape(updateState.instance(), updateState.blockPosition(), facing, half, stairsTag());
        return currentBlock.withProperty("shape", shape);
    }

    private static String computeShape(@NotNull Block.Getter blockGetter, @NotNull Point position, @NotNull BlockFace facing, @NotNull String half, RegistryTag<Block> stairsTag) {
        var behindBlock = blockGetter.getBlock(position.relative(facing));

        if (isStair(behindBlock, stairsTag) && half.equals(behindBlock.getProperty("half"))) {
            var behindFacingName = behindBlock.getProperty("facing");

            if (behindFacingName != null) {
                var behindFacing = BlockFace.valueOf(behindFacingName.toUpperCase());

                if (differentAxis(behindFacing, facing) && canTakeShape(blockGetter, position, facing, half, behindFacing.getOppositeFace(), stairsTag)) {
                    if (behindFacing == counterClockwise(facing)) {
                        return "outer_left";
                    }
                    return "outer_right";
                }
            }
        }

        var frontBlock = blockGetter.getBlock(position.relative(facing.getOppositeFace()));

        if (isStair(frontBlock, stairsTag) && half.equals(frontBlock.getProperty("half"))) {
            var frontFacingName = frontBlock.getProperty("facing");

            if (frontFacingName != null) {
                var frontFacing = BlockFace.valueOf(frontFacingName.toUpperCase());

                if (differentAxis(frontFacing, facing) && canTakeShape(blockGetter, position, facing, half, frontFacing, stairsTag)) {
                    if (frontFacing == counterClockwise(facing)) {
                        return "inner_left";
                    }
                    return "inner_right";
                }
            }
        }
        return "straight";
    }

    private static boolean canTakeShape(@NotNull Block.Getter blockGetter, @NotNull Point position, @NotNull BlockFace facing, @NotNull String half, @NotNull BlockFace neighborFace, RegistryTag<Block> stairsTag) {
        var neighborBlock = blockGetter.getBlock(position.relative(neighborFace));

        if (!isStair(neighborBlock, stairsTag)) {
            return true;
        }

        var neighborFacing = neighborBlock.getProperty("facing");
        var neighborHalf = neighborBlock.getProperty("half");
        return !facing.name().toLowerCase().equals(neighborFacing) || !half.equals(neighborHalf);
    }

    private static boolean isStair(@NotNull Block candidate, RegistryTag<Block> stairsTag) {
        return stairsTag != null && stairsTag.contains(candidate);
    }

    private static RegistryTag<Block> stairsTag() {
        return MinecraftServer.process().blocks().getTag(Key.key("minecraft:stairs"));
    }

    private static boolean differentAxis(@NotNull BlockFace first, @NotNull BlockFace second) {
        return !first.isSimilar(second);
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
}

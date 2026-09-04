package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.Objects;

public final class BellPlacementRule extends BlockPlacementRule {
    public BellPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var clickedFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var horizontalFacing = BlockFace.fromYaw(yaw);

        if (clickedFace == BlockFace.TOP || clickedFace == BlockFace.BOTTOM) {
            var attachment = clickedFace == BlockFace.BOTTOM ? "ceiling" : "floor";
            var support = placementState.instance().getBlock(
                    placementState.placePosition().relative(clickedFace.getOppositeFace()));

            if (!Utility.canSupportCenter(support, clickedFace)) {
                return null;
            }

            return placementState.block()
                    .withHandler(BellBlockHandler.INSTANCE)
                    .withProperty("attachment", attachment)
                    .withProperty("facing", horizontalFacing.name().toLowerCase());
        }

        var facing = clickedFace.getOppositeFace();
        var support = placementState.instance().getBlock(placementState.placePosition().relative(facing));

        if (!Utility.canSupportCenter(support, clickedFace)) {
            return null;
        }

        var doubleAttached = isDoubleAttached(placementState.instance(), placementState.placePosition(), clickedFace);
        var attachment = doubleAttached ? "double_wall" : "single_wall";

        return placementState.block()
                .withHandler(BellBlockHandler.INSTANCE)
                .withProperty("attachment", attachment)
                .withProperty("facing", facing.name().toLowerCase());
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var attachment = currentBlock.getProperty("attachment");
        var supportDirection = switch (attachment) {
            case "floor" -> BlockFace.BOTTOM;
            case "ceiling" -> BlockFace.TOP;
            case "single_wall", "double_wall" -> parseFacing(currentBlock.getProperty("facing"));
            case null, default -> null;
        };

        if (supportDirection == null) {
            return currentBlock;
        }

        var support = updateState.instance().getBlock(updateState.blockPosition().relative(supportDirection));
        var supportFace = "floor".equals(attachment) ? BlockFace.TOP
                : "ceiling".equals(attachment) ? BlockFace.BOTTOM : supportDirection.getOppositeFace();

        if (updateState.fromFace() == supportDirection && !Utility.canSupportCenter(support, supportFace)) {
            return Block.AIR;
        }

        if (("single_wall".equals(attachment) || "double_wall".equals(attachment))
                && isHorizontal(updateState.fromFace())) {
            var doubleAttached = isDoubleAttached(updateState.instance(), updateState.blockPosition(), supportDirection.getOppositeFace());
            return currentBlock.withProperty("attachment", doubleAttached ? "double_wall" : "single_wall");
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

    private static boolean isDoubleAttached(Block.Getter blockGetter, Point position, BlockFace clickedFace) {
        if (clickedFace == BlockFace.WEST || clickedFace == BlockFace.EAST) {
            var westNeighbor = blockGetter.getBlock(position.relative(BlockFace.WEST));
            var eastNeighbor = blockGetter.getBlock(position.relative(BlockFace.EAST));
            return westNeighbor.registry().collisionShape().isFaceFull(BlockFace.EAST)
                    && eastNeighbor.registry().collisionShape().isFaceFull(BlockFace.WEST);
        }

        if (clickedFace == BlockFace.NORTH || clickedFace == BlockFace.SOUTH) {
            var northNeighbor = blockGetter.getBlock(position.relative(BlockFace.NORTH));
            var southNeighbor = blockGetter.getBlock(position.relative(BlockFace.SOUTH));
            return northNeighbor.registry().collisionShape().isFaceFull(BlockFace.SOUTH)
                    && southNeighbor.registry().collisionShape().isFaceFull(BlockFace.NORTH);
        }

        return false;
    }

    private static boolean isHorizontal(BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH
                || face == BlockFace.EAST || face == BlockFace.WEST;
    }
}

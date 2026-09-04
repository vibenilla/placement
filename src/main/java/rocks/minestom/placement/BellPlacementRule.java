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

            if (!canSurvive(support, clickedFace.getOppositeFace(), attachment)) {
                return null;
            }

            return placementState.block()
                    .withHandler(BellBlockHandler.INSTANCE)
                    .withProperty("attachment", attachment)
                    .withProperty("facing", horizontalFacing.name().toLowerCase());
        }

        var facing = clickedFace.getOppositeFace();
        var support = placementState.instance().getBlock(placementState.placePosition().relative(facing));

        var doubleAttached = isDoubleAttached(placementState.instance(), placementState.placePosition(), clickedFace);
        var attachment = doubleAttached ? "double_wall" : "single_wall";
        var result = placementState.block()
                .withHandler(BellBlockHandler.INSTANCE)
                .withProperty("attachment", attachment)
                .withProperty("facing", facing.name().toLowerCase());

        if (canSurvive(support, facing, attachment)) {
            return result;
        }

        var below = placementState.instance().getBlock(placementState.placePosition().relative(BlockFace.BOTTOM));
        attachment = Utility.canSupportRigidBlock(below, BlockFace.TOP) ? "floor" : "ceiling";
        var fallbackDirection = "floor".equals(attachment) ? BlockFace.BOTTOM : BlockFace.TOP;
        var fallbackSupport = placementState.instance().getBlock(placementState.placePosition().relative(fallbackDirection));

        if (!canSurvive(fallbackSupport, fallbackDirection, attachment)) {
            return null;
        }

        return result.withProperty("attachment", attachment);
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
        if (updateState.fromFace() == supportDirection
                && !"double_wall".equals(attachment)
                && !canSurvive(support, supportDirection, attachment)) {
            return Block.AIR;
        }

        var facing = parseFacing(currentBlock.getProperty("facing"));
        var fromFace = updateState.fromFace();

        if (facing != null && isHorizontal(fromFace) && facing.isSimilar(fromFace)) {
            var neighbor = updateState.instance().getBlock(updateState.blockPosition().relative(fromFace));

            if ("double_wall".equals(attachment)
                    && !Utility.canSupportRigidBlock(neighbor, fromFace.getOppositeFace())) {
                return currentBlock
                        .withProperty("attachment", "single_wall")
                        .withProperty("facing", fromFace.getOppositeFace().name().toLowerCase());
            }

            if ("single_wall".equals(attachment)
                    && supportDirection.getOppositeFace() == fromFace
                    && Utility.canSupportRigidBlock(neighbor, facing)) {
                return currentBlock.withProperty("attachment", "double_wall");
            }
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
            return Utility.canSupportRigidBlock(westNeighbor, BlockFace.EAST)
                    && Utility.canSupportRigidBlock(eastNeighbor, BlockFace.WEST);
        }

        if (clickedFace == BlockFace.NORTH || clickedFace == BlockFace.SOUTH) {
            var northNeighbor = blockGetter.getBlock(position.relative(BlockFace.NORTH));
            var southNeighbor = blockGetter.getBlock(position.relative(BlockFace.SOUTH));
            return Utility.canSupportRigidBlock(northNeighbor, BlockFace.SOUTH)
                    && Utility.canSupportRigidBlock(southNeighbor, BlockFace.NORTH);
        }

        return false;
    }

    private static boolean canSurvive(Block support, BlockFace supportDirection, String attachment) {
        var supportFace = supportDirection.getOppositeFace();
        return "ceiling".equals(attachment)
                ? Utility.canSupportCenter(support, supportFace)
                : Utility.canSupportRigidBlock(support, supportFace);
    }

    private static boolean isHorizontal(BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH
                || face == BlockFace.EAST || face == BlockFace.WEST;
    }
}

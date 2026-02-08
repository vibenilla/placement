package rocks.minestom.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChestPlacementRule extends BlockPlacementRule {
    public ChestPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var facing = this.getFacingDirection(playerPosition);
        var chestType = "single";

        // If player is sneaking, don't auto-connect to adjacent chests
        if (!placementState.isPlayerShifting()) {
            var instance = placementState.instance();
            var blockPosition = placementState.placePosition();

            // Check for adjacent chests to form double chest
            chestType = this.getChestType(instance, blockPosition, facing);
        }

        var handler = MinecraftServer.getBlockManager().getHandler(this.block.key().asString());
        var placedBlock = this.block
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("type", chestType)
                .withProperty("waterlogged", "false");

        if (handler != null) {
            placedBlock = placedBlock.withHandler(handler);
        }

        return placedBlock;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var blockPosition = updateState.blockPosition();
        var instance = updateState.instance();
        var fromFace = updateState.fromFace();

        var facing = currentBlock.getProperty("facing");
        var currentType = currentBlock.getProperty("type");

        if (facing == null) {
            return currentBlock;
        }

        var fromDirection = this.blockFaceToDirection(fromFace);
        if (fromDirection == null) {
            return currentBlock;
        }

        // Check if a neighbor chest in the fromFace direction wants to connect
        if (this.isHorizontal(fromDirection) && "single".equals(currentType)) {
            var neighborBlock = instance.getBlock(blockPosition.add(fromDirection.normalX(), fromDirection.normalY(), fromDirection.normalZ()));

            if (Block.CHEST.compare(neighborBlock)) {
                var neighborType = neighborBlock.getProperty("type");
                var neighborFacing = neighborBlock.getProperty("facing");

                // Only connect if neighbor is already part of a double chest (LEFT or RIGHT)
                // and has the same facing, and points back at us
                if (!"single".equals(neighborType) && facing.equals(neighborFacing)) {
                    var neighborDirection = this.getConnectedDirection(neighborFacing, neighborType);
                    if (neighborDirection == this.getOppositeDirection(fromDirection)) {
                        var newType = "left".equals(neighborType) ? "right" : "left";
                        var updatedBlock = currentBlock.withProperty("type", newType);

                        var handler = currentBlock.handler();
                        if (handler != null) {
                            updatedBlock = updatedBlock.withHandler(handler);
                        }

                        return updatedBlock;
                    }
                }
            }
        }

        // Check if we should revert to single (neighbor was removed)
        if (!"single".equals(currentType)) {
            var connectedDir = this.getConnectedDirection(facing, currentType);
            if (connectedDir == fromDirection) {
                var updatedBlock = currentBlock.withProperty("type", "single");

                var handler = currentBlock.handler();
                if (handler != null) {
                    updatedBlock = updatedBlock.withHandler(handler);
                }

                return updatedBlock;
            }
        }

        return currentBlock;
    }

    @Nullable
    private Direction blockFaceToDirection(net.minestom.server.instance.block.BlockFace blockFace) {
        return switch (blockFace) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case EAST -> Direction.EAST;
            case WEST -> Direction.WEST;
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
        };
    }

    private boolean isHorizontal(Direction direction) {
        return direction == Direction.NORTH || direction == Direction.SOUTH
                || direction == Direction.EAST || direction == Direction.WEST;
    }

    private Direction getOppositeDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
        };
    }

    @Nullable
    private Direction getConnectedDirection(String facingStr, String chestType) {
        var facing = Direction.valueOf(facingStr.toUpperCase());

        if ("left".equals(chestType)) {
            return this.getClockwiseDirection(facing);
        } else if ("right".equals(chestType)) {
            return this.getCounterClockwiseDirection(facing);
        }

        return null;
    }

    private String getChestType(Block.Getter instance, Point blockPosition, Direction facing) {
        var clockwiseDirection = this.getClockwiseDirection(facing);
        var candidateFacing = this.candidatePartnerFacing(instance, blockPosition, clockwiseDirection);

        if (candidateFacing != null && candidateFacing == facing) {
            return "left";
        }

        var counterClockwiseDirection = this.getCounterClockwiseDirection(facing);
        candidateFacing = this.candidatePartnerFacing(instance, blockPosition, counterClockwiseDirection);

        if (candidateFacing != null && candidateFacing == facing) {
            return "right";
        }

        return "single";
    }

    @Nullable
    private Direction candidatePartnerFacing(Block.Getter instance, Point blockPosition, Direction direction) {
        var adjacentPosition = blockPosition.add(direction.normalX(), direction.normalY(), direction.normalZ());
        var adjacentBlock = instance.getBlock(adjacentPosition);

        // Check if the adjacent block is a chest using Block.compare
        if (!Block.CHEST.compare(adjacentBlock)) {
            return null;
        }

        var adjacentType = adjacentBlock.getProperty("type");
        if (!"single".equals(adjacentType)) {
            return null;
        }

        var adjacentFacing = adjacentBlock.getProperty("facing");
        if (adjacentFacing == null) {
            return null;
        }

        return Direction.valueOf(adjacentFacing.toUpperCase());
    }

    private Direction getFacingDirection(@Nullable Pos position) {
        if (position == null) {
            return Direction.NORTH;
        }

        return MathUtils.getHorizontalDirection(position.yaw()).opposite();
    }

    private Direction getClockwiseDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> direction;
        };
    }

    private Direction getCounterClockwiseDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            default -> direction;
        };
    }
}

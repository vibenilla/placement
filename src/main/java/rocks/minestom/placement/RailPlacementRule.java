package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class RailPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:rails");

    private final boolean isStraightRail;

    public RailPlacementRule(@NotNull Block block) {
        super(block);
        this.isStraightRail = block.compare(Block.POWERED_RAIL)
                || block.compare(Block.DETECTOR_RAIL)
                || block.compare(Block.ACTIVATOR_RAIL);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var placePosition = placementState.placePosition();
        var instance = placementState.instance();
        var playerPosition = Objects.requireNonNullElse(placementState.playerPosition(), Pos.ZERO);

        var blockX = placePosition.blockX();
        var blockY = placePosition.blockY();
        var blockZ = placePosition.blockZ();

        var blockBelow = instance.getBlock(blockX, blockY - 1, blockZ);
        if (!this.canSupportRail(blockBelow)) {
            return null;
        }

        var facingDirection = BlockFace.fromYaw(playerPosition.yaw());
        var initialShape = switch (facingDirection) {
            case NORTH, SOUTH -> "north_south";
            case EAST, WEST -> "east_west";
            default -> "north_south";
        };

        var shape = this.calculateRailShape(instance, blockX, blockY, blockZ, initialShape);

        var block = this.block.withProperty("shape", shape);
        block = block.withProperty("waterlogged", String.valueOf(
                instance.getBlock(blockX, blockY, blockZ).compare(Block.WATER)));

        if (this.isStraightRail) {
            block = block.withProperty("powered", "false");
        }

        if (instance instanceof Instance inst) {
            var finalShape = shape;
            inst.scheduleNextTick(currentInstance -> {
                this.updateNeighborRails(currentInstance, blockX, blockY, blockZ);
            });
        }

        return block;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var blockPosition = updateState.blockPosition();
        var instance = updateState.instance();

        var blockX = blockPosition.blockX();
        var blockY = blockPosition.blockY();
        var blockZ = blockPosition.blockZ();

        var blockBelow = instance.getBlock(blockX, blockY - 1, blockZ);
        if (!this.canSupportRail(blockBelow)) {
            return Block.AIR;
        }

        var currentBlock = updateState.currentBlock();
        var shape = currentBlock.getProperty("shape");

        if (shape != null && shape.startsWith("ascending_")) {
            var hasSupport = switch (shape) {
                case "ascending_east" -> {
                    var eastBlock = instance.getBlock(blockX + 1, blockY, blockZ);
                    yield this.canSupportRail(eastBlock);
                }
                case "ascending_west" -> {
                    var westBlock = instance.getBlock(blockX - 1, blockY, blockZ);
                    yield this.canSupportRail(westBlock);
                }
                case "ascending_north" -> {
                    var northBlock = instance.getBlock(blockX, blockY, blockZ - 1);
                    yield this.canSupportRail(northBlock);
                }
                case "ascending_south" -> {
                    var southBlock = instance.getBlock(blockX, blockY, blockZ + 1);
                    yield this.canSupportRail(southBlock);
                }
                default -> true;
            };

            if (!hasSupport) {
                return Block.AIR;
            }
        }

        var newShape = this.calculateRailShape(instance, blockX, blockY, blockZ, shape != null ? shape : "north_south");
        return currentBlock.withProperty("shape", newShape);
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private String calculateRailShape(Block.Getter instance, int x, int y, int z, String currentShape) {
        var hasNorth = this.hasNeighborRail(instance, x, y, z - 1);
        var hasSouth = this.hasNeighborRail(instance, x, y, z + 1);
        var hasWest = this.hasNeighborRail(instance, x - 1, y, z);
        var hasEast = this.hasNeighborRail(instance, x + 1, y, z);

        var hasNorthSouth = hasNorth || hasSouth;
        var hasEastWest = hasWest || hasEast;

        String shape = null;

        if (hasNorthSouth && !hasEastWest) {
            shape = "north_south";
        }

        if (hasEastWest && !hasNorthSouth) {
            shape = "east_west";
        }

        var hasSouthEast = hasSouth && hasEast;
        var hasSouthWest = hasSouth && hasWest;
        var hasNorthEast = hasNorth && hasEast;
        var hasNorthWest = hasNorth && hasWest;

        if (!this.isStraightRail) {
            if (hasSouthEast && !hasNorth && !hasWest) {
                shape = "south_east";
            }

            if (hasSouthWest && !hasNorth && !hasEast) {
                shape = "south_west";
            }

            if (hasNorthWest && !hasSouth && !hasEast) {
                shape = "north_west";
            }

            if (hasNorthEast && !hasSouth && !hasWest) {
                shape = "north_east";
            }
        }

        if (shape == null) {
            if (hasNorthSouth && hasEastWest) {
                shape = currentShape;
            } else if (hasNorthSouth) {
                shape = "north_south";
            } else if (hasEastWest) {
                shape = "east_west";
            } else {
                shape = currentShape;
            }
        }

        if (shape.equals("north_south")) {
            if (this.isRail(instance, x, y, z - 1, 1)) {
                shape = "ascending_north";
            }

            if (this.isRail(instance, x, y, z + 1, 1)) {
                shape = "ascending_south";
            }
        }

        if (shape.equals("east_west")) {
            if (this.isRail(instance, x + 1, y, z, 1)) {
                shape = "ascending_east";
            }

            if (this.isRail(instance, x - 1, y, z, 1)) {
                shape = "ascending_west";
            }
        }

        return shape;
    }

    private boolean hasNeighborRail(Block.Getter instance, int x, int y, int z) {
        return this.isRail(instance, x, y, z, 0)
                || this.isRail(instance, x, y, z, 1)
                || this.isRail(instance, x, y, z, -1);
    }

    private boolean isRail(Block.Getter instance, int x, int y, int z, int yOffset) {
        var block = instance.getBlock(x, y + yOffset, z);
        return block.compare(Block.RAIL)
                || block.compare(Block.POWERED_RAIL)
                || block.compare(Block.DETECTOR_RAIL)
                || block.compare(Block.ACTIVATOR_RAIL);
    }

    private void updateNeighborRails(Instance instance, int x, int y, int z) {
        this.updateRailAt(instance, x, y, z - 1);
        this.updateRailAt(instance, x, y, z + 1);
        this.updateRailAt(instance, x - 1, y, z);
        this.updateRailAt(instance, x + 1, y, z);
    }

    private void updateRailAt(Instance instance, int x, int y, int z) {
        for (var yOffset = -1; yOffset <= 1; yOffset++) {
            var block = instance.getBlock(x, y + yOffset, z);
            if (!this.isRail(instance, x, y, z, yOffset)) {
                continue;
            }

            var currentShape = block.getProperty("shape");
            if (currentShape == null) {
                continue;
            }

            var newShape = this.calculateRailShape(instance, x, y + yOffset, z, currentShape);
            if (!newShape.equals(currentShape)) {
                instance.setBlock(x, y + yOffset, z, block.withProperty("shape", newShape), false);
            }
        }
    }

    private boolean canSupportRail(@NotNull Block block) {
        return !block.isAir() && block.registry().isSolid();
    }
}

package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChestPlacementRule extends BlockPlacementRule {
    public ChestPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw).getOppositeFace();
        var clickedFace = placementState.blockFace();
        var shifting = placementState.isPlayerShifting();
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        var type = "single";

        if (shifting && clickedFace != null && clickedFace != BlockFace.TOP && clickedFace != BlockFace.BOTTOM) {
            var neighborFacing = this.candidatePartnerFacing(instance, placePosition, clickedFace.getOppositeFace());

            if (neighborFacing != null && !neighborFacing.isSimilar(clickedFace)) {
                facing = neighborFacing;
                type = counterClockwise(neighborFacing) == clickedFace.getOppositeFace() ? "right" : "left";
            }
        }

        if ("single".equals(type) && !shifting) {
            type = this.getChestType(instance, placePosition, facing);
        }

        return this.block
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("type", type)
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    private @NotNull String getChestType(@NotNull Block.Getter blockGetter, @NotNull Point position, @NotNull BlockFace facing) {
        if (facing == this.candidatePartnerFacing(blockGetter, position, clockwise(facing))) {
            return "left";
        }

        return facing == this.candidatePartnerFacing(blockGetter, position, counterClockwise(facing)) ? "right" : "single";
    }

    private @Nullable BlockFace candidatePartnerFacing(@NotNull Block.Getter blockGetter, @NotNull Point position, @NotNull BlockFace neighborDirection) {
        var neighborBlock = blockGetter.getBlock(position.relative(neighborDirection));

        if (!neighborBlock.compare(this.block)) {
            return null;
        }

        var neighborType = neighborBlock.getProperty("type");

        if (!"single".equals(neighborType)) {
            return null;
        }

        var neighborFacing = neighborBlock.getProperty("facing");

        if (neighborFacing == null) {
            return null;
        }

        return BlockFace.valueOf(neighborFacing.toUpperCase());
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

package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.Objects;

public final class LightningRodPlacementRule extends BlockPlacementRule {
    public LightningRodPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var facing = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var placePosition = placementState.placePosition();
        var support = placementState.instance().getBlock(placePosition.relative(facing.getOppositeFace()));

        if (!Utility.canSupportCenter(support, facing)) {
            return null;
        }

        var replaced = placementState.instance().getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));

        return placementState.block()
                .withProperty("facing", facingName(facing))
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var facing = parseFacing(currentBlock.getProperty("facing"));

        if (facing == null || updateState.fromFace() != facing.getOppositeFace()) {
            return currentBlock;
        }

        var support = updateState.instance().getBlock(
                updateState.blockPosition().relative(facing.getOppositeFace()));
        return Utility.canSupportCenter(support, facing) ? currentBlock : Block.AIR;
    }

    private static BlockFace parseFacing(String facingName) {
        return switch (facingName) {
            case "up" -> BlockFace.TOP;
            case "down" -> BlockFace.BOTTOM;
            case "north" -> BlockFace.NORTH;
            case "east" -> BlockFace.EAST;
            case "south" -> BlockFace.SOUTH;
            case "west" -> BlockFace.WEST;
            case null, default -> null;
        };
    }

    private static String facingName(BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

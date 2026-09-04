package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.Objects;

public final class EndRodPlacementRule extends BlockPlacementRule {
    public EndRodPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var clickedFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var placePosition = placementState.placePosition();
        var againstPosition = placePosition.relative(clickedFace.getOppositeFace());
        var againstBlock = placementState.instance().getBlock(againstPosition);

        if (!Utility.canSupportCenter(againstBlock, clickedFace)) {
            return null;
        }

        var againstFacing = againstBlock.getProperty("facing");
        var facing = clickedFace;

        if (againstBlock.compare(placementState.block()) && facingName(clickedFace).equals(againstFacing)) {
            facing = clickedFace.getOppositeFace();
        }

        return placementState.block().withProperty("facing", facingName(facing));
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

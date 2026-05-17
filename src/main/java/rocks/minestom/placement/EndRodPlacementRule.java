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
        var againstFacing = againstBlock.getProperty("facing");
        var facing = clickedFace;

        if (againstBlock.compare(placementState.block()) && facingName(clickedFace).equals(againstFacing)) {
            facing = clickedFace.getOppositeFace();
        }

        return placementState.block().withProperty("facing", facingName(facing));
    }

    private static String facingName(BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

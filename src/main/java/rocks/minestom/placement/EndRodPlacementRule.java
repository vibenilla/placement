package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class EndRodPlacementRule extends BlockPlacementRule {
    public EndRodPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var clickedFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var placePosition = placementState.placePosition();
        var againstPosition = placePosition.relative(clickedFace.getOppositeFace());
        var againstBlock = placementState.instance().getBlock(againstPosition);
        var againstFacing = againstBlock.getProperty("facing");
        var facing = clickedFace;

        if (againstBlock.compare(this.block) && facingName(clickedFace).equals(againstFacing)) {
            facing = clickedFace.getOppositeFace();
        }

        return this.block.withProperty("facing", facingName(facing));
    }

    private static String facingName(@NotNull BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

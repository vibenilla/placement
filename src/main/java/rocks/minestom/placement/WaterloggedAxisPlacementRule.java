package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class WaterloggedAxisPlacementRule extends BlockPlacementRule {
    public WaterloggedAxisPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var axis = switch (blockFace) {
            case WEST, EAST -> "x";
            case SOUTH, NORTH -> "z";
            case TOP, BOTTOM -> "y";
        };
        var waterlogged = placementState.instance().getBlock(placementState.placePosition()).compare(Block.WATER);

        return this.block
                .withProperty("axis", axis)
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }
}

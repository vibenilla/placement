package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Code from <a href="https://github.com/vibenilla/placement">vibenilla placement</a>
 * Licensed under Apache License 2.0.
 */

public final class AxisPlacementRule extends BlockPlacementRule {
    public AxisPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);

        return this.block.withProperty("axis", switch (blockFace) {
            case WEST, EAST -> "x";
            case SOUTH, NORTH -> "z";
            case TOP, BOTTOM -> "y";
        });
    }
}

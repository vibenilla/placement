package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import rocks.minestom.placement.properties.enums.FacingXZ;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class GrindstonePlacementRule extends BlockPlacementRule {
    public GrindstonePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var placeFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var playerPosition = Objects.requireNonNullElse(placementState.playerPosition(), Pos.ZERO);
        String facing = FacingXZ.fromLook(playerPosition).name().toLowerCase();
        return switch (placeFace) {
            case NORTH, SOUTH, EAST, WEST -> block
                    .withProperty("face", "wall")
                    .withProperty("facing", placeFace.name().toLowerCase());
            case TOP -> block
                    .withProperty("face", "floor")
                    .withProperty("facing", facing);
            case BOTTOM -> block
                    .withProperty("face", "ceiling")
                    .withProperty("facing", facing);
        };
    }
}

package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import rocks.minestom.placement.properties.enums.FacingXZ;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class JigsawPlacementRule extends BlockPlacementRule {
    public JigsawPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placement) {
        String orientation = "up_east";
        if (placement.playerPosition() != null && placement.blockFace() != null) {
            String horizontal = FacingXZ.fromLook(placement.playerPosition()).opposite().name().toLowerCase(Locale.ROOT);
            if (placement.blockFace() == BlockFace.TOP || placement.blockFace() == BlockFace.BOTTOM) {
                String vertical = placement.blockFace() == BlockFace.BOTTOM ? "down" : "up";
                orientation = vertical + "_" + horizontal;
            } else {
                orientation = horizontal + "_up";
            }
        }
        return block.withProperty("orientation", orientation);
    }
}

package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

public final class FirePlacementRule extends BlockPlacementRule {
    public FirePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placement) {
        var instance = placement.instance();
        var blockPosition = placement.placePosition();
        var placeFace = Objects.requireNonNullElse(placement.blockFace(), BlockFace.TOP);

        var belowBlock = instance.getBlock(blockPosition.add(0, -1, 0));
        if (placement.blockFace() == BlockFace.TOP || belowBlock.isSolid()) return block;

        var faceProperty = switch (placeFace) {
            case NORTH, SOUTH, EAST, WEST -> placeFace.getOppositeFace().name().toLowerCase(Locale.ROOT);
            default -> "up";
        };
        return block.withProperty(faceProperty, "true");
    }

    @Override
    public boolean isSelfReplaceable(@NotNull Replacement replacement) {
        return true;
    }
}

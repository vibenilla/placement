package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;

public final class PaleMossCarpetPlacementRule extends BlockPlacementRule {
    private static final BlockFace[] HORIZONTAL = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public PaleMossCarpetPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        return computeState(placementState.instance(), placementState.placePosition(), this.block);
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        return computeState(updateState.instance(), updateState.blockPosition(), updateState.currentBlock());
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private @NotNull Block computeState(@NotNull Block.Getter instance, @NotNull Point blockPosition, @NotNull Block block) {
        var above = instance.getBlock(blockPosition.relative(BlockFace.TOP));
        boolean isAboveCarpet = above.id() == Block.PALE_MOSS_CARPET.id();

        var isAnyAttached = false;
        var newProperties = new HashMap<String, String>();
        for (var face : HORIZONTAL) {
            var adjacent = instance.getBlock(blockPosition.relative(face));
            var isLow = adjacent.registry().collisionShape().isFaceFull(face.getOppositeFace());
            isAnyAttached |= isLow;
            if (!isLow) {
                newProperties.put(face.name().toLowerCase(Locale.ROOT), "none");
                continue;
            }

            if (isAboveCarpet) {
                var aboveSide = instance.getBlock(blockPosition.relative(face).relative(BlockFace.TOP));
                if (aboveSide.registry().collisionShape().isFaceFull(face.getOppositeFace())) {
                    newProperties.put(face.name().toLowerCase(Locale.ROOT), "tall");
                    continue;
                }
            }

            newProperties.put(face.name().toLowerCase(Locale.ROOT), "low");
        }

        var below = instance.getBlock(blockPosition.relative(BlockFace.BOTTOM));
        newProperties.put("bottom", !below.isAir() || !isAnyAttached ? "true" : "false");

        return block.withProperties(newProperties);
    }
}

package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import rocks.minestom.placement.handlers.FacingXZBlock;
import rocks.minestom.placement.properties.enums.FacingXZ;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SmallDripleafPlacementRule extends BlockPlacementRule {
    public SmallDripleafPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        if (updateState.fromFace() != BlockFace.TOP) return currentBlock;

        var blockAbove = updateState.instance().getBlock(updateState.blockPosition().add(0, 1, 0));
        if (blockAbove.id() == this.block.id()) {
            return currentBlock.withProperty("half", "lower");
        }

        return currentBlock;
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var posBelow = placementState.placePosition().add(0, -1, 0);
        var blockBelow = placementState.instance().getBlock(posBelow);
        if (blockBelow.id() == this.block.id()) {
            return FacingXZBlock.FACING_XZ.get(FacingXZ.valueOf(blockBelow.getProperty("facing").toUpperCase()))
                    .on(this.block.withProperty("half", "upper"));
        }

        var posAbove = placementState.placePosition().add(0, 1, 0);
        var blockAbove = placementState.instance().getBlock(posAbove);
        if (blockAbove.id() == this.block.id()) {
            return FacingXZBlock.FACING_XZ.get(FacingXZ.valueOf(blockAbove.getProperty("facing").toUpperCase()))
                    .on(this.block.withProperty("half", "upper"));
        }

        FacingXZ facing = FacingXZ.fromLook(placementState.playerPosition()).opposite();
        return FacingXZBlock.FACING_XZ.get(facing).on(this.block.withProperty("half", "upper"));
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }
}

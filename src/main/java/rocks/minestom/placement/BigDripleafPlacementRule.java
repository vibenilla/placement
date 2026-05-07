package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import rocks.minestom.placement.handlers.FacingXZBlock;
import rocks.minestom.placement.handlers.WaterBlock;
import rocks.minestom.placement.properties.enums.FacingXZ;
import rocks.minestom.placement.utils.PlacementUtils;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class BigDripleafPlacementRule extends BlockPlacementRule {
    public BigDripleafPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        if (updateState.fromFace() != BlockFace.TOP) return currentBlock;

        var posAbove = updateState.blockPosition().add(0, 1, 0);
        var blockAbove = updateState.instance().getBlock(posAbove);
        if (blockAbove.id() == Block.BIG_DRIPLEAF.id() || blockAbove.id() == Block.BIG_DRIPLEAF_STEM.id()) {
            var worldBlock = updateState.instance().getBlock(updateState.blockPosition());
            Block stem = WaterBlock.WATERLOGGED.get(WaterBlock.WATERLOGGED.is(worldBlock)).on(Block.BIG_DRIPLEAF_STEM);
            return FacingXZBlock.FACING_XZ.get(FacingXZ.valueOf(currentBlock.getProperty("facing").toUpperCase())).on(stem);
        }

        return currentBlock;
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        Block block = PlacementUtils.waterlogged(placementState);

        var posBelow = placementState.placePosition().add(0, -1, 0);
        var blockBelow = placementState.instance().getBlock(posBelow);
        if (blockBelow.id() == Block.BIG_DRIPLEAF.id() || blockBelow.id() == Block.BIG_DRIPLEAF_STEM.id()) {
            return FacingXZBlock.FACING_XZ.get(FacingXZ.valueOf(blockBelow.getProperty("facing").toUpperCase())).on(block);
        }

        var posAbove = placementState.placePosition().add(0, 1, 0);
        var blockAbove = placementState.instance().getBlock(posAbove);
        if (blockAbove.id() == Block.BIG_DRIPLEAF.id() || blockAbove.id() == Block.BIG_DRIPLEAF_STEM.id()) {
            return FacingXZBlock.FACING_XZ.get(FacingXZ.valueOf(blockAbove.getProperty("facing").toUpperCase())).on(block);
        }

        FacingXZ facing = FacingXZ.fromLook(placementState.playerPosition()).opposite();
        return FacingXZBlock.FACING_XZ.get(facing).on(block);
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }
}

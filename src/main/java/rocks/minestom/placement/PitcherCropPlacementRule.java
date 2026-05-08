package rocks.minestom.placement;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class PitcherCropPlacementRule extends BlockPlacementRule {
    public PitcherCropPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        if (!(placementState.instance() instanceof Instance instance)) {
            return null;
        }

        var placePosition = placementState.placePosition();
        var upperPosition = placePosition.relative(BlockFace.TOP);
        var maxY = instance.getCachedDimensionType().maxY();

        if (upperPosition.blockY() >= maxY) {
            return null;
        }

        var existingUpperBlock = instance.getBlock(upperPosition);

        if (!existingUpperBlock.registry().isReplaceable()) {
            return null;
        }

        var upperBlock = this.block
                .withProperty("age", "0")
                .withProperty("half", "upper");
        instance.setBlock(upperPosition, upperBlock, false);

        return this.block
                .withProperty("age", "0")
                .withProperty("half", "lower");
    }
}

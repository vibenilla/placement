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

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var half = currentBlock.getProperty("half");
        var fromFace = updateState.fromFace();

        if ("lower".equals(half) && fromFace == BlockFace.TOP) {
            var aboveBlock = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.TOP));
            return aboveBlock.compare(this.block) && "upper".equals(aboveBlock.getProperty("half")) ? currentBlock : Block.AIR;
        }

        if ("upper".equals(half) && fromFace == BlockFace.BOTTOM) {
            var belowBlock = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
            return belowBlock.compare(this.block) && "lower".equals(belowBlock.getProperty("half")) ? currentBlock : Block.AIR;
        }
        return currentBlock;
    }
}

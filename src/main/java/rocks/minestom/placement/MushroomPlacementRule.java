package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MushroomPlacementRule extends BlockPlacementRule {
    public MushroomPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var placePosition = placementState.placePosition();
        var instance = placementState.instance();

        var blockX = placePosition.blockX();
        var blockY = placePosition.blockY();
        var blockZ = placePosition.blockZ();

        var currentBlock = instance.getBlock(blockX, blockY, blockZ);
        if (!currentBlock.isAir() && !currentBlock.registry().isReplaceable()) {
            return null;
        }

        var blockBelow = instance.getBlock(blockX, blockY - 1, blockZ);
        if (!this.canPlaceOn(blockBelow)) {
            return null;
        }

        return this.block;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var blockPosition = updateState.blockPosition();
        var instance = updateState.instance();

        var blockX = blockPosition.blockX();
        var blockY = blockPosition.blockY();
        var blockZ = blockPosition.blockZ();

        var blockBelow = instance.getBlock(blockX, blockY - 1, blockZ);
        if (!this.canPlaceOn(blockBelow)) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private boolean canPlaceOn(@NotNull Block block) {
        return block.compare(Block.MYCELIUM)
                || block.compare(Block.PODZOL)
                || block.compare(Block.CRIMSON_NYLIUM)
                || block.compare(Block.WARPED_NYLIUM)
                || block.registry().isSolid();
    }
}

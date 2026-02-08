package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CactusPlacementRule extends BlockPlacementRule {
    public CactusPlacementRule(@NotNull Block block) {
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

        if (!this.canSurvive(instance, blockX, blockY, blockZ)) {
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

        if (!this.canSurvive(instance, blockX, blockY, blockZ)) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private boolean canSurvive(@NotNull Block.Getter instance, int blockX, int blockY, int blockZ) {
        var north = instance.getBlock(blockX, blockY, blockZ - 1);
        var south = instance.getBlock(blockX, blockY, blockZ + 1);
        var west = instance.getBlock(blockX - 1, blockY, blockZ);
        var east = instance.getBlock(blockX + 1, blockY, blockZ);

        if (north.registry().isSolid() || south.registry().isSolid() || west.registry().isSolid() || east.registry().isSolid()) {
            return false;
        }

        if (north.compare(Block.LAVA) || south.compare(Block.LAVA) || west.compare(Block.LAVA) || east.compare(Block.LAVA)) {
            return false;
        }

        var blockBelow = instance.getBlock(blockX, blockY - 1, blockZ);
        if (!blockBelow.compare(Block.CACTUS) && !blockBelow.compare(Block.SAND) && !blockBelow.compare(Block.RED_SAND)) {
            return false;
        }

        var blockAbove = instance.getBlock(blockX, blockY + 1, blockZ);
        if (blockAbove.isLiquid()) {
            return false;
        }

        return true;
    }
}

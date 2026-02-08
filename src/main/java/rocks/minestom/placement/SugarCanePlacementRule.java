package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SugarCanePlacementRule extends BlockPlacementRule {
    public SugarCanePlacementRule(@NotNull Block block) {
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

        if (blockBelow.compare(Block.SUGAR_CANE)) {
            return this.block;
        }

        if (!this.isValidSoil(blockBelow)) {
            return null;
        }

        if (!this.hasWaterAdjacent(instance, blockX, blockY - 1, blockZ)) {
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

        if (blockBelow.compare(Block.SUGAR_CANE)) {
            return updateState.currentBlock();
        }

        if (!this.isValidSoil(blockBelow)) {
            return Block.AIR;
        }

        if (!this.hasWaterAdjacent(instance, blockX, blockY - 1, blockZ)) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private boolean isValidSoil(@NotNull Block block) {
        return block.compare(Block.DIRT)
                || block.compare(Block.GRASS_BLOCK)
                || block.compare(Block.PODZOL)
                || block.compare(Block.COARSE_DIRT)
                || block.compare(Block.MYCELIUM)
                || block.compare(Block.ROOTED_DIRT)
                || block.compare(Block.MOSS_BLOCK)
                || block.compare(Block.PALE_MOSS_BLOCK)
                || block.compare(Block.MUD)
                || block.compare(Block.MUDDY_MANGROVE_ROOTS)
                || block.compare(Block.SAND)
                || block.compare(Block.RED_SAND);
    }

    private boolean hasWaterAdjacent(@NotNull Block.Getter instance, int blockX, int blockY, int blockZ) {
        var north = instance.getBlock(blockX, blockY, blockZ - 1);
        var south = instance.getBlock(blockX, blockY, blockZ + 1);
        var west = instance.getBlock(blockX - 1, blockY, blockZ);
        var east = instance.getBlock(blockX + 1, blockY, blockZ);

        return north.compare(Block.WATER) || north.compare(Block.FROSTED_ICE)
                || south.compare(Block.WATER) || south.compare(Block.FROSTED_ICE)
                || west.compare(Block.WATER) || west.compare(Block.FROSTED_ICE)
                || east.compare(Block.WATER) || east.compare(Block.FROSTED_ICE);
    }
}

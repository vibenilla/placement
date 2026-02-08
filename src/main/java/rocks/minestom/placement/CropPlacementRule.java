package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CropPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:crops");

    public CropPlacementRule(@NotNull Block block) {
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
        if (!blockBelow.compare(Block.FARMLAND)) {
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
        if (!blockBelow.compare(Block.FARMLAND)) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }
}

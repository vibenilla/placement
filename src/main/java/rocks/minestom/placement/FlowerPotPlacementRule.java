package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FlowerPotPlacementRule extends BlockPlacementRule {
    public FlowerPotPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        Block flowerBlock = placementState.block();
        Block pottedBlock = Block.fromKey("minecraft:potted_" + flowerBlock.key().value());
        if (pottedBlock != null) return pottedBlock;
        return Block.FLOWER_POT;
    }

    @Override
    public boolean isSelfReplaceable(@NotNull Replacement replacement) {
        return Block.fromKey("minecraft:potted_" + replacement.block().key().value()) != null;
    }
}

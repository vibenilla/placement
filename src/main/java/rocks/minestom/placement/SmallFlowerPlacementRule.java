package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import rocks.minestom.placement.utils.PlacementUtils;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SmallFlowerPlacementRule extends BlockPlacementRule {
    private final boolean canBeWaterlogged;

    public SmallFlowerPlacementRule(@NotNull Block block) {
        super(block);
        this.canBeWaterlogged = block.properties().containsKey("waterlogged");
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var existingBlock = placementState.instance().getBlock(placementState.placePosition());
        if (existingBlock.id() == Block.FLOWER_POT.id()) {
            return Block.fromKey("minecraft:potted_" + block.key().value());
        }
        return canBeWaterlogged ? PlacementUtils.waterlogged(placementState) : block;
    }
}

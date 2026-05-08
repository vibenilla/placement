package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class WaterloggedDummyPlacementRule extends BlockPlacementRule {
    public WaterloggedDummyPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var waterlogged = placementState.instance().getBlock(placementState.placePosition()).compare(Block.WATER);

        return this.block.withProperty("waterlogged", String.valueOf(waterlogged));
    }
}

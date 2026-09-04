package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class WaterloggedDummyPlacementRule extends BlockPlacementRule {
    private final boolean sourceOnly;

    public WaterloggedDummyPlacementRule(Block block) {
        this(block, false);
    }

    public WaterloggedDummyPlacementRule(Block block, boolean sourceOnly) {
        super(block);
        this.sourceOnly = sourceOnly;
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var replaced = placementState.instance().getBlock(placementState.placePosition());
        var waterlogged = replaced.compare(Block.WATER)
                && (!this.sourceOnly || "0".equals(replaced.getProperty("level")));

        return this.block.withProperty("waterlogged", String.valueOf(waterlogged));
    }
}

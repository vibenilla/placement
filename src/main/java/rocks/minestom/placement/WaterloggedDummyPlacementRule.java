package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class WaterloggedDummyPlacementRule extends BlockPlacementRule {
    public WaterloggedDummyPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var replaced = placementState.instance().getBlock(placementState.placePosition());
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));

        return this.block.withProperty("waterlogged", String.valueOf(waterlogged));
    }
}

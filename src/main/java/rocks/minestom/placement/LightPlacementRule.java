package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class LightPlacementRule extends BlockPlacementRule {
    public LightPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var replaced = placementState.instance().getBlock(placementState.placePosition());
        return placementState.block()
                .withProperty("waterlogged", String.valueOf(replaced.compare(Block.WATER)))
                .withHandler(LightBlockHandler.INSTANCE);
    }
}

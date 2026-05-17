package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class HandlerAttachingPlacementRule extends BlockPlacementRule {
    private final BlockHandler handler;

    public HandlerAttachingPlacementRule(Block block, BlockHandler handler) {
        super(block);
        this.handler = handler;
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        return placementState.block().withHandler(this.handler);
    }
}

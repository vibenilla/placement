package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

/**
 * A placement rule that does not modify the block state.
 * Used for blocks that have no special placement logic in vanilla Minecraft.
 */
public final class DummyPlacementRule extends BlockPlacementRule {
    public DummyPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        return this.block;
    }
}

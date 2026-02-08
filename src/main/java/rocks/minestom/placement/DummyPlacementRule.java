package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

/**
 * A placement rule that does not modify the block state.
 * Used for blocks that have no special placement logic in vanilla Minecraft.
 */
public final class DummyPlacementRule extends BlockPlacementRule {
    public DummyPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        return this.block;
    }
}

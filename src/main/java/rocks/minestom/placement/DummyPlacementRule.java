package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

/**
 * Code from <a href="https://github.com/vibenilla/placement">vibenilla placement</a>
 * Licensed under Apache License 2.0.
 * 
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

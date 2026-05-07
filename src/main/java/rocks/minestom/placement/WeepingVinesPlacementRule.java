package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WeepingVinesPlacementRule extends BlockPlacementRule {
    public WeepingVinesPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        return block;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        int x = updateState.blockPosition().blockX();
        int y = updateState.blockPosition().blockY();
        int z = updateState.blockPosition().blockZ();

        var below = updateState.instance().getBlock(x, y - 1, z);
        if (below.id() == Block.WEEPING_VINES.id()) return Block.WEEPING_VINES_PLANT;

        return updateState.currentBlock();
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }
}

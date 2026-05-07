package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CaveVinesPlacementRule extends BlockPlacementRule {
    public CaveVinesPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        if (placementState.blockFace() != BlockFace.BOTTOM) return null;
        return Block.CAVE_VINES;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        Block block = updateState.currentBlock();
        int x = updateState.blockPosition().blockX();
        int y = updateState.blockPosition().blockY();
        int z = updateState.blockPosition().blockZ();

        Block below = updateState.instance().getBlock(x, y - 1, z);
        if (below.compare(Block.CAVE_VINES)) return Block.CAVE_VINES_PLANT;

        return block;
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }
}

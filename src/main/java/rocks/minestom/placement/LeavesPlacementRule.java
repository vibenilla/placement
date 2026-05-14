package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class LeavesPlacementRule extends BlockPlacementRule {
    public LeavesPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var placePosition = placementState.placePosition();
        var replaced = placementState.instance().getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));

        // TODO: vanilla computes distance via BFS over neighbouring logs (LeavesBlock.updateDistance); not implemented
        return this.block
                .withProperty("persistent", "true")
                .withProperty("waterlogged", String.valueOf(waterlogged))
                .withProperty("distance", "7");
    }
}

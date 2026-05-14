package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class HandlerAttachingPlacementRule extends BlockPlacementRule {
    private final BlockHandler handler;

    public HandlerAttachingPlacementRule(@NotNull Block block, @NotNull BlockHandler handler) {
        super(block);
        this.handler = handler;
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        return this.block.withHandler(this.handler);
    }
}

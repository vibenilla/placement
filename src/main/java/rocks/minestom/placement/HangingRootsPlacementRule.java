package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class HangingRootsPlacementRule extends BlockPlacementRule {
    public HangingRootsPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var aboveBlock = instance.getBlock(placePosition.relative(BlockFace.TOP));

        if (!aboveBlock.registry().collisionShape().isFaceFull(BlockFace.BOTTOM)) {
            return null;
        }

        var waterlogged = instance.getBlock(placePosition).compare(Block.WATER);
        return this.block.withProperty("waterlogged", String.valueOf(waterlogged));
    }
}

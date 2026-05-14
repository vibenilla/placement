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

        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        return this.block.withProperty("waterlogged", String.valueOf(waterlogged));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {

        if (updateState.fromFace() != BlockFace.TOP) {
            return updateState.currentBlock();
        }
        var above = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.TOP));

        if (!above.registry().collisionShape().isFaceFull(BlockFace.BOTTOM)) {
            return Block.AIR;
        }
        return updateState.currentBlock();
    }
}

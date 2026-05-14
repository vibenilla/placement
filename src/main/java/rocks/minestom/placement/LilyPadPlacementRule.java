package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class LilyPadPlacementRule extends BlockPlacementRule {
    public LilyPadPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var below = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!below.compare(Block.WATER) || !"0".equals(below.getProperty("level"))) {
            return null;
        }
        return this.block;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {

        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }
        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
        var stillWater = below.compare(Block.WATER) && "0".equals(below.getProperty("level"));
        return stillWater || below.compare(Block.ICE) ? updateState.currentBlock() : Block.AIR;
    }
}

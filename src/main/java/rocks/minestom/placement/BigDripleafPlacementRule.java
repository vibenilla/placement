package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class BigDripleafPlacementRule extends BlockPlacementRule {
    public BigDripleafPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        String facing;

        if (belowBlock.compare(Block.BIG_DRIPLEAF) || belowBlock.compare(Block.BIG_DRIPLEAF_STEM)) {
            var belowFacing = belowBlock.getProperty("facing");
            facing = belowFacing == null ? BlockFace.fromYaw(yaw).getOppositeFace().name().toLowerCase() : belowFacing;
        } else {
            facing = BlockFace.fromYaw(yaw).getOppositeFace().name().toLowerCase();
        }

        var waterlogged = instance.getBlock(placePosition).compare(Block.WATER);

        return this.block
                .withProperty("facing", facing)
                .withProperty("waterlogged", String.valueOf(waterlogged))
                .withProperty("tilt", "none");
    }
}

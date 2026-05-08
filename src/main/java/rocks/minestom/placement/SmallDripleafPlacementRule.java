package rocks.minestom.placement;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class SmallDripleafPlacementRule extends BlockPlacementRule {
    public SmallDripleafPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        if (!(placementState.instance() instanceof Instance instance)) {
            return null;
        }

        var placePosition = placementState.placePosition();
        var upperPosition = placePosition.relative(BlockFace.TOP);
        var maxY = instance.getCachedDimensionType().maxY();

        if (upperPosition.blockY() >= maxY) {
            return null;
        }

        var existingUpperBlock = instance.getBlock(upperPosition);

        if (!existingUpperBlock.registry().isReplaceable()) {
            return null;
        }

        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw).getOppositeFace().name().toLowerCase();
        var lowerWaterlogged = instance.getBlock(placePosition).compare(Block.WATER);
        var upperWaterlogged = existingUpperBlock.compare(Block.WATER);
        var upperBlock = this.block
                .withProperty("facing", facing)
                .withProperty("half", "upper")
                .withProperty("waterlogged", String.valueOf(upperWaterlogged));
        instance.setBlock(upperPosition, upperBlock, false);

        return this.block
                .withProperty("facing", facing)
                .withProperty("half", "lower")
                .withProperty("waterlogged", String.valueOf(lowerWaterlogged));
    }
}

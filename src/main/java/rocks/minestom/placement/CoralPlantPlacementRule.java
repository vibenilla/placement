package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class CoralPlantPlacementRule extends BlockPlacementRule {
    public CoralPlantPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var below = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!below.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return null;
        }

        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && isWaterSource(replaced);

        return this.block.withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    private static boolean isWaterSource(@NotNull Block water) {
        var level = water.getProperty("level");
        return level == null || "0".equals(level);
    }
}

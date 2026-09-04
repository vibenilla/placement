package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class CropPlacementRule extends BlockPlacementRule {
    public CropPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var instance = placementState.instance();
        var supportPosition = placementState.placePosition().relative(BlockFace.BOTTOM);
        var supportBlock = instance.getBlock(supportPosition);
        var supportsCropsTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_crops"));

        if (supportsCropsTag != null && supportsCropsTag.contains(supportBlock)
                && hasSufficientLight(instance, placementState.placePosition())) {
            return placementState.block();
        }

        return null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
        var supportsCropsTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_crops"));

        if (supportsCropsTag != null && supportsCropsTag.contains(below)
                && hasSufficientLight(updateState.instance(), updateState.blockPosition())) {
            return updateState.currentBlock();
        }

        return Block.AIR;
    }

    private static boolean hasSufficientLight(Block.Getter blockGetter, Point position) {
        if (!(blockGetter instanceof Instance instance)) {
            return true;
        }

        var x = position.blockX();
        var y = position.blockY();
        var z = position.blockZ();
        return Math.max(instance.getBlockLight(x, y, z), instance.getSkyLight(x, y, z)) >= 8;
    }
}

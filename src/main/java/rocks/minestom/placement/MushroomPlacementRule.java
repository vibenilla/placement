package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;

public final class MushroomPlacementRule extends BlockPlacementRule {
    public MushroomPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var instance = placementState.instance();
        var supportPosition = placementState.placePosition().relative(BlockFace.BOTTOM);
        var supportBlock = instance.getBlock(supportPosition);
        var growTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:mushroom_grow_block"));

        if (supportsSpecialBlock(supportBlock, growTag)
                || hasLowLight(instance, placementState.placePosition())
                && supportBlock.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
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
        var growTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:mushroom_grow_block"));
        var supported = supportsSpecialBlock(below, growTag)
                || hasLowLight(updateState.instance(), updateState.blockPosition())
                && below.registry().collisionShape().isFaceFull(BlockFace.TOP);
        return supported ? updateState.currentBlock() : Block.AIR;
    }

    private static boolean supportsSpecialBlock(Block block, RegistryTag<Block> growTag) {
        return growTag != null && growTag.contains(block);
    }

    private static boolean hasLowLight(Block.Getter blockGetter, Point position) {
        if (!(blockGetter instanceof Instance instance)) {
            return true;
        }

        var x = position.blockX();
        var y = position.blockY();
        var z = position.blockZ();
        return Math.max(instance.getBlockLight(x, y, z), instance.getSkyLight(x, y, z)) < 13;
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

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

        if (growTag != null && growTag.contains(supportBlock)) {
            return placementState.block();
        }

        if (supportBlock.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return placementState.block();
        }

        // TODO: vanilla also checks light level (< 13); not implemented
        return null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));

        if (!below.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }
}

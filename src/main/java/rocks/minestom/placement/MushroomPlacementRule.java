package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
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

        if (supports(supportBlock, growTag)) {
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
        var growTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:mushroom_grow_block"));
        return supports(below, growTag) ? updateState.currentBlock() : Block.AIR;
    }

    private static boolean supports(Block block, RegistryTag<Block> growTag) {
        return (growTag != null && growTag.contains(block))
                || block.registry().collisionShape().isFaceFull(BlockFace.TOP);
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class SugarCanePlacementRule extends BlockPlacementRule {
    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    public SugarCanePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var supportPosition = placementState.placePosition().relative(BlockFace.BOTTOM);
        var supportBlock = instance.getBlock(supportPosition);

        if (supportBlock.compare(Block.SUGAR_CANE)) {
            return this.block;
        }
        var registry = MinecraftServer.process().blocks();
        var dirtTag = registry.getTag(Key.key("minecraft:dirt"));
        var sandTag = registry.getTag(Key.key("minecraft:sand"));
        var onValidGround = (dirtTag != null && dirtTag.contains(supportBlock))
                || (sandTag != null && sandTag.contains(supportBlock));

        if (!onValidGround) {
            return null;
        }

        for (var face : HORIZONTAL_FACES) {
            var adjacentBlock = instance.getBlock(supportPosition.relative(face));

            if (adjacentBlock.compare(Block.WATER) || adjacentBlock.compare(Block.FROSTED_ICE)) {
                return this.block;
            }
        }
        return null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var instance = updateState.instance();
        var blockPosition = updateState.blockPosition();
        var below = instance.getBlock(blockPosition.relative(BlockFace.BOTTOM));

        if (below.compare(Block.SUGAR_CANE)) {
            return updateState.currentBlock();
        }
        var registry = MinecraftServer.process().blocks();
        var dirtTag = registry.getTag(Key.key("minecraft:dirt"));
        var sandTag = registry.getTag(Key.key("minecraft:sand"));
        var onValidGround = (dirtTag != null && dirtTag.contains(below))
                || (sandTag != null && sandTag.contains(below));

        if (!onValidGround) {
            return Block.AIR;
        }
        var belowPosition = blockPosition.relative(BlockFace.BOTTOM);

        for (var face : HORIZONTAL_FACES) {
            var adjacentBlock = instance.getBlock(belowPosition.relative(face));

            if (adjacentBlock.compare(Block.WATER) || adjacentBlock.compare(Block.FROSTED_ICE)) {
                return updateState.currentBlock();
            }
        }
        return Block.AIR;
    }
}

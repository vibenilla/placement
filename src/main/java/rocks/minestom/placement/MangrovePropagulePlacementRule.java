package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class MangrovePropagulePlacementRule extends BlockPlacementRule {
    public MangrovePropagulePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        var aboveBlock = instance.getBlock(placePosition.relative(BlockFace.TOP));
        var registry = MinecraftServer.process().blocks();
        var hangingSupportTag = registry.getTag(Key.key("minecraft:supports_hanging_mangrove_propagule"));
        var hanging = hangingSupportTag != null && hangingSupportTag.contains(aboveBlock);

        if (hanging) {
            return this.block
                    .withProperty("hanging", "true")
                    .withProperty("age", "4")
                    .withProperty("waterlogged", String.valueOf(waterlogged))
                    .withProperty("stage", "0");
        }

        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));
        var supportTag = registry.getTag(Key.key("minecraft:supports_mangrove_propagule"));

        if (supportTag == null || !supportTag.contains(belowBlock)) {
            return null;
        }
        return this.block
                .withProperty("hanging", "false")
                .withProperty("age", "4")
                .withProperty("waterlogged", String.valueOf(waterlogged))
                .withProperty("stage", "0");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var hanging = "true".equals(currentBlock.getProperty("hanging"));

        if (!hanging) {
            return currentBlock;
        }

        if (updateState.fromFace() != BlockFace.TOP) {
            return currentBlock;
        }
        var above = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.TOP));
        var hangingSupportTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_hanging_mangrove_propagule"));

        if (hangingSupportTag != null && hangingSupportTag.contains(above)) {
            return currentBlock;
        }
        return Block.AIR;
    }
}

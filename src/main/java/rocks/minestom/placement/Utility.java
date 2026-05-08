package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.function.Function;

public final class Utility {
    private Utility() {

    }

    /**
     * Registers a single placement rule for blocks which share the same registry tag.
     * Silently does nothing if the tag is missing from the loaded registry data.
     */
    public static void registerPlacementRules(Function<Block, BlockPlacementRule> function, Key key) {
        var registry = MinecraftServer.process().blocks();
        var tag = registry.getTag(key);

        if (tag == null) {
            return;
        }

        var blockManager = MinecraftServer.getBlockManager();

        for (var entry : tag) {
            blockManager.registerBlockPlacementRule(function.apply(Block.fromKey(entry.key())));
        }
    }

    public static void registerPlacementRules(Function<Block, BlockPlacementRule> function, Block... blocks) {
        for (var block : blocks) {
            MinecraftServer.getBlockManager().registerBlockPlacementRule(function.apply(block));
        }
    }

    /**
     * Returns whether the block has the given registry tag (e.g. {@code minecraft:doors}).
     */
    public static boolean hasTag(Block block, Key key) {
        var tag = MinecraftServer.process().blocks().getTag(key);
        return tag == null || tag.contains(block);
    }
}

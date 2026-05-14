package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
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
     * Returns {@code false} when the tag is missing from the loaded registry data.
     */
    public static boolean hasTag(Block block, Key key) {
        var tag = MinecraftServer.process().blocks().getTag(key);
        return tag != null && tag.contains(block);
    }

    /**
     * Mirrors vanilla's block-interact-skip-on-sneaking rule: when the player is sneaking and has
     * at least one item in hand, vanilla skips the block's {@code use} action so that the held
     * item's placement / use can proceed instead.
     */
    public static boolean shouldSkipInteract(BlockHandler.Interaction interaction) {
        var player = interaction.getPlayer();

        if (!player.isSneaking()) {
            return false;
        }
        return !player.getItemInMainHand().isAir() || !player.getItemInOffHand().isAir();
    }
}

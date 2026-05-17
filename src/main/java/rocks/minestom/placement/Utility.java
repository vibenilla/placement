package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
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
     * Mirrors vanilla's {@code Block.canSupportCenter}: whether the given face of the block has
     * enough material at its centre to support a centred attachment (torch, redstone wire, etc.).
     * Walls, fences and other "post" blocks fail {@link net.minestom.server.collision.Shape#isFaceFull}
     * but still support a torch on top because their central column reaches the face.
     */
    public static boolean canSupportCenter(Block block, BlockFace face) {
        var shape = block.registry().collisionShape();

        if (shape.isFaceFull(face)) {
            return true;
        }

        return shape.intersectBox(Vec.ZERO, centerProbe(face));
    }

    private static BoundingBox centerProbe(BlockFace face) {
        var thin = 1.0 / 16.0;
        var width = 2.0 / 16.0;
        var lo = 7.0 / 16.0;
        var hi = 15.0 / 16.0;

        return switch (face) {
            case TOP -> new BoundingBox(width, thin, width, new Vec(lo, hi, lo));
            case BOTTOM -> new BoundingBox(width, thin, width, new Vec(lo, 0.0, lo));
            case NORTH -> new BoundingBox(width, width, thin, new Vec(lo, lo, 0.0));
            case SOUTH -> new BoundingBox(width, width, thin, new Vec(lo, lo, hi));
            case EAST -> new BoundingBox(thin, width, width, new Vec(hi, lo, lo));
            case WEST -> new BoundingBox(thin, width, width, new Vec(0.0, lo, lo));
        };
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

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CrossConnectingPlacementRule extends BlockPlacementRule {
    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    private static final Block[] CROSS_CONNECTING_BLOCKS = {
            Block.IRON_BARS,
            Block.GLASS_PANE,
            Block.WHITE_STAINED_GLASS_PANE,
            Block.LIGHT_GRAY_STAINED_GLASS_PANE,
            Block.GRAY_STAINED_GLASS_PANE,
            Block.BLACK_STAINED_GLASS_PANE,
            Block.BROWN_STAINED_GLASS_PANE,
            Block.RED_STAINED_GLASS_PANE,
            Block.ORANGE_STAINED_GLASS_PANE,
            Block.YELLOW_STAINED_GLASS_PANE,
            Block.LIME_STAINED_GLASS_PANE,
            Block.GREEN_STAINED_GLASS_PANE,
            Block.CYAN_STAINED_GLASS_PANE,
            Block.LIGHT_BLUE_STAINED_GLASS_PANE,
            Block.BLUE_STAINED_GLASS_PANE,
            Block.PURPLE_STAINED_GLASS_PANE,
            Block.MAGENTA_STAINED_GLASS_PANE,
            Block.PINK_STAINED_GLASS_PANE
    };

    public CrossConnectingPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var blockRegistry = MinecraftServer.process().blocks();
        var wallsTag = blockRegistry.getTag(Key.key("minecraft:walls"));
        var leavesTag = blockRegistry.getTag(Key.key("minecraft:leaves"));
        var shulkerBoxesTag = blockRegistry.getTag(Key.key("minecraft:shulker_boxes"));
        var waterlogged = blockGetter.getBlock(placePosition).compare(Block.WATER);
        var result = this.block.withProperty("waterlogged", String.valueOf(waterlogged));

        for (var face : HORIZONTAL_FACES) {
            var neighborPosition = placePosition.relative(face);
            var neighbor = blockGetter.getBlock(neighborPosition);
            var oppositeFace = face.getOppositeFace();
            var sturdy = neighbor.registry().collisionShape().isFaceFull(oppositeFace);
            var connects = attachsTo(neighbor, sturdy, wallsTag, leavesTag, shulkerBoxesTag);
            result = result.withProperty(face.name().toLowerCase(), String.valueOf(connects));
        }

        return result;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var fromFace = updateState.fromFace();

        if (fromFace == BlockFace.TOP || fromFace == BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var blockGetter = updateState.instance();
        var blockPosition = updateState.blockPosition();
        var neighborPosition = blockPosition.relative(fromFace);
        var neighbor = blockGetter.getBlock(neighborPosition);
        var oppositeFace = fromFace.getOppositeFace();
        var sturdy = neighbor.registry().collisionShape().isFaceFull(oppositeFace);
        var blockRegistry = MinecraftServer.process().blocks();
        var wallsTag = blockRegistry.getTag(Key.key("minecraft:walls"));
        var leavesTag = blockRegistry.getTag(Key.key("minecraft:leaves"));
        var shulkerBoxesTag = blockRegistry.getTag(Key.key("minecraft:shulker_boxes"));
        var connects = attachsTo(neighbor, sturdy, wallsTag, leavesTag, shulkerBoxesTag);
        return updateState.currentBlock().withProperty(fromFace.name().toLowerCase(), String.valueOf(connects));
    }

    private static boolean attachsTo(
            @NotNull Block neighbor,
            boolean sturdy,
            @Nullable RegistryTag<Block> wallsTag,
            @Nullable RegistryTag<Block> leavesTag,
            @Nullable RegistryTag<Block> shulkerBoxesTag
    ) {
        if (wallsTag != null && wallsTag.contains(neighbor)) {
            return true;
        }

        if (isCrossConnecting(neighbor)) {
            return true;
        }

        return sturdy && !isExceptionForConnection(neighbor, leavesTag, shulkerBoxesTag);
    }

    private static boolean isCrossConnecting(@NotNull Block neighbor) {

        for (var crossConnecting : CROSS_CONNECTING_BLOCKS) {

            if (neighbor.compare(crossConnecting)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isExceptionForConnection(
            @NotNull Block neighbor,
            @Nullable RegistryTag<Block> leavesTag,
            @Nullable RegistryTag<Block> shulkerBoxesTag
    ) {
        if (leavesTag != null && leavesTag.contains(neighbor)) {
            return true;
        }

        if (shulkerBoxesTag != null && shulkerBoxesTag.contains(neighbor)) {
            return true;
        }
        return neighbor.compare(Block.BARRIER)
                || neighbor.compare(Block.CARVED_PUMPKIN)
                || neighbor.compare(Block.JACK_O_LANTERN)
                || neighbor.compare(Block.MELON)
                || neighbor.compare(Block.PUMPKIN);
    }
}

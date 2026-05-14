package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FencePlacementRule extends BlockPlacementRule {
    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    private final boolean wooden;

    public FencePlacementRule(@NotNull Block block) {
        super(block);
        var woodenFencesTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:wooden_fences"));
        this.wooden = woodenFencesTag != null && woodenFencesTag.contains(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var blockRegistry = MinecraftServer.process().blocks();
        var fencesTag = blockRegistry.getTag(Key.key("minecraft:fences"));
        var woodenFencesTag = blockRegistry.getTag(Key.key("minecraft:wooden_fences"));
        var fenceGatesTag = blockRegistry.getTag(Key.key("minecraft:fence_gates"));
        var leavesTag = blockRegistry.getTag(Key.key("minecraft:leaves"));
        var shulkerBoxesTag = blockRegistry.getTag(Key.key("minecraft:shulker_boxes"));
        var replaced = blockGetter.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        var result = this.block.withProperty("waterlogged", String.valueOf(waterlogged));

        for (var face : HORIZONTAL_FACES) {
            var neighborPosition = placePosition.relative(face);
            var neighbor = blockGetter.getBlock(neighborPosition);
            var oppositeFace = face.getOppositeFace();
            var sturdy = neighbor.registry().collisionShape().isFaceFull(oppositeFace);
            var connects = connectsTo(neighbor, sturdy, oppositeFace, this.wooden, fencesTag, woodenFencesTag, fenceGatesTag, leavesTag, shulkerBoxesTag);
            result = result.withProperty(face.name().toLowerCase(), String.valueOf(connects));
        }

        return result;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        // TODO: vanilla schedules a water tick on update; not implemented
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
        var fencesTag = blockRegistry.getTag(Key.key("minecraft:fences"));
        var woodenFencesTag = blockRegistry.getTag(Key.key("minecraft:wooden_fences"));
        var fenceGatesTag = blockRegistry.getTag(Key.key("minecraft:fence_gates"));
        var leavesTag = blockRegistry.getTag(Key.key("minecraft:leaves"));
        var shulkerBoxesTag = blockRegistry.getTag(Key.key("minecraft:shulker_boxes"));
        var connects = connectsTo(neighbor, sturdy, oppositeFace, this.wooden, fencesTag, woodenFencesTag, fenceGatesTag, leavesTag, shulkerBoxesTag);
        return updateState.currentBlock().withProperty(fromFace.name().toLowerCase(), String.valueOf(connects));
    }

    private static boolean connectsTo(
            @NotNull Block neighbor,
            boolean sturdy,
            @NotNull BlockFace oppositeFace,
            boolean selfWooden,
            @Nullable RegistryTag<Block> fencesTag,
            @Nullable RegistryTag<Block> woodenFencesTag,
            @Nullable RegistryTag<Block> fenceGatesTag,
            @Nullable RegistryTag<Block> leavesTag,
            @Nullable RegistryTag<Block> shulkerBoxesTag
    ) {

        if (fencesTag != null && fencesTag.contains(neighbor)) {
            var neighborWooden = woodenFencesTag != null && woodenFencesTag.contains(neighbor);
            return neighborWooden == selfWooden;
        }

        if (fenceGatesTag != null && fenceGatesTag.contains(neighbor)) {
            var gateFacing = neighbor.getProperty("facing");
            return isPerpendicular(gateFacing, oppositeFace);
        }

        return sturdy && !isExceptionForConnection(neighbor, leavesTag, shulkerBoxesTag);
    }

    private static boolean isPerpendicular(@Nullable String gateFacing, @NotNull BlockFace oppositeFace) {
        if (gateFacing == null) {
            return false;
        }

        var oppositeIsZ = oppositeFace == BlockFace.NORTH || oppositeFace == BlockFace.SOUTH;
        var gateIsZ = "north".equals(gateFacing) || "south".equals(gateFacing);
        return oppositeIsZ != gateIsZ;
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

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

public final class WallPlacementRule extends BlockPlacementRule {
    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    private static final Block[] CROSS_CONNECTING_BLOCKS = {
            Block.IRON_BARS,
            Block.COPPER_BARS,
            Block.EXPOSED_COPPER_BARS,
            Block.WEATHERED_COPPER_BARS,
            Block.OXIDIZED_COPPER_BARS,
            Block.WAXED_COPPER_BARS,
            Block.WAXED_EXPOSED_COPPER_BARS,
            Block.WAXED_WEATHERED_COPPER_BARS,
            Block.WAXED_OXIDIZED_COPPER_BARS,
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

    public WallPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var blockRegistry = MinecraftServer.process().blocks();
        var wallsTag = blockRegistry.getTag(Key.key("minecraft:walls"));
        var fenceGatesTag = blockRegistry.getTag(Key.key("minecraft:fence_gates"));
        var leavesTag = blockRegistry.getTag(Key.key("minecraft:leaves"));
        var shulkerBoxesTag = blockRegistry.getTag(Key.key("minecraft:shulker_boxes"));
        var replaced = blockGetter.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        var north = connectsTo(blockGetter, placePosition, BlockFace.NORTH, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag);
        var east = connectsTo(blockGetter, placePosition, BlockFace.EAST, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag);
        var south = connectsTo(blockGetter, placePosition, BlockFace.SOUTH, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag);
        var west = connectsTo(blockGetter, placePosition, BlockFace.WEST, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag);
        var aboveBlock = blockGetter.getBlock(placePosition.relative(BlockFace.TOP));
        return buildState(this.block, north, east, south, west, aboveBlock, waterlogged, wallsTag);
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var fromFace = updateState.fromFace();

        if (fromFace == BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var blockGetter = updateState.instance();
        var blockPosition = updateState.blockPosition();
        var blockRegistry = MinecraftServer.process().blocks();
        var wallsTag = blockRegistry.getTag(Key.key("minecraft:walls"));
        var fenceGatesTag = blockRegistry.getTag(Key.key("minecraft:fence_gates"));
        var leavesTag = blockRegistry.getTag(Key.key("minecraft:leaves"));
        var shulkerBoxesTag = blockRegistry.getTag(Key.key("minecraft:shulker_boxes"));
        var current = updateState.currentBlock();
        var waterlogged = "true".equals(current.getProperty("waterlogged"));
        var north = fromFace == BlockFace.NORTH
                ? connectsTo(blockGetter, blockPosition, BlockFace.NORTH, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag)
                : isConnected(current.getProperty("north"));
        var east = fromFace == BlockFace.EAST
                ? connectsTo(blockGetter, blockPosition, BlockFace.EAST, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag)
                : isConnected(current.getProperty("east"));
        var south = fromFace == BlockFace.SOUTH
                ? connectsTo(blockGetter, blockPosition, BlockFace.SOUTH, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag)
                : isConnected(current.getProperty("south"));
        var west = fromFace == BlockFace.WEST
                ? connectsTo(blockGetter, blockPosition, BlockFace.WEST, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag)
                : isConnected(current.getProperty("west"));
        var aboveBlock = blockGetter.getBlock(blockPosition.relative(BlockFace.TOP));
        return buildState(this.block, north, east, south, west, aboveBlock, waterlogged, wallsTag);
    }

    private static Block buildState(
            @NotNull Block base,
            boolean north,
            boolean east,
            boolean south,
            boolean west,
            @NotNull Block aboveBlock,
            boolean waterlogged,
            @Nullable RegistryTag<Block> wallsTag
    ) {
        var topIsWall = wallsTag != null && wallsTag.contains(aboveBlock);
        var topIsWallWithPost = topIsWall && "true".equals(aboveBlock.getProperty("up"));
        var northNone = !north;
        var eastNone = !east;
        var southNone = !south;
        var westNone = !west;
        var hasCorner = northNone && southNone && westNone && eastNone
                || northNone != southNone
                || westNone != eastNone;
        var connectionCount = (north ? 1 : 0) + (east ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0);
        var collinearOnly = connectionCount == 0
                || connectionCount == 2 && (north && south || east && west);
        var up = topIsWallWithPost || hasCorner || topIsWall;
        var tallEligible = up && collinearOnly;
        // TODO: not 100% accurate - vanilla also depends on shape sturdiness of the upper neighbor
        return base
                .withProperty("north", wallSide(north, tallEligible))
                .withProperty("east", wallSide(east, tallEligible))
                .withProperty("south", wallSide(south, tallEligible))
                .withProperty("west", wallSide(west, tallEligible))
                .withProperty("up", String.valueOf(up))
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }

    private static String wallSide(boolean connected, boolean tallEligible) {
        if (!connected) {
            return "none";
        }

        return tallEligible ? "tall" : "low";
    }

    private static boolean isConnected(@Nullable String wallSideValue) {
        return "low".equals(wallSideValue) || "tall".equals(wallSideValue);
    }

    private static boolean connectsTo(
            @NotNull Block.Getter blockGetter,
            @NotNull Point centerPosition,
            @NotNull BlockFace face,
            @Nullable RegistryTag<Block> wallsTag,
            @Nullable RegistryTag<Block> fenceGatesTag,
            @Nullable RegistryTag<Block> leavesTag,
            @Nullable RegistryTag<Block> shulkerBoxesTag
    ) {
        var neighborPosition = centerPosition.relative(face);
        var neighbor = blockGetter.getBlock(neighborPosition);
        var oppositeFace = face.getOppositeFace();
        var sturdy = neighbor.registry().collisionShape().isFaceFull(oppositeFace);

        if (wallsTag != null && wallsTag.contains(neighbor)) {
            return true;
        }

        if (fenceGatesTag != null && fenceGatesTag.contains(neighbor)) {
            var gateFacing = neighbor.getProperty("facing");
            return isPerpendicular(gateFacing, oppositeFace);
        }

        if (isCrossConnecting(neighbor)) {
            return true;
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

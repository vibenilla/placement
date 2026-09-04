package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.Shape;
import net.minestom.server.collision.ShapeImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
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

    public WallPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var blockRegistry = MinecraftServer.process().blocks();
        var wallsTag = blockRegistry.getTag(Key.key("minecraft:walls"));
        var wallPostOverrideTag = blockRegistry.getTag(Key.key("minecraft:wall_post_override"));
        var fenceGatesTag = blockRegistry.getTag(Key.key("minecraft:fence_gates"));
        var leavesTag = blockRegistry.getTag(Key.key("minecraft:leaves"));
        var shulkerBoxesTag = blockRegistry.getTag(Key.key("minecraft:shulker_boxes"));
        var replaced = blockGetter.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER);
        var north = connectsTo(blockGetter, placePosition, BlockFace.NORTH, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag);
        var east = connectsTo(blockGetter, placePosition, BlockFace.EAST, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag);
        var south = connectsTo(blockGetter, placePosition, BlockFace.SOUTH, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag);
        var west = connectsTo(blockGetter, placePosition, BlockFace.WEST, wallsTag, fenceGatesTag, leavesTag, shulkerBoxesTag);
        var aboveBlock = blockGetter.getBlock(placePosition.relative(BlockFace.TOP));
        return buildState(
                placementState.block(), north, east, south, west, aboveBlock, waterlogged, wallsTag, wallPostOverrideTag);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var fromFace = updateState.fromFace();

        if (fromFace == BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var blockGetter = updateState.instance();
        var blockPosition = updateState.blockPosition();
        var blockRegistry = MinecraftServer.process().blocks();
        var wallsTag = blockRegistry.getTag(Key.key("minecraft:walls"));
        var wallPostOverrideTag = blockRegistry.getTag(Key.key("minecraft:wall_post_override"));
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
        return buildState(
                this.block, north, east, south, west, aboveBlock, waterlogged, wallsTag, wallPostOverrideTag);
    }

    private static Block buildState(
            Block base,
            boolean north,
            boolean east,
            boolean south,
            boolean west,
            Block aboveBlock,
            boolean waterlogged,
            @Nullable RegistryTag<Block> wallsTag,
            @Nullable RegistryTag<Block> wallPostOverrideTag
    ) {
        var aboveShape = aboveBlock.collisionShape();
        var northSide = wallSide(north, isCovered(aboveShape, BlockFace.NORTH));
        var eastSide = wallSide(east, isCovered(aboveShape, BlockFace.EAST));
        var southSide = wallSide(south, isCovered(aboveShape, BlockFace.SOUTH));
        var westSide = wallSide(west, isCovered(aboveShape, BlockFace.WEST));
        var topIsWall = wallsTag != null && wallsTag.contains(aboveBlock);
        var topIsWallWithPost = topIsWall && "true".equals(aboveBlock.getProperty("up"));
        var northNone = !north;
        var eastNone = !east;
        var southNone = !south;
        var westNone = !west;
        var hasCorner = northNone && southNone && westNone && eastNone
                || northNone != southNone
                || westNone != eastNone;
        var hasHighWall = "tall".equals(northSide) && "tall".equals(southSide)
                || "tall".equals(eastSide) && "tall".equals(westSide);
        var postOverride = wallPostOverrideTag != null && wallPostOverrideTag.contains(aboveBlock);
        var up = topIsWallWithPost
                || hasCorner
                || !hasHighWall && (postOverride || isCovered(aboveShape, null));

        return base
                .withProperty("north", northSide)
                .withProperty("east", eastSide)
                .withProperty("south", southSide)
                .withProperty("west", westSide)
                .withProperty("up", String.valueOf(up))
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }

    private static String wallSide(boolean connected, boolean covered) {
        if (!connected) {
            return "none";
        }

        return covered ? "tall" : "low";
    }

    private static boolean isCovered(Shape aboveShape, @Nullable BlockFace face) {
        if (!(aboveShape instanceof ShapeImpl shape)) {
            return aboveShape.isFaceFull(BlockFace.BOTTOM);
        }

        var startX = face == BlockFace.WEST ? 0 : 7;
        var endX = face == BlockFace.EAST ? 16 : 9;
        var startZ = face == BlockFace.NORTH ? 0 : 7;
        var endZ = face == BlockFace.SOUTH ? 16 : 9;

        for (var x = startX; x < endX; x++) {
            for (var z = startZ; z < endZ; z++) {
                var sampleX = (x + 0.5D) / 16.0D;
                var sampleZ = (z + 0.5D) / 16.0D;
                var covered = false;

                for (var box : shape.boundingBoxes()) {
                    if (box.minY() <= 0.0D
                            && box.maxY() > 0.0D
                            && sampleX >= box.minX()
                            && sampleX <= box.maxX()
                            && sampleZ >= box.minZ()
                            && sampleZ <= box.maxZ()) {
                        covered = true;
                        break;
                    }
                }

                if (!covered) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isConnected(@Nullable String wallSideValue) {
        return "low".equals(wallSideValue) || "tall".equals(wallSideValue);
    }

    private static boolean connectsTo(
            Block.Getter blockGetter,
            Point centerPosition,
            BlockFace face,
            @Nullable RegistryTag<Block> wallsTag,
            @Nullable RegistryTag<Block> fenceGatesTag,
            @Nullable RegistryTag<Block> leavesTag,
            @Nullable RegistryTag<Block> shulkerBoxesTag
    ) {
        var neighborPosition = centerPosition.relative(face);
        var neighbor = blockGetter.getBlock(neighborPosition);
        var oppositeFace = face.getOppositeFace();
        var sturdy = Utility.canSupportRigidBlock(neighbor, oppositeFace);

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

    private static boolean isPerpendicular(@Nullable String gateFacing, BlockFace oppositeFace) {
        if (gateFacing == null) {
            return false;
        }

        var oppositeIsZ = oppositeFace == BlockFace.NORTH || oppositeFace == BlockFace.SOUTH;
        var gateIsZ = "north".equals(gateFacing) || "south".equals(gateFacing);
        return oppositeIsZ != gateIsZ;
    }

    private static boolean isCrossConnecting(Block neighbor) {
        for (var crossConnecting : CROSS_CONNECTING_BLOCKS) {
            if (neighbor.compare(crossConnecting)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isExceptionForConnection(
            Block neighbor,
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

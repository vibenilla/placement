package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public final class WallPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:walls");
    private static final Key FENCE_GATES = Key.key("minecraft:fence_gates");
    private static final Key LEAVES = Key.key("minecraft:leaves");
    private static final Key SHULKER_BOXES = Key.key("minecraft:shulker_boxes");

    public WallPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        if (!(blockGetter instanceof Instance instance)) {
            return placementState.block();
        }

        var placePosition = placementState.placePosition();

        var northPosition = placePosition.relative(BlockFace.NORTH);
        var eastPosition = placePosition.relative(BlockFace.EAST);
        var southPosition = placePosition.relative(BlockFace.SOUTH);
        var westPosition = placePosition.relative(BlockFace.WEST);
        var abovePosition = placePosition.relative(BlockFace.TOP);

        var northBlock = blockGetter.getBlock(northPosition);
        var eastBlock = blockGetter.getBlock(eastPosition);
        var southBlock = blockGetter.getBlock(southPosition);
        var westBlock = blockGetter.getBlock(westPosition);
        var aboveBlock = blockGetter.getBlock(abovePosition);

        var northConnects = this.connectsTo(northBlock, this.isFaceSturdy(northBlock, BlockFace.SOUTH), BlockFace.NORTH);
        var eastConnects = this.connectsTo(eastBlock, this.isFaceSturdy(eastBlock, BlockFace.WEST), BlockFace.EAST);
        var southConnects = this.connectsTo(southBlock, this.isFaceSturdy(southBlock, BlockFace.NORTH), BlockFace.SOUTH);
        var westConnects = this.connectsTo(westBlock, this.isFaceSturdy(westBlock, BlockFace.EAST), BlockFace.WEST);

        var placed = this.updateShapeForPlacement(
                placementState.block(),
                aboveBlock,
                northConnects,
                eastConnects,
                southConnects,
                westConnects
        );

        VanillaPlacementUtils.scheduleHorizontalNeighborRuleUpdates(instance, placePosition);

        return placed;
    }

    private Block updateShapeForPlacement(
            Block baseBlock,
            Block aboveBlock,
            boolean northConnects,
            boolean eastConnects,
            boolean southConnects,
            boolean westConnects
    ) {
        var north = this.makeWallState(northConnects);
        var east = this.makeWallState(eastConnects);
        var south = this.makeWallState(southConnects);
        var west = this.makeWallState(westConnects);

        var blockWithSides = baseBlock.withProperties(Map.of(
                "north", north,
                "east", east,
                "south", south,
                "west", west,
                "waterlogged", "false"
        ));

        var raisePost = this.shouldRaisePost(blockWithSides, aboveBlock);

        return blockWithSides.withProperty("up", Boolean.toString(raisePost));
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var blockGetter = updateState.instance();
        var placePosition = updateState.blockPosition();
        var currentBlock = updateState.currentBlock();

        var northPosition = placePosition.relative(BlockFace.NORTH);
        var eastPosition = placePosition.relative(BlockFace.EAST);
        var southPosition = placePosition.relative(BlockFace.SOUTH);
        var westPosition = placePosition.relative(BlockFace.WEST);
        var abovePosition = placePosition.relative(BlockFace.TOP);

        var northBlock = blockGetter.getBlock(northPosition);
        var eastBlock = blockGetter.getBlock(eastPosition);
        var southBlock = blockGetter.getBlock(southPosition);
        var westBlock = blockGetter.getBlock(westPosition);
        var aboveBlock = blockGetter.getBlock(abovePosition);

        var northConnects = this.connectsTo(northBlock, this.isFaceSturdy(northBlock, BlockFace.SOUTH), BlockFace.NORTH);
        var eastConnects = this.connectsTo(eastBlock, this.isFaceSturdy(eastBlock, BlockFace.WEST), BlockFace.EAST);
        var southConnects = this.connectsTo(southBlock, this.isFaceSturdy(southBlock, BlockFace.NORTH), BlockFace.SOUTH);
        var westConnects = this.connectsTo(westBlock, this.isFaceSturdy(westBlock, BlockFace.EAST), BlockFace.WEST);

        var waterlogged = Objects.requireNonNullElse(currentBlock.getProperty("waterlogged"), "false");

        return this.updateShapeForUpdate(
                currentBlock,
                aboveBlock,
                northConnects,
                eastConnects,
                southConnects,
                westConnects,
                waterlogged
        );
    }

    private Block updateShapeForUpdate(
            Block currentBlock,
            Block aboveBlock,
            boolean northConnects,
            boolean eastConnects,
            boolean southConnects,
            boolean westConnects,
            String waterlogged
    ) {
        var north = this.makeWallState(northConnects);
        var east = this.makeWallState(eastConnects);
        var south = this.makeWallState(southConnects);
        var west = this.makeWallState(westConnects);

        var blockWithSides = currentBlock.withProperties(Map.of(
                "north", north,
                "east", east,
                "south", south,
                "west", west,
                "waterlogged", waterlogged
        ));

        var raisePost = this.shouldRaisePost(blockWithSides, aboveBlock);

        return blockWithSides.withProperty("up", Boolean.toString(raisePost));
    }

    private boolean connectsTo(Block blockState, boolean isFaceSturdy, BlockFace direction) {
        var isWall = Utility.hasTag(blockState, KEY);
        var isFenceGate = Utility.hasTag(blockState, FENCE_GATES) && this.isFenceGateAligned(blockState, direction);
        var isIronBars = blockState.compare(Block.IRON_BARS);
        var connectsToSturdy = !this.cannotConnect(blockState) && isFaceSturdy;
        return isWall || isFenceGate || isIronBars || connectsToSturdy;
    }

    private boolean isFenceGateAligned(Block fenceGate, BlockFace direction) {
        var facing = fenceGate.getProperty("facing");
        if (facing == null) {
            return false;
        }
        var gateAxis = switch (facing) {
            case "north", "south" -> "z";
            case "east", "west" -> "x";
            default -> null;
        };
        if (gateAxis == null) {
            return false;
        }
        var directionClockwiseAxis = switch (direction) {
            case NORTH, SOUTH -> "x";
            case EAST, WEST -> "z";
            default -> null;
        };
        if (directionClockwiseAxis == null) {
            return false;
        }
        return gateAxis.equals(directionClockwiseAxis);
    }

    private boolean cannotConnect(Block block) {
        return Utility.hasTag(block, LEAVES)
                || Utility.hasTag(block, SHULKER_BOXES)
                || block.compare(Block.BARRIER)
                || block.compare(Block.CARVED_PUMPKIN)
                || block.compare(Block.JACK_O_LANTERN)
                || block.compare(Block.MELON)
                || block.compare(Block.PUMPKIN);
    }

    private boolean isFaceSturdy(Block block, BlockFace face) {
        var registry = block.registry();
        var shape = registry != null ? registry.collisionShape() : null;
        return shape != null && shape.isFaceFull(face);
    }

    private String makeWallState(boolean connects) {
        return connects ? "low" : "none";
    }

    private boolean shouldRaisePost(Block wallBlock, Block aboveBlock) {
        var isAboveWall = Utility.hasTag(aboveBlock, KEY)
                && "true".equals(this.getWallSide(aboveBlock.getProperty("up")));

        if (isAboveWall) {
            return true;
        }

        var north = this.getWallSide(wallBlock.getProperty("north"));
        var south = this.getWallSide(wallBlock.getProperty("south"));
        var east = this.getWallSide(wallBlock.getProperty("east"));
        var west = this.getWallSide(wallBlock.getProperty("west"));

        var northNone = "none".equals(north);
        var southNone = "none".equals(south);
        var eastNone = "none".equals(east);
        var westNone = "none".equals(west);

        var allNone = northNone && southNone && eastNone && westNone;
        var asymmetric = northNone != southNone || eastNone != westNone;

        if (allNone || asymmetric) {
            return true;
        }

        var bothNorthSouthTall = "tall".equals(north) && "tall".equals(south);
        var bothEastWestTall = "tall".equals(east) && "tall".equals(west);

        return !(bothNorthSouthTall || bothEastWestTall);
    }

    private String getWallSide(String property) {
        return property == null ? "none" : property;
    }

    @Override
    public int maxUpdateDistance() {
        return 10;
    }
}

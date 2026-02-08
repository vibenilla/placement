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

public final class FencePlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:fences");
    private static final Key FENCE_GATES = Key.key("minecraft:fence_gates");
    private static final Key WOODEN_FENCES = Key.key("minecraft:wooden_fences");
    private static final Key LEAVES = Key.key("minecraft:leaves");
    private static final Key SHULKER_BOXES = Key.key("minecraft:shulker_boxes");

    public FencePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        if (!(blockGetter instanceof Instance instance)) {
            return placementState.block();
        }

        var placePosition = placementState.placePosition();
        var placed = placementState.block().withProperties(Map.of(
                "north", Boolean.toString(this.canConnect(blockGetter, placePosition.relative(BlockFace.NORTH), BlockFace.SOUTH)),
                "east", Boolean.toString(this.canConnect(blockGetter, placePosition.relative(BlockFace.EAST), BlockFace.WEST)),
                "south", Boolean.toString(this.canConnect(blockGetter, placePosition.relative(BlockFace.SOUTH), BlockFace.NORTH)),
                "west", Boolean.toString(this.canConnect(blockGetter, placePosition.relative(BlockFace.WEST), BlockFace.EAST)),
                "waterlogged", "false"
        ));

        VanillaPlacementUtils.scheduleHorizontalNeighborRuleUpdates(instance, placePosition);
        return placed;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var blockGetter = updateState.instance();
        var placePosition = updateState.blockPosition();
        var currentBlock = updateState.currentBlock();
        var updated = currentBlock.withProperties(Map.of(
                "north", Boolean.toString(this.canConnect(blockGetter, placePosition.relative(BlockFace.NORTH), BlockFace.SOUTH)),
                "east", Boolean.toString(this.canConnect(blockGetter, placePosition.relative(BlockFace.EAST), BlockFace.WEST)),
                "south", Boolean.toString(this.canConnect(blockGetter, placePosition.relative(BlockFace.SOUTH), BlockFace.NORTH)),
                "west", Boolean.toString(this.canConnect(blockGetter, placePosition.relative(BlockFace.WEST), BlockFace.EAST)),
                "waterlogged", Objects.requireNonNullElse(currentBlock.getProperty("waterlogged"), "false")
        ));

        return updated;
    }

    private boolean canConnect(Block.Getter blockGetter, Point neighborPosition, BlockFace direction) {
        var neighbor = blockGetter.getBlock(neighborPosition);
        var sameFence = this.isSameFence(neighbor);
        var fenceGate = Utility.hasTag(neighbor, FENCE_GATES) && this.isFenceGateAligned(neighbor, direction);
        var neighborFaceSturdy = this.isFaceFull(neighbor, direction);
        return (!this.cannotConnect(neighbor) && neighborFaceSturdy) || sameFence || fenceGate;
    }

    private boolean isSameFence(Block neighbor) {
        var neighborWooden = Utility.hasTag(neighbor, WOODEN_FENCES);
        var selfWooden = Utility.hasTag(this.block.defaultState(), WOODEN_FENCES);
        var tagFence = Utility.hasTag(neighbor, KEY) && neighborWooden == selfWooden;
        if (tagFence) {
            return true;
        }

        var neighborName = neighbor.key().value();
        var selfName = this.block.key().value();

        if (neighborName.endsWith("_fence") && selfName.endsWith("_fence")) {
            var neighborIsNetherBrickFence = neighborName.contains("nether_brick_fence");
            var selfIsNetherBrickFence = selfName.contains("nether_brick_fence");
            var sameFenceFamily = (neighborIsNetherBrickFence && selfIsNetherBrickFence)
                    || (!neighborIsNetherBrickFence && !selfIsNetherBrickFence);
            return sameFenceFamily;
        }

        return false;
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

    private boolean isFaceFull(Block block, BlockFace direction) {
        var registry = block.registry();
        var shape = registry != null ? registry.collisionShape() : null;
        return shape != null && shape.isFaceFull(direction);
    }

    @Override
    public int maxUpdateDistance() {
        return 10;
    }

}

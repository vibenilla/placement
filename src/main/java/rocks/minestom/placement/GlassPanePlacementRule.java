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

public final class GlassPanePlacementRule extends BlockPlacementRule {
    private static final Key WALLS = Key.key("minecraft:walls");
    private static final Key LEAVES = Key.key("minecraft:leaves");
    private static final Key SHULKER_BOXES = Key.key("minecraft:shulker_boxes");

    public GlassPanePlacementRule(@NotNull Block block) {
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
                "north", Boolean.toString(this.attachsTo(blockGetter, placePosition.relative(BlockFace.NORTH), BlockFace.SOUTH)),
                "east", Boolean.toString(this.attachsTo(blockGetter, placePosition.relative(BlockFace.EAST), BlockFace.WEST)),
                "south", Boolean.toString(this.attachsTo(blockGetter, placePosition.relative(BlockFace.SOUTH), BlockFace.NORTH)),
                "west", Boolean.toString(this.attachsTo(blockGetter, placePosition.relative(BlockFace.WEST), BlockFace.EAST)),
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
                "north", Boolean.toString(this.attachsTo(blockGetter, placePosition.relative(BlockFace.NORTH), BlockFace.SOUTH)),
                "east", Boolean.toString(this.attachsTo(blockGetter, placePosition.relative(BlockFace.EAST), BlockFace.WEST)),
                "south", Boolean.toString(this.attachsTo(blockGetter, placePosition.relative(BlockFace.SOUTH), BlockFace.NORTH)),
                "west", Boolean.toString(this.attachsTo(blockGetter, placePosition.relative(BlockFace.WEST), BlockFace.EAST)),
                "waterlogged", Objects.requireNonNullElse(currentBlock.getProperty("waterlogged"), "false")
        ));

        return updated;
    }

    private boolean attachsTo(Block.Getter blockGetter, Point neighborPosition, BlockFace direction) {
        var neighbor = blockGetter.getBlock(neighborPosition);
        var isFaceSturdy = this.isFaceSturdy(neighbor, direction);
        return this.attachsTo(neighbor, isFaceSturdy);
    }

    private boolean attachsTo(Block neighbor, boolean isFaceSturdy) {
        return (!this.isExceptionForConnection(neighbor) && isFaceSturdy)
                || this.isIronBars(neighbor)
                || Utility.hasTag(neighbor, WALLS);
    }

    private boolean isIronBars(Block block) {
        return block.compare(Block.IRON_BARS)
                || block.compare(Block.GLASS_PANE)
                || this.isStainedGlassPane(block);
    }

    private boolean isStainedGlassPane(Block block) {
        var name = block.key().value();
        return name.endsWith("_stained_glass_pane");
    }

    private boolean isExceptionForConnection(Block block) {
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

    @Override
    public int maxUpdateDistance() {
        return 10;
    }
}

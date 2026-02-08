package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SlabPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:slabs");

    public SlabPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var placePosition = placementState.placePosition();
        var blockGetter = placementState.instance();

        // Check if there's already a slab at this position
        var existingBlock = blockGetter.getBlock(placePosition, Block.Getter.Condition.TYPE);

        // If placing on an existing slab of the same type, make it a double slab
        if (existingBlock.compare(this.block, Block.Comparator.ID)) {
            var existingType = existingBlock.getProperty("type");
            if (existingType != null && !existingType.equals("double")) {
                return this.block.withProperty("type", "double");
            }
        }

        // Determine if it should be top or bottom based on placement context
        var placeFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var cursorY = Objects.requireNonNullElse(placementState.cursorPosition(), Vec.ZERO).y();

        // Minecraft logic: direction != DOWN && (direction == UP || !(cursorY > 0.5))
        // This means:
        // - If clicked on bottom face -> top slab
        // - If clicked on top face -> bottom slab
        // - If clicked on side and cursor Y <= 0.5 -> bottom slab
        // - If clicked on side and cursor Y > 0.5 -> top slab
        var type = (placeFace != BlockFace.BOTTOM && (placeFace == BlockFace.TOP || cursorY <= 0.5))
                ? "bottom"
                : "top";

        return this.block.withProperty("type", type);
    }
}

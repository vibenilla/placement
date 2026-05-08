package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class HugeMushroomPlacementRule extends BlockPlacementRule {
    private static final BlockFace[] DIRECTIONS = {
            BlockFace.TOP, BlockFace.BOTTOM, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
    };

    public HugeMushroomPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var result = this.block;

        for (var direction : DIRECTIONS) {
            var neighbor = instance.getBlock(placePosition.relative(direction));
            result = result.withProperty(propertyName(direction), String.valueOf(!neighbor.compare(this.block)));
        }

        return result;
    }

    private static String propertyName(@NotNull BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

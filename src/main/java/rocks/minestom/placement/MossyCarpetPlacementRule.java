package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class MossyCarpetPlacementRule extends BlockPlacementRule {
    public MossyCarpetPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var hasBase = isSturdyAbove(blockGetter, placePosition.relative(BlockFace.BOTTOM));

        return this.block
                .withProperty("base", String.valueOf(hasBase))
                .withProperty("north", side(blockGetter, placePosition, BlockFace.NORTH))
                .withProperty("east", side(blockGetter, placePosition, BlockFace.EAST))
                .withProperty("south", side(blockGetter, placePosition, BlockFace.SOUTH))
                .withProperty("west", side(blockGetter, placePosition, BlockFace.WEST));
    }

    private static String side(@NotNull Block.Getter blockGetter, @NotNull Point placePosition, @NotNull BlockFace face) {
        var neighbor = blockGetter.getBlock(placePosition.relative(face));

        if (neighbor.registry().collisionShape().isFaceFull(face.getOppositeFace())) {
            return "low";
        }
        return "none";
    }

    private static boolean isSturdyAbove(@NotNull Block.Getter blockGetter, @NotNull Point belowPosition) {
        var below = blockGetter.getBlock(belowPosition);
        return below.registry().collisionShape().isFaceFull(BlockFace.TOP);
    }
}

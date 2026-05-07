package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import rocks.minestom.placement.handlers.FacingXZBlock;
import rocks.minestom.placement.properties.enums.FacingXZ;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ShelfPlacementRule extends BlockPlacementRule {
    public ShelfPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        FacingXZ facing = FacingXZ.fromLook(placementState.playerPosition()).opposite();
        Block block = FacingXZBlock.FACING_XZ.get(facing).on(this.block);
        return genericUpdateState(placementState.instance(), block, placementState.placePosition());
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        return genericUpdateState(updateState.instance(), updateState.currentBlock(), updateState.blockPosition());
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private @NotNull Block genericUpdateState(@NotNull Block.Getter instance, @NotNull Block block, @NotNull Point blockPosition) {
        String facingProp = block.getProperty("facing");
        if (facingProp == null) return block;

        FacingXZ facing = FacingXZ.valueOf(facingProp.toUpperCase());
        FacingXZ leftDir = facing.rotateClockwise();
        FacingXZ rightDir = facing.rotateCounterClockwise();

        var left = instance.getBlock(blockPosition.relative(leftDir.toBlockFace()));
        var right = instance.getBlock(blockPosition.relative(rightDir.toBlockFace()));

        boolean leftIsShelf = left.getProperty("side_chain") != null;
        boolean rightIsShelf = right.getProperty("side_chain") != null;

        if (leftIsShelf && rightIsShelf) {
            return block.withProperty("side_chain", "center");
        } else if (leftIsShelf) {
            return block.withProperty("side_chain", "right");
        } else if (rightIsShelf) {
            return block.withProperty("side_chain", "left");
        } else {
            return block.withProperty("side_chain", "unconnected");
        }
    }
}

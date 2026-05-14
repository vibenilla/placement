package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PointedDripstonePlacementRule extends BlockPlacementRule {
    public PointedDripstonePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var playerPosition = placementState.playerPosition();
        var pitch = playerPosition == null ? 0.0F : playerPosition.pitch();
        var defaultTipDirection = pitch < 0.0F ? BlockFace.BOTTOM : BlockFace.TOP;
        var tipDirection = this.calculateTipDirection(blockGetter, placePosition, defaultTipDirection);

        if (tipDirection == null) {
            return null;
        }

        var mergeOpposingTips = !placementState.isPlayerShifting();
        var thickness = this.calculateThickness(blockGetter, placePosition, tipDirection, mergeOpposingTips);
        var replaced = blockGetter.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));

        return this.block
                .withProperty("vertical_direction", verticalName(tipDirection))
                .withProperty("thickness", thickness)
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    private @Nullable BlockFace calculateTipDirection(@NotNull Block.Getter blockGetter, @NotNull Point placePosition, @NotNull BlockFace defaultTipDirection) {
        if (this.isValidPlacement(blockGetter, placePosition, defaultTipDirection)) {
            return defaultTipDirection;
        }

        var opposite = defaultTipDirection.getOppositeFace();

        if (this.isValidPlacement(blockGetter, placePosition, opposite)) {
            return opposite;
        }
        return null;
    }

    private boolean isValidPlacement(@NotNull Block.Getter blockGetter, @NotNull Point placePosition, @NotNull BlockFace tipDirection) {
        var attachmentFace = tipDirection.getOppositeFace();
        var attachmentPosition = placePosition.relative(attachmentFace);
        var attachmentBlock = blockGetter.getBlock(attachmentPosition);

        if (attachmentBlock.registry().collisionShape().isFaceFull(tipDirection)) {
            return true;
        }
        return attachmentBlock.compare(this.block) && isMatchingDirection(attachmentBlock, tipDirection);
    }

    private String calculateThickness(@NotNull Block.Getter blockGetter, @NotNull Point placePosition, @NotNull BlockFace tipDirection, boolean mergeOpposingTips) {
        var aheadPosition = placePosition.relative(tipDirection);
        var aheadBlock = blockGetter.getBlock(aheadPosition);
        var oppositeDirection = tipDirection.getOppositeFace();
        var behindPosition = placePosition.relative(oppositeDirection);
        var behindBlock = blockGetter.getBlock(behindPosition);

        if (!isPointedDripstone(aheadBlock, this.block)) {
            return mergeOpposingTips && isPointedDripstone(behindBlock, this.block) && isMatchingDirection(behindBlock, oppositeDirection)
                    ? "tip_merge"
                    : "tip";
        }

        if (!isMatchingDirection(aheadBlock, tipDirection)) {
            return "tip";
        }

        if (!isPointedDripstone(behindBlock, this.block) || !isMatchingDirection(behindBlock, tipDirection)) {
            return "frustum";
        }
        return aheadBlock.getProperty("thickness") != null && "tip".equals(aheadBlock.getProperty("thickness"))
                ? "middle"
                : "base";
    }

    private static boolean isPointedDripstone(@NotNull Block candidate, @NotNull Block dripstoneBlock) {
        return candidate.compare(dripstoneBlock);
    }

    private static boolean isMatchingDirection(@NotNull Block dripstoneBlock, @NotNull BlockFace expected) {
        return verticalName(expected).equals(dripstoneBlock.getProperty("vertical_direction"));
    }

    private static String verticalName(@NotNull BlockFace face) {
        return face == BlockFace.TOP ? "up" : "down";
    }
}

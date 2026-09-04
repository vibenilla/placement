package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.Nullable;

public final class PointedDripstonePlacementRule extends BlockPlacementRule {
    public PointedDripstonePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
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

        return placementState.block()
                .withProperty("vertical_direction", verticalName(tipDirection))
                .withProperty("thickness", thickness)
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var tipDirection = parseDirection(currentBlock.getProperty("vertical_direction"));

        if (tipDirection == null || updateState.fromFace() != tipDirection.getOppositeFace()) {
            return currentBlock;
        }

        return this.isValidPlacement(updateState.instance(), updateState.blockPosition(), tipDirection)
                ? currentBlock
                : Block.AIR;
    }

    private @Nullable BlockFace calculateTipDirection(Block.Getter blockGetter, Point placePosition, BlockFace defaultTipDirection) {
        if (this.isValidPlacement(blockGetter, placePosition, defaultTipDirection)) {
            return defaultTipDirection;
        }

        var opposite = defaultTipDirection.getOppositeFace();

        if (this.isValidPlacement(blockGetter, placePosition, opposite)) {
            return opposite;
        }

        return null;
    }

    private boolean isValidPlacement(Block.Getter blockGetter, Point placePosition, BlockFace tipDirection) {
        var attachmentFace = tipDirection.getOppositeFace();
        var attachmentPosition = placePosition.relative(attachmentFace);
        var attachmentBlock = blockGetter.getBlock(attachmentPosition);

        if (attachmentBlock.registry().collisionShape().isFaceFull(tipDirection)) {
            return true;
        }

        return attachmentBlock.compare(this.block) && isMatchingDirection(attachmentBlock, tipDirection);
    }

    private String calculateThickness(Block.Getter blockGetter, Point placePosition, BlockFace tipDirection, boolean mergeOpposingTips) {
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

    private static boolean isPointedDripstone(Block candidate, Block dripstoneBlock) {
        return candidate.compare(dripstoneBlock);
    }

    private static BlockFace parseDirection(String directionName) {
        return switch (directionName) {
            case "up" -> BlockFace.TOP;
            case "down" -> BlockFace.BOTTOM;
            case null, default -> null;
        };
    }

    private static boolean isMatchingDirection(Block dripstoneBlock, BlockFace expected) {
        return verticalName(expected).equals(dripstoneBlock.getProperty("vertical_direction"));
    }

    private static String verticalName(BlockFace face) {
        return face == BlockFace.TOP ? "up" : "down";
    }
}

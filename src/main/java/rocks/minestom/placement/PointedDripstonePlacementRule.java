package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.Nullable;

public final class PointedDripstonePlacementRule extends BlockPlacementRule {
    private static final Key SPELEOTHEMS = Key.key("minecraft:speleothems");

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
        var waterlogged = replaced.compare(Block.WATER);

        return placementState.block()
                .withProperty("vertical_direction", verticalName(tipDirection))
                .withProperty("thickness", thickness)
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var tipDirection = parseDirection(currentBlock.getProperty("vertical_direction"));

        if (tipDirection == null || (updateState.fromFace() != BlockFace.TOP && updateState.fromFace() != BlockFace.BOTTOM)) {
            return currentBlock;
        }

        if (!this.isValidPlacement(updateState.instance(), updateState.blockPosition(), tipDirection)) {
            return Block.AIR;
        }

        var mergeOpposingTips = "tip_merge".equals(currentBlock.getProperty("thickness"));
        var thickness = this.calculateThickness(
                updateState.instance(), updateState.blockPosition(), tipDirection, mergeOpposingTips);
        return currentBlock.withProperty("thickness", thickness);
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

        if (isMatchingSpeleothem(aheadBlock, oppositeDirection) && aheadBlock.compare(this.block)) {
            return !mergeOpposingTips && !"tip_merge".equals(aheadBlock.getProperty("thickness"))
                    ? "tip"
                    : "tip_merge";
        }

        if (!isMatchingSpeleothem(aheadBlock, tipDirection)) {
            return "tip";
        }

        var aheadThickness = aheadBlock.getProperty("thickness");

        if ("tip".equals(aheadThickness) || "tip_merge".equals(aheadThickness)) {
            return "frustum";
        }

        return isMatchingSpeleothem(behindBlock, tipDirection) ? "middle" : "base";
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

    private static boolean isMatchingSpeleothem(Block block, BlockFace expected) {
        return Utility.hasTag(block, SPELEOTHEMS) && isMatchingDirection(block, expected);
    }

    private static String verticalName(BlockFace face) {
        return face == BlockFace.TOP ? "up" : "down";
    }
}

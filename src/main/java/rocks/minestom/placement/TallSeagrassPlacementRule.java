package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class TallSeagrassPlacementRule extends BlockPlacementRule {
    private static final Key CANNOT_SUPPORT_SEAGRASS_TAG = Key.key("minecraft:cannot_support_seagrass");

    public TallSeagrassPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!(placementState.instance() instanceof Instance instance)) {
            return null;
        }

        var placePosition = placementState.placePosition();
        var upperPosition = placePosition.relative(BlockFace.TOP);

        if (upperPosition.blockY() >= instance.getCachedDimensionType().maxY()) {
            return null;
        }

        var lowerWater = instance.getBlock(placePosition);
        var upperWater = instance.getBlock(upperPosition);
        var below = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!isSourceWater(lowerWater) || !isSourceWater(upperWater) || !canSupport(below)) {
            return null;
        }

        instance.setBlock(upperPosition, placementState.block().withProperty("half", "upper"), false);
        return placementState.block().withProperty("half", "lower");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var half = currentBlock.getProperty("half");
        var fromFace = updateState.fromFace();

        if ("lower".equals(half) && fromFace == BlockFace.TOP) {
            var above = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.TOP));
            return this.isMatchingHalf(above, "upper") ? currentBlock : Block.AIR;
        }

        if ("lower".equals(half) && fromFace == BlockFace.BOTTOM) {
            var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
            return canSupport(below) ? currentBlock : Block.AIR;
        }

        if ("upper".equals(half) && fromFace == BlockFace.BOTTOM) {
            var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
            return this.isMatchingHalf(below, "lower") ? currentBlock : Block.AIR;
        }

        return currentBlock;
    }

    private boolean isMatchingHalf(Block candidate, String half) {
        return candidate.compare(this.block) && half.equals(candidate.getProperty("half"));
    }

    private static boolean canSupport(Block block) {
        return !Utility.hasTag(block, CANNOT_SUPPORT_SEAGRASS_TAG)
                && block.registry().collisionShape().isFaceFull(BlockFace.TOP);
    }

    private static boolean isSourceWater(Block block) {
        return block.compare(Block.WATER) && "0".equals(block.getProperty("level"));
    }
}

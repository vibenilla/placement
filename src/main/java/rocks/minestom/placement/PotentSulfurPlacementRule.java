package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class PotentSulfurPlacementRule extends BlockPlacementRule {
    private static final Key CONTINUOUS_GEYSER_TAG = Key.key("minecraft:causes_continuous_geyser_eruptions");
    private static final Key PERIODIC_GEYSER_TAG = Key.key("minecraft:causes_periodic_geyser_eruptions");

    public PotentSulfurPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        return withValidState(
                placementState.block(), placementState.instance(), placementState.placePosition());
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        return withValidState(
                updateState.currentBlock(), updateState.instance(), updateState.blockPosition());
    }

    private static Block withValidState(Block block, Block.Getter blockGetter, Point position) {
        var above = blockGetter.getBlock(position.relative(BlockFace.TOP));

        if (!isSourceWater(above)) {
            return block.withProperty("potent_sulfur_state", "dry");
        }

        var below = blockGetter.getBlock(position.relative(BlockFace.BOTTOM));

        if (Utility.hasTag(below, CONTINUOUS_GEYSER_TAG) && isSourceIfFluid(below)) {
            return block.withProperty("potent_sulfur_state", "continuous");
        }

        if (Utility.hasTag(below, PERIODIC_GEYSER_TAG) && isSourceIfFluid(below)) {
            return "erupting".equals(block.getProperty("potent_sulfur_state"))
                    ? block
                    : block.withProperty("potent_sulfur_state", "dormant");
        }

        return block.withProperty("potent_sulfur_state", "wet");
    }

    private static boolean isSourceIfFluid(Block block) {
        return !block.isFluid() || "0".equals(block.getProperty("level"));
    }

    private static boolean isSourceWater(Block block) {
        return block.compare(Block.WATER) && "0".equals(block.getProperty("level"));
    }
}

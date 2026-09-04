package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class SnowyPlacementRule extends BlockPlacementRule {
    private static final Key SNOW_TAG = Key.key("minecraft:snow");

    public SnowyPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var above = placementState.instance().getBlock(placementState.placePosition().relative(BlockFace.TOP));
        return placementState.block().withProperty("snowy", String.valueOf(Utility.hasTag(above, SNOW_TAG)));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.TOP) {
            return updateState.currentBlock();
        }

        var above = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.TOP));
        return updateState.currentBlock().withProperty("snowy", String.valueOf(Utility.hasTag(above, SNOW_TAG)));
    }
}

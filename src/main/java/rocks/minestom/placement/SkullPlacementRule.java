package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class SkullPlacementRule extends BlockPlacementRule {
    public SkullPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var rotation = Math.round(yaw * 16.0F / 360.0F) & 15;

        var powered = VanillaPlacementUtils.hasNeighborSignal(
                placementState.instance(), placementState.placePosition());

        return placementState.block()
                .withProperty("rotation", Integer.toString(rotation))
                .withProperty("powered", String.valueOf(powered));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var powered = VanillaPlacementUtils.hasNeighborSignal(
                updateState.instance(), updateState.blockPosition());
        return updateState.currentBlock().withProperty("powered", String.valueOf(powered));
    }
}

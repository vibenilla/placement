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

        // TODO: powered should reflect hasNeighborSignal at placePosition; needs neighbor redstone scan + blockUpdate handling.
        return this.block
                .withProperty("rotation", Integer.toString(rotation))
                .withProperty("powered", "false");
    }
}

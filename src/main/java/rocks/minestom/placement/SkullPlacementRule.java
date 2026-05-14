package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class SkullPlacementRule extends BlockPlacementRule {
    public SkullPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var rotation = Math.round(yaw * 16.0F / 360.0F) & 15;

        // TODO: powered should reflect hasNeighborSignal at placePosition; needs neighbor redstone scan + blockUpdate handling.
        return this.block
                .withProperty("rotation", Integer.toString(rotation))
                .withProperty("powered", "false");
    }
}

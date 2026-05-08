package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class StandingSignPlacementRule extends BlockPlacementRule {
    public StandingSignPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var rotation = Math.round((yaw + 180.0F) * 16.0F / 360.0F) & 15;
        var waterlogged = placementState.instance().getBlock(placementState.placePosition()).compare(Block.WATER);

        return this.block
                .withProperty("rotation", Integer.toString(rotation))
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }
}

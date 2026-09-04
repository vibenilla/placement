package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.Nullable;

public final class WaterloggedHorizontalFacingPlacementRule extends BlockPlacementRule {
    private final boolean awayFromPlayer;
    private final @Nullable BlockHandler handler;

    public WaterloggedHorizontalFacingPlacementRule(Block block, boolean awayFromPlayer, @Nullable BlockHandler handler) {
        super(block);
        this.awayFromPlayer = awayFromPlayer;
        this.handler = handler;
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw);

        if (!this.awayFromPlayer) {
            facing = facing.getOppositeFace();
        }

        var replaced = placementState.instance().getBlock(placementState.placePosition());
        var result = this.handler == null ? placementState.block() : placementState.block().withHandler(this.handler);
        return result
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("waterlogged", String.valueOf(replaced.compare(Block.WATER)));
    }
}

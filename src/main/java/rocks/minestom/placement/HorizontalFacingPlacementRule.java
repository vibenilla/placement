package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.Nullable;

public final class HorizontalFacingPlacementRule extends BlockPlacementRule {
    private final boolean awayFromPlayer;
    private final @Nullable BlockHandler handler;

    public HorizontalFacingPlacementRule(Block block) {
        this(block, false, null);
    }

    public HorizontalFacingPlacementRule(Block block, boolean awayFromPlayer) {
        this(block, awayFromPlayer, null);
    }

    public HorizontalFacingPlacementRule(Block block, @Nullable BlockHandler handler) {
        this(block, false, handler);
    }

    public HorizontalFacingPlacementRule(Block block, boolean awayFromPlayer, @Nullable BlockHandler handler) {
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

        var result = this.handler == null ? placementState.block() : placementState.block().withHandler(this.handler);
        return result.withProperty("facing", facingName(facing));
    }

    private static String facingName(BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

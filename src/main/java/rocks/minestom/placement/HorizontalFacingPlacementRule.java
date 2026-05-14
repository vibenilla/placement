package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HorizontalFacingPlacementRule extends BlockPlacementRule {
    private final boolean awayFromPlayer;
    private final @Nullable BlockHandler handler;

    public HorizontalFacingPlacementRule(@NotNull Block block) {
        this(block, false, null);
    }

    public HorizontalFacingPlacementRule(@NotNull Block block, boolean awayFromPlayer) {
        this(block, awayFromPlayer, null);
    }

    public HorizontalFacingPlacementRule(@NotNull Block block, @Nullable BlockHandler handler) {
        this(block, false, handler);
    }

    public HorizontalFacingPlacementRule(@NotNull Block block, boolean awayFromPlayer, @Nullable BlockHandler handler) {
        super(block);
        this.awayFromPlayer = awayFromPlayer;
        this.handler = handler;
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw);

        if (!this.awayFromPlayer) {
            facing = facing.getOppositeFace();
        }

        var result = this.handler == null ? this.block : this.block.withHandler(this.handler);
        return result.withProperty("facing", facingName(facing));
    }

    private static String facingName(@NotNull BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

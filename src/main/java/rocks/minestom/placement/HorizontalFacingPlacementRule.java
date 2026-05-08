package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class HorizontalFacingPlacementRule extends BlockPlacementRule {
    private final boolean awayFromPlayer;

    public HorizontalFacingPlacementRule(@NotNull Block block) {
        this(block, false);
    }

    public HorizontalFacingPlacementRule(@NotNull Block block, boolean awayFromPlayer) {
        super(block);
        this.awayFromPlayer = awayFromPlayer;
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw);

        if (!this.awayFromPlayer) {
            facing = facing.getOppositeFace();
        }

        return this.block.withProperty("facing", facingName(facing));
    }

    private static String facingName(@NotNull BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

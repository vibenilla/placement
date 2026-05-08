package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class JigsawPlacementRule extends BlockPlacementRule {
    public JigsawPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var front = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var top = front.toDirection().vertical() ? BlockFace.fromYaw(yaw).getOppositeFace() : BlockFace.TOP;

        return this.block.withProperty("orientation", orientationName(front) + "_" + orientationName(top));
    }

    private static String orientationName(@NotNull BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

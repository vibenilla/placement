package rocks.minestom.placement;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Code from <a href="https://github.com/vibenilla/placement">vibenilla placement</a>
 * Licensed under Apache License 2.0.
 */

public final class HorizontalFacingPlacementRule extends BlockPlacementRule {
    public HorizontalFacingPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var facing = this.getFacingDirection(playerPosition);
        return this.block.withProperty("facing", facing.name().toLowerCase());
    }

    private Direction getFacingDirection(@Nullable Pos position) {
        if (position == null) {
            return Direction.NORTH;
        }

        return MathUtils.getHorizontalDirection(position.yaw()).opposite();
    }
}

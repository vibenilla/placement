package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class RailPlacementRule extends BlockPlacementRule {
    public RailPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var direction = BlockFace.fromYaw(yaw);
        var shape = (direction == BlockFace.EAST || direction == BlockFace.WEST) ? "east_west" : "north_south";
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        // TODO: vanilla calls new RailState(level, pos, state).place(...) to reshape rails based on neighbors (S/E corners, ascending slopes, etc.); not implemented
        return this.block
                .withProperty("shape", shape)
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }
}

package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class AnvilPlacementRule extends BlockPlacementRule {
    public AnvilPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var horizontalDirection = BlockFace.fromYaw(yaw);
        var facing = clockwise(horizontalDirection);

        return this.block
                .withHandler(ConsumeInteractionBlockHandler.INSTANCE)
                .withProperty("facing", facing.name().toLowerCase());
    }

    private static BlockFace clockwise(@NotNull BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> face;
        };
    }
}

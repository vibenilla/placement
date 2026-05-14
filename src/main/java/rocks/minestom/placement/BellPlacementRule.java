package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BellPlacementRule extends BlockPlacementRule {
    public BellPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var clickedFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var horizontalFacing = BlockFace.fromYaw(yaw);

        if (clickedFace == BlockFace.TOP || clickedFace == BlockFace.BOTTOM) {
            var attachment = clickedFace == BlockFace.BOTTOM ? "ceiling" : "floor";

            return this.block
                    .withHandler(ConsumeInteractionBlockHandler.INSTANCE)
                    .withProperty("attachment", attachment)
                    .withProperty("facing", horizontalFacing.name().toLowerCase());
        }

        var facing = clickedFace.getOppositeFace();
        var doubleAttached = isDoubleAttached(placementState.instance(), placementState.placePosition(), clickedFace);
        var attachment = doubleAttached ? "double_wall" : "single_wall";

        return this.block
                .withHandler(ConsumeInteractionBlockHandler.INSTANCE)
                .withProperty("attachment", attachment)
                .withProperty("facing", facing.name().toLowerCase());
    }

    private static boolean isDoubleAttached(@NotNull Block.Getter blockGetter, @NotNull Point position, @NotNull BlockFace clickedFace) {
        if (clickedFace == BlockFace.WEST || clickedFace == BlockFace.EAST) {
            var westNeighbor = blockGetter.getBlock(position.relative(BlockFace.WEST));
            var eastNeighbor = blockGetter.getBlock(position.relative(BlockFace.EAST));
            return westNeighbor.registry().collisionShape().isFaceFull(BlockFace.EAST)
                    && eastNeighbor.registry().collisionShape().isFaceFull(BlockFace.WEST);
        }

        if (clickedFace == BlockFace.NORTH || clickedFace == BlockFace.SOUTH) {
            var northNeighbor = blockGetter.getBlock(position.relative(BlockFace.NORTH));
            var southNeighbor = blockGetter.getBlock(position.relative(BlockFace.SOUTH));
            return northNeighbor.registry().collisionShape().isFaceFull(BlockFace.SOUTH)
                    && southNeighbor.registry().collisionShape().isFaceFull(BlockFace.NORTH);
        }

        return false;
    }
}

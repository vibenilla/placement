package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FenceGatePlacementRule extends BlockPlacementRule {
    public FenceGatePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        // TODO: vanilla checks hasNeighborSignal(pos) and sets open=powered=true; not implemented
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw);
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var inWall = isInWall(blockGetter, placePosition, facing);

        return this.block
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("in_wall", String.valueOf(inWall))
                .withProperty("open", "false")
                .withProperty("powered", "false");
    }

    private static boolean isInWall(@NotNull Block.Getter blockGetter, @NotNull Point placePosition, @NotNull BlockFace facing) {
        var wallsTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:walls"));

        if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
            return isWall(blockGetter, placePosition.relative(BlockFace.WEST), wallsTag)
                    || isWall(blockGetter, placePosition.relative(BlockFace.EAST), wallsTag);
        }

        return isWall(blockGetter, placePosition.relative(BlockFace.NORTH), wallsTag)
                || isWall(blockGetter, placePosition.relative(BlockFace.SOUTH), wallsTag);
    }

    private static boolean isWall(@NotNull Block.Getter blockGetter, @NotNull Point neighborPosition, @Nullable RegistryTag<Block> wallsTag) {
        var neighbor = blockGetter.getBlock(neighborPosition);
        return wallsTag != null && wallsTag.contains(neighbor);
    }
}

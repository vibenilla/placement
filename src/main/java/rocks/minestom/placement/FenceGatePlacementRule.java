package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.Nullable;

public final class FenceGatePlacementRule extends BlockPlacementRule {
    public FenceGatePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        // TODO: vanilla checks hasNeighborSignal(pos) and sets open=powered=true; not implemented
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw);
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var inWall = isInWall(blockGetter, placePosition, facing);

        return placementState.block()
                .withHandler(FenceGateBlockHandler.INSTANCE)
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("in_wall", String.valueOf(inWall))
                .withProperty("open", "false")
                .withProperty("powered", "false");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var fromFace = updateState.fromFace();

        if (fromFace == BlockFace.TOP || fromFace == BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var facing = parseFacing(updateState.currentBlock().getProperty("facing"));

        if (facing == null) {
            return updateState.currentBlock();
        }

        var inWall = isInWall(updateState.instance(), updateState.blockPosition(), facing);
        return updateState.currentBlock().withProperty("in_wall", String.valueOf(inWall));
    }

    private static boolean isInWall(Block.Getter blockGetter, Point placePosition, BlockFace facing) {
        var wallsTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:walls"));

        if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
            return isWall(blockGetter, placePosition.relative(BlockFace.WEST), wallsTag)
                    || isWall(blockGetter, placePosition.relative(BlockFace.EAST), wallsTag);
        }

        return isWall(blockGetter, placePosition.relative(BlockFace.NORTH), wallsTag)
                || isWall(blockGetter, placePosition.relative(BlockFace.SOUTH), wallsTag);
    }

    private static boolean isWall(Block.Getter blockGetter, Point neighborPosition, @Nullable RegistryTag<Block> wallsTag) {
        var neighbor = blockGetter.getBlock(neighborPosition);
        return wallsTag != null && wallsTag.contains(neighbor);
    }

    private static @Nullable BlockFace parseFacing(String facingName) {
        return switch (facingName) {
            case "north" -> BlockFace.NORTH;
            case "east" -> BlockFace.EAST;
            case "south" -> BlockFace.SOUTH;
            case "west" -> BlockFace.WEST;
            case null, default -> null;
        };
    }
}

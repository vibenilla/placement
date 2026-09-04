package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.instance.Instance;
import net.kyori.adventure.sound.Sound;

import java.util.concurrent.ThreadLocalRandom;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.Nullable;

public final class FenceGatePlacementRule extends BlockPlacementRule {
    public FenceGatePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw);
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var inWall = isInWall(blockGetter, placePosition, facing);
        var powered = VanillaPlacementUtils.hasNeighborSignal(blockGetter, placePosition);

        return placementState.block()
                .withHandler(FenceGateBlockHandler.INSTANCE)
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("in_wall", String.valueOf(inWall))
                .withProperty("open", String.valueOf(powered))
                .withProperty("powered", String.valueOf(powered));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var fromFace = updateState.fromFace();

        var current = updateState.currentBlock();
        var facing = parseFacing(current.getProperty("facing"));

        if (facing == null) {
            return current;
        }

        var updated = current;

        if (fromFace != BlockFace.TOP && fromFace != BlockFace.BOTTOM) {
            var inWall = isInWall(updateState.instance(), updateState.blockPosition(), facing);
            updated = updated.withProperty("in_wall", String.valueOf(inWall));
        }

        var powered = VanillaPlacementUtils.hasNeighborSignal(
                updateState.instance(), updateState.blockPosition());

        var wasPowered = "true".equals(current.getProperty("powered"));

        if (powered != wasPowered) {
            if (updateState.instance() instanceof Instance instance) {
                instance.playSound(
                        Sound.sound(FenceGateBlockHandler.soundEvent(current, powered), Sound.Source.BLOCK, 1.0F,
                                ThreadLocalRandom.current().nextFloat() * 0.1F + 0.9F),
                        updateState.blockPosition().add(0.5D, 0.5D, 0.5D));
            }

            updated = updated
                    .withProperty("powered", String.valueOf(powered))
                    .withProperty("open", String.valueOf(powered));
        }

        return updated;
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

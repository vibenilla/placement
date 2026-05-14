package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CocoaPlacementRule extends BlockPlacementRule {
    public CocoaPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var pitch = playerPosition == null ? 0.0F : playerPosition.pitch();
        var nearest = playerPosition == null
                ? new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.TOP, BlockFace.BOTTOM}
                : orderedByNearest(yaw, pitch);
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var jungleLogs = MinecraftServer.process().blocks().getTag(Key.key("minecraft:jungle_logs"));
        BlockFace facing = null;

        for (var direction : nearest) {
            if (!isHorizontal(direction)) {
                continue;
            }

            var supportBlock = instance.getBlock(placePosition.relative(direction));

            if (isJungleLog(supportBlock, jungleLogs)) {
                facing = direction;
                break;
            }
        }

        if (facing == null) {
            return null;
        }

        return this.block.withProperty("facing", facing.name().toLowerCase());
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var facing = parseFacing(currentBlock.getProperty("facing"));

        if (facing == null) {
            return currentBlock;
        }

        if (updateState.fromFace() != facing) {
            return currentBlock;
        }
        var supportBlock = updateState.instance().getBlock(updateState.blockPosition().relative(facing));
        var jungleLogs = MinecraftServer.process().blocks().getTag(Key.key("minecraft:jungle_logs"));

        if (!isJungleLog(supportBlock, jungleLogs)) {
            return Block.AIR;
        }
        return currentBlock;
    }

    private static BlockFace parseFacing(String facingName) {
        return switch (facingName) {
            case "north" -> BlockFace.NORTH;
            case "east" -> BlockFace.EAST;
            case "south" -> BlockFace.SOUTH;
            case "west" -> BlockFace.WEST;
            case null, default -> null;
        };
    }

    private static boolean isJungleLog(@NotNull Block block, @Nullable RegistryTag<Block> jungleLogs) {
        if (jungleLogs != null) {
            return jungleLogs.contains(block);
        }
        return block.compare(Block.JUNGLE_LOG) || block.compare(Block.STRIPPED_JUNGLE_LOG);
    }

    private static boolean isHorizontal(@NotNull BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.EAST || face == BlockFace.WEST;
    }

    private static BlockFace[] orderedByNearest(float yawDegrees, float pitchDegrees) {
        var pitchRadians = pitchDegrees * (float) (Math.PI / 180.0D);
        var yawRadians = -yawDegrees * (float) (Math.PI / 180.0D);
        var pitchSin = (float) Math.sin(pitchRadians);
        var pitchCos = (float) Math.cos(pitchRadians);
        var yawSin = (float) Math.sin(yawRadians);
        var yawCos = (float) Math.cos(yawRadians);
        var xPositive = yawSin > 0.0F;
        var yPositive = pitchSin < 0.0F;
        var zPositive = yawCos > 0.0F;
        var xYaw = xPositive ? yawSin : -yawSin;
        var yMagnitude = yPositive ? -pitchSin : pitchSin;
        var zYaw = zPositive ? yawCos : -yawCos;
        var xMagnitude = xYaw * pitchCos;
        var zMagnitude = zYaw * pitchCos;
        var axisX = xPositive ? BlockFace.EAST : BlockFace.WEST;
        var axisY = yPositive ? BlockFace.TOP : BlockFace.BOTTOM;
        var axisZ = zPositive ? BlockFace.SOUTH : BlockFace.NORTH;

        if (xYaw > zYaw) {
            if (yMagnitude > xMagnitude) {
                return makeDirectionArray(axisY, axisX, axisZ);
            }

            if (zMagnitude > yMagnitude) {
                return makeDirectionArray(axisX, axisZ, axisY);
            }
            return makeDirectionArray(axisX, axisY, axisZ);
        }

        if (yMagnitude > zMagnitude) {
            return makeDirectionArray(axisY, axisZ, axisX);
        }

        if (xMagnitude > yMagnitude) {
            return makeDirectionArray(axisZ, axisX, axisY);
        }
        return makeDirectionArray(axisZ, axisY, axisX);
    }

    private static BlockFace[] makeDirectionArray(@NotNull BlockFace first, @NotNull BlockFace second, @NotNull BlockFace third) {
        return new BlockFace[]{first, second, third, third.getOppositeFace(), second.getOppositeFace(), first.getOppositeFace()};
    }
}

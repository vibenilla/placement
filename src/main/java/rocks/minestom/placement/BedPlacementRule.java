package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BedPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:beds");

    public BedPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        if (!(placementState.instance() instanceof Instance instance)) {
            return null;
        }

        var playerPosition = placementState.playerPosition();
        var facing = getFacingDirection(playerPosition);

        var placePosition = placementState.placePosition();
        var dimension = instance.getCachedDimensionType();

        var baseX = placePosition.blockX();
        var baseY = placePosition.blockY();
        var baseZ = placePosition.blockZ();

        if (baseY < dimension.minY() || baseY >= dimension.maxY()) {
            return null;
        }

        var headOffset = getDirectionOffset(facing);
        var headX = baseX + headOffset.x();
        var headY = baseY + headOffset.y();
        var headZ = baseZ + headOffset.z();

        var headBlock = instance.getBlock(headX, headY, headZ);
        if (!isReplaceable(headBlock)) {
            return null;
        }

        var configured = placementState.block()
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("occupied", "false");

        var headPosition = placePosition.add(headOffset.x(), headOffset.y(), headOffset.z());
        instance.setBlock(headPosition, configured.withProperty("part", "head"));

        return configured.withProperty("part", "foot");
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var part = currentBlock.getProperty("part");
        var facing = currentBlock.getProperty("facing");

        if (part == null || facing == null) {
            return currentBlock;
        }

        var direction = Direction.valueOf(facing.toUpperCase());
        var neighborDirection = part.equals("foot") ? direction : direction.opposite();

        var neighborOffset = getDirectionOffset(neighborDirection);
        var neighborX = updateState.blockPosition().blockX() + neighborOffset.x();
        var neighborY = updateState.blockPosition().blockY() + neighborOffset.y();
        var neighborZ = updateState.blockPosition().blockZ() + neighborOffset.z();

        var neighborBlock = updateState.instance().getBlock(neighborX, neighborY, neighborZ);

        if (!isBedPart(neighborBlock, getOppositePart(part), facing)) {
            return Block.AIR;
        }

        var neighborOccupied = neighborBlock.getProperty("occupied");
        if (neighborOccupied != null) {
            return currentBlock.withProperty("occupied", neighborOccupied);
        }

        return currentBlock;
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private static Direction getFacingDirection(@Nullable Pos position) {
        return position == null ? Direction.NORTH : MathUtils.getHorizontalDirection(position.yaw());
    }

    private static boolean isReplaceable(Block block) {
        return block.isAir() || block.registry().isReplaceable();
    }

    private static boolean isBedPart(Block block, String expectedPart, String expectedFacing) {
        var part = block.getProperty("part");
        var facing = block.getProperty("facing");
        return expectedPart.equals(part) && expectedFacing.equals(facing);
    }

    private static String getOppositePart(String part) {
        return part.equals("foot") ? "head" : "foot";
    }

    private static BlockOffset getDirectionOffset(Direction direction) {
        return switch (direction) {
            case NORTH -> new BlockOffset(0, 0, -1);
            case SOUTH -> new BlockOffset(0, 0, 1);
            case WEST -> new BlockOffset(-1, 0, 0);
            case EAST -> new BlockOffset(1, 0, 0);
            default -> new BlockOffset(0, 0, 0);
        };
    }

    private record BlockOffset(int x, int y, int z) {
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WallSignPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:wall_signs");

    public WallSignPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var blockFace = placementState.blockFace();
        if (blockFace == null) {
            return null;
        }

        var placePosition = placementState.placePosition();
        var instance = placementState.instance();

        var blockX = placePosition.blockX();
        var blockY = placePosition.blockY();
        var blockZ = placePosition.blockZ();

        var facing = switch (blockFace) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
            default -> null;
        };

        if (facing == null) {
            return null;
        }

        var supportDirection = facing.opposite();
        var blockBehind = instance.getBlock(
                blockX + supportDirection.normalX(),
                blockY,
                blockZ + supportDirection.normalZ()
        );

        if (!blockBehind.registry().isSolid()) {
            return null;
        }

        return this.block.withProperty("facing", facing.name().toLowerCase());
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var facing = currentBlock.getProperty("facing");

        if (facing == null) {
            return Block.AIR;
        }

        var direction = Direction.valueOf(facing.toUpperCase());
        var supportDirection = direction.opposite();
        var blockPosition = updateState.blockPosition();
        var instance = updateState.instance();

        var blockX = blockPosition.blockX();
        var blockY = blockPosition.blockY();
        var blockZ = blockPosition.blockZ();

        var blockBehind = instance.getBlock(
                blockX + supportDirection.normalX(),
                blockY,
                blockZ + supportDirection.normalZ()
        );

        if (!blockBehind.registry().isSolid()) {
            return Block.AIR;
        }

        return currentBlock;
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }
}

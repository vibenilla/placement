package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AmethystClusterPlacementRule extends BlockPlacementRule {
    public AmethystClusterPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var facing = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var supportPosition = placePosition.relative(facing.getOppositeFace());
        var supportBlock = instance.getBlock(supportPosition);

        if (!supportBlock.registry().collisionShape().isFaceFull(facing)) {
            return null;
        }

        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));

        return this.block
                .withProperty("facing", facingName(facing))
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var facing = parseFacing(currentBlock.getProperty("facing"));

        if (facing == null) {
            return currentBlock;
        }
        var supportFace = facing.getOppositeFace();

        if (updateState.fromFace() != supportFace) {
            return currentBlock;
        }
        var supportBlock = updateState.instance().getBlock(updateState.blockPosition().relative(supportFace));

        if (!supportBlock.registry().collisionShape().isFaceFull(facing)) {
            return Block.AIR;
        }
        return currentBlock;
    }

    private static BlockFace parseFacing(String facingName) {
        return switch (facingName) {
            case "up" -> BlockFace.TOP;
            case "down" -> BlockFace.BOTTOM;
            case "north" -> BlockFace.NORTH;
            case "east" -> BlockFace.EAST;
            case "south" -> BlockFace.SOUTH;
            case "west" -> BlockFace.WEST;
            case null, default -> null;
        };
    }

    private static String facingName(@NotNull BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

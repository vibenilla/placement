package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class HugeMushroomPlacementRule extends BlockPlacementRule {
    private static final BlockFace[] DIRECTIONS = {
            BlockFace.TOP, BlockFace.BOTTOM, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
    };

    public HugeMushroomPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        return this.withNeighborProperties(placementState.block(), placementState.instance(), placementState.placePosition());
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        return this.withNeighborProperties(updateState.currentBlock(), updateState.instance(), updateState.blockPosition());
    }

    private Block withNeighborProperties(Block base, Block.Getter blockGetter, Point position) {
        var result = base;

        for (var direction : DIRECTIONS) {
            var neighbor = blockGetter.getBlock(position.relative(direction));
            result = result.withProperty(propertyName(direction), String.valueOf(!neighbor.compare(this.block)));
        }

        return result;
    }

    private static String propertyName(BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

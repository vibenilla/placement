package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class LeavesPlacementRule extends BlockPlacementRule {
    private static final Key PREVENTS_NEARBY_LEAF_DECAY = Key.key("minecraft:prevents_nearby_leaf_decay");

    public LeavesPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var placePosition = placementState.placePosition();
        var replaced = placementState.instance().getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER);

        return placementState.block()
                .withProperty("persistent", "true")
                .withProperty("waterlogged", String.valueOf(waterlogged))
                .withProperty("distance", String.valueOf(distanceAt(placementState.instance(), placePosition)));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        return updateState.currentBlock().withProperty(
                "distance", String.valueOf(distanceAt(updateState.instance(), updateState.blockPosition())));
    }

    private static int distanceAt(Block.Getter blockGetter, Point position) {
        var distance = 7;

        for (var face : BlockFace.values()) {
            var neighbor = blockGetter.getBlock(position.relative(face));
            var neighborDistance = distanceFrom(neighbor) + 1;
            distance = Math.min(distance, neighborDistance);

            if (distance == 1) {
                return distance;
            }
        }

        return distance;
    }

    private static int distanceFrom(Block block) {
        if (Utility.hasTag(block, PREVENTS_NEARBY_LEAF_DECAY)) {
            return 0;
        }

        var distance = block.getProperty("distance");
        return distance == null ? 7 : Integer.parseInt(distance);
    }
}

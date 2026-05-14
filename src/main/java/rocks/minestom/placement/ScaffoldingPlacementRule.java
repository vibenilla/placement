package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class ScaffoldingPlacementRule extends BlockPlacementRule {
    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    public ScaffoldingPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        var distance = computeDistance(placementState);
        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));
        var bottom = distance > 0 && belowBlock.isAir();

        return this.block
                .withProperty("waterlogged", waterlogged ? "true" : "false")
                .withProperty("distance", Integer.toString(distance))
                .withProperty("bottom", bottom ? "true" : "false");
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        return replacement.material() == this.block.registry().material();
    }

    private int computeDistance(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var belowPosition = placePosition.relative(BlockFace.BOTTOM);
        var belowBlock = instance.getBlock(belowPosition);
        var distance = 7;

        if (belowBlock.compare(this.block)) {
            distance = parseDistance(belowBlock.getProperty("distance"));
        } else if (belowBlock.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return 0;
        }

        for (var face : HORIZONTAL_FACES) {
            var neighborBlock = instance.getBlock(placePosition.relative(face));

            if (neighborBlock.compare(this.block)) {
                distance = Math.min(distance, parseDistance(neighborBlock.getProperty("distance")) + 1);

                if (distance == 1) {
                    break;
                }
            }
        }
        return distance;
    }

    private static int parseDistance(String property) {
        if (property == null) {
            return 7;
        }

        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException exception) {
            return 7;
        }
    }
}

package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class ScaffoldingPlacementRule extends BlockPlacementRule {
    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    public ScaffoldingPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER);
        var distance = computeDistance(placementState);

        if (distance >= 7) {
            return null;
        }

        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));
        var bottom = distance > 0 && !belowBlock.compare(this.block);

        return placementState.block()
                .withProperty("waterlogged", waterlogged ? "true" : "false")
                .withProperty("distance", Integer.toString(distance))
                .withProperty("bottom", bottom ? "true" : "false");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var fromFace = updateState.fromFace();

        if (fromFace != BlockFace.BOTTOM && !isHorizontal(fromFace)) {
            return updateState.currentBlock();
        }

        var distance = computeDistance(updateState);

        if (distance >= 7) {
            return Block.AIR;
        }

        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
        return updateState.currentBlock()
                .withProperty("distance", Integer.toString(distance))
                .withProperty("bottom", String.valueOf(distance > 0 && !below.compare(this.block)));
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        return replacement.material() == this.block.registry().material();
    }

    private int computeDistance(PlacementState placementState) {
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

    private int computeDistance(UpdateState updateState) {
        var instance = updateState.instance();
        var placePosition = updateState.blockPosition();
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
            }
        }

        return distance;
    }

    private static boolean isHorizontal(BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH
                || face == BlockFace.EAST || face == BlockFace.WEST;
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

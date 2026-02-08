package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class StairPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:stairs");

    // indices here are blockFace.ordinal - 2
    private static final BlockFace[][] HORIZONTAL_FACING = {
            // North
            {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST},
            // South
            {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST},
            // West
            {BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH},
            // East
            {BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH}
    };

    private static final String[] SHAPE_INDICES = {
            "straight", "straight", "inner_right", "inner_left", "inner_right", "straight",
            "straight", "straight", "straight", "inner_left", "straight", "straight",
            "outer_left", "outer_left", "outer_left", "outer_left", "outer_left", "outer_left",
            "straight", "straight", "straight", "inner_left", "straight", "straight",
            "outer_right", "straight", "outer_right", "outer_right", "inner_right", "straight",
            "outer_right", "straight", "outer_right", "outer_right", "straight", "straight"
    };

    public StairPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        return this.genericUpdateShape(updateState.instance(), updateState.currentBlock(), updateState.blockPosition());
    }

    @Override
    public @NotNull Block blockPlace(@NotNull PlacementState placementState) {
        BlockFace placeFace = placementState.blockFace();
        double placeY = Objects.requireNonNullElse(placementState.cursorPosition(), Vec.ZERO).y();
        String half = (placeFace == BlockFace.TOP || (placeFace != BlockFace.BOTTOM && placeY < 0.5))
                ? "bottom"
                : "top";

        // Facing is always the player facing direction, and is never updated
        Pos playerPosition = Objects.requireNonNullElse(placementState.playerPosition(), Pos.ZERO);
        BlockFace facing = BlockFace.fromYaw(playerPosition.yaw());
        Block baseBlock = this.block.withProperties(Map.of(
                "half", half,
                "facing", facing.name().toLowerCase(Locale.ROOT)
        ));

        return this.genericUpdateShape(placementState.instance(), baseBlock, placementState.placePosition());
    }

    private Block genericUpdateShape(Block.Getter blockGetter, Block block, Point blockPos) {
        String facingProperty = block.getProperty("facing");
        if (facingProperty == null) {
            return block;
        }
        BlockFace facing = BlockFace.valueOf(facingProperty.toUpperCase(Locale.ROOT));

        // Reordered directions for the side array. See comment in parseShapeFromSides
        // for explanation of the order here.
        int[] sides = new int[4];
        BlockFace[] orderedFaces = HORIZONTAL_FACING[facing.ordinal() - 2];

        for (int i = 0; i < 4; i++) {
            BlockFace blockFace = orderedFaces[i];
            Block relativeBlock = blockGetter.getBlock(blockPos.relative(blockFace), Block.Getter.Condition.TYPE);

            // Non-stairs never connect
            if (!Utility.hasTag(relativeBlock, KEY)) {
                continue;
            }

            // Top and bottom stairs never connect
            if (!Objects.equals(block.getProperty("half"), relativeBlock.getProperty("half"))) {
                continue;
            }

            String relativeFacingProperty = relativeBlock.getProperty("facing");
            if (relativeFacingProperty == null) {
                continue;
            }
            BlockFace relativeFacing = BlockFace.valueOf(relativeFacingProperty.toUpperCase(Locale.ROOT));

            if (facing.isSimilar(blockFace)) {
                // If it is a face next to the stair, then the rule is:
                // canConnect = rel.facing is perpendicular to block.facing
                boolean canConnect = !relativeFacing.isSimilar(facing);

                if (canConnect) {
                    BlockFace nextFace = orderedFaces[(i + 1) % 4];
                    sides[i] = nextFace == relativeFacing ? 2 : 1;
                }
            } else {
                // If it is a face opposite to the stair, then the rule is:
                // canConnect = rel.facing == block.facing || rel.facing == blockFace
                boolean canConnect = relativeFacing == facing || relativeFacing == blockFace;

                // Weird edge case in vanilla
                String shape = block.getProperty("shape");
                if (("outer_right".equals(shape) || "outer_left".equals(shape)) && relativeFacing != facing) {
                    canConnect = false;
                }

                if (canConnect) {
                    sides[i] = 1;
                }
            }
        }

        return block.withProperty("shape", parseShapeFromSides(sides));
    }

    // Sides are a 4 element array with a value of whether the stair may connect to
    // that block, and how. The order of the elements is clockwise from the
    // "perspective" of the stair block. For example, if the stair block is facing
    // north, then the array will be "north east south west".
    // The numbers in the array are as follows:
    // - 0: No connection
    // - 1: Connection to the side, right
    // - 2: Connection to the side, left
    // Those are only relevant for faces parallel to the stair.
    private static String parseShapeFromSides(int[] sides) {
        return SHAPE_INDICES[
                (sides[0] * 12)
                        + (sides[1] * 6)
                        + (sides[2] == 0 ? 0 : sides[2] + sides[3] + 1)
                        + sides[3]
                ];
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }
}

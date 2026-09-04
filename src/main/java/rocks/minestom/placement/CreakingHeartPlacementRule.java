package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.minestom.server.world.attribute.EnvironmentAttributeMap;

import java.util.Objects;

public final class CreakingHeartPlacementRule extends BlockPlacementRule {
    private static final Key PALE_OAK_LOGS = Key.key("minecraft:pale_oak_logs");
    private static final Key CREAKING_ACTIVE = Key.key("minecraft:creaking_active");

    public CreakingHeartPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var blockFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var placed = placementState.block().withProperty("axis", axisName(blockFace));

        return updateState(placementState.instance(), placementState.placePosition(), placed);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        return updateState(updateState.instance(), updateState.blockPosition(), updateState.currentBlock());
    }

    private static Block updateState(Block.Getter blockGetter, Point position, Block block) {
        if (!"uprooted".equals(block.getProperty("creaking_heart_state")) || !hasRequiredLogs(blockGetter, position, block)) {
            return block;
        }

        var active = blockGetter instanceof Instance instance && environmentFlag(instance, CREAKING_ACTIVE);
        return block.withProperty("creaking_heart_state", active ? "awake" : "dormant");
    }

    private static boolean hasRequiredLogs(Block.Getter blockGetter, Point position, Block block) {
        var axis = block.getProperty("axis");
        var faces = switch (axis) {
            case "x" -> new BlockFace[]{BlockFace.WEST, BlockFace.EAST};
            case "z" -> new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH};
            default -> new BlockFace[]{BlockFace.BOTTOM, BlockFace.TOP};
        };

        for (var face : faces) {
            var neighbor = blockGetter.getBlock(position.relative(face));

            if (!Utility.hasTag(neighbor, PALE_OAK_LOGS) || !axis.equals(neighbor.getProperty("axis"))) {
                return false;
            }
        }

        return true;
    }

    private static String axisName(BlockFace face) {
        return switch (face) {
            case WEST, EAST -> "x";
            case SOUTH, NORTH -> "z";
            case TOP, BOTTOM -> "y";
        };
    }

    private static boolean environmentFlag(Instance instance, Key key) {
        for (var attribute : EnvironmentAttribute.values()) {
            if (!attribute.key().equals(key)) {
                continue;
            }

            var entry = instance.getCachedDimensionType().attributes().entries().get(attribute);
            return entry == null
                    ? Boolean.TRUE.equals(attribute.defaultValue())
                    : Boolean.TRUE.equals(applyEntry(attribute, entry));
        }

        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object applyEntry(EnvironmentAttribute attribute, EnvironmentAttributeMap.Entry entry) {
        return entry.modifier().modify(attribute.defaultValue(), entry.argument());
    }
}

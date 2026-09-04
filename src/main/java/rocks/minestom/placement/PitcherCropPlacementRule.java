package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class PitcherCropPlacementRule extends BlockPlacementRule {
    public PitcherCropPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var placePosition = placementState.placePosition();
        var below = placementState.instance().getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!supportsCrops(below) || !hasSufficientLight(placementState.instance(), placePosition)) {
            return null;
        }

        return placementState.block()
                .withProperty("age", "0")
                .withProperty("half", "lower");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var half = currentBlock.getProperty("half");
        var age = parseAge(currentBlock.getProperty("age"));
        var fromFace = updateState.fromFace();

        if (age >= 3) {
            if ("lower".equals(half) && fromFace == BlockFace.TOP) {
                var above = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.TOP));
                return this.isMatchingHalf(above, "upper") ? currentBlock : Block.AIR;
            }

            if ("upper".equals(half) && fromFace == BlockFace.BOTTOM) {
                var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
                return this.isMatchingHalf(below, "lower") ? currentBlock : Block.AIR;
            }
        }

        if ("lower".equals(half)) {
            var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));

            if (!supportsCrops(below) || !hasSufficientLight(updateState.instance(), updateState.blockPosition())) {
                return Block.AIR;
            }
        }

        return currentBlock;
    }

    private boolean isMatchingHalf(Block candidate, String half) {
        return candidate.compare(this.block) && half.equals(candidate.getProperty("half"));
    }

    private static boolean supportsCrops(Block block) {
        var supportsCrops = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_crops"));
        return supportsCrops != null && supportsCrops.contains(block);
    }

    private static boolean hasSufficientLight(Block.Getter blockGetter, Point position) {
        if (!(blockGetter instanceof Instance instance)) {
            return true;
        }

        var x = position.blockX();
        var y = position.blockY();
        var z = position.blockZ();
        return Math.max(instance.getBlockLight(x, y, z), instance.getSkyLight(x, y, z)) >= 8;
    }

    private static int parseAge(String age) {
        if (age == null) {
            return 0;
        }

        try {
            return Integer.parseInt(age);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}

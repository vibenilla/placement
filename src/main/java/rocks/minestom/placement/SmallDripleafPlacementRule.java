package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class SmallDripleafPlacementRule extends BlockPlacementRule {
    public SmallDripleafPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!(placementState.instance() instanceof Instance instance)) {
            return null;
        }

        var placePosition = placementState.placePosition();
        var upperPosition = placePosition.relative(BlockFace.TOP);
        var maxY = instance.getCachedDimensionType().maxY();

        if (upperPosition.blockY() >= maxY) {
            return null;
        }

        var existingUpperBlock = instance.getBlock(upperPosition);

        if (!existingUpperBlock.registry().isReplaceable()) {
            return null;
        }

        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));
        var lowerReplaced = instance.getBlock(placePosition);
        var lowerWaterlogged = lowerReplaced.compare(Block.WATER) && isWaterSource(lowerReplaced);
        var validSupport = supports(belowBlock, lowerWaterlogged);

        if (!validSupport) {
            return null;
        }

        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw).getOppositeFace().name().toLowerCase();
        var upperWaterlogged = existingUpperBlock.compare(Block.WATER) && isWaterSource(existingUpperBlock);
        var upperBlock = placementState.block()
                .withProperty("facing", facing)
                .withProperty("half", "upper")
                .withProperty("waterlogged", String.valueOf(upperWaterlogged));
        instance.setBlock(upperPosition, upperBlock, false);

        return placementState.block()
                .withProperty("facing", facing)
                .withProperty("half", "lower")
                .withProperty("waterlogged", String.valueOf(lowerWaterlogged));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var half = currentBlock.getProperty("half");
        var fromFace = updateState.fromFace();
        var instance = updateState.instance();
        var blockPosition = updateState.blockPosition();

        if ("upper".equals(half)) {
            if (fromFace != BlockFace.BOTTOM) {
                return currentBlock;
            }

            var belowBlock = instance.getBlock(blockPosition.relative(BlockFace.BOTTOM));
            return belowBlock.compare(this.block) && "lower".equals(belowBlock.getProperty("half")) ? currentBlock : Block.AIR;
        }

        if ("lower".equals(half)) {
            if (fromFace == BlockFace.TOP) {
                var aboveBlock = instance.getBlock(blockPosition.relative(BlockFace.TOP));
                return aboveBlock.compare(this.block) && "upper".equals(aboveBlock.getProperty("half")) ? currentBlock : Block.AIR;
            }

            if (fromFace == BlockFace.BOTTOM) {
                var belowBlock = instance.getBlock(blockPosition.relative(BlockFace.BOTTOM));
                return supports(belowBlock, "true".equals(currentBlock.getProperty("waterlogged")))
                        ? currentBlock
                        : Block.AIR;
            }
        }

        return currentBlock;
    }

    private static boolean supports(Block below, boolean waterlogged) {
        var registry = MinecraftServer.process().blocks();
        var supportsSmall = registry.getTag(Key.key("minecraft:supports_small_dripleaf"));

        if (supportsSmall != null && supportsSmall.contains(below)) {
            return true;
        }

        var supportsVegetation = registry.getTag(Key.key("minecraft:supports_vegetation"));
        return waterlogged && supportsVegetation != null && supportsVegetation.contains(below);
    }

    private static boolean isWaterSource(Block water) {
        var level = water.getProperty("level");
        return level == null || "0".equals(level);
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TallPlantPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:tall_flowers");

    public TallPlantPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        if (!(placementState.instance() instanceof Instance instance)) {
            return null;
        }

        var placePosition = placementState.placePosition();

        var blockX = placePosition.blockX();
        var blockY = placePosition.blockY();
        var blockZ = placePosition.blockZ();

        var currentBlock = instance.getBlock(blockX, blockY, blockZ);
        if (!currentBlock.isAir() && !currentBlock.registry().isReplaceable()) {
            return null;
        }

        var blockBelow = instance.getBlock(blockX, blockY - 1, blockZ);
        if (!this.canPlaceOn(blockBelow)) {
            return null;
        }

        var blockAbove = instance.getBlock(blockX, blockY + 1, blockZ);
        if (!blockAbove.isAir() && !blockAbove.registry().isReplaceable()) {
            return null;
        }

        instance.setBlock(blockX, blockY + 1, blockZ, this.block.withProperty("half", "upper"));

        return this.block.withProperty("half", "lower");
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var blockPosition = updateState.blockPosition();
        var getter = updateState.instance();
        var currentBlock = updateState.currentBlock();

        var blockX = blockPosition.blockX();
        var blockY = blockPosition.blockY();
        var blockZ = blockPosition.blockZ();

        var half = currentBlock.getProperty("half");
        if (half == null) {
            return Block.AIR;
        }

        if (half.equals("lower")) {
            var blockBelow = getter.getBlock(blockX, blockY - 1, blockZ);
            if (!this.canPlaceOn(blockBelow)) {
                if (getter instanceof Instance instance) {
                    instance.setBlock(blockX, blockY + 1, blockZ, Block.AIR);
                }
                return Block.AIR;
            }

            var blockAbove = getter.getBlock(blockX, blockY + 1, blockZ);
            if (!blockAbove.compare(currentBlock) || !"upper".equals(blockAbove.getProperty("half"))) {
                return Block.AIR;
            }
        } else if (half.equals("upper")) {
            var blockBelow = getter.getBlock(blockX, blockY - 1, blockZ);
            if (!blockBelow.compare(currentBlock) || !"lower".equals(blockBelow.getProperty("half"))) {
                return Block.AIR;
            }
        }

        return currentBlock;
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private boolean canPlaceOn(@NotNull Block block) {
        return block.compare(Block.DIRT)
                || block.compare(Block.GRASS_BLOCK)
                || block.compare(Block.PODZOL)
                || block.compare(Block.COARSE_DIRT)
                || block.compare(Block.MYCELIUM)
                || block.compare(Block.ROOTED_DIRT)
                || block.compare(Block.MOSS_BLOCK)
                || block.compare(Block.PALE_MOSS_BLOCK)
                || block.compare(Block.MUD)
                || block.compare(Block.MUDDY_MANGROVE_ROOTS)
                || block.compare(Block.FARMLAND);
    }
}

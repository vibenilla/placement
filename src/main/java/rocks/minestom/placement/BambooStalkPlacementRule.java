package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class BambooStalkPlacementRule extends BlockPlacementRule {
    public BambooStalkPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var replacedBlock = instance.getBlock(placePosition);

        if (replacedBlock.compare(Block.WATER)) {
            return null;
        }

        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));
        var supportsBambooTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_bamboo"));

        if (supportsBambooTag == null || !supportsBambooTag.contains(belowBlock)) {
            return null;
        }

        if (belowBlock.compare(Block.BAMBOO_SAPLING)) {
            return this.block
                    .withProperty("age", "0")
                    .withProperty("leaves", "none")
                    .withProperty("stage", "0");
        }

        if (belowBlock.compare(Block.BAMBOO)) {
            var existingAgeProperty = belowBlock.getProperty("age");
            var existingAge = existingAgeProperty == null ? 0 : Integer.parseInt(existingAgeProperty);
            var newAge = existingAge > 0 ? 1 : 0;

            return this.block
                    .withProperty("age", Integer.toString(newAge))
                    .withProperty("leaves", "none")
                    .withProperty("stage", "0");
        }

        var aboveBlock = instance.getBlock(placePosition.relative(BlockFace.TOP));

        if (aboveBlock.compare(Block.BAMBOO)) {
            var aboveAgeProperty = aboveBlock.getProperty("age");
            var aboveAge = aboveAgeProperty == null ? 0 : Integer.parseInt(aboveAgeProperty);

            return this.block
                    .withProperty("age", Integer.toString(aboveAge))
                    .withProperty("leaves", "none")
                    .withProperty("stage", "0");
        }
        return Block.BAMBOO_SAPLING;
    }
}

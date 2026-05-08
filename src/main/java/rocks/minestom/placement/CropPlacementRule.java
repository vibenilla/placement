package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class CropPlacementRule extends BlockPlacementRule {
    public CropPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var supportPosition = placementState.placePosition().relative(BlockFace.BOTTOM);
        var supportBlock = instance.getBlock(supportPosition);
        var supportsCropsTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_crops"));

        if (supportsCropsTag != null && supportsCropsTag.contains(supportBlock)) {
            return this.block;
        }
        return null;
    }
}

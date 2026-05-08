package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class CactusFlowerPlacementRule extends BlockPlacementRule {
    public CactusFlowerPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var supportPosition = placementState.placePosition().relative(BlockFace.BOTTOM);
        var supportBlock = instance.getBlock(supportPosition);
        var overrideTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:support_override_cactus_flower"));

        if (overrideTag != null && overrideTag.contains(supportBlock)) {
            return this.block;
        }

        if (supportBlock.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return this.block;
        }
        return null;
    }
}

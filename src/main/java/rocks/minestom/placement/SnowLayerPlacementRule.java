package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class SnowLayerPlacementRule extends BlockPlacementRule {
    public SnowLayerPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var existingBlock = instance.getBlock(placePosition);

        if (existingBlock.compare(this.block)) {
            var layersProperty = existingBlock.getProperty("layers");
            var layers = layersProperty == null ? 1 : Integer.parseInt(layersProperty);

            if (layers < 8) {
                return existingBlock.withProperty("layers", Integer.toString(Math.min(8, layers + 1)));
            }
        }

        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));
        var registry = MinecraftServer.process().blocks();
        var cannotSupport = registry.getTag(Key.key("minecraft:cannot_support_snow_layer"));

        if (cannotSupport != null && cannotSupport.contains(belowBlock)) {
            return null;
        }

        var supportOverride = registry.getTag(Key.key("minecraft:support_override_snow_layer"));
        var hasOverride = supportOverride != null && supportOverride.contains(belowBlock);
        var faceFull = belowBlock.registry().collisionShape().isFaceFull(BlockFace.TOP);
        var isFullSnow = belowBlock.compare(this.block) && "8".equals(belowBlock.getProperty("layers"));

        if (!hasOverride && !faceFull && !isFullSnow) {
            return null;
        }

        return this.block.withProperty("layers", "1");
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        if (!replacement.block().compare(this.block)) {
            return false;
        }

        if (replacement.material() != this.block.registry().material()) {
            return false;
        }

        var layersProperty = replacement.block().getProperty("layers");
        var layers = layersProperty == null ? 1 : Integer.parseInt(layersProperty);
        return layers < 8;
    }
}

package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class SnowLayerPlacementRule extends BlockPlacementRule {
    public SnowLayerPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var existingBlock = placementState.instance().getBlock(placementState.placePosition());

        if (existingBlock.compare(this.block)) {
            var layersProperty = existingBlock.getProperty("layers");
            var layers = layersProperty == null ? 1 : Integer.parseInt(layersProperty);

            if (layers < 8) {
                return existingBlock.withProperty("layers", Integer.toString(Math.min(8, layers + 1)));
            }
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

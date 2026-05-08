package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class CandlePlacementRule extends BlockPlacementRule {
    public CandlePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var existingBlock = instance.getBlock(placePosition);

        if (existingBlock.compare(this.block)) {
            var candlesProperty = existingBlock.getProperty("candles");
            var candles = candlesProperty == null ? 1 : Integer.parseInt(candlesProperty);

            if (candles < 4) {
                return existingBlock.withProperty("candles", Integer.toString(candles + 1));
            }
        }

        var waterlogged = existingBlock.compare(Block.WATER);

        return this.block
                .withProperty("candles", "1")
                .withProperty("lit", "false")
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        if (!replacement.block().compare(this.block)) {
            return false;
        }

        if ("4".equals(replacement.block().getProperty("candles"))) {
            return false;
        }

        if (replacement.material() != this.block.registry().material()) {
            return false;
        }
        return true;
    }
}

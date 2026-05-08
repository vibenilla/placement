package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class ConcretePowderPlacementRule extends BlockPlacementRule {
    private static final BlockFace[] FACES = BlockFace.values();

    private final Block concrete;

    public ConcretePowderPlacementRule(@NotNull Block block, @NotNull Block concrete) {
        super(block);
        this.concrete = concrete;
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();

        if (this.shouldSolidify(blockGetter, placePosition)) {
            return this.concrete;
        }
        return this.block;
    }

    private boolean shouldSolidify(@NotNull Block.Getter blockGetter, @NotNull Point placePosition) {
        var replaced = blockGetter.getBlock(placePosition);

        if (replaced.compare(Block.WATER)) {
            return true;
        }

        for (var face : FACES) {
            var neighbor = blockGetter.getBlock(placePosition.relative(face));

            if (neighbor.compare(Block.WATER)) {
                return true;
            }
        }
        return false;
    }
}

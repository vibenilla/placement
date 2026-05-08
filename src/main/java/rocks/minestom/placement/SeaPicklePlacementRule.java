package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class SeaPicklePlacementRule extends BlockPlacementRule {
    public SeaPicklePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var existingBlock = instance.getBlock(placePosition);

        if (existingBlock.compare(this.block)) {
            var picklesProperty = existingBlock.getProperty("pickles");
            var pickles = picklesProperty == null ? 1 : Integer.parseInt(picklesProperty);

            if (pickles < 4) {
                return existingBlock.withProperty("pickles", Integer.toString(pickles + 1));
            }
        }

        var supportPosition = placePosition.relative(BlockFace.BOTTOM);
        var supportBlock = instance.getBlock(supportPosition);

        if (!supportBlock.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return null;
        }

        var waterlogged = existingBlock.compare(Block.WATER);

        return this.block
                .withProperty("pickles", "1")
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        if (!replacement.block().compare(this.block)) {
            return false;
        }

        if (replacement.material() != this.block.registry().material()) {
            return false;
        }

        var picklesProperty = replacement.block().getProperty("pickles");
        var pickles = picklesProperty == null ? 1 : Integer.parseInt(picklesProperty);
        return pickles < 4;
    }
}

package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class SlabPlacementRule extends BlockPlacementRule {
    public SlabPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var existingBlock = placementState.instance().getBlock(placementState.placePosition());

        if (existingBlock.compare(this.block)) {
            return this.block.withProperty("type", "double").withProperty("waterlogged", "false");
        }

        var blockFace = placementState.blockFace();
        var cursorPosition = placementState.cursorPosition();
        var cursorY = cursorPosition == null ? 0.0D : cursorPosition.y();
        var top = blockFace == BlockFace.BOTTOM || (blockFace != BlockFace.TOP && cursorY > 0.5D);
        var waterlogged = existingBlock.compare(Block.WATER);

        return this.block
                .withProperty("type", top ? "top" : "bottom")
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        if (!replacement.block().compare(this.block)) {
            return false;
        }

        if ("double".equals(replacement.block().getProperty("type"))) {
            return false;
        }

        if (replacement.material() != this.block.registry().material()) {
            return false;
        }

        if (replacement.isOffset()) {
            return true;
        }

        var cursorPosition = replacement.cursorPosition();
        var above = cursorPosition != null && cursorPosition.y() > 0.5D;
        var clickedFace = replacement.blockFace();
        var horizontal = clickedFace != null && clickedFace != BlockFace.TOP && clickedFace != BlockFace.BOTTOM;
        var type = replacement.block().getProperty("type");

        if ("bottom".equals(type)) {
            return clickedFace == BlockFace.TOP || (above && horizontal);
        }
        return clickedFace == BlockFace.BOTTOM || (!above && horizontal);
    }
}

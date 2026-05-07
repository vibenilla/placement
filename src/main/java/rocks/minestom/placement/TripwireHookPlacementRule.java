package rocks.minestom.placement;

/**
 * Original code taken and modified from <a href="https://hollowcube.net/">hollowcube</a>.
 * Original code licensed under MIT.
 */

import rocks.minestom.placement.properties.enums.FacingXZ;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TripwireHookPlacementRule extends BlockPlacementRule {
    public TripwireHookPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        BlockFace face = placementState.blockFace();
        if (face == null || face == BlockFace.TOP || face == BlockFace.BOTTOM) return null;
        return block.withProperty("facing", FacingXZ.fromBlockFace(face).name().toLowerCase());
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var facing = BlockFace.valueOf(currentBlock.getProperty("facing").toUpperCase());
        var currentAttached = Boolean.parseBoolean(currentBlock.getProperty("attached"));

        var neighbor = updateState.instance().getBlock(updateState.blockPosition().relative(facing));
        if (!currentAttached && (neighbor.id() == Block.TRIPWIRE.id() || neighbor.id() == Block.TRIPWIRE_HOOK.id())) {
            return currentBlock.withProperty("attached", "true");
        } else if (currentAttached && neighbor.id() != Block.TRIPWIRE.id() && neighbor.id() != Block.TRIPWIRE_HOOK.id()) {
            return currentBlock.withProperty("attached", "false");
        }
        return currentBlock;
    }
}

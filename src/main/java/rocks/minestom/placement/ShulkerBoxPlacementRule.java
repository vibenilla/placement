package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ShulkerBoxPlacementRule extends BlockPlacementRule {
    public ShulkerBoxPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var facing = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);

        return this.block.withProperty("facing", facingName(facing));
    }

    private static String facingName(@NotNull BlockFace face) {
        return switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

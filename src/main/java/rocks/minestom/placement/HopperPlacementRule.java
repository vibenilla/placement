package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class HopperPlacementRule extends BlockPlacementRule {
    public HopperPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var clickedFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.BOTTOM);
        var direction = clickedFace.getOppositeFace();
        var facing = direction.toDirection().vertical() ? BlockFace.BOTTOM : direction;

        return this.block
                .withHandler(ConsumeInteractionBlockHandler.INSTANCE)
                .withProperty("facing", facingName(facing))
                .withProperty("enabled", "true");
    }

    private static String facingName(@NotNull BlockFace face) {
        return switch (face) {
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

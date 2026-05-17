package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.Objects;

public final class HopperPlacementRule extends BlockPlacementRule {
    public HopperPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var clickedFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.BOTTOM);
        var direction = clickedFace.getOppositeFace();
        var facing = direction.toDirection().vertical() ? BlockFace.BOTTOM : direction;

        return placementState.block()
                .withHandler(ConsumeInteractionBlockHandler.INSTANCE)
                .withProperty("facing", facingName(facing))
                .withProperty("enabled", "true");
    }

    private static String facingName(BlockFace face) {
        return switch (face) {
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };
    }
}

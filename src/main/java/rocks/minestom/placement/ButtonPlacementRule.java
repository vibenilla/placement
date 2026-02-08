package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ButtonPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:buttons");

    public ButtonPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var face = Objects.requireNonNull(placementState.blockFace());

        return placementState.block()
                .withProperty("face", switch (face) {
                    case BOTTOM -> "floor";
                    case TOP -> "ceiling";
                    default -> "wall";
                })
                .withProperty("facing", face.toDirection().name().toLowerCase())
                .withProperty("powered", "false");
    }
}

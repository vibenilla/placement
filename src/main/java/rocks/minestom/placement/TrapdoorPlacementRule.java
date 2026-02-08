package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class TrapdoorPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:trapdoors");

    public TrapdoorPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockFace = Objects.requireNonNullElse(placementState.blockFace(), BlockFace.TOP);
        var playerPosition = Objects.requireNonNullElse(placementState.playerPosition(), Pos.ZERO);

        String facing;
        String half;

        // Check if clicked face is horizontal (not top/bottom)
        if (blockFace != BlockFace.TOP && blockFace != BlockFace.BOTTOM) {
            // Clicked on horizontal face
            facing = blockFace.name().toLowerCase(Locale.ROOT);

            // Determine half based on cursor Y position
            // If cursor Y > 0.5, place on top half, otherwise bottom half
            var cursorY = Objects.requireNonNullElse(placementState.cursorPosition(), Vec.ZERO).y();
            half = cursorY > 0.5D ? "top" : "bottom";
        } else {
            // Clicked on top or bottom face
            // Facing is opposite of player's horizontal direction
            var playerFacing = BlockFace.fromYaw(playerPosition.yaw());
            facing = playerFacing.getOppositeFace().name().toLowerCase(Locale.ROOT);

            // If clicked on UP face, place on bottom. If clicked on DOWN face, place on top
            half = blockFace == BlockFace.TOP ? "bottom" : "top";
        }

        return this.block.withProperties(Map.of(
                "facing", facing,
                "half", half,
                "open", "false",
                "powered", "false",
                "waterlogged", "false"
        ));
    }
}

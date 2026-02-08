package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StandingSignPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:standing_signs");

    public StandingSignPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var rotation = getRotation(playerPosition);

        var placePosition = placementState.placePosition();
        var instance = placementState.instance();

        var blockX = placePosition.blockX();
        var blockY = placePosition.blockY();
        var blockZ = placePosition.blockZ();

        var blockBelow = instance.getBlock(blockX, blockY - 1, blockZ);
        if (!blockBelow.registry().isSolid()) {
            return null;
        }

        return this.block.withProperty("rotation", String.valueOf(rotation));
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var blockPosition = updateState.blockPosition();
        var instance = updateState.instance();

        var blockX = blockPosition.blockX();
        var blockY = blockPosition.blockY();
        var blockZ = blockPosition.blockZ();

        var blockBelow = instance.getBlock(blockX, blockY - 1, blockZ);
        if (!blockBelow.registry().isSolid()) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private static int getRotation(@Nullable Pos playerPosition) {
        if (playerPosition == null) {
            return 0;
        }

        var rotation = (float) (playerPosition.yaw() + 180.0F);
        return (int) Math.floor((double) (rotation * 16.0F / 360.0F) + 0.5D) & 15;
    }
}

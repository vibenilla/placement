package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class EnderChestPlacementRule extends BlockPlacementRule {
    public EnderChestPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw).getOppositeFace();
        var placePosition = placementState.placePosition();
        var waterlogged = placementState.instance().getBlock(placePosition).compare(Block.WATER);

        return this.block
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("waterlogged", String.valueOf(waterlogged));
    }
}

package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class CampfirePlacementRule extends BlockPlacementRule {
    public CampfirePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw).getOppositeFace();
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var waterlogged = instance.getBlock(placePosition).compare(Block.WATER);
        var signalFire = instance.getBlock(placePosition.relative(BlockFace.BOTTOM)).compare(Block.HAY_BLOCK);

        return this.block
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("waterlogged", String.valueOf(waterlogged))
                .withProperty("lit", String.valueOf(!waterlogged))
                .withProperty("signal_fire", String.valueOf(signalFire));
    }
}

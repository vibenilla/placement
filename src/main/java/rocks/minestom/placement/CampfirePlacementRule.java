package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class CampfirePlacementRule extends BlockPlacementRule {
    public CampfirePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw).getOppositeFace();
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        var signalFire = instance.getBlock(placePosition.relative(BlockFace.BOTTOM)).compare(Block.HAY_BLOCK);

        return placementState.block()
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("waterlogged", String.valueOf(waterlogged))
                .withProperty("lit", String.valueOf(!waterlogged))
                .withProperty("signal_fire", String.valueOf(signalFire));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var signalFire = updateState.instance()
                .getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM))
                .compare(Block.HAY_BLOCK);
        return updateState.currentBlock().withProperty("signal_fire", String.valueOf(signalFire));
    }
}

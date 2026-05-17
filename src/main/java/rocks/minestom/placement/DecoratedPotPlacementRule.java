package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class DecoratedPotPlacementRule extends BlockPlacementRule {
    public DecoratedPotPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw);
        var placePosition = placementState.placePosition();
        var replaced = placementState.instance().getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));

        return placementState.block()
                .withHandler(ConsumeInteractionBlockHandler.INSTANCE)
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("waterlogged", String.valueOf(waterlogged))
                .withProperty("cracked", "false");
    }
}

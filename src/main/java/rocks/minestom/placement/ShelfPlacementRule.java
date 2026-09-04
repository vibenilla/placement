package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class ShelfPlacementRule extends BlockPlacementRule {
    public ShelfPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw).getOppositeFace();
        var blockGetter = placementState.instance();
        var position = placementState.placePosition();
        var replaced = blockGetter.getBlock(position);
        var powered = VanillaPlacementUtils.hasNeighborSignal(blockGetter, position);

        return placementState.block()
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("powered", String.valueOf(powered))
                .withProperty("side_chain", "unconnected")
                .withProperty("waterlogged", String.valueOf(replaced.compare(Block.WATER)));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var current = updateState.currentBlock();
        var powered = VanillaPlacementUtils.hasNeighborSignal(
                updateState.instance(), updateState.blockPosition());

        if (powered == "true".equals(current.getProperty("powered"))) {
            return current;
        }

        var updated = current.withProperty("powered", String.valueOf(powered));
        return powered ? updated : updated.withProperty("side_chain", "unconnected");
    }
}

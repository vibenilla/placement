package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class TrapdoorPlacementRule extends BlockPlacementRule {
    public TrapdoorPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var clickedFace = placementState.blockFace();
        var cursorPosition = placementState.cursorPosition();
        var cursorY = cursorPosition == null ? 0.0D : cursorPosition.y();
        var horizontalClick = clickedFace != null && clickedFace != BlockFace.TOP && clickedFace != BlockFace.BOTTOM;
        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = horizontalClick ? clickedFace : BlockFace.fromYaw(yaw).getOppositeFace();
        var half = horizontalClick
                ? (cursorY > 0.5D ? "top" : "bottom")
                : (clickedFace == BlockFace.BOTTOM ? "top" : "bottom");
        var blockGetter = placementState.instance();
        var position = placementState.placePosition();
        var replaced = blockGetter.getBlock(position);
        var waterlogged = replaced.compare(Block.WATER);
        var powered = VanillaPlacementUtils.hasNeighborSignal(blockGetter, position);

        return placementState.block()
                .withHandler(TrapdoorBlockHandler.INSTANCE)
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("half", half)
                .withProperty("open", String.valueOf(powered))
                .withProperty("powered", String.valueOf(powered))
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var current = updateState.currentBlock();
        var powered = VanillaPlacementUtils.hasNeighborSignal(
                updateState.instance(), updateState.blockPosition());

        if (powered == "true".equals(current.getProperty("powered"))) {
            return current;
        }

        return current
                .withProperty("powered", String.valueOf(powered))
                .withProperty("open", String.valueOf(powered));
    }
}

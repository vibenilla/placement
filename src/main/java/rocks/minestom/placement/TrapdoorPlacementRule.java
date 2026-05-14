package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class TrapdoorPlacementRule extends BlockPlacementRule {
    public TrapdoorPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
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
        var replaced = placementState.instance().getBlock(placementState.placePosition());
        var waterlogged = replaced.compare(Block.WATER) && "0".equals(replaced.getProperty("level"));
        // TODO: vanilla checks hasNeighborSignal(pos) and sets open=powered=true; not yet implemented

        return this.block
                .withHandler(TrapdoorBlockHandler.INSTANCE)
                .withProperty("facing", facing.name().toLowerCase())
                .withProperty("half", half)
                .withProperty("open", "false")
                .withProperty("powered", "false")
                .withProperty("waterlogged", waterlogged ? "true" : "false");
    }
}

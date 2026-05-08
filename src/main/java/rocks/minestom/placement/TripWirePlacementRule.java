package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class TripWirePlacementRule extends BlockPlacementRule {
    public TripWirePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();

        return this.block
                .withProperty("north", String.valueOf(this.connects(blockGetter, placePosition, BlockFace.NORTH)))
                .withProperty("east", String.valueOf(this.connects(blockGetter, placePosition, BlockFace.EAST)))
                .withProperty("south", String.valueOf(this.connects(blockGetter, placePosition, BlockFace.SOUTH)))
                .withProperty("west", String.valueOf(this.connects(blockGetter, placePosition, BlockFace.WEST)));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var fromFace = updateState.fromFace();

        if (fromFace == BlockFace.TOP || fromFace == BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var connected = this.connects(updateState.instance(), updateState.blockPosition(), fromFace);
        return updateState.currentBlock().withProperty(fromFace.name().toLowerCase(), String.valueOf(connected));
    }

    private boolean connects(@NotNull Block.Getter blockGetter, @NotNull Point placePosition, @NotNull BlockFace face) {
        var neighbor = blockGetter.getBlock(placePosition.relative(face));

        if (neighbor.compare(Block.TRIPWIRE_HOOK)) {
            var hookFacing = neighbor.getProperty("facing");
            return hookFacing != null && face.getOppositeFace().name().toLowerCase().equals(hookFacing);
        }
        return neighbor.compare(this.block);
    }
}

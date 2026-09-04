package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class RedstoneLampPlacementRule extends BlockPlacementRule {
    public RedstoneLampPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var lit = VanillaPlacementUtils.hasNeighborSignal(
                placementState.instance(), placementState.placePosition());
        return placementState.block().withProperty("lit", String.valueOf(lit));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var current = updateState.currentBlock();
        var powered = VanillaPlacementUtils.hasNeighborSignal(
                updateState.instance(), updateState.blockPosition());

        if (powered) {
            return current.withProperty("lit", "true");
        }

        if ("true".equals(current.getProperty("lit")) && updateState.instance() instanceof Instance instance) {
            scheduleTurnOff(instance, updateState.blockPosition(), 4);
        }

        return current;
    }

    private static void scheduleTurnOff(Instance instance, Point position, int ticks) {
        instance.scheduleNextTick(currentInstance -> {
            if (ticks > 1) {
                scheduleTurnOff(currentInstance, position, ticks - 1);
                return;
            }

            var current = currentInstance.getBlock(position);

            if (current.compare(Block.REDSTONE_LAMP)
                    && !VanillaPlacementUtils.hasNeighborSignal(currentInstance, position)) {
                currentInstance.setBlock(position, current.withProperty("lit", "false"));
            }
        });
    }
}

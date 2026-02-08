package rocks.minestom.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

final class VanillaPlacementUtils {
    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    private VanillaPlacementUtils() {

    }

    static void scheduleHorizontalNeighborRuleUpdates(Block.Getter blockGetter, Point centerPosition) {
        if (!(blockGetter instanceof Instance instance)) {
            return;
        }

        instance.scheduleNextTick(currentInstance -> {
            for (var neighborFace : HORIZONTAL_FACES) {
                var neighborPosition = centerPosition.relative(neighborFace);
                var neighborBlock = currentInstance.getBlock(neighborPosition);
                var placementRule = MinecraftServer.getBlockManager().getBlockPlacementRule(neighborBlock);

                if (placementRule == null) {
                    continue;
                }

                var updateState = new BlockPlacementRule.UpdateState(
                        currentInstance,
                        neighborPosition,
                        neighborBlock,
                        neighborFace.getOppositeFace()
                );

                var updatedNeighbor = placementRule.blockUpdate(updateState);
                currentInstance.setBlock(neighborPosition, updatedNeighbor, false);
            }
        });
    }
}

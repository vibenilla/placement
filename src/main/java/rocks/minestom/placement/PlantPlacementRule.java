package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class PlantPlacementRule extends BlockPlacementRule {
    public PlantPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var supportPosition = placementState.placePosition().relative(BlockFace.BOTTOM);
        var supportBlock = instance.getBlock(supportPosition);
        var dirtTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:dirt"));

        if (dirtTag != null && dirtTag.contains(supportBlock)) {
            return this.block;
        }

        if (supportBlock.compare(Block.FARMLAND)) {
            return this.block;
        }
        return null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {

        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }
        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
        var dirtTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:dirt"));

        if (dirtTag != null && dirtTag.contains(below)) {
            return updateState.currentBlock();
        }

        if (below.compare(Block.FARMLAND)) {
            return updateState.currentBlock();
        }
        return Block.AIR;
    }
}

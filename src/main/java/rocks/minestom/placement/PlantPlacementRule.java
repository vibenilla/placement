package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class PlantPlacementRule extends BlockPlacementRule {
    private static final Key SUPPORTS_VEGETATION_TAG = Key.key("minecraft:supports_vegetation");

    public PlantPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var below = placementState.instance().getBlock(placementState.placePosition().relative(BlockFace.BOTTOM));
        return supportsVegetation(below) ? this.block : null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {

        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }
        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
        return supportsVegetation(below) ? updateState.currentBlock() : Block.AIR;
    }

    private static boolean supportsVegetation(@NotNull Block block) {
        var tag = MinecraftServer.process().blocks().getTag(SUPPORTS_VEGETATION_TAG);
        return tag != null && tag.contains(block);
    }
}

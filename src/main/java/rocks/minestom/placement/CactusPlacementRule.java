package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class CactusPlacementRule extends BlockPlacementRule {
    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    public CactusPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();

        for (var face : HORIZONTAL_FACES) {
            var neighbor = instance.getBlock(placePosition.relative(face));

            if (neighbor.registry().isSolid()) {
                return null;
            }
        }
        var supportBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (supportBlock.compare(Block.CACTUS)) {
            return this.block;
        }
        var supportsCactusTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_cactus"));

        if (supportsCactusTag != null && supportsCactusTag.contains(supportBlock)) {
            return this.block;
        }
        return null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var fromFace = updateState.fromFace();

        if (fromFace == BlockFace.TOP) {
            return updateState.currentBlock();
        }
        var instance = updateState.instance();
        var blockPosition = updateState.blockPosition();

        if (isHorizontal(fromFace)) {
            var neighbor = instance.getBlock(blockPosition.relative(fromFace));

            if (neighbor.registry().isSolid()) {
                return Block.AIR;
            }
            return updateState.currentBlock();
        }
        var below = instance.getBlock(blockPosition.relative(BlockFace.BOTTOM));

        if (below.compare(Block.CACTUS)) {
            return updateState.currentBlock();
        }
        var supportsCactusTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_cactus"));

        if (supportsCactusTag != null && supportsCactusTag.contains(below)) {
            return updateState.currentBlock();
        }
        return Block.AIR;
    }

    private static boolean isHorizontal(@NotNull BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.EAST || face == BlockFace.WEST;
    }
}

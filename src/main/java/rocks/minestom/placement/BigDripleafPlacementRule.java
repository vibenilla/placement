package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class BigDripleafPlacementRule extends BlockPlacementRule {
    public BigDripleafPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));
        var supportTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_big_dripleaf"));
        var validSupport = belowBlock.compare(Block.BIG_DRIPLEAF)
                || belowBlock.compare(Block.BIG_DRIPLEAF_STEM)
                || (supportTag != null && supportTag.contains(belowBlock));

        if (!validSupport) {
            return null;
        }

        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        String facing;

        if (belowBlock.compare(Block.BIG_DRIPLEAF) || belowBlock.compare(Block.BIG_DRIPLEAF_STEM)) {
            var belowFacing = belowBlock.getProperty("facing");
            facing = belowFacing == null ? BlockFace.fromYaw(yaw).getOppositeFace().name().toLowerCase() : belowFacing;
        } else {
            facing = BlockFace.fromYaw(yaw).getOppositeFace().name().toLowerCase();
        }

        var replaced = instance.getBlock(placePosition);
        var waterlogged = replaced.compare(Block.WATER) && isWaterSource(replaced);

        return this.block
                .withProperty("facing", facing)
                .withProperty("waterlogged", String.valueOf(waterlogged))
                .withProperty("tilt", "none");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {

        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }
        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));

        if (below.compare(Block.BIG_DRIPLEAF) || below.compare(Block.BIG_DRIPLEAF_STEM)) {
            return updateState.currentBlock();
        }
        var supportTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_big_dripleaf"));

        if (supportTag != null && supportTag.contains(below)) {
            return updateState.currentBlock();
        }
        return Block.AIR;
    }

    private static boolean isWaterSource(@NotNull Block water) {
        var level = water.getProperty("level");
        return level == null || "0".equals(level);
    }
}

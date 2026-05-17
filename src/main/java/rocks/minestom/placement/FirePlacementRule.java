package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class FirePlacementRule extends BlockPlacementRule {
    public FirePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var soulFireBaseTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:soul_fire_base_blocks"));
        var belowBlock = blockGetter.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (soulFireBaseTag != null && soulFireBaseTag.contains(belowBlock)) {
            return Block.SOUL_FIRE;
        }

        return Block.FIRE;
    }
}

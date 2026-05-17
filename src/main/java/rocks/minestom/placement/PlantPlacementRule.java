package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public final class PlantPlacementRule extends BlockPlacementRule {
    private static final Key SUPPORTS_VEGETATION_TAG = Key.key("minecraft:supports_vegetation");

    private final Key supportTagKey;

    public PlantPlacementRule(Block block) {
        this(block, SUPPORTS_VEGETATION_TAG);
    }

    public PlantPlacementRule(Block block, Key supportTagKey) {
        super(block);
        this.supportTagKey = supportTagKey;
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var below = placementState.instance().getBlock(placementState.placePosition().relative(BlockFace.BOTTOM));
        return supports(below) ? this.block : null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));
        return supports(below) ? updateState.currentBlock() : Block.AIR;
    }

    private boolean supports(Block block) {
        var tag = MinecraftServer.process().blocks().getTag(this.supportTagKey);
        return tag != null && tag.contains(block);
    }
}

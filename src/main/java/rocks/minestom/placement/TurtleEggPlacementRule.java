package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class TurtleEggPlacementRule extends BlockPlacementRule {
    public TurtleEggPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var existingBlock = instance.getBlock(placePosition);

        if (existingBlock.compare(this.block)) {
            var eggsProperty = existingBlock.getProperty("eggs");
            var eggs = eggsProperty == null ? 1 : Integer.parseInt(eggsProperty);

            if (eggs < 4) {
                return existingBlock.withProperty("eggs", Integer.toString(Math.min(4, eggs + 1)));
            }
        }

        var belowBlock = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));
        var sandTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:sand"));

        if (sandTag == null || !sandTag.contains(belowBlock)) {
            return null;
        }

        return this.block.withProperty("eggs", "1");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {

        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }
        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));

        if (!below.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return Block.AIR;
        }
        return updateState.currentBlock();
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        if (!replacement.block().compare(this.block)) {
            return false;
        }

        if (replacement.material() != this.block.registry().material()) {
            return false;
        }

        var eggsProperty = replacement.block().getProperty("eggs");
        var eggs = eggsProperty == null ? 1 : Integer.parseInt(eggsProperty);
        return eggs < 4;
    }
}

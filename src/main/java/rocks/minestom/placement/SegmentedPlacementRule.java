package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public final class SegmentedPlacementRule extends BlockPlacementRule {
    private static final Key SUPPORTS_VEGETATION_TAG = Key.key("minecraft:supports_vegetation");

    public SegmentedPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var below = instance.getBlock(placePosition.relative(BlockFace.BOTTOM));

        if (!supportsVegetation(below)) {
            return null;
        }

        var existingBlock = instance.getBlock(placePosition);

        if (existingBlock.compare(this.block)) {
            var amount = parseAmount(existingBlock.getProperty("flower_amount"));
            var increased = Math.min(4, amount + 1);
            return existingBlock.withProperty("flower_amount", Integer.toString(increased));
        }

        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw).getOppositeFace();

        return this.block
                .withProperty("flower_amount", "1")
                .withProperty("facing", facing.name().toLowerCase());
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {

        if (updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }
        var below = updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM));

        if (!supportsVegetation(below)) {
            return Block.AIR;
        }
        return updateState.currentBlock();
    }

    private static boolean supportsVegetation(@NotNull Block block) {
        var tag = MinecraftServer.process().blocks().getTag(SUPPORTS_VEGETATION_TAG);
        return tag != null && tag.contains(block);
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        if (!replacement.block().compare(this.block)) {
            return false;
        }

        if (replacement.material() != this.block.registry().material()) {
            return false;
        }

        var amount = parseAmount(replacement.block().getProperty("flower_amount"));
        return amount < 4;
    }

    private static int parseAmount(String property) {
        if (property == null) {
            return 1;
        }

        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException exception) {
            return 1;
        }
    }
}

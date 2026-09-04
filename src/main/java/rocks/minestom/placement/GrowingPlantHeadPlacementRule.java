package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public final class GrowingPlantHeadPlacementRule extends BlockPlacementRule {
    private final BlockFace growthDirection;
    private final @Nullable BlockHandler handler;

    public GrowingPlantHeadPlacementRule(Block block) {
        this(block, null);
    }

    public GrowingPlantHeadPlacementRule(Block block, @Nullable BlockHandler handler) {
        super(block);
        this.growthDirection = growthDirection(block);
        this.handler = handler;
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var instance = placementState.instance();
        var placePosition = placementState.placePosition();
        var oppositePosition = placePosition.relative(this.growthDirection.getOppositeFace());
        var supportBlock = instance.getBlock(oppositePosition);

        if (!canAttach(supportBlock)) {
            return null;
        }

        var age = ThreadLocalRandom.current().nextInt(25);
        var result = placementState.block().withProperty("age", Integer.toString(age));
        return this.handler == null ? result : result.withHandler(this.handler);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var fromFace = updateState.fromFace();

        if (fromFace != this.growthDirection.getOppositeFace()) {
            return updateState.currentBlock();
        }

        var supportBlock = updateState.instance().getBlock(updateState.blockPosition().relative(fromFace));
        return canAttach(supportBlock) ? updateState.currentBlock() : Block.AIR;
    }

    private boolean canAttach(Block supportBlock) {
        if (supportBlock.compare(this.block)) {
            return true;
        }

        if (supportBlock.compare(bodyBlock(this.block))) {
            return true;
        }

        return supportBlock.registry().collisionShape().isFaceFull(this.growthDirection);
    }

    private static BlockFace growthDirection(Block block) {
        if (block.compare(Block.KELP) || block.compare(Block.TWISTING_VINES)) {
            return BlockFace.TOP;
        }

        return BlockFace.BOTTOM;
    }

    private static Block bodyBlock(Block block) {
        if (block.compare(Block.KELP)) {
            return Block.KELP_PLANT;
        }

        if (block.compare(Block.WEEPING_VINES)) {
            return Block.WEEPING_VINES_PLANT;
        }

        if (block.compare(Block.TWISTING_VINES)) {
            return Block.TWISTING_VINES_PLANT;
        }

        return Block.CAVE_VINES_PLANT;
    }
}

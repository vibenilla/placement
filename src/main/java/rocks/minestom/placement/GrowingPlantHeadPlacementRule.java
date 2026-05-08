package rocks.minestom.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class GrowingPlantHeadPlacementRule extends BlockPlacementRule {
    public GrowingPlantHeadPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var age = ThreadLocalRandom.current().nextInt(25);

        // TODO: vanilla canSurvive checks per-plant attachment; not implemented
        return this.block.withProperty("age", Integer.toString(age));
    }
}

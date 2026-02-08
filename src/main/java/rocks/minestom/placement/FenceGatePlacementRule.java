package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class FenceGatePlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:fence_gates");
    private static final Key WALLS = Key.key("minecraft:walls");

    public FenceGatePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        if (!(blockGetter instanceof Instance instance)) {
            return placementState.block();
        }

        var placePosition = placementState.placePosition();
        var playerPosition = Objects.requireNonNullElse(placementState.playerPosition(), Pos.ZERO);
        var gateFacing = BlockFace.fromYaw(playerPosition.yaw()).name().toLowerCase(Locale.ROOT);
        var inWall = this.isInWall(blockGetter, placePosition, gateFacing);

        var placed = this.block.withProperties(Map.of(
                "facing", gateFacing,
                "open", "false",
                "powered", "false",
                "in_wall", Boolean.toString(inWall)
        ));

        VanillaPlacementUtils.scheduleHorizontalNeighborRuleUpdates(instance, placePosition);

        return placed;
    }

    @Override
    public Block blockUpdate(@NotNull UpdateState updateState) {
        var blockGetter = updateState.instance();
        var placePosition = updateState.blockPosition();
        var currentBlock = updateState.currentBlock();
        var facingDirection = Objects.requireNonNullElse(currentBlock.getProperty("facing"), "north");
        var inWall = this.isInWall(blockGetter, placePosition, facingDirection);
        return currentBlock.withProperty("in_wall", Boolean.toString(inWall));
    }

    private boolean isInWall(Block.Getter blockGetter, Point position, String facing) {
        return switch (facing) {
            case "north", "south" -> {
                var west = position.relative(BlockFace.WEST);
                var east = position.relative(BlockFace.EAST);
                yield this.isWall(blockGetter.getBlock(west)) || this.isWall(blockGetter.getBlock(east));
            }
            case "east", "west" -> {
                var north = position.relative(BlockFace.NORTH);
                var south = position.relative(BlockFace.SOUTH);
                yield this.isWall(blockGetter.getBlock(north)) || this.isWall(blockGetter.getBlock(south));
            }
            default -> false;
        };
    }

    private boolean isWall(Block block) {
        return Utility.hasTag(block, WALLS);
    }

    @Override
    public int maxUpdateDistance() {
        return 10;
    }
}

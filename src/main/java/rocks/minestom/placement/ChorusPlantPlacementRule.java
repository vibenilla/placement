package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChorusPlantPlacementRule extends BlockPlacementRule {
    public ChorusPlantPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        var blockGetter = placementState.instance();
        var placePosition = placementState.placePosition();
        var supportsTag = MinecraftServer.process().blocks().getTag(Key.key("minecraft:supports_chorus_plant"));

        return this.block
                .withProperty("down", String.valueOf(this.connects(blockGetter, placePosition, BlockFace.BOTTOM, supportsTag)))
                .withProperty("up", String.valueOf(this.connects(blockGetter, placePosition, BlockFace.TOP, null)))
                .withProperty("north", String.valueOf(this.connects(blockGetter, placePosition, BlockFace.NORTH, null)))
                .withProperty("east", String.valueOf(this.connects(blockGetter, placePosition, BlockFace.EAST, null)))
                .withProperty("south", String.valueOf(this.connects(blockGetter, placePosition, BlockFace.SOUTH, null)))
                .withProperty("west", String.valueOf(this.connects(blockGetter, placePosition, BlockFace.WEST, null)));
    }

    private boolean connects(@NotNull Block.Getter blockGetter, @NotNull Point placePosition, @NotNull BlockFace face, @Nullable RegistryTag<Block> supportsTag) {
        var neighbor = blockGetter.getBlock(placePosition.relative(face));

        if (neighbor.compare(this.block) || neighbor.compare(Block.CHORUS_FLOWER)) {
            return true;
        }

        return supportsTag != null && supportsTag.contains(neighbor);
    }
}

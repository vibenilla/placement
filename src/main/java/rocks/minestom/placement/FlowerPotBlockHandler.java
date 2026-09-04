package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class FlowerPotBlockHandler implements BlockHandler {
    public static final FlowerPotBlockHandler INSTANCE = new FlowerPotBlockHandler();
    private static final Key KEY = Key.key("placement:flower_pot");

    private static final Map<Material, Block> POTTED_BY_CONTENT = Map.<Material, Block>ofEntries(
            Map.entry(Material.DANDELION, Block.POTTED_DANDELION),
            Map.entry(Material.GOLDEN_DANDELION, Block.POTTED_GOLDEN_DANDELION),
            Map.entry(Material.POPPY, Block.POTTED_POPPY),
            Map.entry(Material.BLUE_ORCHID, Block.POTTED_BLUE_ORCHID),
            Map.entry(Material.ALLIUM, Block.POTTED_ALLIUM),
            Map.entry(Material.AZURE_BLUET, Block.POTTED_AZURE_BLUET),
            Map.entry(Material.RED_TULIP, Block.POTTED_RED_TULIP),
            Map.entry(Material.ORANGE_TULIP, Block.POTTED_ORANGE_TULIP),
            Map.entry(Material.WHITE_TULIP, Block.POTTED_WHITE_TULIP),
            Map.entry(Material.PINK_TULIP, Block.POTTED_PINK_TULIP),
            Map.entry(Material.OXEYE_DAISY, Block.POTTED_OXEYE_DAISY),
            Map.entry(Material.CORNFLOWER, Block.POTTED_CORNFLOWER),
            Map.entry(Material.LILY_OF_THE_VALLEY, Block.POTTED_LILY_OF_THE_VALLEY),
            Map.entry(Material.WITHER_ROSE, Block.POTTED_WITHER_ROSE),
            Map.entry(Material.TORCHFLOWER, Block.POTTED_TORCHFLOWER),
            Map.entry(Material.OPEN_EYEBLOSSOM, Block.POTTED_OPEN_EYEBLOSSOM),
            Map.entry(Material.CLOSED_EYEBLOSSOM, Block.POTTED_CLOSED_EYEBLOSSOM),
            Map.entry(Material.OAK_SAPLING, Block.POTTED_OAK_SAPLING),
            Map.entry(Material.SPRUCE_SAPLING, Block.POTTED_SPRUCE_SAPLING),
            Map.entry(Material.BIRCH_SAPLING, Block.POTTED_BIRCH_SAPLING),
            Map.entry(Material.JUNGLE_SAPLING, Block.POTTED_JUNGLE_SAPLING),
            Map.entry(Material.ACACIA_SAPLING, Block.POTTED_ACACIA_SAPLING),
            Map.entry(Material.CHERRY_SAPLING, Block.POTTED_CHERRY_SAPLING),
            Map.entry(Material.DARK_OAK_SAPLING, Block.POTTED_DARK_OAK_SAPLING),
            Map.entry(Material.PALE_OAK_SAPLING, Block.POTTED_PALE_OAK_SAPLING),
            Map.entry(Material.MANGROVE_PROPAGULE, Block.POTTED_MANGROVE_PROPAGULE),
            Map.entry(Material.FERN, Block.POTTED_FERN),
            Map.entry(Material.RED_MUSHROOM, Block.POTTED_RED_MUSHROOM),
            Map.entry(Material.BROWN_MUSHROOM, Block.POTTED_BROWN_MUSHROOM),
            Map.entry(Material.DEAD_BUSH, Block.POTTED_DEAD_BUSH),
            Map.entry(Material.CACTUS, Block.POTTED_CACTUS),
            Map.entry(Material.BAMBOO, Block.POTTED_BAMBOO),
            Map.entry(Material.CRIMSON_FUNGUS, Block.POTTED_CRIMSON_FUNGUS),
            Map.entry(Material.WARPED_FUNGUS, Block.POTTED_WARPED_FUNGUS),
            Map.entry(Material.CRIMSON_ROOTS, Block.POTTED_CRIMSON_ROOTS),
            Map.entry(Material.WARPED_ROOTS, Block.POTTED_WARPED_ROOTS),
            Map.entry(Material.AZALEA, Block.POTTED_AZALEA_BUSH),
            Map.entry(Material.FLOWERING_AZALEA, Block.POTTED_FLOWERING_AZALEA_BUSH));

    private FlowerPotBlockHandler() {

    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        var player = interaction.getPlayer();
        var hand = interaction.getHand();
        var heldItem = player.getItemInHand(hand);
        var block = interaction.getBlock();
        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();

        if (block.compare(Block.FLOWER_POT)) {
            var potted = POTTED_BY_CONTENT.get(heldItem.material());

            if (potted == null) {
                return true;
            }

            instance.setBlock(position, potted.withHandler(this));
            consume(player, hand, heldItem);
            return false;
        }

        var content = contentMaterialFor(block);

        if (content == null) {
            return false;
        }

        if (!heldItem.isAir()) {
            return true;
        }

        instance.setBlock(position, Block.FLOWER_POT.withHandler(this));
        giveOrDrop(player, instance, position, ItemStack.of(content));
        return false;
    }

    private static @Nullable Material contentMaterialFor(Block pottedBlock) {
        for (var entry : POTTED_BY_CONTENT.entrySet()) {
            if (pottedBlock.compare(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    private static void consume(Player player, PlayerHand hand, ItemStack heldItem) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        player.setItemInHand(hand, heldItem.consume(1));
    }

    private static void giveOrDrop(Player player, Instance instance, net.minestom.server.coordinate.Point position, ItemStack stack) {
        var inventory = player.getInventory();
        var leftover = inventory.addItemStack(stack) ? ItemStack.AIR : stack;

        if (leftover.isAir()) {
            return;
        }

        var entity = new net.minestom.server.entity.ItemEntity(leftover);
        entity.setInstance(instance, position.add(0.5D, 0.5D, 0.5D));
    }
}

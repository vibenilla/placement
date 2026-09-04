package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public final class BeeHiveBlockHandler implements BlockHandler {
    public static final BeeHiveBlockHandler INSTANCE = new BeeHiveBlockHandler();
    private static final Key KEY = Key.key("placement:bee_hive");

    private BeeHiveBlockHandler() {

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

        var block = interaction.getBlock();
        var honeyLevel = Integer.parseInt(block.getProperty("honey_level"));
        var player = interaction.getPlayer();
        var hand = interaction.getHand();
        var heldItem = player.getItemInHand(hand);

        if (honeyLevel < 5 || (heldItem.material() != Material.SHEARS && heldItem.material() != Material.GLASS_BOTTLE)) {
            return true;
        }

        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();

        if (heldItem.material() == Material.SHEARS) {
            var honeycomb = new ItemEntity(ItemStack.of(Material.HONEYCOMB, 3));
            honeycomb.setInstance(instance, position.add(0.5D, 0.5D, 0.5D));
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setItemInHand(hand, heldItem.damage(1));
            }

            instance.playSound(
                    Sound.sound(SoundEvent.BLOCK_BEEHIVE_SHEAR, Sound.Source.BLOCK, 1.0F, 1.0F),
                    position.add(0.5D, 0.5D, 0.5D));
        } else {
            giveHoneyBottle(interaction, heldItem);
            instance.playSound(
                    Sound.sound(SoundEvent.ITEM_BOTTLE_FILL, Sound.Source.BLOCK, 1.0F, 1.0F),
                    position.add(0.5D, 0.5D, 0.5D));
        }

        instance.setBlock(position, block.withProperty("honey_level", "0").withHandler(INSTANCE));
        return false;
    }

    private static void giveHoneyBottle(Interaction interaction, ItemStack heldItem) {
        var player = interaction.getPlayer();
        var hand = interaction.getHand();
        var honeyBottle = ItemStack.of(Material.HONEY_BOTTLE);
        var remaining = heldItem.consume(1);

        player.setItemInHand(hand, remaining);

        if (!player.getInventory().addItemStack(honeyBottle)) {
            player.dropItem(honeyBottle);
        }
    }
}

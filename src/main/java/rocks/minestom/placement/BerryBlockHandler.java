package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class BerryBlockHandler implements BlockHandler {
    public static final BerryBlockHandler INSTANCE = new BerryBlockHandler();
    private static final Key KEY = Key.key("placement:berries");

    private BerryBlockHandler() {

    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        var block = interaction.getBlock();

        if (block.compare(Block.SWEET_BERRY_BUSH)) {
            return this.harvestSweetBerries(interaction);
        }

        if (!"true".equals(block.getProperty("berries")) || Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        this.harvest(interaction, Material.GLOW_BERRIES, 1, SoundEvent.BLOCK_CAVE_VINES_PICK_BERRIES);
        interaction.getInstance().setBlock(
                interaction.getBlockPosition(), block.withProperty("berries", "false").withHandler(INSTANCE));
        return false;
    }

    private boolean harvestSweetBerries(Interaction interaction) {
        var block = interaction.getBlock();
        var age = Integer.parseInt(block.getProperty("age"));
        var heldItem = interaction.getPlayer().getItemInHand(interaction.getHand());

        if (age < 3 && heldItem.material() == Material.BONE_MEAL) {
            return true;
        }

        if (age <= 1 || Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        var amount = ThreadLocalRandom.current().nextInt(1, 3) + (age == 3 ? 1 : 0);
        this.harvest(
                interaction, Material.SWEET_BERRIES, amount, SoundEvent.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES);
        interaction.getInstance().setBlock(
                interaction.getBlockPosition(), block.withProperty("age", "1").withHandler(INSTANCE));
        return false;
    }

    private void harvest(Interaction interaction, Material material, int amount, SoundEvent soundEvent) {
        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();
        var itemEntity = new ItemEntity(ItemStack.of(material, amount));
        itemEntity.setInstance(instance, position.add(0.5D, 0.5D, 0.5D));

        var pitch = ThreadLocalRandom.current().nextFloat(0.8F, 1.2F);
        instance.playSound(
                Sound.sound(soundEvent, Sound.Source.BLOCK, 1.0F, pitch),
                position.add(0.5D, 0.5D, 0.5D));
    }
}

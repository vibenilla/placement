package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class CandleBlockHandler implements BlockHandler {
    public static final CandleBlockHandler INSTANCE = new CandleBlockHandler();
    private static final Key KEY = Key.key("placement:candle");

    private CandleBlockHandler() {

    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        var block = interaction.getBlock();
        var lit = block.getProperty("lit");
        var player = interaction.getPlayer();
        var hand = interaction.getHand();
        var heldItem = player.getItemInHand(hand);

        if (!"true".equals(lit)
                && !"true".equals(block.getProperty("waterlogged"))
                && (heldItem.material() == Material.FLINT_AND_STEEL
                || heldItem.material() == Material.FIRE_CHARGE)) {
            var instance = interaction.getInstance();
            var blockPosition = interaction.getBlockPosition();
            instance.setBlock(blockPosition, block.withProperty("lit", "true"));

            var fireCharge = heldItem.material() == Material.FIRE_CHARGE;
            var random = ThreadLocalRandom.current();
            var pitch = fireCharge
                    ? (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F
                    : random.nextFloat() * 0.4F + 0.8F;
            var soundEvent = fireCharge ? SoundEvent.ITEM_FIRECHARGE_USE : SoundEvent.ITEM_FLINTANDSTEEL_USE;
            instance.playSoundExcept(
                    player,
                    Sound.sound(soundEvent, Sound.Source.BLOCK, 1.0F, pitch),
                    blockPosition.add(0.5D, 0.5D, 0.5D));

            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setItemInHand(hand, fireCharge ? heldItem.consume(1) : heldItem.damage(1));
            }

            return false;
        }

        if (Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        if (!"true".equals(lit)
                || player.getGameMode() == GameMode.ADVENTURE
                || player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }

        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();
        instance.setBlock(blockPosition, block.withProperty("lit", "false"));

        var pitch = ThreadLocalRandom.current().nextFloat() * 0.4F + 0.8F;
        var sound = Sound.sound(SoundEvent.BLOCK_CANDLE_EXTINGUISH, Sound.Source.BLOCK, 1.0F, pitch);
        instance.playSoundExcept(interaction.getPlayer(), sound, blockPosition.add(0.5D, 0.5D, 0.5D));
        return false;
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.Map;

public final class CakeBlockHandler implements BlockHandler {
    public static final CakeBlockHandler INSTANCE = new CakeBlockHandler();
    private static final Key KEY = Key.key("placement:cake");
    private static final Map<Material, Block> CANDLE_CAKES = Map.ofEntries(
            Map.entry(Material.CANDLE, Block.CANDLE_CAKE),
            Map.entry(Material.WHITE_CANDLE, Block.WHITE_CANDLE_CAKE),
            Map.entry(Material.LIGHT_GRAY_CANDLE, Block.LIGHT_GRAY_CANDLE_CAKE),
            Map.entry(Material.GRAY_CANDLE, Block.GRAY_CANDLE_CAKE),
            Map.entry(Material.BLACK_CANDLE, Block.BLACK_CANDLE_CAKE),
            Map.entry(Material.BROWN_CANDLE, Block.BROWN_CANDLE_CAKE),
            Map.entry(Material.RED_CANDLE, Block.RED_CANDLE_CAKE),
            Map.entry(Material.ORANGE_CANDLE, Block.ORANGE_CANDLE_CAKE),
            Map.entry(Material.YELLOW_CANDLE, Block.YELLOW_CANDLE_CAKE),
            Map.entry(Material.LIME_CANDLE, Block.LIME_CANDLE_CAKE),
            Map.entry(Material.GREEN_CANDLE, Block.GREEN_CANDLE_CAKE),
            Map.entry(Material.CYAN_CANDLE, Block.CYAN_CANDLE_CAKE),
            Map.entry(Material.LIGHT_BLUE_CANDLE, Block.LIGHT_BLUE_CANDLE_CAKE),
            Map.entry(Material.BLUE_CANDLE, Block.BLUE_CANDLE_CAKE),
            Map.entry(Material.PURPLE_CANDLE, Block.PURPLE_CANDLE_CAKE),
            Map.entry(Material.MAGENTA_CANDLE, Block.MAGENTA_CANDLE_CAKE),
            Map.entry(Material.PINK_CANDLE, Block.PINK_CANDLE_CAKE));

    private CakeBlockHandler() {

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
        var heldItem = player.getItemInHand(interaction.getHand());
        var candleCake = CANDLE_CAKES.get(heldItem.material());
        var block = interaction.getBlock();
        var bitesProperty = block.getProperty("bites");
        var bites = bitesProperty == null ? 0 : Integer.parseInt(bitesProperty);

        if (candleCake != null && bites == 0) {
            var instance = interaction.getInstance();
            var blockPosition = interaction.getBlockPosition();
            instance.setBlock(blockPosition, candleCake.withHandler(CandleCakeBlockHandler.INSTANCE));
            instance.playSoundExcept(
                    player,
                    Sound.sound(SoundEvent.BLOCK_CAKE_ADD_CANDLE, Sound.Source.BLOCK, 1.0F, 1.0F),
                    blockPosition.add(0.5D, 0.5D, 0.5D));

            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setItemInHand(interaction.getHand(), heldItem.consume(1));
            }

            return false;
        }

        if (player.getFood() >= 20) {
            return true;
        }

        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();

        var newFood = Math.min(20, player.getFood() + 2);
        player.setFood(newFood);
        player.setFoodSaturation(Math.min((float) newFood, player.getFoodSaturation() + 0.4F));

        if (bites >= 6) {
            instance.setBlock(blockPosition, Block.AIR);
        } else {
            instance.setBlock(blockPosition, block.withProperty("bites", String.valueOf(bites + 1)));
        }

        return false;
    }
}

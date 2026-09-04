package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class CandleCakeBlockHandler implements BlockHandler {
    public static final CandleCakeBlockHandler INSTANCE = new CandleCakeBlockHandler();
    private static final Key KEY = Key.key("placement:candle_cake");
    private static final Map<Block, Material> CANDLES = Map.ofEntries(
            Map.entry(Block.CANDLE_CAKE, Material.CANDLE),
            Map.entry(Block.WHITE_CANDLE_CAKE, Material.WHITE_CANDLE),
            Map.entry(Block.LIGHT_GRAY_CANDLE_CAKE, Material.LIGHT_GRAY_CANDLE),
            Map.entry(Block.GRAY_CANDLE_CAKE, Material.GRAY_CANDLE),
            Map.entry(Block.BLACK_CANDLE_CAKE, Material.BLACK_CANDLE),
            Map.entry(Block.BROWN_CANDLE_CAKE, Material.BROWN_CANDLE),
            Map.entry(Block.RED_CANDLE_CAKE, Material.RED_CANDLE),
            Map.entry(Block.ORANGE_CANDLE_CAKE, Material.ORANGE_CANDLE),
            Map.entry(Block.YELLOW_CANDLE_CAKE, Material.YELLOW_CANDLE),
            Map.entry(Block.LIME_CANDLE_CAKE, Material.LIME_CANDLE),
            Map.entry(Block.GREEN_CANDLE_CAKE, Material.GREEN_CANDLE),
            Map.entry(Block.CYAN_CANDLE_CAKE, Material.CYAN_CANDLE),
            Map.entry(Block.LIGHT_BLUE_CANDLE_CAKE, Material.LIGHT_BLUE_CANDLE),
            Map.entry(Block.BLUE_CANDLE_CAKE, Material.BLUE_CANDLE),
            Map.entry(Block.PURPLE_CANDLE_CAKE, Material.PURPLE_CANDLE),
            Map.entry(Block.MAGENTA_CANDLE_CAKE, Material.MAGENTA_CANDLE),
            Map.entry(Block.PINK_CANDLE_CAKE, Material.PINK_CANDLE));

    private CandleCakeBlockHandler() {

    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        var heldItem = interaction.getPlayer().getItemInHand(interaction.getHand());

        if (heldItem.material() == Material.FLINT_AND_STEEL || heldItem.material() == Material.FIRE_CHARGE) {
            return CandleBlockHandler.INSTANCE.onInteract(interaction);
        }

        var cursor = interaction.getCursorPosition();

        if (heldItem.isAir() && cursor.y() > 0.5D && "true".equals(interaction.getBlock().getProperty("lit"))) {
            return CandleBlockHandler.INSTANCE.onInteract(interaction);
        }

        if (Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        var player = interaction.getPlayer();

        if (player.getFood() >= 20) {
            return true;
        }

        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();
        var candle = candleFor(interaction.getBlock());
        var newFood = Math.min(20, player.getFood() + 2);
        player.setFood(newFood);
        player.setFoodSaturation(Math.min((float) newFood, player.getFoodSaturation() + 0.4F));
        instance.setBlock(position, Block.CAKE.withHandler(CakeBlockHandler.INSTANCE).withProperty("bites", "1"));

        if (candle != null) {
            var itemEntity = new ItemEntity(ItemStack.of(candle));
            itemEntity.setInstance(instance, position.add(0.5D, 0.5D, 0.5D));
        }

        return false;
    }

    private static @Nullable Material candleFor(Block block) {
        for (var entry : CANDLES.entrySet()) {
            if (block.compare(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }
}

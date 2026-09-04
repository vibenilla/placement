package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class ComposterBlockHandler implements BlockHandler {
    public static final ComposterBlockHandler INSTANCE = new ComposterBlockHandler();
    private static final Key KEY = Key.key("placement:composter");
    private static final Set<String> LOW = items("""
            jungle_leaves oak_leaves spruce_leaves dark_oak_leaves pale_oak_leaves acacia_leaves cherry_leaves
            birch_leaves azalea_leaves mangrove_leaves oak_sapling spruce_sapling birch_sapling jungle_sapling
            acacia_sapling cherry_sapling dark_oak_sapling pale_oak_sapling mangrove_propagule beetroot_seeds
            dried_kelp short_grass kelp melon_seeds pumpkin_seeds seagrass sweet_berries glow_berries wheat_seeds
            moss_carpet pale_moss_carpet pale_hanging_moss pink_petals wildflowers leaf_litter small_dripleaf
            hanging_roots mangrove_roots torchflower_seeds pitcher_pod firefly_bush bush cactus_flower dry_short_grass
            dry_tall_grass
            """);
    private static final Set<String> LOW_MID = items("""
            dried_kelp_block tall_grass flowering_azalea_leaves cactus sugar_cane vine nether_sprouts weeping_vines
            twisting_vines melon_slice glow_lichen
            """);
    private static final Set<String> MID = items("""
            sea_pickle lily_pad pumpkin carved_pumpkin melon apple beetroot carrot cocoa_beans potato wheat
            brown_mushroom red_mushroom mushroom_stem crimson_fungus warped_fungus nether_wart crimson_roots
            warped_roots shroomlight dandelion poppy blue_orchid allium azure_bluet red_tulip orange_tulip
            white_tulip pink_tulip oxeye_daisy cornflower lily_of_the_valley wither_rose open_eyeblossom
            closed_eyeblossom fern sunflower lilac rose_bush peony large_fern spore_blossom azalea moss_block
            pale_moss_block big_dripleaf
            """);
    private static final Set<String> MID_HIGH = items("""
            hay_block brown_mushroom_block red_mushroom_block nether_wart_block warped_wart_block flowering_azalea
            bread baked_potato cookie torchflower pitcher_plant
            """);
    private static final Set<String> HIGH = items("cake pumpkin_pie");

    private ComposterBlockHandler() {

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
        var level = Integer.parseInt(block.getProperty("level"));
        var heldItem = interaction.getPlayer().getItemInHand(interaction.getHand());

        if (level == 8) {
            if (!heldItem.isAir()) {
                return true;
            }

            this.extractBoneMeal(interaction);
            return false;
        }

        var player = interaction.getPlayer();
        var hand = interaction.getHand();
        heldItem = player.getItemInHand(hand);
        var chance = chanceFor(heldItem.material());

        if (chance < 0.0F || level >= 7) {
            return chance >= 0.0F ? false : true;
        }

        var success = level == 0 || ThreadLocalRandom.current().nextFloat() < chance;
        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();

        if (success) {
            var newLevel = level + 1;
            instance.setBlock(position, block
                    .withProperty("level", String.valueOf(newLevel))
                    .withHandler(INSTANCE));

            if (newLevel == 7) {
                scheduleReady(instance, position, 20);
            }
        }

        var soundEvent = success ? SoundEvent.BLOCK_COMPOSTER_FILL_SUCCESS : SoundEvent.BLOCK_COMPOSTER_FILL;
        instance.playSound(
                Sound.sound(soundEvent, Sound.Source.BLOCK, 1.0F, 1.0F),
                position.add(0.5D, 0.5D, 0.5D));

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setItemInHand(hand, heldItem.consume(1));
        }

        return false;
    }

    private void extractBoneMeal(Interaction interaction) {
        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();
        var itemEntity = new ItemEntity(ItemStack.of(Material.BONE_MEAL));
        itemEntity.setInstance(instance, position.add(0.5D, 1.01D, 0.5D));
        instance.setBlock(position, interaction.getBlock()
                .withProperty("level", "0")
                .withHandler(INSTANCE));
        instance.playSound(
                Sound.sound(SoundEvent.BLOCK_COMPOSTER_EMPTY, Sound.Source.BLOCK, 1.0F, 1.0F),
                position.add(0.5D, 0.5D, 0.5D));
    }

    private static void scheduleReady(Instance instance, Point position, int ticks) {
        instance.scheduleNextTick(currentInstance -> {
            if (ticks > 1) {
                scheduleReady(currentInstance, position, ticks - 1);
                return;
            }

            var current = currentInstance.getBlock(position);

            if (!current.compare(Block.COMPOSTER) || !"7".equals(current.getProperty("level"))) {
                return;
            }

            currentInstance.setBlock(position, current.withProperty("level", "8").withHandler(INSTANCE));
            currentInstance.playSound(
                    Sound.sound(SoundEvent.BLOCK_COMPOSTER_READY, Sound.Source.BLOCK, 1.0F, 1.0F),
                    position.add(0.5D, 0.5D, 0.5D));
        });
    }

    private static float chanceFor(Material material) {
        var path = material.key().value();

        if (LOW.contains(path)) {
            return 0.3F;
        }

        if (LOW_MID.contains(path)) {
            return 0.5F;
        }

        if (MID.contains(path)) {
            return 0.65F;
        }

        if (MID_HIGH.contains(path)) {
            return 0.85F;
        }

        return HIGH.contains(path) ? 1.0F : -1.0F;
    }

    private static Set<String> items(String names) {
        return Set.of(names.split("\\s+"));
    }
}

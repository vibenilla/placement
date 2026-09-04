package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public final class RespawnAnchorBlockHandler implements BlockHandler {
    public static final RespawnAnchorBlockHandler INSTANCE = new RespawnAnchorBlockHandler();
    private static final Key KEY = Key.key("placement:respawn_anchor");
    private static final Key RESPAWN_ANCHOR_WORKS = Key.key("minecraft:respawn_anchor_works");

    private RespawnAnchorBlockHandler() {

    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        var block = interaction.getBlock();
        var charges = Integer.parseInt(block.getProperty("charges"));
        var player = interaction.getPlayer();
        var heldItem = player.getItemInHand(interaction.getHand());
        var position = interaction.getBlockPosition();
        var instance = interaction.getInstance();

        if (heldItem.material() == Material.GLOWSTONE && charges < 4) {
            instance.setBlock(position, block.withProperty("charges", String.valueOf(charges + 1)).withHandler(INSTANCE));
            instance.playSound(
                    Sound.sound(SoundEvent.BLOCK_RESPAWN_ANCHOR_CHARGE, Sound.Source.BLOCK, 1.0F, 1.0F),
                    position.add(0.5D, 0.5D, 0.5D));

            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setItemInHand(interaction.getHand(), heldItem.consume(1));
            }

            return false;
        }

        if (!heldItem.isAir() || Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        if (charges == 0) {
            return true;
        }

        if (!Utility.environmentBoolean(instance, RESPAWN_ANCHOR_WORKS, false)) {
            instance.setBlock(position, Block.AIR);
            instance.explode((float) position.x() + 0.5F, (float) position.y() + 0.5F,
                    (float) position.z() + 0.5F, 5.0F);
            return false;
        }

        var respawn = new net.minestom.server.coordinate.Pos(
                position.x() + 0.5D, position.y() + 0.1D, position.z() + 0.5D);
        var currentRespawn = player.getRespawnPoint();

        if (currentRespawn == null || !currentRespawn.samePoint(respawn)) {
            player.setRespawnPoint(respawn);
            instance.playSound(
                    Sound.sound(SoundEvent.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, Sound.Source.BLOCK, 1.0F, 1.0F),
                    position.add(0.5D, 0.5D, 0.5D));
        }

        return false;
    }
}

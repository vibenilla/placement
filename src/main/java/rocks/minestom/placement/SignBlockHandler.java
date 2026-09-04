package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import net.minestom.server.sound.SoundEvent;

public final class SignBlockHandler implements BlockHandler {
    public static final SignBlockHandler INSTANCE = new SignBlockHandler();
    private static final Key KEY = Key.key("placement:sign");
    private static final Key HANGING_SIGNS = Key.key("minecraft:all_hanging_signs");

    private SignBlockHandler() {

    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        var block = interaction.getBlock();
        var player = interaction.getPlayer();
        var heldItem = player.getItemInHand(interaction.getHand());
        var position = interaction.getBlockPosition();
        var isFrontText = isFacingFrontText(block, player, position, interaction.getBlockFace());
        var nbt = block.nbtOrEmpty();

        if (!heldItem.isAir()) {
            return this.applyItem(interaction, heldItem, isFrontText, nbt);
        }

        if (Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        if (nbt.getBoolean("is_waxed")) {
            var soundEvent = Utility.hasTag(block, HANGING_SIGNS)
                    ? SoundEvent.BLOCK_HANGING_SIGN_WAXED_INTERACT_FAIL
                    : SoundEvent.BLOCK_SIGN_WAXED_INTERACT_FAIL;
            interaction.getInstance().playSound(
                    Sound.sound(soundEvent, Sound.Source.BLOCK, 1.0F, 1.0F),
                    position.add(0.5D, 0.5D, 0.5D));
            return false;
        }

        if (!mayBuild(player)) {
            return true;
        }

        player.sendPacket(new OpenSignEditorPacket(position, isFrontText));
        return false;
    }

    private boolean applyItem(
            Interaction interaction, ItemStack heldItem, boolean isFrontText, CompoundBinaryTag nbt) {
        var material = heldItem.material();
        var color = dyeColor(material);
        var applicator = material == Material.HONEYCOMB
                || material == Material.GLOW_INK_SAC
                || material == Material.INK_SAC
                || color != null;

        if (!applicator || !mayBuild(interaction.getPlayer()) || nbt.getBoolean("is_waxed")) {
            return true;
        }

        var textKey = isFrontText ? "front_text" : "back_text";
        var text = nbt.getCompound(textKey);
        CompoundBinaryTag updatedNbt;
        SoundEvent soundEvent;

        if (material == Material.HONEYCOMB) {
            updatedNbt = nbt.putBoolean("is_waxed", true);
            soundEvent = SoundEvent.ITEM_HONEYCOMB_WAX_ON;
        } else if (material == Material.GLOW_INK_SAC) {
            if (text.getBoolean("has_glowing_text")) {
                return true;
            }

            updatedNbt = nbt.put(textKey, text.putBoolean("has_glowing_text", true));
            soundEvent = SoundEvent.ITEM_GLOW_INK_SAC_USE;
        } else if (material == Material.INK_SAC) {
            if (!text.getBoolean("has_glowing_text")) {
                return true;
            }

            updatedNbt = nbt.put(textKey, text.putBoolean("has_glowing_text", false));
            soundEvent = SoundEvent.ITEM_INK_SAC_USE;
        } else {
            if (color.equals(text.getString("color", "black"))) {
                return true;
            }

            updatedNbt = nbt.put(textKey, text.putString("color", color));
            soundEvent = SoundEvent.ITEM_DYE_USE;
        }

        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();
        instance.setBlock(position, interaction.getBlock().withNbt(updatedNbt).withHandler(INSTANCE));

        if (material == Material.HONEYCOMB) {
            var packet = new WorldEventPacket(3003, position, 0, false);

            for (var player : instance.getPlayers()) {
                player.sendPacket(packet);
            }
        } else {
            instance.playSound(
                    Sound.sound(soundEvent, Sound.Source.BLOCK, 1.0F, 1.0F),
                    position.add(0.5D, 0.5D, 0.5D));
        }

        var player = interaction.getPlayer();

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setItemInHand(interaction.getHand(), heldItem.consume(1));
        }

        return false;
    }

    private static String dyeColor(Material material) {
        var path = material.key().value();
        return path.endsWith("_dye") ? path.substring(0, path.length() - "_dye".length()) : null;
    }

    private static boolean mayBuild(Player player) {
        return player.getGameMode() != GameMode.ADVENTURE && player.getGameMode() != GameMode.SPECTATOR;
    }

    private static boolean isFacingFrontText(Block block, Player player, Point position, BlockFace clickedFace) {
        var facingName = block.getProperty("facing");

        if (facingName != null) {
            var facing = parseFacing(facingName);

            if (facing != null) {
                return clickedFace == facing;
            }
        }

        var rotationName = block.getProperty("rotation");

        if (rotationName == null) {
            return true;
        }

        int rotation;

        try {
            rotation = Integer.parseInt(rotationName);
        } catch (NumberFormatException ignored) {
            return true;
        }

        var frontYawRadians = rotation * (float) (Math.PI / 8.0D);
        var frontX = -Math.sin(frontYawRadians);
        var frontZ = Math.cos(frontYawRadians);
        var playerPosition = player.getPosition();
        var deltaX = playerPosition.x() - (position.blockX() + 0.5D);
        var deltaZ = playerPosition.z() - (position.blockZ() + 0.5D);
        return deltaX * frontX + deltaZ * frontZ > 0.0D;
    }

    private static BlockFace parseFacing(String name) {
        return switch (name) {
            case "north" -> BlockFace.NORTH;
            case "south" -> BlockFace.SOUTH;
            case "east" -> BlockFace.EAST;
            case "west" -> BlockFace.WEST;
            default -> null;
        };
    }
}

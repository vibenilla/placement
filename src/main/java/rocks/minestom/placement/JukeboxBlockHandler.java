package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.WorldEventPacket;

public final class JukeboxBlockHandler implements BlockHandler {
    public static final JukeboxBlockHandler INSTANCE = new JukeboxBlockHandler();
    private static final Key KEY = Key.key("placement:jukebox");
    private static final String RECORD_ITEM = "RecordItem";

    private JukeboxBlockHandler() {

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

        if ("true".equals(block.getProperty("has_record"))) {
            this.ejectRecord(interaction);
            return false;
        }

        var player = interaction.getPlayer();
        var hand = interaction.getHand();
        var heldItem = player.getItemInHand(hand);
        var song = heldItem.get(DataComponents.JUKEBOX_PLAYABLE);

        if (song == null) {
            return true;
        }

        var record = heldItem.withAmount(1);
        var nbt = CompoundBinaryTag.builder()
                .put(block.nbtOrEmpty())
                .put(RECORD_ITEM, record.toItemNBT(MinecraftServer.process().registries()))
                .putLong("ticks_since_song_started", 0L)
                .build();
        var updated = block
                .withProperty("has_record", "true")
                .withNbt(nbt)
                .withHandler(INSTANCE);
        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();
        instance.setBlock(position, updated);

        var songId = MinecraftServer.process().jukeboxSong().getId(song);
        this.sendWorldEvent(interaction, 1010, songId);

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setItemInHand(hand, heldItem.consume(1));
        }

        return false;
    }

    private void ejectRecord(Interaction interaction) {
        var block = interaction.getBlock();
        var recordTag = block.nbtOrEmpty().getCompound(RECORD_ITEM);
        var clearedNbt = block.nbtOrEmpty()
                .remove(RECORD_ITEM)
                .remove("ticks_since_song_started");
        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();
        instance.setBlock(position, block
                .withProperty("has_record", "false")
                .withNbt(clearedNbt)
                .withHandler(INSTANCE));

        if (!recordTag.isEmpty()) {
            var record = ItemStack.fromItemNBT(recordTag, MinecraftServer.process().registries());
            var itemEntity = new ItemEntity(record);
            itemEntity.setInstance(instance, position.add(0.5D, 1.0D, 0.5D));
        }

        this.sendWorldEvent(interaction, 1011, 0);
    }

    private void sendWorldEvent(Interaction interaction, int eventId, int data) {
        var packet = new WorldEventPacket(eventId, interaction.getBlockPosition(), data, false);

        for (var player : interaction.getInstance().getPlayers()) {
            player.sendPacket(packet);
        }
    }
}

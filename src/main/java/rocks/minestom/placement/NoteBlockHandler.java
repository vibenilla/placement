package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public final class NoteBlockHandler implements BlockHandler {
    public static final NoteBlockHandler INSTANCE = new NoteBlockHandler();
    private static final Key KEY = Key.key("placement:note_block");

    private NoteBlockHandler() {

    }

    @Override
    public @NotNull Key getKey() {
        return KEY;
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        if (Utility.shouldSkipInteract(interaction)) {
            return true;
        }

        var block = interaction.getBlock();
        var noteProperty = block.getProperty("note");
        var note = noteProperty == null ? 0 : Integer.parseInt(noteProperty);
        var nextNote = (note + 1) % 25;
        var updatedBlock = block.withProperty("note", Integer.toString(nextNote));
        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();

        instance.setBlock(blockPosition, updatedBlock);

        var pitch = (float) Math.pow(2.0D, (nextNote - 12) / 12.0D);
        var instrument = block.getProperty("instrument");
        var soundEvent = soundEventFor(instrument);
        var sound = Sound.sound(soundEvent, Sound.Source.RECORD, 3.0F, pitch);
        instance.playSound(sound, blockPosition.add(0.5D, 0.5D, 0.5D));
        return false;
    }

    private static SoundEvent soundEventFor(String instrument) {
        return switch (instrument) {
            case "bass" -> SoundEvent.BLOCK_NOTE_BLOCK_BASS;
            case "snare" -> SoundEvent.BLOCK_NOTE_BLOCK_SNARE;
            case "hat" -> SoundEvent.BLOCK_NOTE_BLOCK_HAT;
            case "basedrum" -> SoundEvent.BLOCK_NOTE_BLOCK_BASEDRUM;
            case "bell" -> SoundEvent.BLOCK_NOTE_BLOCK_BELL;
            case "flute" -> SoundEvent.BLOCK_NOTE_BLOCK_FLUTE;
            case "chime" -> SoundEvent.BLOCK_NOTE_BLOCK_CHIME;
            case "guitar" -> SoundEvent.BLOCK_NOTE_BLOCK_GUITAR;
            case "xylophone" -> SoundEvent.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case "iron_xylophone" -> SoundEvent.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case "cow_bell" -> SoundEvent.BLOCK_NOTE_BLOCK_COW_BELL;
            case "didgeridoo" -> SoundEvent.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case "bit" -> SoundEvent.BLOCK_NOTE_BLOCK_BIT;
            case "banjo" -> SoundEvent.BLOCK_NOTE_BLOCK_BANJO;
            case "pling" -> SoundEvent.BLOCK_NOTE_BLOCK_PLING;
            case "zombie" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_ZOMBIE;
            case "skeleton" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_SKELETON;
            case "creeper" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_CREEPER;
            case "dragon" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON;
            case "wither_skeleton" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_WITHER_SKELETON;
            case "piglin" -> SoundEvent.BLOCK_NOTE_BLOCK_IMITATE_PIGLIN;
            case null, default -> SoundEvent.BLOCK_NOTE_BLOCK_HARP;
        };
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

public final class ButtonBlockHandler implements BlockHandler {
    public static final ButtonBlockHandler INSTANCE = new ButtonBlockHandler();
    private static final Key KEY = Key.key("placement:button");
    private static final Key WOODEN_BUTTONS_TAG = Key.key("minecraft:wooden_buttons");
    private static final Key BUTTONS_TAG = Key.key("minecraft:buttons");
    private static final int WOODEN_DELAY = 30;
    private static final int STONE_DELAY = 20;

    private ButtonBlockHandler() {

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

        if ("true".equals(block.getProperty("powered"))) {
            return false;
        }

        var instance = interaction.getInstance();
        var blockPosition = interaction.getBlockPosition();
        var pressedBlock = block.withProperty("powered", "true");

        instance.setBlock(blockPosition, pressedBlock);

        var delay = this.releaseDelay(block);

        instance.scheduler().buildTask(() -> {
            var currentBlock = instance.getBlock(blockPosition);
            var buttonsTag = MinecraftServer.process().blocks().getTag(BUTTONS_TAG);

            if (buttonsTag == null || !buttonsTag.contains(currentBlock)) {
                return;
            }

            if (!"true".equals(currentBlock.getProperty("powered"))) {
                return;
            }

            instance.setBlock(blockPosition, currentBlock.withProperty("powered", "false"));
        }).delay(TaskSchedule.tick(delay)).schedule();

        // TODO: vanilla plays the per-material button click sound on press and release
        // TODO: vanilla emits a redstone signal on the connected face; redstone is not simulated here
        return false;
    }

    private int releaseDelay(@NotNull Block block) {
        var tag = MinecraftServer.process().blocks().getTag(WOODEN_BUTTONS_TAG);

        if (tag != null && tag.contains(block)) {
            return WOODEN_DELAY;
        }

        return STONE_DELAY;
    }
}

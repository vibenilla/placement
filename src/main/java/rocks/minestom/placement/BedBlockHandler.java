package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.world.attribute.BedRule;

public final class BedBlockHandler implements BlockHandler {
    public static final BedBlockHandler INSTANCE = new BedBlockHandler();
    private static final Key KEY = Key.key("placement:bed");

    private BedBlockHandler() {

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

        var instance = interaction.getInstance();
        var position = interaction.getBlockPosition();
        var block = interaction.getBlock();
        var half = block.getProperty("part");

        if (!"head".equals(half)) {
            var facingName = block.getProperty("facing");
            var facing = facingName == null
                    ? net.minestom.server.instance.block.BlockFace.NORTH
                    : net.minestom.server.instance.block.BlockFace.valueOf(facingName.toUpperCase());
            position = position.relative(facing);
        }

        var bedRuleValue = Utility.environmentValue(
                instance, Key.key("minecraft:bed_rule"), BedRule.CAN_SLEEP_WHEN_DARK);
        var bedRule = bedRuleValue instanceof BedRule value ? value : BedRule.CAN_SLEEP_WHEN_DARK;

        if (bedRule.explodes()) {
            instance.setBlock(position, net.minestom.server.instance.block.Block.AIR);
            instance.explode((float) position.x() + 0.5F, (float) position.y() + 0.5F,
                    (float) position.z() + 0.5F, 5.0F);
            return false;
        }

        if (bedRule.canSetSpawn() == BedRule.Rule.ALWAYS
                || bedRule.canSetSpawn() == BedRule.Rule.WHEN_DARK && isDark(instance)) {
            interaction.getPlayer().setRespawnPoint(new Pos(position.x() + 0.5D, position.y() + 0.1D, position.z() + 0.5D));
        }

        return false;
    }

    private static boolean isDark(net.minestom.server.instance.Instance instance) {
        var time = instance.getTime() % 24000L;
        return time >= 12500L && time < 23500L;
    }
}

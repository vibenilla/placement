package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket;

public final class SignBlockHandler implements BlockHandler {
    public static final SignBlockHandler INSTANCE = new SignBlockHandler();
    private static final Key KEY = Key.key("placement:sign");

    private SignBlockHandler() {

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

        if ("true".equals(block.getProperty("waxed"))) {
            return false;
        }

        var player = interaction.getPlayer();
        var position = interaction.getBlockPosition();
        var isFrontText = isFacingFrontText(block, player, position, interaction.getBlockFace());

        player.sendPacket(new OpenSignEditorPacket(position, isFrontText));
        return false;
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

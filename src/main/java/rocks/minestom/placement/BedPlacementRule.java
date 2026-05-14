package rocks.minestom.placement;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BedPlacementRule extends BlockPlacementRule {
    public BedPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        if (!(placementState.instance() instanceof Instance instance)) {
            return null;
        }

        var playerPosition = placementState.playerPosition();
        var yaw = playerPosition == null ? 0.0F : playerPosition.yaw();
        var facing = BlockFace.fromYaw(yaw);
        var placePosition = placementState.placePosition();
        var headPosition = placePosition.relative(facing);
        var existingHeadBlock = instance.getBlock(headPosition);

        if (!existingHeadBlock.registry().isReplaceable()) {
            return null;
        }

        var facingName = facing.name().toLowerCase();
        var headBlock = this.block
                .withHandler(BedBlockHandler.INSTANCE)
                .withProperty("facing", facingName)
                .withProperty("part", "head")
                .withProperty("occupied", "false");

        instance.setBlock(headPosition, headBlock, false);

        return this.block
                .withHandler(BedBlockHandler.INSTANCE)
                .withProperty("facing", facingName)
                .withProperty("part", "foot")
                .withProperty("occupied", "false");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var part = currentBlock.getProperty("part");
        var facing = parseFacing(currentBlock.getProperty("facing"));

        if (part == null || facing == null) {
            return currentBlock;
        }

        var fromFace = updateState.fromFace();
        var partnerDirection = "head".equals(part) ? facing.getOppositeFace() : facing;

        if (fromFace != partnerDirection) {
            return currentBlock;
        }

        var partner = updateState.instance().getBlock(updateState.blockPosition().relative(partnerDirection));
        var expectedPartnerPart = "head".equals(part) ? "foot" : "head";
        var matches = partner.compare(this.block) && expectedPartnerPart.equals(partner.getProperty("part"));
        return matches ? currentBlock : Block.AIR;
    }

    private static BlockFace parseFacing(@Nullable String facingName) {
        return switch (facingName) {
            case "north" -> BlockFace.NORTH;
            case "east" -> BlockFace.EAST;
            case "south" -> BlockFace.SOUTH;
            case "west" -> BlockFace.WEST;
            case null, default -> null;
        };
    }
}

package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class BannerPlacementRule extends BlockPlacementRule {
    public static final Key KEY = Key.key("minecraft:banners");
    private static final String BANNER_SUFFIX = "_banner";
    private static final String WALL_BANNER_SUFFIX = "_wall_banner";
    private static final List<Direction> DEFAULT_NEAREST_DIRECTIONS = List.of(
            Direction.DOWN,
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
            Direction.UP
    );

    public BannerPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        return this.placeBannerLikeVanilla(placementState);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
        if (this.isWallBanner()) {
            return this.updateWallBanner(updateState);
        } else {
            return this.updateStandingBanner(updateState);
        }
    }

    @Override
    public int maxUpdateDistance() {
        return 1;
    }

    private boolean isWallBanner() {
        return this.block.key().value().endsWith(WALL_BANNER_SUFFIX);
    }

    private @Nullable Block placeBannerLikeVanilla(@NotNull PlacementState placementState) {
        var standingBanner = this.getStandingBanner();
        var wallBanner = this.getWallBanner();

        if (standingBanner == null || wallBanner == null) {
            return null;
        }

        var baseBlock = placementState.block();
        standingBanner = standingBanner.withNbt(baseBlock.nbt()).withHandler(baseBlock.handler());
        wallBanner = wallBanner.withNbt(baseBlock.nbt()).withHandler(baseBlock.handler());

        var replaceClicked = this.isReplacingClickedBlock(placementState);
        var nearestDirections = getNearestLookingDirections(placementState.playerPosition(), placementState.blockFace(), replaceClicked);

        var placePosition = placementState.placePosition();
        for (var direction : nearestDirections) {
            if (direction == Direction.UP) {
                continue;
            }

            if (direction == Direction.DOWN) {
                var rotation = this.getRotation(placementState.playerPosition());
                var candidate = standingBanner.withProperty("rotation", String.valueOf(rotation));
                if (this.canSurviveStandingBanner(placementState.instance(), placePosition)
                        && this.isUnobstructed(placementState.instance(), placePosition, candidate)) {
                    return candidate;
                }
            } else {
                var facing = direction.opposite();
                var candidate = wallBanner.withProperty("facing", facing.name().toLowerCase());

                if (this.canSurviveWallBanner(placementState.instance(), placePosition, facing) && this.isUnobstructed(placementState.instance(), placePosition, candidate)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private Block updateStandingBanner(@NotNull UpdateState updateState) {
        var blockPosition = updateState.blockPosition();
        var instance = updateState.instance();
        var blockBelow = instance.getBlock(blockPosition.blockX(), blockPosition.blockY() - 1, blockPosition.blockZ());

        if (!this.isLegacySolid(blockBelow)) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    private int getRotation(@Nullable Pos playerPosition) {
        if (playerPosition == null) {
            return 0;
        }

        var rotation = (float) (playerPosition.yaw() + 180.0D);
        return (int) Math.floor((double) (rotation * 16.0F / 360.0F) + 0.5D) & 15;
    }

    private Block updateWallBanner(@NotNull UpdateState updateState) {
        var currentBlock = updateState.currentBlock();
        var facing = currentBlock.getProperty("facing");

        if (facing == null) {
            return Block.AIR;
        }

        var direction = Direction.valueOf(facing.toUpperCase());
        var supportDirection = direction.opposite();
        var blockPosition = updateState.blockPosition();
        var instance = updateState.instance();

        var blockX = blockPosition.blockX();
        var blockY = blockPosition.blockY();
        var blockZ = blockPosition.blockZ();

        var blockBehind = instance.getBlock(
                blockX + supportDirection.normalX(),
                blockY,
                blockZ + supportDirection.normalZ());

        if (!this.isLegacySolid(blockBehind)) {
            return Block.AIR;
        }

        return currentBlock;
    }

    private boolean isReplacingClickedBlock(@NotNull PlacementState placementState) {
        var currentBlock = placementState.instance().getBlock(placementState.placePosition());
        return !currentBlock.isAir() && currentBlock.registry().isReplaceable();
    }

    private boolean canSurviveStandingBanner(@NotNull Block.Getter instance, @NotNull Point position) {
        var blockBelow = instance.getBlock(position.blockX(), position.blockY() - 1, position.blockZ());
        return this.isLegacySolid(blockBelow);
    }

    private boolean canSurviveWallBanner(@NotNull Block.Getter instance,
                                         @NotNull Point position,
                                         @NotNull Direction facing) {
        var supportDirection = facing.opposite();
        var blockBehind = instance.getBlock(
                position.blockX() + supportDirection.normalX(),
                position.blockY() + supportDirection.normalY(),
                position.blockZ() + supportDirection.normalZ()
        );
        return this.isLegacySolid(blockBehind);
    }

    private boolean isUnobstructed(@NotNull Block.Getter instance, @NotNull Point position, @NotNull Block banner) {
        if (!(instance instanceof Instance level)) {
            return true;
        }

        return CollisionUtils.canPlaceBlockAt(level, position, banner) == null;
    }

    private boolean isLegacySolid(@NotNull Block block) {
        var shape = block.registry().collisionShape();
        var start = shape.relativeStart();
        var end = shape.relativeEnd();

        if (start.x() == 0.0D && start.y() == 0.0D && start.z() == 0.0D
                && end.x() == 0.0D && end.y() == 0.0D && end.z() == 0.0D) {
            return false;
        }

        var xSize = end.x() - start.x();
        var ySize = end.y() - start.y();
        var zSize = end.z() - start.z();
        var averageSize = (xSize + ySize + zSize) / 3.0D;

        return averageSize >= 0.7291666666666666D || ySize >= 1.0D;
    }

    private @Nullable Block getStandingBanner() {
        var key = this.block.key();
        var namespace = key.namespace();
        var value = key.value();

        if (value.endsWith(WALL_BANNER_SUFFIX)) {
            value = value.substring(0, value.length() - WALL_BANNER_SUFFIX.length()) + BANNER_SUFFIX;
        }

        return Block.fromKey(Key.key(namespace + ":" + value));
    }

    private @Nullable Block getWallBanner() {
        var key = this.block.key();
        var namespace = key.namespace();
        var value = key.value();

        if (value.endsWith(BANNER_SUFFIX) && !value.endsWith(WALL_BANNER_SUFFIX)) {
            value = value.substring(0, value.length() - BANNER_SUFFIX.length()) + WALL_BANNER_SUFFIX;
        }

        return Block.fromKey(Key.key(namespace + ":" + value));
    }

    private static Iterable<Direction> getNearestLookingDirections(@Nullable Pos playerPosition,
                                                                   @Nullable BlockFace clickedFace,
                                                                   boolean replaceClicked) {
        var ordered = orderedByNearest(playerPosition);

        if (replaceClicked || clickedFace == null) {
            return ordered;
        }

        var preferred = clickedFace.getOppositeFace().toDirection();
        return preferredFirst(ordered, preferred);
    }

    private static Iterable<Direction> orderedByNearest(@Nullable Pos playerPosition) {
        if (playerPosition == null) {
            return DEFAULT_NEAREST_DIRECTIONS;
        }

        var pitchRadians = (float) (playerPosition.pitch() * (Math.PI / 180.0D));
        var yawRadians = (float) (-playerPosition.yaw() * (Math.PI / 180.0D));

        var pitchSin = (float) Math.sin(pitchRadians);
        var pitchCos = (float) Math.cos(pitchRadians);
        var yawSin = (float) Math.sin(yawRadians);
        var yawCos = (float) Math.cos(yawRadians);

        var isEast = yawSin > 0.0F;
        var isUp = pitchSin < 0.0F;
        var isSouth = yawCos > 0.0F;

        var horizontalEastOrWest = isEast ? yawSin : -yawSin;
        var verticalUpOrDown = isUp ? -pitchSin : pitchSin;
        var horizontalSouthOrNorth = isSouth ? yawCos : -yawCos;

        var eastOrWestWeight = horizontalEastOrWest * pitchCos;
        var southOrNorthWeight = horizontalSouthOrNorth * pitchCos;

        var eastOrWest = isEast ? Direction.EAST : Direction.WEST;
        var upOrDown = isUp ? Direction.UP : Direction.DOWN;
        var southOrNorth = isSouth ? Direction.SOUTH : Direction.NORTH;

        if (horizontalEastOrWest > horizontalSouthOrNorth) {
            if (verticalUpOrDown > eastOrWestWeight) {
                return new DirectionOrder(upOrDown, eastOrWest, southOrNorth);
            }

            return southOrNorthWeight > verticalUpOrDown
                    ? new DirectionOrder(eastOrWest, southOrNorth, upOrDown)
                    : new DirectionOrder(eastOrWest, upOrDown, southOrNorth);
        }

        if (verticalUpOrDown > southOrNorthWeight) {
            return new DirectionOrder(upOrDown, southOrNorth, eastOrWest);
        }

        return eastOrWestWeight > verticalUpOrDown
                ? new DirectionOrder(southOrNorth, eastOrWest, upOrDown)
                : new DirectionOrder(southOrNorth, upOrDown, eastOrWest);
    }

    private static Iterable<Direction> preferredFirst(@NotNull Iterable<Direction> orderedByNearest, @NotNull Direction preferred) {
        return () -> new Iterator<>() {
            private final Iterator<Direction> iterator = orderedByNearest.iterator();
            private int emittedCount;

            @Override
            public boolean hasNext() {
                return this.emittedCount < 6;
            }

            @Override
            public Direction next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }

                if (this.emittedCount == 0) {
                    this.emittedCount++;
                    return preferred;
                }

                while (this.iterator.hasNext()) {
                    var nextDirection = this.iterator.next();
                    if (nextDirection != preferred) {
                        this.emittedCount++;
                        return nextDirection;
                    }
                }

                throw new NoSuchElementException();
            }
        };
    }

    private record DirectionOrder(@NotNull Direction first, @NotNull Direction second, @NotNull Direction third) implements Iterable<Direction> {
        @Override
        public @NotNull Iterator<Direction> iterator() {
            return new Iterator<>() {
                private int index;

                @Override
                public boolean hasNext() {
                    return this.index < 6;
                }

                @Override
                public Direction next() {
                    var nextDirection = switch (this.index) {
                        case 0 -> DirectionOrder.this.first;
                        case 1 -> DirectionOrder.this.second;
                        case 2 -> DirectionOrder.this.third;
                        case 3 -> DirectionOrder.this.third.opposite();
                        case 4 -> DirectionOrder.this.second.opposite();
                        case 5 -> DirectionOrder.this.first.opposite();
                        default -> throw new NoSuchElementException();
                    };
                    this.index++;
                    return nextDirection;
                }
            };
        }
    }
}

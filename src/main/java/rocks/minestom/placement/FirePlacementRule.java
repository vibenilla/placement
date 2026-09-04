package rocks.minestom.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.Set;

public final class FirePlacementRule extends BlockPlacementRule {
    private static final Key SOUL_FIRE_BASE_BLOCKS = Key.key("minecraft:soul_fire_base_blocks");
    private static final Set<String> FLAMMABLE_BLOCKS = Set.of("""
            oak_planks spruce_planks birch_planks jungle_planks acacia_planks cherry_planks dark_oak_planks
            pale_oak_planks mangrove_planks bamboo_planks bamboo_mosaic oak_slab spruce_slab birch_slab jungle_slab
            acacia_slab cherry_slab dark_oak_slab pale_oak_slab mangrove_slab bamboo_slab bamboo_mosaic_slab
            oak_fence_gate spruce_fence_gate birch_fence_gate jungle_fence_gate acacia_fence_gate cherry_fence_gate
            dark_oak_fence_gate pale_oak_fence_gate mangrove_fence_gate bamboo_fence_gate oak_fence spruce_fence
            birch_fence jungle_fence acacia_fence cherry_fence dark_oak_fence pale_oak_fence mangrove_fence bamboo_fence
            oak_stairs birch_stairs spruce_stairs jungle_stairs acacia_stairs cherry_stairs dark_oak_stairs
            pale_oak_stairs mangrove_stairs bamboo_stairs bamboo_mosaic_stairs oak_log spruce_log birch_log jungle_log
            acacia_log cherry_log pale_oak_log dark_oak_log mangrove_log bamboo_block stripped_oak_log
            stripped_spruce_log stripped_birch_log stripped_jungle_log stripped_acacia_log stripped_cherry_log
            stripped_dark_oak_log stripped_pale_oak_log stripped_mangrove_log stripped_bamboo_block stripped_oak_wood
            stripped_spruce_wood stripped_birch_wood stripped_jungle_wood stripped_acacia_wood stripped_cherry_wood
            stripped_dark_oak_wood stripped_pale_oak_wood stripped_mangrove_wood oak_wood spruce_wood birch_wood
            jungle_wood acacia_wood cherry_wood pale_oak_wood dark_oak_wood mangrove_wood mangrove_roots oak_leaves
            spruce_leaves birch_leaves jungle_leaves acacia_leaves cherry_leaves dark_oak_leaves pale_oak_leaves
            mangrove_leaves bookshelf tnt short_grass fern dead_bush short_dry_grass tall_dry_grass sunflower lilac
            rose_bush peony tall_grass large_fern dandelion golden_dandelion poppy open_eyeblossom closed_eyeblossom
            blue_orchid allium azure_bluet red_tulip orange_tulip white_tulip pink_tulip oxeye_daisy cornflower
            lily_of_the_valley torchflower pitcher_plant wither_rose pink_petals wildflowers leaf_litter cactus_flower
            white_wool orange_wool magenta_wool light_blue_wool yellow_wool lime_wool pink_wool gray_wool
            light_gray_wool cyan_wool purple_wool blue_wool brown_wool green_wool red_wool black_wool
            vine coal_block hay_block target
            white_carpet orange_carpet magenta_carpet light_blue_carpet yellow_carpet lime_carpet pink_carpet gray_carpet
            light_gray_carpet cyan_carpet purple_carpet blue_carpet brown_carpet green_carpet red_carpet black_carpet
            pale_moss_block pale_moss_carpet pale_hanging_moss dried_kelp_block bamboo scaffolding lectern composter
            sweet_berry_bush beehive bee_nest azalea_leaves flowering_azalea_leaves cave_vines cave_vines_plant
            spore_blossom azalea flowering_azalea big_dripleaf big_dripleaf_stem small_dripleaf hanging_roots
            glow_lichen firefly_bush bush acacia_shelf bamboo_shelf birch_shelf cherry_shelf dark_oak_shelf jungle_shelf
            mangrove_shelf oak_shelf pale_oak_shelf spruce_shelf
            """.split("\\s+"));

    public FirePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!this.canSurvive(placementState.instance(), placementState.placePosition())) {
            return null;
        }

        return this.stateFor(placementState.instance(), placementState.placePosition(), 0);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var blockGetter = updateState.instance();
        var position = updateState.blockPosition();
        var current = updateState.currentBlock();

        if (current.compare(Block.SOUL_FIRE)) {
            return this.isSoulFireBase(blockGetter.getBlock(position.relative(BlockFace.BOTTOM)))
                    ? current
                    : Block.AIR;
        }

        if (!this.canSurvive(blockGetter, position)) {
            return Block.AIR;
        }

        var age = Integer.parseInt(current.getProperty("age"));
        return this.stateFor(blockGetter, position, age);
    }

    private Block stateFor(Block.Getter blockGetter, Point position, int age) {
        var below = blockGetter.getBlock(position.relative(BlockFace.BOTTOM));

        if (this.isSoulFireBase(below)) {
            return Block.SOUL_FIRE;
        }

        var result = Block.FIRE.withProperty("age", String.valueOf(age));

        if (this.canBurn(below) || Utility.canSupportRigidBlock(below, BlockFace.TOP)) {
            return result;
        }

        return result
                .withProperty("north", String.valueOf(this.canBurn(blockGetter.getBlock(position.relative(BlockFace.NORTH)))))
                .withProperty("east", String.valueOf(this.canBurn(blockGetter.getBlock(position.relative(BlockFace.EAST)))))
                .withProperty("south", String.valueOf(this.canBurn(blockGetter.getBlock(position.relative(BlockFace.SOUTH)))))
                .withProperty("west", String.valueOf(this.canBurn(blockGetter.getBlock(position.relative(BlockFace.WEST)))))
                .withProperty("up", String.valueOf(this.canBurn(blockGetter.getBlock(position.relative(BlockFace.TOP)))));
    }

    private boolean canSurvive(Block.Getter blockGetter, Point position) {
        var below = blockGetter.getBlock(position.relative(BlockFace.BOTTOM));

        if (this.isSoulFireBase(below) || Utility.canSupportRigidBlock(below, BlockFace.TOP)) {
            return true;
        }

        for (var face : BlockFace.values()) {
            if (this.canBurn(blockGetter.getBlock(position.relative(face)))) {
                return true;
            }
        }

        return false;
    }

    private boolean isSoulFireBase(Block block) {
        var tag = MinecraftServer.process().blocks().getTag(SOUL_FIRE_BASE_BLOCKS);
        return tag != null && tag.contains(block);
    }

    private boolean canBurn(Block block) {
        return FLAMMABLE_BLOCKS.contains(block.key().value());
    }
}

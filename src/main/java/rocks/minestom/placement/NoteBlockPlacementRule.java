package rocks.minestom.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.Set;

public final class NoteBlockPlacementRule extends BlockPlacementRule {
    private static final Set<String> BASE_DRUM_BLOCKS = blocks("""
            stone granite polished_granite diorite polished_diorite andesite polished_andesite cobblestone bedrock
            gold_ore iron_ore coal_ore nether_gold_ore lapis_ore dispenser sandstone chiseled_sandstone cut_sandstone
            bricks mossy_cobblestone obsidian spawner creaking_heart diamond_ore furnace stone_pressure_plate
            redstone_ore netherrack basalt polished_basalt stone_bricks mossy_stone_bricks cracked_stone_bricks
            chiseled_stone_bricks mud_bricks resin_block resin_bricks resin_brick_slab resin_brick_wall
            chiseled_resin_bricks nether_bricks nether_brick_fence enchanting_table end_portal_frame end_stone
            emerald_ore ender_chest nether_quartz_ore quartz_block chiseled_quartz_block quartz_pillar dropper
            prismarine prismarine_bricks dark_prismarine prismarine_slab prismarine_brick_slab dark_prismarine_slab
            terracotta coal_block red_sandstone chiseled_red_sandstone cut_red_sandstone stone_slab smooth_stone_slab
            sandstone_slab cut_sandstone_slab petrified_oak_slab cobblestone_slab brick_slab stone_brick_slab
            mud_brick_slab nether_brick_slab quartz_slab red_sandstone_slab cut_red_sandstone_slab purpur_slab
            smooth_stone smooth_sandstone smooth_quartz smooth_red_sandstone purpur_block purpur_pillar end_stone_bricks
            magma_block red_nether_bricks observer dead_tube_coral_block dead_brain_coral_block dead_bubble_coral_block
            dead_fire_coral_block dead_horn_coral_block tube_coral_block brain_coral_block bubble_coral_block
            fire_coral_block horn_coral_block dead_tube_coral dead_brain_coral dead_bubble_coral dead_fire_coral
            dead_horn_coral dead_tube_coral_fan dead_brain_coral_fan dead_bubble_coral_fan dead_fire_coral_fan
            dead_horn_coral_fan dead_tube_coral_wall_fan dead_brain_coral_wall_fan dead_bubble_coral_wall_fan
            dead_fire_coral_wall_fan dead_horn_coral_wall_fan smoker blast_furnace stonecutter warped_nylium
            crimson_nylium crying_obsidian respawn_anchor blackstone polished_blackstone_pressure_plate
            chiseled_nether_bricks cracked_nether_bricks tuff sulfur cinnabar calcite dripstone_block pointed_dripstone
            sulfur_spike deepslate raw_iron_block raw_copper_block raw_gold_block reinforced_deepslate trial_spawner vault
            deepslate_gold_ore deepslate_iron_ore deepslate_coal_ore deepslate_lapis_ore deepslate_diamond_ore
            cobblestone_stairs deepslate_redstone_ore brick_stairs stone_brick_stairs mud_brick_stairs
            resin_brick_stairs nether_brick_stairs sandstone_stairs deepslate_emerald_ore cobblestone_wall
            mossy_cobblestone_wall quartz_stairs prismarine_stairs prismarine_brick_stairs dark_prismarine_stairs
            red_sandstone_stairs purpur_stairs polished_granite_stairs smooth_red_sandstone_stairs
            mossy_stone_brick_stairs polished_diorite_stairs mossy_cobblestone_stairs end_stone_brick_stairs
            stone_stairs smooth_sandstone_stairs smooth_quartz_stairs granite_stairs andesite_stairs
            red_nether_brick_stairs polished_andesite_stairs diorite_stairs polished_granite_slab
            smooth_red_sandstone_slab mossy_stone_brick_slab polished_diorite_slab mossy_cobblestone_slab
            end_stone_brick_slab smooth_sandstone_slab smooth_quartz_slab granite_slab andesite_slab
            red_nether_brick_slab polished_andesite_slab diorite_slab brick_wall prismarine_wall red_sandstone_wall
            mossy_stone_brick_wall granite_wall stone_brick_wall mud_brick_wall nether_brick_wall andesite_wall
            red_nether_brick_wall sandstone_wall end_stone_brick_wall diorite_wall blackstone_stairs blackstone_wall
            blackstone_slab polished_blackstone polished_blackstone_bricks cracked_polished_blackstone_bricks
            chiseled_polished_blackstone polished_blackstone_brick_slab polished_blackstone_brick_stairs
            polished_blackstone_brick_wall gilded_blackstone polished_blackstone_stairs polished_blackstone_slab
            polished_blackstone_wall quartz_bricks tuff_slab tuff_stairs tuff_wall polished_tuff polished_tuff_slab
            polished_tuff_stairs polished_tuff_wall chiseled_tuff tuff_bricks tuff_brick_slab tuff_brick_stairs
            tuff_brick_wall chiseled_tuff_bricks potent_sulfur polished_sulfur sulfur_bricks chiseled_sulfur
            polished_cinnabar cinnabar_bricks chiseled_cinnabar copper_ore deepslate_copper_ore cobbled_deepslate
            cobbled_deepslate_stairs cobbled_deepslate_slab cobbled_deepslate_wall polished_deepslate
            polished_deepslate_stairs polished_deepslate_slab polished_deepslate_wall deepslate_tiles
            deepslate_tile_stairs deepslate_tile_slab deepslate_tile_wall deepslate_bricks deepslate_brick_stairs
            deepslate_brick_slab deepslate_brick_wall chiseled_deepslate cracked_deepslate_bricks
            cracked_deepslate_tiles smooth_basalt
            """);
    private static final Set<String> BASS_BLOCKS = blocks("""
            oak_planks spruce_planks birch_planks jungle_planks acacia_planks cherry_planks dark_oak_planks
            pale_oak_wood pale_oak_planks mangrove_planks bamboo_planks bamboo_mosaic oak_log spruce_log birch_log
            jungle_log acacia_log cherry_log dark_oak_log pale_oak_log mangrove_log mangrove_roots bamboo_block
            stripped_spruce_log stripped_birch_log stripped_jungle_log stripped_acacia_log stripped_cherry_log
            stripped_dark_oak_log stripped_pale_oak_log stripped_oak_log stripped_mangrove_log stripped_bamboo_block
            oak_wood spruce_wood birch_wood jungle_wood acacia_wood cherry_wood dark_oak_wood mangrove_wood
            stripped_oak_wood stripped_spruce_wood stripped_birch_wood stripped_jungle_wood stripped_acacia_wood
            stripped_cherry_wood stripped_dark_oak_wood stripped_pale_oak_wood stripped_mangrove_wood note_block
            bookshelf chiseled_bookshelf acacia_shelf bamboo_shelf birch_shelf cherry_shelf crimson_shelf
            dark_oak_shelf jungle_shelf mangrove_shelf oak_shelf pale_oak_shelf spruce_shelf warped_shelf chest
            crafting_table oak_sign spruce_sign birch_sign acacia_sign cherry_sign jungle_sign dark_oak_sign
            pale_oak_sign mangrove_sign bamboo_sign oak_door oak_wall_sign spruce_wall_sign birch_wall_sign
            acacia_wall_sign cherry_wall_sign jungle_wall_sign dark_oak_wall_sign pale_oak_wall_sign mangrove_wall_sign
            bamboo_wall_sign oak_hanging_sign spruce_hanging_sign birch_hanging_sign acacia_hanging_sign
            cherry_hanging_sign jungle_hanging_sign dark_oak_hanging_sign pale_oak_hanging_sign crimson_hanging_sign
            warped_hanging_sign mangrove_hanging_sign bamboo_hanging_sign oak_wall_hanging_sign spruce_wall_hanging_sign
            birch_wall_hanging_sign acacia_wall_hanging_sign cherry_wall_hanging_sign jungle_wall_hanging_sign
            dark_oak_wall_hanging_sign pale_oak_wall_hanging_sign mangrove_wall_hanging_sign crimson_wall_hanging_sign
            warped_wall_hanging_sign bamboo_wall_hanging_sign oak_pressure_plate spruce_pressure_plate
            birch_pressure_plate jungle_pressure_plate acacia_pressure_plate cherry_pressure_plate
            dark_oak_pressure_plate pale_oak_pressure_plate mangrove_pressure_plate bamboo_pressure_plate jukebox
            oak_fence oak_trapdoor spruce_trapdoor birch_trapdoor jungle_trapdoor acacia_trapdoor cherry_trapdoor
            dark_oak_trapdoor pale_oak_trapdoor mangrove_trapdoor bamboo_trapdoor brown_mushroom_block
            red_mushroom_block mushroom_stem oak_fence_gate trapped_chest daylight_detector oak_slab spruce_slab
            birch_slab jungle_slab acacia_slab cherry_slab dark_oak_slab pale_oak_slab mangrove_slab bamboo_slab
            bamboo_mosaic_slab spruce_fence_gate birch_fence_gate jungle_fence_gate acacia_fence_gate cherry_fence_gate
            dark_oak_fence_gate pale_oak_fence_gate mangrove_fence_gate bamboo_fence_gate spruce_fence birch_fence
            jungle_fence acacia_fence cherry_fence dark_oak_fence pale_oak_fence mangrove_fence bamboo_fence
            spruce_door birch_door jungle_door acacia_door cherry_door dark_oak_door pale_oak_door mangrove_door
            bamboo_door loom barrel cartography_table fletching_table lectern smithing_table campfire soul_campfire
            warped_stem stripped_warped_stem warped_hyphae stripped_warped_hyphae crimson_stem stripped_crimson_stem
            crimson_hyphae stripped_crimson_hyphae crimson_planks warped_planks crimson_slab warped_slab
            crimson_pressure_plate warped_pressure_plate crimson_fence warped_fence crimson_trapdoor warped_trapdoor
            crimson_fence_gate warped_fence_gate crimson_door warped_door crimson_sign warped_sign crimson_wall_sign
            warped_wall_sign composter bee_nest beehive firefly_bush oak_stairs spruce_stairs birch_stairs jungle_stairs
            acacia_stairs cherry_stairs dark_oak_stairs pale_oak_stairs mangrove_stairs bamboo_stairs
            bamboo_mosaic_stairs crimson_stairs warped_stairs
            """);
    private static final Set<String> HAT_BLOCKS = blocks("glass glass_pane beacon sea_lantern conduit tinted_glass");
    private static final Set<String> SNARE_BLOCKS = blocks(
            "sand suspicious_sand red_sand gravel suspicious_gravel heavy_core");
    private static final Set<String> COPPER_INSTRUMENT_BLOCKS = blocks("""
            copper_block cut_copper chiseled_copper cut_copper_stairs cut_copper_slab
            exposed_copper exposed_cut_copper exposed_chiseled_copper exposed_cut_copper_stairs exposed_cut_copper_slab
            weathered_copper weathered_cut_copper weathered_chiseled_copper weathered_cut_copper_stairs
            weathered_cut_copper_slab oxidized_copper oxidized_cut_copper oxidized_chiseled_copper
            oxidized_cut_copper_stairs oxidized_cut_copper_slab waxed_copper_block waxed_cut_copper
            waxed_chiseled_copper waxed_cut_copper_stairs waxed_cut_copper_slab waxed_exposed_copper
            waxed_exposed_cut_copper waxed_exposed_chiseled_copper waxed_exposed_cut_copper_stairs
            waxed_exposed_cut_copper_slab waxed_weathered_copper waxed_weathered_cut_copper
            waxed_weathered_chiseled_copper waxed_weathered_cut_copper_stairs waxed_weathered_cut_copper_slab
            waxed_oxidized_copper waxed_oxidized_cut_copper waxed_oxidized_chiseled_copper
            waxed_oxidized_cut_copper_stairs waxed_oxidized_cut_copper_slab
            """);
    private static final Set<String> COLORS = blocks("""
            white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown green red black
            """);

    public NoteBlockPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        return this.setInstrument(
                placementState.block(), placementState.instance(), placementState.placePosition());
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (updateState.fromFace() != BlockFace.TOP && updateState.fromFace() != BlockFace.BOTTOM) {
            return updateState.currentBlock();
        }

        return this.setInstrument(
                updateState.currentBlock(), updateState.instance(), updateState.blockPosition());
    }

    private Block setInstrument(Block noteBlock, Block.Getter blockGetter, Point position) {
        var aboveInstrument = instrumentFor(blockGetter.getBlock(position.relative(BlockFace.TOP)));
        var instrument = isHeadInstrument(aboveInstrument)
                ? aboveInstrument
                : instrumentFor(blockGetter.getBlock(position.relative(BlockFace.BOTTOM)));

        if (isHeadInstrument(instrument) && !isHeadInstrument(aboveInstrument)) {
            instrument = "harp";
        }

        return noteBlock
                .withProperty("instrument", instrument)
                .withHandler(NoteBlockHandler.INSTANCE);
    }

    private static String instrumentFor(Block block) {
        var path = block.key().value();

        if (COPPER_INSTRUMENT_BLOCKS.contains(path)) {
            if (path.contains("oxidized")) {
                return "trumpet_oxidized";
            }

            if (path.contains("weathered")) {
                return "trumpet_weathered";
            }

            if (path.contains("exposed")) {
                return "trumpet_exposed";
            }

            return "trumpet";
        }

        if (isColorVariant(path, "wool")) {
            return "guitar";
        }

        if (isColorVariant(path, "stained_glass") || isColorVariant(path, "stained_glass_pane")
                || HAT_BLOCKS.contains(path)) {
            return "hat";
        }

        if (isColorVariant(path, "concrete_powder") || SNARE_BLOCKS.contains(path)) {
            return "snare";
        }

        if (isColorVariant(path, "terracotta") || isColorVariant(path, "glazed_terracotta")
                || isColorVariant(path, "concrete") || BASE_DRUM_BLOCKS.contains(path)) {
            return "basedrum";
        }

        if (isColorVariant(path, "banner") || isColorVariant(path, "wall_banner") || BASS_BLOCKS.contains(path)) {
            return "bass";
        }

        return switch (path) {
            case "gold_block" -> "bell";
            case "iron_block" -> "iron_xylophone";
            case "clay" -> "flute";
            case "soul_sand" -> "cow_bell";
            case "glowstone" -> "pling";
            case "pumpkin" -> "didgeridoo";
            case "emerald_block" -> "bit";
            case "hay_block" -> "banjo";
            case "packed_ice" -> "chime";
            case "bone_block" -> "xylophone";
            case "skeleton_skull", "skeleton_wall_skull" -> "skeleton";
            case "wither_skeleton_skull", "wither_skeleton_wall_skull" -> "wither_skeleton";
            case "zombie_head", "zombie_wall_head" -> "zombie";
            case "player_head", "player_wall_head" -> "custom_head";
            case "creeper_head", "creeper_wall_head" -> "creeper";
            case "dragon_head", "dragon_wall_head" -> "dragon";
            case "piglin_head", "piglin_wall_head" -> "piglin";
            default -> "harp";
        };
    }

    static boolean isHeadInstrument(String instrument) {
        return switch (instrument) {
            case "skeleton", "wither_skeleton", "zombie", "custom_head", "creeper", "dragon", "piglin" -> true;
            default -> false;
        };
    }

    private static boolean isColorVariant(String path, String suffix) {
        for (var color : COLORS) {
            if (path.equals(color + "_" + suffix)) {
                return true;
            }
        }

        return false;
    }

    private static Set<String> blocks(String names) {
        return Set.of(names.split("\\s+"));
    }
}

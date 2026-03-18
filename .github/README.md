# Placement

A library for Minestom providing vanilla-like block placement mechanics. This library implements complex placement logic for various blocks, ensuring they behave correctly when placed in the world (e.g., correct rotation, connecting to neighbors, multiblock structures).

## Features

- **Comprehensive Rule Set**: Includes `BlockPlacementRule` implementations for a wide variety of vanilla Minecraft blocks.
- **Easy Registration**: Provides a `Utility` class to simplify registering rules with your Minestom server.
- **Tag Support**: Automatically register rules for entire categories of blocks (e.g., all wooden doors) using Minecraft tags.

### Supported Blocks
The library includes placement rules for:
- **Structure**: Stairs, Slabs, Walls, Fences, Glass Panes, Doors, Trapdoors, Fence Gates
- **Furniture**: Beds, Chests, Banners
- **Nature**: Crops, Flowers, Mushrooms, Sugar Cane, Cactus, Bushes, Bamboo
- **Redstone & Mechanics**: Buttons, Rails
- **Signs**: Standing, Wall, and Hanging signs
- **Other**: Axis-aligned blocks (Logs, Pillars), Dummy blocks

## Requirements

- **Java**: 25 or higher

## Installation

This library is available on Maven Central.

### Gradle (Kotlin DSL)

```kotlin
implementation("rocks.minestom:placement:0.1.0")
```

### Gradle (Groovy DSL)

```groovy
implementation 'rocks.minestom:placement:0.1.0'
```

### Maven

```xml
<dependency>
    <groupId>rocks.minestom</groupId>
    <artifactId>placement</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage

Minestom requires you to explicitly register a `BlockPlacementRule` for any block that needs custom placement logic. This library provides these rules and a helper utility to register them.

### Quick Start

To use the library, register the desired placement rules during your server initialization. The `Utility` class makes it easy to register rules either by block tag (using the rule's `KEY` constant) or by specific blocks.

#### Java Example

```java
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import rocks.minestom.placement.*;

public class MyServer {
    public void registerPlacementRules() {
        Utility.registerPlacementRules(
                AxisPlacementRule::new,
                Block.CREAKING_HEART,
                Block.HAY_BLOCK,
                Block.CHAIN,
                Block.DEEPSLATE,
                Block.INFESTED_DEEPSLATE,
                Block.MUDDY_MANGROVE_ROOTS,
                Block.BAMBOO_BLOCK,
                Block.STRIPPED_BAMBOO_BLOCK,
                Block.BASALT,
                Block.POLISHED_BASALT,
                Block.QUARTZ_PILLAR,
                Block.PURPUR_PILLAR,
                Block.BONE_BLOCK,
                Block.OCHRE_FROGLIGHT,
                Block.VERDANT_FROGLIGHT,
                Block.PEARLESCENT_FROGLIGHT);

        Utility.registerPlacementRules(AxisPlacementRule::new, Key.key("minecraft:logs"));
        Utility.registerPlacementRules(StairPlacementRule::new, StairPlacementRule.KEY);
        Utility.registerPlacementRules(SlabPlacementRule::new, SlabPlacementRule.KEY);
        Utility.registerPlacementRules(FencePlacementRule::new, FencePlacementRule.KEY);
        Utility.registerPlacementRules(FenceGatePlacementRule::new, FenceGatePlacementRule.KEY);
        Utility.registerPlacementRules(WallPlacementRule::new, WallPlacementRule.KEY);
        Utility.registerPlacementRules(GlassPanePlacementRule::new,
                Block.GLASS_PANE,
                Block.IRON_BARS,
                Block.WHITE_STAINED_GLASS_PANE,
                Block.ORANGE_STAINED_GLASS_PANE,
                Block.MAGENTA_STAINED_GLASS_PANE,
                Block.LIGHT_BLUE_STAINED_GLASS_PANE,
                Block.YELLOW_STAINED_GLASS_PANE,
                Block.LIME_STAINED_GLASS_PANE,
                Block.PINK_STAINED_GLASS_PANE,
                Block.GRAY_STAINED_GLASS_PANE,
                Block.LIGHT_GRAY_STAINED_GLASS_PANE,
                Block.CYAN_STAINED_GLASS_PANE,
                Block.PURPLE_STAINED_GLASS_PANE,
                Block.BLUE_STAINED_GLASS_PANE,
                Block.BROWN_STAINED_GLASS_PANE,
                Block.GREEN_STAINED_GLASS_PANE,
                Block.RED_STAINED_GLASS_PANE,
                Block.BLACK_STAINED_GLASS_PANE);

        Utility.registerPlacementRules(DoorPlacementRule::new, DoorPlacementRule.KEY);
        Utility.registerPlacementRules(BedPlacementRule::new, BedPlacementRule.KEY);
        Utility.registerPlacementRules(ButtonPlacementRule::new, ButtonPlacementRule.KEY);
        Utility.registerPlacementRules(TrapdoorPlacementRule::new, TrapdoorPlacementRule.KEY);
        Utility.registerPlacementRules(StandingSignPlacementRule::new, StandingSignPlacementRule.KEY);
        Utility.registerPlacementRules(WallSignPlacementRule::new, WallSignPlacementRule.KEY);
        Utility.registerPlacementRules(CeilingHangingSignPlacementRule::new, CeilingHangingSignPlacementRule.KEY);
        Utility.registerPlacementRules(WallHangingSignPlacementRule::new, WallHangingSignPlacementRule.KEY);
        Utility.registerPlacementRules(BannerPlacementRule::new, BannerPlacementRule.KEY);
        Utility.registerPlacementRules(HorizontalFacingPlacementRule::new, 
                Block.FURNACE, Block.BLAST_FURNACE, Block.SMOKER, Block.STONECUTTER);
        Utility.registerPlacementRules(ChestPlacementRule::new, Block.CHEST);
        Utility.registerPlacementRules(PlantPlacementRule::new, PlantPlacementRule.KEY);
        Utility.registerPlacementRules(PlantPlacementRule::new, Key.key("minecraft:saplings"));
        Utility.registerPlacementRules(CropPlacementRule::new, CropPlacementRule.KEY);
        Utility.registerPlacementRules(TallPlantPlacementRule::new,
                Block.SUNFLOWER,
                Block.LILAC,
                Block.PEONY,
                Block.ROSE_BUSH,
                Block.TALL_GRASS,
                Block.LARGE_FERN,
                Block.TALL_SEAGRASS,
                Block.PITCHER_PLANT);

        Utility.registerPlacementRules(MushroomPlacementRule::new, Block.BROWN_MUSHROOM, Block.RED_MUSHROOM);
        Utility.registerPlacementRules(SugarCanePlacementRule::new, Block.SUGAR_CANE);
        Utility.registerPlacementRules(CactusPlacementRule::new, Block.CACTUS);
        Utility.registerPlacementRules(CactusFlowerPlacementRule::new, Block.CACTUS_FLOWER);
        Utility.registerPlacementRules(RailPlacementRule::new, RailPlacementRule.KEY);
    }
}
```

#### Kotlin Example

```kotlin
import net.kyori.adventure.key.Key
import net.minestom.server.instance.block.Block
import rocks.minestom.placement.*

fun registerPlacementRules() {
    Utility.registerPlacementRules(::AxisPlacementRule,
        Block.CREAKING_HEART,
        Block.HAY_BLOCK,
        Block.CHAIN,
        Block.DEEPSLATE,
        Block.INFESTED_DEEPSLATE,
        Block.MUDDY_MANGROVE_ROOTS,
        Block.BAMBOO_BLOCK,
        Block.STRIPPED_BAMBOO_BLOCK,
        Block.BASALT,
        Block.POLISHED_BASALT,
        Block.QUARTZ_PILLAR,
        Block.PURPUR_PILLAR,
        Block.BONE_BLOCK,
        Block.OCHRE_FROGLIGHT,
        Block.VERDANT_FROGLIGHT,
        Block.PEARLESCENT_FROGLIGHT
    )

    Utility.registerPlacementRules(::AxisPlacementRule, Key.key("minecraft:logs"))
    Utility.registerPlacementRules(::StairPlacementRule, StairPlacementRule.KEY)
    Utility.registerPlacementRules(::SlabPlacementRule, SlabPlacementRule.KEY)
    Utility.registerPlacementRules(::FencePlacementRule, FencePlacementRule.KEY)
    Utility.registerPlacementRules(::FenceGatePlacementRule, FenceGatePlacementRule.KEY)
    Utility.registerPlacementRules(::WallPlacementRule, WallPlacementRule.KEY)
    Utility.registerPlacementRules(::GlassPanePlacementRule,
        Block.GLASS_PANE,
        Block.IRON_BARS,
        Block.WHITE_STAINED_GLASS_PANE,
        Block.ORANGE_STAINED_GLASS_PANE,
        Block.MAGENTA_STAINED_GLASS_PANE,
        Block.LIGHT_BLUE_STAINED_GLASS_PANE,
        Block.YELLOW_STAINED_GLASS_PANE,
        Block.LIME_STAINED_GLASS_PANE,
        Block.PINK_STAINED_GLASS_PANE,
        Block.GRAY_STAINED_GLASS_PANE,
        Block.LIGHT_GRAY_STAINED_GLASS_PANE,
        Block.CYAN_STAINED_GLASS_PANE,
        Block.PURPLE_STAINED_GLASS_PANE,
        Block.BLUE_STAINED_GLASS_PANE,
        Block.BROWN_STAINED_GLASS_PANE,
        Block.GREEN_STAINED_GLASS_PANE,
        Block.RED_STAINED_GLASS_PANE,
        Block.BLACK_STAINED_GLASS_PANE
    )

    Utility.registerPlacementRules(::DoorPlacementRule, DoorPlacementRule.KEY)
    Utility.registerPlacementRules(::BedPlacementRule, BedPlacementRule.KEY)
    Utility.registerPlacementRules(::ButtonPlacementRule, ButtonPlacementRule.KEY)
    Utility.registerPlacementRules(::TrapdoorPlacementRule, TrapdoorPlacementRule.KEY)
    Utility.registerPlacementRules(::StandingSignPlacementRule, StandingSignPlacementRule.KEY)
    Utility.registerPlacementRules(::WallSignPlacementRule, WallSignPlacementRule.KEY)
    Utility.registerPlacementRules(::CeilingHangingSignPlacementRule, CeilingHangingSignPlacementRule.KEY)
    Utility.registerPlacementRules(::WallHangingSignPlacementRule, WallHangingSignPlacementRule.KEY)
    Utility.registerPlacementRules(::BannerPlacementRule, BannerPlacementRule.KEY)
    Utility.registerPlacementRules(::HorizontalFacingPlacementRule, 
        Block.FURNACE, Block.BLAST_FURNACE, Block.SMOKER, Block.STONECUTTER)
    Utility.registerPlacementRules(::ChestPlacementRule, Block.CHEST)
    Utility.registerPlacementRules(::PlantPlacementRule, PlantPlacementRule.KEY)
    Utility.registerPlacementRules(::PlantPlacementRule, Key.key("minecraft:saplings"))
    Utility.registerPlacementRules(::CropPlacementRule, CropPlacementRule.KEY)
    Utility.registerPlacementRules(::TallPlantPlacementRule,
        Block.SUNFLOWER,
        Block.LILAC,
        Block.PEONY,
        Block.ROSE_BUSH,
        Block.TALL_GRASS,
        Block.LARGE_FERN,
        Block.TALL_SEAGRASS,
        Block.PITCHER_PLANT
    )

    Utility.registerPlacementRules(::MushroomPlacementRule, Block.BROWN_MUSHROOM, Block.RED_MUSHROOM)
    Utility.registerPlacementRules(::SugarCanePlacementRule, Block.SUGAR_CANE)
    Utility.registerPlacementRules(::CactusPlacementRule, Block.CACTUS)
    Utility.registerPlacementRules(::CactusFlowerPlacementRule, Block.CACTUS_FLOWER)
    Utility.registerPlacementRules(::RailPlacementRule, RailPlacementRule.KEY)
}
```

### Available Rules

The following placement rule classes are available in the `rocks.minestom.placement` package:

| Rule Class | Key (if available) | Description / Target |
|------------|--------------------|----------------------|
| `AxisPlacementRule` | - | Blocks with axis orientation (logs, pillars). |
| `BannerPlacementRule` | `minecraft:banners` | Standing and wall banners. |
| `BedPlacementRule` | `minecraft:beds` | Beds (handles head/foot parts). |
| `ButtonPlacementRule` | `minecraft:buttons` | Stone and wooden buttons (floor, wall, ceiling). |
| `CactusFlowerPlacementRule` | - | Placement logic for cactus flowers. |
| `CactusPlacementRule` | - | Cactus placement validation (sand/red sand). |
| `CeilingHangingSignPlacementRule` | `minecraft:ceiling_hanging_signs` | Ceiling hanging signs rotation. |
| `ChestPlacementRule` | - | Single and double chests connections. |
| `CropPlacementRule` | `minecraft:crops` | Crops validation (farmland). |
| `DoorPlacementRule` | `minecraft:doors` | Doors (handles top/bottom halves, hinge). |
| `DummyPlacementRule` | - | No-op rule for blocks needing no logic. |
| `FenceGatePlacementRule` | `minecraft:fence_gates` | Fence gates (open/close, in-wall). |
| `FencePlacementRule` | `minecraft:fences` | Fences and connections. |
| `GlassPanePlacementRule` | - | Glass panes and connections. |
| `HorizontalFacingPlacementRule` | - | Generic horizontal rotation (furnaces, etc.). |
| `MushroomPlacementRule` | - | Mushroom placement validation. |
| `PlantPlacementRule` | `minecraft:small_flowers` | Small flowers/plants validation. |
| `RailPlacementRule` | `minecraft:rails` | Rails (connections, slopes). |
| `SlabPlacementRule` | `minecraft:slabs` | Slabs (bottom, top, double). |
| `StairPlacementRule` | `minecraft:stairs` | Stairs (rotation, shape). |
| `StandingSignPlacementRule` | `minecraft:standing_signs` | Standing signs (16-way rotation). |
| `SugarCanePlacementRule` | - | Sugar cane validation (water proximity). |
| `TallPlantPlacementRule` | `minecraft:tall_flowers` | Double-tall plants (sunflower, etc.). |
| `TrapdoorPlacementRule` | `minecraft:trapdoors` | Trapdoors (open/close, halves). |
| `WallHangingSignPlacementRule` | `minecraft:wall_hanging_signs` | Wall hanging signs facing. |
| `WallPlacementRule` | `minecraft:walls` | Walls and connections. |
| `WallSignPlacementRule` | `minecraft:wall_signs` | Wall signs facing. |

Most rules expose a `public static final Key KEY` constant if they target a specific Minecraft block tag (e.g., `minecraft:stairs`, `minecraft:doors`). If a rule does not have a `KEY`, you should register it for the specific blocks it supports using the `Utility.registerPlacementRules(factory, block...)` method.

## License

This project is licensed under the Apache-2.0 License.


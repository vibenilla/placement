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
import net.minestom.server.instance.block.Block;
import rocks.minestom.placement.*;

public class MyServer {
    public void registerPlacementRules() {
        // Register rules for block tags (e.g., all stairs, all doors)
        Utility.registerPlacementRules(DoorPlacementRule::new, DoorPlacementRule.KEY);
        Utility.registerPlacementRules(BedPlacementRule::new, BedPlacementRule.KEY);
        Utility.registerPlacementRules(StairPlacementRule::new, StairPlacementRule.KEY);
        Utility.registerPlacementRules(SlabPlacementRule::new, SlabPlacementRule.KEY);
        Utility.registerPlacementRules(FencePlacementRule::new, FencePlacementRule.KEY);
        Utility.registerPlacementRules(WallPlacementRule::new, WallPlacementRule.KEY); 
        Utility.registerPlacementRules(TrapdoorPlacementRule::new, TrapdoorPlacementRule.KEY);

        // Register rules for specific blocks that don't use a tag key
        Utility.registerPlacementRules(CactusPlacementRule::new, Block.CACTUS);
        Utility.registerPlacementRules(ChestPlacementRule::new, Block.CHEST, Block.TRAPPED_CHEST);
        Utility.registerPlacementRules(SugarCanePlacementRule::new, Block.SUGAR_CANE);
    }
}
```

#### Kotlin Example

```kotlin
import net.minestom.server.instance.block.Block
import rocks.minestom.placement.*

fun registerPlacementRules() {
    // Register rules for block tags
    Utility.registerPlacementRules(::DoorPlacementRule, DoorPlacementRule.KEY)
    Utility.registerPlacementRules(::BedPlacementRule, BedPlacementRule.KEY)
    Utility.registerPlacementRules(::StairPlacementRule, StairPlacementRule.KEY)
    Utility.registerPlacementRules(::SlabPlacementRule, SlabPlacementRule.KEY)
    Utility.registerPlacementRules(::FencePlacementRule, FencePlacementRule.KEY)
    Utility.registerPlacementRules(::WallPlacementRule, WallPlacementRule.KEY)
    Utility.registerPlacementRules(::TrapdoorPlacementRule, TrapdoorPlacementRule.KEY)

    // Register rules for specific blocks
    Utility.registerPlacementRules(::CactusPlacementRule, Block.CACTUS)
    Utility.registerPlacementRules(::ChestPlacementRule, Block.CHEST, Block.TRAPPED_CHEST)
    Utility.registerPlacementRules(::SugarCanePlacementRule, Block.SUGAR_CANE)
}
```

### Available Rules

The following placement rule classes are available in the `rocks.minestom.placement` package:

| Rule Class | Description / Target |
|------------|----------------------|
| `AxisPlacementRule` | Blocks with axis orientation (logs, pillars) |
| `BannerPlacementRule` | Standing and wall banners |
| `BedPlacementRule` | Beds (handles head/foot parts) |
| `ButtonPlacementRule` | Stone and wooden buttons |
| `CactusPlacementRule` | Cactus validation logic |
| `ChestPlacementRule` | Single and double chests |
| `DoorPlacementRule` | Doors (handles top/bottom halves) |
| `FencePlacementRule` | Fences and connections |
| `RailPlacementRule` | Rails and minecart tracks |
| `SlabPlacementRule` | Slabs (bottom, top, double) |
| `StairPlacementRule` | Stairs (rotation and shape) |
| `TrapdoorPlacementRule` | Trapdoors (open/close, halves) |
| `WallPlacementRule` | Walls and connections |
| ... and many more. | |

Most rules expose a `public static final Key KEY` constant if they target a specific Minecraft block tag (e.g., `minecraft:stairs`, `minecraft:doors`). If a rule does not have a `KEY`, you should register it for the specific blocks it supports using the `Utility.registerPlacementRules(factory, block...)` method.

## License

This project is licensed under the Apache-2.0 License.


# placement
A library for Minestom providing vanilla-like block placement mechanics.

## Installation

<details>
<summary>Gradle (Kotlin)</summary>
<br>

```kts
dependencies {
    implementation("rocks.minestom:placement:0.2.1")
}
```

</details>

<details>
<summary>Gradle (Groovy)</summary>
<br>

```groovy
dependencies {
    implementation 'rocks.minestom:placement:0.2.1'
}
```

</details>

<details>
<summary>Maven</summary>
<br>

```xml
<dependency>
    <groupId>rocks.minestom</groupId>
    <artifactId>placement</artifactId>
    <version>0.2.1</version>
</dependency>
```

</details>

## Usage
```java
// Vanilla
Registrations.registerAllVanilla(MinecraftServer.getBlockManager());

// More convenient ruleset for creative mode
Registrations.registerAllBuilderMode(MinecraftServer.getBlockManager());
```

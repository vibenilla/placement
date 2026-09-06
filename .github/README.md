# placement
A library for Minestom providing vanilla-like block placement mechanics.

## Installation

<details>
<summary>Gradle (Kotlin)</summary>
<br>

```kts [Gradle (Kotlin)]
repositories {
    maven("https://maven.skylite.gg/releases")
}

dependencies {
    implementation("rocks.minestom:placement:2026.09.04-26.2")
}
```

</details>

<details>
<summary>Gradle (Groovy)</summary>
<br>

```groovy [Gradle (Groovy)]
repositories {
    maven {
        url 'https://maven.skylite.gg/releases'
    }
}

dependencies {
    implementation 'rocks.minestom:placement:2026.09.04-26.2'
}
```

</details>

<details>
<summary>Maven</summary>
<br>

```xml [Maven]
<repositories>
    <repository>
        <id>skylite</id>
        <url>https://maven.skylite.gg/releases</url>
    </repository>
</repositories>

<dependency>
    <groupId>rocks.minestom</groupId>
    <artifactId>placement</artifactId>
    <version>2026.09.04-26.2</version>
</dependency>
```

</details>

## Usage
```java
Registrations.registerAllVanilla(MinecraftServer.getBlockManager());
```

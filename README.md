# :bagel: Dough

<hr />
<p align="center">
  <a href="https://github.com/rutwok-labs/dough-core/actions/workflows/maven.yml">
    <img alt="Build Status" src="https://github.com/rutwok-labs/dough-core/actions/workflows/maven.yml/badge.svg">
  </a>
    <a href="https://javadoc.io/doc/io.github.baked-libs/dough-api">
	<img alt="javadocs" src="https://javadoc.io/badge2/io.github.baked-libs/dough-api/javadoc.svg" />
    </a>
  <a href="https://jitpack.io/#rutwok-labs/dough-core">
    <img alt="JitPack" src="https://jitpack.io/v/rutwok-labs/dough-core.svg">
  </a>
	
</p>
<hr />
.
Formerly known as "cs-corelib2", dough is a very powerful library aiming to help the everyday Spigot/Plugin developer.
It is packed to the brim with useful features and APIs to use and play around with.

Dough may be more commonly known as the backbone of [Slimefun](https://github.com/Slimefun/Slimefun4).

## :sparkles: Current Update

This fork keeps Dough compatible with modern Slimefun-based servers while preserving the original API surface.
Existing callers should continue to compile and behave the same, with new helpers added around the existing modules.

### Recent changes

- Updated the project target to Java 17 and Paper API `1.21.1-R0.1-SNAPSHOT`.
- Added Paper API fast paths for modern item names and player heads where available.
- Improved JitPack readiness for the multi-module build.
- Fixed inventory empty-slot detection, GitHub updater JSON parsing, and BlockPosition equality with cleared world references.
- Added helper APIs across chat, config, common, data, inventories, items, scheduling, skins, updater, and protection modules.
- Added compatibility helpers for future Paper versions while keeping Spigot NMS fallback behavior.

## :white_check_mark: Supported versions

| Minecraft version | Paper support | Spigot support |
| --- | --- | --- |
| `1.19.4` | Full | Full |
| `1.20` - `1.20.4` | Full | Full |
| `1.20.5` - `1.20.8` | Full | NMS reflection fallback |
| `1.21.1` - `1.21.11` | Full Paper API path | NMS reflection fallback |
| `26.0.1+` / future Paper | Best-effort full Paper API path | Best-effort NMS reflection fallback |

## :toolbox: Features

- Modular Bukkit/Paper utility library for plugin developers.
- Slimefun-friendly APIs for items, skins, config, data, recipes, scheduling, protection hooks, inventories, chat input, and updater flows.
- Paper-aware compatibility paths for newer server versions.
- Backward-compatible helpers: new overloads and utilities were added without removing existing public methods.
- JitPack and Maven-friendly multi-module build layout.

## :busts_in_silhouette: Authors

Previous authors:

- TheBusyBiscuit
- WalshyDev
- md5sha256

Modified and developed by:

- RutwokLabs

## :mag: Getting Started
Dough is hosted on maven-central (OSS Sonatype) for easy access.
Furthermore, it consists of multiple different submodules.

If you want to utilise the entirety of dough, use the artifact `dough-api`.<br>
Otherwise replace `dough-api` in the following examples with whatever module you want to import. Note that
some modules have dependencies on other modules, all modules require `dough-common` as an example.

### Adding dough via gradle
Dough can easily be included in gradle using mavenCentral.<br />
Simply replace `[DOUGH VERSION]` with the most up to date version of dough:
![Maven Central](https://img.shields.io/maven-central/v/io.github.baked-libs/dough?label=latest%20version)

```gradle
repositories {
	mavenCentral()
}

dependencies {
	implementation 'io.github.baked-libs:dough-api:[DOUGH VERSION]'
}
```

To shadow dough and relocate it:
```gradle
plugins {
  id "com.github.johnrengelman.shadow" version "7.0.0"
}

shadowJar {
   relocate "io.github.bakedlibs.dough", "[YOUR PACKAGE].dough"
}
```

### Adding dough via JitPack

This build also includes a `jitpack.yml` for Java 17 multi-module publishing.

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.[OWNER]:[REPOSITORY]:[TAG]'
}
```

### Adding dough via Maven
Dough can easily be included be added using maven-central.<br />
Simply replace `[DOUGH VERSION]` with the most up to date version of dough:
![Maven Central](https://img.shields.io/maven-central/v/io.github.baked-libs/dough?label=latest%20version)

```xml
<dependencies>
  <dependency>
    <groupId>io.github.baked-libs</groupId>
    <artifactId>dough-api</artifactId>
    <version>[DOUGH VERSION]</version>
    <scope>compile</scope>
  </dependency>
</dependencies>
```

To shadow dough and relocate it:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.2.4</version>

      <configuration>
        <relocations>
          <relocation>
            <pattern>io.github.bakedlibs.dough</pattern>
            <shadedPattern>[YOUR PACKAGE].dough</shadedPattern>
          </relocation>
        </relocations>
      </configuration>

      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

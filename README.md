# Ancient Warfare 2 Remastered 0.2.9 → NeoForge 26.3: Stage 2

**STATUS: incomplete source project, NOT a playable full Ancient Warfare 2 mod.** Do not place this ZIP or the original Forge 1.20.1 Remastered JAR into a 26.3 `mods/` folder.

## Work completed against supplied 0.2.9 binary

- Verified the Remastered JAR metadata says Minecraft 1.20.1, Forge 47+, GeckoLib 4+ required; compiled for Java 17 (class file 61).
- Replaced 11 material models, original textures and display names with the ones **directly extracted** from the user's Remastered 0.2.9 JAR. IDs are confirmed against the current JAR.
- Kept NeoForge 26.3 registration of these 11 items and `assets/ancientwarfare/items/*.json` model definitions.
- Converted **10 of their exact Remastered crafting recipes** to modern recipe JSON with `data/ancientwarfare/recipe/`, `result.id`, string / `#` tag ingredients, and `c:ingots/iron` instead of the old Forge tag. The `steel_ingot` is normally made through a custom engineering recipe; it is **not** given an invented vanilla recipe, and the engineering station is not ported yet.
- Generated a complete Remastered class/resource name inventory, a SHA256 fingerprint, side-by-side class name differences with the original 1.12.2 JAR, and preserved Remastered mod metadata in `reports/remastered_0.2.9/`.

## Build prerequisites

- Java 25 and an internet-connected Gradle development environment.
- This is still an **overlay project**; first clone `https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle`, including Gradle wrapper, then overwrite the example files with this project. Remove all examplemod sources/resources.
- From that directory, run `./gradlew build`, `./gradlew runClient`, and `./gradlew runServer` to compile and test. Do not install to a production world before testing.
- This environment provides Java 21, so actual Java 25/NeoForge compilation and Minecraft client/server integration tests **have not been run**.

## Remaining to produce a full port

Remastered 0.2.9 still has 1,000 compiled Java class files. Nearly all gameplay logic relies on 1.20.1 Forge and 1.20-era Minecraft APIs. For a full 26.3 release, migrate classes from the corresponding **0.2.9 source** (or carefully decompile and audit the GPL-licensed JAR), including:

1. Registries and custom items/blocks/block entities; no placeholder registration for machines that do not actually work.
2. Research/engineering recipe serializers and menus, custom networking and data components.
3. NPCs, factions, behavior / routing, worker jobs, client-side renderers and GeckoLib compatibility.
4. Torque machines, farms, logistics and chunk loaders; structure templates, worldgen, protection and saved data.
5. Vehicles/siege weapons and projectiles, multiplayer sync, world persistence, interoperability and performance.
6. Comprehensive testing on Minecraft Java 26.3 with NeoForge and Java 25.

**No claim of compatibility or completeness until compile, test and gameplay verification succeed.** Original authors, remastered contributors and models remain attributed under the licensing inherited from Ancient Warfare 2. Use the original/Remastered credits and GPL requirements for downstream releases.

Sources: user-supplied 0.2.9 and 1.12.2 JARs; official NeoForge 26.3 MDK; original GPL-3.0-or-later authorship data.

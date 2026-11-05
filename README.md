# Ancient Warfare 2 — Minecraft 26.3 NeoForge port

**Work in progress. Not a full port. No client/server gameplay testing has been performed.**

## This milestone (Stage 3)

- The 11 material items and 10 crafting recipes from Stage 2 remain.
- Added 20 Ancient Warfare melee weapons: wooden, stone, iron, golden, and diamond spears, halberds, lances, and cleavers. They use modern vanilla melee durability/attack mechanics through NeoForge's 26.3 `ToolMaterial` and `Item.Properties.sword` configuration. **Legacy extra reach and special attack effects are not yet restored.**
- Copied corresponding original 0.2.9 textures and item models. Kept existing resource paths.
- Added 15 original shaped crafting patterns (spear/halberd/lance) using modern 26.3 recipe data. Cleavers are registered but **creative-only** until their original engineering-station crafting mechanics are ported.
- Preserved all 1,484 Remastered 0.2.9 Ancient Warfare `.aws` structure templates in the correct resource tree.
- Added `AwsTemplateParser.java`, which decodes the original compressed block-rule matrix, duplicate layers and raw metadata while checking input bounds. The standalone Java regression suite parsed **all 1,484** `.aws` templates successfully, including 147,067 rule definitions and 7,479 entity entries.
- Added validation for the new resources and parser to `scripts/validate_stage2.py` (the workflow's existing entry point).

## What is NOT yet ported

There are no functioning Ancient Warfare NPCs, combat orders, faction AI, siege engines, production machines, warehouse network, structure placement/world generation, or research mechanics. The AWS files are data only: they still need modern ID remapping, proper tile/entity rule conversion, and actual world-placement code.

The port uses the GPL-licensed Ancient Warfare 2 and the Remastered 0.2.9 release as source/reference material. Original and Remastered JARs are *not* in this repository.

## GitHub build

The existing `.github/workflows/import.yml` workflow imports an uploaded archive named `ancientwarfare_26_3_github_upload.zip`, commits the extracted source, runs the validation suite, and attempts a Gradle build using JDK 25. A green build means only that the source compiles and structural tests passed; a green build does not certify client/server gameplay.

To test manually, place the successful artifact JAR in a **Minecraft 26.3 NeoForge client** and inspect items / crafting; always back up worlds before testing. Do not use on production servers yet.

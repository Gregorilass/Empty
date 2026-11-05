# Ancient Warfare 2 — Minecraft 26.3 NeoForge development port

**Stage 4 source. Not the complete Ancient Warfare 2 mod. Gameplay testing remains necessary.**

## Implemented so far
- Stage 2: 11 material items + 10 recipes.
- Stage 3: 20 melee weapons and validated parser for 1,484 `.aws` structure templates (placement not implemented).
- Stage 4: `ancientwarfare:worker` and `ancientwarfare:soldier` NPCs, spawn eggs, follow/guard orders, player ownership, worker crop harvest/replant, soldier monster attacks and save/reload state. Initial rendering uses vanilla Villager textures/models.

## Build
Java 25 + NeoForge 26.3; `gradle --no-daemon build`. The GitHub Actions import workflow is set separately in the repository.

## In-game tests
`/summon ancientwarfare:worker ~ ~ ~` and `/summon ancientwarfare:soldier ~ ~ ~` or use the spawn eggs in the Ancient Warfare creative tab. Right-click to recruit, then right-click to toggle follow/guard. A guarding worker harvests crops nearby; a soldier engages nearby hostile monsters. See `reports/STAGE4_LIMITATIONS.md` for the complete test checklist.

**Source compilation is required, and a compiled JAR must be tested in Minecraft before release.**

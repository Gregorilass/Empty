# Ancient Warfare 2 — NeoForge Minecraft 26.3 (Stage 5+6)

**Partial port. NOT a complete or gameplay-tested Ancient Warfare 2.**

This update is based on Stage 4, and combines two development milestones:

## Stage 5: Manual structure placement (limited)

- Preserves all **1,484 original `.aws` templates** in the mod JAR.
- Provides a real in-game **operator-only** `/awstructure` command with list, inspect and place.
- Uses a searchable template index and the existing fully tested AWS compressed-template decoder.
- Maps selected 1.12-era **vanilla blocks** and their states to Minecraft 26.3, including common stone, wood, glass, terracotta and doors.
- **Refuses unsafe/unsupported templates before placing anything** (special tile entities, inventories, entities, unrecognized blocks). Does not pretend full structures or their NBT are already implemented.
- Limit: 8,192 template cells per command; the destination must be loaded and in world height range. No world-generation integration yet.
- Placement origin: two blocks along X and Z from the executing player's position. Always try this in a **backup/testing world**. The command replaces blocks; undo is not implemented.

Examples (must be OP/commands enabled):

```text
/awstructure list FillCobble
/awstructure inspect FillCobble1X16X16
/awstructure place FillCobble1X16X16
```

Only small **vanilla-only** designs are placeable initially. Other structures can be listed and inspected, including a report of missing legacy functionality.

## Stage 6: Farm worksite (limited)

- Adds a real placeable **Farm Worksite** block with textures from Remastered 0.2.9.
- Workers assigned to **guard** near a farm worksite automatically farm within four blocks of it. Place one near mature crops, spawn a worker, recruit it with right click, and toggle it into guard/work mode.
- Workers replant the same crop and drop harvested items in the world. **No warehouse or inventory transfer yet**, and no natural structures, advanced AI or game rules integrations.

## Testing & build

- `scripts/validate_stage4.py` invokes `scripts/validate_stage56.py`, so the existing GitHub Actions workflow tests all stages.
- `scripts/validate_stage56.py` checks resource integrity, the structure index, all 1,484 templates, and a Java regression test for legacy block conversions.
- `gradle build` must be run with **Java 25** and NeoForge **26.3**; source has not yet passed this build in the current environment.

**Not implemented:** worldgen, NPC original models/inventory/factions, original research system, quarry, warehouse/automation networks, siege engines, legacy NBT/block-entity migration, automatic large structure building and full gameplay verification.

The project retains the upstream authors' attribution and its GPL license where applicable. No original or Remastered JAR binaries are committed to the project.

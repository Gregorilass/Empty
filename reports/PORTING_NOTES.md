# Verified inventory and migration blockers

Uploaded version: **1.12.2-2.7.0.1038**. SHA-256 `8d1a080c33537bbe8b9585fe67a0b52efa8879c4210972082dd52995d9de90ca`.

- Compiled classes: **2,182**, including code for NPCs, automation, vehicles and structures.
- `.aws` structures: **1,484**. These are Ancient Warfare's own template files; they are not vanilla structure NBT and require parser/registry migration.
- Original assets: `skin_pack` 3,607, `template` 1,495, `textures` 716, `sounds` 364, `research_recipes` 282, `models` 211, `loot_tables` 177, `registry` 155, `blockstates` 71, `recipes` 40, `lang` 8, `trades` 1.

Current reference branch publicly located: `hailam0314-cell/ancientwarfare-0.1.0-porting-source` (1.20.1, Forge). **Not** evidence of full Remastered v0.2.9 source.

Minecraft 26.3 requires Java 25; NeoForge 26.3.0.101 and GeckoLib 5.5.7 exist. The old AW2 binary cannot run on these without a recompiled source port.

## Still blocked / unverified

- Remastered v0.2.9 latest corresponding code not available in this workspace.
- This environment has JDK 21, no NeoForge libraries, no Gradle wrapper, no outbound runtime dependency downloads.
- No functional build or gameplay testing completed.
- Old and modern registry IDs / codecs not compared exhaustively; no migration guarantee.

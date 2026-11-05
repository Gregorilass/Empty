# Stage 4 — NPC implementation notes

Scope: manually spawnable worker and soldier entity types; vanilla villager rendering; owner is set on first right click; owner right click toggles between following and guarding; soldier attacks nearby Monster-type mobs; worker harvests mature CropBlock crops within 4 horizontal blocks of its guard point, replants automatically, and drops produce on ground. Owner, mode, and guard coordinates persist to world saves.

**Not included:** original Ancient Warfare NPC inventory, item picking, paid recruitment, courier routes, advanced jobs, factions, worker resource delivery, precise combat orders, NPC animations/skins from Remastered, natural spawning, siege crew, or complete safety/performance testing. Crop planting does not currently consume seed and drops are not collected.

The game client currently renders both entity types as vanilla villagers. Never claim this is a full NPC restoration.

## Manual test after CI compilation
1. Install the mod on a NeoForge 26.3 client. Test in a fresh throwaway world, not an important save.
2. `/summon ancientwarfare:worker ~ ~ ~`; verify NPC appears. Right click to recruit; right click again to assign guard/farming area.
3. Place fully grown wheat within 4 blocks. Verify harvesting, drops, and replanting occur at 20-tick cadence.
4. `/summon ancientwarfare:soldier ~ ~ ~`; recruit and guard, spawn zombie nearby; verify navigation and damage.
5. Test follow, guard, save/reload, multiplayer ownership, client rendering and dedicated server startup.

Build success alone is not evidence that these interactions have passed.

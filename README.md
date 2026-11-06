# Ancient Warfare 2 — NeoForge Minecraft 26.3 — Stages 9 + 10

**Mod version:** `0.0.10-stage10`  
**Minecraft:** `26.3`  
**NeoForge:** `26.3.0.101`  
**Java:** 25 (GitHub compiler)  
**Current state:** Offline source tests and Java syntax parsing pass. **A GitHub Gradle compile and in-game tests are still required.**

This update preserves Stage 8 workers, guards, couriers, farm worksites, controller/storage inventories, structure placement and all **1,484 legacy `.aws` structures**. Existing registry IDs and saved NPC assignment/inventory keys have not been renamed. The primary reference remains original Ancient Warfare 2 **1.12.2 v2.7.0.1038**, not the incomplete 1.20 remaster.

## Stage 9 — Tree Farm + Lumberjack

- New craftable **Tree Farm Worksite** with **27 persistent inventory slots**, using textures restored from the original Ancient Warfare 2 1.12.2 JAR.
- New **Lumberjack NPC spawn egg** and recruitable NPC with standard follow/home/work controls, 27 cargo slots, saved ownership and worksite assignment.
- Lumberjacks path to nearby eligible **single-column trees** and cut their trunk, depositing logs in the assigned Tree Farm Worksite, then personal storage, then dropping only overflow to the ground.
- Tree harvest is restricted to **oak, birch, spruce, jungle, acacia and cherry logs**, growing on dirt/grass and with a real leaf canopy, within the assigned worksite's configured radius. There is a cooldown to prevent rapid repeated harvesting. No forced chunk loading.
- **Replanting consumes one matching sapling** from the Tree Farm Worksite inventory; no free saplings are created. Leave appropriate saplings in the worksite to enable replanting.
- Couriers can use **either Farm Worksite or Tree Farm Worksite** as their pickup source, and leave saplings in tree-worksite inventory for replanting.

### Test a Lumberjack

1. On your **test world**, place Tree Farm Worksite close to naturally grown **1x1 oak or birch trees** with leaves above and soil below. Begin with a tree 3–8 blocks tall and within 4 blocks of the worksite.
2. Open the Tree Farm inventory and place several **matching saplings** in it (e.g. oak saplings for an oak tree).
3. Spawn a Lumberjack. Right-click once to recruit; right-click again for its six-row NPC menu.
4. Stand within **10 blocks** of the tree worksite and within **8 blocks of the Lumberjack**. Click top-row **slot 3 / fourth icon** (**Assign tree farm**), then **slot 1 / second icon** (**Cut trees**). If needed, use top-row range +/- icons and tree-cutting toggle.
5. Observe walking, harvesting, logs entering the Tree Farm's 27-slot inventory, and **one matching sapling** being consumed and planted. Watch leaves decay normally. If the inventory and NPC cargo fill, the remaining logs should drop on the ground.
6. Spawn and recruit a Courier. Assign the Tree Farm worksite from near that block, then a Warehouse Controller while standing near it. Activate Deliver. The Courier should move wood into your existing warehouse; saplings should remain reserved at the tree farm.

## Stage 10 — Warehouse Stock Viewer

- New craftable **Warehouse Stock Viewer** block, using original 1.12.2 artwork.
- Place it within **12 blocks horizontally and two blocks vertically** of a Warehouse Controller, then right-click to open a vanilla **six-row overview menu**.
- The first five rows show **up to 45 distinct stored item variants per page**; the bottom row contains previous, refresh/status and next buttons. The overview scans the **controller plus loaded Warehouse Storage blocks** within the existing Stage 8 range (8 horizontal, +/-2 vertical blocks).
- Identical item stacks are counted together; different item components stay separate in the stock listing. Navigation and item displays are strictly **read-only** to prevent theft or duplication through virtual item slots.
- **The Controller and Storage blocks still open their normal 54-slot inventories** for item transfers, exactly as in Stage 8. The Stock Viewer is *not* an additional storage inventory.
- No custom UI protocol or new packet registration; the existing vanilla chest menu is reused for proxy compatibility.

### Test the Stock Viewer

1. Place the Stock Viewer close to the existing controller, with some different items in the controller and nearby storage.
2. Right-click the Viewer. Check item names, total quantities and pagination using the bottom-row buttons. Right-click the status/compass button to refresh after changing storage contents.
3. Try to take or shift-click items out of the Viewer: **no virtual items should be obtainable**. Retrieve actual items normally by opening Controller/Storage inventories.
4. Restart the server. Confirm original warehouse inventories, NPC settings, worksite inventories and Viewer are still present. Recheck stock quantities and courier deliveries.

## Build and validation

```bash
python3 scripts/validate_stage910.py
gradle --no-daemon --stacktrace build
```

On GitHub Actions, Gradle `check` automatically runs the offline validator, which includes inherited Stage 7/8 checks, the structure manifest, all JSON assets, and new integration points. This does **not** replace Java 25 / NeoForge compilation or gameplay tests. Prior workflow configuration in the GitHub repo is deliberately not changed by this ZIP; read `GITHUB_SETUP.md`.

## Known limitations

- Only conservative 1x1 tree trunks are processed; **2x2 giant trees, dark oak, mangrove, Nether fungi and extensive branches are not supported**. Leaves may decay and drop saplings on the ground (the Lumberjack doesn't collect free-floating items).
- Tree cutting is an initial gameplay implementation, not the full 1.12.2 tree scanner or forestry research module. Use the test world until gameplay checks finish.
- The stock viewer is view-only: original stock filters, item withdrawal screen, auto-crafting, stock linkers, research requirements and advanced permission settings remain future work.
- Existing Stage 8 crop farms still replant without consuming seeds; they are unchanged by this release.
- No forced chunk loading, cross-dimensional courier transfers or server-wide global warehouse indexing.
- Multiplayer and dedicated server gameplay validation must follow a successful GitHub build.

See `reports/STAGE9_10_LIMITATIONS.md` for scope and regression checklist. Legacy Stage 8 README is saved in `reports/README_STAGE8_ARCHIVED.md`.

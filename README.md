# Ancient Warfare 2 — NeoForge Minecraft 26.3 (Stage 7 NPC Controls)

**Partial port, source build candidate. Full compile, server startup, and gameplay tests are still required.**

Stage 7 starts from the previous *working Stage 5+6 source with the equipment recipe category correction*. It preserves the original 11 materials, 20 basic melee weapons, two NPC roles, Farm Worksite, structure decoder and 1,484 `.aws` templates.

## Combined Stage 7 features

- **NPC control GUI**: first right-click recruits an unowned NPC; subsequent right-clicks by the owner open a **six-row vanilla chest-style graphical interface** (not a custom-textured screen). Top 3 rows are clickable labeled controls with descriptions; bottom 3 rows are 27 inventory slots. **The control slots cannot legitimately transfer items to/from players**; inventory slots support ordinary/shift-click transfers.
- **Soldiers**: Follow, Guard/Home, Set Guard/Home Here (owner position), Enable/Disable auto monster combat, Increase/Decrease detection radius (displayed as a task range). Soldiers defend a configurable home region instead of chasing monsters indefinitely.
- **Workers**: Follow, Work near a farm, Set Home Here, Assign nearest real Farm Worksite, Increase/Decrease farming radius (2–8 blocks), and Enable/Disable farming. Unassigned workers still auto-search for nearby worksite as in Stage 6.
- **Personal inventory**: 27 permanent storage slots per NPC, server-owned and saved with the NPC under `AWStorage`. Harvested crop items are automatically inserted into available slots; overflow is dropped in the world, not deleted. NPC inventory drops on death. No warehouse network yet.
- **Persistent settings**: Owner ID and existing follow/guard state retained across upgrades; home, assigned Farm Worksite, task radius and selected job toggles survive world reloads.
- **Access control**: Only the owner can operate the NPC GUI, with an 8-block distance limit. Commands execute server-side. Minecraft's built-in `GENERIC_9x6` menu type is used, avoiding new custom-menu packet registration through Velocity.

### GUI controls

| Slot (first row is 0–8) | Worker | Soldier |
|---|---|---|
| 0 | Follow owner | Follow owner |
| 1 | Work near assigned farm | Guard home position |
| 2 | Set home to player position | Set guard position to player position |
| 3 | Assign nearest Farm Worksite (within 10 blocks of player) | Toggle monster combat |
| 4–5 | Decrease / increase farm radius (2–8) | Decrease / increase combat range (mapped from 2–8) |
| 6 | Toggle farming enabled | Hold guard position |
| 7 | Show NPC status in chat | Show NPC status in chat |
| 8 | Show storage information in chat | Show storage information in chat |
| 27–53 | 27 actual storage slots | 27 actual storage slots |

Other upper slots contain information/filler items. Hover over the buttons to see instructions. Clicking slots only changes NPC behavior, never grants the UI's decorative item to the player.

### Testing after building

1. **Back up your server world**, stop Warfare, update the Stage 7 JAR on both the NeoForge 26.3.0.101 server and the corresponding Windows client (retain exactly one Ancient Warfare JAR per mods folder), restart.
2. Spawn worker and soldier using existing spawn eggs, recruit each with the first right click, then right-click again to open the GUI.
3. Toggle Follow/Guard/Work using GUI and ensure ownership enforcement works for another player.
4. Place a Farm Worksite near fully grown wheat. Use the GUI to assign it. Enable Work, then check wheat is harvested/replanted, items appear in NPC storage, and overflow remains on the ground if storage fills.
5. Put an ordinary item in the NPC's bottom storage rows; close/reopen GUI and restart the server to confirm persistence. Destroy NPC in an isolated test world to verify storage contents drop once on death.
6. Set soldier home, increase/decrease combat range, turn monster attacks on/off, and spawn a zombie to verify modes.
7. Verify `/awstructure list FillCobble`, `/awstructure inspect FillCobble1X16X16`, and `/awstructure place FillCobble1X16X16` still work in an empty test area. Inspecting does not modify the world; placing does.
8. Test transferring through your updated Velocity lobby and watch Warfare/Velocity logs for packet errors.

## Build and validation

```bash
python3 scripts/validate_stage7.py
gradle --no-daemon build
```

Gradle requires Java 25 and the Minecraft 26.3/NeoForge 26.3.0.101 mod development dependencies. The previous GitHub Actions workflow compiles with Java 25 and Gradle 9.2.1. This archive is intended for the same GitHub workflow after importing it. Static/offline checks are included; they do **not** prove that the mod compiles or loads.

### Important limitations

This is a functional **vanilla chest-style menu**, not the original Ancient Warfare custom GUI, and does not reproduce every original NPC role. Farm replanting still does not consume seeds (from Stage 6). Storage is not a warehouse network. Factions, research, siege machines, quarry, other jobs, and complete legacy building support are still missing. No automatic worldgen or advanced NPC models yet.

The project retains upstream attributions and GPL-3.0-or-later license files where applicable.

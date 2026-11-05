# Stage 3 status

| Subsystem | Current state | What was verified |
|---|---|---|
| Build and loader | Existing Stage 2 passed GitHub CI | Prior artifact only. Stage 3 awaits new CI build. |
| Original 11 materials | Migrated in Stage 2 | Previous CI compiled, no gameplay test. |
| 20 combat weapons | Ported with vanilla melee properties | Models and resources structurally checked; Stage 3 awaits compilation. |
| 15 shaped weapon recipes | Converted from Remastered patterns | JSON, output IDs, keys and resource paths validated. |
| 1,484 structure templates | Preserved as raw AWS | Java decoder parsed all templates and checked layer dimensions. |
| Structure world placement | Not implemented | No claim of structures spawning. |
| NPCs / factions / combat AI | Not implemented | No claim of NPC functionality. |
| Research / machines / torque | Not implemented | No claim of gameplay implementation. |
| Vehicles / siege | Not implemented | No claim of vehicle functionality. |

## Next necessary engineering

1. Verify Stage 3 NeoForge compilation and both client/server loading.
2. Restore original weapon reach using new attribute-based reach handling and correct special effects.
3. Implement AWS block-rule mapping and native world placement *with compatibility reporting for unknown legacy IDs*.
4. Port NPC entity registration, animation/rendering, data persistence, server-authoritative commands, job/route AI and faction standings.
5. Restore research, automations, warehouse logistics and torque network.
6. Restore siege systems and test entities/commands/networking in multiplayer.

Do not label this project complete until gameplay parity tests pass.

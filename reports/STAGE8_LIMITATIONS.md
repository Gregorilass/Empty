# Stage 8 logistics — current scope and remaining limitations

Stage 8 is **not** a full port of the 1.12.2 Automation module. It implements only the first real farm-to-warehouse delivery loop.

## Included
- Existing `farm_worksite` registry ID now has 27 persistent inventory slots (accessible by right-click).
- New `warehouse_controller` and `warehouse_storage` blocks have 54 persistent slots each.
- New recruitable Courier NPC (vanilla villager model), 27 cargo slots, explicit farm and controller assignments, same-world pathfinding.
- Farmer deposits drops into assigned / nearby Farm Worksite before personal storage, then drops overflow normally.
- Courier physically travels between the farm and controller, transfers only the items that fit, and never teleports items. When controller is full, nearest loaded warehouse storage within 8 horizontal / 2 vertical blocks accepts overflow. Unavailable or full stock causes the courier to retain cargo.
- Container items are saved in the BlockEntity; destroying stock blocks uses the default block-entity container-drop lifecycle.
- Stage 7 ownership rules still protect NPC commands and carried inventory. Stock blocks are shared/public, as in a basic communal warehouse.

## Missing or incomplete
- Original 1.12 warehouse crafting, stock viewer, stock filters, separate storage tiers, advanced stock-linkers and automated machine supply.
- Explicit courier item filters, route waypoints, automatic source/destination discovery, speed upgrades, jobs beyond farm deliveries.
- Cross-dimensional transport and chunk loading: deliberately **not** supported.
- Full original NPC custom textures, worker equipment and combat AI.
- Existing Stage 6 auto-replanting still creates new crops without consuming seed items.
- If an old worksite in a pre-Stage 8 world has no block entity, break and replace it in a backed-up world to generate its stock buffer.
- No client game startup or multiplayer gameplay validation has been performed; successful GitHub compilation is a separate milestone.

## Regression test plan (copy world first)
1. Verify the three new blocks and Courier egg appear in the Ancient Warfare creative tab, and warehouse recipes craft normally.
2. Right-click Farm Worksite (27 slots), Controller and Storage (54 slots). Insert a stack; close/reopen and restart world. The stack must persist.
3. Break each container with a few stored stacks. Exactly one copy of each stored stack should drop, in addition to the block item.
4. Spawn worker and courier. Recruit each. Assign both to the same farm, and assign the courier to a controller. Enable Work / Deliver modes. Crop drops should go Worker -> Worksite -> Courier -> Controller.
5. Fill the controller, place warehouse storage within eight horizontal and two vertical blocks, and confirm courier delivers overflow there.
6. Fill both controller and storage; courier must retain remaining inventory. Break the controller mid-route; courier must not discard cargo.
7. Restart the server while courier is travelling, and check farm/warehouse orders and contents persist.
8. Repeat on dedicated NeoForge 26.3 server behind Velocity to ensure GUI traffic and interactions do not disconnect clients.

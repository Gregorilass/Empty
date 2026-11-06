# Stage 9 + 10 verification, migration and limitations

## Preserved from Stage 8

- Worker, Soldier, Courier entity IDs and world-save keys
- Existing Farm Worksite, Warehouse Controller and Warehouse Storage block IDs
- Their 27-/54-slot inventories and persistence
- 1,484 historical `.aws` templates, earlier materials, melee weapons and recipes
- Owner-only NPC order panels and legacy structure commands

## New in Stage 9

- `ancientwarfare:tree_farm_worksite`: 27-slot persistent inventory, original-style texture
- `ancientwarfare:lumberjack`: NPC (villager renderer temporarily), same 27-slot equipment/cargo panel
- Assignment through existing NPC GUI, work mode, 2–8 radius
- Restricted vertical logs with dirt underneath and leaf crown; only 1x1 eligible trunks
- Safe extraction: block has to be removed before items are stored; log drops are inserted into worksite first, personal cargo next, ground last
- Matching sapling is consumed from worksite for replacement; otherwise tree remains felled
- Courier can now pick from farm OR tree-farm worksite; tree-farm saplings are retained for planting

## New in Stage 10

- `ancientwarfare:warehouse_stock_viewer` block with original 1.12 viewer texture
- Read-only vanilla 9x6 menu: 45 item entries/page + nav + refresh
- Aggregates matching item/component variants across controller and nearby loaded storage
- Does not mutate or transfer any stock; Controller and Storage still provide inventory access

## Known limitations and risks

- Does not implement the original complete Tree Farm tree-scanner, permissions, resource filtering, tiered warehouse, or research system
- Does not yet harvest floating sapling drops or collect tree leaf drops; wild multi-block trunks are deliberately skipped
- NPCs work only in loaded chunks and reachable same-dimension positions
- Client and server need matching modern port version
- Direct gameplay verification not yet performed for this release. New test world should be used until confirmed.

## Required test cases

1. Upgrade existing working Stage 8 world copy; verify all existing inventory and NPC data unchanged.
2. Place Tree Farm Worksite and Lumberjack, recruit and assign with menu, harvest a small mature oak tree.
3. Verify logs stored once only and saplings consumed once only; verify no leaf-free wood structure is chopped.
4. Fill worksite and Lumberjack inventory; any extra items must drop, not vanish.
5. Verify Courier collects logs from Tree Farm but leaves matching sapling supplies.
6. Verify Stock Viewer quantity calculation and pages; try every inventory manipulation (left, right, shift, double, drag, clone, drop). It must not give any display items.
7. Change warehouse contents and refresh viewer; verify counts update. Restart and check worksite and NPC persistence.
8. Test as server admin through Velocity and monitor server/proxy logs for failures.

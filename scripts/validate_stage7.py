"""Offline Stage 7 structural and safety checks. Does not replace Gradle compilation."""
from pathlib import Path
import json
import re
import subprocess

root = Path(__file__).resolve().parents[1]
pkg = root / 'src/main/java/net/shadowmage/ancientwarfare/core/npc'
npc = (pkg / 'AncientNpc.java').read_text()
menu = (pkg / 'NpcControlsMenu.java').read_text()

# Ensure preservation of previous work, persistence and server-only permissions.
assert 'extends Villager' in npc and 'mobInteract' in npc
for symbol in ('ownerId', 'following', 'customServerAiStep', 'followOwner', 'fightNearbyMonster',
               'harvestNearbyCrop', 'nearestFarmWorksite', 'guardPosition', 'AWFollowing',
               'AWOwner', 'AWStorage', 'AWTaskRadius', 'AWCombat', 'AWFarming', 'AWHasFarm',
               'ContainerHelper.saveAllItems', 'ContainerHelper.loadAllItems',
               'dropCustomDeathLoot', 'Block.getDrops', 'insertStorage', 'setControlsOpen'):
    assert symbol in npc, f'Missing stage7 NPC feature: {symbol}'
for symbol in ('extends ChestMenu', 'MenuType.GENERIC_9x6', '27', '54', 'clicked(',
               'ContainerInput.PICKUP', 'quickMoveStack(', 'stillValid(',
               'npc.isOwnedBy(player)', 'distanceToSqr(player)', 'broadcastChanges()',
               'persistStorage', 'setControlsOpen(false)'):
    assert symbol in menu, f'Missing stage7 GUI/safety feature: {symbol}'
assert 'this.moveItemStackTo(existing, CONTROL_SLOTS, ALL_NPC_SLOTS' in menu
assert 'this.moveItemStackTo(existing, ALL_NPC_SLOTS, this.slots.size()' in menu
assert 'super.clicked(slotIndex, buttonNum, input, player)' in menu
assert 'slotIndex >= 0 && slotIndex < CONTROL_SLOTS' in menu
assert 'serverPlayer.openMenu(new SimpleMenuProvider(' in npc

# Resource and recipe validation; reject the earlier 26.3 category crash.
for recipe in (root / 'src/main/resources/data/ancientwarfare/recipe').rglob('*.json'):
    parsed = json.loads(recipe.read_text())
    assert parsed.get('category') not in ('combat', 'building'), f'Unsupported recipe category: {recipe}'

subprocess.run(['python', str(root / 'scripts/validate_stage4.py')], cwd=root, check=True)
print('PASS Stage 7 offline regression checks: source features and recipes')

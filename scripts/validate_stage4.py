from pathlib import Path
import json
root=Path(__file__).resolve().parent.parent
files=[p for p in (root/'src/main/resources').rglob('*.json')]
for file in files:
    json.loads(file.read_text(encoding='utf-8'))
entities=(root/'src/main/java/net/shadowmage/ancientwarfare/core/npc/AWNpcRegistry.java').read_text()
core=(root/'src/main/java/net/shadowmage/ancientwarfare/core/AncientWarfareCore.java').read_text()
npc=(root/'src/main/java/net/shadowmage/ancientwarfare/core/npc/AncientNpc.java').read_text()
for name in ('worker','soldier'):
    assert f'"{name}"' in entities
    assert f'{name}_spawn_egg' in core
    assert (root/f'src/main/resources/assets/ancientwarfare/items/{name}_spawn_egg.json').is_file()
    assert (root/f'src/main/resources/assets/ancientwarfare/models/item/{name}_spawn_egg.json').is_file()
assert 'addAdditionalSaveData' in npc and 'readAdditionalSaveData' in npc
assert 'fightNearbyMonster' in npc and 'harvestNearbyCrop' in npc
structure=list((root/'src/main/resources').rglob('*.aws'))
assert len(structure)==1484, f'Expected 1484 AWS templates, found {len(structure)}'
print(f'Stage 4 data validation PASSED: {len(files)} JSON files, {len(structure)} AWS templates, NPC source/resources present')

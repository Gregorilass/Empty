"""Offline Stage 9+10 regression checks. Not a substitute for NeoForge build or gameplay."""
from pathlib import Path
import json
import subprocess

base = Path(__file__).resolve().parents[1]
java = base / 'src/main/java/net/shadowmage/ancientwarfare/core'
assets = base / 'src/main/resources/assets/ancientwarfare'
data = base / 'src/main/resources/data/ancientwarfare'

subprocess.run(['python3', 'scripts/validate_stage8.py'], cwd=base, check=True)
assert 'mod_version=0.0.10-stage10' in (base/'gradle.properties').read_text()
assert not (base/'.github').exists(), 'Do not upload workflow files through historic importer'
assert len(list((assets/'template').rglob('*.aws'))) == 1484

core = (java/'AncientWarfareCore.java').read_text()
registry = (java/'npc/AWNpcRegistry.java').read_text()
client = (java/'client/AWNpcClient.java').read_text()
npc = (java/'npc/AncientNpc.java').read_text()
menu = (java/'npc/NpcControlsMenu.java').read_text()
block = (java/'logistics/LogisticsBlockEntity.java').read_text()
tree = (java/'logistics/TreeFarmTask.java').read_text()
viewer = (java/'logistics/WarehouseOverviewMenu.java').read_text()
network = (java/'logistics/WarehouseNetwork.java').read_text()

for symbol in ('TREE_FARM_WORKSITE', 'WAREHOUSE_STOCK_VIEWER', 'LUMBERJACK_SPAWN_EGG',
               'TREE_FARM_WORKSITE_ITEM', 'WAREHOUSE_STOCK_VIEWER_ITEM'):
    assert symbol in core, symbol
assert 'LUMBERJACK' in registry and 'AWNpcRegistry.LUMBERJACK.get()' in client
assert 'AncientWarfareCore.TREE_FARM_WORKSITE.get()' in block
assert 'AWNpcRegistry.LUMBERJACK' in npc
assert 'TreeFarmTask.tick(' in npc and 'isDeliveryWorksite(level, assignedFarm)' in npc
assert 'Assign tree farm' in menu and 'npc.isLumberjack()' in menu
for symbol in ('BlockTags.DIRT', 'BlockTags.LEAVES', 'level.hasChunkAt',
               'level.setBlock', 'if (!level.setBlock', 'stock.removeItem(slot, 1)',
               'Block.getDrops', 'Block.popResource', 'saplingFor('):
    assert symbol in tree, symbol
for symbol in ('extends ChestMenu', 'MenuType.GENERIC_9x6', 'quickMoveStack',
               'canDragTo', 'canTakeItemForPickAll', 'refresh()', 'stillValid',
               'BlockPos controller = WarehouseNetwork.findControllerNear',
               'WarehouseNetwork.summarize', 'Read-only'):
    assert symbol in viewer, symbol
assert 'if (treeFarm && (itemPath.endsWith("_sapling")' in network
assert 'isSameItemSameComponents' in network, 'Stock summaries must retain item components'
assert 'List<StockEntry> summarize' in network

for name in ('tree_farm_worksite', 'warehouse_stock_viewer'):
    for parent in ('blockstates', 'models/block', 'models/item', 'items'):
        f = assets/parent/(name+'.json')
        assert f.is_file(), f
        json.loads(f.read_text())
    for parent in ('recipe', 'loot_table/blocks'):
        f = data/parent/(name+'.json')
        assert f.is_file(), f
        json.loads(f.read_text())
    assert json.loads((data/'recipe'/f'{name}.json').read_text())['category'] == 'misc'
for parent in ('items', 'models/item'):
    f=assets/parent/'lumberjack_spawn_egg.json'
    assert f.is_file(), f
    json.loads(f.read_text())

for tex in ('tree_farm_bottom', 'tree_farm_front','tree_farm_left','tree_farm_right',
            'tree_farm_rear','tree_farm_top','warehouse_stock_viewer'):
    assert (assets/'textures/block'/f'{tex}.png').is_file()

for file in (base/'src/main/resources').rglob('*.json'):
    json.loads(file.read_text())

lang = json.loads((assets/'lang/en_us.json').read_text())
for s in ('block.ancientwarfare.tree_farm_worksite',
          'block.ancientwarfare.warehouse_stock_viewer',
          'entity.ancientwarfare.lumberjack',
          'item.ancientwarfare.lumberjack_spawn_egg'):
    assert s in lang, s
print('PASS Stages 9+10 offline checks: legacy structures, tree farm, stock viewer, models, recipes, safety guards')

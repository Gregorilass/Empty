"""Stage 8 structural regression tests. These are not game runtime tests."""
from pathlib import Path
import json, subprocess

base = Path(__file__).resolve().parents[1]
src = base / 'src/main/java/net/shadowmage/ancientwarfare/core'
assets = base / 'src/main/resources/assets/ancientwarfare'
data = base / 'src/main/resources/data/ancientwarfare'

# Verify all previous milestones and the unique 1,484 original AWS structures remain intact.
subprocess.run(['python3', 'scripts/validate_stage7.py'], cwd=base, check=True)

core = (src/'AncientWarfareCore.java').read_text()
npc = (src/'npc/AncientNpc.java').read_text()
menu = (src/'npc/NpcControlsMenu.java').read_text()
reg = (src/'npc/AWNpcRegistry.java').read_text()
render = (src/'client/AWNpcClient.java').read_text()
block = (src/'logistics/LogisticsBlock.java').read_text()
be = (src/'logistics/LogisticsBlockEntity.java').read_text()
network = (src/'logistics/WarehouseNetwork.java').read_text()

for name in ('FARM_WORKSITE', 'WAREHOUSE_CONTROLLER', 'WAREHOUSE_STORAGE', 'LOGISTICS_BLOCK_ENTITY', 'COURIER_SPAWN_EGG'):
    assert name in core, name
for name in ('courier', 'COURIER', 'registerAttributes'):
    assert name in reg, name
assert 'AWNpcRegistry.COURIER.get()' in render
for name in ('newBlockEntity', 'ChestMenu.threeRows', 'ChestMenu.sixRows', 'useWithoutItem'):
    assert name in block, name
for name in ('implements Container', 'saveAdditional(ValueOutput', 'loadAdditional(ValueInput', 'setChanged()', 'ContainerHelper.saveAllItems', 'ContainerHelper.loadAllItems', 'return this.level != null', 'public ItemStack insert('):
    assert name in be, name
for name in ('hasChunkAt', 'isController', 'WAREHOUSE_STORAGE', 'deliverCargo(', 'findDestinations(', 'collect(', 'source.removeItem('):
    assert name in network, name
for name in ('runCourier(', 'findWarehouseControllerNear(', 'AWHasWarehouse', 'AWCourierEnabled', 'WarehouseNetwork.collect(', 'WarehouseNetwork.deliverCargo(', 'stock.insert(remaining)', 'getNavigation().moveTo('):
    assert name in npc, name
for name in ('isCourier()', 'hasAssignedWarehouse()', 'Toggle courier delivery', 'Assign nearest warehouse', 'npc.isOwnedBy(player)', 'controlContainer.persistStorage()'):
    assert name in menu, name
assert 'minecraft_version=26.3' in (base/'gradle.properties').read_text()
assert 'minecraft_version=26.3' in (base/'gradle.properties').read_text()  # version can advance beyond Stage 8
for name in ('warehouse_storage', 'warehouse_controller'):
    for parent in ('blockstates', 'models/block', 'models/item', 'items'):
        resource = assets/parent/(name+'.json')
        assert resource.is_file(), resource
        json.loads(resource.read_text())
    for parent in ('recipe', 'loot_table/blocks'):
        resource = data/parent/(name+'.json')
        assert resource.is_file(), resource
        json.loads(resource.read_text())
    assert json.loads((data/'recipe'/f'{name}.json').read_text())['category'] == 'misc'
assert 'entity.ancientwarfare.courier' in json.loads((assets/'lang/en_us.json').read_text())
print('PASS Stage 8 source, recipe, asset, renderer, storage and NPC regression checks')

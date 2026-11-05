"""Stage 5+6 source, assets, legacy structure decoder and palette regression tests."""
from pathlib import Path
import json
import subprocess
import sys
import tempfile

root = Path(__file__).resolve().parents[1]
res = root / 'src/main/resources'
assets = res / 'assets/ancientwarfare'
tree = assets / 'template'
manifest = tree / 'manifest.txt'
assert manifest.is_file()
listed = manifest.read_text(encoding='utf-8').splitlines()
actual = sorted(p.relative_to(tree).as_posix() for p in tree.rglob('*.aws'))
assert listed == actual and len(listed) == 1484, 'Structure index must match actual templates exactly'
assert len(set(listed)) == len(listed), 'Duplicate structure IDs'
for f in res.rglob('*.json'):
    json.loads(f.read_text(encoding='utf-8'))
for asset in ['blockstates/farm_worksite.json', 'models/block/farm_worksite.json',
              'models/item/farm_worksite.json', 'items/farm_worksite.json',
              'textures/block/farm_worksite_top.png', 'textures/block/farm_worksite_side.png',
              'textures/block/farm_worksite_bottom.png']:
    assert (assets / asset).is_file(), f'Missing farm worksite resource {asset}'
java = root / 'src/main/java/net/shadowmage/ancientwarfare/core'
assert 'registerSimpleBlockItem("farm_worksite"' in (java/'AncientWarfareCore.java').read_text()
assert 'nearestFarmWorksite' in (java/'npc/AncientNpc.java').read_text()
assert (java/'structure/AwsStructureCommands.java').is_file()
assert 'Commands.hasPermission' in (java/'structure/AwsStructureCommands.java').read_text()
with tempfile.TemporaryDirectory() as d:
    source = [java/'structure/AwsTemplateParser.java', java/'structure/AwsLegacyPalette.java',
              root/'tests/AwsTemplateParserTest.java', root/'tests/AwsLegacyPaletteTest.java']
    subprocess.run(['javac', '-encoding', 'UTF-8', '-d', d, *map(str, source)], check=True)
    for test in ('AwsTemplateParserTest','AwsLegacyPaletteTest'):
        subprocess.run(['java','-cp',d,test,str(tree)],check=True)
print('Stage 5+6 validation passed: all source, resources, manifest and Java regression tests')

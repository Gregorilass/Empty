#!/usr/bin/env python3
"""Structure-preserving checks for Stage 3. No mod/runtime claims here."""
import json
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / 'src/main/resources'
SRC = ROOT / 'src/main/java/net/shadowmage/ancientwarfare/core/structure/AwsTemplateParser.java'
TEST = ROOT / 'tests/AwsTemplateParserTest.java'

def main():
    manifest = json.loads((ROOT / 'reports/stage3_content_manifest.json').read_text())
    templates = list((RES / 'assets/ancientwarfare/template').rglob('*.aws'))
    assert len(templates) == 1484, f'Expected 1484 templates, found {len(templates)}'
    assert len(manifest['migrated_weapons']) == 20
    assert len(manifest['recipes']) == 15
    for n in manifest['migrated_weapons']:
        modelpath = RES / f'assets/ancientwarfare/models/item/{n}.json'
        wrapper = RES / f'assets/ancientwarfare/items/{n}.json'
        texture = RES / f'assets/ancientwarfare/textures/item/{n}.png'
        assert modelpath.is_file() and wrapper.is_file() and texture.is_file(), n
        model = json.loads(modelpath.read_text())
        assert json.loads(wrapper.read_text())['model']['model'] == f'ancientwarfare:item/{n}'
        parent = model.get('parent', '')
        if parent.startswith('ancientwarfare:item/'):
            assert (RES / ('assets/ancientwarfare/models/item/'+parent.split('/')[-1]+'.json')).is_file(), (n,parent)
    for n in manifest['recipes']:
        recipe = json.loads((RES / f'data/ancientwarfare/recipe/{n}.json').read_text())
        assert recipe['result']['id'] == 'ancientwarfare:'+n
        assert recipe['type'] == 'minecraft:crafting_shaped'
        assert set(''.join(recipe['pattern'])) - {' '} <= set(recipe['key'])
    with tempfile.TemporaryDirectory() as tempdir:
        subprocess.run(['javac', '-encoding', 'UTF-8', '-d', tempdir, str(SRC), str(TEST)], check=True)
        subprocess.run(['java', '-Xmx768m', '-cp', tempdir, 'AwsTemplateParserTest', str(RES / 'assets/ancientwarfare/template')], check=True)
    print('PASS Stage3: 20 weapons, 15 recipes, 1484 AWS templates, Java parser regression')

if __name__ == '__main__': main()

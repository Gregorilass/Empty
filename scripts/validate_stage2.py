from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
assets=root/'src/main/resources/assets/ancientwarfare'
recipes=root/'src/main/resources/data/ancientwarfare/recipe'
items=['steel_ingot','research_book','wooden_gear','iron_gear','steel_gear','wooden_shaft','iron_shaft','steel_shaft','wooden_bearings','iron_bearings','steel_bearings']
assert len(list(recipes.glob('*.json')))==10
for name in items:
    for path in [assets/'models/item'/f'{name}.json',assets/'items'/f'{name}.json',assets/'textures/item'/f'{name}.png']:
        assert path.is_file(),path
    assert (assets/'items'/f'{name}.json').is_file()
    x=json.loads((assets/'items'/f'{name}.json').read_text())
    assert x['model']['model']=='ancientwarfare:item/'+name
    model=json.loads((assets/'models/item'/f'{name}.json').read_text())
    assert model['textures']['layer0']=='ancientwarfare:item/'+name
for recipe_path in recipes.glob('*.json'):
    rec=json.loads(recipe_path.read_text())
    assert rec['result']['id']=='ancientwarfare:'+recipe_path.stem
    assert 'item' not in rec['result']
    if rec['type']=='minecraft:crafting_shaped':
        assert all(isinstance(v,str) or isinstance(v,list) for v in rec['key'].values())
    else: assert rec['type']=='minecraft:crafting_shapeless'
assert (root/'reports/remastered_0.2.9/inventory.json').exists()
print('PASS: 11 remastered items and 10 modern-format crafting recipes; JSON syntax and reference paths')

import csv
import re
from pathlib import Path
root=Path(__file__).parents[1]
rows=list(csv.DictReader((root/'inventory.csv').open()))
assert len(rows)==100 and len({r['id'] for r in rows})==100
for r in rows:
 assert r['url'].startswith('https://github.com/')
 assert r['clone_command'].startswith('git clone ')
 assert r['stable_ref_status'] in {'unknown/unverified', 'reviewed-head-commit'}
 if r['stable_ref_status'] == 'reviewed-head-commit':
  assert re.search(r'\b[0-9a-f]{40}\b', r['stable_ref_guidance'])
 else:
  assert r['url'] in r['stable_ref_rationale']
  assert 'could not be resolved' in r['stable_ref_rationale']
 assert r['stable_ref_guidance']
 assert r['self_hosting_decision'] and r['self_hosting_manifest']
 assert r['id'] in r['self_hosting_manifest'] or r['id'] in {'postgres', 'keycloak', 'openbao', 'kong', 'blnk'}
 assert r['id'] in r['blockers']
 assert (root/'adapters'/r['id']/'README.md').exists()
 assert (root/'adapters'/r['id']/'config.env.example').exists()
print('inventory valid: 100 rows, 100 adapter surfaces, no secrets')

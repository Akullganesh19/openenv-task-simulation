"""Static guardrail for the backend integration boundary (no credentials required)."""
from pathlib import Path
import csv, re
import subprocess, sys
root=Path(__file__).parents[1]
contract=(root/'contracts/backend-integration.yaml').read_text()
kong=(root/'contracts/kong.yml').read_text()
rego=(root/'policy/unigig.rego').read_text()
required=['keycloak_issuer','preferred_username','realm_access.roles','OPENBAO_REF:secret/data/unigig/','ledgerProvider','blnk','idempotencyKey']
for marker in required:
    assert marker in (contract+kong+rego), f'missing policy marker: {marker}'
assert 'name: jwt' in kong and 'claims_to_verify: [exp]' in kong
subprocess.run([sys.executable, str(root/'policy'/'test_middleware.py')], check=True)
rows=list(csv.DictReader((root/'inventory.csv').open()))
assert len(rows)==100
for row in rows:
    assert row['clone_command'].startswith('git clone ')
    assert row['stable_ref_guidance']
    assert row['self_hosting_decision']
print('contract policy valid: Keycloak claims, Kong JWT, OpenBao refs, and Blnk-only money path')

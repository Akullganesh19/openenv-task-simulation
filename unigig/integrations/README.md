# UniGig integration registry

`inventory.csv` is the auditable 100-repository master list. Every row contains capability, URL, license/cost status, overlap mapping, phase, blockers, self-hosting decision, exact clone command, and stable-reference guidance. Every row has a matching `adapters/<id>/` directory with the same decision, acquisition command, environment placeholder, and risk note.

## Enablement boundary

Kong is the only backend ingress. After Kong verifies the JWT signature, run the executable boundary with `KEYCLOAK_ISSUER=... ALLOWED_ROLES=... python3 -m integrations.policy.server`; it requires a matching issuer, `sub`, `preferred_username`, expiry, and an allowed `realm_access.roles` value before dispatch. The boundary accepts only `OPENBAO_REF:secret/data/unigig/...` references and rejects money operations unless `ledgerProvider=blnk` plus an idempotency key are present. Adapter responses cannot create balances.

## Repeatable setup

```sh
cp .env.example .env # fill values locally; never commit .env
python3 integrations/scripts/validate_inventory.py
python3 integrations/policy/validate_contract.py
./gradlew testDebugUnitTest lintDebug assembleDebug
```

`compose/docker-compose.yml` is a local foundation for services that can be safely evaluated here. The inventory's per-tool decision is authoritative for the remaining tools; unavailable infrastructure, hosted costs, hardware, provider onboarding, or license uncertainty are recorded as blockers rather than faked.

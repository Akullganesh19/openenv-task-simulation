# Inspector verdict — iteration 4

Builder commit inspected: `99a67e40bc2a5fc026a160d40decdf3d8b87aed7`
(`feat(integrations): [B] record reviewed upstream references`).

## Validation evidence

- `python3 integrations/scripts/validate_inventory.py` — **PASS**: 100 rows,
  100 unique IDs, 100 adapter surfaces, required fields, and no committed
  credential patterns.
- Adapter audit — **PASS**: every inventory row has a matching `README.md` and
  `config.env.example`; each README contains the inventory URL and exact clone
  command.
- Stable-reference count — **PASS**: 42 rows are marked
  `reviewed-head-commit` and 58 are marked `unknown/unverified`.
- Stable-reference authenticity — **FAIL**: 41 of the 42 reviewed SHAs matched
  the live `HEAD` returned by `git ls-remote`. The `grafana` reference records
  `85de834cd7c3a89c06ee74e3be48531ce6ac881f`, while the live upstream HEAD is
  `766573646d4f1782dc98713649c4c38678ecaa37`.
- Unknown-reference rationale audit — **FAIL**: all 58 rationales normalize to
  one sentence template after replacing the repository URL. They are
  ID/URL-specific only and do not provide genuinely tool-specific rationale.
- `python3 -m unittest discover -s integrations/policy -p 'test_*.py' -v` —
  **PASS** (5 tests).
- `python3 integrations/policy/validate_contract.py` — **PASS**.
- `python3 -m compileall -q integrations/scripts integrations/policy` —
  **PASS**.
- Executable middleware probes — **PASS** for valid Keycloak claims, wrong
  issuer rejection, scoped OpenBao references, and non-Blnk money rejection.
- Secret scan and `git diff --check 99a67e4^ 99a67e4` — **PASS**. The
  `api.example.invalid` values are documented configuration placeholders in
  `.env.example` and the contract, not credentials or Android fallbacks.
- `./gradlew --version` — **PASS** (Gradle 9.3.1).
- `./gradlew testDebugUnitTest lintDebug assembleDebug` — **BLOCKED** first by
  the required `UNIGIG_BACKEND_URL`; with an explicit non-secret URL it reaches
  the accurately reported missing Android SDK (`ANDROID_HOME`/`local.properties`).
- Docker Compose validation — **BLOCKED**: Docker is unavailable.
- OPA validation — **BLOCKED**: `opa` is unavailable; the Python middleware is
  the runnable enforcement path.

## Acceptance criteria

1. **PASS** — complete 100-row inventory and required fields.
2. **PASS** — isolated adapter/configuration surfaces exist for all 100 tools
   without client-side secrets.
3. **PASS with scope note** — executable middleware enforces Keycloak claims,
   OpenBao reference scope, and Blnk-only money operations; Kong signature
   verification and OPA are documented/runtime prerequisites.
4. **PASS** — foundation Compose services are represented and other tools have
   explicit blocked records rather than fabricated manifests.
5. **PASS with blocker** — Android configuration has no hardcoded backend
   fallback or provider credentials; the Android SDK is unavailable here.
6. **FAIL** — one reviewed SHA is stale, and the remaining 58 rationales are a
   single generic template rather than genuinely tool-specific review records.
7. **PASS with blockers** — available Python, inventory, policy, secret, diff,
   and wrapper gates ran; Android SDK, Docker, and OPA are unavailable.

## Verdict

**FAIL** — iteration 4 improves the reference inventory, but criterion 6 is
still not satisfied: one immutable reference is incorrect and all 58
unresolved-reference rationales are templated.

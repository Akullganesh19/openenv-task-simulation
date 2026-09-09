# Goal: Integrate UniGig's open-source tool ecosystem

## User Request

Clone and integrate every feasible repository in the UniGig master list, enhance the project, and leave secure configuration slots for secrets that the user will fill later.

## Refined Goal

Turn the extracted UniGig Android project into the first phase of an integration-ready platform. Create an auditable inventory and isolated integration surface for the full 100-repository tool list, implement the locally feasible adapters and configuration contracts, and document how external services are enabled without placing secrets in the Android client. Integrations must preserve Keycloak-claim authorization, OpenBao-managed secrets, Kong-routed backend traffic, and Blnk as the source of truth for money-relevant state.

## Acceptance Criteria

- [ ] Criterion 1: The project contains a complete 100-repository inventory grouped by capability, with repository URL, license/cost verification status, overlap/replacement mapping, implementation phase, and explicit blockers or required credentials.
- [ ] Criterion 2: The project contains an isolated, repeatable integration structure for each repository, with local adapters/configuration slots where implementation is feasible and no committed secrets or client-side API keys.
- [ ] Criterion 3: Core integration contracts enforce server-side authorization via Keycloak claims, secret references through OpenBao, service traffic through Kong, and money state through Blnk rather than tool-local balances.
- [ ] Criterion 4: A Docker Compose or equivalent self-hosting manifest exists for each service that can be safely self-hosted from the available project context; services requiring unavailable infrastructure are documented as blocked rather than silently faked.
- [ ] Criterion 5: The Android app remains buildable in principle, contains no hardcoded credentials, and delegates external integrations to backend/service boundaries.
- [ ] Criterion 6: Documentation includes exact clone commands, stable branch/tag guidance when discoverable, environment-variable placeholders, risk notes, and a short README-ready entry for every tool.
- [ ] Criterion 7: Existing project tests and the narrowest available Gradle quality gates are run or, when blocked by missing Android tooling, the blocker and static validation evidence are recorded.

## Scope Boundaries

**In scope:**
- All 100 repositories in the supplied UniGig master list.
- Inventory, license/cost review, overlap mapping, phased architecture, clone manifests, adapter contracts, Compose/service configuration, and secure environment placeholders.
- Enhancements to the extracted project at `/Users/ganesh/Desktop/unigig`.

**Out of scope:**
- Committing secrets, private keys, API tokens, production credentials, or user data.
- Claiming production readiness for upstream services that require external infrastructure, paid providers, unsupported hardware, or license review.
- Moving money, KYC decisions, authorization, or trust decisions into the Android client.
- Rewriting or vendoring the upstream repositories into the Android module.

## Applicable Project Conventions

**Quality gate command:**
- `./gradlew testDebugUnitTest lintDebug assembleDebug` when Android SDK and Gradle wrapper are available.
- Current environment lacks a working Android toolchain; record any resulting blocker and run static checks available locally.

**Commit convention:**
- Conventional commits by default; Builder commits use `type(scope): [B] description`, Inspector commits use `chore(scope): [I] description`.
- Assisted-by trailer required: `Assisted-by: Claude:Sonnet-4.6` for Builder and `Assisted-by: Claude:Haiku-4.5` for Inspector.

**Guidelines:**
- No UniGig-specific `AGENTS.md`, `CONSTITUTION.md`, `.agents/`, or `.github/` guidelines were found.
- Project guidance is in `/Users/ganesh/Desktop/unigig/README.md` and the Gradle configuration.

**Rules:**
- Preserve unrelated user changes.
- Do not add secrets to source control.
- Keep external integrations behind backend boundaries and use explicit failure states rather than silent fallbacks.

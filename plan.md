# ProgressiveStages 3.0.4 Polish and Release Closure Plan

> **Plan ID:** PLAN-MASTER
> **Plan status:** VALIDATED WITH KNOWN EXTERNAL BLOCKER
> **Project state:** EXISTING
> **Planning subject:** ProgressiveStages 3.0.4 polish and stable release closure
> **Plan profile:** software_product

## 1. Project Identity

```text
Project: ProgressiveStages NeoForge mod
Requested artifact: authoritative_plan
Repository root: /tmp/ProgressiveStages-polish-plan
Starting branch: envy/polish-3.0.4-plan
Starting commit: bd462b2533c7776b91fae1e302d98151f1fb1b38
Authoritative remote:
origin
https://github.com/MCEnvision/ProgressiveStages.git
Remote ref: origin/envy/polish-3.0.4-plan
Remote commit: bd462b2533c7776b91fae1e302d98151f1fb1b38
Package metadata: mod_id progressivestages, version 3.0.3, Minecraft 1.21.1, NeoForge 21.1.219
Target release: 3.0.4
```

## 2. Planning Subject and Source Roles

| ID | Role | Subject | Source | Intended use |
|---|---|---|---|---|
| SRC-001 | owner_request | 3.0.4 polish release objective and locked scope | EnVy owner request and direct Plan Creator invocation on 2026-09-02 | defines endpoint, six issue baseline, release scope, and owner decisions |
| SRC-002 | existing_plan | existing ProgressiveStages 3.0.4 plan set | /tmp/ProgressiveStages-polish-plan/plan.md and phases | preserves stable requirements, decisions, phases, and completion endpoint |
| SRC-003 | repository_evidence | ProgressiveStages implementation and supported platform | README.md, Gradle metadata, source, tests, and workflows | defines NeoForge 1.21.1 compatibility and implementation boundaries |
| SRC-004 | review_feedback | six fixed issue reports | GitHub issues #8, #10, #11, #16, #24, and #25 | defines mandatory defect outcomes and evidence requirements |
| SRC-005 | status | release and CI state | origin/master, release history, and Actions evidence | defines current release gate and known workflow blocker |
| SRC-006 | audit_evidence | entity presence performance report | GitHub issue #24 profile and configuration | defines performance fixture and threshold evidence |
| SRC-007 | audit_evidence | shared release validation failure | GitHub Actions run 31453460136 job 93662356066 | defines EXT-002 attestation prerequisite |
| SRC-008 | owner_request | recipe lock serialization defect | EnVy report and GitHub issue #25 | defines canonical recipe fields, atomic recovery, and runtime proof |
| SRC-009 | owner_request | player initiated inventory insertion locks | EnVy request and SuperDevyn profession selling bin use case | promotes CORE-REQ-012 without adding a seventh issue |

The planning subject is the ProgressiveStages 3.0.4 polish and stable release closure. Source artifacts provide scope, current state, and evidence. They do not replace the authoritative plan or add scope beyond the locked intake.

## 3. Purpose and Intended Outcome

ProgressiveStages needs a bounded polish release that converts the six active reports into verified outcomes, adds the owner-promoted inventory insertion lock, and preserves the existing NeoForge 1.21.1 stage model. Server administrators need entity presence rules that do not dominate the server thread. Pack authors need Curios, JEI, and EMI behavior that survives supported optional dependency combinations. Operators need the Easy Builder controls, recipe-lock serialization, inventory insertion rules, and in game category menu to match published behavior in the installed artifact.

The intended outcome is the exact completion endpoint recorded in §18 and §19. The work is limited to the six-issue baseline, the explicit CORE-REQ-012 inventory insertion feature, and only those advertised or documented 3.0.3 capabilities that the Phase 000 audit proves missing from a shipped artifact.

## 4. Evidence-Based Current State

| Area | Evidence class | Finding | Evidence |
|---|---|---|---|
| Repository baseline | VERIFIED | The locked audit baseline is the saved goal checkout `5b3077764907249b3711886cca538794f6139acf` on `origin/envy/polish-3.0.4-plan` | Saved goal provenance and Git revision inspection recorded in §1 |
| Runtime contract | VERIFIED | The supported release target is ProgressiveStages for Minecraft 1.21.1 on NeoForge 21.1.219 | Gradle metadata and mod metadata inspection described by SRC-003 |
| Issue baseline | VERIFIED | The active issue baseline is exactly `#8`, `#10`, `#11`, `#16`, `#24`, and `#25` | GitHub issue inspection in SRC-004 and SRC-008 |
| Entity presence cost | OBSERVED | The reported hotspot constructs Minecraft rule context through the entity tracking decision path | Spark profile and configuration in SRC-006 |
| Curios integration | OBSERVED | The current slot gate needs version tolerant resolution for the Curios 9.5.1 API surface | Issue `#8` and implementation evidence in SRC-003 |
| Recipe viewers | VERIFIED | Existing configuration exposes EMI controls but does not prove independent JEI and EMI behavior in combined installations | Configuration inspection and optional integration evidence in SRC-003 |
| Easy Builder enchantments | UNKNOWN | Existing source and tests require final editor bundle and JAR workflow verification | Issue `#11`, SRC-003, and Phase 000 audit |
| Easy Builder recipe locks | OBSERVED | Issue `#25` reports an ineffective or lost visual recipe lock. The exact historical visual serializer representation remains unresolved until CORE-PHASE-000 records a production-bundle observation. The parser accepts the distinct canonical `[recipes].locked_ids` and `[recipes].locked_items` fields. | SRC-008 and the CORE-PHASE-000 evidence packet |
| Inventory insertion locks | OBSERVED | Existing `[[interactions]]` entries cover item-on-block, block right-click, and item-on-entity with one held-item selector and one world target. The current container-menu hook gates locked-item movement and hotbar placement but does not resolve a destination inventory owner or compile a two-selector insertion decision. The Easy Builder currently serializes one category selector per rule. | `StageFileParser.parseInteractions`, `LockDefinition.InteractionLock`, `AbstractContainerMenuMixin`, `Schema4StageCompiler.addGenericRules`, `BuiltinEditorSchemas`, `RulesPanel.tsx`, and SRC-009 |
| Category menu depth | UNKNOWN | Source ordering changes require production map verification at supported GUI scales | Issue `#16`, SRC-003, and Phase 000 audit |
| Artifact parity | UNKNOWN | Published 3.0.3 documentation has not been fully reconciled with the shipped JAR | SRC-002 and CORE-REQ-007 |
| Release validation | VERIFIED | The 3.0.3 release validation fails during shared attestation verification because its GitHub CLI signer flags are incompatible | Failed GitHub Actions run `31453460136`, job `93662356066` in SRC-007 |

## 5. Product Contract and Profile Coverage

| Profile area | Status | Source | Contract location | Rationale |
|---|---|---|---|---|
| inputs and outputs | covered | SRC-001, SRC-003 | Public contract and architecture | The plan defines stage files, editor input, command input, runtime decisions, and release outputs. |
| component architecture | covered | SRC-003 | Architecture and ownership | The plan assigns core engine, adapters, editor, client, enforcement, and release ownership. |
| state and persistence | covered | SRC-003, SRC-008 | State, compatibility, migration, and recovery | The plan defines authoritative snapshots, persisted TOML, atomic editor state, and compatibility. |
| failure taxonomy | covered | SRC-004, SRC-008 | Failure, recovery, and risk boundaries | The plan defines invalid drafts, optional dependency failures, transaction denial, and recovery. |
| versioning | covered | SRC-001, SRC-003 | Compatibility, migration, rollout, and recovery | The plan pins 3.0.4, Minecraft 1.21.1, NeoForge 21.1.219, and compatible schemas. |
| security | covered | SRC-003, SRC-009 | Security and trust boundaries | The plan preserves server authority, operator permission, destination resolution, and secret boundaries. |
| test system | covered | SRC-004, SRC-006, SRC-008 | Verification strategy | The plan requires unit, integration, runtime, multiplayer, performance, artifact, and recovery proof. |
| release lifecycle | external_prerequisite | EXT-002, EXT-003 | External prerequisites and release gates | The final signed release requires shared workflow repair and scoped owner publication authorization. |
| generalization | covered | SRC-003, SRC-009 | Compatibility and architecture | Optional integrations, playerless automation, and modded target resolver behavior remain bounded. |
| determinism | covered | SRC-008, SRC-009 | Architecture and verification strategy | The plan requires canonical rule normalization, deterministic priority, atomic state, and artifact hashes. |

## 6. Mandatory Scope

- CORE-REQ-001 — Freeze and reproduce or artifact-verify the six baseline reports, including stale tracking reports
- CORE-REQ-002 — Repair entity-presence performance without changing multiplayer visibility, spawning, targeting, or lifecycle semantics
- CORE-REQ-003 — Restore Curios slot gating with version-tolerant API resolution and absent-mod safety
- CORE-REQ-004 — Make JEI and EMI independently configurable, optional, and simultaneously functional
- CORE-REQ-005 — Verify or repair published Easy Builder enchantment controls in the production editor bundle and final JAR
- CORE-REQ-006 — Verify or repair category menu depth in the in-game progression map without regressions to navigation and inspector behavior
- CORE-REQ-007 — Implement any previously advertised 3.0.3 user-visible capability that the polish matrix proves missing from a shipped artifact
- CORE-REQ-008 — Preserve supported configuration, stage schema, saved data, commands, APIs, and multiplayer compatibility
- CORE-REQ-009 — Repair and prove the shared release attestation verifier for the 3.0.4 release artifact
- CORE-REQ-010 — Build, integrate, attest, document, publish, and verify 3.0.4, then close the six baseline issues with evidence
- CORE-REQ-011 — Correct the Easy Builder recipe-lock round trip so visual rules serialize, validate, persist, reload, compile, and enforce through the canonical `[recipes].locked_items` field without rule loss
- CORE-REQ-012 — Add server-authoritative player-initiated inventory insertion locks with source-item and destination-inventory selectors, priority exceptions, complete Easy Builder and TOML round trips, and lossless transaction enforcement

## 7. Optional / Future Scope

Every item in this section is excluded and non-blocking for this plan.

- FUT-001 — The legacy roadmap's unimplemented feature phases beyond advertised 3.0.3 behavior — excluded
- FUT-002 — Dependabot pull request #19 unless it becomes security or compatibility blocking evidence — excluded
- FUT-003 — New feature requests outside the six-issue audit baseline and the explicitly owner-promoted CORE-REQ-012 inventory insertion feature — excluded
- FUT-004 — Minecraft, NeoForge, Java, Gradle, or mapping upgrades — excluded

## 8. Non-Goals

- NG-001 — No unscoped progression redesign, broad schema overhaul, or lock category beyond the bounded inventory-interaction extension in CORE-REQ-012
- NG-002 — No hard dependency on Curios, JEI, EMI, or another optional integration
- NG-003 — No full registry, stage, or entity scan in server tick or render hot paths
- NG-004 — No issue closure based only on source presence, prior release notes, or lower-fidelity checks

## 9. Owner Decisions

### DEC-001 — Completion endpoint

**Status:** RESOLVED
**Selected choice:** Publish verified ProgressiveStages 3.0.4 to CurseForge and Modrinth after master integration.
**Rationale:** The owner selected a public patch release with verified integration
**Affected requirements:** CORE-REQ-009, CORE-REQ-010
**Supersedes:** none

### DEC-002 — Mandatory scope boundary

**Status:** RESOLVED
**Selected choice:** Correct promised 3.0.3 gaps proven by audit and exclude unpromised features except promoted CORE-REQ-012.
**Rationale:** The polish matrix controls completeness without expanding the product roadmap
**Affected requirements:** CORE-REQ-001, CORE-REQ-007
**Supersedes:** none

### DEC-003 — Recipe viewer operation

**Status:** RESOLVED
**Selected choice:** JEI and EMI settings are independent, default enabled, and concurrent when installed.
**Rationale:** Mixed client installations require independent optional viewer controls
**Affected requirements:** CORE-REQ-004
**Supersedes:** none

### DEC-004 — Entity presence performance gate

**Status:** RESOLVED
**Selected choice:** Use one context per player, server tick, and authoritative state revision, with no per entity rebuild, under five percent work, and at most ten percent p95 MSPT regression.
**Rationale:** The lag correction needs fixed correctness and performance limits
**Affected requirements:** CORE-REQ-002
**Supersedes:** none

### DEC-005 — Shared workflow release gate

**Status:** RESOLVED
**Selected choice:** Shared attestation repair and verification are mandatory before publication.
**Rationale:** Release evidence must be verified by the supported shared workflow
**Affected requirements:** CORE-REQ-009, CORE-REQ-010
**Supersedes:** none

### DEC-006 — Issue 25 inclusion

**Status:** RESOLVED
**Selected choice:** Issue 25 is mandatory and the fixed baseline contains exactly six issues.
**Rationale:** The owner promoted the reported editor serialization defect into the current polish release before goal creation
**Affected requirements:** CORE-REQ-001, CORE-REQ-008, CORE-REQ-010, CORE-REQ-011
**Supersedes:** none

### DEC-007 — Inventory insertion lock inclusion

**Status:** RESOLVED
**Selected choice:** CORE-REQ-012 adds destination aware player initiated item_into_inventory locks without attributing automation to nearby players.
**Rationale:** Profession packs need to deny ore insertion into a selling bin, chest, furnace, or compatible modded container until the player owns the required stage, without item loss, duplication, or automation regressions.
**Affected requirements:** CORE-REQ-008, CORE-REQ-010, CORE-REQ-012
**Supersedes:** DEC-002 only for the explicitly bounded CORE-REQ-012 feature. All other novel feature requests remain excluded by FUT-003.

## 10. External Prerequisites

### External prerequisites and release gates

The external contracts below gate their owning runtime or publication actions without authorizing lower-fidelity substitutes.

| ID | Prerequisite | Affected requirements | Availability | Authorization | Required external action |
|---|---|---|---|---|---|
| EXT-001 | Public Curios JEI and EMI test artifacts for Minecraft 1.21.1 and NeoForge 21.1.219 | CORE-REQ-003, CORE-REQ-004 | available | not_required | Resolve the artifact set and execute the runtime matrix |
| EXT-002 | Corrected MCEnvision shared release-validation workflow revision | CORE-REQ-009, CORE-REQ-010 | unavailable | unknown | Merge and validate the compatible attestation verifier revision |
| EXT-003 | Owner-approved platform publication confirmation for the final 3.0.4 artifact | CORE-REQ-010 | unknown | unknown | Confirm the final preview through the configured release broker |

### EXT-001 — Public Curios JEI and EMI test artifacts for Minecraft 1.21.1 and NeoForge 21.1.219

**Kind:** artifact
**Availability:** available
**Authorization:** not_required
**Affected requirements:** CORE-REQ-003, CORE-REQ-004
**Artifact evidence:** authoritative_source, compatibility, exact_version, license_provenance, security_review, sha256, sha512

**Required evidence**

- authoritative source
- exact version
- sha256
- sha512
- compatibility
- license provenance
- security review

### EXT-002 — Corrected MCEnvision shared release-validation workflow revision

**Kind:** service
**Availability:** unavailable
**Authorization:** unknown
**Affected requirements:** CORE-REQ-009, CORE-REQ-010

**Required evidence**

- merged workflow revision
- successful disposable candidate verification
- failed tampered candidate verification
- attestation evidence

### EXT-003 — Owner-approved platform publication confirmation for the final 3.0.4 artifact

**Kind:** authorization
**Availability:** unknown
**Authorization:** unknown
**Affected requirements:** CORE-REQ-010
**Authorization scope binding:** runbook_digest, artifact_identities, systems, operations, operators, time_window, rollback

**Required evidence**

- approval bound to final artifact hashes
- runbook digest
- platform targets
- operator window
- rollback contract

## 11. Architecture and Ownership Boundaries

### Architecture and ownership

The logical server owns stage membership, compiled rule decisions, configuration reload, permissions, and all access enforcement. Client code owns only presentation, interaction capture, and synchronized caches. A client may request an operator action through the editor but cannot authorize a stage mutation, condition decision, or rule bypass.

### Security and trust boundaries

Server authority, authenticated player origin, operator permission, stable destination identity, validation before mutation, optional dependency isolation, and secret exclusion are mandatory across every component described below.

`CompiledRuleEngine` owns normalized rule resolution. `MinecraftConditionContextFactory` owns a player relevant immutable condition context. `EntityPresenceEnforcer` owns tracking concealment, interaction denial, and pacification for player specific entity presence rules. CORE-REQ-002 must add a server thread confined snapshot boundary keyed by player identity, server tick, rule revision, and relevant state revision. Exactly one context may exist for each player, tick, and authoritative state-revision tuple. A rule or relevant-state revision that changes during the same server tick invalidates the previous tuple before the next decision and creates at most one context for the new tuple. It must invalidate on rule reload, stage mutation, team mutation, score or metric mutation, dimension transition, session transition, disconnect, and server stop. It must not share contexts across players, worlds, or rule revisions.

Curios, JEI, and EMI are optional adapter boundaries. Their classes must remain isolated from common startup and dedicated server class loading. The adapters translate a core decision into external API behavior but do not own stage policy. A missing or incompatible optional mod retains no integration behavior and emits a concise diagnostic rather than a crash or a permissive fallback.

The Easy Builder, TOML source view, schema compiler, runtime enforcement, and cleanup fallback share one canonical data model. Easy Builder owns deterministic visual-form serialization. The server schema registry and draft validator own canonical-field admission and atomic rejection before persistence. For the issue `#25` recipe-output workflow, the single canonical key is `[recipes].locked_items`; `[recipes].locked_ids` remains the distinct recipe-identifier key, and the generic `[recipes].locked` alias must never be persisted as a successful recipe lock. A valid existing rule or live stage file must survive any migration or rejected draft unchanged. `StageTreeScreen` owns client menu depth and input routing. The category overlay must render above map nodes, capture menu input while open, and preserve map navigation and inspector semantics after closing.

Inventory insertion gating extends the existing interaction-rule surface rather than creating an unrelated lock category. The canonical schema is a `[[interactions]]` entry with `type = "item_into_inventory"`, a source `held_item` selector, `target_kind = "block"`, `"menu"`, or `"inventory"`, a destination `target` selector, an effect, and a priority. Existing interaction entries and their `target_block` or `target_entity` fields remain unchanged. Both selectors use the existing `all`, `id`, `mod`, `tag`, `name`, and `#` tag-alias grammar and must remain paired as one compiled decision. A broad `all:*` rule and narrower higher-priority allow or exclude rule use the existing safe tie policy. The source selector resolves against the item stack being inserted. The destination selector resolves against an authoritative block ID for a block-backed inventory, registered menu type ID for a menu-backed inventory, or stable inventory-owner ID and tags supplied by a built-in or registered inventory-target resolver. A resolver must not use display text, client claims, Java class names, coordinates, or other unstable identities as serialized rule IDs.

The logical server owns inventory target resolution and the complete player transaction. An insertion is a player-initiated menu operation that increases a matching stack in a matched destination inventory. Removal-only operations remain allowed unless another existing rule denies them. Standard click, quick move or shift click, quick craft or drag, hotbar swap, and pickup-all or double-click paths must be classified by resulting transfer direction rather than blocked by click name alone. Supported paths must be denied before observable mutation or side effects. A fallback rollback is acceptable only when it restores every touched slot, carried stack, stack count, state ID, and client view and proves no irreversible callback or external side effect ran. Hopper, pipe, capability, and other automation with no authenticated initiating player retain current behavior. A supported integration may supply an authenticated `ServerPlayer`; it must never infer one from proximity or ownership. Rules recompile and swap atomically, and already-open menus use the new snapshot on the next transaction after reload.

Release tooling owns packaging, checksum generation, SBOM generation, source commit manifest creation, signed tag verification, and attestation validation. The shared MCEnvision workflow owns the attestation verifier implementation. The broker owns platform mutation and accepts only a release manifest authorized by EXT-003. No credentials, confirmation codes, or private release data may enter source, Git history, documentation, test fixtures, or logs.

## 12. Product Contract and Requirements

### Public contract and architecture

The public contract covers stage files, server configuration, commands, KubeJS and integration APIs, editor drafts, synchronized player state, in game UI, optional recipe viewers, Curios slots, player-initiated inventory transfers, and release artifacts. Inputs must be schema validated and authority checked before state changes. Identical stage state, rule revision, configuration revision, player relevant facts, source stack, destination kind, and destination identity must produce identical normalized rule decisions. Product fixes must generalize across integrated and dedicated servers, solo players, mixed multiplayer ownership, and supported optional mod combinations.

### CORE-REQ-001 — Freeze the polish baseline

**Behavior:** Freeze and reproduce or artifact-verify the six baseline reports, including stale tracking reports
**Owner:** RepositoryAudit
**Contributors:** IssueTracker
**Dependencies:** none
**Lifecycle stage:** readiness
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- Each of issues `#8`, `#10`, `#11`, `#16`, `#24`, and `#25` has an explicit reproduction, artifact verification, or evidence based stale classification
- The matrix records source revision, installed artifact identity, test configuration, expected behavior, observed behavior, and assigned requirement
- The audit identifies every public 3.0.3 capability gap proven by a shipped artifact inspection without promoting new features, and records the required downstream corrected-runtime proof without treating it as Phase 000 evidence
- The audit freezes the current interaction parser, compiled-rule, menu-transaction, target-catalog, editor serializer, and test seams that CORE-REQ-012 may change, without counting the owner-promoted feature as a seventh issue

**Required evidence**

- A versioned audit matrix tracing each baseline report to CORE-REQ-002 through CORE-REQ-007 or CORE-REQ-011
- A shipped 3.0.3 JAR inventory and public documentation comparison for advertised 3.0.3 capabilities, with no corrected candidate JAR or corrected-runtime assertion required before CORE-PHASE-003
- A current-state inventory insertion seam map covering legacy interactions, selector matching, destination identity, menu click paths, editor catalogs, and existing regression fixtures

### CORE-REQ-002 — Repair entity presence performance

**Behavior:** Repair entity-presence performance without changing multiplayer visibility, spawning, targeting, or lifecycle semantics
**Owner:** EntityPresenceEnforcer
**Contributors:** CompiledRuleEngine, MinecraftConditionContextFactory
**Dependencies:** CORE-REQ-001
**Lifecycle stage:** change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- Each player receives at most one immutable condition context for each server tick and authoritative state revision. Every tracked-entity decision in that tuple reuses the same context, while a same-tick rule or relevant-state revision invalidates the prior tuple before the next decision and creates at most one context for the new tuple
- A denied player cannot render, target, attack, or be targeted by a denied entity while an eligible nearby player retains normal entity behavior
- The controlled entity presence fixture stays under five percent of sampled server thread work and p95 MSPT increases no more than ten percent versus disabled enforcement

**Required evidence**

- Cache revision and invalidation tests covering every architecture boundary in §11, including a same-tick authoritative revision change that invalidates the prior context before the next decision
- A multiplayer dedicated server scenario with eligible and denied players observing the same entity
- Baseline and enabled server profile captures with p95 MSPT calculation instructions

### CORE-REQ-003 — Restore Curios slot gating

**Behavior:** Restore Curios slot gating with version-tolerant API resolution and absent-mod safety
**Owner:** CuriosBridge
**Contributors:** InventoryEnforcement
**Dependencies:** CORE-REQ-001, EXT-001
**Lifecycle stage:** change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- Curios 9.5.1 slot rules enforce the documented deny, ejection, or retention behavior without item duplication or loss
- Grant, revoke, reconnect, and reload transitions preserve authoritative inventory contents
- Core startup and dedicated server startup succeed with Curios absent

**Required evidence**

- EXT-001 artifact coordinates, hashes, compatibility proof, provenance review, and security review
- Curios present and Curios absent integration smoke tests with inventory conservation assertions

### CORE-REQ-004 — Make recipe viewers independent

**Behavior:** Make JEI and EMI independently configurable, optional, and simultaneously functional
**Owner:** RecipeViewerBridge
**Contributors:** JeIAdapter, EmiAdapter
**Dependencies:** CORE-REQ-001, EXT-001
**Lifecycle stage:** change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- JEI and EMI have independent enabled settings that default enabled for a missing setting
- JEI only, EMI only, both enabled, either viewer disabled, and neither viewer installed produce the documented visibility decision
- Optional viewer classes do not load in an installation where that viewer is absent

**Required evidence**

- Configuration migration and default behavior tests
- EXT-001 runtime matrix screenshots or logs for the supported client combinations

### CORE-REQ-005 — Verify Easy Builder enchantments

**Behavior:** Verify or repair published Easy Builder enchantment controls in the production editor bundle and final JAR
**Owner:** EasyBuilder
**Contributors:** EnchantmentCompiler
**Dependencies:** CORE-REQ-001
**Lifecycle stage:** change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- The production editor creates, edits, removes, validates, saves, and reopens each supported enchantment restriction
- Easy Builder output round trips through TOML source and compiles to the same normalized runtime rule
- The packaged JAR serves the verified editor bundle and reports field specific validation errors before apply

**Required evidence**

- Browser or editor tests for form state, serialization, validation, and source round trip
- Operator apply and client synchronization smoke test using the final JAR

### CORE-REQ-006 — Verify category menu depth

**Behavior:** Verify or repair category menu depth in the in-game progression map without regressions to navigation and inspector behavior
**Owner:** StageTreeScreen
**Contributors:** ClientRenderTests
**Dependencies:** CORE-REQ-001
**Lifecycle stage:** change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- The open category overlay renders above every stage item icon at supported GUI scales
- Menu entries receive pointer input before stage nodes and prevent click through
- Closing or selecting a category preserves map pan, zoom, search, navigation, and inspector behavior

**Required evidence**

- Render order and input routing regression tests
- Client smoke procedure with screenshots at default and nondefault GUI scales

### CORE-REQ-007 — Correct proven advertised capability gaps

**Behavior:** Implement any previously advertised 3.0.3 user-visible capability that the polish matrix proves missing from a shipped artifact
**Owner:** ArtifactParity
**Contributors:** Documentation
**Dependencies:** CORE-REQ-001
**Lifecycle stage:** change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- Every correction cites a CORE-REQ-001 matrix row and a public 3.0.3 documentation or release source
- Each corrected capability is present in the final JAR and passes its documented runtime workflow
- No correction introduces an unpromised product feature or platform upgrade

**Required evidence**

- Per capability regression evidence and final artifact inventory
- Documentation changes limited to behavior that the final JAR proves

### CORE-REQ-011 — Preserve canonical recipe-lock serialization

**Behavior:** Correct the Easy Builder recipe-output lock round trip so the visual form writes `[recipes].locked_items`, the server rejects or safely migrates invalid editor drafts before mutation, the persisted stage file reloads into the same normalized rule, and runtime recipe enforcement applies it without deleting a valid rule
**Owner:** EasyBuilder
**Contributors:** EditorDraftValidator, StageFileParser, Schema4StageCompiler, RecipeEnforcer
**Dependencies:** CORE-REQ-001
**Lifecycle stage:** change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- Creating or editing the issue `#25` recipe-output rule in Easy Builder writes exactly `[recipes].locked_items` and never reports success with a generic `[recipes].locked` field
- The same selected output-item selectors and priorities survive visual form, TOML source, draft save, review, confirmed apply, persisted stage file, server reload, compiler normalization, editor reopen, and client synchronization without semantic drift or disappearance
- A player who lacks the stage cannot craft every recipe producing the locked item after apply and reload, while an eligible player retains normal crafting behavior
- An invalid or legacy editor draft containing `[recipes].locked` is either migrated without ambiguity to `[recipes].locked_items` or rejected atomically with a field-specific error before live-file mutation; the last valid draft, persisted rule, compiled snapshot, and synchronized runtime state remain intact
- Exact recipe-identifier locks continue to use `[recipes].locked_ids` and are not silently converted into output-item locks

**Required evidence**

- Frontend regression tests covering create, edit, remove, save, reopen, source round trip, priority preservation, and invalid or legacy draft handling for recipe-output locks
- Server validation and compiler tests proving canonical-field admission, unknown alias rejection or deterministic migration, atomic apply rollback, and normalized rule equality before and after persistence and reload
- A packaged-JAR operator workflow showing visual form to TOML to compiler to persisted file to reload to runtime recipe enforcement, with denied and eligible players and evidence that the served production bundle matches the built artifact
- Before-and-after fixture digests proving a rejected or failed migration does not delete or alter the last valid recipe rule

### CORE-REQ-012 — Gate player-initiated inventory insertion

**Behavior:** Add a destination-aware interaction rule that restricts a selected source item from being placed into a selected target inventory while the initiating player lacks the owning stage, with priority-based allows and exclusions, server-authoritative transaction enforcement, complete Easy Builder and TOML round trips, and compatible modded-container coverage
**Owner:** InventoryInsertionEnforcer
**Contributors:** InteractionRuleCompiler, InventoryTargetResolverRegistry, EasyBuilder, EditorCatalogService, MenuTransactionTests
**Dependencies:** CORE-REQ-001
**Lifecycle stage:** change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- The canonical `[[interactions]]` form accepts `type = "item_into_inventory"`, `held_item`, `target_kind`, `target`, `effect`, and `priority`; existing `block_right_click`, `item_on_block`, and `item_on_entity` entries load and behave unchanged
- `held_item` and `target` independently support `all:*`, `id:`, `mod:`, `tag:`, `name:`, and `#` selectors through the existing matcher registry, while the compiler preserves the pair as one rule and never broadens either axis into an independent single-selector decision
- `target_kind = "block"` resolves a block-backed inventory by registered block ID, `target_kind = "menu"` resolves its registered menu type, and `target_kind = "inventory"` resolves a stable built-in or registered inventory-owner identity and tags; unresolved or ambiguous destinations produce bounded diagnostics and never trust a client-supplied identity
- A player missing the owning stage is denied when both selectors and the active condition match. Owning the stage restores access. A narrower higher-priority allow or exclude defeats a broad lock, `all:*` works on either selector axis, and equal-priority conflicts use the existing safe tie policy
- The rule covers standard click placement, quick move or shift click, quick craft or drag across multiple slots, hotbar-number swap, and pickup-all or double-click. A removal-only operation is not misclassified as insertion, and a multi-slot transaction is accepted or denied as one coherent operation
- A denied transaction leaves every source slot, destination slot, carried stack, hotbar stack, stack count, menu state ID, and synchronized client view equivalent to the pre-transaction state. It emits no crafting, trade, advancement, sound, statistic, callback, or external inventory side effect and cannot lose, duplicate, split, or ghost an item
- Enforcement uses the authenticated initiating `ServerPlayer`, authoritative stage snapshot, authoritative menu, and server-resolved destination. Two players with different stage ownership may use the same open container concurrently and receive independent decisions without corrupting shared contents
- Hopper, pipe, capability, machine, and other automation without an authenticated initiating player remain unaffected. An integration may pass a real player origin through the registered server-side extension contract, but the implementation never guesses a player from proximity, last opener, owner metadata, or client input
- Chest and furnace inventories, the player inventory where explicitly selected, and a non-vanilla test container using supported `AbstractContainerMenu` and `Slot` contracts enforce consistently. Compatible modded targets can contribute stable inventory identities, tags, and editor catalog entries without a hard dependency
- Rule reload is atomic. Existing open menus use the newly compiled decision on the next player transaction, an invalid rule keeps the last valid compiled snapshot and persisted files, and reconnect, dimension change, server restart, and integrated-server lifecycle do not create a bypass
- Easy Builder exposes a plain-language “Put item into inventory” action with separate source-item and destination fields, target-kind choice, filtered item, block, menu, and inventory-owner autocomplete, selector mode, effect, priority, conditions, and priority exceptions. Create, edit, duplicate, remove, review, apply, reopen, TOML source, and packaged-bundle workflows preserve identical semantics
- Validation and runtime diagnostics identify the invalid source field, destination kind, destination selector, unsupported resolver, or denied rule without leaking private state or spamming the player. The explain API and operator evidence show the winning rule, both matched selectors, priority, stage state, and destination identity

**Required evidence**

- Parser, compiler, selector-pair, resolver-registry, priority, safe-tie, legacy-interaction, validation, and normalized-rule round-trip tests for every source and destination selector mode
- A server transaction matrix for standard click, shift click, drag, hotbar swap, and double click with accepted and denied cases, before-and-after slot and carried-stack snapshots, packet synchronization checks, and explicit item-conservation and no-side-effect assertions
- Dedicated and integrated server scenarios covering chest, furnace, player inventory when selected, a compatible custom menu fixture, two players with different stages sharing one container, reload while a menu is open, reconnect, restart, and malformed-rule rollback
- Automation proof showing hopper and playerless capability insertion remains unchanged, plus an authenticated player-origin integration test proving the server-side extension path enforces the rule
- Browser tests and a final-JAR operator workflow covering Easy Builder creation through persisted TOML, confirmed apply, server reload, editor reopen, autocomplete catalogs, field-specific errors, runtime denial, eligible-player success, and production editor bundle identity
- Documentation with canonical TOML, Easy Builder, chest, furnace, broad-lock with priority-exception, profession selling-bin, compatible modded-container resolver, automation boundary, diagnostics, migration, and troubleshooting examples

### CORE-REQ-008 — Preserve compatibility and security

**Behavior:** Preserve supported configuration, stage schema, saved data, commands, APIs, and multiplayer compatibility
**Owner:** CompatibilityHarness
**Contributors:** NetworkValidation, ConfigurationSchema
**Dependencies:** CORE-REQ-002, CORE-REQ-003, CORE-REQ-004, CORE-REQ-005, CORE-REQ-006, CORE-REQ-007, CORE-REQ-011, CORE-REQ-012
**Lifecycle stage:** post_change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- Existing supported stage packs, configuration files, saved player stage data, commands, and public API calls remain loadable and behaviorally compatible
- Existing canonical `[recipes].locked_ids` and `[recipes].locked_items` rules remain behaviorally distinct and survive editor inspection, apply, reload, and restart without data loss
- Existing interaction rules remain source compatible, and the new inventory insertion form preserves unknown namespaced extension data, priorities, comments where the established preservation layer supports them, and the last valid compiled snapshot across editor apply and reload
- Playerless automation and container behavior without an inventory insertion rule remain byte-for-byte or behaviorally equivalent at the public boundary, while mixed-stage multiplayer decisions remain isolated to the initiating player
- Editor and client packets require operator authorization and schema validation before a server mutation
- Integrated server, dedicated server, reconnect, reload, optional integration, and multiplayer ownership paths pass the compatibility matrix

**Required evidence**

- Formatter, static analysis, unit test, GameTest, build, dedicated server, client, multiplayer, and JAR inspection results
- Configuration compatibility fixtures, legacy and new interaction fixtures, menu transaction conservation tests, and packet authorization regression tests

### CORE-REQ-009 — Repair shared attestation verification

**Behavior:** Repair and prove the shared release attestation verifier for the 3.0.4 release artifact
**Owner:** ReleaseValidation
**Contributors:** SharedWorkflow
**Dependencies:** CORE-REQ-008, EXT-002
**Lifecycle stage:** post_change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- The pinned shared workflow revision verifies build provenance and SBOM attestations using compatible GitHub CLI flags
- A disposable release candidate passes the reusable workflow and a tampered candidate fails verification
- Release validation produces matching signed artifact, checksum, SBOM, source manifest, and attestation evidence

**Required evidence**

- EXT-002 merged revision identity and successful reusable workflow run
- Attestation success and tamper failure records for a disposable release candidate

### CORE-REQ-010 — Release and close the baseline

**Behavior:** Build, integrate, attest, document, publish, and verify 3.0.4, then close the six baseline issues with evidence
**Owner:** ReleaseBroker
**Contributors:** ReleaseDocumentation
**Dependencies:** CORE-REQ-009
**Lifecycle stage:** post_change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- The verified 3.0.4 patch is merged into master with a signed integration commit and signed annotated release tag
- The publication preview matches the final JAR SHA-256, SHA-512, source commit, metadata, release notes, platforms, and dependencies
- The final artifact contains the verified CORE-REQ-012 schema, editor bundle, target catalogs, resolver extension point, server enforcement, tests, and documentation, and its downloaded platform copies preserve the same inventory insertion behavior
- CurseForge and Modrinth downloads hash match the verified artifact and all six baseline issues close with merged revision acceptance evidence

**Required evidence**

- Pull request merge record, signed commit verification, signed tag verification, JAR listing including CORE-REQ-012 resources and classes, checksums, SBOM, source manifest, and attestation verification
- EXT-003 confirmation, platform URLs, downloaded artifact hash checks, and issue closure evidence

## 13. Phased Roadmap

The master owns the global order and concise phase catalog. Every required execution blueprint is linked below and registered in the deterministic plan manifest.

| Phase ID | Objective | Owner | Dependencies | Canonical requirements | Entry summary | Exit summary | Next transition | Blueprint path |
|---|---|---|---|---|---|---|---|---|
| CORE-PHASE-000 | Freeze the defect, inventory-interaction seam, and artifact baseline | RepositoryAudit | none | CORE-REQ-001 | Baseline revision, six-issue set, and SRC-009 feature request are pinned | Audit classifies every mandatory report and advertised capability claim and records the current CORE-REQ-012 implementation seams | CORE-PHASE-001 | [phases/plan-phase-000.md](phases/plan-phase-000.md) |
| CORE-PHASE-001 | Repair entity presence performance | EntityPresenceEnforcer | CORE-PHASE-000 | CORE-REQ-002 | CORE-REQ-001 audit identifies the hot path fixture | CORE-REQ-002 correctness, same-tick revision invalidation, and DEC-004 performance evidence pass through signed integration | CORE-PHASE-002 | [phases/plan-phase-001.md](phases/plan-phase-001.md) |
| CORE-PHASE-002 | Restore optional integration behavior | OptionalIntegrations | CORE-PHASE-001, EXT-001 | CORE-REQ-003, CORE-REQ-004 | CORE-PHASE-001 is integrated with signed completion evidence and EXT-001 artifact contract is complete | Curios, JEI, and EMI compatibility matrix passes | CORE-PHASE-003 | [phases/plan-phase-002.md](phases/plan-phase-002.md) |
| CORE-PHASE-003 | Complete editor serialization, inventory insertion gating, client UI, and artifact parity | EasyBuilder | CORE-PHASE-002 | CORE-REQ-005, CORE-REQ-006, CORE-REQ-007, CORE-REQ-011, CORE-REQ-012 | CORE-PHASE-002 is integrated with signed completion evidence, the audit assigns source-owned corrections, and the inventory interaction seams are frozen | Production-bundle, corrected-runtime, candidate-JAR, and signed Phase 003 integration evidence pass for editor controls, recipe serialization, two-selector `item_into_inventory` rules, transaction enforcement, UI, and proven artifact parity | CORE-PHASE-004 | [phases/plan-phase-003.md](phases/plan-phase-003.md) |
| CORE-PHASE-004 | Prove compatibility and regression safety | CompatibilityHarness | CORE-PHASE-003 | CORE-REQ-008 | All sequential component changes, including CORE-REQ-012, are integrated with signed completion evidence | Full compatibility, inventory-conservation, automation, multiplayer, and security verification passes | CORE-PHASE-005 | [phases/plan-phase-004.md](phases/plan-phase-004.md) |
| CORE-PHASE-005 | Integrate and validate the release artifact | ReleaseValidation | CORE-PHASE-004, EXT-002 | CORE-REQ-009 | Shared verifier repair is merged and pinned | Signed master artifact and release validation evidence pass | CORE-PHASE-006 | [phases/plan-phase-005.md](phases/plan-phase-005.md) |
| CORE-PHASE-006 | Publish the verified patch and close issues | ReleaseBroker | CORE-PHASE-005 | CORE-REQ-010 | The signed Phase 005 artifact is frozen; read-only preview work may begin while EXT-003 remains unresolved | EXT-003 authorizes only the exact preview, both platforms verify, and the six-issue completion endpoint and Definition of Done pass with no known mandatory phase owned defect remaining | final completion | [phases/plan-phase-006.md](phases/plan-phase-006.md) |

## 14. Verification Strategy

### Architecture and verification strategy

| Requirement | Unit | Integration | Real behavior | Security | Artifact or runtime |
|---|---|---|---|---|---|
| CORE-REQ-001 | Matrix consistency checks | Source, issue, documentation, and JAR reconciliation | Reproduce or artifact verify all six reports | Sanitized logs and screenshots | Versioned issue matrix |
| CORE-REQ-002 | Snapshot, cache, and same-tick revision-invalidation tests | Two player server scenario | Profiled entity tracking and p95 MSPT fixture | Player, tick, and authoritative-state-revision cache isolation | Spark or equivalent profile capture |
| CORE-REQ-003 | API resolver and inventory conservation tests | Curios present and absent server smoke tests | Grant, revoke, reconnect, and reload | Optional class loading isolation | EXT-001 artifact matrix |
| CORE-REQ-004 | Independent configuration default tests | JEI only, EMI only, both, disabled, absent | Stage visibility update in each viewer | Optional class loading isolation | EXT-001 client runtime matrix |
| CORE-REQ-005 | Form, serializer, and compiler round trip tests | Operator apply and client sync | Packaged editor bundle workflow | Operator authority and malformed draft rejection | Final JAR browser smoke |
| CORE-REQ-006 | Render order and input routing tests | Populated stage map | GUI scale client smoke | Client side only class boundary | Screenshots and procedure |
| CORE-REQ-007 | Per gap regression tests | JAR resource inspection | Documented user workflow | Scope trace to SRC-001 | Artifact parity matrix |
| CORE-REQ-011 | Canonical-field, serializer, parser, compiler, and atomicity tests | Visual form to TOML to persisted file to reload and editor reopen | Packaged-JAR operator apply plus denied and eligible player crafting workflow | Unknown alias, ambiguous legacy draft, failed reload, rollback, and valid-rule preservation | Issue #25 browser, file-digest, compiler, reload, and runtime packet |
| CORE-REQ-012 | Two-selector parser, compiler, priority, resolver, and transaction tests | Chest, furnace, player inventory, custom menu, mixed-player, reload, and automation matrix | Packaged-JAR Easy Builder to TOML to live menu workflow for denied and eligible players | Server-only destination identity, authenticated player origin, atomic denial, no side effects, and item conservation | Browser bundle, dedicated and integrated server logs, slot-state snapshots, packet traces, and JAR inspection |
| CORE-REQ-008 | Existing and targeted regression suite | Reconnect, reload, multiplayer, optional mod, legacy interaction, and inventory-transfer matrix | Dedicated and integrated server smoke | Packet, editor authorization, destination identity, and transaction review | Build and final diff inspection |
| CORE-REQ-009 | Checksum and metadata checks | Shared reusable workflow | Disposable release candidate verification | Attestation success and tamper failure | Signed JAR, SBOM, and source manifest |
| CORE-REQ-010 | Release manifest and six-issue equivalence checks | Broker preview validation | Download each platform artifact and verify six closure packets | EXT-003 authorization binding | Platform URLs, hashes, and issue closure evidence |

All changed Java, Gradle, resource, configuration, networking, client, and optional integration paths run the applicable formatter, static analysis, unit tests, GameTests, `./gradlew build`, dedicated server smoke test, client smoke test, multiplayer or reconnect verification, and final JAR inspection. A failed gate blocks the owning phase and cannot be replaced by a lower fidelity claim.

## 15. Compatibility, Migration, Rollout, and Recovery

### State, compatibility, migration, and recovery

3.0.4 is a compatible patch. Existing stage identifiers, `pack:stage` parsing, canonical TOML schema, configuration keys, commands, persisted player stage data, packets, public APIs, editor drafts, and default behavior remain supported. The new independent JEI setting must default enabled when absent so existing configurations retain their expected recipe viewer behavior. EMI configuration must remain readable. Canonical recipe-output locks continue to use `[recipes].locked_items`, while exact recipe-identifier locks continue to use `[recipes].locked_ids`. Existing `[[interactions]]` entries retain their fields and behavior. The additive `item_into_inventory` form has no effect until configured, and a pack that does not use it retains existing player and automation transfer behavior.

### Compatibility and architecture

The editor-produced generic `[recipes].locked` form is not promoted into the public schema. On draft load, save, review, or apply, an unambiguous legacy value whose editor intent is recipe-output locking may migrate to `[recipes].locked_items` while preserving selectors, priorities, comments where the established preservation layer supports them, and the last valid rule. An ambiguous value must be rejected with a field-specific correction message before any live-file mutation. Validation, backup, atomic write, reload, compiler activation, and synchronization form one transaction: a failure restores the prior persisted file, compiled snapshot, and player-visible state. Inventory insertion rules use the additive canonical form in §11 and CORE-REQ-012; no legacy interaction is auto-converted, and invalid two-selector entries are rejected before live mutation. No Minecraft, NeoForge, Java, Gradle, mappings, persistence format, or unrelated schema upgrade is in scope.

Entity presence snapshots are transient server state and never enter persistent player data. A snapshot invalidates and rebuilds from authoritative server state after every relevant state revision, including a revision that occurs during the same server tick, plus reload, reconnect, dimension change, or restart. Curios transitions must conserve inventory contents. A failed optional adapter must disable only its adapter boundary and not alter core rule decisions.

Rollout order is Phase 000 audit, signed integration of Phase 001, Phase 002 optional-integration work, sequential remaining source changes including the issue `#25` editor transaction and CORE-REQ-012 inventory insertion feature, compatibility proof, signed master integration, release validation, broker preview, EXT-003 confirmation, dual platform publication, and downloaded hash verification. Before publication, recovery is a corrective change on the appropriate sequential phase branch followed by the complete verification set. A failed editor migration or apply restores the last valid recipe rule and inventory rule snapshot and requires the full visual-form-to-runtime workflow to be rerun. A failed denied inventory transaction must resynchronize from the authoritative pre-transaction state before the player continues using the menu. After publication, recovery follows the rollback or unpublish policy bound by EXT-003 and requires a new verified artifact for any replacement.

## 16. Documentation, Operations, and Release Gates

- Update `README.md`, `DOCUMENTATION.md`, the documentation index, and affected references only for verified 3.0.4 behavior
- Document Curios support, absent mod behavior, JEI and EMI independent settings, defaults, and coexistence behavior
- Document entity presence snapshot invalidation, profiling fixture, performance thresholds, and diagnostic collection procedure
- Document Easy Builder enchantment controls, TOML equivalence, category overlay behavior, and troubleshooting workflows
- Document the recipe-lock choice between `locked_items` and `locked_ids`, the canonical Easy Builder output, legacy draft handling, atomic rejection and recovery, reload expectations, and an end-to-end worked example
- Document the canonical `item_into_inventory` interaction, both selector axes, target kinds and resolver IDs, `all:*`, priority allows and exclusions, stage ownership behavior, click and transfer coverage, playerless automation boundary, multiplayer behavior, live reload, diagnostics, and item-conservation guarantees
- Include Easy Builder and TOML examples for a miner stage controlling ore insertion into a profession selling bin, broad chest restrictions with a higher-priority exception, furnace input gating, menu-type targeting, and a compatible modded inventory resolver
- Produce issue closure evidence that references the merged revision and user visible verification for each baseline issue
- Require a clean final diff, signed master integration, signed tag, JAR inspection, SHA-256, SHA-512, SBOM, source manifest, and verified attestations
- Require EXT-002 before release validation is accepted and EXT-003 before platform publication begins

## 17. Risks and Failure Boundaries

### Failure, recovery, and risk boundaries

| Risk | Impact | Prevention | Detection | Recovery |
|---|---|---|---|---|
| Snapshot invalidation omits a relevant player fact or same-tick revision | Stale allow or deny decision | Player, server-tick, and authoritative-state-revision keyed snapshots plus mutation tests | Direct decision comparison and multiplayer smoke | Invalidate the prior tuple before the next decision, correct the boundary, and rerun CORE-REQ-002 evidence |
| Performance cache changes shared entity behavior | Incorrect visibility or targeting | Preserve core rule resolution and test mixed player ownership | Two player entity fixture | Revert semantic change and retain the profiling harness |
| Curios API drift links optional classes | Startup failure or broken slots | Version tolerant bridge and absent mod boundary | Present and absent integration matrix | Correct adapter resolution without changing core rules |
| JEI and EMI conflict in combined clients | Incorrect recipe display | Independent adapters and settings | Full recipe viewer matrix | Restore isolated adapter behavior and add regression test |
| Editor source diverges from Easy Builder | Invalid or surprising saved rules | Shared normalized schema and round trips | Operator apply and compiler equivalence test | Reject invalid draft and repair serializer |
| Recipe lock serializes to a noncanonical alias | Rule disappears, compiles as no lock, or overwrites valid intent | One field mapping for recipe-output locks plus server-side canonical-key validation | Visual-to-TOML-to-file-to-reload-to-runtime fixture and persisted-file inspection | Migrate only unambiguous editor drafts or reject atomically while restoring the prior valid file and compiled snapshot |
| Inventory insertion checks only the clicked slot | Shift click, drag, hotbar swap, or modded menu path bypasses the rule | Direction-aware transaction classification across every supported click path | Transfer matrix and custom-menu fixture | Correct the shared transaction boundary and rerun all CORE-REQ-012 conservation evidence |
| Denied transaction mutates or rolls back incompletely | Item loss, duplication, ghost stacks, or irreversible callbacks | Pre-mutation denial and whole-transaction snapshots where fallback is proven safe | Slot, carried-stack, packet, side-effect, and total-count assertions | Restore authoritative state, remove unsafe fallback, and block Phase 004 until conservation proof passes |
| Destination identity is unstable or client controlled | Rules miss modded containers or can be bypassed | Server-side block, menu, and registered inventory-owner resolvers with stable IDs | Resolver tests, malformed payload tests, and explain traces | Reject ambiguous rules or targets without mutating state and add a stable resolver contract |
| Playerless automation is attributed to a player | Hoppers, pipes, or machines stop unpredictably | Require an authenticated player transaction origin | Automation matrix with nearby staged and unstaged players | Remove inferred identity and preserve the playerless path |
| Category overlay regresses at another GUI scale | Icons appear over menu or clicks leak | Render depth and input capture tests | Scale screenshot procedure | Correct client render order and rerun CORE-REQ-006 |
| Shared verifier remains incompatible | Release cannot be attestation verified | EXT-002 blocks Phase 005 | Release workflow failure | Remain NOT COMPLETE — EXTERNALLY BLOCKED and do not bypass validation |
| Broker confirmation becomes stale | Wrong artifact publication | EXT-003 binds artifact identities and runbook digest | Preview hash mismatch | Stop publication and request a new scoped confirmation |

## 18. Definition of Done

**Plan completion status:** NOT COMPLETE — EXTERNALLY BLOCKED

- A signed and verified 3.0.4 patch is merged into master, published to CurseForge and Modrinth, and all six baseline GitHub issues are closed with merged-revision acceptance evidence.
- Every CORE-REQ-001 through CORE-REQ-012 acceptance criterion and required evidence gate passes at the defined fidelity
- The final JAR, SHA-256, SHA-512, SBOM, source manifest, signed integration, signed tag, and attestation evidence identify the same release artifact
- Corrected MCEnvision shared release-validation workflow revision is complete and demonstrates compatible attestation verification under EXT-002
- Owner-approved platform publication confirmation for the final 3.0.4 artifact is complete and binds the final publication under EXT-003
- While EXT-002 or EXT-003 remains unsatisfied, the plan remains NOT COMPLETE — EXTERNALLY BLOCKED and no publication or issue closure may claim completion
- FUT-001, FUT-002, FUT-003, and FUT-004 remain excluded

## 19. Goal Creator Handoff

Mandatory boundary: Resolve issues #8, #10, #11, #16, #24, and #25, implement the explicit CORE-REQ-012 inventory insertion feature, and correct previously advertised or documented 3.0.3 capabilities proven missing by the CORE-PHASE-000 artifact audit. Do not count CORE-REQ-012 as a seventh issue.
Optional/future disposition: excluded
Locked owner decisions: DEC-001, DEC-002, DEC-003, DEC-004, DEC-005, DEC-006, DEC-007
Active phase: CORE-PHASE-000
Next executable action: Freeze the six issue reproductions, advertised capability audit, and CORE-REQ-012 interaction, menu, editor, catalog, and test seams against saved goal checkout 5b3077764907249b3711886cca538794f6139acf
Known failing checks: GitHub Actions run 31453460136 job 93662356066 fails shared attestation verification because incompatible GitHub CLI signer flags are combined; issue #25 reports an ineffective visual recipe lock whose exact production-bundle representation must be recorded by CORE-PHASE-000 before CORE-PHASE-003 corrects it
Known external blockers: Corrected MCEnvision shared release-validation workflow revision under EXT-002 and Owner-approved platform publication confirmation for the final 3.0.4 artifact under EXT-003
Completion endpoint: A signed and verified 3.0.4 patch is merged into master, published to CurseForge and Modrinth, and all six baseline GitHub issues are closed with merged-revision acceptance evidence.
Required evidence gates: CORE-REQ-001 audit and inventory-interaction seam matrix, DEC-004 performance profile, EXT-001 compatibility matrix, CORE-REQ-005 editor bundle proof, CORE-REQ-006 client UI proof, CORE-REQ-011 visual-form-to-runtime recipe-lock proof with preservation and rollback evidence, CORE-REQ-012 two-selector schema, Easy Builder, destination resolver, click-path, transaction-conservation, automation, multiplayer, reload, custom-menu, diagnostics, documentation, and final-JAR proof, CORE-REQ-008 regression suite, EXT-002 reusable workflow proof, signed master integration and tag, checksums, SBOM, source manifest, attestation verification, EXT-003 confirmation, platform URLs, downloaded artifact hashes, and six issue closure packets

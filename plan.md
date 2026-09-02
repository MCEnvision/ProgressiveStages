# ProgressiveStages 3.0.4 Polish and Release Closure Plan

> **Plan ID:** PLAN-MASTER
> **Plan status:** VALIDATED WITH KNOWN EXTERNAL BLOCKER
> **Project state:** EXISTING
> **Planning subject:** ProgressiveStages 3.0.4 polish and release closure
> **Plan profile:** software_product

## 1. Project Identity

```text
Project: ProgressiveStages NeoForge mod
Requested artifact: authoritative_plan
Repository root: /tmp/ProgressiveStages-polish-plan
Starting branch: envy/polish-3.0.4-plan
Starting commit: ab4ad138e1f74eec82cf0392a47ac1e54dd66d01
Authoritative remote:
origin
https://github.com/MCEnvision/ProgressiveStages.git
Remote ref: origin/master
Remote commit: ab4ad138e1f74eec82cf0392a47ac1e54dd66d01
Package metadata: mod_id progressivestages, version 3.0.3, Minecraft 1.21.1, NeoForge 21.1.219
Target release: 3.0.4
```

## 2. Planning Subject and Source Roles

| ID | Role | Subject | Source | Intended use |
|---|---|---|---|---|
| SRC-001 | owner_request | bounded 3.0.4 polish plan for active issues and missing advertised capabilities | EnVy direct Plan Creator invocation and locked answers on 2026-09-01 | defines completion endpoint, issue scope, feature-completeness boundary, recipe-viewer behavior, and performance gate |
| SRC-002 | existing_plan | legacy ProgressiveStages roadmap | /tmp/ProgressiveStages-polish-plan/plan.md at origin/master ab4ad138e1f74eec82cf0392a47ac1e54dd66d01 | migration source and deferred historical scope |
| SRC-003 | repository_evidence | current ProgressiveStages implementation, documented 3.0.3 contract, build metadata, tests, and workflows | README.md, DOCUMENTATION.md, ARCHITECTURE.md, TESTING.md, Gradle metadata, source, tests, and workflows on origin/master | establishes supported platform, public behavior, architecture, and verification boundaries |
| SRC-004 | review_feedback | open defect reports | GitHub issues #8, #10, #11, #16, and #24 on MCEnvision/ProgressiveStages | defines mandatory defect outcomes and reproduction evidence |
| SRC-005 | status | release and continuous integration state | origin/master, v3.0.3 release history, open Dependabot PR #19, and successful master quality workflow | distinguishes release quality evidence from optional dependency maintenance |
| SRC-006 | audit_evidence | issue #24 Spark profile and configuration | GitHub issue #24 attachment and report | establishes entity-presence context construction as the performance hotspot |
| SRC-007 | audit_evidence | failed v3.0.3 release validation | GitHub Actions run 31453460136, job 93662356066 | establishes the shared attestation-verification workflow as a release prerequisite |

The planning subject is the ProgressiveStages 3.0.4 polish and release closure. Source artifacts provide scope, current state, and evidence. They do not replace the authoritative plan or add scope beyond the locked intake.

## 3. Purpose and Intended Outcome

ProgressiveStages needs a bounded polish release that converts the five active reports into verified outcomes while preserving the existing NeoForge 1.21.1 stage model. Server administrators need entity presence rules that do not dominate the server thread. Pack authors need Curios, JEI, and EMI behavior that survives supported optional dependency combinations. Operators need the Easy Builder controls and in game category menu to match published behavior in the installed artifact.

The intended outcome is the exact completion endpoint recorded in §18 and §19. The work is limited to the issue baseline and only those advertised or documented 3.0.3 capabilities that the Phase 000 audit proves missing from a shipped artifact.

## 4. Evidence-Based Current State

| Area | Evidence class | Finding | Evidence |
|---|---|---|---|
| Repository baseline | VERIFIED | The locked implementation baseline is `origin/master` at commit `ab4ad138e1f74eec82cf0392a47ac1e54dd66d01` | Git revision inspection recorded in SRC-002 |
| Runtime contract | VERIFIED | The supported release target is ProgressiveStages for Minecraft 1.21.1 on NeoForge 21.1.219 | Gradle metadata and mod metadata inspection described by SRC-003 |
| Issue baseline | VERIFIED | The active issue baseline is exactly `#8`, `#10`, `#11`, `#16`, and `#24` | GitHub issue inspection in SRC-004 |
| Entity presence cost | OBSERVED | The reported hotspot constructs Minecraft rule context through the entity tracking decision path | Spark profile and configuration in SRC-006 |
| Curios integration | OBSERVED | The current slot gate needs version tolerant resolution for the Curios 9.5.1 API surface | Issue `#8` and implementation evidence in SRC-003 |
| Recipe viewers | VERIFIED | Existing configuration exposes EMI controls but does not prove independent JEI and EMI behavior in combined installations | Configuration inspection and optional integration evidence in SRC-003 |
| Easy Builder enchantments | UNKNOWN | Existing source and tests require final editor bundle and JAR workflow verification | Issue `#11`, SRC-003, and Phase 000 audit |
| Category menu depth | UNKNOWN | Source ordering changes require production map verification at supported GUI scales | Issue `#16`, SRC-003, and Phase 000 audit |
| Artifact parity | UNKNOWN | Published 3.0.3 documentation has not been fully reconciled with the shipped JAR | SRC-002 and CORE-REQ-007 |
| Release validation | VERIFIED | The 3.0.3 release validation fails during shared attestation verification because its GitHub CLI signer flags are incompatible | Failed GitHub Actions run `31453460136`, job `93662356066` in SRC-007 |

## 5. Product Contract and Profile Coverage

| Profile area | Status | Source | Contract location | Rationale |
|---|---|---|---|---|
| Inputs and outputs | covered | SRC-001 | Product Contract and Requirements | Stage files, commands, APIs, GUI, editor, optional integrations, and release artifacts are public product surfaces. |
| Component architecture | covered | SRC-003 | Architecture and Ownership Boundaries | The plan must preserve server-authoritative rules, client presentation, optional integration isolation, and editor bundle boundaries. |
| State and persistence | covered | SRC-003 | Architecture and Ownership Boundaries | Stage ownership, condition contexts, caches, server snapshots, and configuration must remain compatible. |
| Failure taxonomy | covered | SRC-004 | Risks and Failure Boundaries | Reported defects span performance, optional integration absence, artifact drift, GUI depth, and release automation failure. |
| Versioning | covered | SRC-001 | Compatibility, Migration, Rollout, and Recovery | The endpoint is a compatible 3.0.4 patch without platform or schema upgrades. |
| Security | covered | SRC-003 | Architecture and Ownership Boundaries | Editor authority, client packets, optional reflection, release provenance, and no-secret handling remain mandatory. |
| Test system | covered | SRC-004 | Verification Strategy | Every issue requires focused regression evidence plus integrated, multiplayer, optional-mod, performance, and artifact proof. |
| Release lifecycle | external_prerequisite | EXT-002 | Documentation, Operations, and Release Gates | The current shared attestation verifier fails and must be repaired upstream before 3.0.4 publication. |
| Generalization | covered | SRC-003 | Compatibility, Migration, Rollout, and Recovery | Fixes must work for integrated and dedicated servers, solo and mixed multiplayer ownership, and optional-mod combinations. |
| Determinism | covered | SRC-006 | Architecture and Ownership Boundaries | Rule snapshot revision and player-relevant state must determine cached presence decisions and invalidation. |

## 6. Mandatory Scope

- CORE-REQ-001 — Freeze and reproduce or artifact-verify the five baseline reports, including stale tracking reports
- CORE-REQ-002 — Repair entity-presence performance without changing multiplayer visibility, spawning, targeting, or lifecycle semantics
- CORE-REQ-003 — Restore Curios slot gating with version-tolerant API resolution and absent-mod safety
- CORE-REQ-004 — Make JEI and EMI independently configurable, optional, and simultaneously functional
- CORE-REQ-005 — Verify or repair published Easy Builder enchantment controls in the production editor bundle and final JAR
- CORE-REQ-006 — Verify or repair category menu depth in the in-game progression map without regressions to navigation and inspector behavior
- CORE-REQ-007 — Implement any previously advertised 3.0.3 user-visible capability that the polish matrix proves missing from a shipped artifact
- CORE-REQ-008 — Preserve supported configuration, stage schema, saved data, commands, APIs, and multiplayer compatibility
- CORE-REQ-009 — Repair and prove the shared release attestation verifier for the 3.0.4 release artifact
- CORE-REQ-010 — Build, integrate, attest, document, publish, and verify 3.0.4, then close the five baseline issues with evidence

## 7. Optional / Future Scope

Every item in this section is excluded and non-blocking for this plan.

- FUT-001 — The legacy roadmap's unimplemented feature phases beyond advertised 3.0.3 behavior — excluded
- FUT-002 — Dependabot pull request #19 unless it becomes security or compatibility blocking evidence — excluded
- FUT-003 — New feature requests opened after the five-issue audit baseline — excluded
- FUT-004 — Minecraft, NeoForge, Java, Gradle, or mapping upgrades — excluded

## 8. Non-Goals

- NG-001 — No unscoped progression redesign, schema overhaul, or new lock category
- NG-002 — No hard dependency on Curios, JEI, EMI, or another optional integration
- NG-003 — No full registry, stage, or entity scan in server tick or render hot paths
- NG-004 — No issue closure based only on source presence, prior release notes, or lower-fidelity checks

## 9. Owner Decisions

### DEC-001 — Completion endpoint

**Status:** RESOLVED
**Selected choice:** Publish ProgressiveStages 3.0.4 to CurseForge and Modrinth after verified master integration.
**Rationale:** The owner selected a public patch release with verified integration
**Affected requirements:** CORE-REQ-009, CORE-REQ-010
**Supersedes:** none

### DEC-002 — Mandatory scope boundary

**Status:** RESOLVED
**Selected choice:** Implement previously advertised or documented 3.0.3 capability if a polish audit proves it missing. Exclude novel unpromised product features.
**Rationale:** The polish matrix controls completeness without expanding the product roadmap
**Affected requirements:** CORE-REQ-001, CORE-REQ-007
**Supersedes:** none

### DEC-003 — Recipe viewer operation

**Status:** RESOLVED
**Selected choice:** JEI and EMI have independent enabled settings, default enabled, and both work concurrently when installed.
**Rationale:** Mixed client installations require independent optional viewer controls
**Affected requirements:** CORE-REQ-004
**Supersedes:** none

### DEC-004 — Entity presence performance gate

**Status:** RESOLVED
**Selected choice:** Use the approved per-player-per-tick context, no per-entity rebuild, under-five-percent sampled work, and at-most-ten-percent p95 MSPT regression thresholds.
**Rationale:** The lag correction needs fixed correctness and performance limits
**Affected requirements:** CORE-REQ-002
**Supersedes:** none

### DEC-005 — Shared workflow release gate

**Status:** RESOLVED
**Selected choice:** Yes. Repair and verification of the shared attestation workflow are mandatory before 3.0.4 publication.
**Rationale:** Release evidence must be verified by the supported shared workflow
**Affected requirements:** CORE-REQ-009, CORE-REQ-010
**Supersedes:** none

## 10. External Prerequisites

| ID | Prerequisite | Affected requirements | Availability | Authorization | Required external action |
|---|---|---|---|---|---|
| EXT-001 | Public Curios, JEI, and EMI test artifacts for Minecraft 1.21.1 and NeoForge 21.1.219 | CORE-REQ-003, CORE-REQ-004 | available | not_required | Resolve the artifact set and execute the runtime matrix |
| EXT-002 | Corrected MCEnvision shared release-validation workflow revision | CORE-REQ-009, CORE-REQ-010 | unavailable | unknown | Merge and validate the compatible attestation verifier revision |
| EXT-003 | Owner-approved platform publication confirmation for the final 3.0.4 artifact | CORE-REQ-010 | unknown | not_authorized | Confirm the final preview through the configured release broker |

### EXT-001 — Public Curios, JEI, and EMI test artifacts for Minecraft 1.21.1 and NeoForge 21.1.219

**Kind:** artifact
**Availability:** available
**Authorization:** not_required
**Affected requirements:** CORE-REQ-003, CORE-REQ-004
**Artifact evidence:** authoritative_source, compatibility, exact_version, license_provenance, security_review, sha256, sha512

**Required evidence**

- Resolved dependency coordinates, artifact checksums, compatibility evidence, license or provenance review, and runtime matrix execution.

### EXT-002 — Corrected MCEnvision shared release-validation workflow revision

**Kind:** other
**Availability:** unavailable
**Authorization:** unknown
**Affected requirements:** CORE-REQ-009, CORE-REQ-010

**Required evidence**

- A merged shared-workflow revision that verifies both build provenance and SBOM attestations using compatible GitHub CLI flags, plus a successful reusable workflow run on a disposable release candidate.

### EXT-003 — Owner-approved platform publication confirmation for the final 3.0.4 artifact

**Kind:** authorization
**Availability:** unknown
**Authorization:** not_authorized
**Affected requirements:** CORE-REQ-010
**Authorization scope binding:** artifact_identities, operations, operators, rollback, runbook_digest, systems, time_window

**Required evidence**

- Confirmation bound to final artifact SHA-256 and SHA-512, source commit, target platforms, allowed publication operation, operator, time window, and rollback or unpublish policy.

## 11. Architecture and Ownership Boundaries

The logical server owns stage membership, compiled rule decisions, configuration reload, permissions, and all access enforcement. Client code owns only presentation, interaction capture, and synchronized caches. A client may request an operator action through the editor but cannot authorize a stage mutation, condition decision, or rule bypass.

`CompiledRuleEngine` owns normalized rule resolution. `MinecraftConditionContextFactory` owns a player relevant immutable condition context. `EntityPresenceEnforcer` owns tracking concealment, interaction denial, and pacification for player specific entity presence rules. CORE-REQ-002 must add a server thread confined snapshot boundary keyed by player identity, server tick, rule revision, and relevant state revision. It must invalidate on rule reload, stage mutation, team mutation, score or metric mutation, dimension transition, session transition, disconnect, and server stop. It must not share contexts across players, worlds, or rule revisions.

Curios, JEI, and EMI are optional adapter boundaries. Their classes must remain isolated from common startup and dedicated server class loading. The adapters translate a core decision into external API behavior but do not own stage policy. A missing or incompatible optional mod retains no integration behavior and emits a concise diagnostic rather than a crash or a permissive fallback.

The Easy Builder, TOML source view, schema compiler, runtime enchantment enforcement, and cleanup fallback share one canonical data model. `StageTreeScreen` owns client menu depth and input routing. The category overlay must render above map nodes, capture menu input while open, and preserve map navigation and inspector semantics after closing.

Release tooling owns packaging, checksum generation, SBOM generation, source commit manifest creation, signed tag verification, and attestation validation. The shared MCEnvision workflow owns the attestation verifier implementation. The broker owns platform mutation and accepts only a release manifest authorized by EXT-003. No credentials, confirmation codes, or private release data may enter source, Git history, documentation, test fixtures, or logs.

## 12. Product Contract and Requirements

The public contract covers stage files, server configuration, commands, KubeJS and integration APIs, editor drafts, synchronized player state, in game UI, optional recipe viewers, Curios slots, and release artifacts. Inputs must be schema validated and authority checked before state changes. Identical stage state, rule revision, configuration revision, and player relevant facts must produce identical normalized rule decisions. Product fixes must generalize across integrated and dedicated servers, solo players, mixed multiplayer ownership, and supported optional mod combinations.

### CORE-REQ-001 — Freeze the polish baseline

**Behavior:** Freeze and reproduce or artifact-verify the five baseline reports, including stale tracking reports
**Owner:** RepositoryAudit
**Contributors:** IssueTracker
**Dependencies:** none
**Lifecycle stage:** readiness
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- Each of issues `#8`, `#10`, `#11`, `#16`, and `#24` has an explicit reproduction, artifact verification, or evidence based stale classification
- The matrix records source revision, installed artifact identity, test configuration, expected behavior, observed behavior, and assigned requirement
- The audit identifies every public 3.0.3 capability gap proven by a shipped artifact inspection without promoting new features

**Required evidence**

- A versioned audit matrix tracing each baseline report to CORE-REQ-002 through CORE-REQ-007
- A final JAR inventory and public documentation comparison for advertised 3.0.3 capabilities

### CORE-REQ-002 — Repair entity presence performance

**Behavior:** Repair entity-presence performance without changing multiplayer visibility, spawning, targeting, or lifecycle semantics
**Owner:** EntityPresenceEnforcer
**Contributors:** CompiledRuleEngine, MinecraftConditionContextFactory
**Dependencies:** CORE-REQ-001
**Lifecycle stage:** change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- A player receives at most one immutable condition context per server tick and no tracked entity rebuilds that context
- A denied player cannot render, target, attack, or be targeted by a denied entity while an eligible nearby player retains normal entity behavior
- The controlled entity presence fixture stays under five percent of sampled server thread work and p95 MSPT increases no more than ten percent versus disabled enforcement

**Required evidence**

- Cache revision and invalidation tests covering every architecture boundary in §11
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
**Lifecycle stage:** post_change
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
**Lifecycle stage:** post_change
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
**Lifecycle stage:** post_change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- Every correction cites a CORE-REQ-001 matrix row and a public 3.0.3 documentation or release source
- Each corrected capability is present in the final JAR and passes its documented runtime workflow
- No correction introduces an unpromised product feature or platform upgrade

**Required evidence**

- Per capability regression evidence and final artifact inventory
- Documentation changes limited to behavior that the final JAR proves

### CORE-REQ-008 — Preserve compatibility and security

**Behavior:** Preserve supported configuration, stage schema, saved data, commands, APIs, and multiplayer compatibility
**Owner:** CompatibilityHarness
**Contributors:** NetworkValidation, ConfigurationSchema
**Dependencies:** CORE-REQ-002, CORE-REQ-003, CORE-REQ-004, CORE-REQ-005, CORE-REQ-006, CORE-REQ-007
**Lifecycle stage:** continuous
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- Existing supported stage packs, configuration files, saved player stage data, commands, and public API calls remain loadable and behaviorally compatible
- Editor and client packets require operator authorization and schema validation before a server mutation
- Integrated server, dedicated server, reconnect, reload, optional integration, and multiplayer ownership paths pass the compatibility matrix

**Required evidence**

- Formatter, static analysis, unit test, GameTest, build, dedicated server, client, multiplayer, and JAR inspection results
- Configuration compatibility fixtures and packet authorization regression tests

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

**Behavior:** Build, integrate, attest, document, publish, and verify 3.0.4, then close the five baseline issues with evidence
**Owner:** ReleaseBroker
**Contributors:** ReleaseDocumentation
**Dependencies:** CORE-REQ-009, EXT-003
**Lifecycle stage:** post_change
**Production verification:** nondestructive
**Release impact:** stable release

**Acceptance criteria**

- The verified 3.0.4 patch is merged into master with a signed integration commit and signed annotated release tag
- The publication preview matches the final JAR SHA-256, SHA-512, source commit, metadata, release notes, platforms, and dependencies
- CurseForge and Modrinth downloads hash match the verified artifact and all five baseline issues close with merged revision acceptance evidence

**Required evidence**

- Pull request merge record, signed commit verification, signed tag verification, JAR listing, checksums, SBOM, source manifest, and attestation verification
- EXT-003 confirmation, platform URLs, downloaded artifact hash checks, and issue closure evidence

## 13. Phased Roadmap

The master owns the global order and concise phase catalog. Each blueprint path is a required future plan artifact and is intentionally recorded as a plain path until the phase file is authored. This avoids a broken Markdown link while preserving the exact mandatory topology.

| Phase ID | Objective | Owner | Dependencies | Canonical requirements | Entry summary | Exit summary | Next transition | Blueprint path |
|---|---|---|---|---|---|---|---|---|
| CORE-PHASE-000 | Freeze the defect and artifact baseline | RepositoryAudit | none | CORE-REQ-001 | Baseline revision and issue set are pinned | Audit classifies every mandatory report and advertised capability claim | CORE-PHASE-001 | `phases/plan-phase-000.md` |
| CORE-PHASE-001 | Repair entity presence performance | EntityPresenceEnforcer | CORE-PHASE-000 | CORE-REQ-002 | CORE-REQ-001 audit identifies the hot path fixture | Correctness and DEC-004 performance evidence pass | CORE-PHASE-002 | `phases/plan-phase-001.md` |
| CORE-PHASE-002 | Restore optional integration behavior | CuriosBridge | CORE-PHASE-000, EXT-001 | CORE-REQ-003, CORE-REQ-004 | EXT-001 artifact contract is complete | Curios, JEI, and EMI compatibility matrix passes | CORE-PHASE-003 | `phases/plan-phase-002.md` |
| CORE-PHASE-003 | Verify editor, client UI, and artifact parity | EasyBuilder | CORE-PHASE-000 | CORE-REQ-005, CORE-REQ-006, CORE-REQ-007 | Audit assigns only source owned corrections | Editor, UI, and proven artifact parity evidence pass | CORE-PHASE-004 | `phases/plan-phase-003.md` |
| CORE-PHASE-004 | Prove compatibility and regression safety | CompatibilityHarness | CORE-PHASE-001, CORE-PHASE-002, CORE-PHASE-003 | CORE-REQ-008 | Component changes are complete | Full compatibility and security verification passes | CORE-PHASE-005 | `phases/plan-phase-004.md` |
| CORE-PHASE-005 | Integrate and validate the release artifact | ReleaseValidation | CORE-PHASE-004, EXT-002 | CORE-REQ-009 | Shared verifier repair is merged and pinned | Signed master artifact and release validation evidence pass | CORE-PHASE-006 | `phases/plan-phase-005.md` |
| CORE-PHASE-006 | Publish the verified patch and close issues | ReleaseBroker | CORE-PHASE-005, EXT-003 | CORE-REQ-010 | Broker preview matches final signed artifact | The plan wide completion endpoint and Definition of Done pass with no known mandatory phase owned defect remaining | final completion | `phases/plan-phase-006.md` |

## 14. Verification Strategy

| Requirement | Unit | Integration | Real behavior | Security | Artifact or runtime |
|---|---|---|---|---|---|
| CORE-REQ-001 | Matrix consistency checks | Source, issue, documentation, and JAR reconciliation | Reproduce or artifact verify all five reports | Sanitized logs and screenshots | Versioned issue matrix |
| CORE-REQ-002 | Snapshot, cache, and invalidation tests | Two player server scenario | Profiled entity tracking and p95 MSPT fixture | Player scoped cache isolation | Spark or equivalent profile capture |
| CORE-REQ-003 | API resolver and inventory conservation tests | Curios present and absent server smoke tests | Grant, revoke, reconnect, and reload | Optional class loading isolation | EXT-001 artifact matrix |
| CORE-REQ-004 | Independent configuration default tests | JEI only, EMI only, both, disabled, absent | Stage visibility update in each viewer | Optional class loading isolation | EXT-001 client runtime matrix |
| CORE-REQ-005 | Form, serializer, and compiler round trip tests | Operator apply and client sync | Packaged editor bundle workflow | Operator authority and malformed draft rejection | Final JAR browser smoke |
| CORE-REQ-006 | Render order and input routing tests | Populated stage map | GUI scale client smoke | Client side only class boundary | Screenshots and procedure |
| CORE-REQ-007 | Per gap regression tests | JAR resource inspection | Documented user workflow | Scope trace to SRC-001 | Artifact parity matrix |
| CORE-REQ-008 | Existing and targeted regression suite | Reconnect, reload, multiplayer, optional mod matrix | Dedicated and integrated server smoke | Packet and editor authorization review | Build and final diff inspection |
| CORE-REQ-009 | Checksum and metadata checks | Shared reusable workflow | Disposable release candidate verification | Attestation success and tamper failure | Signed JAR, SBOM, and source manifest |
| CORE-REQ-010 | Release manifest equivalence checks | Broker preview validation | Download each platform artifact | EXT-003 authorization binding | Platform URLs and hashes |

All changed Java, Gradle, resource, configuration, networking, client, and optional integration paths run the applicable formatter, static analysis, unit tests, GameTests, `./gradlew build`, dedicated server smoke test, client smoke test, multiplayer or reconnect verification, and final JAR inspection. A failed gate blocks the owning phase and cannot be replaced by a lower fidelity claim.

## 15. Compatibility, Migration, Rollout, and Recovery

3.0.4 is a compatible patch. Existing stage identifiers, `pack:stage` parsing, TOML schema, configuration keys, commands, persisted player stage data, packets, public APIs, editor drafts, and default behavior remain supported. The new independent JEI setting must default enabled when absent so existing configurations retain their expected recipe viewer behavior. EMI configuration must remain readable. No Minecraft, NeoForge, Java, Gradle, mappings, persistence format, or schema upgrade is in scope.

Entity presence snapshots are transient server state and never enter persistent player data. A snapshot invalidates and rebuilds from authoritative server state after every relevant state revision, reload, reconnect, dimension change, or restart. Curios transitions must conserve inventory contents. A failed optional adapter must disable only its adapter boundary and not alter core rule decisions.

Rollout order is Phase 000 audit, sequential source changes, compatibility proof, signed master integration, release validation, broker preview, EXT-003 confirmation, dual platform publication, and downloaded hash verification. Before publication, recovery is a corrective change on the appropriate sequential phase branch followed by the complete verification set. After publication, recovery follows the rollback or unpublish policy bound by EXT-003 and requires a new verified artifact for any replacement.

## 16. Documentation, Operations, and Release Gates

- Update `README.md`, `DOCUMENTATION.md`, the documentation index, and affected references only for verified 3.0.4 behavior
- Document Curios support, absent mod behavior, JEI and EMI independent settings, defaults, and coexistence behavior
- Document entity presence snapshot invalidation, profiling fixture, performance thresholds, and diagnostic collection procedure
- Document Easy Builder enchantment controls, TOML equivalence, category overlay behavior, and troubleshooting workflows
- Produce issue closure evidence that references the merged revision and user visible verification for each baseline issue
- Require a clean final diff, signed master integration, signed tag, JAR inspection, SHA-256, SHA-512, SBOM, source manifest, and verified attestations
- Require EXT-002 before release validation is accepted and EXT-003 before platform publication begins

## 17. Risks and Failure Boundaries

| Risk | Impact | Prevention | Detection | Recovery |
|---|---|---|---|---|
| Snapshot invalidation omits a relevant player fact | Stale allow or deny decision | Revision keyed player snapshots and mutation tests | Direct decision comparison and multiplayer smoke | Invalidate snapshot, correct boundary, rerun CORE-REQ-002 evidence |
| Performance cache changes shared entity behavior | Incorrect visibility or targeting | Preserve core rule resolution and test mixed player ownership | Two player entity fixture | Revert semantic change and retain the profiling harness |
| Curios API drift links optional classes | Startup failure or broken slots | Version tolerant bridge and absent mod boundary | Present and absent integration matrix | Correct adapter resolution without changing core rules |
| JEI and EMI conflict in combined clients | Incorrect recipe display | Independent adapters and settings | Full recipe viewer matrix | Restore isolated adapter behavior and add regression test |
| Editor source diverges from Easy Builder | Invalid or surprising saved rules | Shared normalized schema and round trips | Operator apply and compiler equivalence test | Reject invalid draft and repair serializer |
| Category overlay regresses at another GUI scale | Icons appear over menu or clicks leak | Render depth and input capture tests | Scale screenshot procedure | Correct client render order and rerun CORE-REQ-006 |
| Shared verifier remains incompatible | Release cannot be attestation verified | EXT-002 blocks Phase 005 | Release workflow failure | Remain NOT COMPLETE — EXTERNALLY BLOCKED and do not bypass validation |
| Broker confirmation becomes stale | Wrong artifact publication | EXT-003 binds artifact identities and runbook digest | Preview hash mismatch | Stop publication and request a new scoped confirmation |

## 18. Definition of Done

**Plan completion status:** NOT COMPLETE — EXTERNALLY BLOCKED

- A signed and verified 3.0.4 patch is merged into master, published to CurseForge and Modrinth, and all five baseline GitHub issues are closed with merged-revision acceptance evidence.
- Every CORE-REQ-001 through CORE-REQ-010 acceptance criterion and required evidence gate passes at the defined fidelity
- The final JAR, SHA-256, SHA-512, SBOM, source manifest, signed integration, signed tag, and attestation evidence identify the same release artifact
- `Corrected MCEnvision shared release-validation workflow revision` is complete and demonstrates compatible attestation verification under EXT-002
- `Owner-approved platform publication confirmation for the final 3.0.4 artifact` is complete and binds the final publication under EXT-003
- While EXT-002 or EXT-003 remains unsatisfied, the plan remains NOT COMPLETE — EXTERNALLY BLOCKED and no publication or issue closure may claim completion
- FUT-001, FUT-002, FUT-003, and FUT-004 remain excluded

## 19. Goal Creator Handoff

Mandatory boundary: Resolve only issues #8, #10, #11, #16, and #24 plus previously advertised or documented 3.0.3 capabilities proven missing by the CORE-PHASE-000 artifact audit
Optional/future disposition: excluded
Locked owner decisions: DEC-001, DEC-002, DEC-003, DEC-004, DEC-005
Active phase: CORE-PHASE-000
Next executable action: Author phases/plan-phase-000.md and freeze the five issue reproductions and advertised capability audit against commit ab4ad138e1f74eec82cf0392a47ac1e54dd66d01
Known failing checks: GitHub Actions run 31453460136 job 93662356066 fails shared attestation verification because incompatible GitHub CLI signer flags are combined
Known external blockers: Corrected MCEnvision shared release-validation workflow revision under EXT-002 and Owner-approved platform publication confirmation for the final 3.0.4 artifact under EXT-003
Completion endpoint: A signed and verified 3.0.4 patch is merged into master, published to CurseForge and Modrinth, and all five baseline GitHub issues are closed with merged-revision acceptance evidence.
Required evidence gates: CORE-REQ-001 audit matrix, DEC-004 performance profile, EXT-001 compatibility matrix, CORE-REQ-005 editor bundle proof, CORE-REQ-006 client UI proof, CORE-REQ-008 regression suite, EXT-002 reusable workflow proof, signed master integration and tag, checksums, SBOM, source manifest, attestation verification, EXT-003 confirmation, platform URLs, and downloaded artifact hashes

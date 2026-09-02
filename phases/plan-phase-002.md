# Phase 002 Execution Plan

> **Plan ID:** PLAN-PHASE-002
> **Phase ID:** CORE-PHASE-002
> **Owner:** OptionalIntegrations
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 002 of 006

## Purpose and Ownership

This phase restores the optional integration behavior owned by `CORE-REQ-003` and `CORE-REQ-004`. It exists to make Curios slot enforcement safe and functional against the supported public API, and to make JEI and EMI independently configurable and concurrently functional without turning any optional integration into a hard dependency. The master plan owns the product contract, the locked owner decisions, and the global phase order. This blueprint owns only the dependency ordered implementation and proof for Curios, JEI, and EMI behavior in Phase 002.

The phase may begin only after `CORE-PHASE-000` identifies the applicable issue reproductions and artifact claims, and after `EXT-001` supplies the complete public artifact evidence contract. No Curios package correction may be selected from memory or an unverified private artifact. The resolved public artifacts, exact versions, compatibility evidence, license provenance, security review, and checksums are the authoritative compatibility inputs.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| VERIFIED | Phase scope | `CORE-REQ-003` owns Curios slot gating, transition safety, and absent-mod startup behavior | Master plan requirements and roadmap | Invalidated by an owner-approved master plan revision |
| VERIFIED | Recipe viewer decision | JEI and EMI require independent enabled settings, both default enabled, and concurrent operation when both are installed | `DEC-003` in the master plan | Invalidated only by an owner-approved decision that supersedes `DEC-003` |
| OBSERVED | Curios defect | The baseline reports Curios slot gating against a supported API surface as defective | `CORE-PHASE-000` issue matrix for issue `#8` | Invalidated when the tested artifact identity or reproduced configuration changes |
| VERIFIED | Optional dependency boundary | Curios, JEI, and EMI must remain optional and must not load absent integration classes through common or dedicated-server startup | Master plan architecture boundary and `NG-002` | Invalidated by an approved architecture or supported-platform change |
| UNKNOWN | Curios public API shape | The exact supported public type and lookup path must be established by `EXT-001` rather than assumed | `EXT-001` artifact evidence packet | Resolved only when all required artifact evidence fields identify the exact artifact set |
| UNKNOWN | Combined viewer behavior | Independent and concurrent JEI and EMI behavior is not proven until the supported runtime matrix passes | `CORE-REQ-004` acceptance criteria | Resolved by repeatable runtime evidence from the phase completion packet |

## Scope Boundaries

### Included Scope

- `CORE-REQ-003` restores Curios slot gating through an `EXT-001` verified public API resolution strategy, preserves authoritative inventory contents across grant, revoke, reconnect, and reload transitions, and proves startup safety with Curios absent.
- `CORE-REQ-004` provides separate JEI and EMI enabled settings, treats a missing setting as enabled, preserves existing readable EMI configuration, and proves JEI-only, EMI-only, combined, disabled, and absent combinations.
- Optional adapter diagnostics, test fixtures, configuration compatibility proof, documentation, and completion evidence needed to close this phase are included.

### Explicit Exclusions

- `CORE-REQ-002` entity-presence performance is owned by `CORE-PHASE-001` and must not be mixed into optional integration adapters.
- `CORE-REQ-005`, `CORE-REQ-006`, `CORE-REQ-007`, and `CORE-REQ-011` editor, in-game UI, artifact-parity, and browser-editor recipe-lock serialization corrections are owned by `CORE-PHASE-003`.
- Recipe-viewer work in this phase is limited to JEI and EMI visibility adapters. It must not change Easy Builder recipe-rule serialization, TOML recipe keys, draft persistence, or runtime recipe-lock enforcement owned by `CORE-REQ-011`.
- `CORE-REQ-008` plan-wide compatibility verification is owned by `CORE-PHASE-004`; this phase supplies its optional-integration inputs but does not close that requirement.
- `FUT-001`, `FUT-002`, `FUT-003`, and `FUT-004` remain excluded, including platform upgrades and unpromised optional-integration features.
- `NG-001`, `NG-002`, and `NG-003` prohibit progression redesign, hard optional dependencies, and hot-path registry scans.
- No package correction based on an unverified class name, bundled private artifact, or unsupported integration version is allowed.

## Phase Contract

### CORE-PHASE-002 — Restore optional integration behavior

**Objective:** Produce verified Curios slot enforcement and independent, default-enabled, concurrently operable JEI and EMI adapters while preserving soft-dependency startup safety
**Owner:** OptionalIntegrations
**Dependencies:** CORE-PHASE-000, EXT-001
**Canonical requirements:** CORE-REQ-003, CORE-REQ-004
**Documentation and release impact:** Update the configuration and integration documentation for verified Curios behavior, absent-mod behavior, independent JEI and EMI settings, default values, coexistence, diagnostics, and the supported runtime matrix; provide release evidence for issues `#8` and `#10`
**Next transition:** CORE-PHASE-003

**Entry criteria**

- `CORE-PHASE-000` is integrated and its completion packet identifies the exact issue `#8` and issue `#10` reproduction or artifact-verification state against the pinned baseline.
- `EXT-001` records `authoritative_source`, `compatibility`, `exact_version`, `license_provenance`, `security_review`, `sha256`, and `sha512` for the Curios, JEI, and EMI artifacts used by this phase.
- Resolved artifacts match Minecraft 1.21.1 and NeoForge 21.1.219, and their checksums match the recorded evidence before any integration implementation begins.
- The current optional-integration configuration and public behavior identified by Phase 000 are captured as compatibility fixtures so the phase can distinguish correction from unintended schema change.

**Implementation scope**

- `CORE-REQ-003` resolves the supported Curios public API from the verified artifact set, confines all Curios linkage behind its optional adapter boundary, restores documented slot deny, ejection, or retention behavior, and proves inventory conservation through lifecycle transitions.
- `CORE-REQ-004` separates the JEI and EMI enable decisions, defaults each missing setting to enabled, allows both adapters to receive the same normalized visibility decision when installed together, and prevents an absent or disabled adapter from affecting the other.
- CORE-REQ-003 and CORE-REQ-004 receive deterministic configuration, class-loading, transition, runtime-matrix, negative-path, and artifact evidence suitable for the later compatibility phase.

**Execution order**

1. `P002-TASK-001` validates and freezes the `EXT-001` public artifact contract before implementation.
2. `P002-TASK-002` converts the CORE-PHASE-000 Curios and recipe-viewer findings into reproducible fixtures and a complete runtime matrix.
3. `P002-TASK-003` executes `CORE-REQ-003` by restoring the Curios API resolution and optional class-loading boundary.
4. `P002-TASK-004` executes `CORE-REQ-003` inventory enforcement and transition conservation coverage.
5. `P002-TASK-005` executes the `CORE-REQ-004` independent configuration contract and compatible defaults.
6. `P002-TASK-006` executes `CORE-REQ-004` adapter coexistence and isolation behavior.
7. `P002-TASK-007` runs the CORE-REQ-003 and CORE-REQ-004 present, absent, enabled, disabled, combined, reload, reconnect, and dedicated-server matrix.
8. `P002-TASK-008` reconciles CORE-REQ-003 and CORE-REQ-004 documentation, final artifact contents, issue evidence, and the downstream compatibility handoff.

**Required evidence**

- The immutable `EXT-001` artifact manifest with authoritative coordinates or identities, compatibility proof, exact versions, license provenance, security review, SHA-256, and SHA-512 values.
- Focused tests that prove API resolution selects only a supported public Curios surface and does not resolve optional integration classes when the mod is absent.
- Inventory conservation assertions for Curios allow, deny, ejection or retention, stage grant, stage revoke, reconnect, reload, full-inventory, and repeated enforcement scenarios applicable to the documented contract.
- Configuration tests for missing, explicit true, explicit false, legacy readable EMI, malformed, and reload cases for both JEI and EMI settings.
- Client runtime evidence for JEI only, EMI only, both installed and enabled, each individually disabled while the other remains enabled, both disabled, and neither installed.
- Dedicated-server startup evidence with all three optional integrations absent, plus applicable Curios-present server evidence.
- A packaged-JAR inspection proving that optional integration support is present without bundling forbidden dependency artifacts or introducing unconditional optional-class references at startup boundaries.

**Exit criteria**

- Every `CORE-REQ-003` and `CORE-REQ-004` acceptance criterion passes against the exact `EXT-001` artifacts and the pinned Phase 000 reproductions.
- Curios present and absent paths start safely, documented slot behavior is enforced, and all tested transitions conserve inventory contents without duplication or loss.
- JEI and EMI settings are independent, both default enabled when absent, every supported viewer combination has the expected visibility result, and both viewers operate together when installed and enabled.
- Required tests, runtime logs or screenshots, artifact manifest, JAR inspection, documentation changes, rollback notes, and issue-specific evidence are assembled in the completion packet.
- The phase branch is integrated through its required review and verification gates before `CORE-PHASE-003` begins.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Issue and artifact baseline | `CORE-PHASE-000` | Issues `#8` and `#10` have exact artifact identities, configurations, expectations, observations, and assigned requirements | Inspect the Phase 000 completion packet and reproduce its fixtures | Stop and return the ambiguous row to Phase 000 evidence correction |
| Public optional artifacts | `EXT-001` | Curios, JEI, and EMI artifacts match the supported platform and include every required evidence field | Resolve from the authoritative source and independently match SHA-256 and SHA-512 | Stop before implementation; do not substitute an unverified package name or artifact |
| Recipe-viewer decision | `DEC-003` | JEI and EMI settings are independent, default enabled, and permit concurrent operation | Compare configuration and matrix expectations with the selected choice verbatim | Reject an implementation or migration that couples the settings or disables a missing setting by default |
| Existing configuration contract | Master plan compatibility boundary | Supported files remain readable, including the existing EMI configuration | Load the pinned compatibility fixtures and compare normalized values | Preserve the fixture, correct the migration or parser, and rerun configuration tests |
| Core visibility decision | Core rule engine boundary | Optional adapters consume the authoritative normalized decision without owning stage policy | Compare viewer adapter results for identical player state and rule state | Stop adapter rollout if either viewer invents or mutates policy |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Curios adapter boundary | `CORE-PHASE-004` | Supported public API resolution works when present and no Curios class loads when absent | Compatible patch behavior for the `EXT-001` version without a hard dependency | Resolver tests, present and absent startup logs, JAR inspection |
| Curios transition behavior | `CORE-PHASE-004` | Slot enforcement preserves authoritative contents across grant, revoke, reconnect, and reload | No persisted schema change; existing stage behavior remains authoritative | Inventory conservation test report and runtime trace |
| Independent viewer settings | `CORE-PHASE-004` | JEI and EMI each have an independent enabled value, and a missing value resolves to enabled | Existing EMI configuration remains readable; new missing values use the locked compatible default | Configuration fixture results and reload evidence |
| Concurrent viewer adapters | `CORE-PHASE-004` | Installed and enabled JEI and EMI adapters can both apply the same core visibility decision | Each adapter remains optional and independently disabled | Full viewer matrix results and client runtime evidence |
| Issue evidence packet | `CORE-PHASE-005` and release operations | Issues `#8` and `#10` have merged-revision-ready acceptance evidence | Evidence identifies exact artifacts and source revision | Completion packet, artifact hashes, and issue-specific verification summary |
| Documentation delta | Operators and pack authors | Curios and recipe-viewer configuration describes only verified 3.0.4 behavior | Compatible patch notes, no platform upgrade or new feature claim | Documentation diff and link check results |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P002-TASK-001 | CORE-REQ-003, CORE-REQ-004 | Validate, hash, and freeze the supported public optional-artifact set | `EXT-001`, supported platform metadata | Versioned artifact manifest and verified local inputs | Optional dependency resolution and test runtime | Coordinate or identity check, compatibility check, provenance and security review, SHA-256 and SHA-512 match |
| P002-TASK-002 | CORE-REQ-003, CORE-REQ-004 | Define the exact Curios lifecycle fixture and complete JEI and EMI combination matrix from Phase 000 evidence | `CORE-PHASE-000`, `DEC-003`, P002-TASK-001 | Reproducible fixtures with expected outcomes and negative cases | Curios slots, inventory state, configuration, recipe visibility | Fixture dry run against the pinned baseline reproduces or artifact-verifies each assigned defect |
| P002-TASK-003 | CORE-REQ-003 | Restore Curios resolution through the verified public API while isolating optional classes | P002-TASK-001, P002-TASK-002 | Version-tolerant Curios bridge with absent-mod safety | Curios adapter, common startup, dedicated-server class loading | Resolver tests and present or absent startup smoke tests |
| P002-TASK-004 | CORE-REQ-003 | Restore slot enforcement and prove contents remain conserved across stage and server lifecycle transitions | P002-TASK-003, Phase 000 documented slot contract | Correct allow, deny, ejection, or retention outcomes without loss or duplication | Inventory enforcement, stage grant and revoke, reconnect, reload | State-by-state inventory identity and count assertions, repeated enforcement test, full-inventory negative case |
| P002-TASK-005 | CORE-REQ-004 | Introduce independent JEI and EMI enable controls with default-enabled missing values and compatible configuration loading | `DEC-003`, P002-TASK-002 | Two independent normalized configuration decisions | Main configuration, reload, viewer activation | Missing, true, false, legacy readable EMI, malformed, and reload fixture tests |
| P002-TASK-006 | CORE-REQ-004 | Isolate viewer adapters and support simultaneous application of the authoritative visibility decision | P002-TASK-005, core visibility boundary | JEI and EMI adapters that coexist and disable independently | JEI adapter, EMI adapter, optional class loading, visibility refresh | JEI-only, EMI-only, both-enabled, mixed-disabled, both-disabled, and absent integration tests |
| P002-TASK-007 | CORE-REQ-003, CORE-REQ-004 | Execute the supported client, dedicated-server, reconnect, reload, and optional-mod runtime matrix | P002-TASK-003 through P002-TASK-006, `EXT-001` artifacts | Runtime proof and classified failures for every required combination | Client viewer behavior, server startup, inventory transitions | Exact-version logs or screenshots, server smoke results, configuration snapshots, artifact identity record |
| P002-TASK-008 | CORE-REQ-003, CORE-REQ-004 | Inspect the packaged output, update verified documentation, and assemble the downstream completion packet | P002-TASK-007 and clean build artifact | Final phase evidence and compatibility handoff | JAR resources, documentation, issue evidence, release notes input | JAR listing, documentation links, clean diff inspection, completion-packet checklist |

`P002-TASK-001` and `P002-TASK-002` are strict prerequisites for code selection because an unverified artifact or ambiguous fixture can produce a false compatibility fix. After the artifact manifest and fixtures are frozen, the Curios stream in `P002-TASK-003` and `P002-TASK-004` may proceed in parallel with the recipe-viewer stream in `P002-TASK-005` and `P002-TASK-006`. `P002-TASK-007` joins both streams and blocks documentation finalization. A failed matrix cell returns to the task that owns that cell; successful unrelated cells remain evidence only if the implementation and artifact identities that produced them are unchanged.

## Architecture and Implementation Boundaries

The logical server remains authoritative for stage ownership, rule decisions, Curios slot enforcement, and inventory mutation. The Curios adapter translates that decision into a supported public API call but does not own stage policy. Client recipe-viewer adapters consume synchronized authoritative visibility state and apply presentation changes only. Neither JEI nor EMI may mutate server rule state or infer a different allow or deny result.

All optional integration linkage remains behind runtime availability checks and isolated adapter boundaries. Common initialization, dedicated-server startup, packet handling, and configuration parsing must not require Curios, JEI, or EMI classes when the corresponding mod is absent. A present but incompatible verified artifact is a compatibility failure, not permission to guess another package or silently allow a rule. Diagnostics must identify the integration and failed boundary without printing private data or repeating on a hot path.

Configuration produces two independent normalized booleans, one for JEI and one for EMI. Each missing value resolves to enabled under `DEC-003`. An explicit false value disables only its matching adapter. When both are installed and enabled, each adapter receives the same normalized core visibility decision; one adapter must not short-circuit registration, refresh, or filtering for the other. Existing readable EMI settings remain compatible, and no unrelated configuration key or stage schema changes in this phase.

Curios enforcement must be idempotent and inventory-conserving. Repeated checks cannot duplicate or delete items. Grant, revoke, reconnect, reload, and full-inventory boundaries must use the existing documented enforcement policy and authoritative contents. No broad registry scan, per-render filesystem access, or recurring reflection lookup is allowed in a hot path. Any version-tolerant resolution is established once at the appropriate lifecycle boundary and cached only for the exact installed artifact identity.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| `EXT-001` evidence is incomplete or a hash differs | Manifest validation or checksum mismatch | Stop before selecting or changing an API path | Re-resolve from the authoritative source and replace the evidence packet only after review | Artifact validation rerun records matching SHA-256 and SHA-512 |
| Curios is absent | Runtime availability check and absent-mod fixture | Core and dedicated-server startup succeed without loading Curios classes | Disable only the Curios adapter; retain core rule behavior | Absent-mod class-loading and startup smoke test |
| Verified Curios artifact exposes an unsupported surface | Resolver test or present-mod startup failure | Emit one actionable diagnostic and fail the Curios compatibility gate without item mutation | Correct the adapter against verified public evidence or hold the phase | Present-mod resolver test and untouched-inventory assertion |
| Curios enforcement repeats during a transition | Duplicate event or repeated enforcement fixture | Produce the same final inventory state without duplication or loss | Restore the pretest inventory fixture, correct idempotency, rerun every transition case | Item identity and count conservation across repeated checks |
| Destination inventory has no capacity | Full-inventory fixture | Follow the documented safe retention or ejection policy without destroying or cloning the item | Restore fixture snapshot and correct transition handling | Full-inventory conservation assertion |
| JEI is absent or disabled while EMI is enabled | Runtime matrix and normalized settings | EMI continues to register, refresh, and filter independently | Correct adapter activation isolation | EMI-only and JEI-disabled runtime cells pass |
| EMI is absent or disabled while JEI is enabled | Runtime matrix and normalized settings | JEI continues to register, refresh, and filter independently | Correct adapter activation isolation | JEI-only and EMI-disabled runtime cells pass |
| Both viewers are installed and enabled | Combined client fixture | Both viewers apply the same authoritative visibility decision | Remove cross-adapter short circuit and rerun all viewer cells | Combined visibility comparison and refresh evidence |
| A viewer setting is missing | Compatibility fixture | The missing setting resolves to enabled for that viewer | Correct default normalization without rewriting unrelated keys | Missing-setting load and restart tests |
| Configuration is malformed | Configuration validation | Reject or recover according to existing configuration error handling without silently changing the other viewer setting | Restore the valid fixture and correct field-specific handling | Malformed-field negative test and unchanged sibling value |
| Optional adapter throws during refresh | Integration-specific diagnostic and runtime test | Contain failure to that adapter and preserve server authority and the other installed viewer | Disable or correct only the failing adapter, then retry from a clean client state | Fault-injection or controlled failure test plus unaffected-adapter evidence |

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Evidence artifact |
|---|---|---|---|---|---|
| P002-TASK-001 | Manifest schema and hash checks | Exact artifact resolution | Resolve supported public artifacts from authoritative sources | Reject missing provenance, incompatible version, or checksum mismatch | Immutable `EXT-001` artifact manifest |
| CORE-REQ-003 | Resolver and inventory conservation tests | Curios present and absent startup; grant, revoke, reconnect, reload | Equip or retain gated contents under the documented slot policy on the supported runtime | Absent mod, incompatible surface, repeated enforcement, full inventory, rollback fixture | Curios matrix report, server logs, inventory before and after record |
| P002-TASK-004 | Idempotency and item-count assertions | Stage and server lifecycle transition suite | Repeat enforcement before and after authoritative state changes | Restore known inventory snapshot after injected failure | Transition conservation report |
| CORE-REQ-004 | Independent default and activation tests | JEI-only, EMI-only, combined, mixed-disabled, both-disabled, neither-installed matrix | Change stage visibility and verify both installed viewers refresh to the same expected state | Missing and malformed settings, absent classes, one adapter failure | Viewer matrix table, client logs or screenshots, configuration fixtures |
| P002-TASK-005 | Parser, default, and reload tests | Existing EMI fixture plus new JEI field | Load, restart, and reload each explicit or missing setting combination | Invalid field does not mutate the independent sibling decision | Configuration compatibility report |
| P002-TASK-006 | Adapter activation isolation tests | Both adapters consume one normalized decision | Enable both, change authoritative stage state, and inspect both viewer results | Disable either adapter and prove the other remains active | Combined and isolated adapter evidence |
| P002-TASK-007 | Test-result consistency check | Complete optional-mod matrix | Supported client and dedicated-server smoke procedures | Rerun failed cell after clean recovery; never substitute a lower-fidelity cell | Versioned runtime matrix with artifact identities |
| P002-TASK-008 | Documentation and JAR listing checks | Final build consumes verified adapters and settings | Run the relevant workflow from the packaged artifact | Reject stale web resources, bundled forbidden dependency, or undocumented setting drift | JAR inventory, documentation diff, completion packet |

Fixtures must pin the source revision, exact optional artifact identities, Minecraft and NeoForge versions, configuration values, stage state, starting inventory, expected recipe visibility, and expected final inventory. Client tests use fresh instances for absent or present mod combinations. Server transition tests restore a known inventory and authoritative stage state between cases. The rerun order is artifact validation, focused unit tests, Curios present and absent integration, recipe-viewer matrix, dedicated-server smoke, client smoke, build, and JAR inspection. A source-only assertion, compilation result, or screenshot cannot replace the required runtime cell.

## Documentation, Operations, and Release

- Update `README.md`, `DOCUMENTATION.md`, the existing documentation index, and affected integration references only after behavior is proven by the packaged artifact.
- Document the exact Curios version or compatibility boundary established by `EXT-001`, slot enforcement outcomes, absent-mod behavior, transition safety, and concise diagnostics.
- Document separate JEI and EMI settings, both defaulting enabled when missing, explicit disable behavior, coexistence behavior, configuration examples, reload or restart expectations, and troubleshooting for an absent or incompatible viewer.
- Record the complete optional-mod runtime matrix, fixture preparation, expected results, exact artifact identities, and rerun procedure for maintainers.
- Prepare issue `#8` and issue `#10` acceptance evidence that identifies the tested merged revision and artifacts; do not close issues in this phase before the later integration and release gates authorize closure.
- Add the verified behavior to 3.0.4 release-note input without claiming publication or plan completion.
- No migration beyond compatible missing-value defaults and continued readable EMI configuration is authorized.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| Artifact drift changes a public API after implementation | Pin exact `EXT-001` identities and hashes | Resolution or checksum differs from packet | Re-audit compatibility before modifying the adapter | Resolver, runtime, security, and provenance evidence | Rerun P002-TASK-001 through P002-TASK-007 |
| Optional classes leak into common startup | Keep integration linkage isolated and test absent combinations | Dedicated-server or absent-mod class-loading failure | Move linkage behind the adapter lifecycle boundary | All absent-mod and JAR safety evidence | Static class-boundary inspection plus absent startup matrix |
| Curios transition loses or duplicates contents | Idempotent authoritative transition design and conservation assertions | Before and after identity or count mismatch | Restore fixture, correct transition, repeat all lifecycle cases | Curios acceptance and compatibility handoff | Rerun P002-TASK-004 and all Curios cells in P002-TASK-007 |
| Independent settings accidentally share one activation path | Two normalized values and adapter-specific activation tests | Mixed-disabled matrix changes the wrong adapter | Separate activation path and restore compatibility fixture | All configuration and viewer coexistence evidence | Rerun P002-TASK-005 through P002-TASK-007 |
| Combined viewers overwrite or short-circuit each other | Apply one core decision through isolated adapters | JEI and EMI disagree after the same stage transition | Correct registration or refresh isolation | Combined and single-viewer runtime evidence | Rerun every recipe-viewer matrix cell |
| Runtime evidence uses a development resource absent from the JAR | Test the packaged artifact and inspect its contents | Packaged workflow differs from development workflow | Correct packaging and rebuild from a clean state | All client runtime and documentation claims | Rebuild, inspect JAR, repeat P002-TASK-007 and P002-TASK-008 |
| A platform or integration upgrade enters the fix | Compare dependency and lock metadata with the pinned baseline | Unapproved version drift in final diff or artifact manifest | Revert unrelated version change and retain supported artifact set | Build, compatibility, and runtime evidence | Repeat artifact validation and affected matrix cells |

## Phase Completion Packet

The phase may close only when the ordinary execution record contains the integrated source revision, exact `EXT-001` artifact manifest, provenance and security review, SHA-256 and SHA-512 verification, issue `#8` and `#10` baseline rows, focused test results, Curios present and absent startup evidence, inventory transition and conservation results, independent configuration fixtures, the complete JEI and EMI runtime matrix, dedicated-server and client smoke results, packaged-JAR inventory, clean diff inspection, documentation changes, rollback and rerun instructions, and issue-specific acceptance summaries. The packet must identify every artifact and environment strongly enough for `CORE-PHASE-004` to repeat the proof.

The corresponding phase branch, commits, pull request, deterministic checks, review result, and merge record belong in the execution packet rather than this protected plan file. An unresolved matrix cell, artifact mismatch, class-loading failure, content loss, content duplication, coupled viewer setting, combined-viewer conflict, or undocumented compatibility change keeps the phase open.

## Next Transition

After the Phase 002 completion packet passes all exit criteria and GitHub confirms the phase integration is merged, fetch and verify that the authoritative `master` contains the merge result. Hand the exact optional-artifact manifest, configuration fixtures, runtime matrix, JAR inspection, documentation evidence, and issue-specific acceptance summaries to `CORE-PHASE-003` and the later plan-wide compatibility harness. Start `CORE-PHASE-003` from that updated approved baseline. Do not begin `CORE-PHASE-003` while Phase 002 checks, review, integration, or evidence reconciliation remain incomplete.

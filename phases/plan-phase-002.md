# Phase 002 Execution Plan

> **Plan ID:** PLAN-PHASE-002
> **Phase ID:** CORE-PHASE-002
> **Owner:** OptionalIntegrations
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 002 of 006
> **Execution state:** COMPLETED through pull request `#28`, merge commit `1cc3d5e6d7227a147b2096c8b85253b083b57a2a`, and verified signed annotated tag `3.0.4-phase-002`.

## Purpose and Ownership

This phase restored the optional integration behavior owned by `CORE-REQ-003` and `CORE-REQ-004`. It made Curios 9.5.1 slot enforcement resolve through the supported public API while remaining safe when Curios is absent, and it made JEI and EMI independently configurable and concurrently functional without turning any integration into a hard dependency. The master plan owns the product contract, locked owner decisions, and global phase order. This blueprint preserves the dependency-ordered implementation contract and verified completion record for Curios, JEI, and EMI behavior in Phase 002.

The sole upstream phase dependency was `CORE-PHASE-001`. Phase 002 began only after Phase 001 was integrated into authoritative `master` as merge commit `09b18ad5b91a8c5b59faf1d35821f5c786427b80` and its signed annotated tag `3.0.4-phase-001` was verified. `EXT-001` was a required public artifact and test input, not a phase dependency. The Phase 000 issue reproductions and artifact claims were preserved evidence carried through the Phase 001 baseline, not another dependency edge.

Phase 002 neither required nor produced the corrected recipe-lock serialization, recipe-output runtime proof, or corrected packaged candidate JAR owned by `CORE-PHASE-003`. Its completed evidence is not contingent on future Phase 003 recipe evidence. It did not implement `CORE-REQ-012` or any inventory-target resolver. Historical task wording below records the contract that was executed and cannot reopen this completed phase.

## Evidence-Based Entry State

| Evidence class | Area | Verified finding | Evidence identity | Invalidation boundary |
|---|---|---|---|---|
| VERIFIED | Sequential entry | Phase 002 began from the completed Phase 001 integration on authoritative `master` | Phase 001 merge `09b18ad5b91a8c5b59faf1d35821f5c786427b80` and signed tag `3.0.4-phase-001` | Historical entry remains valid unless repository history or tag identity is proven corrupt |
| VERIFIED | Phase scope | `CORE-REQ-003` and `CORE-REQ-004` were the only canonical requirements implemented by this phase | Master plan roadmap and pull request `#28` | A later compatibility failure returns to the owning current phase; it does not silently rewrite this history |
| VERIFIED | External artifact input | Public Curios, JEI, and EMI artifacts satisfied `EXT-001` as a test input | [3.0.4 optional integration artifact record](../docs/verification/3.0.4-optional-integration-artifacts.md) | A changed artifact identity invalidates only evidence that used that artifact and requires the current owning phase to rerun it |
| VERIFIED | Curios integration | Curios 9.5.1 used the supported capability API boundary and passed present, absent, transition, and inventory-conservation coverage | Phase 002 candidate `9ba46b188c55d7b8d7346c99ca95132063ae2b0f` and artifact record | Later adapter or resolved-artifact changes require `CORE-PHASE-004` or the then-current owning phase to revalidate |
| VERIFIED | Recipe viewers | JEI and EMI controls were independent, missing values defaulted enabled, and individual, combined, disabled, and absent cells passed the recorded matrix | Phase 002 candidate and artifact record | Later viewer, configuration, network, or artifact changes require downstream revalidation |
| VERIFIED | Packaged output | The Phase 002 JAR contained the ProgressiveStages adapters without bundling Curios, JEI, or EMI implementation classes | JAR hashes and inventory in the artifact record | A later candidate is a distinct artifact and must receive its own inspection |
| VERIFIED | Integration | Pull request `#28` merged the completed implementation into `master`; required deterministic checks reported success or an applicable documented skip | Merge commit `1cc3d5e6d7227a147b2096c8b85253b083b57a2a` | Historical completion remains fixed unless the merge record is proven invalid |
| VERIFIED | Completion tag | The signed annotated Phase 002 tag resolves to the exact merged commit | `3.0.4-phase-002` -> `1cc3d5e6d7227a147b2096c8b85253b083b57a2a` | A signature or target mismatch is a repository-integrity failure, not permission to rerun later phases out of order |
| VERIFIED | Transition | The only next phase is `CORE-PHASE-003`, whose observed branch descends from the Phase 002 merge | `origin/envy/3.0.4-phase-003` at the master-plan observation | Phase 003 execution remains governed by its own blueprint and current remote reconciliation |

## Scope Boundaries

### Included Scope

- `CORE-REQ-003` restored Curios slot gating through an `EXT-001` verified public API resolution strategy, preserved authoritative inventory contents across grant, revoke, reconnect, reload, repeated enforcement, and full-inventory transitions, and proved startup safety with Curios absent.
- `CORE-REQ-004` provided separate JEI and EMI enabled settings, treated a missing setting as enabled, preserved readable EMI configuration, and covered JEI-only, EMI-only, combined, disabled, and absent combinations.
- Optional adapter diagnostics, test fixtures, configuration compatibility proof, packaged-artifact inspection, documentation, and issue `#8` and `#10` completion evidence were included.

### Explicit Exclusions

- `CORE-REQ-002` entity-presence performance belonged to completed `CORE-PHASE-001` and was not mixed into optional integration adapters.
- `CORE-REQ-005`, `CORE-REQ-006`, `CORE-REQ-007`, and `CORE-REQ-011` editor, in-game UI, artifact-parity, and recipe-lock serialization work belongs to `CORE-PHASE-003`.
- Recipe-viewer work was limited to JEI and EMI visibility adapters. It did not change Easy Builder recipe-rule serialization, TOML recipe keys, draft persistence, or runtime recipe-lock enforcement.
- `CORE-REQ-008` plan-wide compatibility verification belongs to `CORE-PHASE-004`. Phase 002 supplied its optional-integration inputs but did not close that requirement.
- `CORE-REQ-012` inventory insertion schema, selector pairing, inventory-target resolver registry, extension contracts, editor catalogs, menu transaction classification, enforcement, diagnostics, and runtime proof belong to `CORE-PHASE-003`. Phase 002 did not register target identities or intercept inventory insertion transactions.
- Shared optional-integration discovery and class-loading changes had to remain contribution-local. An absent integration could omit only its own adapter and could not disable another contribution, change vanilla inventory behavior, or load absent-mod classes.
- `FUT-001`, `FUT-002`, `FUT-003`, and `FUT-004` remained excluded.
- `NG-001`, `NG-002`, and `NG-003` continued to prohibit unscoped progression redesign, hard optional dependencies, and hot-path registry scans.
- No package correction based on memory, an unverified class name, a private artifact, or an unsupported integration version was allowed.

## Phase Contract

### CORE-PHASE-002 — Restore optional integration behavior

**Objective:** Produce verified Curios slot enforcement and independent, default-enabled, concurrently operable JEI and EMI adapters while preserving soft-dependency startup safety

**Owner:** OptionalIntegrations

**Dependencies:** CORE-PHASE-001

**Required external artifact and test input:** EXT-001

**Canonical requirements:** CORE-REQ-003, CORE-REQ-004

**Documentation and release impact:** Record verified Curios behavior, absent-mod behavior, independent JEI and EMI settings, defaults, coexistence, diagnostics, supported artifact identities, runtime matrix, and issue `#8` and `#10` evidence

**Next transition:** CORE-PHASE-003

**Entry criteria**

- `CORE-PHASE-001` completed its acceptance, verification, pull request, authoritative-default, and signed-tag gates before Phase 002 began.
- `P002-TASK-001` used a fresh remote comparison to establish the `CORE-PHASE-001` merge as the Phase 002 starting baseline. No saved-goal checkout, Phase 000 artifact, or runtime cache substituted for that sequential check.
- `CORE-REQ-003` and `CORE-REQ-004` received issue `#8` and issue `#10` reproduction and artifact-verification state from the preserved Phase 000 completion packet as evidence input.
- `EXT-001` supplied authoritative source, compatibility, exact version, license provenance, security review, SHA-256, and SHA-512 evidence for the Curios, JEI, and EMI test artifacts. This artifact contract gated tasks that used those artifacts but did not create a phase-dependency edge.
- `EXT-001` artifacts matched Minecraft 1.21.1 and NeoForge 21.1.219, and `P002-TASK-002` captured the existing optional-integration configuration and behavior as compatibility fixtures.
- `CORE-PHASE-003` corrected recipe results, recipe workflows, and candidate JARs were not Phase 002 entry criteria.

**Implementation scope**

- `CORE-REQ-003` resolved the supported Curios public API from the verified artifact set, confined Curios linkage behind the optional adapter boundary, restored the documented slot behavior, and conserved inventory contents across lifecycle transitions.
- `CORE-REQ-004` separated the JEI and EMI enable decisions, defaulted each missing setting to enabled, delivered the same normalized authoritative visibility decision to both installed adapters, and prevented one absent or disabled adapter from affecting the other.
- `CORE-REQ-003` permitted only a one-time optional-adapter API-resolution cache. Its key and validity boundary were the exact installed artifact identity and the adapter lifecycle that loaded it. A changed artifact identity or lifecycle rebuild required fresh resolution. It neither consumed nor shared the Phase 001 player-tick condition-context snapshot, and it was not evidence of a valid branch baseline.
- `CORE-REQ-003` and `CORE-REQ-004` kept shared optional discovery neutral to the inventory-target resolver registry owned by `CORE-PHASE-003`. Phase 002 did not implement resolver behavior.

**Execution order**

1. `P002-TASK-001` executed the `CORE-PHASE-001` sequential-entry proof and froze the `EXT-001` artifact identities required by `CORE-REQ-003` and `CORE-REQ-004`.
2. `P002-TASK-002` converted the `CORE-REQ-003` and `CORE-REQ-004` findings into lifecycle fixtures and the complete `DEC-003` viewer matrix.
3. `P002-TASK-003` and `P002-TASK-004` implemented and verified the `CORE-REQ-003` Curios adapter and inventory-conservation behavior.
4. `P002-TASK-005` and `P002-TASK-006` implemented and verified the `CORE-REQ-004` independent JEI and EMI settings and adapter behavior.
5. `P002-TASK-007` joined the `CORE-REQ-003` and `CORE-REQ-004` streams in the supported runtime matrix.
6. `P002-TASK-008` inspected the `CORE-REQ-003` and `CORE-REQ-004` packaged output and assembled the completion packet.

**Required evidence**

- `CORE-REQ-003` required the exact `EXT-001` artifact identities and hashes, resolver tests, present and absent startup proof, lifecycle transition evidence, and item-conservation assertions.
- `CORE-REQ-004` required independent-default tests and the JEI-only, EMI-only, combined, mixed-disabled, both-disabled, and absent runtime cells.
- `P002-TASK-008` required the Phase 002 JAR inventory and hashes, documentation links, clean-diff inspection, merge record, and signed annotated phase-tag verification.

**Exit criteria**

- `CORE-REQ-003` Curios 9.5.1 enforcement, lifecycle, conservation, and absent-mod evidence passed against the frozen `EXT-001` identity.
- `CORE-REQ-004` independent default-enabled JEI and EMI configuration, combined operation, disabled states, and absent-mod evidence passed.
- `P002-TASK-001` through `P002-TASK-008` completed with no known mandatory Phase 002 defect.
- No known mandatory phase-owned defect remains.
- Pull request `#28` merged the Phase 002 result as commit `1cc3d5e6d7227a147b2096c8b85253b083b57a2a`, and signed annotated tag `3.0.4-phase-002` verified at that exact commit.
- The only next transition is `CORE-PHASE-003`.

## Inputs and Upstream Contracts

| Input or contract | Provider | Relationship | Required historical state | Validation and failure behavior |
|---|---|---|---|---|
| Sequential integration baseline | `CORE-PHASE-001` | Sole upstream phase dependency | Merged, present on authoritative `master`, and signed-tag verified | A mismatch would have stopped Phase 002 before branch work; it could not be replaced by another input |
| Issue and artifact baseline | `CORE-PHASE-000`, carried through Phase 001 | Preserved evidence input | Issues `#8` and `#10` identified exact expectations, observations, and requirement ownership | A stale row required evidence correction without creating a new phase edge |
| Public optional artifacts | `EXT-001` | Required external artifact and test input | Exact supported versions with authoritative origin, compatibility, license, security boundary, SHA-256, and SHA-512 | A mismatch stopped artifact-dependent tasks; no unverified substitute was permitted |
| Recipe-viewer decision | `DEC-003` | Locked decision input | Independent default-enabled JEI and EMI settings with concurrent operation | Coupled settings or disabled missing values were rejected |
| Existing configuration | Master compatibility contract | Compatibility input | Supported files, including existing EMI configuration, remained readable | Parser or migration defects required correction and fixture rerun |
| Core visibility decision | Core rule engine | Architecture input | Adapters consumed authoritative state without owning policy | Any invented or mutated viewer policy blocked acceptance |
| Inventory-target resolver seam | `CORE-PHASE-003` master boundary | Downstream compatibility constraint only | Shared discovery remained contribution-local without resolver implementation | Coupling had to be removed; it did not authorize Phase 003 implementation in Phase 002 |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P002-TASK-001 | CORE-REQ-003, CORE-REQ-004 | Verified Phase 001 integration and its signed tag, reconciled preserved Phase 000 evidence, then validated, hashed, and froze the supported public optional-artifact set | `CORE-PHASE-001` as the sole phase dependency; Phase 000 packet as evidence input; `EXT-001` as an external artifact input | Sequential entry record and versioned artifact manifest | Authoritative branch and tag boundary; optional-artifact manifest | Remote and merge comparison, tag verification, artifact identity and compatibility checks, provenance and license review, security boundary review, SHA-256 and SHA-512 |
| P002-TASK-002 | CORE-REQ-003, CORE-REQ-004 | Converted the preserved Curios and recipe-viewer findings into lifecycle fixtures and a complete viewer matrix | P002-TASK-001, Phase 000 evidence, `DEC-003` | Reproducible expected, negative, and recovery cases | Curios lifecycle fixtures; recipe-viewer matrix | Fixture dry run and baseline comparison |
| P002-TASK-003 | CORE-REQ-003 | Restored Curios resolution through the verified public API while isolating optional classes | P002-TASK-001, P002-TASK-002 | Version-tolerant Curios bridge and absent-mod safety | Curios adapter and optional class-loading boundary | Resolver tests and present or absent startup smoke tests |
| P002-TASK-004 | CORE-REQ-003 | Restored slot enforcement and proved transition conservation | P002-TASK-003 and the documented slot contract | Correct allow, deny, ejection, and retention outcomes without loss or duplication | Curios slot enforcement and authoritative inventory contents | State-by-state identity and count assertions, repeated enforcement, reconnect, reload, revoke, grant, and full-inventory coverage |
| P002-TASK-005 | CORE-REQ-004 | Added independent JEI and EMI controls with compatible default-enabled missing values | `DEC-003`, P002-TASK-002 | Two independent normalized configuration decisions | JEI and EMI configuration normalization | Missing, true, false, readable EMI, malformed, and reload tests |
| P002-TASK-006 | CORE-REQ-004 | Isolated viewer adapters and supported simultaneous authoritative visibility updates | P002-TASK-005 and the core visibility boundary | Coexisting adapters with independent disable behavior | JEI and EMI adapter activation and synchronized visibility | JEI-only, EMI-only, combined, mixed-disabled, both-disabled, and absent tests |
| P002-TASK-007 | CORE-REQ-003, CORE-REQ-004 | Executed the supported client, dedicated-server, reconnect, reload, lifecycle, and optional-mod matrix | P002-TASK-003 through P002-TASK-006 and the frozen `EXT-001` artifacts | Runtime evidence for every required combination | Supported client and dedicated-server integration boundaries | Exact-version logs or screenshots, server smoke results, configuration snapshots, inventory conservation, and artifact identities |
| P002-TASK-008 | CORE-REQ-003, CORE-REQ-004 | Inspected the packaged output, updated verified documentation, and assembled downstream evidence | P002-TASK-007 and the clean Phase 002 build artifact | Final phase packet and compatibility handoff | Packaged JAR, operator documentation, and downstream evidence packet | JAR listing and hashes, documentation links, clean-diff inspection, and completion checklist |

`P002-TASK-001` was the hard sequential entry gate. It distinguished the sole phase dependency from preserved evidence and from the external artifact input. `P002-TASK-002` froze the fixtures. The Curios stream in `P002-TASK-003` and `P002-TASK-004` and the recipe-viewer stream in `P002-TASK-005` and `P002-TASK-006` joined in `P002-TASK-007`; `P002-TASK-008` then closed artifact and documentation evidence. Their completed status is historical. Later changes do not reopen these tasks; the current owning phase reruns invalidated compatibility cells.

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed historical state | Compatibility boundary | Evidence |
|---|---|---|---|---|
| Sequential integration record | `CORE-PHASE-003` | Phase 002 began from Phase 001 and completed through its own merge and signed tag | Contiguous sequence only | Phase 001 and Phase 002 merge and tag identities |
| Curios adapter boundary | `CORE-PHASE-004` | Supported public resolution worked when present and no Curios class loaded when absent | Valid for the recorded `EXT-001` identity; no hard dependency | Resolver tests, startup logs, JAR inspection |
| Curios transition behavior | `CORE-PHASE-004` | Enforcement conserved authoritative contents across grant, revoke, reconnect, reload, repeated sweep, and full inventory | No persisted-schema change | GameTest and runtime evidence |
| Independent viewer settings | `CORE-PHASE-004` | Each viewer had an independent enabled value and a missing value resolved enabled | Existing EMI configuration remained readable | Configuration tests and reload evidence |
| Concurrent viewer adapters | `CORE-PHASE-004` | Both installed adapters consumed the same authoritative visibility state and remained independently disabled | Each integration remained optional | Viewer unit, sync, startup, and real stage-change evidence |
| Issue evidence packet | `CORE-PHASE-005` and later release operations | Issues `#8` and `#10` received merge-ready acceptance evidence tied to exact source and artifacts | Closure remains a final release action | Artifact record and issue summaries |
| Documentation delta | Operators and pack authors | Documentation described only verified Curios and viewer behavior | A later artifact must be revalidated separately | Documentation diff and links |

## Architecture and Implementation Boundaries

The logical server remained authoritative for stage ownership, rule decisions, Curios slot enforcement, and inventory mutation. The Curios adapter translated that decision into the supported public API without owning stage policy. JEI and EMI adapters consumed synchronized authoritative visibility state for presentation only.

Optional integration linkage remained behind runtime availability checks and isolated adapter boundaries. Common initialization, dedicated-server startup, packet handling, and configuration parsing could not require Curios, JEI, or EMI classes when the corresponding mod was absent. Absence or incompatibility disabled only that adapter. Diagnostics identified the integration and failed boundary without private data or hot-path repetition.

Configuration produced two independent normalized booleans. Each missing value resolved enabled under `DEC-003`; an explicit false value disabled only its matching adapter. When both viewers were installed and enabled, both received the same normalized decision. Existing readable EMI settings remained compatible, and no unrelated configuration or stage schema changed.

Curios enforcement was idempotent and inventory-conserving. Grant, revoke, reconnect, reload, repeated sweep, and full-inventory paths followed the documented policy. No broad registry scan, per-render filesystem access, or recurring hot-path reflection lookup was allowed. API resolution occurred once for the exact installed artifact identity and could not outlive or be reused for another identity.

`CORE-PHASE-003` remained the sole owner of inventory-target resolver identities, tags, catalogs, player-origin rules, menu interception, and insertion outcomes. Phase 002 preserved the contribution-local discovery boundary without defining or accepting any part of `CORE-REQ-012`.

## Failure, Recovery, and Edge Cases

| Scenario | Required behavior | Recovery contract | Regression evidence |
|---|---|---|---|
| Phase 001 absent, unmerged, unsigned, or mismatched | No Phase 002 work or stacked branch | Complete Phase 001, fetch authoritative `master`, and repeat P002-TASK-001 | Matching merge, baseline, and tag identities |
| `EXT-001` field or checksum missing or mismatched | Stop only artifact-dependent work before selecting an API path | Re-resolve from the authoritative source and replace the artifact packet after review | Matching SHA-256 and SHA-512 plus compatibility evidence |
| Curios absent | Start core and dedicated server without loading Curios classes | Disable only the Curios adapter | Absent-mod startup and class-loading evidence |
| Verified Curios surface incompatible | Emit one actionable diagnostic and avoid item mutation | Correct the adapter against verified public evidence | Resolver failure and untouched-inventory assertion |
| Repeated Curios enforcement | Produce the same final inventory without duplication or loss | Restore the fixture and correct idempotency | Item identity and count conservation |
| Full inventory | Follow safe retention or normal ejection behavior without loss or duplication | Restore the fixture and correct transition handling | Full-inventory conservation assertion |
| One viewer absent or disabled | Keep the other viewer active | Correct adapter activation isolation | JEI-only, EMI-only, and mixed-disabled cells |
| Both viewers installed and enabled | Apply the same authoritative visibility decision in both | Remove registration or refresh short circuits | Combined viewer comparison and refresh evidence |
| Viewer setting missing or malformed | Default a missing field enabled; handle malformed input without mutating the sibling decision | Restore valid configuration and correct field-specific handling | Missing, malformed, restart, and sibling-value tests |
| One adapter fails during refresh | Contain failure to that adapter and preserve server authority and the other viewer | Correct or disable only the failing adapter and retry cleanly | Controlled failure and unaffected-adapter evidence |
| Shared discovery coupled to one mod | Omit only the absent adapter and preserve independent contributions and vanilla behavior | Remove the global gate or eager linkage | Static boundary inspection and affected absent-mod fixture |

## Verification Matrix

| Requirement or task | Static or unit | Integration | Runtime | Negative and recovery | Recorded artifact |
|---|---|---|---|---|---|
| P002-TASK-001 | Merge, signature, manifest, and hash checks | Authoritative `master` and exact artifact resolution | Supported public artifacts resolved | Invalid phase state, provenance gap, incompatibility, or checksum mismatch rejected | Sequential entry record and `EXT-001` manifest |
| CORE-REQ-003 | Resolver and conservation tests | Curios present and absent startup plus lifecycle transitions | Documented slot behavior under Curios 9.5.1 | Absent mod, incompatible surface, repeated enforcement, full inventory, and recovery | Curios matrix, server logs, and inventory record |
| P002-TASK-004 | Idempotency and item-count assertions | Stage and lifecycle transition suite | Repeated enforcement before and after authoritative state changes | Known inventory fixture restored after failure | Transition conservation report |
| CORE-REQ-004 | Independent defaults and activation tests | JEI-only, EMI-only, combined, mixed-disabled, both-disabled, and neither-installed | Real JEI and EMI stage-change refresh workflows plus startup cells | Missing and malformed settings, absent classes, and isolated adapter behavior | Viewer matrix, logs, and configuration fixtures |
| P002-TASK-005 | Parser, default, and reload tests | Existing EMI fixture plus independent JEI field | Explicit and missing settings loaded and reloaded | Invalid field left the sibling decision unchanged | Configuration compatibility report |
| P002-TASK-006 | Activation and synchronization isolation | Both adapters consumed authoritative visibility state | Each adapter remained active when the other was disabled | Cross-adapter short circuit rejected | Combined and isolated viewer evidence |
| P002-TASK-007 | Result-consistency checks | Complete optional-mod matrix | Client and dedicated-server smoke plus recorded real viewer refresh | Failed cells rerun from clean state | Versioned runtime matrix with artifact identities |
| P002-TASK-008 | Documentation, build, JAR listing, and hash checks | Final Phase 002 build consumed the verified adapters and settings | Packaged artifact inspected | Bundled dependencies, unconditional linkage, stale docs, and unrelated drift rejected | JAR inventory, hashes, documentation diff, and completion packet |

Fixtures pinned the source revision, exact optional artifact identities, Minecraft 1.21.1, NeoForge 21.1.219, Java 21, configuration values, stage state, starting inventory, expected recipe visibility, and expected final inventory. Each client combination used a clean launch. Server transition fixtures restored known inventory and stage state. Source presence, compilation, or a screenshot alone could not replace the required runtime cell.

No Phase 003 recipe candidate, canonical recipe-output result, or corrected-runtime evidence appears in this verification matrix because none belonged to Phase 002. Phase 004 owns integrated-candidate revalidation if later changes invalidate an optional-integration surface.

## Documentation, Operations, and Release Evidence

- `README.md`, `DOCUMENTATION.md`, and `docs/README.md` were updated for the verified Curios 9.5.1 boundary, absent-mod behavior, independent JEI and EMI settings, defaults, coexistence, and diagnostics.
- [3.0.4 optional integration artifact record](../docs/verification/3.0.4-optional-integration-artifacts.md) records the public artifact identities, license and compatibility inspection, security-review boundary, matrix results, JAR hashes, and package inventory.
- Issue `#8` and issue `#10` evidence was prepared against the tested source and artifacts. Issue closure remains gated by later integration and release completion.
- No migration beyond compatible missing-value defaults and continued readable EMI configuration was authorized.
- The evidence packet is an input to Phase 004 compatibility proof and later release closure. It is not a release artifact and cannot substitute for inspection of a later candidate.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Evidence invalidated | Required downstream response |
|---|---|---|---|---|
| Optional artifact identity changes | Pin coordinates and hashes; key resolution only to the installed identity | Artifact or checksum mismatch | Resolver, runtime, provenance, and security evidence for that artifact | Re-resolve and rerun affected cells in the current owning phase |
| Optional classes leak into common startup | Isolated adapters and absent-mod tests | Startup or class-loading failure | Absent-mod and JAR safety evidence | Correct the boundary and repeat static and startup checks |
| Shared optional loading suppresses another contribution | Contribution-local availability checks | Shared lifecycle inspection or independent-contribution failure | Shared-discovery compatibility evidence | Remove coupling without implementing resolver behavior and rerun affected cells |
| Curios transition loses or duplicates contents | Idempotent enforcement and conservation assertions | Identity or count mismatch | Curios acceptance and compatibility handoff | Correct transition behavior and repeat every Curios lifecycle cell |
| Viewer settings share an activation path | Independent normalized values and mixed-disabled tests | Wrong adapter changes state | Configuration and coexistence evidence | Separate activation and repeat configuration and viewer matrices |
| Combined viewers overwrite or short-circuit | Isolated adapters consume one authoritative state | JEI and EMI disagree after one transition | Combined and single-viewer evidence | Correct refresh isolation and repeat all viewer cells |
| Development resources differ from the JAR | Build and inspect the packaged artifact | Packaged behavior or entries differ | Runtime and documentation claims | Rebuild, inspect, and rerun affected runtime evidence |
| Platform or integration upgrade enters the work | Compare metadata to the pinned platform and artifact manifest | Unapproved version drift | Build, compatibility, and runtime evidence | Remove unrelated drift and repeat affected validation |

Historical evidence remains attached to its exact source and artifact identities. A downstream change invalidates only affected evidence and is handled by the then-current phase. It does not erase the verified Phase 002 merge or authorize a noncontiguous transition.

## Phase Completion Packet

Phase 002 completed the stable task set `P002-TASK-001` through `P002-TASK-008`. The completion packet identifies the verified Phase 001 entry baseline, Phase 002 source revision `9ba46b188c55d7b8d7346c99ca95132063ae2b0f`, public Curios, JEI, and EMI artifact identities and hashes, compatibility and license findings, security-review boundary, focused unit and contract tests, Curios GameTest and present or absent server starts, inventory transition conservation, independent viewer configuration, JEI and EMI startup combinations, real singleplayer stage-change refresh evidence for each viewer, packaged-JAR hashes and inventory, documentation changes, and issue-specific evidence.

Pull request `#28` merged `envy/3.0.4-phase-002` into `master` as `1cc3d5e6d7227a147b2096c8b85253b083b57a2a` on 2026-09-02. Its recorded quality checks passed, with documentation and dependency-submission jobs skipped where the workflow marked them inapplicable. The signed annotated tag `3.0.4-phase-002` verifies and resolves to that exact merge commit. The verified remote default at plan repair also resolved to the same commit. These facts close Phase 002 historically.

## Next Transition

The only permitted successor is `CORE-PHASE-003`. That transition begins from the verified Phase 002 merged and signed baseline and follows the Phase 003 blueprint. The Phase 002 artifact manifest, configuration fixtures, runtime matrix, JAR inspection, documentation evidence, and issue-specific summaries are preserved as downstream inputs; they do not add another dependency edge.

Phase 003 alone owns `CORE-REQ-005`, `CORE-REQ-006`, `CORE-REQ-007`, `CORE-REQ-011`, and `CORE-REQ-012`, including corrected recipe serialization, persisted reload, denied-and-eligible recipe runtime proof, inventory insertion rules and resolver contracts, client UI proof, and its packaged candidate JAR. Phase 002 requires none of those future results for its completion. No transition may skip Phase 003, return to Phase 001 as the next phase, or proceed directly to Phase 004.

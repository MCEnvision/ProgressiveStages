# Phase 000 Execution Plan

> **Plan ID:** PLAN-PHASE-000
> **Phase ID:** CORE-PHASE-000
> **Owner:** RepositoryAudit
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 000 of 006
> **Execution state:** COMPLETED through merge commit `dc9e154871781de262ffd5eb401d65d0fa44cefb` on `master`, with verified signed annotated tag `3.0.4-phase-000` at that commit.

## Purpose and Ownership

CORE-PHASE-000 froze the defect, artifact, and implementation-seam baseline for the 3.0.4 polish release. It owned only CORE-REQ-001. Its report set contained exactly issues `#8`, `#10`, `#11`, `#16`, `#24`, and `#25`. It reproduced or artifact-verified each report where the historical environment allowed, recorded honest missing-evidence classifications elsewhere, reconciled advertised 3.0.3 behavior with the shipped artifact, and assigned every result to a later canonical requirement.

The phase separately froze SRC-009 and the implementation seams that CORE-REQ-012 could extend. SRC-009 is an owner-promoted feature source, not an issue. It was not reproduced, classified, closed, or counted as a seventh baseline report.

The issue `#25` audit distinguished recipe-output locks in `[recipes].locked_items`, exact-recipe locks in `[recipes].locked_ids`, the forbidden generic `[recipes].locked` alias, and the generic `[[rules]]` representation observed in the shipped editor bundle. Phase 000 owned only the historical observation and downstream handoff. Corrected serialization, persistence, reload, enforcement, recovery, and packaged-artifact proof belong to CORE-PHASE-003 and were never Phase 000 completion gates.

## Evidence-Based Entry State

The immutable saved goal records checkout `5b3077764907249b3711886cca538794f6139acf` as goal-creation provenance. That commit does not select, freeze, or recreate the live Phase 000 execution baseline. It is not a required source pin for any current or future phase.

Phase 000 execution used the repository state proven by the completed evidence chain. These identities are historical evidence, not live branch pins:

| Role | Identity | Meaning |
|---|---|---|
| Authoritative default at phase collection | `origin/master` at `ab4ad138e1f74eec82cf0392a47ac1e54dd66d01` | No-predecessor starting baseline established by remote and repository reconciliation |
| Completed phase branch result | `f3a23155a835c5f3056a9c5a2c9d08ae4e9f1e5f` | Final Phase 000 evidence reconciliation committed on `envy/polish-3.0.4-plan` |
| Integrated result | `dc9e154871781de262ffd5eb401d65d0fa44cefb` | Merge commit on `master`, with the prior default as first parent and the completed phase result as second parent |
| Completion tag | `3.0.4-phase-000` | Verified signed annotated tag resolving to the integrated result |

Later default-branch movement does not reopen Phase 000 or turn goal-creation provenance into an execution baseline. Draft status language inside earlier evidence records is superseded by the completed reconciliation, merge ancestry, and verified signed tag. The observations and artifact identities in those records remain preserved evidence.

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| VERIFIED | Repository and integration | The intended repository, starting default, phase branch result, merge ancestry, and signed phase tag were proven | Phase 000 reconciliation, merge commit `dc9e154871781de262ffd5eb401d65d0fa44cefb`, and `git tag -v 3.0.4-phase-000` | A contradictory signed-tag result or repository-identity mismatch invalidates the completion identity; later default-branch movement does not |
| VERIFIED | Issue count | The baseline contains exactly `#8`, `#10`, `#11`, `#16`, `#24`, and `#25` | Phase 000 issue matrix and CORE-REQ-001 reconciliation | A verified duplicate, omission, or issue-identity mismatch invalidates the count |
| VERIFIED | Shipped artifact | ProgressiveStages 3.0.3 for Minecraft 1.21.1 and NeoForge was identified as a 1,758,353-byte JAR with SHA-256 `5c9a290a64629b33953d587a677c6bcf530ad523ad944b63c12d2750c8d1797a` and SHA-512 `2d9561af1f982d1fb1f9678df248bc240c4f2247811cd4fe7ae73a69606a8d931220c5ba07ccf01dbb1ee67efda1fdcbb8f3e967ffbeb1df6d48f70fdde6dca2` | Phase 000 artifact manifest and checksum verification | A hash, size, version, or metadata mismatch invalidates every artifact-derived conclusion |
| VERIFIED | Packed editor bundle | `assets/progressivestages/editor/app.js` had SHA-256 `d0c966b1cfb094de6b9e31b5231e95eb6a20efb19497b96aadfa1e6760289576` | Phase 000 JAR inventory | A different bundle hash invalidates only bundle-specific observations |
| VERIFIED | Curios API mismatch evidence | The shipped compatibility class referenced the old inventory package while the pinned Curios 9.5.1 artifact exposed `ICuriosItemHandler` in the capability package | Phase 000 optional-artifact inspection for issue `#8` | A different Curios or shipped-JAR identity requires a new compatibility observation in its owning phase |
| OBSERVED | Recipe-lock defect | The historical editor workflow persisted a generic `[[rules]]` recipe target after exact-recipe authoring, while canonical parser fields remained distinct | Phase 000 issue `#25` workflow record | Corrected Phase 003 evidence supersedes behavior conclusions but never rewrites this historical observation |
| VERIFIED | Inventory insertion source role | SRC-009 promoted CORE-REQ-012 separately from the six-report baseline | SRC-009 and DEC-007 | An owner contract revision may change scope, but implementation evidence cannot turn SRC-009 into a seventh issue |
| VERIFIED | Advertised parity result | No independently proven missing advertised 3.0.3 capability existed beyond the owned report rows | Phase 000 public-claim matrix | A newly attributable 3.0.3 claim with shipped-artifact evidence invalidates this conclusion and routes through the master plan |

## Scope Boundaries

### Included Scope

- CORE-REQ-001 only: freeze repository, issue, release artifact, public claim, optional artifact, owner-request, and evidence identities before interpretation.
- Capture exactly six normalized issue records with expected behavior, observed behavior, evidence surface, configuration, rerun count, classification, later owner, and missing proof.
- Inventory the shipped 3.0.3 JAR and packed editor bundle, and compare attributable public 3.0.3 claims with that artifact.
- Record only `reproduced`, `artifact_verified`, `stale_with_evidence`, or `blocked_by_missing_evidence` as issue classifications.
- Freeze the issue `#25` historical serialization observation while preserving the distinct canonical meanings of `[recipes].locked_items` and `[recipes].locked_ids`.
- Freeze SRC-009 as a separately typed owner request and map current parser, rule-model, selector, compiler, menu, destination-catalog, editor, reload, and test seams to CORE-REQ-012.
- Produce reproducible downstream fixture and acceptance handoffs without choosing or implementing a corrective design.
- Preserve EXT-001, EXT-002, and EXT-003 as typed downstream contracts without claiming that availability or authorization implied successful runtime or release evidence.

### Explicit Exclusions

- CORE-REQ-002 through CORE-REQ-012 implementation, compatibility execution, release validation, publication, and issue closure were outside this phase.
- Corrective Java, resource, editor, configuration, workflow, documentation, release, or GitHub issue changes were outside this phase.
- A corrected 3.0.4 recipe workflow, corrected packaged JAR, denied-versus-eligible crafting result, or rollback result was not needed for Phase 000 completion.
- CORE-REQ-012 was not a report and could not increase the fixed issue count from six.
- FUT-001 through FUT-004 and all unpromised or roadmap-only capability claims remained excluded.
- NG-001 through NG-004 remained binding. Source presence, release notes, or lower-fidelity checks could not become issue-closure evidence.

## Phase Contract

### CORE-PHASE-000 — Freeze the defect, inventory-interaction seam, and artifact baseline

**Objective:** Produce a reproducible audit matrix that classifies all six baseline reports, reconciles advertised 3.0.3 claims with the shipped artifact, separately freezes the SRC-009 inventory-insertion seams, and maps each result to a downstream requirement with measurable evidence.

**Owner:** RepositoryAudit

**Dependencies:** none

**Canonical requirements:** CORE-REQ-001

**Documentation and release impact:** Produce internal verification records only. Do not update release-facing documentation, publish an artifact, or change issue state.

**Next transition:** CORE-PHASE-001

**Entry criteria**

- `P000-TASK-001` began `CORE-REQ-001` only after the intended repository and live default branch were proven independently of the saved-goal creation checkout.
- `P000-TASK-001` froze the six issue identities and available sanitized attachments, configurations, and screenshots for `CORE-REQ-001`.
- `P000-TASK-001` uniquely identified the shipped 3.0.3 JAR, release identity, hashes, packed editor bundle, and attributable public claims for `CORE-REQ-001`.
- `P000-TASK-002` used test identities that recorded ProgressiveStages, Minecraft 1.21.1, NeoForge 21.1.219, and applicable optional integration versions for `CORE-REQ-001`.
- `P000-TASK-005` applied `DEC-002` to constrain `CORE-REQ-001` parity work to advertised or documented 3.0.3 behavior.
- `P000-TASK-003` represented `SRC-009` and `DEC-007` as the sole owner-promotion basis for `CORE-REQ-012`, separate from the `CORE-REQ-001` issue matrix.

**Implementation scope**

- `P000-TASK-001` froze all `CORE-REQ-001` repository, artifact, issue, claim, optional-input, owner-request, and evidence identities before interpretation.
- `P000-TASK-002` executed the bounded `CORE-REQ-001` reproduction or artifact-verification workflows and preserved missing proof honestly.
- `P000-TASK-003` completed the `CORE-REQ-001` shipped-artifact inventory, claim register, existing-test inventory, and observational `CORE-REQ-012` seam handoff.
- `P000-TASK-004` classified exactly six `CORE-REQ-001` issue rows with the locked vocabulary and strict stale-report rule.
- `P000-TASK-005` reconciled every bounded 3.0.3 public claim under `CORE-REQ-001`, `DEC-002`, and `FUT-001` without adding future scope.
- `P000-TASK-006` mapped every `CORE-REQ-001` report and the separate `SRC-009` record to one downstream requirement and phase.
- `P000-TASK-007` closed the `CORE-REQ-001` consistency, sanitation, count-integrity, integration, and signed-tag gates.

**Execution order**

1. `P000-TASK-001` executed the `CORE-REQ-001` identity freeze before any interpretation.
2. `P000-TASK-002` executed the `CORE-REQ-001` report workflows from the frozen identities.
3. `P000-TASK-003` executed the `CORE-REQ-001` artifact, claim, test, and seam inventories after `P000-TASK-001`.
4. `P000-TASK-004` classified the six `CORE-REQ-001` reports after `P000-TASK-002` and `P000-TASK-003`.
5. `P000-TASK-005` executed the `CORE-REQ-001` claim reconciliation after `P000-TASK-003` and `P000-TASK-004`.
6. `P000-TASK-006` executed the `CORE-REQ-001` downstream ownership mapping after `P000-TASK-003` through `P000-TASK-005`.
7. `P000-TASK-007` executed the final `CORE-REQ-001` consistency review and integration proof after `P000-TASK-001` through `P000-TASK-006`.

**Required evidence**

- `P000-TASK-001` required the `CORE-REQ-001` execution-identity manifest, exact six-issue list, artifact metadata, SHA-256 and SHA-512 hashes, and sanitized evidence index.
- `P000-TASK-002` required exactly six normalized `CORE-REQ-001` issue records with runnable workflow or explicit missing-evidence boundaries.
- `P000-TASK-003` required the `CORE-REQ-001` JAR inventory, packed-bundle identity, public-claim register, existing-test inventory, and observational `CORE-REQ-012` seam map.
- `P000-TASK-004` required the six-row `CORE-REQ-001` classification register with decisive evidence and no source-presence shortcut.
- `P000-TASK-005` required the `CORE-REQ-001` advertised-capability matrix and explicit excluded-claim rationale.
- `P000-TASK-006` required the `CORE-REQ-001` acceptance-traceability matrix with no orphan, duplicate owner, or seventh issue.
- `P000-TASK-007` required the completed `CORE-REQ-001` packet, merge commit `dc9e154871781de262ffd5eb401d65d0fa44cefb`, and verified signed tag `3.0.4-phase-000`.

**Exit criteria**

- `P000-TASK-004` assigned supported `CORE-REQ-001` classifications and downstream owners to exactly six issue rows.
- `P000-TASK-004` proved no `CORE-REQ-001` report was marked stale only because current source appeared to contain a possible fix.
- `P000-TASK-005` gave every bounded `CORE-REQ-001` public claim an artifact-backed result or explicit missing-evidence boundary and proved no additional `CORE-REQ-007` correction.
- `P000-TASK-006` proved the `SRC-009` seam map was sufficient for `CORE-PHASE-003` to execute `CORE-REQ-012` without treating it as a seventh issue.
- `P000-TASK-007` proved `CORE-REQ-001` artifact hashes, issue-count integrity, stable-ID ownership, sanitation, and external-boundary preservation.
- `P000-TASK-007` proved no mandatory `CORE-REQ-001` defect remained before integration.
- `P000-TASK-007` proved the completed `CORE-REQ-001` result was merged into `master` and signed annotated tag `3.0.4-phase-000` resolved to the merge commit.
- No known mandatory phase-owned defect remains.

### Historical Classification Register

These are the only report rows owned by the Phase 000 audit.

| Issue | Frozen classification | Decisive Phase 000 evidence | Downstream mapping |
|---|---|---|---|
| `#8` Curios 9.5.1 | `artifact_verified` | The packed adapter referenced `top.theillusivec4.curios.api.type.inventory.ICuriosItemHandler`; the pinned 9.5.1 artifact exposed it under `api.type.capability` | CORE-REQ-003, CORE-PHASE-002 |
| `#10` JEI with EMI | `artifact_verified` | Packed configuration exposed EMI enablement but no independent JEI setting, despite both integrations being optional in metadata | CORE-REQ-004, CORE-PHASE-002 |
| `#11` enchantment controls | `blocked_by_missing_evidence` | The packed editor contained both enchantment fields, and the matching JavaScript bundle completed a development workflow, but the installed 3.0.3 JAR workflow was not established | CORE-REQ-005, CORE-PHASE-003 |
| `#16` category overlay | `blocked_by_missing_evidence` | Static render-order evidence existed, but supported-GUI-scale shipped-artifact client evidence did not | CORE-REQ-006, CORE-PHASE-003 |
| `#24` entity-presence cost | `artifact_verified` | Packed tracking resolution reached condition-context construction on the tracked-entity path | CORE-REQ-002, CORE-PHASE-001 |
| `#25` visual recipe locks | `blocked_by_missing_evidence` | The historical exact-recipe workflow emitted a noncanonical generic rule, but no frozen run established the separate recipe-output item-selector workflow | CORE-REQ-011, CORE-PHASE-003 |

| Separate source record | Classification | Mapping |
|---|---|---|
| SRC-009 inventory insertion request | owner request, not an issue | CORE-REQ-012, CORE-PHASE-003 |

## Inputs and Upstream Contracts

| Input or contract | Provider | Required Phase 000 state | Validation and failure behavior |
|---|---|---|---|
| Owner request and decisions | SRC-001, DEC-001 through DEC-007 | Endpoint, scope, six-report count, and separate CORE-REQ-012 promotion matched the master | Reject scope expansion or a seventh report row |
| Repository execution identity | Authoritative remote and branch evidence | Live default, phase branch result, merge ancestry, and tag identity were recorded independently of goal provenance | Stop on repository mismatch or irreconcilable ancestry; never substitute a goal-creation checkout |
| Six reports | SRC-004 and SRC-008 | Exactly issues `#8`, `#10`, `#11`, `#16`, `#24`, and `#25` | Missing fields remained explicit; no report details were inferred |
| Shipped 3.0.3 artifact | Release provenance | Version, platform, size, hashes, metadata, and JAR inventory were reproducible | Reject rebuilt, ambiguous, or hash-mismatched substitutes |
| Public claims | SRC-002 and SRC-003 | Claims were attributable to public 3.0.3 material | Exclude ambiguous roadmap text from CORE-REQ-007 |
| Optional artifacts | EXT-001 | Artifact identity could seed later compatibility work | Availability alone could not prove a runtime matrix |
| Inventory insertion request | SRC-009 | Non-issue source role and CORE-REQ-012 relationship were explicit | Reject any issue-count inflation or scope beyond DEC-007 |
| Release prerequisites | EXT-002 and EXT-003 | Their exact state and later owner were preserved | Do not claim release validation or publication readiness |

## Outputs and Downstream Contracts

| Output | Consumer | Guaranteed state | Evidence boundary |
|---|---|---|---|
| Frozen six-issue matrix | CORE-PHASE-001 through CORE-PHASE-003 | Every report has an identity, classification, observation, owning requirement, and later proof obligation | Later evidence supersedes by explicit lineage and never changes the historical count |
| Entity-presence fixture | CORE-PHASE-001 | Issue `#24` supplies the reported hot path, mixed-player correctness boundary, and DEC-004 performance target | Phase 001 owns the repair and runtime profile |
| Optional-integration seed | CORE-PHASE-002 | Issues `#8` and `#10` supply artifact, configuration, and expected-behavior boundaries | Phase 002 owns runtime combinations and inventory conservation |
| Client and editor fixtures | CORE-PHASE-003 | Issues `#11`, `#16`, and `#25` preserve exact historical evidence surfaces and missing proof | Phase 003 owns corrected behavior and higher-fidelity evidence |
| Advertised-capability matrix | CORE-PHASE-003 | No independent CORE-REQ-007 correction was proved | Later work cannot promote an unadvertised feature through this audit |
| Inventory insertion seam map | CORE-PHASE-003 | SRC-009 remains separate; legacy parser, model, selector, compiler, menu, target, editor, reload, and test boundaries are traceable | The handoff is observational and preserves all legacy interaction forms |
| Acceptance traceability matrix | CORE-PHASE-001 through CORE-PHASE-006 | Each report outcome and the separate SRC-009 record have one canonical owner and measurable evidence | No duplicate ownership, orphan result, or seventh issue row |

## Work Packages

| Task ID | Requirement IDs | Historical work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P000-TASK-001 | CORE-REQ-001 | Froze repository execution identity, shipped release artifact, documentation, six issues, optional artifacts, SRC-009, and evidence sources before interpretation | SRC-001 through SRC-009, DEC-001 through DEC-007 | Execution identity manifest, artifact manifest, sanitized evidence index | Repository refs, release artifact identity, issue matrix, and source-role register | Cross-check proved intended repository, distinct goal provenance, artifact hashes, exactly six issues, and separate SRC-009 role |
| P000-TASK-002 | CORE-REQ-001 | Defined and ran bounded historical workflows for each issue where evidence allowed | P000-TASK-001 and the six report records | Exactly six normalized issue records | Client, server, editor, optional-integration, and performance evidence surfaces named by the six reports | Each record captured expected and observed behavior, evidence surface, rerun count, limitation, classification candidate, and later proof |
| P000-TASK-003 | CORE-REQ-001 | Inventoried the shipped JAR and packed editor, enumerated attributable public claims, and traced current CORE-REQ-012 seams | P000-TASK-001, shipped artifact, SRC-002, SRC-003, SRC-009 | JAR inventory, claim register, seam map, existing-test inventory | `StageFileParser`, `LockDefinition.InteractionLock`, selector grammar, `Schema4StageCompiler`, `AbstractContainerMenuMixin`, `BuiltinEditorSchemas`, `RulesPanel.tsx`, and existing editor tests | Hash recheck, metadata inspection, claim attribution, and seam completeness review passed |
| P000-TASK-004 | CORE-REQ-001 | Classified each issue using the allowed vocabulary and a strict stale-report test | P000-TASK-002, P000-TASK-003, NG-004 | Six-row classification table | Issue matrix and evidence-classification contract | Independent review derived every classification without relying on source presence |
| P000-TASK-005 | CORE-REQ-001 | Compared every bounded public claim with the shipped artifact | P000-TASK-003, P000-TASK-004, DEC-002, FUT-001 | Advertised-capability parity matrix | Shipped JAR inventory, public documentation claims, and release scope boundary | No separate missing advertised capability was proved, and excluded claims retained their rationale |
| P000-TASK-006 | CORE-REQ-001 | Mapped report outcomes and the separate inventory-insertion seam to downstream owners | P000-TASK-003 through P000-TASK-005 | Acceptance traceability and phase fixture handoffs | Master requirement ownership and contiguous phase contracts | No orphan or duplicate owner existed; issue `#25` mapped to CORE-REQ-011, while SRC-009 separately mapped to CORE-REQ-012 |
| P000-TASK-007 | CORE-REQ-001 | Audited consistency, issue-count integrity, seam completeness, sanitation, external boundaries, and packet identity | P000-TASK-001 through P000-TASK-006 | Frozen completion packet | Verification documents, merge ancestry, and signed phase tag | Independent review passed, the branch result merged, and the signed phase tag verified |

P000-TASK-001 through P000-TASK-007 completed in numeric order. Parallel evidence collection inside P000-TASK-002 and P000-TASK-003 did not alter their dependency order. A missing historical runtime fixture was retained as `blocked_by_missing_evidence` with a downstream owner; it was not converted into success, a new issue, or a Phase 000 implementation task.

## Architecture and Implementation Boundaries

Phase 000 was observational. It did not change runtime architecture, schemas, persistence, networking, configuration defaults, permissions, client rendering, optional adapters, editor code, or release workflows. Evidence identified whether it came from source inspection, a development runtime, the production editor bundle, the shipped JAR, an integrated server, or a dedicated server.

The CORE-REQ-012 seam audit recorded legacy parsing through `StageFileParser.parseInteractions`, the `LockDefinition.InteractionLock` model, selector grammar and safe-priority behavior, compilation through `Schema4StageCompiler.addGenericRules`, menu enforcement through `AbstractContainerMenuMixin`, editor schema definitions in `BuiltinEditorSchemas`, serialization in `RulesPanel.tsx`, and existing coverage in `EditorSchemaRegistryTest` and related fixtures. It recorded the absence of a paired destination selector, authoritative stable destination resolver, complete transaction classification, and matching test matrix. Absence was downstream gap evidence, not permission to invent an identifier or alter a legacy interaction.

The sole planned additive interaction identifier remained `item_into_inventory`. Existing `item_on_block`, `block_right_click`, and `item_on_entity` forms, selector behavior, priorities, safe ties, removal-only operations, and playerless automation were preserved as downstream compatibility boundaries.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| A `CORE-REQ-001` report lacked an attributable artifact or fixture | `P000-TASK-002` could not bind the report to a reproducible environment or pinned artifact | Use `blocked_by_missing_evidence`; never infer affected behavior | Append later proof with immutable identity and explicit supersession | `P000-TASK-004` classification audit rejects unsupported success or stale status |
| `P000-TASK-003` found source and shipped artifact disagreement | Hash-bound JAR inspection contradicted current source | Classify user-visible behavior from the pinned artifact and retain source only as a hypothesis | Rerun the exact installed-artifact workflow in the owning later phase | `P000-TASK-007` evidence-fidelity review preserves the mismatch and downstream owner |
| Repeated `P000-TASK-002` workflow did not reproduce a report | Recorded reruns produced no failure | Preserve the negative runs and require contradictory artifact evidence for `stale_with_evidence` | Otherwise retain `blocked_by_missing_evidence` | `P000-TASK-004` strict stale-report check |
| `P000-TASK-005` found an ambiguous or roadmap-only public claim | No attributable 3.0.3 release source existed | Exclude it from `CORE-REQ-007` | Only an owner-authorized plan revision may expand scope | `P000-TASK-007` scope audit against `DEC-002` and `FUT-001` |
| `P000-TASK-003` found ambiguous recipe intent or a legacy alias | Historical editor output could not distinguish canonical recipe-output and exact-recipe semantics | Record the ambiguity and downstream atomic-preservation obligation; never normalize by guess | `CORE-PHASE-003` owns validation, migration or rejection, and state-preservation proof | `P000-TASK-006` traceability maps the defect only to `CORE-REQ-011` |
| `SRC-009` appeared as a seventh report | Issue-count check returned more than six rows or typed SRC-009 as an issue | Reject the packet | Restore exactly six issue rows and one separate owner-request record | `P000-TASK-007` count-integrity proof |
| The `CORE-REQ-012` seam map omitted a required boundary | `P000-TASK-003` could not identify a present seam or explicit absence for a required component | Keep the packet incomplete | Repeat the seam review against frozen execution evidence | `P000-TASK-006` downstream-handoff review |
| Evidence exposed sensitive data | `P000-TASK-007` sanitation review found a credential, private raw log, or unrelated private value | Remove the capture from the packet | Recapture only sanitized evidence | `P000-TASK-007` sanitation rerun |
| Evidence identity changed after freeze | Hash, issue identity, environment identity, or revision no longer matched the frozen record | Preserve the original record and invalidate only dependent conclusions | Append a dated superseding record with explicit lineage | `P000-TASK-007` packet-identity recheck |
| `EXT-002` or `EXT-003` remained incomplete | External-prerequisite register showed unavailable workflow repair or unauthorized publication | Preserve the later-phase blocker without claiming release readiness | Route the blocker to its owning phase and do not bypass it | `P000-TASK-006` external-boundary traceability |

## Verification Matrix

| Requirement or task | Static or structural check | Artifact or workflow check | Negative check | Evidence |
|---|---|---|---|---|
| CORE-REQ-001 | Matrix schema, stable IDs, and issue-count integrity | Six report records plus one separately typed SRC-009 seam record | Unsupported stale results and inferred success rejected | Audit packet and acceptance traceability |
| P000-TASK-001 | Identity and hash-format validation | Shipped JAR hashes recalculated and repository ancestry reconciled | Ambiguous repository, artifact, or seven-issue representation rejected | Execution and artifact manifests |
| P000-TASK-002 | Required issue-record fields | Runnable historical workflows or pinned artifact inspection | Missing proof remained explicit | Exactly six issue records |
| P000-TASK-003 | JAR, claim, seam, and test inventory completeness | Exact shipped JAR and packed editor inspected | Rebuilt substitutes, unstable target identities, and inferred player origin rejected | Artifact inventory, claim register, seam map |
| P000-TASK-004 | Allowed classification vocabulary | Decisive evidence linked to every classification | Unsupported stale rows returned to missing-evidence status | Classification table |
| P000-TASK-005 | Claim attribution and DEC-002 scope | Artifact result recorded per bounded claim | Novel and roadmap-only claims excluded | Parity matrix |
| P000-TASK-006 | Owner and orphan check | Handoffs traced to later phase evidence | Duplicate ownership, missing ownership, and issue-count inflation rejected | Acceptance traceability |
| P000-TASK-007 | Packet identity, sanitation, and completeness | Representative issue, claim, and seam rows replayed | Missing proof preserved without false completion claims | Completion manifest, merge record, signed tag |

## Documentation, Operations, and Release

Phase 000 produced the internal audit packet, reconciliation record, artifact inventory, claim register, six issue classifications, SRC-009 source-role record, inventory insertion seam map, test inventory, fixture handoffs, acceptance traceability, external-boundary register, and completion manifest. It did not edit user-facing behavior documentation, release notes, issue state, or platform metadata. It performed no release packaging, attestation, broker preview, publication, or issue closure.

The historical evidence retains artifact hashes, environment identity, collection date, evidence surface, limitations, and supersession lineage. Later phases may consume it but cannot silently rewrite its identity or use it as proof of behavior at a higher fidelity than recorded.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| Goal-creation provenance is mistaken for the `CORE-PHASE-000` execution baseline | `P000-TASK-001` records provenance, starting default, branch result, merge, and tag as distinct identities | A later phase attempts to pin or recreate checkout `5b3077764907249b3711886cca538794f6139acf` | Restore the historical identity table and use the current master baseline rule | Repository-entry conclusions that used the wrong checkout | Re-run the affected phase's fresh remote, ancestry, and signed-predecessor checks; do not reopen `CORE-PHASE-000` |
| The fixed `CORE-REQ-001` issue count drifts | `P000-TASK-001` freezes six issue IDs and types `SRC-009` separately | Matrix count differs from six or includes `SRC-009` | Restore the six canonical rows and separate source record | Issue-count integrity and downstream traceability | Re-run `P000-TASK-006` and `P000-TASK-007` consistency checks |
| Shipped-artifact identity is replaced by a rebuilt or different binary | `P000-TASK-001` binds size, metadata, SHA-256, and SHA-512 | Any identity field mismatches | Reject the substitute and recover the exact artifact or record missing evidence | Every conclusion derived from JAR or bundle inspection | Recalculate both hashes, inspect metadata and bundle, then rerun dependent rows |
| Lower-fidelity source evidence is promoted to runtime proof | `P000-TASK-002` and `P000-TASK-004` preserve evidence class and limitations | A downstream claim cites source presence as completed behavior | Return the claim to its recorded class and route runtime proof to its owning phase | The affected issue classification or parity conclusion | Execute the owning phase's required real workflow and preserve lineage |
| Historical recipe semantics are normalized by guess | `P000-TASK-003` records canonical fields and ambiguity separately | A later record collapses `[recipes].locked_items`, `[recipes].locked_ids`, and generic output | Restore the exact historical observation and atomic-preservation obligation | Issue `#25` interpretation and `CORE-REQ-011` handoff | Re-run the `CORE-PHASE-003` visual, persisted, reload, runtime, and rollback evidence |
| Sensitive evidence enters the packet | `P000-TASK-007` requires sanitation and forbids secret-bearing files | Secret scan or manual review finds credentials or private raw logs | Remove the unsafe capture and obtain a sanitized replacement | The affected evidence item and any dependent conclusion | Repeat sanitation, identity, and completeness review before reuse |
| Later evidence contradicts a frozen observation | Immutable identities and explicit supersession lineage prevent silent rewriting | A newer named artifact or workflow produces a contradictory result | Append a dated superseding record and route it to the canonical owner | Only conclusions that depended on the contradicted observation | Re-run the owning requirement's evidence gate at the new identity |

## Phase Completion Packet

The completed packet consists of:

- `docs/verification/3.0.4-phase-000-audit.md`, containing the shipped artifact inventory, evidence index, original issue observations, parity matrix, seam inventory, traceability, and packet manifest.
- `docs/verification/3.0.4-phase-000-superseding-evidence.md`, preserved as the dated record that challenged earlier status and classification claims.
- `docs/verification/3.0.4-phase-000-plan-reconciliation.md`, containing the final classification reconciliation and evidence corrections accepted into the completed phase result.
- Branch result `f3a23155a835c5f3056a9c5a2c9d08ae4e9f1e5f`, merge commit `dc9e154871781de262ffd5eb401d65d0fa44cefb`, and verified signed annotated tag `3.0.4-phase-000`.

The reconciliation record controls where earlier packet wording conflicts with the final six-row classifications. The signed merge and tag control completed phase status. The packet contains exactly six issue identities. SRC-009 remains a separately typed non-issue record. No corrected recipe serialization or runtime result is represented as Phase 000 evidence.

## Next Transition

CORE-PHASE-001 is the only next phase. It begins from the then-current verified `origin/master` only after confirming that `master` contains merge commit `dc9e154871781de262ffd5eb401d65d0fa44cefb` and that signed annotated tag `3.0.4-phase-000` resolves to that merge. It consumes the issue `#24` fixture and CORE-REQ-002 handoff. No other phase may begin from Phase 000, and no later-phase branch may bypass CORE-PHASE-001.

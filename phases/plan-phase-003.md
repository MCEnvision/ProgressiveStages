# Phase 003 Execution Plan

> **Plan ID:** PLAN-PHASE-003
> **Phase ID:** CORE-PHASE-003
> **Owner:** EasyBuilder
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 003 of 006

## Purpose and Ownership

This phase proves that the production editor, in-game progression map, and shipped artifact deliver the user-visible behavior already promised for ProgressiveStages 3.0.3. It owns CORE-REQ-005, CORE-REQ-006, and CORE-REQ-007 only. Its measurable outcome is a packaged candidate whose Easy Builder can round trip every supported enchantment restriction, whose category overlay remains above stage icons without breaking map interaction, and whose shipped resources contain every other previously advertised 3.0.3 capability that the CORE-PHASE-000 audit proved missing.

The master plan owns the product boundary, requirement definitions, phase order, and release endpoint. This blueprint owns the dependency-ordered implementation and proof for CORE-PHASE-003. It may correct a capability only when a CORE-PHASE-000 audit row links the missing behavior to a public 3.0.3 promise and an inspected shipped artifact. It does not authorize a novel feature, a platform upgrade, a schema redesign, or work assigned to another phase.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| VERIFIED | Requirement boundary | CORE-REQ-005, CORE-REQ-006, and CORE-REQ-007 define the complete canonical scope of this phase | `plan.md` sections 6, 12, and 13 | Invalidated by an owner-approved master plan revision |
| UNKNOWN | Easy Builder enchantment controls | Existing source and tests do not yet prove that the production editor bundle and final JAR expose the published controls | GitHub issue `#11`, SRC-003, and the CORE-REQ-005 current-state row in `plan.md` | Replaced by a CORE-PHASE-000 audit row tied to an exact artifact identity |
| UNKNOWN | Category overlay depth | Existing ordering changes do not yet prove correct icon layering and input routing at supported GUI scales | GitHub issue `#16`, SRC-003, and the CORE-REQ-006 current-state row in `plan.md` | Replaced by a CORE-PHASE-000 reproduction or stale classification with exact revision and GUI settings |
| UNKNOWN | Artifact parity | Published 3.0.3 claims have not yet been fully reconciled with the shipped JAR | SRC-002, SRC-003, and CORE-REQ-007 in `plan.md` | Replaced by the completed CORE-PHASE-000 claim-to-artifact matrix |
| VERIFIED | Editor architecture | Easy Builder, TOML source view, schema compiler, runtime enchantment enforcement, and cleanup fallback share one canonical data model | `plan.md` section 11 | Invalidated by a merged architecture change before this phase starts |
| VERIFIED | Client UI ownership | `StageTreeScreen` owns client category rendering and map input semantics | `plan.md` section 11 | Invalidated by a merged client ownership change before this phase starts |
| VERIFIED | Supported target | This patch remains on Minecraft 1.21.1 and NeoForge 21.1.219 | `plan.md` sections 1 and 15 | Invalidated by an owner-approved platform decision, which is outside this phase |

## Scope Boundaries

### Included Scope

- CORE-REQ-005: verify the production Easy Builder workflow for creating, editing, removing, validating, saving, reopening, and compiling every supported enchantment restriction, and repair only the failed surfaces.
- CORE-REQ-005: prove that visual form state and TOML source round trip to the same normalized runtime rule, including field-specific rejection before apply.
- CORE-REQ-005: prove that the production editor resources used in testing are the resources packaged and served by the candidate JAR.
- CORE-REQ-006: reproduce or artifact-verify category menu layering, then repair render depth and input precedence when the overlay is open.
- CORE-REQ-006: preserve pan, zoom, search, category selection, navigation, inspector behavior, and focus after the overlay closes or a category is selected.
- CORE-REQ-007: correct only a user-visible 3.0.3 capability with a completed CORE-PHASE-000 matrix row that identifies the public promise, inspected artifact, observed absence, and required runtime workflow.
- CORE-REQ-007: reconcile corrected behavior with final artifact inventory and documentation that the candidate JAR proves.

### Explicit Exclusions

- FUT-001 is excluded because unimplemented legacy roadmap features beyond advertised 3.0.3 behavior are not artifact-parity defects.
- FUT-002 is excluded because dependency maintenance is not required for editor, client UI, or artifact parity proof unless later evidence makes it a release blocker.
- FUT-003 is excluded because requests opened after the locked five-issue baseline are novel scope, not a 3.0.3 artifact correction.
- FUT-004 is excluded because Minecraft, NeoForge, Java, Gradle, mappings, and other platform upgrades are not necessary to prove these requirements.
- NG-001 remains binding. This phase does not redesign progression, replace the schema, or create a new lock category.
- NG-002 remains binding. No optional integration becomes a hard dependency through editor preview or artifact correction.
- NG-003 remains binding. Client polish may not introduce full registry or stage scans in render hot paths.
- NG-004 remains binding. Source presence, an old commit, or release-note wording alone cannot close a reported defect.
- Entity-presence optimization belongs to CORE-PHASE-001, optional integration behavior belongs to CORE-PHASE-002, full compatibility proof belongs to CORE-PHASE-004, and release validation or publication belongs to CORE-PHASE-005 and CORE-PHASE-006.
- Novel controls, new enchantment semantics, new category behavior, new editor workflows, and unpromised convenience features are explicitly excluded even when they would be adjacent to a correction.

## Phase Contract

### CORE-PHASE-003 — Verify editor, client UI, and artifact parity

**Objective:** Produce a candidate artifact that passes production-bundle Easy Builder enchantment round trips, category-overlay render and input tests, and runtime proof for every CORE-PHASE-000 row classified as a missing advertised 3.0.3 capability
**Owner:** EasyBuilder
**Dependencies:** CORE-PHASE-000
**Canonical requirements:** CORE-REQ-005, CORE-REQ-006, CORE-REQ-007
**Documentation and release impact:** Update `README.md`, `DOCUMENTATION.md`, the documentation index, and affected references only for behavior verified in the packaged candidate; provide artifact-parity and client evidence to CORE-PHASE-004 and the 3.0.4 release packet
**Next transition:** CORE-PHASE-004

**Entry criteria**

- CORE-PHASE-000 is integrated and its completion packet identifies the exact source revision, shipped artifact identity, test settings, expected behavior, observed behavior, and assigned requirement for issues `#11` and `#16`.
- The CORE-PHASE-000 claim-to-artifact matrix lists every public 3.0.3 capability examined and marks each as present, missing, stale report, or insufficient evidence.
- Every proposed CORE-REQ-007 correction has a matrix row that cites a public 3.0.3 documentation or release source and records an observed absence in the inspected artifact.
- The editor form, TOML source view, schema compiler, packaged editor bundle, and `StageTreeScreen` boundaries are identified without changing the master architecture.
- A clean baseline build can produce the artifact used for before-and-after inspection, or the phase records the exact pre-existing failure and stops implementation until that failure is routed to its owning requirement.

**Implementation scope**

- CORE-REQ-005 owns production Easy Builder enchantment restriction controls, form-state validation, serializer and parser equivalence, compiler equivalence, save and reopen behavior, operator apply, client synchronization, and packaged bundle identity.
- CORE-REQ-006 owns category overlay depth, clipping, pointer precedence, click-through prevention, selection, close behavior, and preservation of map pan, zoom, search, navigation, and inspector state at supported GUI scales.
- CORE-REQ-007 owns only corrections selected by the frozen parity matrix. Every correction must reuse the established public contract, pass its documented user workflow, appear in the candidate JAR, and remain traceable to its source claim.
- CORE-REQ-005, CORE-REQ-006, and CORE-REQ-007 permit shared changes only where the canonical editor data model or client rendering boundary requires them. A shared change must retain server authority, client-only class isolation, packet validation, and existing serialized identifiers.

**Execution order**

1. `P003-TASK-001` converts the CORE-PHASE-000 rows for CORE-REQ-005, CORE-REQ-006, and CORE-REQ-007 into frozen reproduction fixtures and an allowed-correction ledger.
2. `P003-TASK-002` executes CORE-REQ-005 by repairing, when required, the Easy Builder enchantment visual form against the canonical data model.
3. `P003-TASK-003` completes CORE-REQ-005 production-bundle, TOML round-trip, validation, operator-apply, and candidate-JAR verification.
4. `P003-TASK-004` executes CORE-REQ-006 by repairing, when required, category overlay render order and input routing while preserving the rest of the map workflow.
5. `P003-TASK-005` executes CORE-REQ-007 for each and only each allowed-correction ledger row, with a separate regression workflow and artifact check per row.
6. `P003-TASK-006` assembles the CORE-REQ-005, CORE-REQ-006, and CORE-REQ-007 phase completion packet, updates verified documentation, inspects the candidate artifact, and runs the combined editor and client regression pass.

**Required evidence**

- A frozen ledger that maps every phase action to CORE-REQ-005, CORE-REQ-006, or CORE-REQ-007 and, for parity corrections, to a specific CORE-PHASE-000 claim-to-artifact row.
- Browser-driven evidence from the production editor bundle for enchantment control creation, edit, removal, validation, save, reopen, source round trip, and compiler equivalence.
- An operator apply and client synchronization smoke test against the candidate JAR, including a malformed draft that is rejected with a field-specific error before server mutation.
- Render-order and input-routing regression evidence for the open category overlay, including populated nodes beneath its bounds.
- Client screenshots and interaction records at default and at least one supported nondefault GUI scale, with pan, zoom, search, category selection, navigation, and inspector checks.
- Per-row runtime and artifact evidence for every CORE-REQ-007 correction, or a signed-off empty correction ledger when the audit proves no other capability missing.
- A final JAR inventory that identifies the editor bundle and all corrected resources by artifact hash and source revision.
- Applicable formatter, static analysis, unit, browser or editor, client, build, and artifact-inspection results, with exact commands and outputs recorded outside the protected plan set.

**Exit criteria**

- Every CORE-REQ-005 acceptance criterion passes against the production editor bundle packaged in the candidate JAR.
- Every CORE-REQ-006 acceptance criterion passes at the tested supported GUI scales with no click-through or regression to map navigation and inspector behavior.
- Every allowed CORE-REQ-007 ledger row has implementation, documented-workflow, and final-artifact proof, and no unlisted or novel feature entered the phase diff.
- Documentation describes only behavior demonstrated by the candidate artifact, and the final diff contains no unrelated platform, schema, or feature change.
- The phase completion packet identifies the exact commit, candidate JAR hash, test environments, evidence locations, and unresolved blockers; any failed mandatory check blocks transition.
- The final phase, CORE-PHASE-006, verifies the owner-selected completion endpoint and plan-wide Definition of Done.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Five-issue audit matrix | CORE-PHASE-000 | Issues `#11` and `#16` have exact artifact, revision, settings, expected, observed, and classification fields | Confirm required columns are populated and evidence is reproducible | Stop the affected task and return the incomplete row to CORE-PHASE-000; do not infer a defect |
| 3.0.3 claim-to-artifact matrix | CORE-PHASE-000 | Every advertised capability inspected is present, missing, stale, or insufficient evidence | Verify each missing row cites a public claim and an inspected artifact identity | Exclude rows without both sources and record insufficient evidence rather than implementing a feature |
| Existing editor data contract | Master architecture | Easy Builder, TOML source, schema compiler, runtime enchantment enforcement, and cleanup fallback resolve through one canonical model | Compare normalized output for equivalent visual and TOML inputs | Reject divergent output, preserve the last valid source, and repair within CORE-REQ-005 |
| Server authority contract | Master architecture | Editor apply remains operator-authorized, schema-validated, and server-authoritative | Exercise allowed and denied apply paths before observing state | Block phase exit on unauthorized mutation or permissive validation |
| Stage map interaction contract | Master architecture | Category overlay coexists with pan, zoom, search, navigation, and inspector behavior | Execute the populated-map interaction fixture before and after any repair | Revert or correct the client change if an existing interaction regresses |
| Supported client target | Master plan | Candidate runs on Minecraft 1.21.1 and NeoForge 21.1.219 without a platform upgrade | Confirm build metadata and runtime identity in captured evidence | Stop and route any required platform change as out of scope |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Verified production editor bundle | CORE-PHASE-004 | Supported enchantment restrictions create, edit, remove, validate, save, reopen, and compile through one canonical model | Existing TOML fields and normalized runtime semantics remain compatible | Browser workflow record, round-trip comparison, apply log, bundle identity, and JAR hash |
| Verified category overlay | CORE-PHASE-004 | Overlay pixels and pointer handling take precedence over every stage node while open, then release control cleanly | Existing pan, zoom, search, navigation, inspector, and category behavior remain supported | Render/input regression results and GUI-scale screenshots |
| Artifact-parity correction ledger | CORE-PHASE-004 | Every correction is tied to an audited 3.0.3 promise and every excluded row records why it was not implemented | No new public feature or platform boundary is introduced | Matrix diff, per-row test result, documentation citation, and JAR inventory |
| Candidate artifact identity | CORE-PHASE-004 | Editor, client UI, and parity evidence all identify the same source revision and candidate JAR | Candidate is evidence only and is not authorized for publication | Source revision, SHA-256, SHA-512 when produced by the build flow, and archive listing |
| Verified documentation changes | CORE-PHASE-004 | User and operator text matches behavior demonstrated by the candidate | Documentation does not advertise unverified or future behavior | Documentation diff and links to the phase evidence packet |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P003-TASK-001 | CORE-REQ-005, CORE-REQ-006, CORE-REQ-007 | Freeze reproduction fixtures, select allowed parity corrections, and reject rows that lack both a public promise and artifact proof | CORE-PHASE-000 completion packet and claim-to-artifact matrix | Immutable phase fixture set and allowed-correction ledger | Issue evidence, editor workflow contract, stage map workflow contract, artifact parity matrix | Independent row review confirms every action has a canonical requirement and sufficient source evidence |
| P003-TASK-002 | CORE-REQ-005 | Repair only missing or incorrect Easy Builder enchantment controls and bind their create, edit, remove, and validation state to the canonical model | P003-TASK-001 fixture, existing editor data contract | Working visual controls and field-specific validation state | Easy Builder visual form, enchantment schema, serializer, validation interface | Form-level tests cover every supported restriction and invalid boundary without server mutation |
| P003-TASK-003 | CORE-REQ-005 | Prove visual-to-TOML-to-compiler equivalence, save and reopen, operator apply, synchronization, production bundle packaging, and candidate JAR serving | P003-TASK-002 output, operator authority contract, packaging flow | Verified production editor bundle and normalized-rule comparison | TOML source view, schema compiler, editor apply boundary, client synchronization, packaged web resources | Browser workflow, normalized output equality, denied malformed apply, authorized apply, reconnect or reopen, JAR inventory |
| P003-TASK-004 | CORE-REQ-006 | Repair overlay render depth and pointer precedence, then exercise map workflows at supported GUI scales | P003-TASK-001 category fixture and stage map interaction contract | Verified category overlay behavior | `StageTreeScreen`, category overlay rendering, input routing, map state | Automated render/input checks plus default and nondefault GUI-scale client smoke records |
| P003-TASK-005 | CORE-REQ-007 | Implement each allowed advertised-capability correction as a separate traceable change and prove its documented runtime workflow | P003-TASK-001 allowed-correction ledger | Corrected advertised capabilities or an evidence-backed empty correction set | Only components named by the corresponding audit row | Per-row focused regression, documented workflow, final artifact inventory, and negative scope review |
| P003-TASK-006 | CORE-REQ-005, CORE-REQ-006, CORE-REQ-007 | Run combined verification, update only evidence-backed documentation, inspect the candidate artifact, and assemble downstream evidence | P003-TASK-003 through P003-TASK-005 | Phase completion packet and CORE-PHASE-004 handoff | Build output, JAR resources, `README.md`, `DOCUMENTATION.md`, documentation index, affected references | Clean build, final archive listing, documentation comparison, clean diff review, combined browser and client smoke |

P003-TASK-001 must complete before any implementation because it is the only authority for artifact-parity scope. P003-TASK-002 must precede P003-TASK-003 so bundle proof tests the corrected canonical form. After P003-TASK-001, P003-TASK-004 may proceed independently of P003-TASK-002 and P003-TASK-003 because it owns a separate client boundary. P003-TASK-005 corrections may proceed independently of each other only when their audit rows identify disjoint components; shared schema, editor, packet, or client surfaces require serialized integration and rerunning every affected fixture. P003-TASK-006 runs last against the integrated candidate.

If a task uncovers behavior outside its canonical requirement, preserve the evidence and stop that work package. Do not absorb the behavior into this phase. A failed implementation must leave the last validated editor source, server stage state, and client map behavior intact. Revert only the phase-owned change through normal version-control review, then rerun the baseline fixture before attempting another correction. Parallel work may not edit the same normalized schema, editor serialization boundary, or `StageTreeScreen` interaction path without an explicit integration order.

## Architecture and Implementation Boundaries

The visual editor is an authoring surface, not an authority boundary. Easy Builder controls translate user choices into the existing canonical stage representation. TOML source and the visual form must normalize to the same compiler input, and the server must validate operator permission and the complete draft before mutation. A visual control cannot introduce a second enchantment model, silently drop unknown supported fields, or apply partially valid state. Field errors must identify the rejected field before apply while retaining the last valid draft for correction.

The production-bundle boundary is part of correctness. Source-level browser tests are necessary but insufficient. The verified resources must be the resources embedded in and served from the candidate JAR. Bundle identity must be recorded with the candidate source revision and artifact hash so later phases can detect stale or mismatched frontend output.

`StageTreeScreen` remains client-only presentation. The category overlay must be composed and clipped in a layer that visually covers nodes within its bounds. While open, it must receive pointer events before map nodes, prevent click-through, and define predictable close and selection behavior. When it closes, map pan, zoom, search, navigation, category state, focus, and inspector interaction must remain usable. The chosen rendering technique must follow the existing client architecture and must not cause client classes to load on a dedicated server.

Artifact-parity corrections are evidence gated. CORE-PHASE-000 supplies the claim, inspected artifact, and observed gap. This phase may restore the established contract but may not broaden it. Public identifiers, TOML keys, stage schema, commands, packets, persistence, and API signatures remain stable unless the public 3.0.3 contract already requires the exact missing form and a compatible correction is proven. Any unavoidable compatibility change is a plan conflict and blocks work rather than authorizing local redesign.

Client render and input paths must avoid per-frame full registry, stage, or entity scans. Editor serialization must be deterministic for equivalent normalized inputs. Tests must avoid private credentials and sanitize operator URLs, local paths, confirmation codes, and server data from stored evidence. No phase output authorizes release or external publication.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Issue `#11` cannot be reproduced in the audited production bundle | All supported enchantment workflows pass against the exact reported or closest evidence-backed configuration | Record an evidence-based stale classification and make no speculative editor change | Retain the unchanged bundle and route closure evidence through later integration | Repeat the full create, edit, remove, validate, save, reopen, compile, and apply workflow against the candidate JAR |
| Enchantment controls exist in source but not in the packaged bundle | Source test passes while candidate-JAR browser inspection lacks the control or serves stale resources | Treat packaging parity as the defect; do not claim source presence as completion | Correct the established bundle production or resource inclusion path and rebuild from clean inputs | Compare bundle identity and control behavior before and after clean candidate packaging |
| Visual form and TOML source normalize differently | Equivalent inputs produce a semantic diff or compiler output mismatch | Reject apply and preserve both representations for diagnosis | Correct the shared serializer, parser, or mapping without inventing a second schema | Golden round-trip cases cover every supported enchantment restriction and field boundary |
| Malformed enchantment input reaches server mutation | Invalid field produces a stage change, partial save, or generic success | Deny the entire apply before mutation and return a field-specific validation result | Restore the last valid draft and authoritative server stage state | Negative operator apply test proves no file, compiled rule, or synchronized state changed |
| Category icons draw above the open menu | Screenshot, pixel assertion, or render capture shows a node within overlay bounds | Render the complete overlay above affected nodes | Correct the phase-owned render ordering and rerun every GUI-scale fixture | Populated-map screenshots and automated bounds assertions show no icon pixels above the overlay |
| Category menu click activates a covered stage node | Input trace shows both overlay and node handling one pointer event | Overlay consumes applicable input while open | Restore known-good input routing and correct event precedence | Covered-node click fixture confirms category action only, followed by normal node action after close |
| Resize or GUI-scale change corrupts map state | Overlay, focus, viewport, inspector, or selection becomes unusable after scale transition | Recompute presentation safely while preserving valid logical selection and navigation state | Close the transient overlay if required by existing semantics, then restore a valid viewport | Default and nondefault scale workflow covers open, select, close, pan, zoom, search, and inspect |
| A claimed 3.0.3 capability lacks an authoritative public source | CORE-PHASE-000 row contains report wording but no published contract | Mark it insufficient evidence and exclude implementation | Preserve the row for future owner triage outside this plan | Negative scope review shows no code or documentation change for the row |
| A new feature request resembles a parity gap | Request date or contract comparison falls outside the frozen audit baseline | Classify it as FUT-003 and do not implement it | Route it to later planning without changing this phase | Allowed-correction ledger contains only frozen audit row IDs |
| A parity correction requires a platform or schema upgrade | Implementation cannot retain pinned target or serialized compatibility | Stop the correction and report a plan conflict | Restore the pre-change candidate and keep CORE-REQ-007 open | Build metadata and compatibility diff prove no upgrade entered the phase |
| Combined verification fails after independently passing tasks | Integrated candidate fails editor, client, build, or artifact check | Block phase completion and identify the smallest interacting change set | Revert or correct only phase-owned changes, rebuild cleanly, and rerun from the first invalidated fixture | Full P003-TASK-006 rerun produces one coherent evidence packet |

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Evidence artifact |
|---|---|---|---|---|---|
| CORE-REQ-005 | Serializer, parser, schema, validation, and normalized-output tests for every supported enchantment restriction | Visual form, TOML source, compiler, operator apply, and client synchronization use the same fixture | Production editor served from the candidate JAR creates, edits, removes, saves, reopens, compiles, and applies a valid restriction | Malformed and boundary values fail before mutation; last valid draft reopens unchanged | Browser report, normalized-rule comparison, apply log, bundle identity, JAR hash |
| CORE-REQ-006 | Render-order, clipping, hit-test, and input-consumption checks | Populated stage nodes, overlay, search, inspector, navigation, pan, and zoom share one fixture | Client smoke at default and nondefault supported GUI scales opens, selects, closes, pans, zooms, searches, navigates, and inspects | Covered node never receives overlay click; closing returns normal node input; scale transition recovers valid state | Screenshots, interaction trace, test output, client runtime identity |
| CORE-REQ-007 | Focused regression per allowed correction and a ledger-to-diff consistency check | Corrected component participates in its documented surrounding workflow | Each restored capability is exercised from the final candidate artifact exactly as publicly documented | Missing contract source, novel scope, and platform-change cases remain excluded | Per-row test record, public source citation, artifact listing, documentation diff |
| P003-TASK-001 | Matrix schema and stable-ID consistency check | Issue and claim rows reconcile to exact artifact identities | Reproduction fixtures run on the audited baseline when feasible | Insufficient evidence produces exclusion, not implementation | Frozen fixture manifest and allowed-correction ledger |
| P003-TASK-003 | Bundle manifest and canonical output comparison | Editor bundle, server apply, compiler, and synchronized client state | Operator completes valid and invalid workflows through the served editor | Unauthorized or malformed apply leaves server state unchanged | Candidate bundle inventory and operator smoke record |
| P003-TASK-004 | Client-only class boundary and input/render assertions | Existing stage map controls remain interoperable | Manual client acceptance pass on populated graph and supported scales | Click-through and stale focus fixtures prove recovery | Client screenshots and deterministic interaction checklist |
| P003-TASK-006 | Clean diff, documentation link, and archive inventory checks | Combined editor, client, and parity regression suite | Candidate JAR starts and serves the verified editor and progression map workflows | Any mismatch invalidates the completion packet and forces clean rebuild and rerun | Exact commands, results, source revision, artifact hashes, archive listing |

Fixtures use the exact stage, enchantment restrictions, category population, GUI settings, and artifact identities recorded by CORE-PHASE-000. The editor fixture includes one valid example and invalid boundary examples for every supported enchantment restriction identified by the canonical schema. The client fixture places stage nodes beneath the maximum overlay bounds and records viewport, selection, category, search, and inspector state before and after interaction. The parity fixture is data driven from the allowed-correction ledger and is empty when no additional gap is proven.

Run order is static and unit checks, editor or browser integration, client render and input checks, applicable formatter and analysis, clean `./gradlew build`, production-bundle smoke, client runtime smoke, and final JAR inspection. If a failure changes source or generated bundle output, restart at the earliest affected check and always repeat clean build, production-bundle smoke, client smoke, and artifact inspection. Lower-fidelity source or unit evidence cannot replace browser execution against packaged resources or the real client map workflow.

## Documentation, Operations, and Release

- Update `README.md` only when a verified user-facing 3.0.4 correction changes installation, usage, configuration, or feature guidance.
- Update `DOCUMENTATION.md`, the documentation index, and affected references with the supported Easy Builder enchantment workflow, visual-to-TOML equivalence, validation behavior, category overlay behavior, and troubleshooting steps demonstrated by the candidate artifact.
- For each CORE-REQ-007 correction, cite the original public 3.0.3 claim and describe only the behavior proven in the final candidate. Remove or correct an inaccurate claim when implementation is not compatible and the master contract permits documentation correction; do not relabel a new feature as a fix.
- Store browser steps, GUI-scale settings, stage fixture identity, expected pixels or input outcome, operator permissions, server mode, source revision, bundle identity, candidate JAR identity, and rerun instructions in the ordinary test or verification evidence location established by the repository.
- Add 3.0.4 release-note inputs for issue `#11`, issue `#16`, and each proven parity correction. Release wording remains provisional until CORE-PHASE-005 validates the integrated artifact.
- Record no configuration migration unless an audited correction truly changes an existing documented field. Any required migration outside the compatible 3.0.4 contract blocks the phase.
- This phase performs no publication, issue closure, release tag, broker request, or remote deployment. It hands a local and integrated evidence packet to CORE-PHASE-004.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| Tests exercise development assets instead of packaged resources | Bind browser URL or resource identity to the candidate JAR and record bundle digest | Development and JAR bundle manifests or behavior differ | Clean the established generated-output path, rebuild, and retest the packaged bundle | All CORE-REQ-005 production-bundle and CORE-REQ-007 artifact evidence | Repeat P003-TASK-003, affected parity workflows, and P003-TASK-006 |
| Editor repair silently changes normalized TOML semantics | Compare canonical normalized output before server apply | Golden round trip or compiler equivalence changes unexpectedly | Correct mapping and restore compatibility fixture | Editor unit, round-trip, apply, sync, and documentation evidence | Repeat all CORE-REQ-005 verification from static tests through JAR smoke |
| GUI-scale proof misses overlapping content | Populate nodes under every overlay bound and record scale values | Later screenshot shows item pixels or input leakage | Expand deterministic fixture and correct layering | CORE-REQ-006 screenshots and interaction trace | Repeat render, input, default-scale, nondefault-scale, and combined client checks |
| Overlay fix regresses pan, zoom, search, navigation, or inspector | Use one end-to-end map workflow before and after the overlay action | Interaction trace or preserved-state assertion fails | Restore prior behavior and isolate overlay event ownership | All CORE-REQ-006 integration and runtime evidence | Repeat the complete map workflow at every tested scale |
| Parity ledger admits an unpromised feature | Require both public claim and inspected-artifact absence before selection | Ledger-to-source review cannot resolve both evidence links | Remove the change and associated documentation from phase scope | Affected CORE-REQ-007 tests, artifact listing, and documentation | Rebuild candidate and repeat full diff and artifact audit |
| A later shared change alters editor schema or client rendering | Downstream phase records contract-touching revisions | Source revision or bundle/JAR hash no longer matches packet | Mark this packet stale and route targeted revalidation | The affected requirement packet and combined artifact evidence | Rerun every fixture touching the changed contract against the new integrated revision |
| Generated or local output contaminates the diff | Use clean build inputs and final tracked-diff plus archive inspection | Unexpected generated files, caches, absolute paths, credentials, or unrelated changes appear | Remove only phase-created contamination and rebuild | P003-TASK-006 clean-diff and artifact evidence | Repeat clean build, diff review, archive listing, and smoke tests |
| Documentation advances beyond verified behavior | Bind every changed claim to a passed candidate workflow | Claim-to-evidence review finds no matching test | Correct documentation before handoff | Documentation and release-note inputs | Repeat documentation comparison and affected user workflow |

## Phase Completion Packet

CORE-PHASE-003 may close only when its external evidence packet contains all of the following:

- The integrated phase commit identity and a focused diff that contains only CORE-REQ-005, CORE-REQ-006, CORE-REQ-007, required tests, and evidence-backed documentation.
- The frozen CORE-PHASE-000 input rows, reproduction fixtures, and allowed-correction ledger, including explicit exclusion reasons for insufficient or novel requests.
- Easy Builder form, serializer, parser, validator, compiler, save, reopen, operator apply, synchronization, production-bundle, and candidate-JAR results for every supported enchantment restriction.
- Category overlay render and input results, populated-map screenshots, GUI-scale values, and the full pan, zoom, search, category, navigation, and inspector checklist.
- One correction record per CORE-REQ-007 row containing the public 3.0.3 source, inspected baseline artifact, change identity, documented runtime workflow, regression result, and candidate-JAR inventory result, or an evidence-backed statement that the correction ledger is empty.
- Applicable formatter, static analysis, unit, browser or editor, client, `./gradlew build`, and artifact-inspection command records with pass or exact failure status.
- Candidate source revision, JAR filename, SHA-256, SHA-512 when produced by the established build flow, editor bundle identity, archive listing, runtime version, and test environment.
- `README.md`, `DOCUMENTATION.md`, documentation-index, affected-reference, and provisional 3.0.4 release-note diffs tied to verified behavior.
- Negative evidence that malformed or unauthorized editor apply does not mutate server state, overlay clicks do not reach covered nodes, and unpromised features did not enter the diff.
- An explicit declaration that no known mandatory phase-owned defect remains, or an open blocker that prevents phase closure and CORE-PHASE-004 entry.

The completion packet lives in ordinary repository, test, issue, pull request, and verification artifacts created during execution. This protected phase blueprint is not updated as a status diary.

## Next Transition

After CORE-PHASE-003 implementation, required checks, completion evidence, pull request integration, and resulting master verification pass, CORE-PHASE-004 begins by pinning the integrated source revision and candidate artifact identity from this phase. CORE-PHASE-004 must first confirm that the production editor bundle, category overlay, parity ledger, documentation, and candidate hashes in the handoff still match the merged revision before running plan-wide compatibility and security verification. Do not start CORE-PHASE-004 while any CORE-REQ-005, CORE-REQ-006, or CORE-REQ-007 acceptance criterion is failed, unknown, or supported only by lower-fidelity evidence.

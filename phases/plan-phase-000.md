# Phase 000 Execution Plan

> **Plan ID:** PLAN-PHASE-000
> **Phase ID:** CORE-PHASE-000
> **Owner:** RepositoryAudit
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 000 of 006

## Purpose and Ownership

This phase freezes the defect and artifact baseline for the 3.0.4 polish release. It owns only CORE-REQ-001: evidence capture, reproduction or artifact verification of issues `#8`, `#10`, `#11`, `#16`, and `#24`, evidence-based classification of any stale report, reconciliation of advertised 3.0.3 behavior with the shipped artifact, and traceable acceptance mapping for later phases. The master plan owns product scope, phase order, owner decisions, external prerequisites, and the final release endpoint. This blueprint owns the detailed audit procedure and the evidence packet required before any corrective implementation starts.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| VERIFIED | Repository baseline | The audit baseline is `origin/master` at `ab4ad138e1f74eec82cf0392a47ac1e54dd66d01` | Master plan §1 and SRC-002 | Invalid if the baseline commit or authoritative remote changes before capture |
| VERIFIED | Issue baseline | The mandatory report set is exactly issues `#8`, `#10`, `#11`, `#16`, and `#24` | Master plan §4 and SRC-004 | Invalid if the owner formally revises the locked five-issue baseline |
| OBSERVED | Entity-presence performance | Issue `#24` reports condition-context construction in the entity tracking decision path as the server-thread hotspot | SRC-006 and master plan §4 | Invalid if the supplied profile is shown to identify another build, configuration, or code revision |
| OBSERVED | Curios integration | Issue `#8` reports that Curios 9.5.1 slot gating does not resolve the supported API surface | SRC-004 and master plan §4 | Invalid if the report used a different Curios, Minecraft, NeoForge, or ProgressiveStages artifact |
| VERIFIED | Recipe-viewer configuration | Existing evidence does not prove independent JEI and EMI controls or their combined installation behavior | SRC-003 and master plan §4 | Invalid when a shipped-artifact matrix proves all DEC-003 combinations |
| UNKNOWN | Easy Builder enchantments | Source presence does not yet prove that the production editor bundle and shipped JAR expose the advertised controls | Issue `#11`, SRC-003, and master plan §4 | Resolved only by production-bundle and shipped-artifact workflow evidence |
| UNKNOWN | Category overlay | Source ordering changes do not yet prove correct depth and input behavior at supported GUI scales | Issue `#16`, SRC-003, and master plan §4 | Resolved only by shipped-artifact client runtime evidence |
| UNKNOWN | Advertised 3.0.3 parity | Public 3.0.3 claims have not been completely reconciled with the shipped JAR | SRC-001, SRC-002, SRC-003, and CORE-REQ-007 | Invalid when a claim-by-claim artifact matrix is complete for the pinned release |

## Scope Boundaries

### Included Scope

- CORE-REQ-001 — Freeze the pinned repository revision, installed artifact identity, five issue reports, relevant configurations, expected behavior, observed behavior, and evidence classification.
- CORE-REQ-001 — Reproduce each report when its required environment is available, otherwise artifact-verify it or classify it stale using explicit contradictory evidence.
- CORE-REQ-001 — Inspect the shipped 3.0.3 artifact and public 3.0.3 documentation claim by claim, then record only proven missing advertised capabilities for CORE-REQ-007.
- CORE-REQ-001 — Map every confirmed or artifact-verified outcome to exactly one owning corrective requirement from CORE-REQ-002 through CORE-REQ-007 and its measurable acceptance evidence.
- CORE-REQ-001 — Preserve the evidence contracts for EXT-001, EXT-002, and EXT-003 without claiming those later prerequisites are complete.

### Explicit Exclusions

- CORE-REQ-002 through CORE-REQ-010 are downstream implementation, compatibility, release-validation, publication, and issue-closure work. This phase may assign evidence to them but may not execute them.
- FUT-001, FUT-002, FUT-003, and FUT-004 remain excluded and cannot be promoted through the artifact audit.
- NG-001 excludes a progression redesign, schema overhaul, or new lock category.
- NG-002 excludes converting Curios, JEI, EMI, or another integration into a hard dependency.
- NG-003 excludes adding runtime scans or performance instrumentation to production hot paths during the audit.
- NG-004 prohibits treating source presence, release notes, or a lower-fidelity check as sufficient issue-closure evidence.
- Corrective source, resource, configuration, documentation, workflow, release, or GitHub issue changes are outside this phase.

## Phase Contract

### CORE-PHASE-000 — Freeze the defect and artifact baseline

**Objective:** Produce a reproducible, revision-pinned audit matrix that classifies all five baseline reports, reconciles every advertised 3.0.3 capability with the shipped artifact, and assigns every proven gap to a downstream requirement with measurable acceptance evidence
**Owner:** RepositoryAudit
**Dependencies:** none
**Canonical requirements:** CORE-REQ-001
**Documentation and release impact:** Produce the versioned audit matrix, artifact inventory, claim comparison, reproduction records, sanitized evidence index, and downstream acceptance mapping; do not update release-facing documentation or publish artifacts in this phase
**Next transition:** CORE-PHASE-001

**Entry criteria**

- The authoritative baseline remote, branch, and commit match the identities in the master plan.
- The five report records and their supplied attachments, configurations, or screenshots are available in sanitized form, or their absence is explicitly recorded.
- The shipped 3.0.3 JAR, its source revision or release identity, and the public documentation and release claims used for comparison are uniquely identified.
- Test identities record ProgressiveStages, Minecraft 1.21.1, NeoForge 21.1.219, and each optional integration version when applicable.
- DEC-002 remains the scope boundary: a proven missing advertised or documented 3.0.3 capability is mandatory, while an unpromised feature remains excluded.

**Implementation scope**

- CORE-REQ-001 only: freeze evidence, execute reproductions and artifact inspections, classify reports, record advertised-capability parity, and trace outcomes to downstream acceptance gates.
- CORE-REQ-001 preserves raw evidence separately from conclusions so another maintainer can repeat each classification from the same artifact identities and test inputs.
- CORE-REQ-001 records unknowns and unavailable evidence as blockers or downstream prerequisites; it does not convert uncertainty into a confirmed defect, stale report, or completed prerequisite.

**Execution order**

1. `P000-TASK-001` executes CORE-REQ-001 baseline identity and evidence-source freezing.
2. `P000-TASK-002` executes CORE-REQ-001 issue-by-issue reproduction planning and normalized observation capture.
3. `P000-TASK-003` executes CORE-REQ-001 shipped 3.0.3 JAR and public-claim inventory.
4. `P000-TASK-004` executes CORE-REQ-001 evidence-based classification and stale-report review.
5. `P000-TASK-005` executes CORE-REQ-001 advertised-capability gap review under DEC-002.
6. `P000-TASK-006` executes CORE-REQ-001 downstream requirement and acceptance-evidence mapping.
7. `P000-TASK-007` executes CORE-REQ-001 audit consistency review and completion-packet freeze.

**Required evidence**

- A versioned audit matrix with one row per baseline report and one row per advertised 3.0.3 capability claim examined.
- A final shipped-JAR inventory tied to artifact hash, release identity, and source revision when provenance evidence establishes that relationship.
- Reproduction records that identify environment, configuration, fixture, steps, expected behavior, observed behavior, logs or screenshots, rerun count, and result.
- A classification rationale for every report using only `reproduced`, `artifact_verified`, `stale_with_evidence`, or `blocked_by_missing_evidence`.
- A traceability table mapping each proven defect or missing advertised capability to CORE-REQ-002 through CORE-REQ-007, the responsible later phase, acceptance criteria, and required proof.
- An external-boundary register that leaves EXT-002 unavailable and EXT-003 unauthorized until their exact master-plan evidence contracts are satisfied, and that records EXT-001 artifact evidence without using it as proof of runtime success.

**Exit criteria**

- All five baseline issues have an explicit, supported classification and no report is marked stale solely because source appears to contain a possible fix.
- Every audited public 3.0.3 claim has an artifact-backed result, or an explicit missing-evidence blocker with an owner and downstream resolution gate.
- Every confirmed defect or advertised-capability gap maps to one downstream canonical requirement and measurable verification evidence.
- The audit contains no novel feature request, platform upgrade, speculative defect, credential, private raw log, or unsupported completion claim.
- An independent consistency review can reproduce the matrix classifications from the pinned artifacts and evidence index.
- The phase completion packet is immutable by identity: later discoveries append superseding evidence instead of silently rewriting the frozen baseline.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Locked owner request and decisions | EnVy | DEC-001 through DEC-005 retain the selected choices in the master plan | Compare planning subject, endpoint, scope rule, recipe-viewer decision, performance gate, and release gate with the master plan | Stop classification that would expand scope and route any contract conflict to the owner |
| Repository identity | origin/master | Remote, ref, and commit equal the master-plan baseline | Record remote URL, ref, commit, clean inspection state, and collection time | Stop the audit if identity cannot be proven; do not substitute another branch or working tree |
| Five issue reports | SRC-004 | Exactly `#8`, `#10`, `#11`, `#16`, and `#24`, including available attachments and reporter configurations | Record issue identity, update timestamp, affected version claim, and sanitized evidence inventory | Mark only the unavailable evidence field blocked; never infer report details |
| Performance report | SRC-006 | Profile identity and configuration are attributable to issue `#24` | Record artifact version, fixture, configuration, profile identity, and relevant call-path observation | Classify the performance report as blocked if artifact or fixture identity is insufficient |
| Shipped 3.0.3 artifact | Release provenance | Artifact is uniquely identified by version, platform, source revision when provable, SHA-256, and SHA-512 | Recalculate hashes, inspect JAR inventory, and compare embedded metadata | Reject an ambiguous, rebuilt, or hash-mismatched artifact from parity conclusions |
| Public 3.0.3 claims | SRC-002 and SRC-003 | Claims are bounded to published documentation and release material attributable to 3.0.3 | Capture claim text location, publication identity, and advertised user-visible outcome | Exclude ambiguous roadmap language from mandatory parity scope |
| Optional integration artifacts | EXT-001 | Evidence records authoritative source, compatibility, exact version, license provenance, security review, SHA-256, and SHA-512 | Verify the seven typed artifact-evidence fields before using an artifact in a reproduction | Record the missing field and defer affected runtime proof; do not relax the evidence contract |
| Release workflow blocker | EXT-002 | Availability remains `unavailable` until the corrected shared workflow revision and disposable-candidate proof exist | Preserve the exact prerequisite identity and required evidence from the master plan | Keep CORE-REQ-009 and CORE-REQ-010 externally blocked; do not bypass or relabel the prerequisite |
| Publication authority | EXT-003 | Authorization remains `not_authorized` until bound to artifact identities, operations, operators, rollback, runbook digest, systems, and time window | Preserve all seven authorization scope-binding fields | Keep publication prohibited; never treat earlier codes or unscoped approval as valid |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Frozen issue matrix | CORE-PHASE-001 through CORE-PHASE-003 | Every baseline report has a pinned identity, observation record, classification, and owning requirement | Append-only supersession preserves the original baseline and explains any later evidence change | Matrix version, evidence index, reviewer record, and artifact hashes |
| Entity-presence fixture contract | CORE-PHASE-001 | Issue `#24` expected behavior, active rule configuration, multiplayer roles, baseline metric, and reported hotspot are reproducible without prescribing the repair | Fixture remains tied to the audited artifact and DEC-004 thresholds | Reproduction record and profile provenance |
| Optional-integration matrix seed | CORE-PHASE-002 | Issues `#8` and `#10` identify exact integration artifacts, configuration states, expected behavior, and observed behavior | EXT-001 evidence fields remain mandatory and integrations remain optional | Artifact evidence register and reproduction records |
| Client and editor fixture contract | CORE-PHASE-003 | Issues `#11` and `#16` identify artifact, UI state, test content, GUI scale or editor workflow, expected behavior, and observation | Evidence distinguishes source, development bundle, shipped bundle, and installed JAR | Screenshots or recordings, browser or client logs, and artifact inventory |
| Advertised-capability parity matrix | CORE-PHASE-003 | Every proven missing 3.0.3 claim is bounded to a public source and assigned to CORE-REQ-007 | Novel or roadmap-only features remain excluded under DEC-002 | Claim source, artifact observation, classification, and traceability row |
| Acceptance traceability matrix | CORE-PHASE-001 through CORE-PHASE-006 | Each proven gap names exactly one owning requirement, later phase, acceptance condition, and evidence type | Changes require explicit superseding evidence, never silent reassignment | Cross-check report showing no orphan or duplicate ownership |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P000-TASK-001 | CORE-REQ-001 | Freeze repository, release artifact, documentation, issue, optional dependency, and evidence identities before interpretation | SRC-001 through SRC-007, DEC-001 through DEC-005 | Baseline manifest and sanitized evidence index | Repository baseline, release provenance, issue records, evidence store | Identity cross-check proves commit, artifact hashes, issue set, and source roles match the master plan |
| P000-TASK-002 | CORE-REQ-001 | Define and execute normalized reproductions for each issue using the reporter configuration when available | P000-TASK-001, issues `#8`, `#10`, `#11`, `#16`, `#24` | Five reproduction records with expected and observed behavior | Entity-presence fixture, Curios fixture, recipe-viewer fixture, Easy Builder workflow, category-overlay workflow | A second run from the recorded steps produces the same result or records nondeterminism explicitly |
| P000-TASK-003 | CORE-REQ-001 | Inventory the shipped 3.0.3 JAR and enumerate attributable public user-visible claims | P000-TASK-001, shipped artifact, SRC-002, SRC-003 | JAR inventory and public-claim register | Mod metadata, bundled assets, editor bundle, release documentation surfaces | Hash recheck and claim-source review prove artifact and claim identities |
| P000-TASK-004 | CORE-REQ-001 | Classify each issue from reproduction and artifact evidence, including a strict stale-report test | P000-TASK-002, P000-TASK-003, NG-004 | Classification table with rationale and missing-evidence fields | Issue audit matrix | Reviewer can derive each classification without source-presence assumptions |
| P000-TASK-005 | CORE-REQ-001 | Compare every bounded 3.0.3 claim with the shipped artifact and separate proven omissions from novel or roadmap-only ideas | P000-TASK-003, P000-TASK-004, DEC-002, FUT-001 | Advertised-capability parity matrix | Public contract, shipped JAR, production editor bundle | Each mandatory row has a public claim and artifact observation; excluded rows state why they are not promised 3.0.3 behavior |
| P000-TASK-006 | CORE-REQ-001 | Map confirmed outcomes to downstream requirements and measurable acceptance proof | P000-TASK-004, P000-TASK-005, CORE-REQ-002 through CORE-REQ-007 | Acceptance traceability matrix and phase fixture handoffs | Requirements and phase boundaries | Completeness check finds no confirmed gap without one owner and no downstream requirement without relevant baseline input |
| P000-TASK-007 | CORE-REQ-001 | Audit matrix consistency, sensitive-data sanitation, external-boundary preservation, and completion-packet integrity | P000-TASK-001 through P000-TASK-006, EXT-001, EXT-002, EXT-003 | Frozen Phase 000 completion packet | Evidence store and downstream handoff | Independent review confirms identities, hashes, classifications, exclusions, traceability, and external prerequisite states |

Tasks execute in numeric order because interpretation depends on frozen identities, classification depends on reproduction and artifact inspection, and downstream mapping depends on classification. Within P000-TASK-002, independent issue reproductions may run in parallel after P000-TASK-001. Within P000-TASK-003, JAR inventory and public-claim collection may run in parallel after artifact identity is fixed. A failed or ambiguous reproduction does not roll back the baseline manifest; it becomes `blocked_by_missing_evidence` with the missing input recorded. Any exposed sensitive data is removed from the evidence packet and the affected capture is repeated from a sanitized source.

## Architecture and Implementation Boundaries

Phase 000 is observational. It does not change runtime architecture, schemas, persistence, networking, configuration defaults, permissions, client rendering, optional adapters, editor code, or release workflows. RepositoryAudit owns evidence normalization and traceability but does not own a runtime repair.

All observations distinguish the logical server, client presentation, optional adapter, editor bundle, and release tooling boundaries defined in the master plan. Evidence must identify whether it came from source inspection, a development runtime, a production editor bundle, the shipped JAR, an integrated server, or a dedicated server. Source inspection may identify a hypothesis or candidate implementation, but only the required runtime or artifact workflow may prove user-visible behavior.

Reproductions use stable, minimal fixtures and immutable inputs. They record configuration, stage state, player roles, optional-mod set, GUI scale, and artifact identity where applicable. The audit performs no production mutation and requires no publication authorization. Credentials, confirmation codes, private raw logs, personal identifiers, and unrelated environment data are excluded from every retained artifact.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Report lacks an attributable artifact version | Issue record or log omits version identity | Mark classification `blocked_by_missing_evidence`; do not infer the affected release | Request or reconstruct only from authoritative provenance, then append a superseding record | Reviewer sees original blocked row and later evidence identity |
| Source appears fixed but shipped artifact still fails | Source and installed-JAR workflows disagree | Classify from shipped-artifact behavior and record source as a nonconclusive hypothesis | Assign the proven artifact gap to the owning downstream requirement | Re-run installed-JAR workflow from the frozen steps |
| Report cannot be reproduced | Repeated controlled runs differ from the report | Preserve steps, environment, and negative observation; require contradictory artifact evidence before `stale_with_evidence` | Expand only the evidence inputs allowed by the locked report; otherwise remain blocked | Recorded reruns and artifact inspection support the final classification |
| Optional artifact evidence is incomplete | Any EXT-001 typed field is absent | Do not use the artifact to prove supported runtime behavior | Obtain the missing authoritative evidence and repeat the affected reproduction | Evidence register contains all seven typed fields and recalculated hashes |
| Public claim is ambiguous or roadmap-only | Claim lacks attributable 3.0.3 user-visible commitment | Exclude it from CORE-REQ-007 and record the source and exclusion rationale | Escalate only if the owner revises the product contract | Scope review confirms DEC-002 and FUT-001 remain intact |
| Reproduction exposes private or secret data | Sanitization review detects credentials, tokens, personal data, or private raw logs | Quarantine the capture and omit it from the packet | Revoke exposed credentials outside this plan if needed and recapture sanitized evidence | Completion review confirms no prohibited data in retained evidence |
| Evidence changes after baseline freeze | Hash, issue revision, documentation revision, or artifact identity differs | Keep the original record and append a superseding evidence entry with reason and timestamp | Re-run only affected classifications and mappings, then invalidate dependent evidence explicitly | Supersession chain identifies every invalidated and renewed proof |
| External prerequisite remains incomplete | EXT-002 unavailable or EXT-003 unauthorized | Preserve `VALIDATED WITH KNOWN EXTERNAL BLOCKER`; do not claim release readiness | Later owning phase satisfies the exact evidence or authorization contract | Phase packet lists blocker state without implying completion |

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Evidence artifact |
|---|---|---|---|---|---|
| CORE-REQ-001 | Schema and completeness check for required matrix fields and stable IDs | Cross-check issue, source, documentation, and JAR identities | Five recorded reproduction or artifact-verification workflows | Missing-evidence and stale-classification review | Versioned audit matrix and evidence index |
| P000-TASK-001 | Manifest field and hash-format validation | Remote, commit, release, issue, and source-role comparison | Recalculate shipped-artifact hashes | Reject ambiguous or mismatched identity | Baseline manifest |
| P000-TASK-002 | Reproduction record field validation | Fixture inputs compared with reporter evidence | Repeat each applicable issue workflow at least twice | Record nondeterminism or missing prerequisites without inferring success | Five reproduction records |
| P000-TASK-003 | JAR inventory and claim-register completeness | Embedded metadata compared with release provenance | Inspect the exact shipped JAR and production editor bundle | Reject rebuilt or unproven substitute artifacts | Artifact inventory and claim register |
| P000-TASK-004 | Allowed-classification vocabulary and rationale check | Evidence rows linked to classifications | Repeat decisive workflow for each reproduced or stale result | Force unsupported stale results back to blocked | Issue classification table |
| P000-TASK-005 | Claim-source and scope-boundary validation | Public claim linked to artifact observation | Execute the advertised user workflow where artifact presence alone is insufficient | Exclude novel, roadmap-only, or ambiguous claims | Advertised-capability parity matrix |
| P000-TASK-006 | Stable-ID ownership and orphan check | Map classifications to phase contracts | Confirm fixture handoff is executable by the downstream owner | Reject duplicate or missing ownership | Acceptance traceability matrix |
| P000-TASK-007 | Packet manifest and sensitive-data scan | Cross-artifact identity and external-state review | Independent replay of a representative row from each evidence class | Preserve blocked state for missing proof and append superseding evidence for corrections | Signed or hash-identified completion packet manifest |

Fixtures are the smallest report-specific configurations that preserve the observed behavior: a mixed-player entity-presence scenario for issue `#24`, Curios present and absent states for issue `#8`, JEI and EMI installation and enablement states for issue `#10`, an operator enchantment-rule editor round trip for issue `#11`, and a populated stage map at supported GUI scales for issue `#16`. Expected results come from the public contract and issue statements, while observed results come from the pinned artifact workflow. Run identity checks before any reproduction, then reproduce, inspect artifacts, classify, map, and perform the packet audit. A failed high-fidelity workflow cannot be replaced by source inspection or a release-note claim.

## Documentation, Operations, and Release

This phase produces internal execution evidence only: the audit matrix, artifact inventory, claim register, reproduction procedures, classification rationale, acceptance traceability matrix, and sanitized completion-packet manifest. It does not edit `README.md`, `DOCUMENTATION.md`, the documentation index, release notes, issue state, or platform metadata. Later phases may update those surfaces only after behavior is implemented and verified. Operations must retain exact artifact hashes, test environment identities, evidence collection dates, and supersession links. No release packaging, tagging, attestation, broker preview, publication, or issue closure occurs in Phase 000.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| Baseline branch or artifact drifts during audit | Pin commit and artifact hashes before inspection | Identity check before every decisive workflow | Stop, preserve old packet, and start a superseding capture for the new identity | All observations after the first mismatch | Repeat identity check and affected workflows |
| Development source is mistaken for shipped behavior | Label every observation by evidence surface | Source and installed-JAR result disagree | Retain source result as hypothesis and rerun shipped workflow | Any conclusion based only on source presence | Installed-JAR or production-bundle proof |
| A report is incorrectly marked stale | Require explicit contradictory artifact evidence and repeated workflow | Reviewer cannot derive stale result from evidence | Restore `blocked_by_missing_evidence` or `reproduced` classification | Stale classification and downstream mapping | Repeat reproduction and independent classification review |
| Audit expands into new feature work | Require public 3.0.3 claim identity for CORE-REQ-007 | Matrix row lacks attributable claim | Move row to excluded evidence with DEC-002 rationale | Gap classification and assigned requirement | Scope audit against FUT-001 and FUT-003 |
| Optional integration evidence is unauthenticated or incompatible | Enforce every EXT-001 typed evidence field | Version, compatibility, provenance, review, or hash field is missing | Replace with authoritative artifact evidence and rerun | Reproduction using the invalid artifact | Artifact identity review and affected workflow |
| Sanitized evidence omits a decisive fact | Keep a field-level redaction note without private content | Reviewer cannot reproduce the conclusion | Recapture a minimal sanitized proof | Classification depending on omitted fact | Independent replay from sanitized inputs |
| External blocker state is overstated | Copy exact availability and authorization values | Packet differs from master prerequisite table | Correct the packet without claiming the prerequisite resolved | Downstream release-readiness conclusion | External-boundary cross-check |

## Phase Completion Packet

The Phase 000 completion packet must contain the baseline manifest; shipped JAR SHA-256 and SHA-512; JAR inventory; public 3.0.3 claim register; sanitized evidence index; five issue reproduction or artifact-verification records; classification table; advertised-capability parity matrix; acceptance traceability matrix; fixture handoffs; external-boundary register; independent consistency review; and a packet manifest that identifies every artifact by digest or immutable source identity. It must record the repository commit, installed artifact, environment versions, configuration identities, evidence dates, classification vocabulary, exclusions, and any blocked-by-missing-evidence row.

No source commit, corrective patch, issue closure, pull request state change, release artifact, tag, publication preview, or platform mutation is part of this packet. The packet remains execution evidence outside the protected plan set. Phase 000 may close only when CORE-REQ-001 acceptance criteria pass, every packet identity is reproducible, sensitive-data review passes, and downstream phase owners can execute their assigned fixtures without guessing.

## Next Transition

After the Phase 000 completion packet passes its independent consistency review, integrate only the approved Phase 000 evidence through the repository workflow and verify that the resulting `master` revision preserves the pinned audit artifacts. Then CORE-PHASE-001 begins from that verified `master` state using the entity-presence fixture and issue `#24` mapping from the packet. CORE-PHASE-001 must not begin before Phase 000 integration gates pass. Optional-integration and client/editor work remains governed by its own later sequential phases even if their Phase 000 evidence collection completed in parallel.

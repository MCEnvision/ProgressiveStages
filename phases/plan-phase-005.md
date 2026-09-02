# Phase 005 Execution Plan

> **Plan ID:** PLAN-PHASE-005
> **Phase ID:** CORE-PHASE-005
> **Owner:** ReleaseValidation
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 005 of 006

## Purpose and Ownership

This phase integrates the corrected shared release-validation workflow into ProgressiveStages, produces the 3.0.4 release candidate from the signed and verified `master` revision, and proves that the candidate, checksums, SBOM, source manifest, and attestations all identify the same artifact. It exists because the prior production deployment reached artifact generation but failed attestation verification when incompatible GitHub CLI signer flags were combined. Publication must not proceed until the corrected verifier is available through EXT-002 and this phase proves both the successful and failing verification paths.

The master plan owns the product scope, the 3.0.4 endpoint, and the global phase order. This blueprint owns only CORE-REQ-009. The corrected shared-workflow implementation and its merged immutable revision are supplied through EXT-002. This phase validates that revision, pins ProgressiveStages to it, integrates the pin through the repository workflow, creates the release candidate from the resulting signed `master` commit, and records nondestructive release-validation evidence. It does not authorize a CurseForge or Modrinth upload, create an approval substitute, close issues, or bypass a failed check.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| VERIFIED | Upstream product verification | CORE-PHASE-004 must provide complete compatibility, security, build, server, client, multiplayer, and final-JAR evidence for the candidate source | CORE-PHASE-004 completion packet | Any source, resource, dependency, workflow caller, or release metadata change after the recorded verification invalidates the packet |
| VERIFIED | Release validation failure | GitHub Actions run `31453460136`, job `93662356066` failed because the verifier combined mutually incompatible GitHub CLI signer flag groups | SRC-007 and the master plan's known failing check | A successful run against a corrected, pinned shared-workflow revision supersedes this failure only for the exact tested artifact identity |
| UNKNOWN | Corrected shared workflow | EXT-002 is unavailable until the shared revision is merged, accessible, and identified by an immutable revision | EXT-002 prerequisite record | The revision is force-replaced, becomes inaccessible, or differs from the reviewed revision |
| VERIFIED | Required release evidence | CORE-REQ-009 requires a matching release artifact, SHA-256, SHA-512, SBOM, source manifest, and build-provenance and SBOM attestations | CORE-REQ-009 and the master verification strategy | Any rebuild, source-commit change, metadata change, or artifact byte change invalidates the identity set |
| VERIFIED | Verification mode | This phase is nondestructive and cannot publish to either distribution platform | CORE-REQ-009 production-verification classification | Any workflow path capable of platform publication or issue closure falls outside this phase |

## Scope Boundaries

### Included Scope

- CORE-REQ-009 only: validate EXT-002, pin the corrected shared release-validation workflow by immutable revision, integrate that pin, produce the 3.0.4 candidate from the signed `master` revision, generate identity and provenance artifacts, and prove attestation success and tamper rejection.
- Confirm that the corrected verifier checks both build provenance and SBOM attestations without combining incompatible GitHub CLI signer flag groups.
- Establish a single release identity record binding the source commit, candidate JAR, SHA-256, SHA-512, SBOM, source manifest, workflow revision, attestation identities, and verification run.
- Retain a nondestructive validation packet that CORE-PHASE-006 can consume without rebuilding or silently substituting the candidate.

### Explicit Exclusions

- CORE-REQ-010 and CORE-PHASE-006: publishing to CurseForge or Modrinth, obtaining EXT-003 confirmation, creating the platform release records, verifying platform downloads, and closing issues remain later-phase work.
- EXT-002 is not treated as satisfied by a proposed change, branch, mutable reference, local edit, or successful job that does not verify both required attestations. Its correction must be merged, accessible, and immutable before this phase proceeds.
- FUT-001 through FUT-004 remain excluded. This phase introduces no product feature, dependency upgrade, Minecraft or NeoForge upgrade, schema change, or unrelated workflow modernization.
- A retry that disables attestation verification, weakens identity constraints, ignores a failed step, substitutes a manual assertion, or uses administrative bypass is prohibited.
- No credentials, confirmation codes, private logs, or release-broker secrets may be copied into source, plans, workflow output, documentation, or evidence artifacts.

## Phase Contract

### CORE-PHASE-005 — Integrate and validate the release artifact

**Objective:** Pin and integrate the corrected shared verifier, then produce and nondestructively validate one 3.0.4 candidate whose source, binary, hashes, SBOM, manifest, and attestations have one proven identity
**Owner:** ReleaseValidation
**Dependencies:** CORE-PHASE-004, EXT-002
**Canonical requirements:** CORE-REQ-009
**Documentation and release impact:** Update the existing release-validation and verification documentation with the pinned workflow identity, candidate identity procedure, expected success and tamper-failure behavior, evidence locations, rerun conditions, and the explicit no-publication boundary
**Next transition:** CORE-PHASE-006

**Entry criteria**

- CORE-PHASE-004 is merged through its required integration path, `origin/master` contains that merge, and its completion packet passes without an unresolved mandatory defect.
- EXT-002 supplies a merged, accessible, immutable shared-workflow revision plus evidence that its verifier is intended to validate build provenance and SBOM attestations using a compatible GitHub CLI invocation.
- The phase branch is created from the verified updated `origin/master`, the worktree is clean, Git identity and SSH signing configuration are verified, and the repository caller currently used for release validation is identified without changing its publication permissions.
- The exact 3.0.4 metadata, Java 21 environment, checked-in Gradle Wrapper, supported Minecraft 1.21.1 and NeoForge 21.1.219 boundary, and expected release artifact naming are recorded from the verified repository state.
- Publication credentials and EXT-003 are neither required nor consumed. Any workflow invocation selected for this phase is confirmed to be validation-only.

**Implementation scope**

- CORE-REQ-009 validates the merged EXT-002 revision against the prior failure mode and records its immutable revision, reviewed verifier behavior, required permissions, and reusable-workflow interface.
- CORE-REQ-009 pins the ProgressiveStages release-validation caller to that exact immutable shared-workflow revision while preserving least-privilege permissions and existing artifact identity checks.
- CORE-REQ-009 integrates the caller pin through the required branch, pull request, check, merge, and signed-commit workflow. It does not directly update `master` or bypass a failed requirement.
- CORE-REQ-009 builds one clean 3.0.4 candidate from the resulting signed and verified `master` commit using Java 21 and the checked-in Gradle Wrapper. The artifact is identified by its signed source commit and build-provenance attestation without inventing a separate JAR-signing mechanism.
- CORE-REQ-009 generates SHA-256 and SHA-512 checksums, an SPDX SBOM, a source-commit manifest, a JAR inventory, and supported attestations for the exact candidate bytes. Every record uses the same artifact name, size, hashes, and source commit.
- CORE-REQ-009 invokes the pinned reusable workflow against a disposable release candidate. It proves valid build-provenance and SBOM attestations pass and a byte-modified copy or deliberately mismatched identity fails without mutating the valid candidate.
- CORE-REQ-009 stores a sanitized completion packet containing identities, commands or workflow inputs, run references, expected and observed results, and evidence-retention locations for CORE-PHASE-006.

**Execution order**

1. `P005-TASK-001` executes CORE-REQ-009 prerequisite admission by validating CORE-PHASE-004 evidence and the immutable EXT-002 revision.
2. `P005-TASK-002` executes CORE-REQ-009 shared-workflow review by confirming compatible attestation-verification semantics, least-privilege permissions, and failure behavior.
3. `P005-TASK-003` executes CORE-REQ-009 integration by pinning the caller to EXT-002 and carrying the change through required checks and signed `master` integration.
4. `P005-TASK-004` executes CORE-REQ-009 candidate construction by building 3.0.4 once from the resulting clean, signed `master` revision and recording its identity.
5. `P005-TASK-005` executes CORE-REQ-009 evidence generation by producing hashes, JAR inventory, SPDX SBOM, source manifest, build-provenance attestation, and SBOM attestation for the same candidate.
6. `P005-TASK-006` executes CORE-REQ-009 real-workflow validation by proving the valid candidate passes and a tampered or mismatched candidate fails under the pinned verifier.
7. `P005-TASK-007` executes CORE-REQ-009 reconciliation by auditing identity equality, documentation, retention, and the CORE-PHASE-006 handoff without publishing.

**Required evidence**

- The immutable EXT-002 shared-workflow revision, its merge record, and a review record showing the previous incompatible signer-flag combination is absent from the effective verifier path.
- The ProgressiveStages caller diff and merged revision proving the reusable workflow is pinned to EXT-002 rather than a mutable branch or tag.
- Required-check results, signed integration-commit verification, and proof the candidate was built from the resulting `master` commit in a clean workspace.
- Candidate filename and size, SHA-256, SHA-512, JAR inventory, SPDX SBOM identity, source manifest, build-provenance attestation identity, and SBOM attestation identity.
- A successful reusable-workflow run for the untouched candidate and a failed verification record for the tampered or identity-mismatched disposable copy.
- An equality audit showing all evidence records resolve to the same source commit and exact candidate bytes, plus a sanitized runbook for reproducing validation.
- Proof that no CurseForge or Modrinth publication operation ran and no baseline issue was closed by this phase.

**Exit criteria**

- EXT-002 is merged, accessible, pinned by immutable revision, and proven to verify both required attestation classes using compatible GitHub CLI semantics.
- The phase's workflow change is integrated through required checks, and the candidate source is the signed and verified resulting `master` commit.
- One 3.0.4 candidate, its SHA-256, SHA-512, JAR inventory, SPDX SBOM, source manifest, and attestations pass the identity-equality audit.
- The untouched disposable candidate passes the pinned reusable workflow, while the tampered or mismatched copy fails closed with an expected verification error.
- Documentation and the completion packet identify exact rerun conditions, evidence locations, and the prohibition on publishing before CORE-PHASE-006 and EXT-003.
- CORE-PHASE-006 can consume the candidate and validation packet without rebuilding, re-signing, or changing release metadata.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| CORE-PHASE-004 completion packet | CORE-PHASE-004 | All compatibility, security, runtime, build, and JAR inspection gates pass for the source entering release integration | Reconcile packet commit and artifact references with updated `origin/master` | Stop; route the defect to its owning prior phase and invalidate candidate work |
| Corrected shared workflow | EXT-002 | Merged, accessible, immutable revision verifies build provenance and SBOM attestations with compatible GitHub CLI semantics | Inspect the effective reusable-workflow revision and its successful disposable-candidate evidence | Remain `NOT COMPLETE — EXTERNALLY BLOCKED`; do not pin a branch, disable verification, or retry publication |
| Owner decisions | DEC-001, DEC-005 | 3.0.4 publication remains the endpoint and shared verification remains mandatory | Compare the selected choices with the phase boundary | Stop on conflict and return to the authoritative plan; do not infer a new endpoint |
| Repository integration contract | Master plan and repository workflow | Sequential phase branch starts from updated `origin/master`, required checks pass, merge is performed through GitHub, and commits remain signed | Branch ancestry, pull-request merge record, check results, and signature verification | Stop integration; repair the phase branch and rerun invalidated checks |
| Release metadata contract | Verified repository state | Version, platform, artifact naming, and dependency metadata identify 3.0.4 without unrelated upgrades | Compare build metadata, packaged metadata, candidate name, and source manifest | Reject the candidate and correct metadata before evidence generation |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Pinned release-validation caller | CORE-PHASE-006 | Caller resolves to the exact immutable EXT-002 revision and preserves least-privilege validation behavior | Pin remains fixed for the 3.0.4 release; any pin change invalidates Phase 005 | Merged caller diff, revision identities, and required-check run |
| Verified 3.0.4 candidate | CORE-PHASE-006 | Candidate bytes derive from the signed verified `master` commit and passed all Phase 005 validation | Candidate is immutable by identity; any byte, metadata, name, or source revision change requires complete revalidation | Candidate identity record, hashes, source manifest, JAR inventory, and successful workflow run |
| Provenance packet | CORE-PHASE-006 | Build-provenance and SBOM attestations match the candidate and trusted repository/workflow identity | Attestation or workflow-identity changes invalidate the packet | Attestation verification outputs and identity-equality audit |
| Tamper-rejection packet | CORE-PHASE-006 | The pinned verifier fails closed for a modified or identity-mismatched disposable copy | Negative fixture is disposable and must never replace the valid candidate | Expected failure record and fixture derivation note |
| Release-validation runbook | CORE-PHASE-006 and operators | Reproduction, evidence retention, invalidation, and recovery steps are complete and sanitized | Applies only to the recorded 3.0.4 candidate and pinned workflow revision | Documentation diff and completion-packet index |
| No-publication boundary | CORE-PHASE-006 | Phase 005 performed no platform mutation and consumed no EXT-003 authorization | Publication remains gated by CORE-PHASE-006 and EXT-003 | Workflow audit and absence of platform publication records |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P005-TASK-001 | CORE-REQ-009 | Admit upstream evidence and freeze exact input identities | CORE-PHASE-004, EXT-002, DEC-005 | Admission record containing source commit, phase packet, shared revision, and blocker state | Phase completion packets, shared workflow revision, repository ancestry | All input identities exist, are immutable, and agree; otherwise phase remains blocked |
| P005-TASK-002 | CORE-REQ-009 | Review the effective corrected verifier and its reusable interface | P005-TASK-001 | Compatibility review covering both attestation classes, trust identity, permissions, and failure semantics | MCEnvision shared release-validation workflow interface | Review proves compatible flag use and fail-closed behavior without disabling an identity check |
| P005-TASK-003 | CORE-REQ-009 | Pin the caller and integrate through the required repository workflow | P005-TASK-002 | Merged caller pin on signed `master` revision | ProgressiveStages release-validation caller and GitHub integration contract | Required checks pass, merge ancestry is correct, signature verifies, and caller resolves to EXT-002 |
| P005-TASK-004 | CORE-REQ-009 | Build one clean 3.0.4 candidate from the resulting master revision | P005-TASK-003, CORE-PHASE-004 build contract | Candidate JAR, build log, source identity, filename, and size | Gradle packaging, mod metadata, artifact output | Java 21 checked-in Wrapper build passes, workspace is clean, JAR inventory and metadata identify 3.0.4 |
| P005-TASK-005 | CORE-REQ-009 | Generate the complete identity and provenance set | P005-TASK-004 | SHA-256, SHA-512, SPDX SBOM, source manifest, JAR inventory, build-provenance attestation, and SBOM attestation | Release evidence generator and attestation interface | Equality check ties every output to the candidate bytes and signed source commit |
| P005-TASK-006 | CORE-REQ-009 | Run success and tamper-rejection validation through the pinned reusable workflow | P005-TASK-005 | Successful valid-candidate run and expected tampered-candidate failure | Pinned shared release-validation workflow | Both attestation classes pass for valid bytes; modified or mismatched bytes fail closed |
| P005-TASK-007 | CORE-REQ-009 | Reconcile evidence, update operations documentation, and form the downstream packet | P005-TASK-001 through P005-TASK-006 | Sanitized completion packet and CORE-PHASE-006 handoff | Existing release documentation, evidence retention, release broker input manifest | Cross-record identity audit passes and audit proves no publication or issue closure occurred |

P005-TASK-001 is strictly first. P005-TASK-002 cannot accept a mutable or inaccessible EXT-002 reference. P005-TASK-003 must complete integration before the authoritative candidate can be built. P005-TASK-004 through P005-TASK-006 are serial because each output defines the next identity boundary. Documentation drafting may proceed beside evidence generation, but P005-TASK-007 must reconcile only final evidence. On any failure, retain sanitized diagnostics, discard the invalid candidate from the handoff set, correct the owning input or implementation on the appropriate branch, and rerun that task plus every downstream task. No task may safely proceed to platform publication in parallel.

## Architecture and Implementation Boundaries

The ProgressiveStages repository owns its release-validation caller, build metadata, release candidate, and evidence bundle. The shared MCEnvision workflow owns verifier implementation and is consumed only through the immutable EXT-002 revision. Dependency direction is one way: the repository caller invokes the pinned shared workflow; the shared workflow must not mutate ProgressiveStages source, platform releases, issue state, or editor data.

The release identity is a closed set containing the signed source commit, exact candidate bytes, filename, size, SHA-256, SHA-512, source manifest, JAR inventory, SPDX SBOM, build-provenance attestation, SBOM attestation, shared-workflow revision, and verification-run identity. The source commit and artifact hashes are the primary join keys. A rebuild is a new candidate even when its version string is unchanged. No evidence may be reused after a byte or source-identity change.

The verifier must authenticate the expected repository and workflow identities and verify both required attestation predicate classes. Its GitHub CLI invocation must follow the installed CLI's mutually exclusive identity-filter rules. Validation may split checks when one invocation cannot express all required predicates safely. It must never weaken trust by omitting repository or workflow identity, ignoring verification exit status, or treating the mere presence of an attestation as proof.

All build and verification operations are nondestructive. Platform API calls, publication broker calls, release promotion, issue closure, and destructive rollback are outside this phase. Workflow permissions remain least privilege and must be documented. Untrusted pull-request code must not receive secrets or run on a privileged self-hosted runner. Logs and completion evidence are sanitized before retention.

The candidate is built with Java 21 and the checked-in Gradle Wrapper using the versions already pinned by the repository. This phase cannot update Minecraft, NeoForge, mappings, Java, Gradle, dependencies, configuration schemas, saved data, or product behavior. Reproducibility is assessed through exact input capture and artifact identity; if repeated builds are expected by the repository's release process, differing bytes must be investigated rather than silently selected.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| EXT-002 remains unavailable or mutable | Shared revision cannot be fetched by immutable identity or has no merge evidence | Stop before caller changes and retain external-blocker status | Complete EXT-002 externally, then restart admission with the new immutable revision | P005-TASK-001 admission record |
| Corrected verifier still combines incompatible flags | Effective command review or disposable run reproduces the GitHub CLI error | Reject EXT-002 as unsatisfied; do not bypass or edit only the local caller to hide the failure | Correct and merge a new shared revision, then repeat P005-TASK-001 and P005-TASK-002 | Successful shared disposable-candidate run plus command review |
| Caller points to a mutable ref or wrong revision | Resolved reusable-workflow identity differs from EXT-002 | Fail the integration check | Correct the pin on the phase branch and rerun all caller and workflow checks | Merged diff and resolved revision evidence |
| Upstream source changes during integration | `origin/master` or candidate source commit differs from admitted CORE-PHASE-004 identity | Invalidate upstream build and artifact evidence | Reconcile the new revision, rerun affected Phase 004 gates, then restart candidate generation | Updated compatibility packet and ancestry record |
| Build fails or workspace is dirty | Gradle failure, unexpected diff, or untracked release input | Produce no candidate handoff | Correct on the proper phase branch, clean only owned generated output, and rebuild after required checks | Clean-state proof and successful build log |
| Hash, SBOM, manifest, or attestation identity differs | Equality audit cannot join all records to exact bytes and source commit | Quarantine the candidate and mark validation failed | Regenerate all derived evidence from one canonical candidate; investigate nondeterminism before retry | Full P005-TASK-005 identity audit |
| Valid candidate lacks one attestation class | Verifier reports missing or wrong predicate | Fail closed | Correct evidence generation or workflow permissions, then regenerate and reverify all attestations | Successful checks for build provenance and SBOM predicates |
| Tampered candidate passes | Negative fixture returns success | Treat as a release-blocking verifier defect | Correct EXT-002, repin, rebuild if trust inputs changed, and rerun both positive and negative paths | Expected nonzero verification outcome for tampered fixture |
| Validation rate limit or transient service failure | API or workflow reports an explicitly transient condition with no identity mismatch | Preserve candidate identity but do not mark validation complete | Retry with bounded attempts against the same hashes and record attempts; escalate persistent failure | Later successful run tied to unchanged candidate identity |
| Attestation trust identity is broader than intended | Review shows repository or workflow binding is absent or ambiguous | Reject the verifier configuration | Narrow trust binding in the shared workflow and repeat validation | Negative test using an untrusted identity fails |
| Publication-capable path is invoked | Audit log or workflow graph shows a platform mutation step | Stop immediately and do not use its results | Verify no release was created; if mutation occurred, route to owner under EXT-003 recovery rules and keep phase incomplete | No-publication audit before handoff |
| Evidence contains a credential or private token | Secret scan or manual review detects sensitive material | Remove evidence from circulation and stop completion | Revoke exposed credential through the authorized owner process, sanitize evidence, and rerun affected workflow | Clean secret scan and reviewed evidence packet |

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Evidence artifact |
|---|---|---|---|---|---|
| P005-TASK-001 | Identity-format and dependency-completeness checks | Reconcile Phase 004, `master`, and EXT-002 identities | Fetch the exact shared revision and verify merge accessibility | Missing, mutable, or mismatched input blocks entry | Admission record |
| P005-TASK-002 | Review verifier predicates, identity filters, exit-status handling, and permissions | Exercise reusable interface with a disposable candidate | Run corrected shared verifier without the prior incompatible flag error | Wrong predicate, untrusted identity, and incompatible-flag cases fail closed | Shared-workflow compatibility review and run reference |
| P005-TASK-003 | Workflow syntax and immutable-reference checks | Required repository checks on the phase integration | Resolve the merged caller and run it from the signed `master` revision | Mutable pin, wrong ancestry, failed signature, or failed check blocks merge evidence | Caller diff, merge record, signatures, and check results |
| P005-TASK-004 | Metadata, filename, source revision, and JAR-structure checks | Full repository verification inherited from and reconciled with Phase 004 | Java 21 checked-in Wrapper build from clean signed `master` | Dirty source, build failure, metadata drift, or rebuild mismatch rejects candidate | Build log, candidate, source identity, and JAR inventory |
| P005-TASK-005 | Hash formatting, SBOM validity, manifest completeness, and cross-record equality checks | Generate all evidence from one canonical candidate | Create supported build-provenance and SBOM attestations | Altered byte or mismatched source identity breaks equality | SHA-256, SHA-512, SPDX SBOM, source manifest, attestations, equality report |
| P005-TASK-006 | Validate expected predicate and trust-identity configuration | Invoke the pinned reusable workflow with the complete evidence set | Untouched candidate passes both attestation checks | Byte-modified or identity-mismatched disposable copy fails and cannot replace valid candidate | Positive and negative workflow records |
| P005-TASK-007 | Documentation and packet link checks | Reconcile every retained record with the canonical identity | Dry-run the Phase 006 intake without platform mutation | Stale record, missing evidence, or publication trace blocks handoff | Completion-packet index, runbook, and no-publication audit |
| CORE-REQ-009 | All task-level static checks pass | Pinned verifier, signed source integration, artifact generation, and evidence reconciliation operate as one chain | Disposable valid candidate passes the real reusable workflow | Tampered candidate fails, recovery reruns invalidated downstream tasks, and bypass is impossible | Complete Phase 005 validation packet |

The fixture set consists of the untouched 3.0.4 candidate, its complete derived evidence set, a byte-modified copy that cannot share the valid hashes, and, when needed, a metadata or trust-identity mismatch fixture. Tests run in task order. Expected success is zero verification failures for both attestation classes on the untouched candidate. Expected negative behavior is a nonzero verifier result with a specific sanitized identity or digest error. Infrastructure-only retries are permitted only when candidate bytes, source commit, workflow revision, and verification inputs remain unchanged. A source, workflow, metadata, permission, or byte change restarts every downstream check from its change boundary.

## Documentation, Operations, and Release

- Update the repository's existing release-validation documentation with the immutable EXT-002 pin, exact evidence chain, installed-tool assumptions, required permissions, success interpretation, tamper-failure interpretation, invalidation rules, and rerun order.
- Record the candidate identity schema: signed source commit, filename, size, SHA-256, SHA-512, JAR inventory, SPDX SBOM, source manifest, build-provenance attestation, SBOM attestation, workflow revision, and workflow run.
- Document how to distinguish a verification defect from a transient infrastructure failure and how to resume without reusing invalid evidence.
- Add a sanitized Phase 006 handoff checklist that confirms the publication preview must use the exact Phase 005 candidate and hashes. Any rebuild or metadata edit returns work to this phase.
- Keep release notes and platform-specific publication copy in CORE-PHASE-006. Phase 005 may validate metadata inputs but cannot publish, close issues, or claim the plan endpoint.
- Retain the positive and negative validation records according to the repository's established release-evidence policy. If no durable location is established, the phase must define one in existing operations documentation before closure rather than invent an untracked local location.
- Confirm release-facing workflows cannot expose credentials in logs and that the completion packet contains only sanitized, shareable evidence.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| Shared workflow pin drifts after review | Use immutable EXT-002 revision and record resolved identity | Caller resolution differs from completion packet | Repin reviewed merged revision and reintegrate | Caller checks, workflow runs, attestations, completion packet | Repeat P005-TASK-003 through P005-TASK-007 |
| GitHub CLI semantics change | Pin or record effective tool version and test actual invocation | Corrected command begins failing or filtering differently | Update EXT-002 through reviewed shared change | Verifier review and all positive and negative runs | Repeat P005-TASK-001, P005-TASK-002, P005-TASK-006, and P005-TASK-007 |
| Candidate rebuilt after validation | Treat hashes as immutable identity and prevent silent replacement | Candidate hash or size differs | Discard derived evidence and validate the new bytes from source identity | Hashes, SBOM, manifest, attestations, run evidence | Repeat P005-TASK-004 through P005-TASK-007 |
| Source or release metadata changes | Freeze signed `master` commit and metadata in admission record | Git ancestry or packaged metadata differs | Rerun upstream affected gates and rebuild | Phase 004 packet and all Phase 005 evidence | Repeat affected Phase 004 work, then all Phase 005 tasks |
| Attestation verifies but wrong repository or workflow is trusted | Require explicit trust-identity review and negative fixture | Untrusted identity is accepted or expected identity is not recorded | Correct trust binding in EXT-002 | Verifier review, attestations, run evidence | Repeat P005-TASK-001, P005-TASK-002, P005-TASK-005 through P005-TASK-007 |
| SBOM does not describe candidate contents | Cross-check SBOM identity and component evidence against JAR inventory and build inputs | Missing or mismatched package relationships | Regenerate SBOM and investigate generator inputs | SBOM attestation and equality audit | Repeat P005-TASK-005 through P005-TASK-007 |
| Negative test damages canonical evidence | Create a disposable copy after hashes are frozen and keep it outside candidate handoff | Canonical hash changes or negative fixture path is reused | Restore candidate only from verified build output or rebuild and revalidate | Candidate and all derived evidence | Repeat P005-TASK-004 through P005-TASK-007 |
| Validation accidentally publishes | Audit workflow graph and permissions before invocation | Platform release or upload audit event exists | Stop; invoke owner-authorized recovery under EXT-003 and keep Phase 005 incomplete | No-publication evidence and endpoint status | Reaudit external state, then repeat P005-TASK-007 after authorized recovery |
| Secret enters retained evidence | Secret scan and minimal logging | Scanner finding or manual review | Revoke through authorized process and regenerate sanitized evidence | Affected logs, run references, and packet | Repeat affected workflow and packet review |

## Phase Completion Packet

Phase 005 may close only when the external execution record contains all of the following:

- CORE-PHASE-004 completion packet identity and proof that the Phase 005 branch began from the approved updated `origin/master`.
- EXT-002 immutable revision, merge record, accessibility proof, verifier compatibility review, and reusable-workflow interface record.
- ProgressiveStages caller pin diff, pull-request merge record, required-check results, merged `master` revision, and signed-commit verification.
- Clean Java 21 Gradle build result for the 3.0.4 candidate, candidate filename and size, source commit, packaged metadata inspection, and JAR inventory.
- SHA-256, SHA-512, SPDX SBOM, source-commit manifest, build-provenance attestation identity, and SBOM attestation identity for the candidate.
- Successful valid-candidate reusable-workflow run and expected tampered or mismatched candidate failure record.
- Cross-record identity-equality report, evidence-retention index, secret scan, release-validation documentation diff, rerun and recovery runbook, and no-publication audit.
- A downstream handoff stating that CORE-PHASE-006 must use these exact candidate bytes and must obtain EXT-003 before any platform mutation.
- Confirmation that no known CORE-REQ-009 defect remains. Any failed, missing, stale, lower-fidelity, or mismatched item leaves this phase incomplete.

## Next Transition

After GitHub confirms the Phase 005 integration is merged, required checks remain successful, `origin/master` contains the signed resulting commit, and the complete Phase 005 packet passes its identity audit, reread `phases/plan-phase-006.md` through EOF. Begin CORE-PHASE-006 only with the exact validated candidate and completion packet. CORE-PHASE-006 must independently obtain the scope-bound EXT-003 confirmation before publication. If any candidate byte, source revision, workflow pin, release metadata, checksum, SBOM, manifest, or attestation changes, do not transition; return to the earliest invalidated Phase 005 task and regenerate every downstream proof.

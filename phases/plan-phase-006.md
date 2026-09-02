# Phase 006 Execution Plan

> **Plan ID:** PLAN-PHASE-006
> **Phase ID:** CORE-PHASE-006
> **Owner:** ReleaseBroker
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 006 of 006

## Purpose and Ownership

This final phase publishes the exact ProgressiveStages 3.0.4 artifact proven by Phase 005, verifies the CurseForge and Modrinth copies against that signed artifact identity, and closes issues `#8`, `#10`, `#11`, `#16`, `#24`, and `#25` only after their merged-revision acceptance evidence is complete. The master plan owns the product scope, completion endpoint, and external authorization contract. This blueprint owns only the dependency-ordered release, verification, documentation, monitoring, and issue-closure work for CORE-REQ-010.

The configured release broker is the only platform-mutation boundary. Publication requires the owner-approved broker confirmation described by EXT-003, and that confirmation is requested only after the preview identifies the same signed artifact, checksums, source commit, signed tag, metadata, dependencies, release notes, and platform targets as the Phase 005 completion packet. Neither an earlier confirmation nor a confirmation bound to another artifact authorizes publication.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| VERIFIED | Phase topology | CORE-PHASE-006 is the final mandatory phase and owns CORE-REQ-010 only | Master plan phased roadmap and requirements | Invalidated if the authoritative master plan changes phase ownership or the completion endpoint |
| VERIFIED | Release target | The selected endpoint requires ProgressiveStages 3.0.4 on CurseForge and Modrinth | DEC-001 and CORE-REQ-010 | Invalidated by a new resolved owner decision in the authoritative plan |
| VERIFIED | Artifact gate | Phase 005 must provide one signed, attested, checksum-addressed 3.0.4 artifact merged into `master` | CORE-PHASE-005 dependency and CORE-REQ-009 evidence contract | Invalidated by any new commit, rebuild, retag, checksum change, metadata change, or failed attestation validation |
| VERIFIED | Publication authority | EXT-003 is mandatory and currently not authorized | Master plan EXT-003 contract | Invalidated only by confirmation carrying every required scope binding for the frozen artifact and operation |
| VERIFIED | Issue baseline | Closure is limited to issues `#8`, `#10`, `#11`, `#16`, `#24`, and `#25` | CORE-REQ-001 baseline and CORE-REQ-010 | Invalidated if Phase 000 evidence proves one report was not part of the locked baseline |
| PROPOSED | Platform result | Published files and project metadata must match the approved preview and Phase 005 identity | CORE-REQ-010 acceptance criteria | Becomes verified only after both platform downloads and metadata are independently checked |

## Scope Boundaries

### Included Scope

- CORE-REQ-010 — Build, integrate, attest, document, publish, and verify 3.0.4, then close the six baseline issues with evidence. This phase consumes the already built, integrated, and attested Phase 005 artifact and owns its final preview, scoped authorization, platform publication, downloaded-artifact verification, release documentation, immediate post-release monitoring, issue closure, and plan-wide completion audit.
- EXT-003 — Obtain owner-approved platform publication confirmation bound to the final artifact identities, operations, operators, rollback policy, runbook digest, systems, and time window before the broker performs any platform mutation.
- Verify CurseForge and Modrinth independently. Success on one platform does not substitute for the other.
- Close issues `#8`, `#10`, `#11`, `#16`, `#24`, and `#25` only when each closure record links the merged revision and the acceptance evidence specific to that report. Issue `#25` additionally requires the CORE-REQ-011 visual-form-to-runtime recipe-lock packet, including canonical `[recipes].locked_items` serialization, persistence, reload, enforcement, rule-preservation, and rejected-draft rollback proof.

### Explicit Exclusions

- CORE-REQ-001 through CORE-REQ-009 implementation work is upstream. A defect in that work returns to the owning phase boundary and invalidates the release candidate rather than being repaired during publication.
- FUT-001, FUT-002, FUT-003, and FUT-004 remain excluded. This phase does not add legacy roadmap features, merge optional dependency maintenance, accept post-baseline features, or upgrade Minecraft, NeoForge, Java, Gradle, or mappings.
- NG-001 through NG-004 remain binding. Publication cannot justify a progression redesign, schema overhaul, hard optional dependency, hot-path scan, or evidence-free issue closure.
- The broker confirmation does not authorize unrestricted future publication, artifact replacement, another version, credential access, approval bypass, automated confirmation, or destructive rollback outside the exact EXT-003 scope.
- No platform is mutated while the preview, confirmation, release identity, or Phase 005 evidence is incomplete or inconsistent.

## Phase Contract

### CORE-PHASE-006 — Publish the verified patch and close issues

**Objective:** Publish the exact signed and verified ProgressiveStages 3.0.4 artifact to CurseForge and Modrinth, prove both hosted downloads match it, close all six baseline issues with merged-revision acceptance evidence, and satisfy the plan-wide completion endpoint
**Owner:** ReleaseBroker
**Dependencies:** CORE-PHASE-005, EXT-003
**Canonical requirements:** CORE-REQ-010
**Documentation and release impact:** Finalize verified 3.0.4 release notes, README.md and DOCUMENTATION.md changes, the documentation index, platform metadata, release runbook evidence, issue closure records, download verification, rollback guidance, and post-release monitoring evidence
**Next transition:** final completion

**Entry criteria**

- CORE-PHASE-005 is integrated and its completion packet proves that the 3.0.4 JAR, SHA-256, SHA-512, SBOM, source manifest, signed integration commit, signed annotated tag, and verified attestations identify one release candidate on `master`.
- All earlier mandatory phase exit criteria pass, no known mandatory phase-owned defect remains, and all required checks on the merged revision are successful.
- The release notes, platform metadata, supported Minecraft and NeoForge versions, release channel, dependencies, and final JAR name are derived from the verified artifact and repository evidence.
- A read-only CurseForge and Modrinth preview exists and exactly matches the Phase 005 artifact identity before EXT-003 confirmation is requested.
- EXT-003 confirmation is owner approved through the configured broker and binds artifact identities, operations, operators, rollback, runbook digest, systems, and time window to that exact preview. A confirmation value is treated as sensitive operational input and is not recorded in the repository, plan, issue, release notes, or logs.

**Implementation scope**

- CORE-REQ-010 consumes the Phase 005 release candidate without rebuilding or changing it, generates and validates a no-mutation preview for both platforms, requests the narrowly scoped owner confirmation, publishes through the configured broker, verifies hosted metadata and downloaded hashes, records release and monitoring evidence, closes the six baseline issues, and performs the plan-wide completion audit.
- CORE-REQ-010 treats any identity change after preview generation as cancellation of the preview and EXT-003 confirmation. The changed candidate must return through CORE-PHASE-005 validation and receive a new preview and newly scoped confirmation.
- CORE-REQ-010 preserves the verified version `3.0.4`, supported Minecraft `1.21.1`, NeoForge platform identity, dependencies, release channel, and release notes proven by the completion packet.

**Execution order**

1. `P006-TASK-001` executes CORE-REQ-010 by accepting the Phase 005 completion packet, independently reconciling every release identity, and freezing the only candidate eligible for publication.
2. `P006-TASK-002` executes CORE-REQ-010 by finalizing documentation, release notes, platform metadata, dependency declarations, verification instructions, and the rollback or unpublish runbook for that frozen candidate.
3. `P006-TASK-003` executes CORE-REQ-010 by generating a read-only CurseForge and Modrinth broker preview and comparing every preview field with the frozen identity and documentation.
4. `P006-TASK-004` executes CORE-REQ-010 and EXT-003 by obtaining owner confirmation bound to the exact artifact, operations, operator, systems, time window, rollback policy, and runbook digest. Work stops before publication until this evidence exists.
5. `P006-TASK-005` executes CORE-REQ-010 by submitting the approved immutable artifact to CurseForge and Modrinth through the broker and capturing the resulting platform records without rebuilding or substituting files.
6. `P006-TASK-006` executes CORE-REQ-010 by downloading both hosted files, recomputing SHA-256 and SHA-512, checking platform metadata and availability, and running immediate post-release monitoring for publication, download, startup, and baseline-regression signals.
7. `P006-TASK-007` executes CORE-REQ-010 by closing issues `#8`, `#10`, `#11`, `#16`, `#24`, and `#25` only with merged-revision acceptance evidence, including CORE-REQ-011 acceptance proof for issue `#25`, then auditing every plan-wide Definition of Done item and recording final completion.

**Required evidence**

- The Phase 005 completion packet and a release-identity comparison proving one source commit, signed integration commit, signed annotated tag, JAR, SHA-256, SHA-512, SBOM, source manifest, and attestation set.
- A read-only broker preview for CurseForge and Modrinth whose version, artifact hashes, supported platform, dependencies, release channel, metadata, and release notes match the frozen candidate.
- EXT-003 evidence proving the owner confirmation is bound to the required scope fields without exposing the confirmation value.
- CurseForge and Modrinth release URLs, platform file identifiers, hosted metadata captures, downloaded files, and recomputed SHA-256 and SHA-512 values.
- Immediate post-release monitoring results covering platform processing, download availability, startup compatibility, and new reports related to the six baseline regressions.
- Six issue closure records, each naming the merged revision, applicable runtime or artifact acceptance evidence, released version `3.0.4`, and platform availability. The issue `#25` record names CORE-REQ-011 and links its canonical serialization, persisted-file digest, editor reopen, compile, reload, runtime lock, rule-preservation, and rollback evidence.
- A final audit showing every CORE-REQ-001 through CORE-REQ-011 gate, every mandatory phase completion packet, and the owner-selected completion endpoint are satisfied.

**Exit criteria**

- The exact signed and verified 3.0.4 artifact from Phase 005 is published and downloadable from both CurseForge and Modrinth, and each downloaded file matches its approved SHA-256 and SHA-512.
- Hosted version, platform, release channel, dependencies, metadata, and release notes match the approved preview on both platforms.
- Immediate post-release monitoring contains no unresolved publication, download, startup, or baseline-regression failure. Any discovered mandatory defect invalidates completion and routes recovery through the owning phase.
- Issues `#8`, `#10`, `#11`, `#16`, `#24`, and `#25` are closed only after their merged-revision acceptance evidence and verified 3.0.4 availability are recorded. Issue `#25` remains open unless its CORE-REQ-011 visual-form-to-runtime and preservation evidence passes against the released artifact.
- The final phase verifies the owner-selected completion endpoint and plan-wide Definition of Done.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Phase 005 completion packet | CORE-PHASE-005 | One 3.0.4 candidate on `master` with successful required checks, signed integration commit, signed annotated tag, checksums, SBOM, source manifest, and verified attestations | Recompute hashes, verify signatures and attestations, inspect the JAR, and compare all recorded identities | Stop. Reject the candidate and return the discrepancy to CORE-PHASE-005 |
| Release metadata and documentation | CORE-PHASE-005 | Version, supported platform, dependencies, release channel, and behavior claims describe only the verified artifact | Compare metadata and release notes with the JAR, master documentation, and completion packets | Stop. Correct documentation or metadata, then regenerate the preview without changing the artifact |
| Baseline acceptance packets | CORE-PHASE-001, CORE-PHASE-002, CORE-PHASE-003, CORE-PHASE-004 | Each of the six issues has passing merged-revision evidence at its required fidelity, including CORE-REQ-011 evidence for issue `#25` | Trace each issue to its requirement, test evidence, final JAR, and merged revision; for `#25`, also trace canonical `[recipes].locked_items` serialization through persistence, reload, compile, runtime enforcement, preservation, and rollback | Do not close the affected issue or complete the release plan. Route the evidence gap to its owning phase |
| Owner-approved publication confirmation | EXT-003 | Authorization binds artifact identities, operations, operators, rollback, runbook digest, systems, and time window to the exact preview | Broker validates scope and freshness before permitting mutation | Stop without publication. Request a new scoped confirmation only after a valid preview exists |
| Configured release broker | ReleaseBroker | Broker can preview both targets without mutation and publish only after accepted EXT-003 confirmation | Compare preview and response records with the frozen manifest | Stop on broker, authentication, permission, mapping, or platform error. Do not substitute manual credentials or bypass controls |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| CurseForge 3.0.4 release | Players and operators | Hosted file and metadata match the approved artifact and preview | Minecraft 1.21.1 and NeoForge compatibility remain as proven in Phase 005 | Platform URL, file identifier, metadata capture, downloaded hashes |
| Modrinth 3.0.4 release | Players and operators | Hosted file and metadata match the approved artifact and preview | Minecraft 1.21.1 and NeoForge compatibility remain as proven in Phase 005 | Platform URL, version identifier, metadata capture, downloaded hashes |
| Release documentation | Players, pack authors, and support | Release notes and project documentation describe verified 3.0.4 behavior and recovery guidance | No unverified feature claim or future scope is advertised | Documentation diff, final release notes, link and artifact checks |
| Issue closure packet | Maintainers and reporters | Each baseline issue closes with merged-revision, acceptance, artifact, and platform evidence; issue `#25` also carries CORE-REQ-011 acceptance evidence | Closure applies to 3.0.4 and does not generalize beyond the tested compatibility boundary | Six closure records and their evidence links |
| Plan completion packet | Maintainers | Every mandatory gate and exact completion endpoint is satisfied | The packet identifies the immutable 3.0.4 release and excludes future scope | Cross-phase audit, publication records, monitoring evidence, final verdict |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P006-TASK-001 | CORE-REQ-010 | Accept and freeze the Phase 005 release candidate without rebuilding it | CORE-PHASE-005 completion packet | Frozen release identity ledger | Signed commit, signed tag, JAR, checksum, SBOM, source manifest, attestation interfaces | Independent signature, hash, attestation, metadata, and JAR reconciliation |
| P006-TASK-002 | CORE-REQ-010 | Finalize release notes, platform metadata, dependency declarations, documentation, verification steps, and rollback guidance | P006-TASK-001 and prior phase completion packets | Release-ready documentation and broker manifest | README.md, DOCUMENTATION.md, documentation index, release notes, broker runbook | Documentation link check, artifact claim trace, dependency and version comparison |
| P006-TASK-003 | CORE-REQ-010 | Generate read-only platform previews and compare them with the frozen candidate | P006-TASK-001, P006-TASK-002 | Validated CurseForge and Modrinth preview | Configured release broker preview interface | Field-by-field preview diff with zero unexplained differences and no platform mutation |
| P006-TASK-004 | CORE-REQ-010, EXT-003 | Obtain narrowly scoped owner confirmation for the exact preview | P006-TASK-003 and EXT-003 | Broker-accepted authorization envelope without stored confirmation secret | Release broker authorization boundary | Scope, operator, system, time, runbook digest, rollback, operation, and artifact identity validation |
| P006-TASK-005 | CORE-REQ-010 | Publish the approved artifact to both platforms without substitution | P006-TASK-004 | CurseForge and Modrinth release records | Release broker, CurseForge, Modrinth | Broker receipts and platform records match the preview and frozen hashes |
| P006-TASK-006 | CORE-REQ-010 | Verify hosted files and metadata, then monitor immediate release health | P006-TASK-005 | Download hash evidence, availability evidence, and monitoring record | Platform download endpoints, supported startup environment, issue intake | Independent downloads, SHA-256 and SHA-512 comparison, metadata checks, startup smoke, regression-signal review |
| P006-TASK-007 | CORE-REQ-010 | Close the six issues and audit plan-wide completion | P006-TASK-006 and all mandatory phase packets, including CORE-REQ-011 acceptance evidence | Issue closure records and final completion packet | Issue tracker and plan completion evidence | Per-issue evidence trace, explicit issue `#25` to CORE-REQ-011 trace, and exact endpoint audit |

Tasks are strictly ordered because each task narrows or consumes the artifact identity established by its predecessor. Documentation preparation may collect existing verified evidence while P006-TASK-001 runs, but no final metadata, preview, confirmation, publication, platform verification, closure, or completion claim may advance ahead of its dependency. A failure cancels only downstream work; already verified upstream evidence remains reusable if the artifact identity and authoritative contracts have not changed. Any artifact or tag change invalidates every release-specific output from P006-TASK-001 onward.

## Architecture and Implementation Boundaries

Phase 006 is an operational release boundary, not a gameplay implementation phase. The signed Phase 005 artifact is immutable input. The release broker owns platform mutation; CurseForge and Modrinth receive the same verified JAR and platform-appropriate metadata derived from one release manifest. Repository documentation and issue records consume release evidence but cannot authorize publication.

The owner confirmation is an explicit trust boundary. The broker must compare the confirmation scope with the exact preview immediately before mutation. It must reject a different checksum, source commit, tag, version, target, operation, operator, time window, runbook digest, or rollback policy. Credentials and confirmation values remain outside source, Git history, plan artifacts, issue comments, release notes, screenshots, and routine logs.

There is no persistent gameplay state, networking, configuration migration, or runtime cache owned by this phase. Compatibility is inherited only from the verified Phase 005 candidate. Publication does not rebuild, re-sign, repackage, rename internally, or alter the JAR. Platform-specific identifiers are recorded as release evidence and never treated as a substitute for cryptographic artifact identity.

Issue closure is evidence-gated. Each issue requires its own report-specific proof: Curios behavior for `#8`, independent JEI and EMI operation for `#10`, Easy Builder enchantment controls for `#11`, progression category overlay depth and input behavior for `#16`, the approved correctness and performance gate for `#24`, and the CORE-REQ-011 canonical recipe-lock round trip for `#25`. The `#25` proof must show `[recipes].locked_items` is serialized, persisted, reopened, compiled, reloaded, and enforced without deleting an existing valid rule when validation or reload fails. A general build success or published file alone is insufficient.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Phase 005 identity mismatch | Signature, checksum, manifest, tag, attestation, or JAR comparison differs | Stop before preview and reject the candidate | Return to CORE-PHASE-005, produce a fully revalidated candidate, then restart Phase 006 | Repeated P006-TASK-001 reconciliation with one identity |
| Preview differs from release manifest | Version, dependency, channel, notes, target, or hash comparison fails | Do not request confirmation or publish | Correct metadata or mapping, regenerate both previews, and rerun the complete comparison | Zero-difference preview evidence |
| EXT-003 missing, stale, or too broad | Broker scope or freshness validation fails | Remain externally blocked and perform no mutation | Generate a current preview and request a new narrowly scoped owner confirmation | Broker rejection record and later accepted scope validation without exposing secret input |
| One platform publishes and the other rejects | Broker or platform returns partial success | Record exact platform state, do not claim completion or close issues | Follow the EXT-003-bound retry or rollback policy. Do not replace the artifact or expand authority | Both platform records and downloads eventually match one approved identity, or rollback evidence proves neither is advertised as complete |
| Hosted download hash differs | Recomputed SHA-256 or SHA-512 differs from the approved value | Treat the affected release as invalid and stop completion | Invoke the bound unpublish or rollback path, investigate platform or upload corruption, and rerun Phase 005 and Phase 006 as required | Fresh independent download matches both approved hashes |
| Hosted metadata differs but file hash matches | Platform version, loader, dependencies, channel, or notes inspection fails | Do not claim verified publication | Correct only metadata if permitted by the bound operation, otherwise request newly scoped confirmation | Repeated platform metadata comparison |
| Platform processing remains incomplete | File is unavailable, quarantined, rejected, or not publicly downloadable | Keep the release incomplete and monitor platform state without closing issues | Resolve platform feedback through the broker or apply the bound rollback policy | Public availability and independent download proof |
| Immediate startup or baseline regression appears | Supported startup smoke or monitoring identifies a mandatory failure | Stop issue closure and revoke completion claim | Route the defect to its owning phase and use the bound rollback or unpublish policy when required | Corrected candidate repeats all affected phase and release evidence |
| One issue lacks report-specific acceptance evidence | Closure checklist cannot trace merged behavior and final artifact proof | Leave that issue open and keep the plan incomplete | Complete the missing acceptance workflow at the owning phase boundary | Issue-specific acceptance packet linked to the merged revision and 3.0.4 release |
| Publication request is retried | Broker shows an earlier receipt or platform record | Use artifact and operation identifiers to prevent unintended duplicate releases | Reconcile existing platform state before any retry and require scope-valid broker behavior | Exactly one intended 3.0.4 release record per target project |

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Evidence artifact |
|---|---|---|---|---|---|
| P006-TASK-001 | Manifest field and checksum comparison | Signed tag, commit, JAR, SBOM, source manifest, and attestation reconciliation | Inspect the actual Phase 005 release candidate | Altered identity must fail the freeze gate | Release identity ledger and comparison results |
| P006-TASK-002 | Documentation link, version, dependency, and claim checks | Release notes and broker manifest derive from the same candidate | Render final release descriptions as operators and players receive them | Unsupported claim or stale version blocks preview | Final documentation and release metadata packet |
| P006-TASK-003 | Deterministic preview comparison | Both platform previews consume one manifest | Execute configured broker preview without mutation | Any mismatch cancels confirmation eligibility | CurseForge and Modrinth preview records |
| P006-TASK-004 | Authorization scope validation | Broker binds confirmation to preview and operator | Owner provides required confirmation only after identity match | Missing, stale, reused, or overbroad confirmation is rejected | Sanitized EXT-003 authorization result and scope digest |
| P006-TASK-005 | Receipt-to-manifest comparison | Broker submits the same JAR to both targets | Perform the approved platform publication | Partial failure follows bound retry or rollback and cannot complete the phase | Broker receipts and platform identifiers |
| P006-TASK-006 | Download checksum calculation and metadata comparison | Cross-platform identity comparison | Download both hosted files, run supported startup smoke, and inspect release health signals | Hash, metadata, availability, or runtime mismatch triggers rollback and reverification | Downloads, SHA-256, SHA-512, metadata captures, monitoring record |
| P006-TASK-007 | Per-issue evidence checklist and plan gate audit | Link each issue to merged revision, owning requirement, final JAR, and platform release; link issue `#25` to CORE-REQ-011 evidence | Close exactly six baseline issues after verification | Missing evidence leaves the issue and plan open | Issue closure packet and plan completion audit |
| CORE-REQ-010 | Endpoint consistency check | All mandatory phase packets and release records reconcile | Verify the signed 3.0.4 release on CurseForge and Modrinth | Any failed Definition of Done item keeps status incomplete | Final Phase 006 completion packet |

Fixtures are the immutable Phase 005 candidate, its signed tag and integration commit, checksums, SBOM, source manifest, attestations, the two no-mutation platform previews, downloaded platform files, the supported 1.21.1 NeoForge startup environment, and the six issue-specific acceptance packets. The issue `#25` fixture includes the production editor bundle, canonical TOML output, preexisting valid-rule snapshot, persisted file digests, editor reopen result, compiled rule state, reload result, and eligible and ineligible player runtime outcomes. Expected results require exact identity and metadata equality, successful public downloads, successful supported startup, no new baseline-regression signal, and evidence-complete closure. Failures are interpreted as release blockers, not warnings. Rerun order follows P006-TASK-001 through P006-TASK-007, beginning at the earliest invalidated artifact; any artifact change restarts the entire phase.

## Documentation, Operations, and Release

- Finalize `README.md`, `DOCUMENTATION.md`, and the documentation index only with behavior verified in the 3.0.4 JAR. Keep installation, compatibility, configuration, optional integration, editor, UI, performance, troubleshooting, and rollback guidance consistent with prior phase evidence.
- Produce 3.0.4 release notes that summarize the six corrected reports, including the Easy Builder recipe-lock serialization and preservation correction for issue `#25`, and any CORE-REQ-007 advertised-capability corrections proven by the audit. Do not advertise excluded future work.
- Validate CurseForge and Modrinth version, Minecraft 1.21.1 compatibility, NeoForge loader declaration, dependencies, release channel, title, changelog, and artifact selection in the read-only preview.
- Record the release runbook digest and rollback or unpublish policy before requesting EXT-003 confirmation. The operational confirmation value remains outside tracked and public artifacts.
- Publish only through the configured broker after the owner confirms the exact preview. Record sanitized receipts, platform identifiers, public URLs, hosted metadata, and independent download hashes.
- Monitor platform processing, download availability, supported startup, and incoming reports related to the baseline fixes from publication until the Phase 006 completion packet is accepted. A mandatory regression blocks completion and invokes the bound recovery policy.
- Close issues `#8`, `#10`, `#11`, `#16`, `#24`, and `#25` with concise evidence linking the merged revision, applicable verification, 3.0.4 release, and platform availability. The issue `#25` closure also links CORE-REQ-011 acceptance evidence. No closure rests only on a source change or release note.
- Preserve checksums, SBOM, source manifest, attestation proof, signed commit and tag proof, platform downloads, issue evidence, monitoring output, and final audit as the release evidence set.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| Candidate changes after confirmation | Freeze cryptographic and source identities before preview and bind EXT-003 to them | Prepublication identity comparison | Cancel confirmation, return changed candidate through Phase 005, and request a new confirmation | Preview, authorization, receipts, and downstream checks | Entire Phase 006 sequence |
| Platform mapping targets the wrong project or channel | Compare project, version, loader, channel, and artifact in both previews | Preview-to-runbook diff | Correct mapping before confirmation | Platform preview and authorization | P006-TASK-003 onward |
| Broker or platform duplicates publication | Use immutable artifact and operation identifiers and reconcile current state before retry | Duplicate version or file record | Stop retry and resolve or remove duplicate under bound policy | Publication and platform availability proof | P006-TASK-005 onward |
| Partial dual-platform release | Require both targets for completion | One successful receipt with one failure | Follow bound retry or rollback without changing artifact | Cross-platform publication and completion proof | P006-TASK-005 through P006-TASK-007 |
| Download differs from submitted artifact | Verify both hashes after public processing | SHA-256 or SHA-512 mismatch | Unpublish or rollback under EXT-003 policy and investigate before a new release | All affected platform and completion evidence | Phase 005 identity proof if artifact changed, otherwise P006-TASK-005 onward |
| Release documentation drifts from artifact | Trace every claim to prior-phase proof and final JAR | Claim audit or user-visible mismatch | Correct documents and metadata, then revalidate preview or platform record | Documentation, preview, authorization when metadata scope changes | P006-TASK-002 onward |
| Premature issue closure | Require issue-specific merged and released evidence checklist | Missing evidence link or unverified platform availability | Reopen the issue and complete its acceptance packet | Issue closure and plan completion proof | P006-TASK-007 |
| Post-release regression is missed | Monitor platform, startup, and baseline issue signals until packet acceptance | New reproducible mandatory failure | Keep plan incomplete, apply bound rollback, and route to owning phase | Affected issue closure, release health, and completion proof | Affected phase through Phase 006 |
| Sensitive confirmation enters logs or repository | Pass confirmation only through broker input and store sanitized scope result | Secret scan or log review | Revoke exposed value where applicable, remove it from operational output without rewriting protected history, and obtain new authorization | Authorization and security evidence | P006-TASK-004 onward plus secret checks |

## Phase Completion Packet

The phase may close only when its external evidence packet contains all of the following:

- The Phase 005 merged revision and completion packet, including verified signed commit, signed annotated tag, final 3.0.4 JAR identity, SHA-256, SHA-512, SBOM, source manifest, JAR inventory, and attestation validation.
- Final `README.md`, `DOCUMENTATION.md`, documentation index, release notes, platform metadata, dependency declarations, verification instructions, and rollback or unpublish runbook digest.
- Read-only CurseForge and Modrinth previews with a field-by-field identity comparison.
- Sanitized proof that EXT-003 was owner approved for the exact artifact identities, operations, operator, rollback, runbook digest, systems, and time window. The confirmation value itself is excluded.
- Broker publication receipts, CurseForge and Modrinth public URLs and identifiers, public metadata captures, independently downloaded files, and matching SHA-256 and SHA-512 values.
- Immediate post-release monitoring and supported startup evidence with no unresolved mandatory regression.
- Separate closure evidence for issues `#8`, `#10`, `#11`, `#16`, `#24`, and `#25`, each tied to the merged revision, final artifact, report-specific acceptance proof, and verified platform release. Issue `#25` must be tied explicitly to CORE-REQ-011 and its canonical recipe-lock serialization, preservation, reload, compile, and runtime enforcement evidence.
- A plan-wide audit proving every mandatory phase packet is complete, every CORE-REQ-001 through CORE-REQ-011 acceptance gate passes, the exact completion endpoint is achieved, excluded scope remains excluded, and no known mandatory defect remains.

The completion packet records execution evidence outside the protected plan set. It does not alter the master plan or this phase blueprint as a progress diary.

## Next Transition

After CORE-PHASE-006 passes, perform the plan-wide completion audit against the exact endpoint: a signed and verified 3.0.4 patch is merged into `master`, published to CurseForge and Modrinth, and all six baseline GitHub issues are closed with merged-revision acceptance evidence. Confirm that issue `#25` carries CORE-REQ-011 acceptance evidence, all cross-phase evidence identifies the same artifact, every required external prerequisite is satisfied, no mandatory defect remains, and future scope stays excluded. Only then record final completion. If any gate fails, keep the plan incomplete and return to the earliest owning phase or the EXT-003 authorization boundary; there is no later phase and no automatic publication authority.

# Phase 004 Execution Plan

> **Plan ID:** PLAN-PHASE-004
> **Phase ID:** CORE-PHASE-004
> **Owner:** CompatibilityHarness
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 004 of 006

## Purpose and Ownership

This phase proves that the integrated outputs of CORE-PHASE-001, CORE-PHASE-002, and CORE-PHASE-003 preserve ProgressiveStages compatibility and regression safety before release integration begins. Its sole canonical requirement is CORE-REQ-008. The phase owns the compatibility matrix, security and authority checks, runtime exercises, artifact inspection, failure routing, recovery rehearsal, and completion evidence for the supported ProgressiveStages 3.0.4 patch boundary.

The master plan owns product scope, supported behavior, architecture, phase order, and the release endpoint. This blueprint owns only execution of CORE-PHASE-004. It does not reopen the individual fixes from earlier phases. Any failed check that points to an earlier phase-owned defect blocks this phase and routes a corrective change back to the owning component before the affected verification is rerun.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| VERIFIED | Platform boundary | The release target is Minecraft 1.21.1, NeoForge 21.1.219, and Java 21 | Master plan project identity and compatibility contract | Invalidated by any platform, loader, mapping, Java, or build-tool version change |
| VERIFIED | Compatibility contract | Existing supported stage packs, configuration, saved stage data, commands, APIs, editor authority, and multiplayer behavior must remain compatible | CORE-REQ-008 in the master plan | Invalidated by a schema, identifier, persistence, command, API, or protocol change |
| VERIFIED | Upstream topology | CORE-PHASE-001, CORE-PHASE-002, and CORE-PHASE-003 must integrate before this phase starts | Master plan phased roadmap | Invalidated if any upstream phase is unmerged, lacks its completion packet, or receives a corrective change |
| VERIFIED | Optional dependencies | Curios, JEI, and EMI remain optional and require present, absent, and combined-installation coverage | CORE-REQ-003, CORE-REQ-004, and EXT-001 | Invalidated by an optional integration revision or resolved-artifact identity change |
| VERIFIED | Authority boundary | Server mutations require operator authorization and schema validation; the logical server remains authoritative | Master plan architecture boundary and CORE-REQ-008 | Invalidated by packet, command, editor apply, or permission-path changes |
| PROPOSED | Release candidate | A single merged-revision candidate will be used for every check in this phase | This blueprint | Invalidated by any source, resource, generated data, dependency, or build metadata change after evidence capture |

## Scope Boundaries

### Included Scope

- CORE-REQ-008 — prove that supported configuration, stage schema, saved data, commands, public APIs, editor and client authority boundaries, integrated-server operation, dedicated-server operation, reconnect, reload, multiplayer ownership, optional integrations, client behavior, and final JAR contents remain compatible and secure on Minecraft 1.21.1, NeoForge 21.1.219, and Java 21.
- Produce one traceable phase completion packet tied to a single source revision and artifact digest.
- Route regressions to the component and phase that owns the failed contract, then repeat all invalidated evidence after correction.

### Explicit Exclusions

- CORE-REQ-002 through CORE-REQ-007 implementation is owned by earlier phases. This phase verifies their integrated result but does not repeat or independently redesign those fixes.
- CORE-REQ-009 release attestation repair belongs to CORE-PHASE-005.
- CORE-REQ-010 publication and issue closure belong to CORE-PHASE-006.
- FUT-001 through FUT-004 remain excluded. This phase performs no legacy-roadmap feature work, routine dependency update, post-baseline feature request, or Minecraft, NeoForge, Java, Gradle, mapping upgrade.
- NG-001 through NG-004 remain enforced. No progression redesign, hard optional-mod dependency, hot-path full scan, or evidence-free issue closure is permitted.
- Performance threshold acceptance for entity presence is owned by CORE-PHASE-001. This phase checks that its proof remains valid for the integrated candidate and reruns it only when integration invalidates that evidence.

## Phase Contract

### CORE-PHASE-004 — Prove compatibility and regression safety

**Objective:** Produce a complete, revision-bound compatibility and security evidence packet showing that the integrated 3.0.4 candidate preserves CORE-REQ-008 across Minecraft 1.21.1, NeoForge 21.1.219, Java 21, supported server modes, multiplayer lifecycles, optional dependency combinations, and packaged artifacts
**Owner:** CompatibilityHarness
**Dependencies:** CORE-PHASE-001, CORE-PHASE-002, CORE-PHASE-003
**Canonical requirements:** CORE-REQ-008
**Documentation and release impact:** Update `README.md`, `DOCUMENTATION.md`, the documentation index, affected compatibility and testing references, and the 3.0.4 verification record only with behavior proven by this phase; provide the release-candidate evidence consumed by CORE-PHASE-005
**Next transition:** CORE-PHASE-005

**Entry criteria**

- CORE-PHASE-001, CORE-PHASE-002, and CORE-PHASE-003 are merged sequentially and each has a complete, passing completion packet.
- The candidate revision, target Minecraft 1.21.1, NeoForge 21.1.219, Java 21 runtime, Gradle wrapper identity, resolved optional integration artifacts, and relevant configuration are recorded before tests begin.
- EXT-001 evidence identifies the exact Curios, JEI, and EMI artifacts used in the optional-integration matrix.
- Compatibility fixtures cover a supported existing configuration, a supported existing stage definition, persisted player stage ownership, command and API operations, operator and non-operator editor requests, and multiplayer stage-state differences without containing credentials or private player data.
- No uncommitted or unrelated files can enter the release-candidate build or evidence set.

**Implementation scope**

- CORE-REQ-008 owns the compatibility matrix, lifecycle exercises, optional-mod combinations, packet and editor authority tests, static and security checks, build and runtime checks, final JAR inspection, recovery rehearsal, and evidence invalidation rules for the integrated candidate.
- CORE-REQ-008 defines compatibility as loadable existing data and configuration plus behaviorally equivalent public contracts. It does not authorize silent migration, schema widening, identifier renaming, protocol drift, or permissive error handling.
- CORE-REQ-008 requires every verification result to identify the source revision, environment, fixture, command or procedure, expected result, observed result, and evidence artifact.

**Execution order**

1. `P004-TASK-001` freezes the CORE-REQ-008 integrated candidate identity, environment manifest, test fixtures, expected contract matrix, and upstream evidence dependencies.
2. `P004-TASK-002` validates CORE-REQ-008 configuration, stage schema, persisted stage data, commands, and public API compatibility without rewriting accepted legacy input.
3. `P004-TASK-003` exercises CORE-REQ-008 integrated-server startup, world load, grant and revoke transitions, reload, dimension transition, disconnect, reconnect, world close, and reopen lifecycles.
4. `P004-TASK-004` exercises CORE-REQ-008 dedicated-server startup and mixed-ownership multiplayer behavior, including reconnect and client synchronization under authoritative server state.
5. `P004-TASK-005` runs the Curios, JEI, and EMI present, absent, disabled, and concurrent-installation matrix using the exact EXT-001 artifacts.
6. `P004-TASK-006` verifies CORE-REQ-008 operator authority, malformed input rejection, packet validation, client-server boundary safety, optional classloading isolation, and sanitized diagnostics.
7. `P004-TASK-007` runs CORE-REQ-008 formatting, static analysis, unit tests, applicable GameTests, `./gradlew build`, and dependency and resource validation in the repository-defined order.
8. `P004-TASK-008` inspects the CORE-REQ-008 final JAR, executes client and server smoke workflows from that JAR, confirms artifact and documentation parity, and rehearses rollback to the last accepted candidate.
9. `P004-TASK-009` triages failures, reruns invalidated checks, assembles the phase completion packet, and hands the immutable candidate identity to CORE-PHASE-005.

**Required evidence**

- A compatibility matrix tied to one source revision and one JAR digest, with explicit rows for configuration, stage schema, saved player data, commands, APIs, editor authority, packets, integrated server, dedicated server, reconnect, reload, multiplayer, and optional integrations.
- Formatter, static analysis, unit test, applicable GameTest, Gradle build, resource validation, dependency review, and security review results with exact commands or repository-defined procedures.
- Integrated-server and dedicated-server runtime logs showing successful startup, world load, lifecycle transitions, clean shutdown, and absence of client-only classloading on the dedicated server.
- Multiplayer evidence showing server-authoritative state across at least two concurrently connected players with different applicable stage ownership, followed by disconnect and reconnect synchronization.
- EXT-001 identity evidence and runtime results for Curios, JEI, and EMI individually present, jointly present where applicable, individually disabled, and absent.
- Negative tests showing non-operator mutation denial, malformed draft or payload rejection, unsupported data failure without destructive rewrite, and optional dependency absence without core startup failure.
- A final JAR inventory, documentation parity record, recovery rehearsal, and invalidation ledger listing every test rerun after the candidate changed.

**Exit criteria**

- Every CORE-REQ-008 acceptance criterion passes against the same candidate revision and JAR digest on Minecraft 1.21.1, NeoForge 21.1.219, and Java 21.
- Integrated server, dedicated server, reconnect, reload, multiplayer ownership, client synchronization, and optional integration matrices pass with no unresolved compatibility or security finding.
- Formatter, static analysis, unit tests, applicable GameTests, `./gradlew build`, client smoke, dedicated-server smoke, multiplayer verification, and final JAR inspection pass at the required fidelity.
- Any candidate change made after a failed check has invalidated and rerun all affected downstream evidence.
- The phase completion packet records a recovery path and gives CORE-PHASE-005 an exact, immutable source revision and artifact identity.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Entity presence phase output | CORE-PHASE-001 | Integrated implementation and correctness and performance evidence identify the same upstream revision | Verify phase completion packet and candidate ancestry | Stop; route the regression to CORE-PHASE-001 and invalidate dependent runtime evidence |
| Optional integration phase output | CORE-PHASE-002 | Curios, JEI, and EMI behavior and EXT-001 artifact identities are complete and merged | Verify phase packet, artifact hashes, and optional-mod matrix definition | Stop; route the gap to CORE-PHASE-002 and do not substitute another dependency version silently |
| Editor, UI, and artifact parity output | CORE-PHASE-003 | Production editor bundle, client UI, and advertised-capability corrections are merged and packaged | Verify completion packet and packaged-resource identity | Stop; route the gap to CORE-PHASE-003 and invalidate client and JAR evidence |
| Compatibility contract | CORE-REQ-008 | Existing supported configuration, schema, saved data, commands, APIs, and multiplayer behavior remain supported | Load fixtures and compare normalized and observable behavior | Stop on destructive migration, silent data loss, or contract drift |
| Target environment | Master plan | Minecraft 1.21.1, NeoForge 21.1.219, and Java 21 are pinned | Record runtime and build environment versions | Stop if any boundary differs; no platform upgrade is permitted in this plan |
| Optional artifacts | EXT-001 | Exact versions, authoritative source, compatibility, license provenance, security review, SHA-256, and SHA-512 are recorded | Verify the complete EXT-001 artifact-evidence set before runtime use | Stop only the affected optional-integration rows until evidence is complete; core absent-mod tests may continue |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Frozen compatible candidate | CORE-PHASE-005 | One source revision and JAR digest passed the full CORE-REQ-008 matrix | 3.0.4 patch on Minecraft 1.21.1, NeoForge 21.1.219, Java 21; no platform or schema upgrade | Candidate manifest and JAR digest |
| Compatibility evidence packet | CORE-PHASE-005 | All required static, runtime, multiplayer, optional-mod, security, and recovery checks are traceable and passing | Evidence is invalid if the candidate or environment changes | Phase completion packet and invalidation ledger |
| Configuration and data compatibility result | Operators and CORE-PHASE-005 | Supported existing configuration, stage definitions, and saved stage ownership load without silent loss or rewrite | Existing identifiers, keys, schema, and persistence remain stable | Fixture inputs, observed outputs, and before-and-after data comparison |
| Authority and security result | CORE-PHASE-005 | Server mutation paths enforce permission and validation boundaries | Network and editor contracts remain server authoritative | Negative authorization and malformed-input results |
| Runtime compatibility result | CORE-PHASE-005 | Integrated server, dedicated server, reconnect, reload, and multiplayer ownership paths pass | Client-only code remains isolated from dedicated-server startup | Sanitized runtime logs and scenario records |
| Optional integration result | CORE-PHASE-005 | Core startup works with optional mods absent and supported combinations behave according to configuration | Curios, JEI, and EMI remain optional adapters | EXT-001 matrix and classloading evidence |
| Recovery contract | CORE-PHASE-005 | Candidate replacement and evidence invalidation have a deterministic procedure | Any replacement requires a new revision and digest plus affected reruns | Recovery rehearsal and rerun map |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P004-TASK-001 | CORE-REQ-008 | Freeze the candidate, environment, fixtures, expected behavior, upstream packet references, and evidence naming | CORE-PHASE-001, CORE-PHASE-002, CORE-PHASE-003, master contract | Candidate manifest and test matrix | Build environment, compatibility fixtures, evidence packet | Independent identity comparison of revision, dependency set, and JAR digest |
| P004-TASK-002 | CORE-REQ-008 | Load and exercise supported existing configuration, stage schema, saved stage ownership, commands, and public API calls | P004-TASK-001 and existing supported fixtures | Compatibility results and before-and-after data comparisons | ConfigurationSchema, persistence, command surface, public APIs | Normalized behavior assertions, no-loss checks, and unsupported-input negative cases |
| P004-TASK-003 | CORE-REQ-008 | Exercise full integrated-server lifecycle and synchronization | P004-TASK-001, P004-TASK-002 | Integrated-server scenario record | Logical server lifecycle, synchronized client state, reload and reconnect boundaries | Startup, transitions, reload, disconnect, reconnect, close, reopen, and clean-shutdown evidence |
| P004-TASK-004 | CORE-REQ-008 | Exercise dedicated-server and mixed-ownership multiplayer lifecycle | P004-TASK-001, P004-TASK-002 | Dedicated multiplayer scenario record | Dedicated server, networking, authoritative stage state | Two-player mixed-state behavior, reconnect synchronization, clean startup and shutdown logs |
| P004-TASK-005 | CORE-REQ-008 | Execute the supported optional-integration presence and configuration matrix | P004-TASK-001, EXT-001, CORE-PHASE-002 | Optional integration matrix | CuriosBridge, RecipeViewerBridge, JEI adapter, EMI adapter | Present, absent, disabled, and concurrent supported combinations with classloading checks |
| P004-TASK-006 | CORE-REQ-008 | Review and test trust boundaries, validation, diagnostics, and optional isolation | P004-TASK-002 through P004-TASK-005 | Security and negative-test record | Editor apply, client payloads, permissions, schema validation, logging | Non-operator denial, malformed-input rejection, bounds checks, absent-mod startup, sanitized logs |
| P004-TASK-007 | CORE-REQ-008 | Run the repository verification pipeline at the pinned Java and platform versions | P004-TASK-001 through P004-TASK-006 | Deterministic check results and build artifact | Formatter, static analysis, tests, GameTests, Gradle, resources, dependencies | All applicable checks pass, including `./gradlew build` |
| P004-TASK-008 | CORE-REQ-008 | Inspect and smoke-test the packaged JAR, compare documentation, and rehearse candidate rollback | P004-TASK-007 | JAR inventory, smoke evidence, parity record, recovery result | Packaged metadata and resources, client, dedicated server, documentation | JAR identity check, runtime startup from artifact, documentation comparison, rollback rehearsal |
| P004-TASK-009 | CORE-REQ-008 | Resolve evidence invalidation, confirm no open mandatory finding, and assemble the downstream packet | P004-TASK-001 through P004-TASK-008 | Signed-off phase completion packet for CORE-PHASE-005 | Evidence ledger and release-candidate handoff | Traceability audit confirms every matrix row, artifact, and rerun belongs to one final candidate |

`P004-TASK-001` must complete before any evidence-producing task. `P004-TASK-002` establishes the data and API baseline required by the runtime scenarios. After those two tasks, integrated-server, dedicated-server, optional-integration, and trust-boundary exercises may proceed in parallel on isolated fixtures. `P004-TASK-007` starts only after their results pass. `P004-TASK-008` uses the exact artifact produced by `P004-TASK-007`, and `P004-TASK-009` closes the phase only after every invalidated result is rerun.

A task failure stops only work that depends on its evidence when isolation is safe. It never permits a partial candidate to advance. A corrective source or resource change creates a new candidate identity, routes ownership to the appropriate earlier phase component, and requires rerunning the changed component's checks plus every downstream row named by the invalidation ledger.

## Architecture and Implementation Boundaries

The logical server remains authoritative for stage membership, rule decisions, configuration reload, permissions, persistence, and enforcement. Client state is presentation or synchronized cache state only. Compatibility proof must reject any path where a client request mutates authoritative state without server-side operator authorization, payload validation, bounds validation, and execution on the correct logical thread.

This phase does not prescribe new implementation. It verifies that the integrated candidate preserves dependency direction: common and server code cannot load client classes, core startup cannot load absent optional integration classes, and optional adapters cannot own stage policy. An unavailable Curios, JEI, or EMI adapter may remove only that integration's behavior. It must not crash core startup, make a denied action permissive, alter saved state, or contaminate another installed adapter.

Compatibility fixtures must be copied before execution. Tests may mutate only disposable copies. Stage identifiers, `pack:stage` parsing, TOML fields, configuration keys, commands, API contracts, persisted stage ownership, and network behavior remain stable unless the master contract explicitly documents a compatible default. No test may repair a fixture in place and then claim backward compatibility.

Runtime evidence must distinguish integrated and dedicated servers. Dedicated-server startup proves client isolation. Multiplayer scenarios must use server-authoritative state for two concurrently connected players and cover disconnect, reconnect, synchronization, and clean lifecycle transitions. Concurrency checks must identify the server thread mutation boundary and detect stale state rather than masking it with arbitrary waits.

Security evidence must cover permission denial, malformed input, unsupported identifiers, size and bounds validation where applicable, path safety for editor-managed files, and sanitized diagnostics. It must not capture secrets, confirmation codes, private raw logs, or unrelated player data. Static review includes dependency provenance already required by EXT-001 and verifies that no hard optional dependency or unauthorized network mutation was introduced.

All evidence is revision-bound. A change to Java source, resources, generated assets, dependencies, Gradle metadata, mod metadata, editor bundle, or documentation that affects a claimed behavior invalidates the corresponding checks. The invalidation ledger defines the minimum rerun set; uncertainty requires rerunning the complete phase.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Existing configuration or stage file no longer loads | Parser or compiler failure against a supported fixture | Fail clearly without destructive rewrite or partial activation | Preserve fixture, route to owning component, correct candidate, rerun all data and runtime checks | Before-and-after fixture digest and successful reload |
| Persisted stage ownership changes or disappears | Saved-data comparison before and after load, reconnect, or restart | Keep authoritative ownership and unknown supported data intact | Restore fixture copy, correct migration or serialization boundary, rerun lifecycle matrix | Grant, revoke, restart, and reconnect assertions |
| Command or API behavior drifts | Contract fixture returns different authorization, result, or state | Preserve documented command, identifier, and public API behavior | Route correction to owning surface and rerun downstream synchronization | Command and API transcript with authoritative state comparison |
| Non-operator editor or packet mutation succeeds | Negative authorization test observes state change | Reject before mutation and provide a safe diagnostic | Remove mutated disposable world, correct trust boundary, rerun all security and affected multiplayer checks | Denied request and unchanged-state evidence |
| Malformed payload, draft, or configuration crashes a server | Controlled invalid fixture or payload causes exception or partial state | Reject atomically and keep prior authoritative state active | Restart disposable environment, restore fixture, correct validation, rerun startup and recovery | Rejection result, unchanged state, and clean subsequent valid apply |
| Client class loads on dedicated server | Startup linkage or classloading failure | Dedicated server must start without client classes | Route to owning component, correct side boundary, rerun build and dedicated-server smoke | Clean dedicated startup and world load log |
| Optional mod is absent or incompatible | Adapter resolution failure or startup error | Keep core startup safe, isolate the adapter, and emit concise sanitized diagnostics | Remove the unsupported combination from the supported matrix only if the master contract permits; otherwise correct adapter and rerun | Absent-mod startup and supported-artifact matrix |
| Curios, JEI, and EMI interfere when combined | Combined client or server result differs from independent decisions | Keep adapters independent and preserve core policy | Route to CORE-PHASE-002 ownership, replace candidate, rerun full optional matrix | Independent and combined matrix comparison |
| Reconnect or reload exposes stale state | Client and server state comparison after transition | Rebuild synchronized state from the authoritative server revision | Disconnect clients, restart disposable environment, correct invalidation or sync boundary | Reconnect, reload, and revision transition evidence |
| Mixed-ownership multiplayer behavior leaks between players | Two-player fixture observes shared allow or deny state | Keep decisions scoped to the applicable player while preserving shared world behavior | Stop server, restore fixture, correct player isolation, rerun multiplayer and performance proof if affected | Concurrent two-player observation and interaction record |
| Build passes but packaged JAR omits required resource | JAR inventory or runtime artifact workflow differs from source test | Block phase exit and release handoff | Correct packaging, rebuild with new digest, rerun JAR and affected runtime checks | Final JAR listing and smoke test from packaged artifact |
| Candidate changes after evidence capture | Revision or artifact digest mismatch | Mark affected evidence invalid immediately | Freeze new candidate and rerun the invalidation set | Ledger shows old and new identities plus complete reruns |
| Rollback rehearsal loses data | Restored candidate cannot open the preserved fixture | Block release integration | Restore untouched fixture backup, correct compatibility fault, rerun entire phase | Successful recovery rehearsal with digest comparison |

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Evidence artifact |
|---|---|---|---|---|---|
| P004-TASK-001 | Validate manifest completeness and identifier consistency | Compare all upstream packet revisions | Confirm pinned environment before runtime | Reject mismatched revision, dependency, or digest | Candidate and environment manifest |
| P004-TASK-002 | Parser, codec, persistence, command, and API regression tests | Load existing fixtures through the integrated candidate | Apply valid configuration and stage state, invoke commands and APIs | Malformed and unsupported fixtures, preserved prior state | Compatibility fixture report and data digests |
| P004-TASK-003 | Lifecycle and synchronization test coverage | Integrated-server state transition harness | Start world, mutate state, reload, transition, disconnect, reconnect, close, reopen | Interrupted apply and stale-client recovery | Integrated-server sanitized log and scenario record |
| P004-TASK-004 | Packet and player-scope assertions | Dedicated two-player fixture | Start dedicated server, connect mixed-ownership players, reconnect, shut down | Unauthorized mutation, stale session, player isolation | Dedicated multiplayer sanitized log and state comparison |
| P004-TASK-005 | Adapter resolution and independent-setting tests | EXT-001 optional dependency matrix | Launch supported present, absent, disabled, and combined installations | Missing class, disabled adapter, unsupported artifact handling | Optional integration matrix and artifact identities |
| P004-TASK-006 | Static trust-boundary and side-safety review | Permission and validation integration tests | Operator apply and synchronized client observation | Non-operator request, malformed payload, bounds failure, absent optional class | Security review and negative-test record |
| P004-TASK-007 | Formatter, static analysis, unit tests, applicable GameTests | Full Gradle verification and resource validation | Repository-defined test workflows at Java 21 | Deliberate failing fixture proves checks fail closed | Command result manifest and build output identity |
| P004-TASK-008 | JAR structure and metadata inspection | Run client and dedicated-server smoke from packaged JAR | Exercise representative user and operator flows from final artifact | Restore last accepted candidate and preserved fixture | JAR inventory, smoke record, documentation parity, recovery record |
| P004-TASK-009 | Trace stable IDs to all evidence rows | Cross-check revision and digest across every artifact | Confirm downstream consumer can identify the candidate unambiguously | Reject incomplete, stale, or mixed-candidate evidence | Final phase completion packet and invalidation ledger |
| CORE-REQ-008 | Existing and targeted regression suite passes | Integrated, dedicated, reconnect, reload, multiplayer, and optional-mod matrix passes | Supported 3.0.4 behavior is reproduced from the packaged candidate | Authority denial, malformed input, absent dependency, stale state, and rollback pass | Complete revision-bound compatibility packet |

Fixtures must be minimal, disposable, and traceable to a supported public contract. The data set includes existing supported configuration, a stage definition, persisted ownership, operator and non-operator identities, two-player mixed stage state, and optional integration combinations. Each fixture records its starting digest and expected observable state. No private production world or credential is used.

Run order is identity freeze, data and API compatibility, isolated runtime matrices, security checks, repository verification, packaged-artifact smoke and recovery, then packet audit. A failure is interpreted as a candidate defect, environment mismatch, fixture defect, or external-artifact mismatch and must be classified with evidence. Environment and fixture defects are corrected without changing product code, then the affected row is rerun. Candidate defects require a new candidate and invalidation review. Lower-fidelity unit or source evidence never substitutes for integrated server, dedicated server, multiplayer, optional-mod, packaged-JAR, or recovery proof.

## Documentation, Operations, and Release

- Update `README.md` only when a verified 3.0.4 compatibility fact affects installation, supported versions, optional dependencies, configuration defaults, or user-visible behavior.
- Update `DOCUMENTATION.md` and the documentation index with the verified compatibility matrix, server-authority boundaries, optional-mod absence behavior, and links to the applicable test and troubleshooting procedures.
- Update affected testing, compatibility, operations, and troubleshooting references with the exact Java 21, Minecraft 1.21.1, NeoForge 21.1.219 environments, fixture preparation, expected results, evidence collection, failure classification, and rerun order.
- Record no migration step unless evidence proves one is required. The expected release contract is a compatible patch with existing configuration, schema, and saved data preserved.
- Produce an operator-facing recovery procedure that stops the candidate, preserves the world and configuration, restores the last accepted artifact, restarts, verifies saved stage ownership, and captures sanitized diagnostics.
- Provide CORE-PHASE-005 the source revision, JAR digest, build and test results, final JAR inventory, runtime matrix, security result, documentation parity record, invalidation ledger, and recovery evidence.
- Do not publish, tag, close issues, or claim release completion in this phase.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| Matrix omits a public surface | Trace every CORE-REQ-008 noun and upstream completion packet into a row | Coverage audit finds an untraced contract | Add the row before phase exit | Completion packet and any completeness claim | Rerun new row and packet audit |
| Evidence mixes candidate revisions | Freeze revision and JAR digest in every artifact | Identity cross-check mismatch | Discard mixed evidence and freeze one candidate | All mixed or downstream evidence | Repeat from P004-TASK-001 as needed |
| Test environment drifts from supported platform | Pin and record Java 21, Minecraft 1.21.1, NeoForge 21.1.219 | Runtime or build metadata mismatch | Correct environment without product change | Checks run in the wrong environment | Rerun every affected check |
| Fixture is silently migrated or contaminated | Use immutable source fixtures and disposable copies | Starting or ending digest mismatch | Restore clean copy and investigate mutation | Data compatibility and dependent runtime proof | Repeat P004-TASK-002 and dependent scenarios |
| Optional dependency provenance changes | Verify EXT-001 artifact identities and hashes | Coordinate or hash mismatch | Stop affected rows and resolve approved artifact evidence | Optional integration results | Repeat complete optional matrix |
| Dedicated server passes only because client code is absent from the scenario | Require packaged-JAR startup and world load with exercised common paths | Linkage failure or missing exercised boundary | Correct side isolation on a new candidate | Build, dedicated-server, and relevant integration evidence | Repeat build and dedicated runtime suite |
| Security check validates UI only | Exercise server-side permission and validation directly | Unauthorized server state changes | Correct server boundary and restore fixture | Security, editor, packet, and affected multiplayer proof | Repeat P004-TASK-006 and dependent runtime rows |
| Concurrency timing masks stale synchronization | Compare revisioned authoritative and client state at explicit lifecycle points | Intermittent or revision-mismatched result | Correct state boundary and use deterministic transition signals | Reconnect, reload, and multiplayer proof | Repeat integrated and dedicated lifecycle matrices |
| Documentation claims unverified behavior | Bind every changed statement to a passing row | Parity review finds no evidence reference | Remove or correct claim | Documentation parity and release handoff | Repeat parity audit after documentation change |
| Recovery procedure modifies production-like data destructively | Use disposable fixture copies and preserve originals | Digest or ownership mismatch after rehearsal | Restore untouched fixture and block phase | Recovery and compatibility completion claim | Repeat recovery and any affected data checks |
| Upstream correction lands after phase proof | Monitor candidate ancestry and upstream packet identity | Revision or completion packet changes | Freeze new candidate and evaluate blast radius | All rows affected by changed component | Rerun invalidation-ledger set or entire phase when uncertain |

## Phase Completion Packet

CORE-PHASE-004 may close only when its external evidence packet contains all of the following and every item identifies the same final candidate where applicable:

- Source revision, branch ancestry, Minecraft 1.21.1, NeoForge 21.1.219, Java 21, Gradle wrapper, resolved dependency, and JAR digest manifest.
- References to the integrated completion packets for CORE-PHASE-001, CORE-PHASE-002, and CORE-PHASE-003.
- CORE-REQ-008 traceability matrix covering configuration, schema, persistence, commands, APIs, authority, packets, integrated server, dedicated server, reload, reconnect, multiplayer, optional integrations, client behavior, and packaged resources.
- Fixture inventory with starting digests, expected behavior, observed behavior, and proof that tests used disposable copies.
- Formatter, static analysis, unit test, applicable GameTest, Gradle build, resource validation, dependency, and security results.
- Integrated-server, dedicated-server, multiplayer, reconnect, reload, optional-mod present and absent, and client synchronization runtime evidence.
- Non-operator, malformed-input, absent-dependency, stale-state, classloading, and recovery negative-test results.
- Final JAR inventory and smoke results, documentation parity review, regression triage record, invalidation ledger, and recovery rehearsal.
- Evidence that the phase branch or integration workflow satisfied required checks and that no unresolved mandatory compatibility or security finding remains.
- Downstream handoff naming the immutable candidate source revision and artifact identity for CORE-PHASE-005.

The packet stores proof outside the protected plan set during execution. This blueprint is not updated as a status diary.

## Next Transition

After every exit criterion passes and CORE-PHASE-004 is integrated through the required repository workflow, verify that the resulting authoritative branch contains the exact candidate revision represented by the completion packet. Hand that immutable source revision, JAR digest, compatibility matrix, security result, invalidation ledger, and recovery evidence to CORE-PHASE-005. Do not start CORE-PHASE-005 while any CORE-PHASE-004 check is failed, stale, incomplete, or tied to a different candidate.

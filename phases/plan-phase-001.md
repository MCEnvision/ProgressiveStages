# Phase 001 Execution Blueprint

> **Plan ID:** PLAN-PHASE-001
> **Phase ID:** CORE-PHASE-001
> **Owner:** EntityPresenceEnforcer
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 001 of 006
> **Execution state:** COMPLETED
> **Integrated revision:** `09b18ad5b91a8c5b59faf1d35821f5c786427b80`
> **Signed phase tag:** `3.0.4-phase-001`

## Purpose and Ownership

`CORE-PHASE-001` is the completed execution blueprint for `CORE-REQ-002` and `DEC-004`. It repaired the issue `#24` entity presence hotspot while preserving server authoritative, player specific visibility, targeting, interaction, pacification, spawning, and shared entity lifecycle behavior. The master plan owns product scope, global phase topology, and the release endpoint. This file owns only the detailed historical execution contract and evidence for Phase 001.

The completed implementation introduced a transient entity presence context snapshot scoped to one player, one server tick, and one authoritative state revision. It is constructed only when required, reused across applicable tracked entity decisions while its tuple is valid, and retired when player relevant authoritative state or lifecycle changes. This snapshot is runtime state internal to `CORE-REQ-002`. It is not a build cache, optional adapter cache, persisted handoff, or dependency consumed by `CORE-PHASE-002`.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| VERIFIED | Phase 000 predecessor | `CORE-PHASE-000` was merged at `dc9e154871781de262ffd5eb401d65d0fa44cefb` and its signed annotated tag `3.0.4-phase-000` verified before Phase 001 began | Phase 000 completion packet, merge record, and signed tag record | Invalidated only if the recorded merge identity or tag signature is disproved |
| VERIFIED | Issue baseline | `CORE-REQ-001` preserved issue `#24`, the shipped hot path, expected semantics, and the controlled entity presence fixture | `docs/verification/3.0.4-phase-000-audit.md` | Invalidated if the preserved Phase 000 packet or measured shipped artifact identity changes |
| VERIFIED | Runtime implementation | The Phase 001 result uses an on demand player, tick, and authoritative revision context boundary rather than rebuilding a context for every tracked entity | Integrated revision `09b18ad5b91a8c5b59faf1d35821f5c786427b80` and focused test record | Invalidated by a later change to the entity presence context, decision, or invalidation path |
| VERIFIED | Correctness fixture | The dedicated server two player fixture preserved denied and eligible player isolation over shared entities, including grant and revoke transitions | `docs/verification/3.0.4-entity-presence-fixture.md` and the Phase 001 completion packet | Invalidated by a later change to tracking, targeting, pacification, stage mutation, or shared entity lifecycle behavior |
| VERIFIED | Performance fixture | Entity presence used `1.471195%` of sampled server thread work and the enabled p95 MSPT was `7.058566 ms` versus `10.927356 ms` control | Paired capture hashes and calculations in the Phase 001 completion packet | Invalidated if measured bytes, environment, population, warmup, sample selection, or decision code changes |
| VERIFIED | Integration state | Pull request `#27` merged Phase 001 at `09b18ad5b91a8c5b59faf1d35821f5c786427b80`, and signed tag `3.0.4-phase-001` resolves to that commit | GitHub merge record and signed tag verification | Invalidated only if repository history, tag target, or signature verification no longer matches the recorded identity |

The saved goal checkout `5b3077764907249b3711886cca538794f6139acf` is immutable creation provenance only. It was not the Phase 001 execution baseline and does not reopen this completed phase.

## Scope Boundaries

### Included Scope

- `CORE-REQ-002` repaired entity presence performance without changing multiplayer visibility, spawning, targeting, interaction, pacification, or lifecycle semantics.
- `DEC-004` required at most one on demand immutable context per evaluated player, server tick, and authoritative state revision tuple, entity presence work below five percent of sampled server thread work, and enabled p95 MSPT no more than ten percent above disabled enforcement.
- `CORE-REQ-002` included server thread ownership, player isolation, tuple reuse, same tick revision invalidation, lifecycle teardown, focused deterministic tests, a dedicated server two player workflow, and paired performance captures.

### Explicit Exclusions

- `CORE-REQ-003` and `CORE-REQ-004` optional integrations belonged to `CORE-PHASE-002` and were not Phase 001 work.
- `CORE-REQ-005`, `CORE-REQ-006`, `CORE-REQ-007`, `CORE-REQ-011`, and `CORE-REQ-012` belonged to later phases and did not provide Phase 001 entry or exit evidence.
- `CORE-PHASE-001` did not implement or benchmark inventory insertion, recipe serialization, persisted recipe reload, Easy Builder workflows, or any later candidate JAR.
- `FUT-001`, `FUT-002`, `FUT-003`, and `FUT-004` remained excluded.
- `CORE-REQ-002` did not authorize persistent data, configuration, command, packet, public API, or stage schema migration.

## Phase Contract

### CORE-PHASE-001 — Repair entity presence performance

**Objective:** Deliver and verify a player scoped immutable entity presence context boundary that preserves multiplayer semantics and passes the approved correctness and performance gates.
**Owner:** EntityPresenceEnforcer
**Dependencies:** CORE-PHASE-000
**Canonical requirements:** CORE-REQ-002
**Documentation and release impact:** Preserve the context lifetime, invalidation boundaries, controlled fixture, p95 MSPT calculation, profile collection, troubleshooting procedure, and issue `#24` acceptance evidence for the 3.0.4 release.
**Next transition:** CORE-PHASE-002

**Entry criteria**

- `CORE-PHASE-000` is merged at `dc9e154871781de262ffd5eb401d65d0fa44cefb`, and signed tag `3.0.4-phase-000` verifies at that commit.
- `CORE-REQ-001` supplies the issue `#24` shipped artifact hotspot, expected multiplayer behavior, observed decision path, and controlled fixture definition.
- `CORE-REQ-002` and `DEC-004` remain the authoritative behavior and threshold contract.
- `CORE-PHASE-001` has comparable presence disabled and presence enabled fixtures with one denied player, one eligible player, and the same shared entities.

**Implementation scope**

- `CORE-REQ-002` establishes an on demand server thread confined context boundary keyed by player identity, server tick, and authoritative state revision.
- `CORE-REQ-002` reuses one valid tuple across applicable tracked entity decisions and constructs no context when resolution does not require one.
- `CORE-REQ-002` invalidates stale tuples on relevant rule, stage, team, score, metric, dimension, session, disconnect, reload, and server stop changes.
- `CORE-REQ-002` preserves player specific decisions over shared entities without global spawn cancellation, removal, duplication, or eligible player concealment.
- `DEC-004` measures construction count, server thread share, and paired p95 MSPT under the controlled fixture.

**Execution order**

1. `P001-TASK-001` executes `CORE-REQ-002` and `DEC-004` by freezing the controlled correctness and performance protocol before implementation evidence is accepted.
2. `P001-TASK-002` executes `CORE-REQ-002` by establishing the server thread confined on demand context boundary and bounded construction count observability.
3. `P001-TASK-003` executes `CORE-REQ-002` by implementing authoritative revision and lifecycle invalidation, including same tick changes.
4. `P001-TASK-004` executes `CORE-REQ-002` by integrating tuple reuse into all entity presence decision paths without global entity mutation.
5. `P001-TASK-005` executes `CORE-REQ-002` by adding deterministic positive, negative, isolation, and invalidation tests.
6. `P001-TASK-006` executes `CORE-REQ-002` by running the dedicated server two player mixed stage workflow before performance acceptance.
7. `P001-TASK-007` executes `CORE-REQ-002` and `DEC-004` by collecting paired captures and calculating both approved thresholds.
8. `P001-TASK-008` executes `CORE-REQ-002` by integrating documentation, evidence, pull request state, merge identity, and the signed phase tag.

**Required evidence**

- `CORE-REQ-002` requires focused tests proving zero construction when unused, at most one construction per valid tuple, player isolation, tick rollover, and authoritative revision invalidation.
- `CORE-REQ-002` requires a dedicated server two player workflow proving denied and eligible player behavior, immediate grant and revoke transitions, and shared entity lifecycle preservation.
- `DEC-004` requires paired comparable captures proving entity presence work below five percent and enabled p95 MSPT no more than ten percent above disabled enforcement.
- `CORE-REQ-002` requires the measured JAR identity, capture hashes, environment, population, warmup, sample selection, calculation, and failure interpretation.
- `CORE-PHASE-001` requires applicable build, server verification, documentation review, pull request merge evidence, and signed tag verification.

**Exit criteria**

- `CORE-REQ-002` correctness passes at focused test and dedicated server two player runtime fidelity.
- `DEC-004` passes with at most one on demand context per valid tuple, no per tracked entity rebuild, entity presence share below five percent, and enabled p95 MSPT no more than ten percent above control.
- `CORE-PHASE-001` is merged through GitHub and signed tag `3.0.4-phase-001` verifies at the resulting authoritative default branch commit.
- No known mandatory `CORE-PHASE-001` owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Phase 000 integration identity | `CORE-PHASE-000` | Merge `dc9e154871781de262ffd5eb401d65d0fa44cefb` is present and signed tag `3.0.4-phase-000` resolves to it | Git ancestry, tag target, and signature verification | Stop Phase 001 entry and reconcile predecessor integration without substituting saved goal provenance |
| Issue 24 reproduction packet | `CORE-REQ-001` | Shipped artifact path, expected behavior, observed hotspot, and fixture are frozen | Compare packet revision, artifact hash, rule fixture, and scenario | Reject stale or mismatched performance evidence and repeat the baseline freeze |
| Entity presence behavior contract | `CORE-REQ-002` | Visibility, interaction, targeting, pacification, spawning, and lifecycle remain player specific and server authoritative | Mixed player expected result matrix | Reject a global entity behavior change before profiling |
| Performance thresholds | `DEC-004` | One on demand context per valid tuple, work below five percent, and p95 increase at most ten percent | Construction count and paired calculation review | Reject completion if either correctness or performance gate fails |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P001-TASK-001 | `CORE-REQ-002`, `DEC-004` | Freeze the controlled correctness and performance protocol | `CORE-PHASE-000`, issue `#24`, shared entity fixture | Versioned fixture protocol | Entity presence fixture and evidence naming | Fixture review proves matching environment, reset, warmup, population, sample, assertions, and calculations |
| P001-TASK-002 | `CORE-REQ-002` | Establish an on demand tuple scoped context boundary and construction counter | `P001-TASK-001`, compiled rule context contract | Reusable context boundary and bounded diagnostics | EntityPresenceContextCache, EntityPresenceEnforcer, condition context construction | Unit tests prove unused, reuse, player, tick, and revision cases |
| P001-TASK-003 | `CORE-REQ-002` | Connect authoritative revision and lifecycle invalidation | `P001-TASK-002`, stage, rule, team, score, metric, dimension, and session mutation seams | Stale tuple retirement | Authoritative state revision and lifecycle hooks | Focused tests prove same tick state changes cannot reuse stale context |
| P001-TASK-004 | `CORE-REQ-002` | Route entity presence decisions through tuple reuse without global mutation | `P001-TASK-002`, `P001-TASK-003` | Player specific decision path | Tracking concealment, interaction denial, targeting, pacification | Static call path review and mixed player assertions prove no per entity reconstruction or global removal |
| P001-TASK-005 | `CORE-REQ-002` | Add deterministic correctness and negative regression coverage | `P001-TASK-002`, `P001-TASK-003`, `P001-TASK-004` | Focused regression suite | EntityPresenceContextCacheTest and EntityPresenceFixtureProfilerTest | Focused tests pass for construction count, isolation, rollover, invalidation, and threshold calculation |
| P001-TASK-006 | `CORE-REQ-002` | Execute the dedicated server two client workflow | `P001-TASK-005`, controlled stage and entity fixture | Runtime correctness record | Dedicated server tracking and shared entity lifecycle | Denied player, eligible player, grant, revoke, interaction, targeting, and shared lifecycle assertions pass |
| P001-TASK-007 | `CORE-REQ-002`, `DEC-004` | Collect paired captures and compute server thread share and p95 delta | `P001-TASK-006`, identical enabled and control fixtures | Performance record and verdict | Server thread profiler and raw MSPT samples | Enabled share is `1.471195%` and p95 delta is `-35.404630%`, satisfying both thresholds |
| P001-TASK-008 | `CORE-REQ-002` | Integrate documentation and the completion packet | `P001-TASK-001` through `P001-TASK-007` | Merged revision, signed phase tag, and downstream evidence references | Documentation, pull request `#27`, authoritative default branch | Documentation review, merge ancestry, tag target, and signature verification pass |

The work packages executed in numeric dependency order. Correctness passed before performance evidence was accepted. No task used future recipe, Easy Builder, inventory insertion, optional adapter, or release candidate evidence. A fixture mismatch invalidated both paired captures rather than permitting partial reuse.

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Integrated entity presence implementation | `CORE-PHASE-002` | Player scoped on demand tuple reuse is present in the merged default branch | No configuration, schema, persistence, packet, command, or public API migration | Merge `09b18ad5b91a8c5b59faf1d35821f5c786427b80` and verified tag `3.0.4-phase-001` |
| Correctness packet | `CORE-PHASE-004` | Mixed player behavior and shared entity lifecycle pass at the Phase 001 revision | Evidence remains revision bound and is rerun only when an affected path changes | Focused test and dedicated server workflow records |
| Performance packet | `CORE-PHASE-004` | Both `DEC-004` thresholds pass under the frozen fixture | Captures remain bound to exact JAR, environment, population, warmup, and sample identities | JAR hashes, capture hashes, share calculation, and p95 calculation |
| Operator documentation | Maintainers | Context lifetime, invalidation, fixture, calculations, and diagnostics are documented | Documentation describes merged 3.0.4 work only | `docs/verification/3.0.4-entity-presence-fixture.md` and integrated documentation review |

`CORE-PHASE-002` consumed only the merged and signed Phase 001 result. It did not consume the runtime context snapshot as a cache or handoff object. `EXT-001` was Phase 002's separate artifact input and did not originate in Phase 001.

## Architecture and Implementation Boundaries

The logical server owns stages, rule revisions, player relevant facts, and every entity presence decision. `CompiledRuleEngine` owns normalized rule evaluation. `MinecraftConditionContextFactory` owns immutable context construction. `EntityPresenceEnforcer` owns player specific tracking concealment, interaction denial, targeting, pacification, and reuse of the transient snapshot. Client presentation consumes the authoritative result and does not construct or mutate policy.

The tuple identity is player identity, server tick, and authoritative state revision. The authoritative revision covers the compiled rule revision and the stage, team, score, and metric facts used by the context. A decision that does not need condition evaluation constructs no context. A valid tuple that needs evaluation constructs at most one immutable context and reuses it for applicable tracked entity decisions.

Rule reload, stage mutation, team mutation, score or metric mutation, dimension transition, session transition, disconnect, and server stop retire stale tuples. A same tick authoritative change makes the old tuple unavailable before the next decision. The snapshot remains server thread confined and never enters saved data, configuration, packets, public APIs, build artifacts as state, or optional integration boundaries.

Entity presence is a player specific policy over a shared world. A deny for one player cannot cancel spawning, remove or duplicate the entity, or suppress it for an eligible player. Instrumentation remains bounded and cannot add per tick log spam, filesystem access, blocking work, or broad collection scans to the decision path.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Cross player tuple reuse | Isolation assertion or mixed player runtime fails | Keep tuple ownership bound to the evaluated player | Correct ownership and invalidate affected runtime evidence | Player isolation tests and full two player workflow |
| Same tick stale state | Revision test observes an old decision after mutation | Retire the old tuple before the next decision | Correct revision publication or invalidation ordering | Same tick stage, rule, team, score, and metric mutation tests |
| More than one context for a valid tuple | Construction counter exceeds one | Restore one on demand construction path | Correct call routing and repeat correctness before profiling | Construction count suite and enabled profile |
| Global entity mutation | Eligible player loses visibility or shared entity lifecycle changes | Preserve shared entity and apply only player specific policy | Remove global mutation and rerun runtime behavior | Two player dedicated server workflow |
| Fixture identity mismatch | JAR, environment, population, warmup, duration, or sample differs | Reject the comparison | Reset the fixture and collect both captures again | Hash and manifest comparison plus paired calculations |
| Diagnostics add material cost | Profiler attributes meaningful work to instrumentation | Keep measurement bounded outside the normal hot path | Remove or reduce diagnostics and repeat enabled capture | Call path review and enabled profile |
| Public contract migration appears | Config, schema, command, API, packet, or persistence diff is detected | Reject the unowned change | Route the change to an authorized later plan revision | Compatibility diff and affected verification suite |

No recovery permits averaging away a failed run, accepting performance before correctness, or importing a future candidate JAR or recipe output proof into Phase 001.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Evidence artifact |
|---|---|---|---|---|---|
| `P001-TASK-001`, `CORE-REQ-002` | Fixture schema and calculation review | Baseline packet comparison | Controlled environment dry run | Reject mismatched identities | Versioned fixture document |
| `P001-TASK-002`, `CORE-REQ-002` | Unused, reuse, player, tick, and revision tests | Rule engine context path | Bounded construction count under server workload | Counter failure blocks profiling | Focused context test report |
| `P001-TASK-003`, `CORE-REQ-002` | Same tick invalidation tests | Mutation and lifecycle hook coverage | Grant, revoke, reload, dimension, disconnect, and stop transitions | Stale decision requires correction and rerun | Invalidation test and runtime records |
| `P001-TASK-004`, `CORE-REQ-002` | Call path inspection | Tracking, interaction, targeting, and pacification integration | Two staged players share the same persistent entities | Eligible player regression rejects implementation | Dedicated server mixed player record |
| `P001-TASK-005`, `CORE-REQ-002` | Focused regression suite | Applicable Gradle verification | Dedicated server starts with the integrated implementation | Failed test blocks runtime and profile acceptance | Test and build results |
| `P001-TASK-006`, `CORE-REQ-002` | Scenario assertion review | Two client session | Denied and eligible behavior plus immediate grant and revoke | Reset world and rerun from clean fixture on mismatch | Dedicated server runtime record |
| `P001-TASK-007`, `DEC-004` | Calculation tests | Enabled and control manifest comparison | Paired server thread and raw MSPT capture | Any identity or threshold failure rejects both captures | Capture hashes and threshold report |
| `P001-TASK-008`, `CORE-PHASE-001` | Documentation and diff review | Pull request checks | Authoritative default branch inspection | Merge or signature mismatch blocks transition | Pull request `#27`, merge identity, and signed tag record |

The runtime fixture used Minecraft 1.21.1, NeoForge 21.1.219, Java 21, two players in the same overworld area, 96 invulnerable persistent skeletons, a 60 second warmup, and the final 2,400 raw samples from each run. The measured JAR was `build/libs/progressivestages-3.0.3.jar` with SHA-256 `36b66b72b7626814f230761e856d1c1677056a5ece831da7f7566a0fb05db4e6` and SHA-512 `c74223ac02a9083b87f1f956a0328cdad81a27fec527f8a39711fea661f4a48ed2e0565695af96e64bf4e1eb5ffd1b922233b783f628840b03b98db300d565d0`.

The enabled entity presence share was `1.471195%`. Enabled p95 MSPT was `7.058566 ms` versus `10.927356 ms` control, a `-35.404630%` delta. The repository registered no GameTest functions at Phase 001 execution time, so the successful GameTest task with no registered functions was recorded but did not replace focused tests or the dedicated server two player workflow.

## Documentation, Operations, and Release

- `CORE-REQ-002` records the tuple lifetime, on demand construction, server thread ownership, invalidation events, mixed player semantics, and troubleshooting in maintained architecture and operator documentation.
- `DEC-004` records the controlled fixture, environment, reset, warmup, population, sample selection, p95 calculation, server thread share calculation, expected thresholds, and failure interpretation in `docs/verification/3.0.4-entity-presence-fixture.md`.
- `CORE-PHASE-001` preserves issue `#24` acceptance evidence, JAR hashes, capture hashes, pull request `#27`, integrated revision, and signed tag identity for later release traceability.
- `CORE-PHASE-001` introduces no operator migration, configuration key, schema field, release publication, or platform action.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| Context omits a player relevant fact | Authoritative revision covers every consumed fact | Direct decision and same tick mutation tests | Expand revision boundary and retire stale tuples | Context, runtime, and performance evidence | Focused suite, two player workflow, paired captures |
| Reuse crosses player or session boundaries | Tuple remains player and lifecycle scoped | Isolation and reconnect assertions | Correct ownership and teardown | Correctness and performance evidence | Isolation suite, reconnect, workflow, captures |
| Optimization changes shared entity semantics | No global mutation is allowed | Eligible player and entity lifecycle checks | Restore player specific enforcement | Runtime and performance evidence | Two player workflow then paired captures |
| Later code changes the decision path | Evidence is bound to exact integrated revision and JAR | Diff and ancestry inspection | Route revalidation to the owning later phase | Affected call path, runtime, and profile proof | Focused tests, applicable runtime, and performance fixture |
| Fixture or artifact changes | Hash every input and capture | Manifest comparison | Discard unmatched pair and recollect | Both captures and calculations | Full paired run from reset |
| Documentation diverges from implementation | Evidence references exact merged behavior | Documentation and source review | Correct documentation before release | Operator and issue closure evidence | Documentation review and artifact trace |

## Phase Completion Packet

The completed packet contains all of the following revision bound proof:

- `CORE-REQ-002` predecessor merge `dc9e154871781de262ffd5eb401d65d0fa44cefb`, measured decision commit `bb33ef9efd7adc98020b8a519743bfc6944146cd`, fixture follow up `fe319cba2d7f6c8112d485ca34ec87a2b18a351a`, phase branch tip `74ccf5f8ff9daf51223354352539b7de0f4336ba`, pull request `#27`, and integrated merge `09b18ad5b91a8c5b59faf1d35821f5c786427b80`.
- `CORE-REQ-002` focused test results for construction count, unused paths, tuple reuse, player isolation, tick rollover, same tick revision invalidation, and performance calculation.
- `CORE-REQ-002` dedicated server two player runtime evidence for visibility, interaction, targeting, pacification, grant, revoke, and shared entity lifecycle.
- `DEC-004` measured JAR SHA-256 and SHA-512, complete and selected capture hashes, environment manifest, enabled share, p95 values, delta calculation, and threshold verdicts.
- `CORE-REQ-002` documentation and fixture procedure integrated with the implementation.
- `CORE-PHASE-001` pull request merge record, authoritative default branch ancestry, signed annotated tag `3.0.4-phase-001`, verified signature, tag target, and next phase handoff.

This packet is historical evidence outside the protected plan set. Later changes may invalidate affected proof, but they do not rewrite this blueprint, reopen Phase 001, or turn its runtime snapshot into a downstream cache.

## Next Transition

The exact next transition is `CORE-PHASE-002`. Its branch may start only after a fresh remote check proves that the authoritative default branch contains `09b18ad5b91a8c5b59faf1d35821f5c786427b80` and signed tag `3.0.4-phase-001` resolves to that merge with a valid signature. That transition is now historical and complete.

`CORE-PHASE-002` receives the merged implementation and immutable evidence references. It does not receive or recreate the player tick context snapshot as a cache, service, artifact, or handoff object. There is no direct transition from Phase 001 to Phase 003, Phase 004, release work, or final completion, and there is no Phase 001 requirement for corrected recipe output, inventory insertion, Easy Builder, optional integration artifacts, or a forward candidate JAR.

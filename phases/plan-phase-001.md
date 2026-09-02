# Phase 001 Execution Plan

> **Plan ID:** PLAN-PHASE-001
> **Phase ID:** CORE-PHASE-001
> **Owner:** EntityPresenceEnforcer
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 001 of 006

## Purpose and Ownership

This phase repairs the entity presence performance defect tracked by `CORE-REQ-002` while preserving the existing server-authoritative multiplayer behavior for entity visibility, spawning, targeting, interaction, and lifecycle. It exists because issue `#24` identifies condition-context construction in the entity tracking path as a server-thread hotspot. The measurable outcome is a server-thread-confined, on-demand immutable context boundary keyed only by player identity, server tick, and authoritative state revision. That boundary eliminates full context reconstruction for each tracked entity, invalidates a prior same-tick tuple before the next decision when its revision changes even when no context was constructed for that tuple, and satisfies the approved `DEC-004` correctness and performance gate.

The master plan owns the product scope, global phase order, six-issue release endpoint, and `CORE-REQ-002` contract. This blueprint owns only the dependency-ordered implementation and proof for `CORE-PHASE-001`. It consumes the issue `#24` row and controlled entity-presence fixture from the Phase 000 completion packet. Phase 000 also delivers the `CORE-REQ-012` inventory-insertion seam map for Phase 003, but that handoff remains preserved and unconsumed here. This phase does not authorize inventory insertion, optional integration, editor, recipe serialization, client menu, release, or publication work assigned to later phases.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| VERIFIED | Phase dependency | `CORE-PHASE-000` must freeze all six baseline reports, preserve its separate `CORE-REQ-012` inventory-insertion seam map for Phase 003, and provide the issue `#24` reproduction, artifact identity, rules, and controlled fixture before this phase starts | `plan.md` §13 and the Phase 000 completion packet | Invalid if Phase 000 changes the six-issue baseline, issue `#24` reproduction, artifact identity, or entity-presence fixture; inventory-seam changes alone do not expand this phase |
| OBSERVED | Performance hotspot | The issue `#24` profile associates substantial server-thread work with entity-presence condition-context construction | `SRC-006`, issue `#24` Spark profile and configuration | Invalid if a fresh reproduction no longer attributes the cost to this path |
| VERIFIED | Product contract | Entity presence behavior must remain player-specific in mixed multiplayer ownership and must preserve visibility, spawning, targeting, and lifecycle semantics | `CORE-REQ-002` in `plan.md` §12 | Invalid if the master requirement or owner decision changes |
| VERIFIED | Performance decision | `DEC-004`, as refined by `CORE-REQ-002`, permits at most one on-demand immutable context construction for each player-identity, server-tick, and authoritative-state-revision tuple, requires same-tick revision invalidation whether or not the prior tuple constructed a context, forbids per-tracked-entity construction within a valid tuple, and sets the sampled work and p95 MSPT thresholds | `DEC-004` in `plan.md` §9 and `CORE-REQ-002` in `plan.md` §12 | Invalid only by a new locked owner decision or master requirement change |
| VERIFIED | Architecture boundary | `CompiledRuleEngine`, `MinecraftConditionContextFactory`, and `EntityPresenceEnforcer` are the named rule, context, and enforcement ownership boundaries | `plan.md` §11 | Invalid if Phase 000 proves the active implementation uses different boundaries |

## Scope Boundaries

### Included Scope

- `CORE-REQ-002` — repair entity-presence performance without changing multiplayer visibility, spawning, targeting, or lifecycle semantics.
- Establish a server-thread-confined, on-demand boundary that constructs at most one immutable condition context for each player-identity, server-tick, and authoritative-state-revision tuple that needs one.
- Eliminate full context reconstruction per tracked entity while retaining player-scoped decisions.
- Define deterministic invalidation for rule reload, stage mutation, team mutation, score or metric mutation, dimension transition, session transition, disconnect, and server stop as required by the master architecture. When a rule, stage, team, score, or metric change advances authoritative state during a tick, the prior tuple becomes unusable before the next decision whether or not it constructed a context, and the new tuple may construct at most one context on demand.
- Preserve mixed multiplayer behavior in which denied entities remain hidden from and unable to interact with the denied player while an eligible nearby player retains normal entity behavior.
- Preserve performance observability through repeatable profiling, p95 MSPT calculation instructions, fixture identity, and before-and-after evidence.
- Add focused static or unit, integration, dedicated-server multiplayer, negative, invalidation, and performance verification for the owned behavior.

### Explicit Exclusions

- `CORE-REQ-003` and `CORE-REQ-004` optional integration work belongs to `CORE-PHASE-002`.
- `CORE-REQ-005`, `CORE-REQ-006`, `CORE-REQ-007`, and `CORE-REQ-011` editor, recipe-lock serialization, client menu, and artifact-parity work belongs to `CORE-PHASE-003`.
- `CORE-REQ-012` inventory insertion schema, selector pairing, destination resolution, menu transaction enforcement, Easy Builder work, automation boundaries, compatibility proof, and performance work belongs to `CORE-PHASE-003` and `CORE-PHASE-004`. Phase 001 neither implements nor benchmarks inventory insertion.
- `CORE-REQ-008` plan-wide compatibility certification belongs to `CORE-PHASE-004`; this phase supplies its entity-presence packet but does not claim the full matrix.
- `CORE-REQ-009` and `CORE-REQ-010` release validation, publication, and issue closure belong to `CORE-PHASE-005` and `CORE-PHASE-006`.
- `FUT-001`, `FUT-002`, `FUT-003`, and `FUT-004` remain excluded.
- `NG-003` prohibits replacing the current hotspot with a full registry, stage, or entity scan in a server-tick or render hot path.
- No persistent-data or public schema migration, new lock category, entity spawning redesign, or client-authoritative policy decision is permitted.
- `DEC-004`, the entity-presence fixture, the player, server-tick, and authoritative-state-revision context boundary, the five-percent server-thread share threshold, and the ten-percent p95 MSPT threshold apply only to `CORE-REQ-002`. They establish no workload model, cache design, latency target, or acceptance threshold for `CORE-REQ-012` inventory transactions.

## Phase Contract

### CORE-PHASE-001 — Repair entity presence performance

**Objective:** Deliver and verify a player-scoped immutable entity-presence context boundary that preserves multiplayer semantics and passes the approved correctness and performance gates
**Owner:** EntityPresenceEnforcer
**Dependencies:** CORE-PHASE-000
**Canonical requirements:** CORE-REQ-002
**Documentation and release impact:** Update the entity-presence architecture and performance verification documentation with context lifetime, invalidation, controlled fixture, p95 MSPT calculation, profile collection, and troubleshooting guidance; provide release evidence for issue `#24` without closing it in this phase
**Next transition:** CORE-PHASE-002

**Entry criteria**

- `CORE-PHASE-000` is integrated and its completion packet pins the six-issue baseline, source revision, tested artifact, issue `#24` configuration, expected behavior, observed behavior, and controlled performance fixture.
- The Phase 000 packet's inventory-insertion seam map is identifiable as a separate downstream handoff for Phase 003 and can be preserved without adding inventory operations, entities, rules, or measurements to the Phase 001 fixture.
- The baseline fixture reproduces or artifact-verifies the entity-presence hotspot with presence rules enabled and provides a rules-disabled control using the same environment, players, entities, simulation conditions, warmup, and sampling procedure.
- The mixed-player scenario defines one denied player, one eligible nearby player, the same relevant entity, and observable expectations for visibility, targeting, attacks, pacification, disconnect, and re-entry.
- `DEC-004` remains resolved with its five-percent server-thread-work and ten-percent p95 MSPT thresholds, and `CORE-REQ-002` defines the operational boundary as at most one on-demand immutable context construction per player-identity, server-tick, and authoritative-state-revision tuple, with same-tick invalidation and no per-tracked-entity construction while the tuple remains valid.

**Implementation scope**

- `CORE-REQ-002` owns the context-lifetime contract, the key composed only of player identity, server tick, and authoritative state revision, isolation boundaries, invalidation coverage, entity-presence decision integration, mixed-player correctness, negative behavior, profiling fixture, and performance evidence.
- CORE-REQ-002 constructs an immutable condition context only on demand and at most once for each valid player-identity, server-tick, and authoritative-state-revision tuple that needs one. Decisions that do not consume a condition context may leave the tuple without a constructed context. After construction, every tracked-entity decision in that valid tuple reuses it.
- CORE-REQ-002 treats the authoritative state revision as covering the active compiled rule revision and every player-relevant stage, team, score, and metric fact consumed by the context. A same-tick change to any of those facts invalidates the prior tuple before the next decision whether or not the prior tuple had constructed a context; the new tuple may construct at most one context on demand, and subsequent decisions reuse it.
- CORE-REQ-002 prohibits a tracked entity from triggering another condition-context construction while the player-identity, server-tick, and authoritative-state-revision tuple remains valid.
- CORE-REQ-002 denies rendering, targeting, attacks, detection through the entity-presence interface, and targeting by a denied entity for the denied player while preserving normal behavior for an eligible nearby player.
- CORE-REQ-002 preserves shared-world entity lifecycle and global spawning. Player-specific denial must not delete, globally suppress, duplicate, or otherwise mutate an entity needed by an eligible player.
- CORE-REQ-002 observability identifies the fixture, baseline revision, enabled and disabled samples, sample duration, p95 MSPT calculation, entity-presence sample share, and pass or fail result without exposing private data or introducing per-tick log spam.
- No implementation, instrumentation, fixture population, or threshold in this phase is attributed to `CORE-REQ-012`; later phases must define and verify inventory-insertion performance from that requirement's own transaction workload and acceptance contract.

**Execution order**

1. `P001-TASK-001` executes `CORE-REQ-002` by freezing the Phase 000 issue `#24` baseline and defining the controlled fixture, measurements, mixed-player assertions, and failure interpretation.
2. `P001-TASK-002` executes `CORE-REQ-002` by defining the server-thread-confined, on-demand immutable context boundary keyed only by player identity, server tick, and authoritative state revision, plus its ownership, lifetime, player isolation, and observable construction-count contract.
3. `P001-TASK-003` executes `CORE-REQ-002` by defining and implementing every authoritative invalidation boundary named by the master architecture, including same-tick rule, stage, team, score, and metric revision changes before the next decision.
4. `P001-TASK-004` executes `CORE-REQ-002` by integrating context reuse into entity-presence decisions without per-entity reconstruction or global semantic changes.
5. `P001-TASK-005` executes `CORE-REQ-002` by adding focused unit or static and integration tests for reuse, invalidation, isolation, deterministic decisions, and negative behavior.
6. `P001-TASK-006` executes `CORE-REQ-002` by running the dedicated-server mixed-player scenario for concealment, interaction denial, pacification, eligible-player behavior, transition, and recovery paths.
7. `P001-TASK-007` executes `CORE-REQ-002` by collecting controlled rules-disabled and rules-enabled profiles and calculating server-thread share and p95 MSPT against the approved thresholds.
8. `P001-TASK-008` executes `CORE-REQ-002` by completing documentation, regression commands, recovery guidance, and the downstream phase completion packet.

**Required evidence**

- Construction-count proof that a tuple may remain unconstructed until a decision needs its condition context, at most one immutable context is constructed for a valid player-identity, server-tick, and authoritative-state-revision tuple, and multiple tracked entities never cause another construction for that tuple.
- Negative proof that another player never receives or reuses the first player's context, including when both observe the same entity.
- Invalidation tests for rule reload, stage mutation, team mutation, score or metric mutation, dimension transition, session transition, disconnect, and server stop. The rule, stage, team, score, and metric cases must each include a mutation between two decisions in the same server tick and prove that the prior tuple is unavailable before the second decision, the second decision constructs a context on demand when it needs one, and later decisions in that new tuple do not construct another context. At least one same-tick case must advance the authoritative state revision before any context is constructed for the prior tuple and prove invalidation has no prior-cache precondition.
- Dedicated-server multiplayer evidence showing that the denied player cannot render, target, attack, or be targeted by the denied entity while the eligible nearby player retains normal behavior with the same entity.
- Entity lifecycle evidence showing that player-specific concealment does not globally suppress, remove, or duplicate the shared entity.
- Baseline and enabled profile captures, fixture manifest, raw tick measurements, p95 MSPT calculation instructions, and pass or fail calculations.
- Proof of the exact approved gate: the server-thread-confined boundary is keyed only by player identity, server tick, and authoritative state revision; it constructs an immutable condition context on demand and at most once for a valid tuple; same-tick authoritative changes invalidate the prior tuple before the next decision without requiring a previously constructed context and allow at most one on-demand context for the new tuple; no construction occurs per tracked entity while the tuple remains valid; under the controlled fixture, entity presence work stays below five percent of server thread time and adds at most ten percent p95 MSPT versus presence rules disabled.
- Focused regression results, applicable project verification results, documentation diff, and clean artifact or log inspection.

**Exit criteria**

- `CORE-REQ-002` acceptance criteria and required evidence are complete at the specified fidelity.
- The server-thread-confined context boundary is keyed only by player identity, server tick, and authoritative state revision. A valid tuple constructs an immutable context only on demand and at most once, same-tick rule, stage, team, score, and metric revisions invalidate the prior tuple before the next decision even when it had no constructed context, and no construction occurs per tracked entity while a tuple remains valid.
- The mixed-player dedicated-server scenario passes for denied-player concealment and pacification, eligible-player normal behavior, shared entity lifecycle, transitions, disconnect, and recovery.
- Under the controlled fixture, entity presence work stays below five percent of server thread time and adds at most ten percent p95 MSPT versus presence rules disabled.
- The phase branch passes focused tests and every applicable formatter, static analysis, unit test, GameTest, build, dedicated-server, multiplayer, and artifact inspection gate required by the repository.
- The phase completion packet is sufficient for `CORE-PHASE-004` to repeat compatibility verification without reconstructing assumptions.
- The final phase verifies the owner-selected completion endpoint and plan-wide Definition of Done.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Phase 000 completion packet | CORE-PHASE-000 | Integrated packet pins the six-issue baseline, revision, artifact, issue `#24` configuration, expected result, observed result, assignment to `CORE-REQ-002`, and a separately identified `CORE-REQ-012` seam map reserved for Phase 003 | Compare packet identities with the checked-out phase base and controlled entity-presence fixture; confirm the inventory seam handoff remains separate and unchanged | Stop; do not profile or implement against an unpinned or mismatched issue `#24` baseline, and do not absorb the inventory seam handoff into this phase |
| Issue `#24` performance baseline | CORE-PHASE-000 | Reproduction or artifact-verification identifies the entity-presence context path and records a rules-disabled control | Re-run the recorded procedure before implementation | Route a changed hotspot back to the baseline audit instead of forcing this design |
| `CORE-REQ-002` contract | PLAN-MASTER | Multiplayer semantics and performance thresholds are unchanged | Review canonical acceptance criteria and required evidence | Stop with plan revision required if the product contract changed materially |
| `DEC-004` gate | PLAN-MASTER | Selected choice remains resolved and exact thresholds are authoritative | Match the locked owner-decision text and operational checks | Stop; phase cannot weaken, replace, or average away the thresholds |
| Controlled fixture | CORE-PHASE-000 | Enabled and disabled cases share environment, player count, entity population, simulation conditions, warmup, and sample procedure | Validate fixture manifest and reset procedure before each run | Reject incomparable captures and rerun from a clean fixture |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Player-tick-revision condition-context boundary | CORE-PHASE-004 | The server-thread-confined boundary is keyed only by player identity, server tick, and authoritative state revision. It constructs an immutable context on demand and at most once for a tuple that needs one. Same-tick authoritative changes invalidate the prior tuple before the next decision even if it had no constructed context, and no tracked entity causes another construction while its tuple remains valid | Transient only; no persisted stage, configuration, command, API, or network schema change | Tuple construction-count, zero-construction, isolation, same-tick revision, and invalidation tests |
| Entity-presence correctness packet | CORE-PHASE-004 | Denied and eligible players retain their respective behavior for the same shared entity | Existing multiplayer and shared-world semantics remain authoritative | Dedicated-server two-player scenario and negative assertions |
| Performance evidence packet | CORE-PHASE-004 | Fixture, baseline, profiles, raw measurements, calculations, and gate result are reproducible | Measurements identify exact source revision and environment | Rules-disabled and rules-enabled profile captures with p95 calculation |
| Entity-presence documentation | CORE-PHASE-004 | Context lifetime, invalidation, performance procedure, failure interpretation, and recovery are documented | Describes only behavior verified in this phase | Documentation review and link check |
| Phase completion packet | CORE-PHASE-002 | Phase 001 is integrated, no phase-owned defect remains, and next phase may start from approved master | Sequential phase workflow; no stacked phase branch | Merge record, verified checks, evidence inventory, and downstream handoff |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P001-TASK-001 | CORE-REQ-002, DEC-004 | Freeze the controlled performance and mixed-player correctness protocol | CORE-PHASE-000 packet, issue `#24` baseline, `DEC-004` | Fixture manifest, assertions, reset steps, sampling procedure, and failure interpretation | Entity-presence test and profiling interfaces | Dry-run both enabled and disabled cases and confirm comparable conditions |
| P001-TASK-002 | CORE-REQ-002 | Establish the server-thread-confined, on-demand context boundary keyed only by player identity, server tick, and authoritative state revision, with construction-count observability | P001-TASK-001, master architecture | Context ownership contract and bounded observability | MinecraftConditionContextFactory, EntityPresenceEnforcer | A tuple that does not need a context constructs none; many tracked-entity evaluations that need one construct at most one context for their valid tuple and never reuse it across players, ticks, or revisions |
| P001-TASK-003 | CORE-REQ-002 | Implement authoritative invalidation and teardown boundaries | P001-TASK-002 | Revision-aware invalidation behavior and stale-state prevention | EntityPresenceEnforcer, CompiledRuleEngine, player-relevant server state interfaces | Rule, stage, team, score, and metric changes between same-tick decisions invalidate the prior tuple before the next decision and allow at most one on-demand context for the new tuple; a revision advance before the prior tuple constructs a context also passes; transition, disconnect, stop, and reload tests invalidate the applicable tuple before later use |
| P001-TASK-004 | CORE-REQ-002 | Integrate context reuse into every entity-presence decision path | P001-TASK-002, P001-TASK-003 | Tuple-scoped reuse with unchanged decision semantics | EntityPresenceEnforcer, MinecraftConditionContextFactory, CompiledRuleEngine | Decision equivalence and no per-entity reconstruction assertions |
| P001-TASK-005 | CORE-REQ-002 | Add focused deterministic, isolation, same-tick invalidation, lifecycle invalidation, and negative regression coverage | P001-TASK-003, P001-TASK-004 | Automated regression suite | Entity-presence and condition-context test boundaries | Repeated runs produce the same decisions and per-tuple construction counts |
| P001-TASK-006 | CORE-REQ-002 | Execute dedicated-server mixed-player correctness and recovery scenarios | P001-TASK-004, P001-TASK-005 | Runtime logs or recordings and assertion results | Dedicated server tracking, interaction, targeting, and lifecycle behavior | Denied and eligible players pass every assertion against the same entity |
| P001-TASK-007 | CORE-REQ-002, DEC-004 | Execute controlled profiling and calculate both performance thresholds | P001-TASK-001, P001-TASK-005, P001-TASK-006 | Baseline and enabled profiles, raw MSPT series, calculations, and verdict | Server-thread entity-presence path and profiling procedure | Presence work is below five percent and p95 MSPT delta is at most ten percent |
| P001-TASK-008 | CORE-REQ-002 | Complete documentation and assemble the phase handoff | P001-TASK-005, P001-TASK-006, P001-TASK-007 | Updated guidance and completion packet | Entity-presence documentation and downstream verification contract | Documentation review, evidence identity check, and clean phase diff |

`P001-TASK-001` must finish before design or measurements are accepted. It extracts only the issue `#24` entity-presence inputs from the Phase 000 packet and leaves the separate `CORE-REQ-012` seam map for Phase 003 to consume directly from Phase 000. `P001-TASK-002` and the test-fixture implementation portion of `P001-TASK-005` may proceed in parallel after the fixture contract is frozen, but behavioral assertions in `P001-TASK-005` wait for `P001-TASK-003` and `P001-TASK-004`. Runtime correctness in `P001-TASK-006` must pass before performance results in `P001-TASK-007` can satisfy the phase; faster incorrect behavior is a failure. Documentation may be drafted alongside tests, but `P001-TASK-008` must record only verified final behavior. A failed task stops dependent work. Recovery reverts only the phase-owned change to the last passing phase revision, preserves the fixture and evidence, corrects the fault, and reruns every invalidated downstream task.

## Architecture and Implementation Boundaries

The logical server remains authoritative for stages, rule revisions, player-relevant facts, and entity-presence decisions. `CompiledRuleEngine` continues to own normalized rule evaluation. `MinecraftConditionContextFactory` remains the source of the immutable player context. `EntityPresenceEnforcer` owns player-specific concealment, interaction denial, targeting behavior, pacification, snapshot lifetime, and its integration with tracking decisions. Client presentation consumes only an authoritative deny or allow outcome and never constructs policy or mutates server state.

The context boundary is player-scoped, server-thread confined, and on demand. Its key contains only player identity, server tick, and authoritative state revision. Level, world, dimension, session, entity identity, rule identity, and tracked-entity identity are not additional key fields. The authoritative state revision represents the compiled rule revision and every stage, team, score, and metric fact consumed by the context. Reuse is legal only when every key value matches and lifecycle invalidation has not retired the tuple. A decision that does not consume a condition context constructs none. A valid tuple that needs a context may construct at most one immutable context, regardless of how many tracked entities require decisions. Contexts must not be shared across players, worlds, ticks, revisions, dimension transitions, or session transitions; world and lifecycle isolation is enforced by ownership and invalidation rather than additional key components. The context is transient and must not enter saved data, configuration, packets, or public APIs. Construction-count instrumentation must be bounded to tests or diagnostics and must not add broad allocation, filesystem access, blocking work, full collection scans, or per-tick log spam.

Invalidation must cover every authoritative source named by the master architecture: rule reload, stage mutation, team mutation, score or metric mutation, dimension transition, session transition, disconnect, and server stop. A rule, stage, team, score, or metric mutation during a server tick must advance the authoritative state revision and make the prior tuple unavailable before the next entity-presence decision whether or not that tuple constructed a context. The new tuple may construct at most one context on demand. Invalidation may eagerly discard a constructed context or retire an unconstructed tuple through revision advancement or lifecycle state, but it must make stale use impossible without assuming a prior stored context. Server-thread confinement must prevent reentrant events from exposing a partially built context or permitting two contexts for the same tuple. Failure to build a valid context must follow existing safe rule behavior and emit actionable bounded diagnostics; it must not silently share another player's state or retain a failed context as the tuple value.

Entity presence remains a player-specific presentation and interaction policy over a shared world. It must not convert a per-player deny into global spawn cancellation or entity removal when another player is eligible. The implementation must preserve the documented hidden denied-entity behavior and pacifism toward the denied player while retaining normal behavior for an eligible player. Any change that requires a persistence, network protocol, configuration schema, command, API, or stage-file migration is outside this phase and requires plan revision.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Context reused for another player | Player identity mismatch or isolation assertion | Reject reuse and construct the correct player's context on demand for that player's current tick and authoritative state revision tuple | Correct the three-field tuple or ownership boundary and rerun all correctness and profile tests | Two-player same-entity isolation test |
| Context reused after a same-tick rule, stage, team, score, or metric mutation | Revision mismatch, stale-decision assertion, or prior tuple access after mutation | Advance the authoritative state revision, invalidate the prior tuple before the next decision whether or not it constructed a context, construct at most one context on demand for the new tuple, and evaluate from current server state | Fix the missing mutation hook or revision advancement and rerun all dependent correctness and profile tests | Parameterized same-tick invalidation suite including a revision advance before prior-tuple construction and two or more post-mutation decisions |
| Multiple tracked entities rebuild one player's context | Construction count greater than one for the same player, tick, and authoritative state revision tuple | Fail the regression and keep the phase incomplete | Restore tuple-scoped reuse, then rerun unit, multiplayer, and performance evidence | Many-entity single-player tuple construction-count test |
| Denied behavior leaks to eligible player | Eligible player loses visibility, interaction, or targeting | Preserve normal eligible-player behavior without deleting or globally suppressing the entity | Revert global entity mutation and restore player-scoped enforcement | Dedicated-server mixed-player scenario |
| Denied entity can affect denied player | Render, target, attack, detection, or targeting assertion fails | Enforce concealment, interaction denial, and pacification for only that player | Correct the affected enforcement interface and rerun full scenario | Denied-player negative assertions |
| Entity despawns, duplicates, or changes lifecycle due to concealment | Entity identity or lifecycle assertion fails | Retain normal shared-world lifecycle independent of player-specific presentation | Remove lifecycle mutation introduced by the optimization | Shared-entity lifecycle check with both players |
| Player changes dimension or disconnects mid-evaluation | Transition event and stale tuple eligibility | Retire the prior tuple whether or not it constructed a context and never retain player or level references past teardown | Clear player-scoped transient state and rerun transition tests | Dimension, disconnect, reconnect, and stop tests |
| Context construction fails | Bounded diagnostic and missing valid context | Apply existing safe decision behavior without cross-player reuse or partial state | Fix the underlying state input; no stale snapshot may be promoted | Injected construction-failure test where supported by existing test boundaries |
| Performance gate fails despite correctness | Profile share or p95 MSPT exceeds threshold | Keep phase open and retain correctness baseline while locating remaining work | Optimize within existing semantics, then rerun from clean controlled fixture | Repeated paired enabled and disabled captures |
| Profiles are not comparable | Fixture identity, sample duration, warmup, or environment differs | Reject the result without averaging or threshold waiver | Reset both cases and repeat the prescribed procedure | Fixture-manifest comparison |

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Evidence artifact |
|---|---|---|---|---|---|
| P001-TASK-001 | Validate fixture manifest and assertion completeness | Dry-run control and enabled setup | Dedicated-server fixture reaches steady state | Change one fixture identity and prove comparison rejection | Versioned fixture manifest and procedure |
| P001-TASK-002 | Assert context immutability, server-thread confinement, the key's exclusive player-identity, tick, and authoritative-revision fields, on-demand behavior, and construction count | Evaluate many tracked entities for one player in one unchanged tuple | A context-free decision leaves the count at zero, and decisions that need the context produce a count no greater than one for that tuple | Attempt cross-player, cross-tick, and cross-revision reuse; exercise dimension and session changes as invalidation events rather than key fields | Focused unit results and bounded diagnostic output |
| P001-TASK-003 | Parameterized revision and invalidation tests | Mutate rule, stage, team, score, and metric state between decisions in the same tick, then exercise all transition and teardown boundaries | Same-tick mutation, reload, dimension, session, disconnect, and stop workflows | Prove the prior tuple is unavailable before the next decision even when its context was never constructed, and the new tuple constructs no more than one context on demand | Same-tick and lifecycle invalidation test report |
| P001-TASK-004 | Compare optimized and reference decisions for identical inputs | Tracking and interaction decisions share one valid context | Entity-presence behavior remains stable under many tracked entities | Missing, expired, and mismatched contexts do not leak state | Decision-equivalence report |
| P001-TASK-005 | Run focused deterministic regression suite repeatedly | Combine reuse and invalidation paths | Existing relevant GameTests or equivalent integration tests pass | Failure injection and recovery rerun where supported | Test result archive |
| P001-TASK-006 | Validate scenario assertion inventory | Two players observe the same entity with different stage eligibility | Dedicated-server visibility, targeting, attack, pacification, lifecycle, transition, and reconnect scenario passes | Denied player remains isolated while eligible behavior continues | Sanitized runtime log, recording, or assertion output |
| P001-TASK-007 | Validate raw sample parsing and p95 calculation | Pair enabled and disabled samples from identical fixtures | Entity-presence work stays below five percent of server thread and p95 MSPT delta is at most ten percent | An over-threshold or incomparable sample fails the gate | Profiles, raw measurements, calculation, and verdict |
| P001-TASK-008 | Documentation and evidence link checks | Completion packet identity review | Another maintainer can repeat the fixture from the documented procedure | Missing artifact or mismatched revision blocks handoff | Documentation diff and signed evidence inventory |

The controlled fixture must use the Phase 000 pinned environment, entity-presence rules, player roles, entity population, simulation conditions, warmup, sample duration, and reset procedure for both the presence-rules-disabled control and enabled candidate. Inventory insertion rules, container transactions, menu populations, destination resolvers, and automation paths are outside this fixture, and the resulting server-thread share and p95 MSPT calculations make no claim about `CORE-REQ-012`. Expected results are binary for correctness and threshold based for performance. A failed correctness assertion invalidates all performance evidence from that revision. An implementation change after profiling invalidates the profile, p95 calculation, multiplayer runtime proof, and dependent completion packet. Rerun order is focused static or unit checks, invalidation integration checks, mixed-player dedicated-server correctness, disabled control capture, enabled capture, calculation, then completion-packet validation. Lower-fidelity proof never substitutes for the dedicated-server multiplayer scenario or controlled performance profiles.

## Documentation, Operations, and Release

- Update the existing entity-presence architecture documentation with the player, server-tick, and authoritative-state-revision tuple identity; immutable lifetime; server-thread confinement; player isolation; same-tick revision advancement; and every invalidation boundary.
- Update performance verification guidance with the exact controlled fixture, reset and warmup procedure, rules-disabled control, enabled candidate, sample duration, server-thread share calculation, p95 MSPT calculation, pass thresholds, failure interpretation, and rerun order.
- Add operator troubleshooting guidance for collecting a sanitized profile and distinguishing condition-context cost from another hotspot without enabling per-tick debug logging.
- Record mixed-player expectations for denied-player concealment, attack denial, targeting denial, pacification, eligible-player behavior, and shared entity lifecycle.
- Add the issue `#24` acceptance evidence to the phase completion packet. Do not close the issue, publish release claims, or advertise 3.0.4 before the later integration and release phases pass.
- No migration note or configuration-key change is expected because the snapshot is transient and the public configuration, stage schema, saved data, commands, API, and network protocol remain unchanged. If implementation disproves this assumption, stop for plan revision instead of introducing an undocumented migration.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| Incomplete same-tick invalidation serves stale decisions | Enumerated revision boundaries and parameterized rule, stage, team, score, and metric mutation tests | Optimized and fresh reference decisions diverge, or the old tuple remains accessible after its revision changes | Repair revision advancement and invalidation before reuse | Unit, multiplayer, and profile results | Rerun tasks 003 through 008 |
| Duplicate contexts exist for a new same-tick revision tuple | Construction count exceeds one for the new tuple after invalidation | Enforce at most one server-thread-confined, on-demand construction path per tuple | Correct tuple construction and rerun from the first mutation test | Construction-count, multiplayer, and profile results | Rerun tasks 002 through 008 |
| Context is shared across players or survives a world, dimension, or session transition | Player mismatch, transition invalidation assertion, or mixed-player failure | Keep the key limited to player identity, server tick, and authoritative state revision while enforcing world and lifecycle isolation through ownership and invalidation | Correct tuple ownership or lifecycle invalidation and clear transient state | All correctness and performance evidence | Rerun tasks 002 through 008 |
| Optimization changes global entity semantics | Retain player-scoped enforcement and entity identity assertions | Eligible-player or lifecycle behavior changes | Revert global mutation and restore reference behavior | Runtime and performance evidence | Rerun tasks 004 through 008 |
| Instrumentation becomes a new hot path | Bounded counters and no per-tick logging | Profile attributes material work to diagnostics | Disable or redesign production instrumentation | Performance evidence | Rerun task 007 and completion checks |
| Fixture drift hides a regression | Pinned manifest and paired reset procedure | Baseline and candidate identities differ | Discard captures and reset fixture | Profile and p95 evidence | Repeat both task 007 captures |
| Later edit changes the measured implementation | Bind every artifact to source revision | Diff or artifact identity differs from profiles | Rebuild and repeat affected verification | Runtime, profile, and packet evidence | Rerun tasks 005 through 008 |
| Performance passes only by weakening enforcement | Correctness gate precedes profiling | Denied or eligible assertion fails | Restore semantics before optimization | All performance evidence | Rerun tasks 004 through 008 |
| Public contract migration becomes necessary | Preserve transient internal snapshot design | Schema, packet, command, or API diff appears | Stop and request plan revision | Phase scope and downstream contract | Re-author affected plan before implementation continues |

## Phase Completion Packet

The packet must identify the integrated source revision and built artifact, the Phase 000 issue `#24` baseline it consumes, the exact `CORE-REQ-002` and `DEC-004` trace, and every `P001-TASK-001` through `P001-TASK-008` result. It must contain focused test results; per-tuple construction counts; zero-construction proof for a tuple that does not need a context; same-tick rule, stage, team, score, and metric invalidation results; lifecycle invalidation results; the dedicated-server mixed-player scenario; sanitized runtime evidence; shared-entity lifecycle assertions; rules-disabled and enabled profiles; raw MSPT measurements; p95 calculation; server-thread share calculation; fixture manifest; documentation changes; clean diff inspection; and rollback or recovery procedure. It must prove that the server-thread-confined boundary is keyed only by player identity, server tick, and authoritative state revision, constructs at most one immutable context on demand for a valid tuple that needs one, invalidates each prior same-tick tuple before the next decision even if no context existed for it, permits at most one on-demand context for each new tuple, and performs no per-tracked-entity construction while a tuple remains valid. It must state explicitly that the separate Phase 000 `CORE-REQ-012` seam map remains an authoritative direct input to Phase 003, that Phase 001 did not implement or measure inventory insertion, and that none of its fixture or threshold conclusions apply to `CORE-REQ-012`.

The packet must state whether each performance threshold passed, without averaging away a failing run, and bind every result to the tested source revision and environment. It must include the phase branch and pull request state, required checks, independent review result when available, merge record, and confirmation that approved `master` contains the integration before `CORE-PHASE-002` begins. Evidence belongs in the repository's normal tests, verification documents, pull request, issue, and runtime artifacts during execution; this protected blueprint is not updated as a status diary.

## Next Transition

After every exit criterion passes, the phase pull request is merged through GitHub, required checks and review are complete, `origin/master` contains the merge commit, and the signed annotated phase tag is verified, begin `CORE-PHASE-002` from that updated approved `master`. Provide the Phase 001 completion packet as downstream evidence. Do not create or start the Phase 002 branch while Phase 001 is open, pending, failed, or unmerged, and do not treat a performance improvement as sufficient unless the full mixed-player correctness contract also passes.

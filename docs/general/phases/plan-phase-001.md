# Phase 001 Execution Plan

> **Plan ID:** PLAN-PHASE-001  
> **Phase ID:** CORE-PHASE-001  
> **Owner:** Editor  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 001 of 003

## Purpose and Ownership

This phase makes fresh configurations and editor-created stages leave jump, sprint, swim, climb and elytra unrestricted, while preserving every administrator-authored ability restriction. It adds plain configuration language and selected-node actions to the current editor without replacing its graph, draft, source-editing or server validation workflows. It owns execution detail for CORE-REQ-003, CORE-REQ-004 and CORE-REQ-005. The master remains the authority for requirements, IF-001 through IF-006, release publication and all phase topology. Phase 002 owns guide fields and their in-game presentation; this phase must preserve their source data when existing operations already preserve unknown fields, but does not add or require guide controls for exit.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
| --- | --- | --- | --- | --- |
| OBSERVED | Default generation | Startup and loader fallback independently write active showcase files, including an explicit `jump` lock. Existing files bypass generation. | FIND-005, SRC-013 | Recheck on the approved Phase 000 merge commit before editing. |
| OBSERVED | Ability enforcement | Empty ability configuration has no inherent jump lock. Explicit configured gates remain evaluated and synchronized. | FIND-005, SRC-013 | Recheck `AbilityEnforcer` and its tests on the phase baseline. |
| OBSERVED | Editor form | `EssentialsPanel` has the reported labels; `EffectsPanel` already edits explicit player abilities; TOML preservation helpers are newer on master. | FIND-006, SRC-014 | Recheck selected components and preservation tests on current `origin/master`. |
| PROPOSED | Graph interaction | `LayoutPage` has SVG pan, zoom, drag, connection and keyboard paths but no context menu. Existing `StageActions`, `StageDialogs` and `EditorContext` provide draft-safe actions. | FIND-007, SRC-014 | Confirm actual master APIs before binding actions. |
| OBSERVED | Runtime evidence | Research ran no build, server, browser or client workflow. Reporter runtime cause is unknown. | FIND-005 through FIND-008, SRC-017 | All execution evidence must be newly produced from the phase candidate. |

## Scope Boundaries

### Included Scope

- CORE-REQ-003: revise both default-generation paths and new-stage defaults so essential movement is unrestricted, without changing existing administrator files.
- CORE-REQ-004: add plain labels, concise player-effect help, entry and protection settings and priority provenance display using IF-001, IF-002 and IF-005.
- CORE-REQ-005: add selected-node toolbar and context actions through existing draft APIs, preserving graph behavior and destructive safeguards.
- CORE-REQ-007 contributor evidence: extend the Phase 000 shared capture only for default, ability and editor observations needed to prove this phase.

### Explicit Exclusions

- CORE-REQ-001 and CORE-REQ-002 runtime structure composition and priority arbitration are Phase 000 deliverables. This phase consumes their accepted controls and effective-priority source, but does not redefine their semantics.
- CORE-REQ-009 guide schema, editor fields, packets and stages-menu presentation are Phase 002 work. This phase does not block on guide implementation.
- A whole-editor rewrite, new graph framework, localization system, hidden-stage redesign, new hosting service, ability-enforcement removal and silent migration of existing files are excluded.
- CORE-REQ-006 final documentation convergence and CORE-REQ-008 publication are Phase 003 work. This phase updates only its accurate contributor documentation and evidence.

## Phase Contract

### CORE-PHASE-001 — Repair defaults and targeted editor usability

**Objective:** A new installation and newly created stage leave essential movement free, while authors can safely understand and operate the existing editor at the form and selected-node level.  
**Owner:** Editor  
**Dependencies:** CORE-PHASE-000, CORE-REQ-007, EXT-001, EXT-002, EXT-004  
**Canonical requirements:** CORE-REQ-003, CORE-REQ-004, CORE-REQ-005  
**Documentation and release impact:** Update the affected default, ability, editor and diagnostics guidance, `README.md`, `DOCUMENTATION.md` and `docs/README.md` links when actual behavior changes; retain proof in `docs/verification/3.1.0/`. Packaging and publication remain Phase 003.  
**Next transition:** CORE-PHASE-002

**Entry criteria**

- Phase 000 has a checked merge commit in actual `master`, resulting-master verification, a signed annotated tag and complete cleanup receipt. No phase branch is merely open, approved or queued.
- A phase branch under `envy/` is created from freshly verified `origin/master`; its matching milestone and issue/PR associations exist before implementation.
- IF-001 and IF-002 implementation supplies structure settings, actual priority arbitration and the inherited priority-source contract. IF-006 shared controls and Phase 000 diagnostic proof are available.
- EXT-001, EXT-002 and EXT-004 are revalidated for the exact candidate. The current checkout, laptop anchor, disposable runtime locations and Brave connection are discovered rather than guessed.

**Implementation scope**

- CORE-REQ-003 changes both `Progressivestages.generateDefaultStageFilesIfNeeded` and `StageFileLoader` fallback through one revised showcase policy; generated movement locks are removed, new editor stages start with no ability restrictions, and existing files remain untouched.
- CORE-REQ-004 changes the existing form language and help, exposes accepted Phase 000 structure controls, and displays the actual resolved priority and its inheritance source without serializing zero unless the author explicitly chose zero.
- CORE-REQ-005 adds nearby Edit, Connect, Duplicate and Delete actions only where existing draft APIs support them, with right-click and keyboard invocation isolated from graph gesture mutations.

**Execution order**

1. `P001-TASK-001`, CORE-REQ-003, CORE-REQ-004 and CORE-REQ-005, verifies the approved Phase 000 integration, captures both generation entry points and current draft interfaces, and registers test-resource teardown.
2. `P001-TASK-002`, CORE-REQ-003, implements and tests the shared safe default policy and empty new-stage ability defaults.
3. `P001-TASK-003`, CORE-REQ-004, implements plain labels, help and source-aware structure controls without altering unrelated TOML.
4. `P001-TASK-004`, CORE-REQ-005, implements selected-node toolbar, context menu and accessible keyboard behavior through existing draft actions.
5. `P001-TASK-005`, CORE-REQ-003, CORE-REQ-004, CORE-REQ-005 and CORE-REQ-007, extends shared diagnostic observations and performs unit, headless, Brave and residual movement proof.
6. `P001-TASK-006`, CORE-REQ-003, CORE-REQ-004, CORE-REQ-005 and CORE-REQ-006, updates contributor documentation and completes checked PR integration, resulting-master verification, signed tag and cursor-ready packet.

**Required evidence**

- CORE-AC-004 and CORE-AC-005 fixtures cover empty install, valid existing, malformed discovery, package-only, archive and restart states, explicit restrictions and old-file byte preservation.
- CORE-AC-006 and CORE-AC-007 prove exact help, field validation, effective-priority provenance, review/apply/export/reload preservation and no implicit priority zero.
- CORE-AC-008 and CORE-AC-009 prove real Brave toolbar/context behavior at viewport boundaries, keyboard/focus handling, cycle prevention, confirmation, cancellation, failed save and stale draft recovery.
- Headless ability and draft evidence, packaged asset identity, targeted silent laptop movement evidence and exact shared diagnostic records distinguish server decision, client received lock state and observed movement.
- Before any test launch, establish independent normal, validation-error and recovery source-preservation fixtures. Use only the existing test harness readiness and response timeouts after observing them in the current baseline; record each bounded wait, fail on timeout and do not use arbitrary sleep. In `editor-ui`, run `npm ci`, `npm run check`, `npm test` and `npm run build`. Run `./gradlew test` and `./gradlew build`, plus relevant verified server GameTests only after task-graph inspection proves no client, renderer or display dependency. Discover an existing formatter task or record that none exists; do not invent a formatter command.

**Exit criteria**

- CORE-AC-004 through CORE-AC-009 pass with no mandatory phase-owned defect, and generated or newly-created defaults contain no essential-movement locks while explicit authored locks still act and clear correctly after grant.
- All required local and CI checks pass, including `npm ci`, `npm run check`, `npm test`, `npm run build`, `./gradlew test`, `./gradlew build`, relevant verified server GameTests, `quality / gradle`, `quality / node`, `quality / secret scan` and newly required checks. Browser and client gates require their mandated evidence and stay open if unavailable.
- The phase PR receives one private independent review, merges with a merge commit into `master`, is verified on resulting `master`, is tagged with a signed annotated phase tag, and all owned resources are verified cleaned before the cursor can advance.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
| --- | --- | --- | --- | --- |
| IF-001 structure configuration | CORE-PHASE-000 | Accepted `entry_allowed`, independent protection fields and exact-ID semantics | Parser/compiler and Phase 000 integration receipt | Stop field work if contract is absent or differs. |
| IF-002 restriction arbitration | CORE-PHASE-000 | Effective priority and inheritance-source information are available from the accepted runtime path | Existing priority fixtures and diagnostic provenance | Do not infer a value or serialize zero; return ownership to Phase 000 defect resolution. |
| IF-006 diagnostics | CORE-PHASE-000 | Permission 3 bounded shared manager, status/off lifecycle and ability/editor categories | Phase 000 capture tests and command behavior | Keep dependent runtime proof open if controls fail. |
| Existing drafts and source preservation | Editor server APIs | Review/apply, validation and conflict behavior remain authoritative | Focused preservation and stale revision tests | Retain draft and surface server error; never retry or overwrite silently. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
| --- | --- | --- | --- | --- |
| Revised shared showcase policy | Fresh installs and Phase 003 release | Both generation paths avoid default essential-movement restrictions; existing files are not regenerated or rewritten | Existing package layout and nonmovement examples retained | Default discovery fixtures and candidate JAR inspection. |
| New-stage ability default | Editor and administrators | A newly created draft emits no ability restriction unless explicitly selected | Existing ability TOML and enforcement controls retained | Form tests, source diff and explicit-lock negative case. |
| Plain form controls | Phase 002 guide controls | Labels and help preserve serialized identity, unknown fields and explicit field presence | Priority omission remains distinct from explicit `0`; provenance uses IF-002 cascade | Review/apply/export/reload evidence. |
| Selected-node actions | Phase 002 editor extension | Draft-only toolbar/context affordances preserve cycle, confirmation and focus contracts | No graph-library or server-API replacement | Brave request, draft-state and screenshot evidence. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
| --- | --- | --- | --- | --- | --- | --- |
| P001-TASK-001 | CORE-REQ-003, CORE-REQ-004, CORE-REQ-005 | Recheck approved master, Phase 000 tag, current component APIs, both default paths and test/resource boundaries. Reproduce the active default fixture before changing it where practicable, and establish independent normal, validation-error and recovery source-preservation fixtures with current-harness bounded readiness and response waits. | CORE-PHASE-000, IF-001, IF-002, IF-006, EXT-001, EXT-002, EXT-004 | Candidate map, fixture matrix, recorded bounded waits and registered cleanup boundary. | `Progressivestages`, `StageFileLoader`, `DefaultShowcaseStages`, editor form/graph actions, existing tests. | Source/API comparison, current task-graph and existing timeout inspection, then baseline fixtures without arbitrary sleep. |
| P001-TASK-002 | CORE-REQ-003 | Make both generators use the revised nonmovement showcase policy; make new editor stages omit ability locks; preserve valid, archived and malformed files and every explicit restriction. | P001-TASK-001, FIND-005, FIND-008 | Fresh files with no jump, sprint, swim, climb or elytra locks and unchanged existing content. | `Progressivestages.generateDefaultStageFilesIfNeeded`, `StageFileLoader`, `DefaultShowcaseStages`, `AbilityEnforcer`, stage-creation defaults. | Extend `DefaultShowcaseStagesTest`, loader-discovery and `AbilityEnforcerTest`; inspect output bytes and actual lock-state transitions. |
| P001-TASK-003 | CORE-REQ-004 | Replace ambiguous labels with Stage name, Stage icon and Required stages plus concise help and examples; expose entry allowed, independent protections and signed priority sourced from IF-001/IF-002. Preserve advanced/source editing. | P001-TASK-001, IF-001, IF-002, FIND-006 | Accessible form controls and source-aware effective priority display. | `EssentialsPanel`, `EffectsPanel`, `RulesPanel`, TOML helpers and draft validation APIs. | React interaction/preservation tests, invalid type and priority errors, review/apply/export/reload and no implicit `priority = 0`. |
| P001-TASK-004 | CORE-REQ-005 | Add selected-node toolbar and right-click Edit, Connect, Duplicate and Delete where supported; add keyboard context trigger, focus return, Escape/outside dismissal and viewport clamping through existing actions. | P001-TASK-003, FIND-007 | Safe selected-node controls with draft-only mutations. | `LayoutPage`, `StageActions`, `StageDialogs`, `EditorContext` and existing cycle validation. | Focused interaction tests and Brave pan, zoom, drag, keyboard, right-click, delete confirmation, cancellation, stale-save and failed-save checks. |
| P001-TASK-005 | CORE-REQ-003, CORE-REQ-004, CORE-REQ-005, CORE-REQ-007 | Reuse shared ability/editor captures to prove actual decision paths and run layered default, source and browser workflows. | P001-TASK-002, P001-TASK-003, P001-TASK-004, IF-006 | Sanitized capture records, test reports, asset hashes and residual client evidence. | `InteractionCaptureManager`, `/stage debug`, ability sync, editor validation and packaged assets. | Commands and category self-tests, no-GUI server/GameTest after graph inspection, laptop movement and Brave proof. |
| P001-TASK-006 | CORE-REQ-003, CORE-REQ-004, CORE-REQ-005, CORE-REQ-006 | Document actual default recovery and editor behavior, package assets, inspect the complete diff, and execute the sequential integration gate. | P001-TASK-005, EXT-002 | Accurate contributor docs, completion packet and merged/tagged phase. | `README.md`, `DOCUMENTATION.md`, `docs/README.md`, `docs/test/diagnostics.md`, `docs/troubleshooting/easy-builder.md`, verification evidence. | Link/example review, `npm ci`, `npm run check`, `npm test`, `npm run build` in `editor-ui`, `./gradlew test`, `./gradlew build`, relevant verified server GameTests, formatter discovery only, JAR asset inspection, PR checks, independent review, resulting-master verification and cleanup receipt. |

Tasks execute in the listed order. P001-TASK-003 and P001-TASK-004 may share discovery results but P001-TASK-004 must not depend on unfinished structure semantics or bypass P001-TASK-003 preservation controls. Failed validation, stale draft or browser interaction retains the draft, reports the precise error and blocks only the affected proof.

## Architecture and Implementation Boundaries

Default generation remains server-owned. The two existing entry points must call a single policy or equivalent shared representation so a successful startup path and fallback cannot diverge. A malformed discovered file is configuration state, not an empty installation. New editor stages must omit ability restrictions at draft creation; the existing `EffectsPanel` remains the explicit opt-in path. `AbilityEnforcer` remains authoritative for configured locks and client updates.

The editor remains a presentation and draft client. It displays IF-002's resolved priority and inherited source, but server validation and accepted source remain authoritative. Omission means inherited priority and must remain absent in targeted edits; explicit zero is written only after deliberate author input. Help must state that `entry_allowed=true` removes this stage's entry denial rather than creating an ALLOW, while protection controls remain independent. No Phase 001 control can alter generic advanced rules, unknown fields, comments or guide fields through incidental serialization.

`LayoutPage` retains SVG geometry, pan, zoom, node dragging, edge behavior and its existing cycle validation. Context invocation is a separate pointer path: right-click prevents native or graph mutation only while opening the menu, never writes position, creates a connection, selects an edge for deletion or applies a draft. Toolbar and context operations route through `StageActions`, `StageDialogs` and `EditorContext`; Delete uses existing confirmation and remains draft-only. Unsupported legacy or archived operations remain unavailable with useful feedback.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
| --- | --- | --- | --- | --- |
| Default policy diverges between entry points | Empty startup and loader-fallback fixtures produce different sources or ability lists | Both use the same unrestricted essential-movement set | Correct shared policy, retain previous accepted files, rerun discovery matrix | Empty, package-only, restart and archived fixtures. |
| Existing or malformed files are overwritten | File digest changes without explicit editor apply | Existing valid, archived and malformed discovery never triggers replacement | Restore exact test fixture backup and fix discovery classification | Valid, invalid and named-showcase preservation checks. |
| Explicit restriction is lost | Ability capture shows no configured gate, or action succeeds before grant | Explicit ability still denies and clears after proper stage grant | Correct only the default/new-stage path and rerun grant/revoke | `AbilityEnforcerTest`, server state, client received state and action evidence. |
| Priority display changes source semantics | Unrelated edit adds `priority = 0`, or source differs after review/apply | Display inherited value/source without serializing absent field | Retain draft, show server field error, correct targeted serializer | Omitted versus explicit zero, invalid signed bounds, unknown-field/comment round trip. |
| Context gesture mutates graph | Draft revision, node location or edge changes on right click/cancel | Context open, Escape, outside close and cancel cause no mutation | Retain draft, close menu and rerun interaction after fix | Pointer-up, pan/zoom, drag, right-click and stale-save suite. |
| Action is unsupported or save fails | API rejects legacy/archived operation, validation failure or stale revision | No destructive local workaround; useful feedback and draft recovery | Keep draft for correction/review; never auto-retry apply | Server response, request trace and Brave recovery assertion. |
| Client evidence host or audio identity fails | Renderer, window/PID-to-stream mapping or mute readback missing | Stop owned client and leave only client gate unverified | Continue headless work and report exact missing capability | Host registry and cleanup receipt, no substituted server proof. |

## Diagnostics and Debugging

**Requirement IDs:** CORE-REQ-003, CORE-REQ-004, CORE-REQ-005, CORE-REQ-007  
**Task IDs:** P001-TASK-001, P001-TASK-002, P001-TASK-003, P001-TASK-004, P001-TASK-005, P001-TASK-006  
**Controls:** Reuse Phase 000 permission level 3 `/stage debug abilities on <player>`, `status` and `off`, plus `/stage debug editor on <player>`, `status` and `off`; server console omits `/`, requires explicit fixture target and never assumes a player. `off` is idempotent.  
**Signals:** Capture ID, sequence, accepted revision, ability, gate source, missing stages, locked/sync-changed state, editor operation, draft revision, validation result, exact error key, applied revision and effective priority source distinguish default, explicit and rejected paths.  
**Collection procedure:** Use the numbered local procedure below with the Phase 000 bounded capture contract, exact returned output path and cleanup.  
**Headless verification:** After inspecting the task graph for no client/display dependency, run focused JUnit and the actual server-side loader, `AbilityEnforcer` and editor draft-validation path with deterministic fixtures; these prove accepted config and server decisions, not rendered movement or browser input.  
**Client verification:** Laptop evidence is required for actual unrestricted movement and explicit-lock negative movement. Brave evidence is required for form and graph pointer/keyboard behavior. Missing laptop or Brave capability leaves that specific gate open.  
**Client audio isolation:** Before launch set the isolated instance master audio to zero, bind its exact Hyprland window and PID, correlate only its PipeWire or PulseAudio application stream, mute with `wpctl` or `pactl`, verify mute readback before actions, immediately mute and verify replacement streams, then stop the owned client and watcher and verify stream removal during teardown.  
**Budgets and privacy:** Diagnostics stay default off with permission level 3, 60 seconds, 200 events, 20 events per second, 128 KiB output, queue 256, strings 256 UTF-16 units and collections 32; safe-stop on limit/backpressure/writer failure, no disk blocking, no raw source, guide text, credentials, private addresses, chat or session data.  
**Regression and support:** Test enable/status/off, permission denial, absent target, timeout/reload/reset, limits, dropped/truncated counts, redaction, disabled and enabled overhead and unchanged behavior; update `docs/test/diagnostics.md` and `docs/troubleshooting/easy-builder.md`, retaining only sanitized `docs/verification/3.1.0/` evidence.

| Signal | Source and unit | Expected observation |
| --- | --- | --- |
| `ability`, `gate_source`, `missing_stages`, `locked`, `sync_changed` | `AbilityEnforcer`, server side, Boolean and bounded IDs | Empty generated/default stage has no essential-movement gate; explicit configured gate identifies its source and sync changes only when lock state changes. |
| `operation`, `draft_revision`, `validation_result`, `error_key`, `applied_revision` | Existing editor capture, server side, revisions and enum | Invalid type, overflow or stale revision does not publish; accepted edit receives accepted revision without raw TOML. |
| `priority`, `priority_source`, `source_key`, `final_result` | IF-002 resolved restriction path, signed integer and provenance | Omitted source remains inherited; explicit zero is distinguishable and no UI display changes action behavior. |
| `capture_id`, `sequence`, `records`, `bytes`, `dropped`, `truncated`, `stop_reason` | Shared manager, server side, counts and bytes | Capture remains bounded, stops with reason and disabled capture performs no recording work. |

1. Before any suite, record exact candidate commit/JAR hashes, Java/loader pins, fixture configuration, discovered runtime and browser paths, owned processes and scratch paths. Register finally/teardown. Direct only required sanitized records to `docs/verification/3.1.0/`; planning creates no runtime proof.
2. For default and ability fixtures on node-1, confirm the selected Gradle task graph starts no client, renderer or display. If a dedicated server is needed, create a unique runtime under the verified project anchor, write and read back one effective `eula=true`, start `--nogui`, verify readiness and use its owned console.
3. Enable `stage debug abilities on <fixture_player>` or `stage debug editor on <fixture_player>`, then run `status` and record the exact capture path. Exercise the actual discovery, new-stage, explicit-lock, invalid-priority, review/apply or stale-draft path. If target/control is unavailable, stop that capture and leave its proof unverified.
4. Inspect correlated returned records for source key, gate provenance, revision, lock and final result. Run a normal and negative stimulus. Disable capture with `off`, use `status` to confirm draining/off, and confirm one further matching stimulus records nothing.
5. For residual movement only, discover the laptop project anchor and isolated instance, desktop session and actual discrete RTX renderer. Match exact candidate JAR/dependencies/configuration to the ready private node-1 server endpoint, verify laptop reachability and use supported direct-connect or authorized desktop controls. Confirm both sides show the intended player joined the correct world before interaction.
6. Apply the audio-isolation field procedure before every laptop action. Test unrestricted jump, sprint, swim, climb and elytra defaults, then an explicit configured ability denial and post-grant clearance. Keep server decision, client received state and targeted visual evidence distinct. Do not use singleplayer, personal instances, global mute, a public port or a client on node-1.
7. For browser proof, use only the laptop Brave extension connection and exact candidate-served editor. Perform help/form edits, invalid validation, source-preserving review/apply, toolbar and context actions at zoom/viewport bounds, keyboard context/focus, Escape/outside dismissal, cycle rejection, delete confirmation/cancel and failed/stale save. Record requests, console errors, draft state and targeted screenshots without session data.
8. After final consumers finish, stop only owned server, client, watcher and browser test resources; wait for exit and verify owned stream removal. Remove exact disposable runtimes, worlds, logs, reports, screenshots, traces, temporary configs, downloads and test-only output without following symlinks or touching shared caches, source, personal instances or preexisting files. Verify cleanup on every host and report exact leftovers separately.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
| --- | --- | --- | --- | --- | --- | --- |
| CORE-REQ-003, P001-TASK-002 | Extend default and loader fixtures plus `AbilityEnforcerTest` | Both discovery entry points, reload and client lock-state path | Silent laptop default movement and explicit restriction/grant cycle | Valid, malformed, archived, package-only and existing files; restore retained fixture on rejection | node-1 headless after graph check; laptop plus ready private server only for movement | Test results, byte digests, capture excerpts and targeted movement proof. |
| CORE-REQ-004, P001-TASK-003 | Before test launch establish normal, validation-error and recovery preservation fixtures using current-harness bounded readiness/response waits, then run React form and TOML/source-preservation tests | Draft validate, review, apply, export and reload | Brave form/help and validation workflow against candidate | Invalid type/priority, omitted versus explicit zero, stale revision and unknown field/comment retention | node-1 for checks; laptop Brave via existing extension | Reports, recorded wait bounds, request/console summary, source diffs and asset digest. |
| CORE-REQ-005, P001-TASK-004 | Focused graph interaction regressions | Existing action APIs and cycle validation | Brave pointer/keyboard workflow at zoom and edge bounds | Right-click/close/cancel no mutation, failed save retains draft, delete confirmation and unsupported action | laptop Brave only, candidate editor and authorized session | Draft-state evidence, screenshots and console/request result. |
| CORE-REQ-007, P001-TASK-005 | Capture manager/control and overhead tests | Actual ability/editor evaluator records | Bounded server console collection and residual client correlation | Permission, missing target, timeout/reload, limit/backpressure and redaction | node-1 no-GUI server/GameTest; laptop only for named client claim | Sanitized capture, test report and cleanup receipt. |

## Documentation, Operations, and Release

Update `README.md`, `DOCUMENTATION.md` and affected topic links in `docs/README.md` with verified fresh-default behavior, explicit ability opt-in, non-destructive recovery of old generated restrictions, plain editor controls and selected-node actions. Update `docs/test/diagnostics.md` and `docs/troubleshooting/easy-builder.md` with the scoped ability/editor collection procedure and minimum redacted packet. Use actual examples only after their fixtures pass. Rebuild the existing Vite assets and confirm their candidate digest and JAR placement, but do not claim release publication or a separately hosted editor. Phase 003 owns release notes, wiki publication and platform delivery.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
| --- | --- | --- | --- | --- | --- |
| RISK-004, P001-TASK-002 and P001-TASK-005 | One policy for both entry points and no name-based rewrite | Generated source or existing-file digest mismatch, unexpected ability gate | Restore fixture, correct policy/classification and retain user files | Default/discovery and movement evidence | Full default matrix, ability path and client action rerun. |
| RISK-005, P001-TASK-003 through P001-TASK-005 | Reuse existing draft actions and isolate context gesture | Unexpected edge/position/revision or rejected stale apply | Retain draft, surface error and correct event/API route | Browser, source-preservation and graph evidence | Focused interaction suite and Brave workflow rerun. |
| RISK-011, P001-TASK-001, P001-TASK-005 and P001-TASK-006 | Candidate identity, silent-client procedure and owned-resource register | Host, renderer, stream, hash or cleanup mismatch | Stop owned client, preserve gate as unverified, reconcile exact leftovers | All runtime evidence on affected host | Candidate identity, readiness, mute/readback and cleanup rerun. |

## Phase Completion Packet

- Phase branch commit, signed commit evidence, matching milestone/issue/PR state, private independent review outcome, merge commit and signed annotated phase tag on resulting `master`.
- Exact candidate and packaged editor asset SHA-256/SHA-512, Java/NeoForge/Node identities, source diff inspection and no generated files, credentials, absolute paths or unrelated changes.
- CORE-AC-004 through CORE-AC-009 results, including both generation paths, explicit restriction negative case, source-preservation validation and real Brave action proof.
- Sanitized ability/editor capture records, test reports and targeted silent laptop evidence that clearly separates server decisions, client received state and visual movement.
- Accurate contributor documentation and link review, plus exact host/path/process cleanup receipts. A failed or incomplete cleanup remains an open phase gate.

## Next Transition

After every completion-packet item passes, use the bundled phase-transition helper with the expected cursor digest and complete gate receipt to advance exactly once to CORE-PHASE-002. Its first work package verifies the Phase 001 merged/tagged baseline and guide pipeline/transport bounds. Do not create a future branch, begin guide implementation or modify the immutable goal before that transition.

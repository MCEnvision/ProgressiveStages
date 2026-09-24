# ProgressiveStages 3.1.0 Plan

> **Plan ID:** PLAN-MASTER  
> **Plan status:** VALIDATED  
> **Project state:** EXISTING  
> **Planning subject:** ProgressiveStages 3.1.0 structure independence and guided progression  
> **Plan profile:** software_product  
> **Diagnostics contract:** 2

## 1. Project Identity

```text
Project: ProgressiveStages
Requested artifact: authoritative_plan
Repository root: /mnt/hermes/projects/ProgressiveStages
Starting branch: envy/remove-completed-plan
Starting commit: f7ae37bb80679d8ea359a7e0e315d1edae51d67a
Authoritative remote:
origin
https://github.com/MCEnvision/ProgressiveStages.git
Remote ref: untracked
Remote commit: unavailable
```

These values preserve creation provenance. They do not select the implementation baseline. The observed approved default branch is `master`, at `c0a3ef2c32a5406a0bf12e43d2c475aa576fe9d6`. Phase execution must recheck the remote and start from the latest approved `origin/master`, preserving newer editor repairs and unrelated working changes. Do not rename the default branch or use the older starting checkout as the product baseline.

The authoritative installed master is `docs/general/plan.md`. Its mandatory companions are `plan.index.json`, `plan.handoff.json`, and the four registered files under `phases/` beside this master. No aspect plan is needed. The complete finite catalog in Section 13 owns sequence and requirement allocation; the phase files own execution detail.

## 2. Planning Subject and Source Roles

| ID | Role | Subject | Source | Intended use |
| --- | --- | --- | --- | --- |
| SRC-001 | owner_request | Independent structure rules and focused issue 57 improvements | Current owner request and supplied sponsor example | Defines mandatory behavior and excludes a complete editor rewrite. |
| SRC-002 | owner_request | Movement defaults, optional configuration and release endpoint | Current owner answers in this planning task | Locks essential movement freedom, configurable priorities and release publication. |
| SRC-003 | owner_request | Version 3.1.0 and authored next-stage guide | Current owner steering in this planning task | Locks version and player guide edited through custom editor fields. |
| SRC-010 | repository_evidence | Pinned repository observation or issue report | gradle.properties; gradle/wrapper/gradle-wrapper.properties; build.gradle; editor-ui/package.json; editor-ui/package-lock.json; .github/workflows/quality.yml | Evidence for behavior, constraints and validation, never runtime proof or authority |
| SRC-011 | repository_evidence | Pinned repository observation or issue report | src/main/java/com/enviouse/progressivestages/common/lock/LockDefinition.java; src/main/java/com/enviouse/progressivestages/common/lock/LockRegistry.java; src/main/java/com/enviouse/progressivestages/server/loader/StageFileParser.java; src/main/java/com/enviouse/progressivestages/common/rehaul/LegacyStageCompiler.java; src/main/java/com/enviouse/progressivestages/server/loader/Schema4StageCompiler.java | Evidence for behavior, constraints and validation, never runtime proof or authority |
| SRC-012 | repository_evidence | Pinned repository observation or issue report | src/main/java/com/enviouse/progressivestages/server/enforcement/StructureEnforcer.java; src/main/java/com/enviouse/progressivestages/server/enforcement/ConditionalLockEngine.java; src/main/java/com/enviouse/progressivestages/common/api/structure/StructureAccessArbitration.java; src/main/java/com/enviouse/progressivestages/server/enforcement/StructureSessionAccessPolicy.java; src/main/java/com/enviouse/progressivestages/server/structure/StructureSessionManager.java | Evidence for behavior, constraints and validation, never runtime proof or authority |
| SRC-013 | repository_evidence | Pinned repository observation or issue report | src/main/java/com/enviouse/progressivestages/Progressivestages.java; src/main/java/com/enviouse/progressivestages/server/loader/StageFileLoader.java; src/main/java/com/enviouse/progressivestages/server/loader/DefaultShowcaseStages.java; src/main/java/com/enviouse/progressivestages/server/enforcement/AbilityEnforcer.java; src/main/java/com/enviouse/progressivestages/common/config/StageConfig.java | Evidence for behavior, constraints and validation, never runtime proof or authority |
| SRC-014 | repository_evidence | Pinned repository observation or issue report | editor-ui/src/features/LayoutPage.tsx; editor-ui/src/features/stages/EssentialsPanel.tsx; editor-ui/src/features/stages/StageActions.tsx; editor-ui/src/features/stages/StageDialogs.tsx; editor-ui/src/features/stages/EffectsPanel.tsx; editor-ui/src/features/stages/RulesPanel.tsx; editor-ui/src/store/EditorContext.tsx; editor-ui/src/lib/toml.ts; editor-ui/src/lib/tomlSource.ts | Evidence for behavior, constraints and validation, never runtime proof or authority |
| SRC-015 | repository_evidence | Pinned repository observation or issue report | src/main/java/com/enviouse/progressivestages/server/commands/StageCommand.java; docs/test/diagnostics.md; docs/troubleshooting/easy-builder.md; README.md; DOCUMENTATION.md; docs/README.md | Evidence for behavior, constraints and validation, never runtime proof or authority |
| SRC-016 | repository_evidence | Pinned repository observation or issue report | src/test/java/com/enviouse/progressivestages/server/loader/DefaultShowcaseStagesTest.java; src/test/java/com/enviouse/progressivestages/server/loader/StageFileParserTest.java; src/test/java/com/enviouse/progressivestages/server/loader/Schema4StageCompilerTest.java; src/test/java/com/enviouse/progressivestages/server/loader/LegacyCompatibilityBaselineTest.java; src/test/java/com/enviouse/progressivestages/server/enforcement/AbilityEnforcerTest.java; src/test/java/com/enviouse/progressivestages/server/enforcement/ConditionalLockEngineTest.java; src/test/java/com/enviouse/progressivestages/server/enforcement/StructureSessionAccessPolicyTest.java; src/test/java/com/enviouse/progressivestages/common/api/structure/StructureAccessArbitrationTest.java; src/test/java/com/enviouse/progressivestages/client/editor/PackagedEditorAssetsTest.java | Evidence for behavior, constraints and validation, never runtime proof or authority |
| SRC-017 | review_feedback | Pinned repository observation or issue report | https://github.com/MCEnvision/ProgressiveStages/issues/57 | Evidence for behavior, constraints and validation, never runtime proof or authority |
| SRC-018 | repository_evidence | Pinned repository observation or issue report | git ls-remote origin refs/heads/master and local envy/issue_57 history/status | Evidence for behavior, constraints and validation, never runtime proof or authority |
| SRC-019 | repository_evidence | Pinned repository observation or issue report | src/main/java/com/enviouse/progressivestages/common/config/StageDefinition.java; src/main/java/com/enviouse/progressivestages/client/ClientStageCache.java; src/main/java/com/enviouse/progressivestages/client/gui/StageTreeScreen.java; src/main/java/com/enviouse/progressivestages/common/network/NetworkHandler.java; editor-ui/vite.config.ts; .github/workflows/release-validation.yml; docs/release/3.0.4.md | Evidence for behavior, constraints and validation, never runtime proof or authority |
| SRC-004 | audit_evidence | Execution and publication capabilities | research/sources/environment.json | Identifies available infrastructure and remaining candidate-specific verification gates. |

This plan covers a product change and its release. Research, issue comments and source examples explain the change; they are not substitutes for the product or its acceptance evidence. `research/brief.md`, `research/repository-map.md`, `research/intake.json` and `research/evidence.json` retain the bounded evidence package and exact fingerprints. The resolved decisions and this contract define the mandatory scope; repository observations establish the starting state and never override the required outcome.

## 3. Purpose and Intended Outcome

Pack authors can unlock structure entry with one stage while retaining placement protection through a separate stage that has no normal grant mechanism. Administrators can understand the source and priority of a restriction, edit it through the existing editor, and retain every intentional movement restriction in their own packs. New installations and newly created stages do not surprise players by locking essential movement.

The complete player workflow is to open the stages menu, see visible stages whose prerequisites are met, choose among branches, read authored instructions about how to unlock a stage and where to go, and see that guidance refresh after stage or definition changes. The author workflow is to edit those guide fields in the GUI, review and apply a draft, and verify the same text in game. The delivery workflow ends with the tested 3.1.0 JAR on both established platforms, including the updated web editor served by that JAR.

Necessary engineering includes attributed structure contributions, strict configuration validation, real priority arbitration, stable recommendation rules, protocol version handling and bounded diagnostics. A new quest engine, a new graph framework and a separate hosting service provide no necessary benefit to these workflows and remain outside this release.

## 4. Evidence Based Current State

| Area | Evidence class | Finding | Evidence |
| --- | --- | --- | --- |
| Structure rules | OBSERVED | Global merged flags lose stage and structure attribution; compiled entry rules can survive an aggregate only fix | FIND-001, FIND-003; SRC-011, SRC-012 |
| Entry independence | PROPOSED | `entry_allowed=true` suppresses only its own entry denial and keeps independent protections | FIND-002, FIND-004 |
| Movement | OBSERVED | Both initialization paths generate active showcase restrictions; empty ability configuration itself has no jump lock | FIND-005, FIND-008; SRC-013 |
| Editor | OBSERVED | Existing SVG graph and draft operations provide reuse points; current labels and distant controls explain reported friction | FIND-006, FIND-007; SRC-014 |
| Support | OBSERVED | Existing bounded `/stage debug` capture and compiled explain omit some actual legacy and provider decisions | FIND-009; SRC-015 |
| Guide | OBSERVED | Description, dependency availability and inspector exist; dedicated guide fields do not; the positional network protocol is `2` | FIND-012; SRC-019 |
| Publication | OBSERVED | Broker maps both platforms but currently selects an older 3.0.4 artifact; editor assets are bundled | FIND-013; SRC-004, SRC-019 |
| Runtime | UNKNOWN | Reporter configuration and actual client behavior are not reproduced by research | FIND-011; SRC-017 |
| Verification | OBSERVED | Existing tests and configured dedicated GameTests are available; research launched no product or tests | FIND-010; SRC-010, SRC-016 |

The pinned baseline uses Minecraft 1.21.1, NeoForge 21.1.248, Java 21 and Gradle 8.8. The wrapper distribution was read directly at the pinned commit and confirms 8.8. Preserve the existing mapping and dependency pins rather than normalizing unusual version combinations. React, TypeScript, Vite and Vitest already serve the editor. No formatter task was declared in the observed Gradle build; verify available checks without inventing a formatter command.

The chosen structure model replaces lossy aggregation with immutable attributed contributions, rather than turning an entry flag into a global allow. Reusing generic explicit ALLOW rules avoids inventing tri state convenience flags. The chosen editor change adds nearby controls to the existing graph rather than replacing its geometry, navigation and validation. Dedicated guide metadata keeps general description intact rather than overloading its tooltip meaning. A protocol bump makes positional changes explicit rather than pretending old decoders can consume appended fields.

Runtime hypotheses become early regression fixtures. A hidden compiled denial, a cross structure flag leak or an existing malformed configuration must be observed through the actual affected entry point before its corresponding gate can pass. Capability observations are prerequisites, not evidence that the candidate renderer, audio stream, gameplay or release already works.

## 5. Product Contract and Profile Coverage

| Profile area | Status | Source | Contract location | Rationale |
| --- | --- | --- | --- | --- |
| Inputs and outputs | covered | SRC-011, SRC-012, SRC-014, SRC-019 | Inputs and outputs | Define the bounded configuration, client/server/editor and release contracts with concrete failure proof in the master and phases. |
| Component architecture | covered | SRC-011, SRC-012, SRC-014, SRC-019 | Component architecture | Define the bounded configuration, client/server/editor and release contracts with concrete failure proof in the master and phases. |
| State and persistence | covered | SRC-011, SRC-012, SRC-014, SRC-019 | State and persistence | Define the bounded configuration, client/server/editor and release contracts with concrete failure proof in the master and phases. |
| Failure taxonomy | covered | SRC-011, SRC-012, SRC-014, SRC-019 | Failure taxonomy | Define the bounded configuration, client/server/editor and release contracts with concrete failure proof in the master and phases. |
| Versioning | covered | SRC-011, SRC-012, SRC-014, SRC-019 | Versioning | Define the bounded configuration, client/server/editor and release contracts with concrete failure proof in the master and phases. |
| Security | covered | SRC-011, SRC-012, SRC-014, SRC-019 | Security | Define the bounded configuration, client/server/editor and release contracts with concrete failure proof in the master and phases. |
| Test system | covered | SRC-011, SRC-012, SRC-014, SRC-019 | Test system | Define the bounded configuration, client/server/editor and release contracts with concrete failure proof in the master and phases. |
| Release lifecycle | covered | SRC-011, SRC-012, SRC-014, SRC-019 | Release lifecycle | Define the bounded configuration, client/server/editor and release contracts with concrete failure proof in the master and phases. |
| Generalization | covered | SRC-011, SRC-012, SRC-014, SRC-019 | Generalization | Define the bounded configuration, client/server/editor and release contracts with concrete failure proof in the master and phases. |
| Determinism | covered | SRC-011, SRC-012, SRC-014, SRC-019 | Determinism | Define the bounded configuration, client/server/editor and release contracts with concrete failure proof in the master and phases. |

### Inputs and outputs

IF-001 through IF-005 bind authored configuration, validated drafts, accepted state, runtime decisions and rendered player text. Published artifacts are covered by Section 16.

### Component architecture

Section 11 retains the existing common model, server enforcement, web draft and client presentation boundaries.

### State and persistence

Sections 11 and 15 require immutable accepted definitions, unchanged ownership persistence, transactional reload and preservation of administrator files.

### Failure taxonomy

Configuration rejection, stale drafts, protocol mismatch, lost capability and partial publication have distinct recovery in Sections 11, 15 and 17.

### Versioning

The release is 3.1.0, network protocol is 3 and schema 4 metadata is additive. IF-004 requires matching client/server negotiation and coherent snapshots.

### Security

Section 11 preserves server authority and editor permissions; IF-003 and IF-006 bound inert text and redact diagnostic output.

### Test system

Section 14 assigns actual suites, server event fixtures, browser workflows and silent client acceptance to their correct hosts.

### Release lifecycle

Sections 13 and 16 bind checked integration, signed tags, exact artifact selection, accepted public file identity and release verification.

### Generalization

Exact configured structure IDs, independent owner contributions and authored Unicode guides support ordinary packs without fixture specific IDs or a new quest engine.

### Determinism

IF-002 freezes priority and denial ties; IF-003 freezes recommendation ordering; IF-004 applies complete accepted snapshots atomically.

## 6. Mandatory Scope

| Requirement | Mandatory outcome | Canonical implementation phase |
| --- | --- | --- |
| CORE-REQ-001 | Independent structure entry and player protections | CORE-PHASE-000 |
| CORE-REQ-002 | Compatible composition, priorities and advanced overrides | CORE-PHASE-000 |
| CORE-REQ-003 | Unrestricted essential movement defaults with retained explicit controls | CORE-PHASE-001 |
| CORE-REQ-004 | Plain editor labels and effective configuration help | CORE-PHASE-001 |
| CORE-REQ-005 | Nearby graph actions with safe pointer and keyboard workflows | CORE-PHASE-001 |
| CORE-REQ-006 | Accurate examples, compatibility and recovery documentation | CORE-PHASE-003 |
| CORE-REQ-007 | Actual decision diagnostics and bounded support collection | CORE-PHASE-000 |
| CORE-REQ-008 | Verified integration and publication of 3.1.0 and its editor | CORE-PHASE-003 |
| CORE-REQ-009 | Authored guidance in the actual in game stages menu | CORE-PHASE-002 |

Documentation and diagnostics are incremental obligations of each contributing phase. Canonical ownership is unique; it does not postpone documentation until release or make early diagnostics depend on a future guide implementation.

## 7. Optional / Future Scope

**FUT-001:** A complete editor rewrite, a new graph framework, full localization and unrelated configuration expansion are excluded from this plan. They are not completion gates. Adding configurable fields for the requested structure and guide behavior is mandatory, not promotion of this optional scope.

## 8. Non-Goals

**NG-001:** Do not upgrade Minecraft, NeoForge, Java, Gradle, mappings or unrelated dependencies solely for this change.

**NG-002:** Do not silently rewrite existing administrator configurations, remove ability enforcement, generate quests automatically or select a new hosting provider. Do not create reserved stage IDs, universal structure selectors or an implicit ability bypass. Do not publish Discord announcements. A version tag does not establish a GitHub Release object, and creating an additional release channel is not required by the selected endpoint.

## 9. Owner Decisions

### DEC-001 — Defaults movement policy

**Status:** RESOLVED  
**Selected choice:** Generated default configurations and new stages leave essential movement unrestricted. Administrators can explicitly configure movement restrictions.  
**Rationale:** The owner explicitly requires this default while retaining optional administrator controls. Remove generated jump, sprint, swim, climb and elytra restrictions; newly created stages have no ability locks.  
**Affected requirements:** CORE-REQ-003, CORE-REQ-006  
**Supersedes:** none

### DEC-002 — Optional configuration breadth

**Status:** RESOLVED  
**Selected choice:** Implement optional structure configuration, including numeric priorities, with compatible defaults and clear editor controls.  
**Rationale:** This configuration driven mod must let pack authors opt in without silently activating restrictions or replacing existing advanced facilities.  
**Affected requirements:** CORE-REQ-001, CORE-REQ-002, CORE-REQ-004, CORE-REQ-009  
**Supersedes:** none

### DEC-003 — Publication endpoint

**Status:** RESOLVED  
**Selected choice:** Publish the new mod release and deliver the updated existing web editor.  
**Rationale:** A local build or queued upload is not the requested endpoint.  
**Affected requirements:** CORE-REQ-008  
**Supersedes:** none

### DEC-004 — Version and player guidance

**Status:** RESOLVED  
**Selected choice:** Release version 3.1.0 includes author-written unlock instructions, next steps and location guidance configured in the editor and shown in the in-game stages menu.  
**Rationale:** Authors explain unlocking, next actions and locations; players need those instructions in game.  
**Affected requirements:** CORE-REQ-006, CORE-REQ-008, CORE-REQ-009  
**Supersedes:** none

### DEC-005 — Existing editor delivery target

**Status:** RESOLVED  
**Selected choice:** Update the existing mod-served web editor opened with /pstages editor and bundled in the 3.1.0 JAR. No separate public host is specified.  
**Rationale:** This is the identified existing implementation; no additional public website or provider is specified.  
**Affected requirements:** CORE-REQ-004, CORE-REQ-005, CORE-REQ-008, CORE-REQ-009  
**Supersedes:** none

### DEC-006 — Runtime placement and teardown

**Status:** RESOLVED  
**Selected choice:** Use node-1 only for headless builds, tests and dedicated servers. Use the verified Linux laptop for actual clients and graphical checks with isolated prelaunch silence, exact per-application mute, private multiplayer and complete owned-resource cleanup. Disposable server eula=true is already authorized.  
**Rationale:** Runtime fidelity and host safety are mandatory evidence boundaries.  
**Affected requirements:** CORE-REQ-001, CORE-REQ-003, CORE-REQ-005, CORE-REQ-007, CORE-REQ-008, CORE-REQ-009  
**Supersedes:** none

## 10. External Prerequisites

| ID | Prerequisite | Affected requirements | Availability | Authorization | Required external action |
| --- | --- | --- | --- | --- | --- |
| EXT-001 | Existing laptop desktop, discrete GPU hardware, audio tools and private host access | CORE-REQ-001, CORE-REQ-003, CORE-REQ-009, CORE-REQ-008 | available | authorized | Reverify the exact candidate renderer, stream, mute and joined world before client acceptance |
| EXT-002 | Existing mod-served web editor delivery | CORE-REQ-005, CORE-REQ-008, CORE-REQ-009 | available | authorized | Build assets, package them and exercise the published JAR |
| EXT-003 | Established CurseForge and Modrinth publication capability | CORE-REQ-008 | available | authorized | Select the approved 3.1.0 candidate, obtain a fresh concrete preview and verify both accepted public artifacts |
| EXT-004 | Existing Brave extension connection for editor interaction | CORE-REQ-004, CORE-REQ-005, CORE-REQ-009, CORE-REQ-008 | available | authorized | Select the laptop Brave family and exercise exact candidate editor controls |

Each prerequisite has kind `other`. EXT-001 evidence is the discovered host and runtime identity, desktop, discrete renderer, exact window and process correlation, per application mute readback, private endpoint readiness and both sides of joined world proof. Inventory alone is insufficient. EXT-002 evidence is the built asset digest, JAR entries, response identity and successful real editor workflow. EXT-003 evidence is broker inspection of CurseForge `1460273` and Modrinth `rY4mdP4Q`, candidate version and commit, SHA-256 and SHA-512, fresh preview binding, accepted metadata and matching downloaded hashes. The current configured 3.0.4 artifact is invalid for this release. EXT-004 evidence is actual browser interaction, focus behavior, console and request results from the existing extension connection.

No unresolved owner choice remains. A lost capability blocks only its dependent gates, never authorizes weaker evidence, new public networking, credential copying, a different host or a fictional publication operation. Headless independent work continues. Recheck authenticated merge access, signing and the required checks before phase integration as repository execution prerequisites.

## 11. Architecture and Ownership Boundaries

The server owns effective stages, accepted definitions, structure decisions and draft validation. The editor owns authored draft presentation and uses existing apply/review APIs. Common models contain only side safe data. Client caches and `StageTreeScreen` present received state; they never grant stages or decide server permissions. No client class may enter dedicated server classloading.

### IF-001: Structure configuration and attributed contributions

**Owner:** CORE-PHASE-000, CORE-REQ-001 and CORE-REQ-002.  
**Version:** Additive 3.1.0 fields in legacy stage files and schema 4 packages.  
**Acceptance:** CORE-AC-001, CORE-AC-002, CORE-AC-003.

| Field or interface | Type and default | Semantics and limit | Failure behavior |
| --- | --- | --- | --- |
| `structures.locked_entry` | Existing list of exact resource IDs; empty by default | Target list retained for compatibility, including protection only targets and existing priority annotations attached to exact IDs | Reject invalid IDs and unsupported tag, namespace or wildcard convenience selectors with the source key; do not silently accept ineffective targets |
| `structures.rules.entry_allowed` | Strict Boolean, default `false` | `true` removes only this owner's entry denial; `false` or omission gates entry while its stage is missing | Wrong type rejects the candidate snapshot |
| `structures.rules.priority` | Optional signed 32 bit integer; unconfigured effective default `0` | Inclusive range `-2147483648` through `2147483647`; explicit rule level priority for active player action restriction candidates, with omission retaining existing inherited priority | Reject fractional, string, Boolean and overflowing values with file and key |
| `prevent_block_break`, `prevent_block_place` within `structures.rules` | Strict Boolean, default `false` | True contributes denial for that action and exact target while the owner stage is missing; false contributes nothing | Wrong type rejects candidate; false never creates ALLOW |
| `prevent_explosions`, `disable_mob_spawning` within `structures.rules` | Strict Boolean, default `false` | Actorless protection for listed structures, independent of any nearby player or stage ownership | No invented actor; scoped true flags combine by OR only within the matching structure |
| Existing `entry_padding` locations | Existing nonnegative effective padding and default | Preserve accepted locations and existing normalization; apply only to an active entry denial, not a protection only contribution | Preserve documented compatibility, expose effective value and source |
| Contribution identity | Exact structure ID, owner stage ID, action, source file/key, priority and accepted revision | Immutable accepted snapshot; unique identity prevents compiled duplicates; no disk reads per tick | Invalid reload retains the last accepted snapshot |

Retain global `block_structure_entry`, existing creative and spectator bypasses, provider/session checks, and existing conditional controls. Item use is not newly denied by the convenience table. Implicit container, block and entity interaction restrictions arise only from an active entry denial, not from presence in the target list. A protection only stage can therefore permit entry and chest access while independently denying placement. A stage without grant mechanisms is an ordinary configured stage; administrator grant and revoke still work.

Both `LegacyStageCompiler` and `Schema4StageCompiler` must emit the same semantics as runtime aggregation. A convenience restriction must appear exactly once at its effective priority. Do not leave a synthesized `structures.enter` denial behind when `entry_allowed=true`. Keep protection only targets in lookup candidates and invalidate caches and safe positions correctly on accepted reload, dimension change and reconnect.

### IF-002: Actual restriction arbitration

**Owner:** CORE-PHASE-000, CORE-REQ-002.  
**Version:** Existing action rule semantics with attributed convenience candidates.  
**Acceptance:** CORE-AC-002, CORE-AC-003, CORE-AC-014.

For a real player action, select only contributions matching the target structure, action and missing owner stage. Combine those candidates with existing generic action rules through the actual `ConditionalLockEngine` path. Resolve effective priority through the existing `PriorityCascade`: explicit exact selector priority, explicit `structures.rules.priority`, existing category priority, stage priority, then global default. Absence of the new field preserves existing inherited settings; a completely unconfigured cascade resolves to zero. An explicit zero is a rule level choice and must not be confused with omission. The editor shows the effective value and inheritance source without writing an implicit zero during unrelated edits. Use this same resolved value in the contribution snapshot and compiler output, and prevent `Schema4StageCompiler.applySimpleMetadata` from decorating it a second time. Existing generic ALLOW and other advanced rules keep their existing cascade. Higher numeric priority wins; equal priority denial wins over ALLOW. Between equally effective denial candidates, use stable owner stage ID and source key ordering for provenance, never registration order. Explicit existing generic ALLOW above a convenience denial permits that static action; ALLOW below or tied with it does not. `entry_allowed=true` and false optional protection flags contribute no ALLOW at any priority. This is runtime arbitration, not display sorting.

For overlapping structures, evaluate each applicable structure and retain any resulting denial; an allow scoped to one structure cannot clear another's gate. Entry padding follows active entry denying contributions of the final denied structures. For equal highest priority entry denials within one structure, use their maximum padding; lower priority or suppressed denials do not enlarge it. Provider and session arbitration remains subsequent and cannot be bypassed by an empty static entry contribution or by an unrelated explicit allow. Preserve owner, dimension, bounds, instance and access stage checks. Actorless protection does not use player priority or pretend that false is a permit.

### IF-003: Canonical authored guide model and recommendation policy

**Owner:** CORE-PHASE-002, CORE-REQ-009.  
**Version:** Optional top level `[guide]` table in both supported stage representations.  
**Acceptance:** CORE-AC-011, CORE-AC-012, CORE-AC-013.

| Field | Type and default | Semantics | Validation and presentation |
| --- | --- | --- | --- |
| `guide.how_to_unlock` | String, `""` | Author instructions for obtaining this stage | At most 2048 Unicode code points and 8192 UTF-8 bytes |
| `guide.next_steps` | String, `""` | Concrete actions to work toward this stage | Same independent limits |
| `guide.where_to_find` | String, `""` | Author supplied location or discovery hints | Same independent limits |
| `guide.recommendation` | String enum `auto`, `include`, `exclude`; default `auto` | Controls recommendation inclusion, never stage ownership or reveal permissions | Unknown enum or wrong type rejects candidate |
| Existing `stage.description` | Existing string and behavior | General description and tooltip content remain independent | No migration into or replacement by guide fields |

Guide strings are inert Unicode multiline text. Preserve content, line breaks and authored language without command execution, HTML interpretation, click events or automatic external navigation. Reject invalid encoding and control characters other than line feed, carriage return and tab with a source key; do not silently truncate stored content. Treat whitespace only strings as empty for recommendation decisions while preserving the authored value. Render literal formatting markers without interpreting command or formatting syntax. Enforce identical code point and byte limits in parser, draft validation, editor counters, encoding and decoding. Unknown future keys are preserved by targeted source edits and import/export; they do not acquire executable meaning.

A recommendation candidate must pass all existing hidden/reveal visibility checks, be unowned and satisfy the existing ANY, ALL or AT_LEAST prerequisite policy. `exclude` always removes it from recommendations. `include` allows it through this recommendation filter only. `auto` includes it only when it has an existing declared trigger grant route or at least one nonblank guide field. This avoids advertising an empty manual only protection sentinel as an actionable objective without guessing that externally granted stages are impossible. Explicit include permits manual progression; explicit exclude permits authored private sentinel notes. Neither bypasses visibility.

Show every qualifying branch in stable ascending `uiSortOrder`, category, display name and stage ID order, using locale independent string comparisons and stage ID as the final tie breaker. Do not choose a forced unique next stage. Label the section What to do next and identify its candidates as prerequisites met, not guaranteed immediately unlockable. Reuse existing slot, cost and trigger information to explain unmet acquisition conditions without replacing author text or bypassing server validation. Selecting a candidate focuses its existing node and inspector. A normally visible selected stage may show its guide even when it is not recommended or already owned. No guide, search result, cross reference, category summary, aggregate count or empty state may disclose a hidden or unrevealed stage through presentation. This retains the existing visibility policy and does not claim that the existing full definition sync provides cryptographic secrecy from a modified client. Empty guidance means no invented instructions; an empty recommendation list says no suggested stages are available, not that all content is complete.

### IF-004: Guide persistence, snapshot and network boundary

**Owner:** CORE-PHASE-002, CORE-REQ-009.  
**Version:** Network registration changes from `2` to `3`; schema 4 optional metadata remains additive.  
**Acceptance:** CORE-AC-011, CORE-AC-012, CORE-AC-013.

The canonical immutable guide model flows through `StageDefinition`, `StageFileParser`, schema 4 compiled display metadata, `NetworkHandler.sendStageDefinitionsSync`, `StageDefinitionEntry`, `handleStageDefinitionsSyncClient`, `ClientStageCache.StageDefinitionData` and `StageTreeScreen`. Both existing display consumers receive the same accepted guide values; no separate mutable guide authority is introduced. The existing server supplied `hasTriggers:boolean` field supplies the auto policy's declared trigger route test. Preserve that bit through both display consumers; do not send private trigger definitions or ask the client to infer external integrations. Authored nonblank fields supply the other auto inclusion condition. Apply and reload publish definitions transactionally. Invalid type, size, enum or encoding leaves the prior accepted runtime state intact and reports the exact source key to the editor and console.

Protocol 3 bounds every guide string before allocation and rejects malformed payloads. Old protocol 2 peers fail negotiation with a useful version mismatch rather than decoding appended bytes. Definitions carry a nonnegative accepted definition revision; a completed full snapshot atomically replaces the client definition/guide state. Progression and team state continue through existing authoritative sync and trigger candidate recomputation on receipt. Do not compare unrelated revision counters. Reconnect clears stale state before receiving a complete current snapshot. Reload, grant, revoke, team changes and screen refresh recompute against the accepted definition snapshot and current ownership.

Preserve existing payload and definition limits; add explicit validation of the encoded definition payload against the transport's actual maximum before send. If the new fields make a supported pack exceed that maximum, bounded chunking under protocol 3 is required instead of silent omission, client crash or a new arbitrary stage count limit. Chunks carry definition revision, zero based index and total count; bound count and cumulative bytes to the validated full snapshot size, discard incomplete or superseded assembly after 200 client ticks, and apply only after complete validation. The phase verifies the pinned transport limit from source, records it in tests and documentation, and does not assume a limit from another Minecraft version. Existing valid packs with empty guides remain loadable.

### IF-005: Editor and default generation boundary

**Owner:** CORE-PHASE-001 for controls and defaults; CORE-PHASE-002 contributes guide controls.  
**Version:** Existing draft and source preservation APIs.  
**Acceptance:** CORE-AC-004 through CORE-AC-008, CORE-AC-011.

`Progressivestages.generateDefaultStageFilesIfNeeded` and `StageFileLoader` fallback both use the same revised showcase policy. Preserve nonmovement examples and the established package layout. A discovered invalid file is not an empty installation and must not trigger destructive regeneration. Existing files are never rewritten based only on a showcase name. New editor stages have no ability restrictions; existing `EffectsPanel` controls retain explicit opt in restrictions.

Use Stage name, Stage icon and Required stages as plain labels with brief player relevant help and an example. Expose entry allowed, independent protections and signed priority with help explaining absence of denial versus explicit ALLOW. Retain source editing and generic advanced rules. Add guide fields with How to unlock, Next steps, Where to find and recommendation controls in the existing stage form, with inert preview, counters and the visibility caveat.

Add a selected node toolbar beside the node and a right click menu with Edit, Connect, Duplicate and Delete from draft where supported by the existing APIs. Reuse `StageActions`, `StageDialogs`, `EditorContext` and cycle validation. Provide keyboard context invocation, visible pointer controls, focus return, Escape and outside dismissal, viewport clamping and safe pan/zoom. Right click must not drag, save a position, connect an edge or delete anything. Preserve confirmation, review/apply, failed save and stale draft recovery. Do not offer destructive legacy or archived operations that the existing server rejects. Rename and duplicate preserve guide fields, comments and unrelated advanced TOML.

### Security and failure taxonomy

Keep editor authorization, session ownership, permission level checks, path validation, server side draft validation and source preservation. A browser preview cannot grant permissions or publish a draft. Do not log session URLs, access tokens, credentials, whole configuration files or unrelated player data. Diagnostic capture is operator only and never changes the action it observes.

Configuration rejection retains the previous snapshot. A stale editor revision retains unsaved work and reports a conflict through the established flow. Missing renderer, audio or browser capability leaves a named gate unverified. A protocol mismatch rejects connection clearly. A partial platform publication records the accepted file and retries only the missing target after checking identity; it does not overwrite or delete an accepted public file silently. No acceptance statement may conflate these conditions with success.

## 12. Requirements

### CORE-REQ-001 — Independent structure entry and protection

**Behavior:** Separate entry and protection stages produce independent player actions on their listed structures.  
**Owner:** CORE-PHASE-000  
**Contributors:** Compilers, diagnostics, documentation.  
**Dependencies:** none  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

1. CORE-AC-001: In a two stage fixture, grant only the entry stage. The player enters and opens an otherwise permitted chest while the missing protection stage still denies placement. Entry allowed on that protection stage never erases another entry gate.
2. CORE-AC-002: Break, place, entry, containers and interactions follow IF-001 and IF-002 across distinct and overlapping structures, grant, revoke and reload. Actorless explosion and spawn behavior remains scoped without a fictional player.

**Required evidence**

- Parser and compiler parity tests, real `StructureEnforcer` server event fixtures, state assertions before and after grants, and silent laptop multiplayer entry, chest and placement evidence. A helper returning an expected Boolean alone is insufficient.

### CORE-REQ-002 — Deterministic compatible rule composition

**Behavior:** Optional priority controls actual player action arbitration while preserving existing explicit override, global and provider/session boundaries.  
**Owner:** CORE-PHASE-000  
**Contributors:** Loader, editor controls, documentation.  
**Dependencies:** CORE-REQ-001  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

1. CORE-AC-003: Omitted fields preserve legacy entry gating and existing priority inheritance. Explicit zero and existing exact selector overrides follow IF-002. Invalid Boolean types and signed priority boundaries reject candidates without replacing the accepted snapshot. No duplicate compiler contribution or registration order dependence remains.
2. CORE-AC-014: Real actions prove ALLOW above a convenience denial permits that static action, ALLOW below or tied does not, and provider/session or another structure denial remains effective. False optional flags and entry allowed never act as ALLOW. Padding follows the final entry denial contract.

**Required evidence**

- JUnit permutations, legacy and schema 4 compilation comparisons, actual server break/place/entry and actorless events, provider/session negative fixtures, repeated reload and rollback assertions, and diagnostic winner records with source provenance.

### CORE-REQ-003 — Safe movement defaults and preserved explicit restrictions

**Behavior:** Generated defaults and new editor stages do not lock essential movement; existing explicit restrictions remain effective.  
**Owner:** CORE-PHASE-001  
**Contributors:** Ability enforcement, diagnostics, documentation.  
**Dependencies:** CORE-REQ-007  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

1. CORE-AC-004: Both generation entry points produce no jump, sprint, swim, climb or elytra locks. Empty install, package only, restart, archived and malformed discovery fixtures do not create unexpected active restrictions or overwrite existing files.
2. CORE-AC-005: Explicit ability locks still deny their configured actions and clear after a proper grant. Existing packs remain byte unchanged unless the administrator explicitly edits their draft; documented recovery locates the actual old restriction.

**Required evidence**

- Extended `DefaultShowcaseStagesTest`, loader discovery tests, `AbilityEnforcerTest`, client ability state checks and silent laptop actual movement with unrestricted defaults and an explicit restriction negative case. Correlate server decision, received ability state and observed action.

### CORE-REQ-004 — Plain editor labels and configuration help

**Behavior:** Authors understand stage identity, prerequisites, entry/protection controls and ability restrictions without losing advanced editing.  
**Owner:** CORE-PHASE-001  
**Contributors:** Source preservation, documentation.  
**Dependencies:** CORE-REQ-001, CORE-REQ-002, CORE-REQ-003  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

1. CORE-AC-006: The labels and help in IF-005 are visible, describe player effects accurately and expose the new structure settings without changing serialized identity.
2. CORE-AC-007: Targeted edits preserve unrelated comments, advanced rules and unknown future fields through review, apply, export and reload. Invalid priority or type displays the exact field error and does not publish.

**Required evidence**

- Existing editor component and TOML preservation tests extended with new fields, authenticated Brave form editing against the candidate server, validation errors, network outcomes and packaged asset inspection.

### CORE-REQ-005 — Nearby safe graph actions

**Behavior:** Authors edit and manage selected stages directly beside graph nodes using pointer or keyboard while retaining the existing draft workflow.  
**Owner:** CORE-PHASE-001  
**Contributors:** EditorContext, permission validation, browser verification.  
**Dependencies:** CORE-REQ-004  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

1. CORE-AC-008: Toolbar and context actions satisfy IF-005 at different zoom levels and viewport edges. Delete requires confirmation and changes only the draft. Cancel, close, right click and failed save produce no unintended mutation.
2. CORE-AC-009: Connect retains cycle prevention; drag, pan, edge selection, keyboard focus and review/apply still work. Unsupported legacy/archived actions remain unavailable with useful feedback.

**Required evidence**

- Focused React interaction regressions and actual Brave pointer and keyboard workflows with request/draft inspection, console error review, targeted screenshots and server apply outcome.

### CORE-REQ-006 — Accurate configuration and recovery documentation

**Behavior:** Current documentation explains structures, movement, priority, guides, validation, reload and recovery using working examples.  
**Owner:** CORE-PHASE-003  
**Contributors:** CORE-PHASE-000, CORE-PHASE-001, CORE-PHASE-002.  
**Dependencies:** CORE-REQ-001, CORE-REQ-002, CORE-REQ-003, CORE-REQ-004, CORE-REQ-005, CORE-REQ-007, CORE-REQ-009  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

1. CORE-AC-010: README, detailed documentation, affected topics and index links reflect implemented facts. The two stage example, actorless boundary, explicit ALLOW priority example, old movement recovery and authored branch guide parse and behave as documented.
2. Documentation records protocol 3 matching client/server requirements, empty guide behavior, hidden/reveal limits and release support collection. Wiki publishes only merged behavior.

**Required evidence**

- Executable parser/behavior checks using the documented examples, link and command review, release walkthrough against the actual JAR and post merge wiki comparison. Contributor phases deliver their relevant documentation before their own exits.

### CORE-REQ-007 — Bounded diagnostics of actual decisions

**Behavior:** Operators can collect a short scoped explanation of actual decisions with provenance and final outcomes, including console only structure events.  
**Owner:** CORE-PHASE-000  
**Contributors:** CORE-PHASE-001 for default/editor observations, CORE-PHASE-002 for guide observations, CORE-PHASE-003 for released artifact support proof.  
**Dependencies:** none  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

1. CORE-AC-015: The shared diagnostic controls, bounds, signals and numbered procedure below pass permission, lifecycle, missing target, limit, privacy, overhead and unchanged gameplay assertions.
2. Diagnostics identify legacy versus compiled contributions, effective priorities, provider/session reasons and accepted revision. Later contributor observations reuse the same capture identity, limits and output contract rather than creating competing systems.

**Required evidence**

- Command dispatcher and manager tests, no player console scenario, actual evaluator regressions and the existing dedicated capture overhead fixture extended for new recording paths. Actual client reception and UI still require separate client evidence.

### CORE-REQ-008 — Verified 3.1.0 delivery

**Behavior:** ProgressiveStages 3.1.0 is integrated, published to both established platforms and verified from the released JAR with its updated editor and guide.  
**Owner:** CORE-PHASE-003  
**Contributors:** Every phase, documentation and platform broker.  
**Dependencies:** CORE-REQ-001, CORE-REQ-002, CORE-REQ-003, CORE-REQ-004, CORE-REQ-005, CORE-REQ-006, CORE-REQ-007, CORE-REQ-009, EXT-001, EXT-002, EXT-003, EXT-004  
**Lifecycle stage:** post-change  
**Production verification:** nondestructive  
**Release impact:** stable release

**Acceptance criteria**

1. CORE-AC-016: Every phase completes required tests, checked PR integration into actual `master`, resulting default branch verification and a signed annotated phase tag before the next begins. No mandatory defect or unverified required gate remains.
2. CORE-AC-017: Exact 3.1.0 approved artifacts, embedded assets and metadata match both accepted platform files and downloaded SHA-256/SHA-512. The published JAR serves the new editor, supports the guide workflow and passes production dedicated startup.
3. Issue 57 and release documentation distinguish verified fixes from unavailable reporter evidence; wiki and tracking reflect the merged released result. No stale broker candidate, queued publication or tag alone counts as delivery.

**Required evidence**

- Required CI and local suites, source/asset/JAR identity, integration and tag verification, fresh broker preview and accepted results, public metadata and downloaded hashes, published artifact server/browser/silent laptop smoke, documentation/wiki/tracking receipts and cleanup proof.

### CORE-REQ-009 — Authored next stage guidance in game

**Behavior:** Authors create guide text in the GUI and players see safe relevant instructions and branch choices in the actual stages menu.  
**Owner:** CORE-PHASE-002  
**Contributors:** Editor, compilers, diagnostics, documentation.  
**Dependencies:** CORE-REQ-004, CORE-REQ-005, CORE-REQ-007  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

1. CORE-AC-011: Guide fields follow IF-003 through legacy and schema 4 parse, GUI edit, import/export, rename, duplicate, draft apply and restart. Description and unknown fields remain intact; invalid and oversized data is rejected transactionally.
2. CORE-AC-012: Multiple visible branches are stably ordered, manual only empty sentinels are absent under auto, explicit include/exclude work, and hidden or unrevealed stages never leak through guide presentation. Empty state does not claim completion.
3. CORE-AC-013: Protocol 3, current snapshots and reconnect/reload/grant/revoke/team updates preserve coherent guide state. Long Unicode text wraps and scrolls at compact GUI scales with keyboard access, readable contrast and no reliance on color alone.

**Required evidence**

- Parser/editor preservation and boundary tests, packet codec and mismatch/size tests, visibility/order policy tests, dedicated state transition fixtures, and silent laptop actual stages menu captures showing multiple branches, authored locations, hidden absence, long text and changed ownership. Brave proof must edit and apply the text later observed in the player UI.

## 13. Phased Roadmap

| Phase ID | Objective | Owner | Dependencies | Canonical requirements | Entry summary | Exit summary | Next transition | Execution blueprint |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| CORE-PHASE-000 | Deliver independent structure contributions and actual decision diagnostics | Structure enforcement | EXT-001 | CORE-REQ-001, CORE-REQ-002, CORE-REQ-007 | Validate approved master, capabilities and exact fixtures; create matching milestone and branch | Structure contracts, shared diagnostics, real server and residual client proof, documentation, checked merge, resulting master and signed tag pass | CORE-PHASE-001 | [Phase 000](phases/plan-phase-000.md) |
| CORE-PHASE-001 | Repair defaults and targeted editor usability | Editor | CORE-PHASE-000, EXT-001, EXT-002, EXT-004 | CORE-REQ-003, CORE-REQ-004, CORE-REQ-005 | Phase 000 merged, verified and tagged; IF-001, IF-002 and shared capture available | Movement and graph/form proofs, preserved explicit restrictions, documentation, checked merge, resulting master and signed tag pass | CORE-PHASE-002 | [Phase 001](phases/plan-phase-001.md) |
| CORE-PHASE-002 | Deliver the authored player guide across configuration, GUI and sync | Player guide | CORE-PHASE-001, EXT-001, EXT-002, EXT-004 | CORE-REQ-009 | Phase 001 merged, verified and tagged; existing editor and shared capture stable | Guide data, protocol, visibility, branch and real menu proofs, documentation, checked merge, resulting master and signed tag pass | CORE-PHASE-003 | [Phase 002](phases/plan-phase-002.md) |
| CORE-PHASE-003 | Publish and verify the complete 3.1.0 release | Release integration | CORE-PHASE-002, EXT-001, EXT-002, EXT-003, EXT-004 | CORE-REQ-006, CORE-REQ-008 | Phase 002 merged, verified and tagged; full feature evidence and broker mappings available | Complete regression and documentation, checked merge and tag, exact approved artifact publication, live editor/guide proof and plan wide closure pass | Final completion under Section 18 | [Phase 003](phases/plan-phase-003.md) |

The catalog is contiguous and frozen at four phases. No readiness only phase or future design phase is reserved. Every phase has one mandatory registered blueprint and a unique plan ID `PLAN-PHASE-000` through `PLAN-PHASE-003`. Each phase plan depends on `PLAN-MASTER`; subsequent phase plans also depend on the preceding phase plan. The phase files contain the sole full phase declarations. No phase starts while a preceding PR is merely open, approved or queued.

### Ordered phase briefs and reserved task identities

These identities freeze ownership for diagnostics, risks and evidence. The phase blueprints expand these exact work packages without moving canonical requirements.

| Phase | Ordered work packages | Required completion packet |
| --- | --- | --- |
| 000 | P000-TASK-001 verifies baseline, owned workspace, pins, capabilities and failing fixtures. P000-TASK-002 extends existing shared diagnostic controls and real structure/ability/editor observations. P000-TASK-003 implements attributed parse/model/compiler snapshots and validation. P000-TASK-004 implements actual priority, entry, protection, actorless and provider/session composition. P000-TASK-005 proves unit, headless event and residual silent client workflows. P000-TASK-006 updates structure/support documentation and completes integration gates. | Candidate and configuration identities, CORE-AC-001 through CORE-AC-003, CORE-AC-014 and shared CORE-AC-015 results, diagnostic examples, cleanup receipt, PR checks, resulting master and signed tag |
| 001 | P001-TASK-001 verifies prior integration and identifies both default paths and existing draft components. P001-TASK-002 changes generated movement defaults and new stage behavior while preserving existing files. P001-TASK-003 adds plain labels, help and structure controls. P001-TASK-004 adds nearby graph and keyboard actions using existing APIs. P001-TASK-005 extends shared observations and proves movement, source preservation and browser workflows. P001-TASK-006 updates affected guidance and completes integration gates. | CORE-AC-004 through CORE-AC-009 results, exact editor asset identity, browser and movement evidence, explicit restriction negative case, cleanup receipt and integration proof |
| 002 | P002-TASK-001 verifies prior integration and guide pipeline/transport bounds. P002-TASK-002 implements canonical guide parsing, validation, source preservation and editor fields. P002-TASK-003 implements protocol 3 and coherent accepted guide snapshots. P002-TASK-004 implements visible branching recommendations and accessible inspector guide presentation. P002-TASK-005 extends shared progression observations and proves boundary, reveal, reconnect, browser and silent menu workflows. P002-TASK-006 updates guide/protocol documentation and completes integration gates. | CORE-AC-011 through CORE-AC-013 results, matching server/client hashes, guide text round trip and hidden absence proof, protocol mismatch proof, cleanup receipt and integration proof |
| 003 | P003-TASK-001 converges documentation and examples against implemented features. P003-TASK-002 prepares version 3.1.0, asset packaging and complete regression/security/compatibility evidence. P003-TASK-003 completes checked release phase integration, resulting master verification and signed tag. P003-TASK-004 selects exact approved candidate, obtains fresh broker preview and publishes both platform files. P003-TASK-005 verifies accepted metadata, downloads, production startup and released editor/guide workflows. P003-TASK-006 publishes merged wiki, reconciles issue 57 and tracking, closes evidence and cleanup, and checks plan wide Definition of Done. | CORE-AC-010, CORE-AC-016 and CORE-AC-017 results, both public file identities and hashes, live artifact acceptance, documentation/wiki/tracking receipts and zero unexplained owned leftovers |

For each phase, update the matching milestone before implementation and keep its issues and PR assigned. Use a focused `envy/` phase branch from verified approved `origin/master`. Commit and push completed changes with the sole configured owner identity and registered SSH signature, no coauthors. Require `quality / gradle`, `quality / node` and `quality / secret scan`, together with any newly observed required check. After local verification and deterministic checks pass, obtain one private independent review under the existing phase review workflow; resolve actionable findings. Do not use public review mentions. Replacing that review is necessary only after material changes invalidate it.

Integrate through GitHub's merge commit function. If checks remain pending and repository auto merge is available, queue merge commit auto merge and wait for actual merged state. Never bypass a required check or substitute direct pushes, squash or rebase. Verify the resulting default branch commit and affected behavior, create a signed annotated phase tag on that merge commit and push the tag. Preserve historical phase branches and tags. No future phase branch is created before these gates pass. If a saved execution cursor exists during implementation, its only permitted advancement is one contiguous phase after the complete gate receipt; this plan does not create a goal or cursor.

## 14. Verification Strategy

### Execution Hosts

| Workload or gate | Execution host | Required capabilities and launch configuration | Candidate identity and runtime directory | Evidence |
| --- | --- | --- | --- | --- |
| Unit, formatting, static checks, builds, data generation and JAR inspection | `node-1` | Java 21, checked wrapper and existing npm scripts; inspect task dependencies to exclude clients | Exact phase commit and discovered checkout; isolate scratch output and record preexisting paths | Commands, decisive test results, asset/JAR hashes and cleanup |
| World dependent server rules and diagnostic fixtures | `node-1` | Dedicated no GUI NeoForge server or verified server GameTest task graph; owned standard input console; `eula=true` readback | Disposable child runtime under verified main project anchor; exact classes and candidate | Ready log, actual event assertions, bounded captures and teardown |
| Actual movement, entry, chest/placement, guide rendering and sync | Linux laptop with `node-1` server | Active Hyprland, actual discrete GPU renderer, authorized private connection and two layer audio isolation | Discover laptop project anchor and isolated instance; match commit, JAR/dependency hashes, loader and relevant config | Both sides of exact world join, client/server state and targeted visuals |
| Real web editor pointer/keyboard, focus and served asset behavior | Linux laptop Brave | Existing browser extension connection, selected Brave family and authorized editor session | Exact candidate served by owned runtime, recorded asset digest; no session secrets in evidence | Actions, draft state, console/request results and targeted visuals |
| Released artifact smoke | Both hosts according to workload | Same constraints using downloaded accepted artifact; production startup distinct from development GameTests | Public file identity plus SHA-256/SHA-512 matching approved candidate | Published editor/guide behavior and clean teardown |

Before allocating a child workspace or runtime, reuse the applicable existing checkout, verify its main project anchor, and read the project workspace procedure. Register parent Git, build, index and packaging exclusions. New project workspaces are nested children of that host's verified anchor, never sibling clones or copies. Discover the laptop location; do not translate a `node-1` path by guess. Protect unrelated existing worktrees and runtimes.

For each bounded suite, register exact hosts, processes, files and teardown before launch. Record preexisting output when a reused path is unavoidable. Keep only source, tracked fixtures, required sanitized evidence and requested final artifacts. Stop only owned processes, wait for exit, then remove exact disposable output after its last consumer. Include temporary worlds, logs, crash reports, screenshots, traces, downloads, configuration, databases, reports, coverage, incidental bytecode and test only build output. Do not remove shared caches, personal instances, saves, source changes or unrelated data. Use Git aware worktree teardown only for a verified completed owned worktree. Recheck paths without following symlinks; no broad globs, process kills or blanket cleaning. Confirm resource absence on every used host. Failed cleanup remains cleanup incomplete even when tests pass. A read only audit confirms that it created no files or processes.

For Minecraft servers, configure the exact disposable `eula.txt` to one effective `eula=true` and read it back before launch. Consent is already resolved. For server observable behavior use console fixtures and actual event GameTests first. Confirm the configured server task graph has no renderer or client. Existing production NeoForge launch does not run development GameTests; compare tested classes with the packaged candidate and separately prove production startup.

For actual in world clients, discover the private endpoint, port, standard input console and both runtime directories. Confirm server readiness and laptop reachability. Verify host, desktop and discrete GPU path before launch, then verify the actual running renderer from client evidence before acceptance. Set the isolated client's pinned version master audio to zero before startup. Bind exact window address, class, title and PID using `hyprctl clients -j`, correlate only that process tree to its playback stream and mute only that stream with `wpctl` or `pactl`. Read back muted state before interaction. A bounded owned watcher must immediately mute and verify replacement streams after reconnect, reload or device changes. Never mute a system sink, microphone, other application or personal client. If identity or mute cannot be proven, stop the owned client and leave the gate unverified.

Use pinned launcher supported direct connect or quick play to the verified endpoint; inspect support before selecting arguments. Authorized desktop controls can complete automatic connection when launch options cannot. Verify the intended player joined the exact server on both sides and entered its world before assertions. Console fixture setup cannot replace player actions or bypass tested permissions. Do not alter a personal server list, weaken authentication, expose ports or copy credentials. No client runs on `node-1`, including virtual display, offscreen or software renderer workarounds. Singleplayer is allowed only for an identified integrated server bug with its requirement, rationale and separate cleanup; none is currently required by this plan. Connection failure is not such an exception.

### Evidence Coverage

| Requirement | Unit | Integration | Real behavior | Security | Artifact or runtime |
| --- | --- | --- | --- | --- | --- |
| CORE-REQ-001 | Entry/protection contributions | Both compiler routes and actual enforcer events | Two stage entry/chest/place fixture | Preserve bypass and provider boundaries | Dedicated and silent client result |
| CORE-REQ-002 | Priority permutations and exact selector validation | Conditional winner and session checks | Explicit ALLOW above/below/tied, actorless events | Overflow, malformed inputs and no fictional actor | Correlated final decisions and reload identity |
| CORE-REQ-003 | Defaults and existing file preservation | Both discovery/generation paths | Jump, sprint, swim, climb and elytra defaults; explicit negative case | No unauthorized config rewrite | Server/client ability state and action |
| CORE-REQ-004 | Form/source preservation | Draft validate/review/apply | Authenticated Brave edits | Invalid type, stale revision and session permissions | Packaged UI assets |
| CORE-REQ-005 | Gesture, menu and focus regressions | Existing actions and cycle validation | Brave pointer/keyboard and cancellation | Destructive confirmation and server restrictions | Draft mutation evidence |
| CORE-REQ-006 | Parse documented examples | Follow exact reload/recovery instructions | Guide and structure walkthroughs | Redacted support instructions | Accurate docs/index/wiki |
| CORE-REQ-007 | Bounds, reset and permission tests | Actual recorders and console targets | Real evaluator decisions | Default off, privacy and permission denial | Bounded support packet and overhead results |
| CORE-REQ-008 | Full existing affected suites | Checked merged candidate and production start | Released editor and guide smoke | Final diff and dependency/source review | Both public file metadata and hashes |
| CORE-REQ-009 | Text, policy and codec boundaries | Draft to accepted synced revision | Actual branching guide and state refresh | Hidden absence, inert content, protocol mismatch | Published JAR and client menu evidence |

Run `npm ci`, `npm run check`, `npm test` and `npm run build` in `editor-ui` for relevant editor changes and final candidate packaging. Run applicable formatting checks actually present, `./gradlew test`, `./gradlew build`, and relevant verified server GameTests after Java changes. Inspect dedicated server classloading. Run the existing data generation path and inspect generated drift when providers or generated resources change. Do not claim an undeclared formatter ran. Retain decisive results, test counts, candidate/host identity and required screenshots under `docs/verification/3.1.0/`; this is an evidence destination, not a runtime or raw log archive.

## Diagnostics and Debugging

**Requirement IDs:** CORE-REQ-007, CORE-REQ-001, CORE-REQ-002, CORE-REQ-003, CORE-REQ-004, CORE-REQ-005, CORE-REQ-008, CORE-REQ-009  
**Task IDs:** P000-TASK-002, P000-TASK-005, P001-TASK-005, P002-TASK-005, P003-TASK-005  
**Controls:** Extend existing permission level 3 `/stage debug <category> on <player>`, `status` and `off`; exact proposed structure and ability controls, console targeting and reset behavior are defined below.  
**Signals:** Use the typed decision, configuration, editor and guide observations in the local table, linked by capture ID, event sequence and accepted definition revision.  
**Collection procedure:** Follow the numbered procedure below with the exact capture path returned by status; stop and verify inactivity before retaining sanitized evidence.  
**Headless verification:** Use actual `StructureEnforcer` action/actorless entry points, loader apply, `AbilityEnforcer`, editor draft validation and command dispatcher in verified dedicated server GameTests; use the existing `captureoverheadstaysboundedacrosscategories` fixture for real recording overhead.  
**Client verification:** Real laptop evidence closes entry response, movement input, received guide revision and rendering; Brave evidence closes form/graph gestures. Server fixtures alone cannot close those claims.  
**Client audio isolation:** Before launch set the isolated Minecraft instance master audio to zero, bind its exact Hyprland window and PID, correlate only its owned playback stream through PipeWire or PulseAudio, mute it with `wpctl` or `pactl`, verify and read back mute before actions, immediately mute and verify every replacement stream, then stop the owned client and watcher and verify stream removal at teardown.  
**Budgets and privacy:** Preserve the concrete shared limits below, default off behavior, permission checks, source redaction, lazy recording and unchanged gameplay; stop safely on limit, writer failure or lost target.  
**Regression and support:** Extend manager, command, actual evaluator and overhead tests; keep reusable capture in the release, document collection in `docs/test/diagnostics.md` and practical recovery in `docs/troubleshooting/easy-builder.md`, and retain only sanitized evidence in `docs/verification/3.1.0/`.

### Shared controls and typed signal contract

The existing manager is `server/enforcement/InteractionCaptureManager.java`. Existing categories are `interactions`, `progression`, `permissions` and `editor`. Their source presence is observed; no research runtime pass is claimed. Phase 000 delivers the proposed `structures` and `abilities` categories in that same manager, with no second global recorder. Reuse existing output and lifecycle behavior while extending actual evaluator provenance. Phase 002 adds guide fields under `progression`, not another independently controlled logger. The Phase 000 exit proves controls and existing structure, ability and editor paths; guide specific instrumentation and proof are owned exclusively by CORE-REQ-009 in Phase 002.

For player scope, proposed commands are `/stage debug structures on <player>` and `/stage debug abilities on <player>`, with `/stage debug structures status`, `/stage debug structures off` and corresponding ability forms. Existing category commands remain compatible. Add the explicit console capable actorless form `/stage debug structures on structure <dimension_id> <structure_id>`. Its target is exactly one registered structure ID in one dimension and does not require a player. For other player scoped captures the console supplies the actual fixture/player argument; it never calls an implicit player source getter. Missing player, unknown dimension or unknown structure fails without starting capture. The console omits the leading slash. All controls retain permission level 3; unauthorized clients cannot enable capture. One capture is active globally; enabling a second returns busy without replacing the first.

Status reports active or draining/off state, logical side, category, explicit target, capture ID, remaining seconds/ticks, event and byte limits, accepted/dropped/truncated counters and exact output path. `off` is idempotent, stops new records immediately and exposes writer drain until complete. Timeout, reload, shutdown, player disconnect/removal and invalidated dimension/structure scope stop capture. Restart never resumes capture. A valid actorless capture is not stopped merely because no players are online.

IF-006 is the shared diagnostic interface, owned by CORE-PHASE-000 and CORE-REQ-007, version `1` of the extended event schema, accepted by CORE-AC-015. Preserve limits of 60 seconds, 200 accepted events, 20 events per second, 128 KiB output including identity header, a queue of 256 records, strings of at most 256 UTF-16 units under existing safe truncation, and collections of at most 32 entries. Preserve an explicit total/truncated count where the displayed contributor list is shorter. A reached rate, event or byte bound stops with a reason rather than expanding scope. No tick/render thread blocks on disk writes. Capture identity records version, candidate artifact hash, loader, schema, side and relevant sanitized configuration fingerprint once. Event text never includes full guide content, editor credentials, private addresses, chat or unrelated records.

| Signal | Source and unit | Expected observation |
| --- | --- | --- |
| `capture_id:string`, `sequence:long`, `event:string`, `side:enum`, `tick:long`, `definition_revision:long` | Existing manager, server tick and accepted snapshot; identifiers and ticks | Sequence is monotonic within one capture; events identify their accepted definition revision without assuming synchronized clocks |
| `action:string`, `structure_id:resource_id`, `dimension_id:resource_id`, `actor_scope:enum`, `owner_stage:stage_id?`, `owned:boolean?` | Actual StructureEnforcer final path; IDs and Boolean state | Player and actorless scopes are explicit; actorless ownership is absent, not false for a fabricated player |
| `contributors:list`, `priority:int32`, `effect:enum`, `source_path:string`, `source_key:string`, `winner:string?`, `static_result:enum`, `provider_result:enum`, `session_reason:string`, `final_result:enum` | Actual static, conditional and provider/session evaluations; bounded records | Entry true emits no denying candidate; priorities predict actual effect; source paths are configuration relative; final result matches action outcome |
| `ability:string`, `gate_source:enum`, `missing_stages:list`, `locked:boolean`, `sync_changed:boolean` | AbilityEnforcer and outbound ability state; values and counts | Empty defaults have no blocker; explicit legacy or compiled blockers have provenance; outbound changes correlate to state changes |
| `operation:string`, `draft_revision:string`, `validation_result:enum`, `error_key:string?`, `applied_revision:long?` | Existing editor capture around actual validation/apply; revisions and outcome | Bad input or stale draft never publishes; successful apply identifies the accepted runtime revision without raw source/session data |
| `guide_policy:enum`, `candidate_count:int`, `visible_candidate_ids:list`, `guide_lengths:list<int>`, `sync_revision:long`, `reason:enum` | Phase 002 progression extension after visibility filtering; code point counts and IDs | Auto/exclude/owned/prerequisite reasons match visible state; counts and summaries include only visible candidates and never disclose hidden totals |
| `records:int`, `bytes:int`, `dropped:int`, `truncated:int`, `stop_reason:enum`, `record_cpu_ns:long`, `record_elapsed_ns:long`, `disabled_bytes:long` | Manager and existing overhead fixture; counts, bytes and nanoseconds | Limits stop predictably, disabled allocation is zero and measured overhead remains within the existing fixture budget |

Illustrative event only, not executed evidence: a structure placement decision with capture ID `sample`, sequence `4`, owner stage `example:protection`, `owned=false`, priority `0`, static and final result `deny`, and source key `structures.rules.prevent_block_place`. The paired entry decision for that same protection stage has no convenience entry contributor. A higher priority advanced allow changes the static result only when it matches the action and structure and no subsequent provider/session or overlapping structure denial remains.

The existing overhead procedure discards a 1000 attempt warmup and uses three 1000 attempt measured rounds with real accepted capture windows. Preserve its maximum added CPU and elapsed cost of 100000 nanoseconds per attempt and exactly zero allocation on disabled recording paths. Extend the fixture for new categories without hiding inactive or rejected attempts inside enabled measurements. Test slow writer backpressure, output failure, queue bounds and unchanged action results. These measurements do not assert universal modpack performance.

### Collection procedure

1. Identify the requirement/task, commit, candidate SHA-256/SHA-512, versions, accepted configuration and host. Resolve the owned runtime, `logs/progressivestages/<category>/<capture_id>.log` output, test reports and teardown resources. Capture status supplies the concrete path; do not guess a transient ID. For dedicated runs configure and read back `eula=true`, start no GUI and verify readiness. Register cleanup before the stimulus.
2. Prepare the smallest deterministic fixture through the owned console or verified GameTest. For player placement use `stage debug structures on <fixture_player>`; for actorless scope with no players use `stage debug structures on structure minecraft:overworld minecraft:village_plains` only after the fixture proves that registered structure/dimension is present. Check `stage debug structures status`. Use `abilities`, `editor` or `progression` for their respective scoped workflows. Unavailable controls fail the owning phase's instrumentation gate rather than justifying an unbounded log dump.
3. Exercise the actual failing path. The structure fixture grants only its entry stage, attempts entry/chest/place, changes priority or stage ownership, and repeats after reload. The movement fixture observes explicit restriction versus empty defaults. Editor and guide phases perform real draft validation/apply and subsequent sync. Console fixtures must not bypass enforcement or change tested player permissions. Before any client action confirm the owned application stream still reports muted.
4. Inspect the exact file returned by status for the capture ID and sequence. Correlate source key, owner stage, action, priority winner, provider/session reason, revision and final outcome. For example, `rg '"capture_id":"sample"' <exact_capture_file>` is a query template; substitute the returned ID and path, not the illustrative sample. Require absence of an entry contributor for protection only and continued place denial, explicit override tie behavior, scoped actorless results, current guide revision and no hidden identifiers or counts. A missing decisive event is a failed diagnostic assertion, not assumed success.
5. Run the category `off`, then `status`, wait for the bounded writer to drain and verify an additional matching stimulus writes no event. Save the decisive result, sanitized excerpts and remaining unverified claims. Timeouts, interruption and write failure use the same registered teardown. A stopped capture is not proof that its file completed successfully.
6. Retain project/version, platform pins, candidate identity, minimal relevant configuration, reproduction, expected/actual result, capture ID/window, test summary and relevant sanitized client evidence. Redact tokens, session URLs, private endpoints, chat, unrelated identities and sensitive paths. Place only required evidence in `docs/verification/3.1.0/` and support instructions in the existing topic guides; do not upload automatically.
7. After the final evidence consumer, stop the exact owned server/client/watcher and writer, verify processes and playback streams are gone, then remove exact disposable runtime, logs, scratch output and temporary audio state. Preserve reusable diagnostics, source, shared caches and requested deliverables. Verify cleanup on each host and report any exact leftover with its cause separately from test results.

## 15. Compatibility, Migration, Rollout, and Recovery

Existing stage files without new structure fields retain their own entry gating, existing priority inheritance with an unconfigured effective default of zero, and explicit ability behavior. The intended bug correction scopes previously global flag/padding leakage to actual contributing structures. Document that correction rather than promising byte identical erroneous behavior. Both legacy files and schema 4 packages retain source and accepted semantics through repeated load, compile and reload. Reject unsupported convenience selectors explicitly; existing generic selector facilities remain available.

Guide fields default empty and recommendation auto; an old pack remains loadable and receives no fabricated instructions. Preserve descriptions, unknown advanced fields and comments through targeted edits. The guide network change requires matching protocol 3 clients and servers. Roll out server and client artifacts together; prove protocol 2 rejection rather than silent partial sync. Old saved stage ownership remains unchanged and no irreversible world migration is introduced.

Before manual movement recovery or pack edits, document a backup of the exact administrator files and use review/apply. Diagnose the actual owner stage and source key; do not erase named showcase files on assumption. Failed parse, invalid guide or priority input, stale draft and failed apply retain the accepted configuration and expose useful errors. Test valid reload, rejection, correction and repeat application.

For candidate rollback use the last approved artifact with matching clients and restore only backed up files changed for that candidate. Keep the failed candidate's minimal sanitized evidence. A published issue requires a documented forward correction or explicitly authorized platform operation; do not delete public files, force update a tag or rewrite history silently. When one platform succeeds and the other fails, preserve the successful public identity, inspect supported broker state, retry only the missing upload with the exact verified candidate and leave final completion open until both verify.

## 16. Documentation, Operations, and Release Gates

Each implementation phase updates `README.md`, `DOCUMENTATION.md` and affected existing topics when its behavior changes. Update `docs/README.md` and cross links for changed or added topic paths. Keep normal documentation grammar and accurate public behavior. The final convergence proves the two stage example, action/priority truth table, actorless scope, movement defaults and explicit recovery, nearby graph actions, guide schema/recommendation/reveal behavior, protocol compatibility and bounded support collection. Add focused 3.1.0 release notes in `docs/release/3.1.0.md` and evidence in `docs/verification/3.1.0/` using current repository conventions.

The release phase changes only the mod release version to 3.1.0 and corresponding actual release metadata; preserve pinned platform/toolchain dependencies. Rebuild Vite assets through the repository path, run `PackagedEditorAssetsTest` and inspect exact JAR entries so a stale `app.js` or `app.css` cannot ship. Compare the packaged candidate and tested classes where development GameTests differ from production launch. Inspect the complete diff for accidental generated files, caches, credentials, private paths, debug probes and unrelated changes.

Complete the phase PR and required checks before public release. Verify the resulting `master` commit, candidate provenance and signed phase tag. Publication uses the configured release broker's supported inspect, candidate selection, preview and publish operations. Its current stale checkout/version selection must be replaced safely with the reviewed approved 3.1.0 artifact without overwriting unrelated root changes. Discover current command help; no `inspect-file` operation is assumed. Never read credential values or bypass the broker with direct upload APIs.

Bind the fresh concrete preview to exact project IDs, candidate commit, version, release channel, filename, hashes, Minecraft 1.21.1, NeoForge, existing dependency relations and both sides required on Modrinth. Existing publication authorization covers this requested release; do not invent a new approval boundary. Use only a current returned preview identifier or required confirmation value, never stale or guessed values. A material change to destinations, costs, licensing or operations requires a specific owner decision before that changed action.

After upload, inspect accepted public file metadata using supported broker responses and read only platform pages, download the accepted artifacts for hash comparison, and record both platform file/version IDs and links. Prove the downloaded JAR starts in production and serves `/pstages editor` with the correct assets. Perform one bounded released artifact workflow editing guide content and observing it in the silent laptop menu, while rerunning a representative independent structure and explicit ability policy smoke. Retain required acceptance proof and remove download/runtime scratch after the last consumer.

Publish wiki changes from merged tracked documentation, synchronize Project and milestone state, and reconcile issue 57 using precise verified results. Public text follows the repository's maintainer voice and identity rules. No Discord announcement is authorized. Do not claim a GitHub Release exists from a signed version tag alone. Completion requires the actual selected platform endpoint, not an extra hosting service.

## 17. Risks and Failure Boundaries

| Risk ID and causal scenario | Affected requirements or interfaces | Likelihood and impact rationale | Prevention | Detection signals | Recovery | Owning phase and tasks | Required proof |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RISK-001: A synthesized compiled entry denial survives the new flag | CORE-REQ-001, IF-001 | Observed dual compilation makes an aggregate only fix insufficient; sponsor workflow remains blocked | One attributed semantic contribution across compilers and runtime | Source key and contributor identity show a ghost entry gate | Reject candidate and fix compiler parity | CORE-PHASE-000, P000-TASK-003, P000-TASK-005 | Parse both formats, grant entry stage, actual entry/chest/place result and no duplicate denial |
| RISK-002: Flags or padding from one structure affect another | CORE-REQ-002, IF-001, IF-002 | Observed global aggregation loses attribution; unrelated gameplay changes | Per structure/action contributions and final denial padding | Structure ID, owner, priority and final result disagree with fixture | Retain prior snapshot; isolate lookup/cache contribution | CORE-PHASE-000, P000-TASK-004, P000-TASK-005 | Two distinct and overlapping structures, opposite flags, order permutations and actorless zero player case |
| RISK-003: Priority changes diagnostics but not gameplay | CORE-REQ-002, IF-002 | Multiple static and compiled paths can disagree | Route convenience candidates through actual winner semantics | Explained winner differs from actual canceled action | Block phase exit, repair arbitration and rerun affected events | CORE-PHASE-000, P000-TASK-004, P000-TASK-005 | Explicit matching ALLOW above/below/tied and another structure/provider denial |
| RISK-004: Default regeneration overwrites old packs or keeps an unintended movement lock | CORE-REQ-003, IF-005 | Two generation paths and old active showcase files are observed | Shared new default policy, malformed discovery rejection, no name based rewrite | Source fingerprint changes without explicit edit or legacy ability blocker persists | Restore exact backup, diagnose source and edit explicitly | CORE-PHASE-001, P001-TASK-002, P001-TASK-005 | Empty/valid/invalid/package/archived/restart fixtures plus actual movement and explicit negative case |
| RISK-005: Context gesture mutates graph state or stale draft loses data | CORE-REQ-004, CORE-REQ-005, IF-005 | Existing gesture and shared draft behavior has overlapping event paths | Separate context invocation from drag/connect and reuse dialogs/conflict handling | Unexpected draft revision, position/edge mutation or request | Cancel or retain draft, surface conflict, no silent retry overwrite | CORE-PHASE-001, P001-TASK-004, P001-TASK-005 | Real Brave right click/keyboard/pan/zoom/cancel/delete/cycle and stale apply assertions |
| RISK-006: Guide leaks hidden stages or recommends protection sentinels | CORE-REQ-009, IF-003 | Existing availability is prerequisite only; hidden filtering is separate | Visibility first policy and explicit recommendation metadata | Visible candidate list/count includes forbidden stage or blank manual sentinel | Remove invalid candidate publication and repair filtering | CORE-PHASE-002, P002-TASK-004, P002-TASK-005 | Hidden/reveal, branching, auto/include/exclude and global summary absence checks |
| RISK-007: Positional protocol or stale display consumer corrupts guidance | CORE-REQ-009, IF-004 | Protocol 2 is positional and two display consumers exist | Protocol 3, bounded codec, one canonical model and atomic revision | Mismatch rejection, inconsistent revision or old text after reload | Reject malformed snapshot, clear stale reconnect state, retain accepted definitions | CORE-PHASE-002, P002-TASK-003, P002-TASK-005 | Codec limits, large supported pack transport, mismatch, grant/revoke/team/reconnect and real menu refresh |
| RISK-008: Unicode or oversized text breaks source round trip or UI | CORE-REQ-009, IF-003 | New authored multiline content crosses Java, TOML, TypeScript and network boundaries | Identical code point/byte checks and literal rendering | Source key error, length mismatch, clipping or unintended formatting | Reject invalid edit without truncating stored source; fix wrapping | CORE-PHASE-002, P002-TASK-002, P002-TASK-005 | Boundary Unicode, invalid control, oversize, export/import and compact scale scroll proof |
| RISK-009: Capture changes behavior, leaks data or outlives its target | CORE-REQ-007, IF-006 | New real path instrumentation can add cost and incorrect lifecycle assumptions | Existing manager, lazy bounded records, explicit actor scope and reset | Limit/stop reason, overhead, unauthorized record or unexpected action change | Stop capture safely, retain sanitized failure and repair before dependent proof | CORE-PHASE-000, P000-TASK-002, P000-TASK-005 | Permission/no player/removed target/write failure/limits/reset/overhead and identical action outcomes |
| RISK-010: Stale broker selection or stale assets publish the wrong release | CORE-REQ-008 | Stale 3.0.4 selection is observed; Vite assets are separately built | Approved candidate selection and fresh bound preview | Version, commit, JAR or asset hash mismatch | Stop before upload; preserve one platform success if partial and correct missing side | CORE-PHASE-003, P003-TASK-002, P003-TASK-004, P003-TASK-005 | Packaged asset comparison, both public metadata/download hashes and live released editor workflow |
| RISK-011: Runtime evidence uses wrong host, audible client or retained disposable state | CORE-REQ-008, DEC-006 | Hardware availability does not prove renderer/audio identity; failures can interrupt teardown | Exact runtime registry, prelaunch silence, stream watcher and registered cleanup | Host/renderer/stream mismatch or surviving owned resources | Stop owned client, preserve gate as unverified and reconcile exact leftovers | Every phase verification task, P003-TASK-006 | Both host identity, mute readback/recreation, joined world and process/path cleanup receipts |

The unknown reporter configuration does not justify claiming its historical cause was reproduced. The release can prove the new defaults, source diagnosis and requested workflows with controlled real fixtures and describe that limit accurately. An unavailable external prerequisite remains visible under its EXT ID. A newly found material scope conflict requires an authorized plan amendment, not silent requirement removal.

## 18. Definition of Done

ProgressiveStages 3.1.0 is tested, integrated through checked master pull requests and signed phase tags, published on CurseForge and Modrinth with matching verified artifacts, and its updated mod-served web editor and in-game guide are verified from the released JAR. Documentation, wiki and issue 57 reflect the verified result.

1. All nine mandatory requirements satisfy their acceptance criteria with evidence at the required fidelity, and all four phase exits pass. Optional scope remains excluded.
2. Each phase is merged through the checked PR workflow into actual `master`, its resulting commit is verified and its annotated phase tag is signed and published before any next phase begins. No known mandatory owned defect remains.
3. Independent structure entry/protection, real priority arbitration, preserved provider/session/global behavior, safe movement defaults and intentional ability restrictions work in the documented fixtures and actual required client workflows.
4. The editor retains source and draft safeguards while providing plain controls and nearby graph actions. The actual in game menu shows authored guide branches, locations and correct state refresh without hidden information leakage.
5. Diagnostics are reusable and default off, their permission/privacy/budget/lifecycle/overhead checks pass, and the delivered artifact's support collection procedure works. Every client stayed inaudible with verified exact application stream mute and replacement handling.
6. ProgressiveStages 3.1.0 is published on CurseForge and Modrinth with accepted metadata and downloaded hashes matching the tested approved artifact. Its bundled editor and guide are verified from that released JAR, with production dedicated startup proven separately from development tests.
7. README, detailed documentation, topic links, release notes, wiki, issue 57 and relevant tracking reflect the verified merged result. There is no claim of an unnamed separately hosted website or uncreated GitHub Release.
8. Required sanitized evidence and requested artifacts exist at their intended destinations. Every bounded test, verification build and audit has verified cleanup on each involved host; exact unresolved leftovers prevent declaring the workflow finished.
9. No unverified external gate, queued merge, partial upload or plausible inference is represented as completion. The sole completion endpoint is the full 3.1.0 delivery described above.

## 19. Goal Creator Handoff

```text
Mandatory boundary: CORE-REQ-001 through CORE-REQ-009 across all four contiguous phases, with the full release endpoint.
Optional/future disposition: excluded.
Locked owner decisions: DEC-001 through DEC-006.
Active phase: CORE-PHASE-000
Next executable action: P000-TASK-001, verify latest approved master, existing checkout state, owned resource boundaries, pins and phase prerequisites before implementation.
Known failing checks: No runtime or implementation checks ran during research. Observed coupled structure aggregation, active default movement locks and missing dedicated guide remain unimplemented target behavior.
Known external blockers: none
Completion endpoint: ProgressiveStages 3.1.0 is tested, integrated through checked master pull requests and signed phase tags, published on CurseForge and Modrinth with matching verified artifacts, and its updated mod-served web editor and in-game guide are verified from the released JAR. Documentation, wiki and issue 57 reflect the verified result.
Required evidence gates: Parser/compiler/arbitration/default/editor/guide tests, real server events, actual Brave workflows, silent laptop multiplayer and guide evidence, bounded diagnostics and overhead, complete cleanup, checked PR integration, resulting master verification, signed tags, both accepted platform artifacts, hashes, released editor and guide, documentation and wiki convergence.
```

EXT-001 through EXT-004 require candidate specific revalidation and retain their mandatory gates if availability changes.

This handoff describes the complete contract and first action. It does not create or activate a saved goal or execution cursor. `plan.handoff.json` is derived from the validated complete master and registered phase set, not an independent product authority. Future execution retains the current authoritative plan and any immutable saved goal without regenerating either at phase transitions.

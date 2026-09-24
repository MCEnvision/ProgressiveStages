# Repository map

Baseline: current master c0a3ef2c32a5406a0bf12e43d2c475aa576fe9d6. See the source observation file for exact fingerprints. The graph index is on the older root, so remote differences were inspected with pinned Git reads.

## Server configuration and decisions

- Progressivestages.createConfigFolder -> generateDefaultStageFilesIfNeeded -> DefaultShowcaseStages.files writes initial packages during initialization. StageFileLoader.initialize -> discovery -> generateDefaultStageFiles independently provides a second fallback. StageFileLoader candidate validation/application is the transactional reload boundary.
- StageFileParser.parseLocks -> parseStructures -> LockDefinition.StructureRules -> LockRegistry.StructureRulesAggregate.merge. The current aggregate loses stage local flag and padding attribution.
- LegacyStageCompiler.compile -> addCategory structures.enter. Schema4StageCompiler.compile imports those compatibility rules before generic rules and priority decoration. Both routes must honor new entry behavior.
- StructureEnforcer.evaluate(player, position, action) -> matching structure lookup -> firstMissing -> ConditionalLockEngine.resolve -> StructureContextRegistry.evaluate -> StructureAccessArbitration.combine -> session permit validation. Do not substitute a helper-only test for this path.
- StructureEnforcer.filterExplosionBlocks and blocksMobSpawn are separate actorless paths. candidateStructures and lookup cache must retain protection-only targets when entry targets are removed.
- StructureEnforcer.checkPlayerEntry handles repel and padding. Existing per player safe position and per-tick structure cache need regression checks after reload, dimension change and reconnect.
- AbilityEnforcer.rebuild -> GATERS; lacks combines stage ownership with ConditionalLockEngine. Player tick syncs changed locks via NetworkHandler.sendAbilityState; jump event and client ability restrictions enforce them. Default generation changes must preserve configured enforcement.

## Editor

- editor-ui uses React 19, TypeScript, a Vite packaged build and Vitest/jsdom. No external graph library is needed.
- LayoutPage has custom SVG node and edge geometry, automaticPositions, eventPoint camera conversion, activateNode connection workflow, savePosition and removeEdge. Selection currently navigates through selectStage; add stable local selected-node identity for adjacent actions without breaking editing navigation.
- EssentialsPanel handles display name/icon and dependencies. EffectsPanel AbilitiesForm edits abilities.locked. RulesPanel is advanced generic rules, and the current master improves selector and rule source handling.
- StageActions and StageDialogs already own destructive confirmation and duplicate/rename/archive operations. EditorContext provides mutations, boot draft, notifications, selection and review/apply; reuse its current master implementation.
- lib/tomlSource.ts and preservation tests on master guard unrelated TOML source. Keep advanced unknown fields and comments when saving targeted UI changes.

## Diagnostics and documentation

StageCommand has compiled explain target and bounded debug capture controls. Current actual decisions across static structures, session providers and legacy abilities are not fully represented by that compiled trace. Extend the existing diagnostic manager and permission boundary with scoped events before relying on them as failure oracles.

README.md, DOCUMENTATION.md, docs/README.md, docs/troubleshooting/easy-builder.md and docs/test/diagnostics.md are current topic entry points. Do not resurrect unrelated deleted docs or write mutable progress into a saved goal. Add narrowly scoped starter/default recovery, two stage structure example and editor controls guidance with cross-links.

## Existing test footholds

StageFileParserTest, Schema4StageCompilerTest and LegacyCompatibilityBaselineTest cover parse/compile compatibility; DefaultShowcaseStagesTest expects 50 valid packages and 150 files. Keep showcasing tests separate from whatever fresh install policy the owner approves. AbilityEnforcerTest and client ClientAbilityStateTest cover helper policy but do not prove real jumping. StructureAccessArbitrationTest, StructureSessionAccessPolicyTest and structure/session tests guard integration; no focused StructureRulesAggregate regression coverage was returned by the index. Add focused tests for the newly separated contributions plus server GameTests for actual events.

Current master editor tests include EffectsPanel.test.tsx, RulesPanel.test.tsx, EditorContext.test.tsx and TOML preservation tests; no LayoutPage test was observed in the indexed root. PackagedEditorAssetsTest guards the distributable. Existing dedicated server EditorApplyGameTests and related fixtures can inform draft-to-runtime proof. Existing diagnostic overhead fixture can be extended without starting a new benchmark framework.

## Coverage limits

CodeGraph covered indexed Java relationships but omitted full methods and editor/default paths in its bounded responses. Necessary scoped raw reads supplied those gaps. Current master only files were read through git show, not indexed. No reflection/event-bus call map claim or successful runtime claim is made. Files listed only as fingerprint dependencies are not claimed fully reviewed. Issue reporter's environment and actual installed configuration remain unknown.


## Guide extension and release boundaries

StageTreeScreen.rebuild graph around lines 193 through 236 marks unowned dependency-satisfied nodes available, then applies reveal policy. renderInspector around 725 shows description, prerequisite status, slots, costs and trigger progress. ClientStageCache.StageDefinitionData/getDescription and NetworkHandler.StageDefinitionEntry (around 1339) are the sync/data extension points; StageDefinition and StageFileParser own durable fields. Current protocol registration is 2. This is an complete data change, not an editor only textarea.

editor-ui/vite.config.ts packages app.js and app.css directly into mod resources. .github/workflows/release-validation.yml validates tags, while release platform and hosted upload mechanisms are not declared in the inspected repository. docs/release/3.0.4.md supplies established public platform evidence. The delivery target is the existing mod served editor; no separate public site is identified. No environment files or secrets were read.


## Environment and publication evidence

The maintainer observed existing release broker mappings: CurseForge project 1460273 and Modrinth rY4mdP4Q, Minecraft 1.21.1, NeoForge, both sides required on Modrinth. Existing broker inspect succeeded without exposing secrets. Use supported broker inspect, preview and publish operations. Current help does not list inspect-file. Verify accepted metadata through returned broker metadata and supported read only platform pages. Do not substitute direct credential reads or uploads. Its currently selected anchor is stale root version 3.0.4-fix and artifact build/libs/progressivestages-3.0.4.jar. Final execution must refresh or safely select the reviewed approved 3.1.0 candidate and verify commit, identity and hashes before preview/publish. Never guess a preview ID/code or upload the stale file. Sanitized capability evidence is retained in sources/environment.json.

The existing editor served through /pstages editor is the delivery target. Browser, signing and hardware access were observed; exact candidate renderer, audio and gameplay proof remain execution gates.

## Guide navigation and help boundaries

SRC-021 pins the amendment dependencies and FIND-014/FIND-015 record their intended use. `components/ui.tsx` Field currently renders help inline; `TextDraftField` shares that slot with saving feedback. The Phase 001 help component must separate static explanations from live state, and Phase 002 guide fields reuse it.

`ClientModBusEvents.OPEN_TREE` is initially unbound. `ClientEventHandler.onClientTick` drains clicks before the existing request path. Current approved `ClientTriggerProgress` and `StageTreeScreen` separate explicit opening from refreshing an open screen; preserve this boundary when carrying guide-tab intent. The advancement window, item nodes, connections, map controls and inspector remain the presentation basis.

`StageConfig` is registered as COMMON and caches config event values. `client.show_inventory_button` is an independent client option, not guide policy. Add the main `general.enable_stage_guide` Boolean through accepted server snapshot publication and clear policy on disconnect. Config events must filter the correct spec and logical side before scheduling a server publication. Phase 002 verifies startup, reload, opposite-policy reconnect and disabled input. Existing screen focus/layout/viewport, inventory button and snapshot tests are footholds; none proves the new workflow. The source record documents graph coverage gaps and pinned-source observations without claiming runtime proof.


## Main settings and preview amendment

SRC-023 and FIND-016/FIND-017 pin SettingsPage, SettingControl, BuiltinEditorSchemas.addSettings, StageConfig.SPEC, EditorDraftValidator and EditorApplyService. Main settings already flow through the draft, but main validation currently parses syntax only and actual effective config application needs runtime proof. EditorSchemaRegistryTest and EditorApplyServiceTest plus EditorApplyGameTests are existing test footholds. No SettingsPage-specific test was found in the pinned file list; do not claim one exists.

RulesPanel already exposes ownership state, activation condition, lifetime and reset for supported rule forms. CompiledRuleEngine.stageStateMatches and activationPolicy own the effective semantics. StageTreeScreen.rebuild/revealed and EssentialsPanel own separate hidden/reveal/display settings. The current LayoutPage is an authoring SVG; the new preview needs explicit client equivalent fixtures and a read only draft projection. CodeGraph relationships were inspected before bounded pinned Git reads; reflected spec constraints, browser translation behavior and actual runtime effects are coverage gaps, not verified results.

Use RulesPanel.test.tsx, CompiledRuleEngineTest, StageTreeScreenRenderOrderTest, source-preservation tests and the existing draft/apply suites for meaningful regressions. The preview's actual appearance still needs comparison with the silent laptop client. Browser help relies on the primary references in sources/editor-workflows.md, not a new paid translation dependency.

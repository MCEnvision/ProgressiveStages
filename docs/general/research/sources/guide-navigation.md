# Guide navigation and field help

## Product decisions

DEC-007 keeps the labels Stage name, Stage icon and Required stages and moves their short explanations and examples into Minecraft style hover tooltips. Keyboard focus and click or tap expose the same help. Validation and save feedback remain visible.

DEC-008 preserves the existing advancement style stages menu and adds top Stages and What to do next tabs, a separate guide key mapping and a main config enable/disable option. The GUI and its preview omit pack creator attribution. Authored unlock, next step and location text remains editable in the stage form. The selected engineering defaults are an enabled guide and an initially unbound, independently rebindable key, matching the existing stage key convention.

These are required outcomes, not implemented behavior or runtime evidence. The canonical details and acceptance criteria are in IF-003, IF-004 and IF-005 of the master plan.

## Pinned repository observations

Baseline: `c0a3ef2c32a5406a0bf12e43d2c475aa576fe9d6`. CodeGraph reported its older root checkout index current at observation. Its source and relationship results identified the field wrapper, tick input handler, screen, config and caches. Pinned Git reads supplied the newer approved baseline and symbols omitted by the bounded graph response.

- `editor-ui/src/components/ui.tsx` has a shared `Field` wrapper with inline `help`. `EssentialsPanel.TextDraftField` also uses that help slot for saving feedback. Tooltip work must separate optional explanations from live status and avoid nested interactive controls inside labels.
- `StageTreeScreen` uses the vanilla advancement window and title assets, tiled backgrounds, connected item nodes, category/search controls and a selected-stage inspector. Its current top search and control positions leave a concrete compact-layout collision risk when adding tabs.
- `ClientModBusEvents.OPEN_TREE` registers an initially unbound key mapping in the existing ProgressiveStages category. `ClientEventHandler.onClientTick` drains queued presses before one request. A dedicated guide mapping should preserve these conventions and explicitly resolve conflicting bindings and typing contexts.
- Current `ClientTriggerProgress` separates `requestFromServer` from `refreshFromServer`. `StageTreeScreen.open` and `refreshIfOpen`, together with the optional explicit opening payload, prevent ordinary data refresh from opening the screen. Guide-tab intent must survive a legitimate request without letting a late response reopen or reset the screen.
- `Progressivestages` registers `StageConfig.SPEC` as COMMON. `Constants.MAIN_CONFIG_FILE` identifies `config/progressivestages/progressivestages.toml`. The existing `client.show_inventory_button` defaults true and explicitly leaves commands and the existing key available. This client preference is not a server guide policy. The new guide switch requires explicit accepted server-to-client publication and disconnect clearing.
- `StageConfig.onLoad` updates cached values on config events and stops capture on its own spec reload. The new guide policy publication must run only for the correct spec and logical server, schedule through the existing accepted snapshot owner and tolerate startup before a running server exists. Do not access client classes from this common configuration hook.
- Pinned test paths include `StageTreeInventoryButtonTest`, `StageTreeScreenRenderOrderTest`, `StageTreeFocusTest`, `StageTreeLayoutTest`, `StageTreeViewportTest`, `ClientCompiledSnapshotCacheTest` and `ClientSnapshotAssemblerTest`. They are extension points, not proof of new tab, key or policy behavior. The graph did not establish complete event-bus or test coverage.

The existing documentation index differs from the first research fingerprint because it now links the implementation plan and the completed 3.0.5 release. Inspection found navigation additions only. The SRC-015 working-file fingerprint is refreshed without altering its pinned baseline or unrelated documentation.

## Failure analysis and proof ownership

RISK-012 belongs to Phase 001. Hover-only help can exclude keyboard or touch users, obscure live errors or change a field on a help click. CORE-AC-006 requires equivalent focus/click access, wrapping, dismissal, accessible descriptions, inline status and no draft mutation.

RISK-013 belongs to Phase 002. A local COMMON value, delayed payload or shared key handler can expose a disabled guide, select the wrong tab or reopen a closed screen. CORE-AC-018 and CORE-AC-019 require accepted server policy, explicit open intent, reload/reconnect tests and actual silent laptop input and presentation evidence. Phase 003 repeats representative tooltip, tab, rebound key and disable/reenable workflows against both the pre-publication candidate and released artifact.

No product build, browser, client, server or gameplay check ran for these observations.

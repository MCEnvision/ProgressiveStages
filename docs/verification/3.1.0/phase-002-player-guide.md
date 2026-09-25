# ProgressiveStages 3.1.0 phase 002 player guide

This page records the implementation contract and verification procedure for the authored player guide. Runtime observations are recorded only for the candidate server and client paths that were actually exercised.

## authored data

Stage files may contain an optional `[guide]` table. The editor writes the four supported fields without replacing unknown TOML keys. Text is inert, accepts Unicode and line breaks, and is bounded to 2048 Unicode code points and 8 KiB of UTF 8 per field. Invalid types, control characters, overlong values, and unknown recommendation values reject the candidate before the active snapshot changes.

## player presentation

The existing advancement style menu has `Stages` and `What to do next` tabs. The guide lists visible, unrevealed, unowned stages in stable order and shows the pack supplied unlock, next step, location, and prerequisite text. The separate guide keybind is initially unbound. `general.enable_stage_guide` defaults to true and is synchronized from the server.

The editor `Player UI` also has a read only `Player preview`. It uses the current draft display name, icon, description, guide, hidden, reveal, category, search, and dependency data. `Locked`, `Requirements met`, and `Unlocked` simulate the quick ownership states. Hidden stages are absent from the preview and its search, category, count, dependency, and empty state surfaces. Clicking a visible node returns to the adjacent stage form for editing. Preview navigation does not write TOML, apply the draft, grant stages, purchase stages, execute rules, or run rewards.

## verification procedure

1. Use the editor Essentials page to enter multiline Unicode text in all three text fields and choose each recommendation mode. Review and apply the draft.
2. Reload stage definitions and confirm the authored text survives reconnect and a schema 4 source round trip.
3. Open the stages menu and switch tabs with the header controls and the guide keybind. Check hidden, reveal, owned, dependency, empty, long wrapping, scrolling, and compact scale cases.
4. Set `general.enable_stage_guide = false` through the main editor settings, apply, and confirm that the ordinary map remains available while the guide tab and guide key do not open it. Reenable the setting and confirm the guide returns after synchronization.
5. Capture only sanitized diagnostics and remove the owned server, client, browser, screenshots, logs, and build outputs after the last consumer.

The phase completion record adds candidate identity, test commands, hashes, host identity, and cleanup receipts as each gate is completed. Browser evidence remains explicitly open until the required Brave workflow is available.

## current local checks

The editor checks pass with `npm run check`, 19 Vitest files and 165 tests pass with `npm test -- --run`, and the packaged editor is rebuilt with `npm run build`. The Java unit suite passes with `./gradlew test --no-daemon`, and `./gradlew build --no-daemon` produces a jar containing the guide model and packaged editor assets.

The required headless GameTest server was also started from the disposable phase worktree. It shut down cleanly, but the repository's existing suite reported 15 unrelated world and progression failures. Its disposable runtime files were removed after the run.

The silent laptop client gate is complete for the exercised guide path. The candidate was run on the authorized `envision` laptop in an isolated Hyprland workspace with the discrete renderer verified, joined to the matching private dedicated server, and opened through the existing Prism Launcher installation without changing its launcher configuration or starting another launcher process. The advancement style stage menu showed the `Stages` tab, search, owned filter, category selector, and connected stage nodes. Selecting a stage showed the gold question mark beside the close control. Hovering the question mark showed `How to unlock Guide Start`; hovering the close control showed its close tooltip. Activating the question mark displayed the authored unlock, next step, and location text. The client stream was muted and verified before interaction, then the client, server, tunnel, stream, temporary instance jar replacement, screenshots, and disposable runtime files were removed or restored. No unrelated Minecraft or Prism process was changed.

The required Brave editor edit and apply gate remains open. The available browser control surface does not provide the required Brave extension connection, so this record makes no claim for authoring or applying guide text through Brave, browser console state, or browser network evidence. The phase cannot close until that gate is completed.

## candidate identity

The current candidate source commit is `1513763`. A clean `./gradlew build --no-daemon` produced `progressivestages-3.0.5.jar` with SHA 256 `d98f94cd4f5c72de6e4b4ad63d66dd07d20c767af66ccb407036cea455f4a5f4` and SHA 512 `e5340c24c2eed904aeb69d2abca13089f314b488d9c86e9b13d5310e1e231a7ee24d665f148525b47dbfd90f6b8e068e448c94321324d12e73ad0efb43b86cea`. The archive contains `StageGuide`, `StageTreeScreen$StageGuideCard`, the packaged editor bundle, and the English language file.

The packaged editor asset hashes for this candidate are `app.js` SHA 256 `ddc3911277ca6c18db21ccabadfb932036ddd8f6b6432de7e649829499f7ba79`, `app.css` SHA 256 `6bc163ca7269dfb41c3e0e6d0ecdd3b9e0640d98f60199686dc85909b068baae`, and `en_us.json` SHA 256 `48706395879c60b52adb4fe9cd3a80a12b99b197a0dbca2a2d66678e905a4b51`. The build output remains available only for the current verification consumers and will be removed during phase cleanup.

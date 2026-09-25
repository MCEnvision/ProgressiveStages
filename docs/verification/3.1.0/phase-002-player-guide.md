# ProgressiveStages 3.1.0 phase 002 player guide

This page records the implementation contract and verification procedure for the authored player guide. Runtime observations belong here only after the candidate server, Brave editor, and silent laptop client have been tested.

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

The phase completion record will add candidate identity, test commands, hashes, host identity, and cleanup receipts after those gates pass.

## current local checks

The editor checks pass with `npm run check`, 19 Vitest files and 165 tests pass with `npm test -- --run`, and the packaged editor is rebuilt with `npm run build`. The Java unit suite passes with `./gradlew test --no-daemon`, and `./gradlew build --no-daemon` produces a jar containing the guide model and packaged editor assets.

The required headless GameTest server was also started from the disposable phase worktree. It shut down cleanly, but the repository's existing suite reported 15 unrelated world and progression failures. Its disposable runtime files were removed after the run.

Brave editor evidence and silent laptop client evidence remain open. The current execution host is the headless `node-1`, and the authorized `envision` laptop connection was unavailable, so no graphical acceptance result is claimed here.

## candidate identity

The current candidate commit is `971c7a4`. A clean `./gradlew build --no-daemon` produced `progressivestages-3.0.5.jar` with SHA 256 `1d84973f4bde78a191c04bab664fbd13aec90a8e5cdccf7f6e46def4d5235ef9` and SHA 512 `83b4ee13aac0e89dfd5a57de4e43629cb897d674e2cc863176d7ada0984abadd12d8d4baece4ca120ad961ecef64b92f5427bc8062cd3aa36aef1bfd2f455307`. The archive contains `StageGuide`, `StageTreeScreen$StageGuideCard`, the packaged editor bundle, and the English language file.

The packaged editor asset hashes for this candidate are `app.js` SHA 256 `5a17db70a780e6b2c9e62a9f3bb18821d3a19a8be7ca15aa5fd6b5113304608a`, `app.css` SHA 256 `808e3108f139631fe36ddf715c610f7308807a660e448f5db0a4580cb8736d15`, and `en_us.json` SHA 256 `caabeaec01641c8710234fcd272b29955549cc37a1316e7880764ccb5385acae`. The build output was removed after inspection.

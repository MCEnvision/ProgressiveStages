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

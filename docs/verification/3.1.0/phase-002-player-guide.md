# ProgressiveStages 3.1.0 phase 002 player guide

This page records the implementation contract and verification procedure for the authored player guide. Runtime observations are recorded only for the candidate server and client paths that were actually exercised.

## authored data

Stage files may contain an optional `[guide]` table. The editor writes the four supported fields without replacing unknown TOML keys. Text is inert, accepts Unicode and line breaks, and is bounded to 2048 Unicode code points and 8 KiB of UTF 8 per field. Invalid types, control characters, overlong values, and unknown recommendation values reject the candidate before the active snapshot changes.

## player presentation

The existing advancement style menu remains one map without guide tabs. Selecting an eligible stage shows a gold `?` beside the close control. Pressing it opens `How to unlock <stage name>` with the pack supplied unlock, next step, location, and prerequisite text. The separate guide keybind is initially unbound and selects the next visible suggestion in the same map. `general.enable_stage_guide` defaults to true and is synchronized from the server.

The editor `Player UI` also has a read only `Player preview`. It uses the current draft display name, icon, description, guide, hidden, reveal, category, search, and dependency data. `Locked`, `Requirements met`, and `Unlocked` simulate the quick ownership states. Hidden stages are absent from the preview and its search, category, count, dependency, and empty state surfaces. Clicking a visible node returns to the adjacent stage form for editing. Preview navigation does not write TOML, apply the draft, grant stages, purchase stages, execute rules, or run rewards.

Each guide field has a nearby placeholder chooser and an inert rendered preview. The chooser inserts only `{stage_name}`, `{stage_requirements}`, `{remaining_requirements}`, or `{stage_progress}`. Unknown tokens stay visible with a warning, doubled braces render literal braces, and invalid 2048 code point or 8 KiB UTF 8 values cannot be saved. The preview uses sample values and never applies a draft or runs a command.

## verification procedure

1. Use the editor Essentials page to enter multiline Unicode text in all three text fields and choose each recommendation mode. Review and apply the draft.
2. Reload stage definitions and confirm the authored text survives reconnect and a schema 4 source round trip.
3. Open the stages menu and switch tabs with the header controls and the guide keybind. Check hidden, reveal, owned, dependency, empty, long wrapping, scrolling, and compact scale cases.
4. Set `general.enable_stage_guide = false` through the main editor settings, apply, and confirm that the ordinary map remains available while the question mark and guide key do not open guide help. Reenable the setting and confirm the guide returns after synchronization.
5. Capture only sanitized diagnostics and remove the owned server, client, browser, screenshots, logs, and build outputs after the last consumer.

The phase completion record adds candidate identity, test commands, hashes, host identity, and cleanup receipts as each gate is completed. Browser evidence remains explicitly open until the required Brave workflow is available.

## current local checks

`npm ci` completes from the checked lockfile. It reports six dependency audit findings, including two high findings, without changing the pinned dependency set. The editor checks pass with `npm run check`, 20 Vitest files and 169 tests pass with `npm test -- --run`, and the packaged editor is rebuilt with `npm run build`. The Java unit suite passes with `./gradlew test --no-daemon`, and `./gradlew build --no-daemon` produces a jar containing the guide model and packaged editor assets. Incomplete multi-packet definition assemblies now expire after 200 client ticks, while newer revisions supersede older assemblies and logout clears the pending state.

The required headless GameTest server was also started from the disposable phase worktree. It shut down cleanly, but the repository's existing suite reported 15 unrelated world and progression failures. Its disposable runtime files were removed after the run.

The silent laptop client gate is complete for the exercised guide path. The candidate was run on the authorized `envision` laptop in an isolated Hyprland workspace with the discrete renderer verified, joined to the matching private dedicated server, and opened through the existing Prism Launcher installation without changing its launcher configuration or starting another launcher process. The advancement style stage menu showed the `Stages` tab, search, owned filter, category selector, and connected stage nodes. Selecting a stage showed the gold question mark beside the close control. Hovering the question mark showed `How to unlock Guide Start`; hovering the close control showed its close tooltip. Activating the question mark displayed the authored unlock, next step, and location text. The client stream was muted and verified before interaction, then the client, server, tunnel, stream, temporary instance jar replacement, screenshots, and disposable runtime files were removed or restored. No unrelated Minecraft or Prism process was changed.

The stage definition chunk target is the source verified `ClientSnapshotCodec.MAX_CHUNK_BYTES` value of 24 KiB. The definition splitter measures the encoded stage entry list before adding each entry, rejects an individual entry that exceeds that bound, bounds the chunk count to 4096, and applies a revision only after all chunks are present. Incomplete assemblies expire after 200 client ticks.

The required Brave editor edit and apply gate remains open. The available browser control surface does not provide the required Brave extension connection, so this record makes no claim for authoring or applying guide text through Brave, browser console state, or browser network evidence. The phase cannot close until that gate is completed.

## candidate identity

The current candidate source commit is `b1b6ecc`. A clean `./gradlew build --no-daemon` produced `progressivestages-3.0.5.jar` with SHA 256 `95f1c87f59c353a7e978c4c27d705a4cffd48e75eff25c5b13db8c20fcfae376` and SHA 512 `11e7217879edd0b782466c832066ed4e6fb059663c3534a85dcec87b3b6e4676e13dd1a2f43d3e8130c61bee66fcce40820eede3ec09ecf8c89c43ec88413eef`. The archive contains `StageGuide`, `StageGuideTemplate`, `StageTreeScreen`, the packaged editor bundle, and the English language file.

The packaged editor asset hashes for this candidate are `app.js` SHA 256 `2af23dce0c525357ac660a75545ca278e7f768769d20cbf33f00ef0e3c700fcf`, `app.css` SHA 256 `c63c620e6c4c81e0ce91124c1dba384ebaafd46433b1d7f08e42a9a581c10f04`, and `en_us.json` SHA 256 `a9799ae8b24b321c26c3285480ba6f43822c2cf62fd7bf937080ad2c8be89a9f`. The build output remains available only for the current verification consumers and will be removed during phase cleanup.

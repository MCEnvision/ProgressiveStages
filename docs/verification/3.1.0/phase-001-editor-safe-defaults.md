# phase 001 editor and safe defaults

## candidate

- phase branch: `envy/3.1.0-phase-001`
- base commit: `a63dd1573545a5b282072911030703e8255b5f3d`
- minecraft: `1.21.1`
- neoforge: `21.1.248`
- java: `21`

## implementation evidence

- generated showcase stages no longer lock essential movement abilities. the generated templates do not add `jump`, `elytra`, `climb`, or `swim` locks.
- newly created editor stages keep the ability list empty until an author explicitly adds a restriction.
- stage labels use plain terms such as `Stage name`, `Stage icon`, and `Required stages`. field help is available by hover or keyboard focus.
- the layout graph supports nearby edit, connect, duplicate, and delete actions from right click or keyboard context invocation. escape and outside click close the menu and return focus.
- structure controls expose locked entries, entry allowance, signed priority, entry padding, block break, block placement, explosions, and mob spawning. existing source values remain separate from omitted defaults.
- main settings validate against the loaded NeoForge spec, apply live values through the loaded config cache, report restart pending values, and restore files and effective values when stage reload fails.
- the packaged editor assets are included in the mod resources.
- toggle controls expose their plain labels to keyboard and assistive technology, including the
  structure entry and protection controls.

## verification

- `npm ci` passed.
- `npm run check` passed.
- `npm test -- --run` passed, 17 files and 163 tests.
- `npm run build` passed. packaged `app.js` sha256 is `2522c52f5844433656c725e5e8ac31fc16c7d9a9806502673c41c3489015457f`. packaged `app.css` sha256 is `ba1027c5bed62c3268f9e5701a1a1a4def3d6938f4686e355904e82dc102247e`.
- `./gradlew test --no-daemon` passed.
- `./gradlew build --no-daemon` passed. phase candidate jar sha256 is `d0691d1cc0f87a848a703c803e035abee2426822f458a1f45ba55349ddecc3f6`.
- `./gradlew runGameTestServer --no-daemon` completed 111 tests. the phase editor transaction test and other phase-owned tests completed. the task exit code was 15 because the same 15 pre-existing optional LuckPerms provider tests fail when that provider is unavailable. no new phase-owned failure was reported.
- github checks for the phase commit passed for gradle, node, dependency review, secret scan, java codeql, javascript codeql, and the repository codeql gate. documentation and dependency submission jobs were skipped by workflow conditions.
- `git diff --check` passed.
- the editor apply transaction now validates and writes one synchronized draft snapshot, preventing concurrent edits from changing the files between validation and write.
- restart scoped settings stay pending only when their candidate differs from the effective loaded value, numeric settings expose and enforce schema ranges, invalid local settings block validation and review, graph right click opens the context menu without moving a node, and structure edits serialize against the latest draft content.
- editor initiated config watcher reloads preserve restart scoped effective values while applying later live changes, failed draft saves refresh the authoritative draft, failed rollback reloads restore memory and files, and queued structure edits stay isolated by stage path and accepted content. Failed structure saves restore the prior accepted source before later edits run.

## brave editor acceptance

The authenticated editor was opened from the disposable operator world in the laptop Brave
browser through the installed browser control connection. The Stages page showed 50 loaded stages,
the plain Stage name, Stage icon, and Required stages labels, and the hover and focus help text.
The Settings page rejected an inventory button height of `1` with an inline minimum value error and
disabled validation. Restoring `18` cleared the error and allowed validation.

The End Resolve Rules page rendered Structure access with separate Allow entry, block breaking,
block placement, explosion, mob spawning, signed priority, and entry padding controls. Its help
text explains that entry can be allowed while a permanent protection stage keeps block restrictions.
The rebuilt bundle kept help hidden while a field itself was focused and showed it when the question
mark trigger received focus. Switching from Aquatic Blessing to End Resolve replaced the queued
structure draft with the selected stage content. An invalid entry padding of `-1` produced the
inline whole number error and clearing it restored the accepted draft without applying a change.
The Player UI page rendered the advancement style graph with category filtering, search, zoom, fit,
automatic layout, and direct connection controls. Right clicking a graph node opened Edit stage,
Connect stage, Duplicate, and Delete. Escape closed the menu without changing the draft.

A disposable Browser Acceptance stage was created with an empty ability list, edited with a
description and map category, reviewed as 51 compiled stages, applied to the live test server, and
reported as synchronized. The transaction rollback control then reported a synchronized rollback.
A malformed `[stage` source draft was retained for correction, blocked at review with one validation
problem, and returned to the valid source through Undo. These browser checks used only disposable
stage text and did not send secrets or private session data to a translation service.

## laptop gameplay acceptance

On September 24, 2026, the current candidate was loaded on the verified laptop client with
Minecraft 1.21.1, NeoForge 21.1.248, and only ProgressiveStages 3.0.5 in the client mods
directory. The dedicated server used the same candidate classes and listened on a private test
endpoint. The client log recorded the same candidate loading, the connection to that endpoint,
and a cache of 50 stage definitions. The server log recorded
the authenticated player joining and leaving normally.

The owner tested walking, jumping, block placement, and block breaking in the connected world and
reported that all four actions worked. The client rendered with the NVIDIA GeForce RTX 5090
Laptop GPU. Its Java playback stream was identified by the owned client process and read back as
muted before the test. The first disposable launch included an unrelated Selling Bin mod and was
discarded after the client correctly rejected its missing registry data. The accepted run used a
matching client and server mod set. The client and server were stopped after the owner
disconnected, and the disposable instance and runtime were removed.

### explicit ability lock and recovery

The latest candidate was also tested with a disposable stage definition that locked only `jump`.
The server registered `test:jump_lock` with `locked = ["jump"]`, and the owner confirmed that
jumping was blocked before the stage grant. The corrected operator fixture used the owner's exact
uuid with level 4. After reconnecting to the private test endpoint, the server recorded the owner
joining and successfully running `/stage grant EnVyOnMyMind test:jump_lock`. The client received
the stage change, and the owner confirmed that jumping worked after the grant. The client used the
same candidate jar as the server and had master volume set to zero before launch.

## open acceptance gates

The configured block placement path remains intentional and is separate from the safe movement
default change. Full guide fields and the in game guide remain assigned to Phase 002.

## cleanup

The game test server, laptop client, and authenticated editor tab were stopped or closed after the
final evidence consumer finished. The owned Java process, laptop audio stream, temporary Prism
instance link and target, `build`, `run`, `editor-ui/node_modules`, and temporary screenshots were
verified absent. The personal Prism instance and unrelated Brave tabs were left untouched.
`.codegraph` was absent in the worktree. The shared codegraph service outside the worktree remains
untouched.

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
- `npm test -- --run` passed, 17 files and 158 tests.
- `npm run build` passed. packaged `app.js` sha256 is `08fd07f0ef1f24d28ed14c5a361b59e417c8e0e10c53228fa254d9196f5ec46a`. packaged `app.css` sha256 is `61fe46e00c1c89c2ccd2c120b8f11fa2771c68c529b0687aef1f59340e64d00c`.
- `./gradlew test --no-daemon` passed.
- `./gradlew build --no-daemon` passed. candidate jar sha256 is `3ac5d28394b0b45b205ff2d52fbd9250be4e06b149840ecd2339ee968ab6540b`.
- the latest phase candidate jar sha256 is `1f4a99a7fa1da19891070d63cd0e0a356da13d86422d2842b01f0d691d21c566`.
- `./gradlew runGameTestServer --no-daemon` completed 111 tests. the phase editor transaction test and other phase-owned tests completed. the task exit code was 15 because 15 pre-existing optional LuckPerms provider tests fail when that provider is unavailable. no new phase-owned failure was reported.
- github checks for phase commit `3f27d96` passed for gradle, node, dependency review, secret scan, java codeql, javascript codeql, and the repository codeql gate. documentation and dependency submission jobs were skipped by workflow conditions.
- `git diff --check` passed.
- the editor apply transaction now validates and writes one synchronized draft snapshot, preventing concurrent edits from changing the files between validation and write.
- restart scoped settings stay pending only when their candidate differs from the effective loaded value, numeric settings expose and enforce schema ranges, invalid local settings block validation and review, graph right click opens the context menu without moving a node, and structure edits serialize against the latest draft content.

## laptop gameplay acceptance

On September 24, 2026, the current candidate was loaded on the verified laptop client with
Minecraft 1.21.1, NeoForge 21.1.248, and only ProgressiveStages 3.0.5 in the client mods
directory. The dedicated server used the same candidate classes and listened on the private
Tailscale endpoint `100.76.164.109:25589`. The client log recorded the same candidate loading,
the connection to that endpoint, and a cache of 50 stage definitions. The server log recorded
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
uuid with level 4. After reconnecting to `100.76.164.109:25590`, the server recorded the owner
joining and successfully running `/stage grant EnVyOnMyMind test:jump_lock`. The client received
the stage change, and the owner confirmed that jumping worked after the grant. The client used the
same candidate jar as the server and had master volume set to zero before launch.

## open acceptance gates

The Brave editor interaction capture remains unverified because the required Brave control was not
available in this execution environment. Headless checks above do not replace that gate. The
configured block placement path remains intentional and is separate from the safe movement
default change.

## cleanup

The game test server exited. The exact test-owned `build`, `run`, and `editor-ui/node_modules` paths were removed after artifact inspection. `.codegraph` was absent in the worktree. No owned test processes remain. The shared codegraph service outside the worktree was pre-existing and was left untouched.

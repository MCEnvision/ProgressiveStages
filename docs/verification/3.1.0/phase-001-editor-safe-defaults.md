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
- `npm test -- --run` passed, 17 files and 157 tests.
- `npm run build` passed. packaged `app.js` sha256 is `46bb5ea9af3bc4cd72d4fb0c7c48151d145dae137dfa75068aa44cc5aeed3e80`. packaged `app.css` sha256 is `61fe46e00c1c89c2ccd2c120b8f11fa2771c68c529b0687aef1f59340e64d00c`.
- `./gradlew test --no-daemon` passed.
- `./gradlew build --no-daemon` passed. candidate jar sha256 is `19f91b2e78101a6c788958e5bb3a44279485e4256c821237d8fa143fcebae4e3`.
- `./gradlew runGameTestServer --no-daemon` completed 111 tests. the phase editor transaction test and other phase-owned tests completed. the task exit code was 15 because 15 pre-existing optional LuckPerms provider tests fail when that provider is unavailable. no new phase-owned failure was reported.
- `git diff --check` passed.
- the editor apply transaction now validates and writes one synchronized draft snapshot, preventing concurrent edits from changing the files between validation and write.
- restart scoped settings stay pending until their declared restart boundary, numeric settings expose and enforce schema ranges, graph right click opens the context menu without moving a node, and structure edits serialize against the latest draft content.

## open acceptance gates

The required silent laptop movement capture and Brave editor interaction capture remain unverified because those host controls were not available in this execution environment. Headless checks above do not replace those gates. The configured block placement path remains intentional and is separate from the safe movement default change.

## cleanup

The game test server exited. The exact test-owned `build`, `run`, `editor-ui/node_modules`, and `.codegraph` paths remain registered for removal after this evidence and artifact inspection finish.

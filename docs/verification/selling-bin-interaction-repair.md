# Selling Bin interaction repair

This record describes the generic interaction enforcement repair for ProgressiveStages 3.0.5 development. The production code has no Selling Bin dependency or special case.

The repaired path uses holder-aware `PrefixEntry` matching for item and block selectors. It accepts `tag:c:armors` and legacy `#c:armors`, exact `id:selling_bin:selling_bin`, and the `all:*` wildcard. The raw selector text remains unchanged in parsed definitions. A nonempty held stack is required for `item_on_block` rules. One immutable decision is shared by cancellation, feedback, and the optional server capture.

The candidate reviewed for this work is wd's Selling Bin `1.6-NEOFORGE-1.21.1`. The recorded SHA-256 is `025c96f5cf1ab531e75d11ef9ed655fe64878dd4228d73b26c419b688e3abf7d`. Its bundled `wdUtils`, Fancy Tab Sections, and Tiny Multiblock Library dependencies were recorded with the candidate manifest.

The pinned project loader is NeoForge 21.1.219. The candidate metadata requires newer NeoForge for its bundled libraries, so the exact candidate was started only in an isolated NeoForge 21.1.233 runtime. That server reached readiness and loaded the candidate. The pinned candidate runtime was rejected by dependency validation before gameplay. This is an external compatibility limitation, not evidence that the reporter's exact environment was reproduced.

Focused selector, parser, decision, and full build checks pass on the pinned project toolchain. A dedicated core startup smoke also remains independent of Selling Bin. The laptop presentation gate is not claimed here because the isolated client did not reach a responsive joined world and its application audio stream could not be verified over the available desktop connection. No release artifact is published by this work.

## September 12 interaction correction regression

The additional regression uses Minecraft 1.21.1, pinned NeoForge 21.1.219 and Java 21 on a disposable headless dedicated server on node-1. The source baseline is merged commit `1b5c6f00b25ae5c3bda410a2fa60402170f90368`. This correction is pending phase integration; the hashes below identify the tested source and artifact without treating the previous merge as proof of this change.

The new `deniedBlockUseCorrectsPredictionBeforeGrantAndRevoke` GameTest invokes `ServerPlayerGameMode.useItemOn` against a real cauldron. On the production baseline, denial preserved the authoritative bucket and block but failed the assertion `Denied block use must resend unchanged inventory state to correct prediction.` The pinned loader returns immediately when the event is canceled, including when its result is `PASS`. A canceled request reaching the block dispatcher was therefore not the demonstrated failure.

The correction explicitly resends inventory and open menu state, sends the clicked block entity's normal update packet when available, and returns `FAIL`. The same GameTest now passes inventory correction, terminal denial, a successful stage grant and bucket exchange, and offhand denial after revocation. The test passed twice. All 18 existing `InventoryInsertionGameTests` also passed individually, covering ordinary placement, swaps, dragging, partial stacks, quick movement, extraction, block targets, current rules, separate players, and playerless hopper behavior.

The owned test runtime was `.phase-worktrees/phase-003/build/interaction-denial-verification`. A temporary Gradle init selected that directory, enabled both `progressivestages` and `minecraft` test namespaces, kept the launch target `forgeserverdev` with `--nogui`, and forwarded standard input. The latter namespace is necessary because these tests use `minecraft:igloo/top`. The task graph was inspected before launch. Console commands used `test run` with each exact registered method name in lowercase. Before each case the previous result glass was cleared, and after completion the standard GameTest success beacon was verified through the server console. The new test command is:

```text
test run deniedblockusecorrectspredictionbeforegrantandrevoke
```

`./gradlew test build --no-configuration-cache --console=plain` passed with 246 unit tests in 87 suites, zero failures, errors or skips. No formatter or static analysis task is configured in this Gradle build. Dedicated startup also passed with Selling Bin and LuckPerms absent. No data providers changed.

| Tested source or artifact | SHA-256 |
| --- | --- |
| `src/main/java/com/enviouse/progressivestages/server/ServerEventHandler.java` | `67861060c4e6f7d203850ae0099b27d9778c9c6e2c31afd138b4cb2cf468e5c9` |
| `src/main/java/com/enviouse/progressivestages/server/enforcement/InteractionDenialGameTests.java` | `0cad9729a3c56b931bd569cff708a65442c44e83bb66d67cd3661c4d65c28d4d` |
| `build/libs/progressivestages-3.0.5.jar` | `472369e0b77f5d6a0f34531e9383e5d2582d8818f0b39a896ddf055598a89097` |

This proves the generic server correction and the listed container regressions. It does not establish client prediction suppression, actual Selling Bin selective sales or payouts, multiblock presentation, the paired Easy Builder workflow, or reconnect behavior. Those acceptance gates remain open. The exact candidate's bundled loader compatibility limitation above also remains open; this test does not change any platform pin.

The five targeted documentation tests also passed after the guide updates. The owned dedicated server exited through `stop`, its process was verified absent, and the disposable runtime and 274 newly created verification output files were removed after inspection. Preexisting build artifacts and shared caches were preserved. This bounded suite created no laptop client or browser process.

## Client continuation investigation

At commit `d20a64604b75013605ff22c83deadf283f0b13c2`, `ClientEventHandler` has no block interaction
prediction guard. `NetworkHandler.sendLockSync` does not transmit interaction selector pairs.
The compiled presentation snapshot also lacks their held item and target pairing. A multiplayer
client therefore cannot use those existing snapshots to make the corresponding local decision.

Inspection of the pinned NeoForge 21.1.219 patched sources established the relevant client flow.
`MultiPlayerGameMode.performUseItemOn` fires `RightClickBlock` before block or item prediction and
returns the cancellation result when canceled. Its caller still creates the ordinary
`ServerboundUseItemOnPacket`. `Minecraft.startUseItem` stops the current click on a returned
`FAIL`, before item use or another hand. These observations identify an existing event boundary
where local prediction can be suppressed while retaining the server request, authoritative
denial, correction, and feedback. No additional mixin is justified by this dispatch path alone.

This is source evidence, not a completed client repair or proof of the reported sale. A local
guard needs a bounded, compatible projection of the relevant server rules and effective access,
with grant, revoke, reload, bypass, and disconnect handling. It cannot read a server JVM registry
as a substitute for synchronization, use a broad click cooldown, or authorize a server action.
The next real bin trace must distinguish the first denied request, any subsequent request, and
the separate GUI transfer path before accepting that correction.

| Inspected source | SHA256 |
| --- | --- |
| Pinned patched NeoForge sources | `1b2646d74150e1d8f7c62489b9c2d54fe8dd3c94d4eb7074081c817ff7f60d0e` |
| `ClientEventHandler.java` | `1f105d6175b4df48c38784a9a96b10f1a534735b22408640f577ec4467f03ac0` |
| `NetworkHandler.java` | `8aa4113f6cb6296a5eb6f1a752a0227a6f6b0673ea04e1853d9058040ed88f84` |

The audit read the existing source archive in memory and created no runtime, process, downloaded
artifact, or scratch file. Platform pins and the saved goal remain unchanged.

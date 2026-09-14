# Selling Bin interaction repair

This record describes the generic interaction enforcement repair for ProgressiveStages 3.0.5 development. The production code has no Selling Bin dependency or special case.

The repaired path uses holder-aware `PrefixEntry` matching for item and block selectors. It accepts `tag:c:armors` and legacy `#c:armors`, exact `id:selling_bin:selling_bin`, and the `all:*` wildcard. The raw selector text remains unchanged in parsed definitions. A nonempty held stack is required for `item_on_block` rules. One immutable decision is shared by cancellation, feedback, and the optional server capture.

The candidate reviewed for this work is wd's Selling Bin `1.6-NEOFORGE-1.21.1`. The recorded SHA-256 is `025c96f5cf1ab531e75d11ef9ed655fe64878dd4228d73b26c419b688e3abf7d`. Its bundled `wdUtils`, Fancy Tab Sections, and Tiny Multiblock Library dependencies were recorded with the candidate manifest.

The original project loader was NeoForge 21.1.219. The candidate metadata requires newer NeoForge for its bundled libraries, so that earlier investigation started the exact candidate only in an isolated NeoForge 21.1.233 runtime. That server reached readiness and loaded the candidate. The pinned candidate runtime was rejected by dependency validation before gameplay. The current development build uses the owner selected NeoForge 21.1.248. It satisfies those declared minimums, but the earlier startup is not evidence that the reporter's exact environment or the final candidate was verified.

Focused selector, parser, decision, and full build checks pass on the pinned project toolchain. A dedicated core startup smoke also remains independent of Selling Bin. The initial laptop attempt did not reach a responsive joined world with verified application audio. The later [joined laptop verification](#joined-laptop-verification) records the successful input, presentation and reconnect checks with their remaining limits. No release artifact is published by this work.

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

This proves the generic server correction and the listed container regressions. It does not establish client prediction suppression, actual Selling Bin selective sales or payouts, multiblock presentation, the paired Easy Builder workflow, or reconnect behavior. Those acceptance gates remain open. This historical test does not establish runtime compatibility of the current 21.1.248 candidate with the exact bundled libraries.

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

## Actual Selling Bin transactions on NeoForge 21.1.248

On September 12, 2026, the eight opt in [Selling Bin transaction GameTests](../test/selling-bin.md)
passed individually on `node-1` with Minecraft 1.21.1, NeoForge 21.1.248 and Java 21. The source
baseline was `73bd6b0cb225e5870d89f133f4c7524118f7a3fc` plus the new fixture tests. Selling Bin
1.6 and its bundled runtime dependencies loaded through the ordinary loader. The exact
[artifact manifest](selling-bin-artifacts.json) records hashes, sources, declared licenses,
resolved versions and remaining dependency inspection limits.

| Case family | Manual sale | Automatic sale |
| --- | --- | --- |
| Original armor tag rule | Passed | Passed |
| Original standalone wildcard rule | Passed | Passed |
| Paired selective bread insertion with allowed carrot control | Passed | Passed |
| Previously accepted partial input and bounded merge | Passed | Passed twice |

Each case placed the actual two block bin. Both physical parts rejected missing stage direct
insertion. Grant allowed a real insertion and sale, and revoke denied the next offhand or
already open menu attempt. The paired fixture blocked normal carried stack and shift click
bread transfers while carrots remained usable without that stage. Denied held counts, input
and total currency were checked after at least 20 ticks. Automatic cases used the real bin
ticker; manual cases used the real sell all menu action. A preexisting accepted bread could
still sell while a denied new stack remained unchanged. A 63 bread input accepted only one
of three held bread after grant, preserved the remaining two, and sold exactly the accepted
amount. Every case received a freshly cleared success beacon check through the server console.

| Final tested source or artifact | SHA256 |
| --- | --- |
| `SellingBinGameTests.java` | `b21d94b69ca75f5b71267a2bb3bafd8c73c1634058fda5886cb38625261ed561` |
| Candidate JAR | `fbb78556fa0b3193f33b290a1f26e60c88fdb2cd60bd2016f8e745dfefd1487f` |

`./gradlew test build --no-configuration-cache --no-daemon --console=plain` passed with 246 tests
in 87 suites, zero failures, errors or skips. No formatter/static task is configured and no data
provider changed. A subsequent core only dedicated startup passed with the bin artifact absent
and its explicit test option disabled. The bin tests were not counted as passing in that run.

The owned runtime was `build/selling-bin-verification`, bound only to loopback. The final bin
and core runs retained `online-mode=true`; no network player was connected. Two bin runs ended
through their console `stop` command. The core startup process had no writable terminal input,
so its exact identified server JVM received SIGTERM and completed the ordinary shutdown hook,
including all world saves. Gradle reported the expected termination exit value 143 for that
smoke run; this is not a successful Gradle task exit or a gameplay failure. Source build and
unit test commands completed separately with successful exits.

This proves the listed authoritative server routes for this exact artifact and fixture. It does
not reproduce the owner's physical click sequence or prove client prediction suppression,
client inventory correction, rendered GUI access, drag and hotbar swaps in the bin, multiple
players, custom stack components, or reconnect. Those remaining matrix gates stay open. The
reported apparent automatic sale can therefore still be a client prediction or separate request
issue; these server tests alone cannot choose between those causes.

The four documentation reference checks passed, fixture JSON parsed, and the three packaged
editor assets matched their source resources. The JAR contains no fixture datapack or third
party bin classes. All owned server processes exited. Cleanup removed 715 new build
entries and 0 new local Gradle entries, including the disposable runtime and both
worlds. The 837 preexisting build entries, 26 preexisting local Gradle entries, current candidate,
shared caches and preexisting investigation runtime were preserved. Temporary decompiled
third party source, duplicate binary excerpts and scratch logs were removed after their final
consumer. This suite created no laptop or browser resources.

## Client prediction repair on NeoForge 21.1.248

The followup based on `1f78561f4a95f0e280e5842cbe164223df1e1448` adds a client
`RightClickBlock` guard and an additive `interaction_prediction` section to the existing compiled
snapshot. It carries stage IDs and exact selector pairs, including whether a nonempty held
stack is required, plus the server interaction enforcement flag. It uses the existing 16 MiB
snapshot limit, 24 KiB compressed chunks, checksum, delta and atomic activation path. No new
payload channel, public schema field, third party dependency or mixin is introduced.

The client matches live registry holders and current effective stage ownership. A missing
stage returns terminal `FAIL` before local block or item prediction. The ordinary server use
request retains authority, denial feedback and inventory correction. Creative bypass uses the
existing server supplied flag. Empty hands remain outside item rules, unrelated items and
blocks remain usable, and whole block access remains independent. Older snapshot capabilities
disable the new prediction guard, while older clients can retain the additive snapshot bytes.
Definition reload replaces selector pairs only after checksum verification, and disconnect
clears them. Snapshot delta selection also checks the exact acknowledged checksum so a reused
revision with different enforcement state cannot select the wrong base.

The NeoForge 21.1.248 userdev patch independently confirms that
`MultiPlayerGameMode.performUseItemOn` calls the existing event before item and block prediction
and returns its cancellation result. The inspected userdev JAR SHA256 is
`9278d08154a82927391ed5257481f56d6d0fcb147822c6a79ec9b5129799e79a`.
This revalidates the event boundary under the selected loader. It does not replace physical
input, actual client continuation, visible reconciliation or reconnect acceptance.

On September 12, 2026, the Java 21 wrapper command
`./gradlew test build --no-configuration-cache --no-daemon --console=plain` passed
253 tests across 88 suites, with zero failures, errors or skips. Seven new tests cover parsed
bread pairs, unrelated items and blocks, empty hands, wildcard and independent whole block
access, multiple gating stages, grant and revoke, modern and legacy armor tags with live
holder membership changes, disabled enforcement, partial snapshot activation, delta replacement,
legacy capabilities, disconnect clearing, and rejection of malformed counts, lengths and
footers without replacing valid rules. The tag fixture restores the original holder tags.

These tests establish selector and cache behavior. They do not establish the actual laptop
click sequence, receipt of server feedback, client inventory or menu rendering, the game mode
bypass transition, or the complete provider and Brave matrix. Those gates remain open.

| Current implementation artifact | SHA256 |
| --- | --- |
| `InteractionPrediction.java` | `c6559dc85843ed5d36f3744ccb5a838538cf6bc296c72dbc73c744d42417fa2c` |
| `ClientSnapshotCodec.java` | `65e226a81f0d9f9d972d8f1e91e083cb7e36a1eececdca1a5007dbfe4d26f71b` |
| `ClientCompiledSnapshotCache.java` | `77643c390efdfe1000d64132035c35d49ef551306010aafb33e1e695d4c9e2ca` |
| `ClientEventHandler.java` | `160f10b6440c4949d9a5837b9659076dfe4d7c45843ab126be9023b041efe4f6` |
| `NetworkHandler.java` | `7075c3657bad83d0f3b6fe1c9c923b8d5003d2ccd3974239e4785ec8c6fac7aa` |
| `progressivestages-3.0.5.jar` | `537b6484c662a709cd94bc27e186d38d3891c3aa0e1ac3b65d38e393ee7a0575` |

The final headless regression run used the exact Selling Bin 1.6 artifact recorded above,
NeoForge 21.1.248 and the unchanged controlled sale datapack. All eight registered actual bin
cases passed individually with fresh server verified lime success glass. They cover armor and
wildcard denial, selective bread direct and menu insertion, partial stacks, grant and revoke,
and manual and automatic sale output. The initial fixture omitted flat world layers; its
configuration was corrected and all eight cases passed again after restart. The known bundled
`wdutils` missing access transformer error remains visible and its broader compatibility impact
is unresolved. It did not prevent these specific server transactions.

A final fresh core world without optional mod JARs reached dedicated server readiness with no
error in the captured startup. The server task graph contains no client or renderer. All three
owned server runs used the same loopback only `build/prediction-verification` runtime, verified
`eula=true` before launch, and exited normally through `stop` with saved world confirmation.
The four documentation reference checks and packaged editor asset comparisons also passed.

Cleanup verified that no process retained the owned runtime. The runtime, worlds, copied bin
artifact, scratch logs and scripts, and 475 additional build entries were removed. The 837
preexisting build entries, 26 local Gradle entries, shared dependency caches, current candidate
JAR, existing investigation runtime and source were preserved. No laptop client, browser or
laptop resource was created by this suite.

## Joined laptop verification

On September 12, 2026, commit `2874f2756fb4d996433212ef8196362765dc1e9b` was exercised
with a real Prism client on `envision` and an authenticated, headless dedicated server on
`node-1`. Both used Minecraft 1.21.1, NeoForge 21.1.248 and the exact mod JARs listed in
[the bounded observations](selling-bin-client/observations.json). The ProgressiveStages JAR
SHA256 is `537b6484c662a709cd94bc27e186d38d3891c3aa0e1ac3b65d38e393ee7a0575`.
Selling Bin remains 1.6 with SHA256
`025c96f5cf1ab531e75d11ef9ed655fe64878dd4228d73b26c419b688e3abf7d`.
LuckPerms was absent from this specific bin fixture; no permission provider acceptance is
inferred from it.

The server runtime was `.phase-worktrees/phase-003/build/laptop-prediction-verification`.
The reused isolated Prism runtime was
`/home/envy/.local/share/PrismLauncher/instances/pstages-acceptance-20260912/minecraft`.
Its original configuration, mods and logs were backed up before launch. The server bound only
to `127.0.0.1:25585`, retained `online-mode=true` and received the client over the private SSH
route. Both logs confirmed the same player joined, including a second join after an actual
disconnect and Direct Connection journey. No public port or personal server list entry was added.

The client used the NVIDIA GeForce RTX 5090 Laptop GPU with driver 610.57.04. Master volume
was zero before startup. The owned Hyprland window, process and playback stream were correlated
before input, and a watcher verified application mute continuously. Resource reload recreated
stream 1039 as stream 14051; the replacement was muted and read back before further input.
Actions used the game's Use binding and native Wayland pointer input in that exact window.
Console commands prepared fixtures, granted or revoked the stage and read authoritative state;
they did not perform the tested insertion or menu actions.

The controlled price pack valued one iron chestplate at 30 emeralds, bread at 10 and carrot at 7.
The test player was in survival without operator privileges. The stage used `team_stage=false`.
The original armor rule, standalone wildcard rule, paired selective bread rules and independent
`block_right_click` rule were installed in separate definition revisions. No wildcard or whole
bin access rule was active during selective bread testing. The two physical parts were the
primary bin at `(0, -60, 0)` and its companion at `(1, -60, 0)`.

| Actual client action | Observed server and client result |
| --- | --- |
| Original `tag:c:armors` rule, primary part, automatic sale enabled | Denial left one chestplate held, no bin input or output and no open GUI. Grant allowed the sale and produced 30 emeralds. After revoke, three further Use presses preserved the restored chestplate and empty bin. |
| Standalone `all:*` rule, companion part, automatic sale enabled | Denial preserved three bread and an empty bin. Grant allowed their insertion and produced 30 emeralds. |
| Selective `id:minecraft:bread` direct and inventory rules | Bread was denied while a carrot inserted and sold for 7 emeralds without the stage. A separate empty-hand click opened the GUI. |
| Selective GUI insertion with automatic sale enabled | Left placement, right placement, hotbar swap, shift click and offhand swap could not insert denied bread. The stack remained carried or in its original inventory slot, with no new payout. Grant allowed the same shift click and produced 30 emeralds; extracting that output succeeded. Revoking while the menu remained open blocked fresh bread insertion. |
| Selective GUI insertion with automatic sale disabled | Dragging denied bread into the input preserved all three carried bread. Grant allowed the same drag and left three bread unsold in the input. One actual Sell button press consumed one bread and produced 10 emeralds. |
| Previously accepted deposits after revoke | Two bread accepted before revoke still sold, bringing the previously earned total to 30 emeralds. Fresh denied bread could not enter the empty input or create additional output through the Sell button. This follows the existing insertion boundary; deposited items are not retroactively attributed to the current viewer. |
| Disconnect and reconnect | The same muted client joined the exact private server again. Direct bread use remained denied; an empty-hand click opened the selectively gated GUI, where shift click still preserved three bread and the existing 30 emeralds without additional payout. |
| Independent whole bin access | A separate `block_right_click` rule blocked both empty-hand and bread-held clicks. Grant opened the GUI; revoke blocked repeated empty-hand clicks. After resource reload, bread-held use remained denied with the same inventory and empty bin. |

The diagnostic records preserve terminal server denial for the original armor and wildcard
fixtures and for both empty and nonempty whole bin clicks. Server inventory and block entity
observations establish the listed counts and currency, including delayed reads after denial.
The preserved images show [denied carried bread](selling-bin-client/gui_bread_denied.png),
[allowed automatic output](selling-bin-client/gui_shift_allowed.png),
[GUI denial after reconnect](selling-bin-client/reconnect_gui_denied.png) and
[whole bin denial after resource reload](selling-bin-client/resource_reload_denied.png).
The earlier headless records retain their original, narrower scope.

This closes these particular physical input, GUI and reconnect checks for that candidate. It
does not close the complete matrix: multiple real players, custom stack components, creative
and configuration bypass transitions, all hand and sneaking combinations, LuckPerms retention
and source changes, the packaged Brave authoring journey and verification of the eventual
merged default commit remain open. The bundled `wdutils` access transformer error and the
dependency manifest's unresolved license observations also remain visible.

The server exited normally through `stop` after saving every dimension. The exact owned client
and Prism processes exited, the audio watcher finished, both playback streams disappeared and
the private tunnel listener was absent. The isolated client received its original options,
instance configuration, ProgressiveStages and LuckPerms JARs, logs and crash reports back;
its complete runtime path inventory matched the prelaunch inventory. Existing saves and
unrelated instances were preserved. The disposable server's 81 new build entries and both
hosts' scratch files were removed after the evidence was retained. The 837 preexisting build
entries, 26 local Gradle entries, current candidate, shared caches and earlier investigation
runtime remain untouched. No browser resource was created by this bounded suite.

## Component, offhand and bypass followup

On September 13, 2026, the packaged candidate from
`8cdb8e08ef3b16cbe5cb09712a29a69e47f06ba2` passed the following additional actual client
checks. Its SHA256 is `8f586a6ebce9f6bbef51c59451909aaf8fa206e1f62800d1d109c40f5ba1ec13`.
[The observations](selling-bin-client/components-20260913.json) retain the exact artifact
manifest, selective fixture, server state, server decisions, input records and cleanup receipt.
This extends BIN-AC-002D, BIN-AC-002E and BIN-AC-004E evidence without claiming the whole
combined acceptance matrix is complete.

Both hosts used Minecraft 1.21.1 and NeoForge 21.1.248 with the same ProgressiveStages and
Selling Bin archives. The node-1 server used `build/bin-component-client` in the phase 003
worktree, online authentication and a loopback listener reached through the existing private
SSH connection. The same isolated Prism instance on `envision` was reused. The client log
confirmed the RTX 5090 Laptop GPU renderer and connection to the verified endpoint, and the
server confirmed the authenticated player joined. LuckPerms and FTB Teams were absent.
The player had no operator entry. Console commands prepared inventory, position, stage and
creative mode fixtures; the client placed the bin and performed every tested insertion,
offhand swap, menu opening, Sell button press and automatic sale toggle.

The fixture was a legacy TOML definition with `team_stage=false` and only the paired bread
direct use and inventory insertion rules. It contained no wildcard or whole bin access rule.
The bin's primary entity was `(0, -60, 2)`. The controlled price pack valued bread at 10 and
carrot at 7 emeralds. Bread carried a custom name, a custom data compound and an explicit
enchantment glint component. A second distinguishable stack used a different name, custom
data value and count so a forbidden swap could not pass merely by exchanging identical stacks.

| Client action | Authoritative result |
| --- | --- |
| Repeated direct Use without the stage, automatic sale on | All three bread and their components remained in the main hand. Bin input, stored progress and payout stayed empty. Captures recorded terminal `stage_missing` denials. |
| Grant, then direct Use | The three component bearing bread left the player and produced exactly 30 emeralds. |
| Revoke, restore the stack, then sneaking Use | The main hand kept all three bread and their components. The existing 30 emeralds did not change. The denial occurred at tick 10179 and the state was read at tick 10214. |
| Creative mode with bypass enabled | The same stage missing player could sell the stack, adding 30 emeralds. The capture reported `bypass` at tick 10597; the state at tick 10628 showed 60 emeralds. |
| Disable `enforcement.allow_creative_bypass` and reload | Creative use preserved a fresh three item stack and its components, with the existing 60 emeralds unchanged. A repeated request recorded `stage_missing` and visible locked feedback. |
| Survival, stick in the main hand and bread in the offhand | Main hand use opened the GUI without selling the offhand bread. The capture recorded the stick's selector mismatch. This is menu access evidence, not proof of an offhand direct use callback. |
| Offhand swap over the input, automatic sale off | Without the stage, all three component bearing bread stayed in the offhand and the input remained empty. After grant, the same action moved all three into the input with every component preserved and no payout. |
| Revoke while the menu stays open, then swap a distinct fresh stack | Two fresh bread with different components stayed in the offhand. The three previously accepted bread in the input were unchanged. |
| Press Sell, then enable automatic sale through the GUI | One accepted bread sold for 10 emeralds; the two remaining accepted bread retained their components. Enabling automatic sale sold those two, reaching 30 emeralds. Fresh denied bread remained untouched. |
| Repeat the denied offhand swap, then insert an unrestricted carrot with a hotbar swap | Fresh bread remained outside the bin. The carrot sold for 7 emeralds, bringing output to 37 with no bread sale. |
| Resource reload, reopen the GUI and repeat the offhand swap | Fresh bread remained in the offhand with its components. The input stayed empty and output remained 37 emeralds. |

The delayed server observations cover more than twenty ticks for the denial and sale checks.
The first immediate direct denial read was only six ticks after its capture; the later read
also confirmed the unchanged stack and empty bin before grant. It is not treated as sufficient
delayed evidence by itself. The images show [creative denial](selling-bin-client/component_creative_denied.png),
[accepted offhand components](selling-bin-client/component_offhand_allowed.png) and
[denial after resource reload](selling-bin-client/component_reload_denied.png).

Master volume was zero before startup and remained zero at shutdown. The owned window and
Java PID were bound to PipeWire client objects because the native playback node did not carry
a process ID directly. Only their corresponding application streams were muted. The watcher
verified stream 15345 and its replacement 26599 after resource reload before further input.
XWayland input used the verified owned Minecraft window on display `:1`.

Two fixture mistakes were resolved before gameplay acceptance: a package shaped fixture without
`[schema]` was changed to the intended legacy TOML file, and insertion used its required `target`
field. The client rejected a simulation distance of 4 and used its valid fallback. These are
fixture setup observations, not product regressions. The existing bundled `wdutils` access
transformer error remains unresolved. Initial desktop input probes are not counted as gameplay
acceptance. This suite did not repeat all hand combinations, two player behavior, reconnect,
provider retention or the packaged Brave journey.

The server saved all dimensions and exited with code 0. The owned client, launcher, supervisor
and audio watcher exited; both playback streams and the private listener disappeared. All
thirteen preserved instance roots were restored, ten original file hashes matched, and the
complete 360 entry path inventory was restored. The temporary backup was removed. On node-1,
79 test created build paths were removed, restoring the 836 build and 26 local Gradle entry
baselines. The runtime library symlink was removed without following it; the original libraries,
candidate, source and unrelated resources were preserved. Both hosts' scratch output was
removed after retaining this bounded evidence.

## September 14 final repair acceptance

The final repair candidate from source commit `31c17e7e6300b1a956e959415f73a218a994fd56`
was built and tested with Minecraft 1.21.1, NeoForge 21.1.248 and Java 21. The ProgressiveStages
JAR SHA-256 is `50f449d1eb52da2ba72a39a4a09267d9887af2f1dca29ce2233110afc24b6652`. The disposable
node-1 server used Selling Bin 1.6 and LuckPerms 5.4.150, and the laptop client used the same
JARs through the existing private loopback SSH tunnel. The laptop renderer was an NVIDIA RTX
5090 Laptop GPU with driver 610.57.04. The master volume was zero before startup and the owned
application stream remained muted.

The fixture placed a real Selling Bin at `(0,71,1)` and loaded these rules:

* `all:*` `item_on_block` to `id:selling_bin:selling_bin` for whole bin access.
* `tag:c:armors` `item_on_block` to `id:selling_bin:selling_bin` for armor access.
* `id:minecraft:bread` `item_on_block` plus `item_into_inventory` to the same block for selective bread access.

| Client action | Stage state | Authoritative result |
| --- | --- | --- |
| Empty hand right click with the whole `all:*` rule | Revoked | The menu stayed closed and the lock message was shown. See [empty hand denied](selling-bin-client/repair_empty_hand_denied_20260914.png). |
| Empty hand right click with the whole `all:*` rule | Granted | The real Selling Bin menu opened. See [empty hand allowed](selling-bin-client/repair_empty_hand_allowed_20260914.png). |
| Insert bread into the real bin input | Granted | Bread moved from the hotbar into the bin input. See [bread allowed](selling-bin-client/repair_bread_allowed_20260914.png). |
| Insert fresh bread while the existing menu stayed open | Revoked | Bread remained in the player inventory and did not enter the bin. See [bread denied](selling-bin-client/repair_bread_denied_open_menu_20260914.png). |
| Armor right click with the `tag:c:armors` rule | Revoked | The menu stayed closed and the lock message was shown. See [armor denied](selling-bin-client/repair_armor_denied_20260914.png). |

The five screenshots and their hashes are recorded in [repair-20260914.json](selling-bin-client/repair-20260914.json).
The pinned GameTest command passed all 97 required tests, including the real Selling Bin armor,
wildcard, selective and partial transaction cases. These checks establish the reported empty hand,
tagged armor and selective open-menu paths on the final candidate. They do not claim a release or
resolve the existing upstream `wdUtils` access transformer warning. The owned server, client and
private tunnel remain subject to the final integration cleanup receipt.

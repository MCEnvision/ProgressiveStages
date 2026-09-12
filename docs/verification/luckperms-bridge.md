# LuckPerms bridge verification

This record describes the Phase 002 implementation boundary. The selected compile only API is
`net.luckperms:api:5.4`. The selected runtime candidate is LuckPerms NeoForge 5.4.140 for Minecraft
1.21.1. NeoEssentials 1.0.4 build 61 is the real command side effect candidate for
`neoessentials.teleport.home.set`.

The artifact checks are bound to the following bytes. The API SHA 256 is
`086e3971ea63c0b5ad567881b2dfd955acbbffa24fe85ceac8abf54b114ce986`. The LuckPerms runtime SHA 256 is
`6b8097a7e1a27d870d3d472d00079fa958271db16b9433369dce6c7f62530b19`. The NeoEssentials runtime SHA 256 is
`60557f5942985fc538b10d0bd93c8f0065f964e1ee1f38649e3161713e74a506`.

Automated coverage parses valid and invalid inbound, outbound and command tables, rejects duplicate
row IDs and reserved contexts, preserves source configuration, and validates the optional boundary
when the provider is absent. Stage sources are attributed by owner, stage and inbound row. The
bridge supports synchronized and permanent retention, overlapping rows, true versus false or
undefined Boolean results, context matching, outbound reference counting, provider loss and a
bounded reconciliation queue.

Command gates run inside Minecraft command execution tasks after redirects and before execution
side effects. Literal descendants, argument values, aliases and namespaced literals bind to the
registered dispatcher. The effective actor is checked without changing native permission predicates.
The [command execution regression](#command-execution-regression) records the verified subset.

The real runtime acceptance matrix requires a dependency only startup, bridge disabled startup,
provider ready login, inherited group and Boolean permission queries, explicit negative values, and
the NeoEssentials home storage before and after a non operator command attempt. A denied attempt
must leave the home record unchanged. An allowed attempt must create one expected home. Provider
loss, reload, restart, rank removal, source overlap and independent stage preservation are checked
before cleanup.

Headless server evidence is retained with exact artifact hashes, fixture configuration, sanitized
capture excerpts, command and storage oracles, process shutdown confirmation and disposable path
cleanup. A laptop input or rendered feedback claim remains open until the matched silent laptop
client is joined to the disposable dedicated server and its application audio stream is verified
muted.

On 2026-09-11, the exact LuckPerms and NeoEssentials candidates loaded with Minecraft 1.21.1 and
NeoForge 21.1.219 in the disposable dedicated server. LuckPerms reported successful enablement and
the server reached `Done`. A second run with the LuckPerms jar absent reached `Done` with the bridge
dormant. The laptop login, provider-managed rank changes and real command side-effect matrix remain
unverified because the required laptop session was not available on the headless execution host.

## NeoForge 21.1.248 dependency only login failure

On September 12, 2026, the exact selected LuckPerms 5.4.140 JAR was tested alone with
Minecraft 1.21.1 and NeoForge 21.1.248. ProgressiveStages, Selling Bin and NeoEssentials were
absent. The disposable server used fresh local H2 storage, messaging disabled, automatic
translation installation disabled, online authentication and a loopback listener reached
through the existing private SSH connection. It reached readiness and reported successful
LuckPerms enablement.

The actual laptop client then failed to enter the world. At 15:38:16 server local time,
`NeoForgeConnectionListener.onPlayerLoggedIn` reached context invalidation and
`UserCapabilityImpl.getQueryOptionsCache`, which threw
`IllegalStateException: Capability has not been initialised`. The server could not place the
player in the world and disconnected it with `Invalid player data`. The earlier join message
in that same sequence is not successful login evidence. The rendered
[connection failure](luckperms-login/login_failure.png) and
[bounded observations](luckperms-login/observations.json) preserve both sides of the result.

The client ran on `envision` using the RTX 5090 Laptop GPU and NVIDIA 610.57.04, with master
volume zero before launch and its exact process playback stream verified muted. The dedicated
server ran without a GUI on `node-1` in `build/luckperms248-verification`. This reproduces the
failure without ProgressiveStages code and leaves provider dependent bridge, command and
combined acceptance open. The failure matches the earlier
[upstream 5.4.140 report](https://github.com/LuckPerms/LuckPerms/issues/4048), now independently
observed on the selected 21.1.248 loader.

The existing 5.4.150 candidate was inspected without launching it. Its bytes match the official
[CurseForge file 5971552](https://www.curseforge.com/minecraft/mc-mods/luckperms/files/5971552),
SHA256 `f161a939c7320e8a30569c37aae2c0f5bbb3cfbd77bebb233bd0f0787a2d0ef9`.
That file is labeled for Minecraft 1.21.4. The
[upstream context issue](https://github.com/LuckPerms/LuckPerms/issues/4235) also reports
problems with it on 1.21.1. It is not accepted as a replacement or as compatibility proof.
Changing the selected candidate requires an authorized plan amendment and new runtime evidence.

The owned server stopped normally and saved every dimension. Its disposable runtime and 83
new build entries were removed, preserving the preexisting build and local Gradle entries.
The laptop restoration completed after SSH connectivity returned. The client, audio watcher
and launcher had exited. The owned playback stream and test tunnel listener were absent.
The original three mod files, options, launcher configuration, logs and crash reports were
restored. The runtime path inventory matched its baseline with no added or missing paths,
and both hosts' temporary recovery files were removed. This bounded suite is cleaned up;
the failed provider login gate remains open.

## Command execution regression

On September 12, 2026, the Phase 003 candidate passed `./gradlew test build` using Java
21.0.11, Minecraft 1.21.1 and NeoForge 21.1.248. The test reports contained 259 tests across
88 suites, with no failures, errors or skipped tests. Seven command binding tests cover argument
values, descendant selection, literals after arguments, redirects, actual alias bindings,
dispatcher replacement and missing paths. No formatting task is configured; `git diff --check`
passed. No dependency or platform version changed in this repair.

The packaged `progressivestages-3.0.5.jar` SHA256 is
`f6c1b3e0b72f6ea833d4e7a8cdaa4226715bdc41366b7ca7d26f8ea1bf434101`.
Both execution mixins require their injection target. A production dedicated server containing
this JAR and no optional integration mods reached readiness at 16:24:24 America/Chicago.
Its console executed `time query daytime` before normal shutdown. This proves production
classloading and the console execution path, not provider compatibility or player login.

The development dedicated server ran the checked in
`CommandPermissionGameTests.executionRechecksStagesAfterVanillaRedirects` fixture. The exact
console invocation was `execute positioned 0 90 0 run test run executionrechecksstagesaftervanillaredirects`.
The previous result marker was cleared before the final run. At 16:23:49 America/Chicago,
`execute if block -1 118 2 minecraft:lime_stained_glass run say command_gate_final_pass`
reported `command_gate_final_pass`, confirming the successful GameTest marker.

The fixture uses a constructed server player without a login connection and disabled LuckPerms
options. It asserts unchanged world time after direct denial, redirected denial and revocation;
changed world time after a grant; native denial despite a stage; one failure callback for a
blocked custom `return`; the expected result callback when allowed; and unchanged console
semantics without a player actor. It restores previous stage definitions and all dimension times
in its teardown. An earlier fixture using a mock player failed during unnegotiated payload delivery
before command assertions. Replacing that fixture did not change production network enforcement.

The server ran without a GUI on `node-1`, in the Phase 003 worktree's
`build/command-gate-verification`, using loopback port 25589 and the existing read only library
link. No client was launched for this suite. This record does not satisfy real NeoEssentials
home storage, console delegation to a joined player, LuckPerms provider events, or laptop feedback
acceptance. Those gates remain open, as does the complete final phase acceptance matrix.

Both dedicated server runs exited normally and saved every dimension. The owned Gradle daemon
exited, port 25589 had no listener, and the disposable runtime was removed without following its
library symlink. Cleanup removed 659 additional build entries and 0 local Gradle entries created
by this suite. Preexisting output paths and shared dependencies were preserved. The temporary
launch configuration and ownership receipt were removed after evidence retention.

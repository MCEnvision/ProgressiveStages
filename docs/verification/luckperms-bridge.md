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
when the provider is absent. Core and in memory fixtures exercise source attribution, retention,
Boolean results and reconciliation. They do not prove the reflective adapter's provider behavior.
The [adapter source audit](#adapter-source-audit) records concrete discrepancies in persistence,
context queries, ownership, mutation acknowledgement and cleanup. Those gates remain open.

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

## Adapter source audit

This audit inspected the Phase 003 tree at commit
`ae61f6f3ba32f2c2a4faee6abf2bdb6e79339c2f`, whose latest production source commit is
`50ef36d55d4346f1bdcf1e196820ef04bf688bae`. It used current source, a repository search for
provider callbacks and marker lifecycle, and Java 21 `javap` against the existing API 5.4 JAR.
The API SHA256 matched `086e3971ea63c0b5ad567881b2dfd955acbbffa24fe85ceac8abf54b114ce986`.
The code index returned mixed historical worktrees, so only the identified Phase 003 source and
bounded direct inspection were used for findings. No live bridge or provider was initialized.

The API exposes distinct `data()` and `transientData()` methods, plus subject query options,
context calculator registration and context invalidation. Its
[version 5.4 permission holder documentation](https://www.javadocs.dev/net.luckperms/api/5.4/net/luckperms/api/model/PermissionHolder.html)
identifies normal data separately from session scoped transient data, which is never stored.
The [query mode documentation](https://www.javadocs.dev/net.luckperms/api/5.4/net/luckperms/api/query/QueryMode.html)
distinguishes contextual queries from queries that disregard context. API presence and source
inspection are not successful linkage, node mutation or runtime permission evidence.

| Finding | Current source evidence | Consequence and open acceptance |
|---|---|---|
| Persistent output and discarded ownership | `ReflectiveLuckPermsAdapter.mutate` invokes `data()`, adds or removes a node, then invokes `saveUser`. Its `ownerKey` argument is unused. | The adapter attempts persistent writes. The generic reserved context is not an exact ownership manifest. BIN-AC-012E and BIN-AC-012F remain open, including administrative equal nodes and restart cleanup. |
| Context and input exclusion mismatch | Both `snapshot` and `permission` use `QueryOptions.nonContextual()`. The snapshot copies that query's contexts. `LuckPermsBridge.matches` then excludes values by name through `isBridgeOwned`. No reserved marker calculator registration was found in the production source. | These queries do not implement authoritative current contexts or exclusion of bridge only inherited paths. A same name external contribution is also rejected when the manifest contains that value. BIN-AC-012B, BIN-AC-012E and BIN-AC-012F require real contextual and independent source proof. |
| Unacknowledged provider mutation | Adapter mutation methods return `void`, silently return for an unavailable user, and catch reflection or runtime failures. The bridge records success and replaces the manifest regardless. | Diagnostics and the manifest can claim an addition or removal that did not happen. A failed addition can suppress a later retry because it is recorded as already owned. BIN-AC-012E and BIN-AC-012F need explicit outcomes, retry and failure visibility. |
| Same row replacement leaves the old node | `reconcileOutbound` adds a changed value under the same stage, row and context index key. Its removal loop skips an old entry whenever that key still exists. | Changing a row from permission A to B can leave A while the manifest retains only B. The same issue applies to changed context values. BIN-AC-012E and BIN-AC-012F require replacement and subsequent disable cleanup coverage. |
| Shutdown forgets output before cleanup | `LuckPermsBridge.shutdown` shuts down the adapter and clears `outboundManifest`. Adapter shutdown clears the group cache and API reference. Neither path removes the recorded nodes. | Shutdown is not an owned node cleanup path. The normal data writes above can outlive the in memory record if provider calls succeed. BIN-AC-012E and BIN-AC-012F remain open. |
| Missing negative check and incomplete event convergence | Outbound permission publication has no independent explicit negative query. The bridge subscribes to stage changes and NeoForge `PermissionsChangedEvent`; no LuckPerms user or group event subscription was found. Queue overflow clears entries and requests an online player rescan without a resumable cursor. | Explicit negative precedence, provider event invalidation, offline contributors, stale completion guards and bounded overflow convergence are not established. BIN-AC-012C through BIN-AC-012F require their planned fixtures. |

The empty permission map in `SubjectSnapshot` is not evidence that every permission condition
fails: `matches` calls `adapter.permission` separately. Reflection through concrete implementation
classes may present additional linkage problems, but that remains a hypothesis until exercised
against the exact provider. This audit does not claim that a particular production account was
modified or that the failed login fixture executed any ProgressiveStages adapter code.

These are existing BIN-REQ-012 and SHARED-004 implementation gaps, consumed by the final
BIN-REQ-014 acceptance and P003-TASK-005 through P003-TASK-007. The saved plan, goal and phase cursor
remain unchanged. P002-TASK-001 still requires dependency only and disabled bridge login, exact
API linkage and provider query exclusion before live bridge initialization. NeoForge remains
21.1.248; changing the provider candidate is not an implicit part of repairing this adapter.

The next repair must cover acknowledged transient mutations, exact owned references, replacement
and cleanup before dropping provider access, authoritative contextual queries and independent
input exclusion together with their affected lifecycle tests. A method rename from `data()` to
`transientData()` alone cannot close these gates. The runtime candidate's dependency only login
failure remains a separate prerequisite.

This bounded audit created no test runtime, world, process, downloaded artifact, scratch report
or cache. It read the preexisting shared API cache without changing it. Only the requested
documentation and this sanitized evidence are retained. No new runtime acceptance was claimed.

## Outbound node regression

The outbound repair replaces reflective persistent writes with an optional typed helper using
API 5.4 `User.transientData()`. It does not call `data()` or `saveUser`. Created nodes carry a
random private metadata token, with separate reference sets for each subject, node kind, value
and context map. Cleanup selects both the exact node and its token. An equal administrative node
without that token is not adopted or removed. This distinction is necessary because the
[API node equality contract](https://www.javadocs.dev/net.luckperms/api/5.4/net/luckperms/api/node/Node.html)
excludes metadata from equality; an ordinary equal node removal would not establish ownership.

The bridge tracks each row contribution, including ambiguous write attempts. A changed row removes
its old contribution before adding the replacement. Failed removal blocks replacement for that
row and retains the old reference for retry. Multiple rows sharing a node keep independent
references. Applied, unavailable, conflict and failed mutation outcomes reach diagnostics without
reporting a failed operation as a successful addition. Recovery after a failed addition records
the later applied result.

Logout attempts subject cleanup. Shutdown removes tracked and pending output before releasing
provider access; incomplete cleanup retains references and the adapter and prevents a new bind
from silently replacing them. This is a retry boundary, not proof that an unavailable provider
has removed its nodes. Historical persistent nodes from older candidates are not automatically
adopted or deleted by the new transient ledger.

Java 21 `./gradlew test build --no-daemon --no-configuration-cache` passed with 335 tests across
96 suites, no failures, errors or skipped tests. The task graph was inspected before execution
and starts no client, display or renderer. The repository has no formatter or static analysis
task in that graph; `git diff --check` passed. The fifteen new tests cover:

- Seven exact API interface fixtures for idempotent references, equal administrative nodes,
  administrative replacement, an exception after insertion, rejected addition retry, unavailable
  user and failed cleanup recovery, and distinct node kinds and contexts. These fixtures use
  dynamic proxies implementing the pinned API. Any persistent user data or save call fails the
  fixture. They do not initialize a LuckPerms provider or prove its concrete implementation.
- Six core tracker tests for same row replacement order, overlapping owners, failed removal,
  ambiguous write recovery, context and kind replacement, and independent permission preservation
  in the in memory model. The model's explicit negative result is not real provider precedence
  evidence.
- Two bridge shutdown tests for removal of overlapping output while preserving external membership
  and refusal to replace an adapter with unresolved cleanup.

The scoped privilege review covered the transition from stage output to provider mutation and
the deletion boundary. Node equality alone is insufficient, failed writes cannot be treated as
confirmed state, and an unavailable user cannot be assumed cleaned. The regression fixtures cover
those cases. Remaining confirmed gaps are noncontextual inbound queries, value based independent
input exclusion, missing marker lifecycle and independent negative checks, and incomplete provider
event, offline and stale generation handling. Their existing BIN-AC-012 gates remain open. This
repair does not initialize the live bridge before P002-TASK-001 or change the selected runtime,
NeoForge 21.1.248, Minecraft 1.21.1 or other platform pins.

## Command execution regression

The implementation and checked in regression fixtures are bound to source commit
`f458361a79e13513483e6a0084d02fea4236669b`. The artifact below was built from those source bytes.

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

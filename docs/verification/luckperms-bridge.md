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
context queries, ownership, mutation acknowledgement and cleanup at the audited revision.
The repairs below provide bounded regression evidence; real provider acceptance remains open.

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

## Offline contributor regression

Source commit `37b597704e49e91e80ff0bdbe4606a4b8c916c5f` adds bounded offline provider observations to
P003-TASK-005 correction work for BIN-REQ-011 and BIN-REQ-012. The loader retains at most eight
pending or unacknowledged results. It queries configured inbound Boolean keys and inherited groups
using static provider contexts, excluding the reserved bridge marker and any remembered player
world. Existing loaded users are borrowed; a user loaded for the query receives `cleanupUser`
after observation. Cleanup failure retains the reference and prevents trusted application.
Shutdown suppresses late notifications and cannot replace an adapter with unfinished cleanup.
Owned load and cache eviction events do not create a repeated offline reload cycle.

Server reconciliation captures definition identity, compiled revision and resolved owners, then
rejects a changed context before mutation. A provider event can invalidate a completed observation
until acknowledgement. Invalidation during application removes newly added permanent sources and
leaves synchronized input inactive. Current dependencies, slot policy and purchase qualification
remain enforced without granting prerequisites, buying stages or firing acquisition events.
Independent and permanent sources remain separate from each subject's synchronized contribution.
Online beneficiaries receive a fresh effective snapshot and bulk change notification.

The persisted contributor index supports a UUID cursor that continues after earlier subjects are
removed. Online players and offline contributors share the 256 entry queue and sixteen subject
operations per tick. The exact [FTB Teams 2101.1.9 artifact](https://maven.ftb.dev/releases/dev/ftb/mods/ftb-teams-neoforge/2101.1.9/ftb-teams-neoforge-2101.1.9.jar),
SHA256 `c0e4fcb2e349dd24dd3bddb1bcda6c61dad3c1db38e3bf1100e5da9c2753e571`,
was inspected with `javap`. Its API exposes `TeamManager.getTeamForPlayerID(UUID)` and `Team.getId`.
The optional boundary uses these signatures for offline ownership and treats lookup failure as
unknown ownership. This signature check does not prove real FTB membership event behavior.

On September 13, 2026, Java 21.0.11 `./gradlew test build --no-daemon --no-configuration-cache`
passed on Minecraft 1.21.1 and NeoForge 21.1.248. All 386 tests across 103 suites passed with zero
failures, errors or skips. Seven new isolated API tests cover bounded loads, acknowledged slots,
static contextual queries, borrowed users, provider failures, cleanup retries, invalidation,
owned and external cache lifecycle events, shutdown and late results. A source index test covers
removal during scanning and pending source preservation. No separate formatter or static analysis
task is configured; `git diff --check` passed. The final postcommit build passed and produced a
clean source manifest. No platform dependency or data generation provider changed.

The inspected `runServer` graph starts no client or renderer. The final development server reached
readiness at 00:27:54 America/Chicago and ran these actual GameTest dispatcher fixtures.

| Fixture | Started | Matching metadata and fresh lime marker observed |
|---|---|---|
| `offlineresultsrejectstaledefinitionsandunqualifiedgrants` | 00:28:49 | 00:31:09, `offline_stale_results_pass` |
| `offlinerescanconvergeswhilecontributorsareremoved` | 00:31:09 | 00:35:32, `offline_rescan_pass` |
| `offlinesourcesrevalidatewithoutaplayerandpreserveotherowners` | 00:35:32 | 00:35:57, `offline_sources_final_pass` |
| `overflowingpermissioneventsreacheveryonlinesubject` | 00:35:57 | 00:36:23, `offline_online_queue_regression_pass` |
| `permissionsourcespreserveothersubjectsandindependentearnings` | 00:36:23 | 00:36:48, `offline_online_sources_regression_pass` |

The structure block was at `0 181 3` and the success glass at `-1 180 2`. Released chunks were
force loaded before inspection, and the success glass was cleared before each subsequent test.
The offline scan fixture visits all 300 persisted contributors exactly once within 128 simulated
bridge ticks, respects both work limits, removes obsolete sources and leaves no jobs, queue or
rescan. The source fixture verifies personal and server owners, fixed server context qualification,
world context loss, two contributing subjects, permanent retention and independent preservation.
The stale result fixture rejects old definitions and owners, refuses unqualified dependency and
purchase grants, then checks that invalidated input cannot leave a permanent entitlement.

An earlier development run failed the stale definition fixture at 00:23:12 because the fixture
attempted duplicate registration, which `StageOrder` rejects. Clearing the isolated stage registry
before replacement correctly models reload and preserves the same denial assertion. That run
stopped normally at 00:25:54; the corrected fixture passed in the final run above. The final
development server stopped at 00:36:48, saved all dimensions and exited normally.

The packaged `progressivestages-3.0.5.jar` has SHA256
`25f286ac6ce06792f368ae8444523fb45d2c6dfd8a2e627d56014f301558cb8d`. Its manifest identifies the source
commit above and `Build-Dirty: false`. All 752 project classes match compiled output byte for
byte, and no `net/luckperms` API classes are bundled. The same artifact without optional mods
reached production dedicated readiness at 00:38:33. At 00:38:54, `time query gametime` returned
16455 and `stage debug permissions status` reported stopped capture, zero records and an idle
writer. The owned runtime uses online authentication and `127.0.0.1:25589`, with EULA true read
back before every launch. Both development runs and the packaged smoke used only `node-1` at
`/mnt/hermes/projects/ProgressiveStages/.phase-worktrees/phase-003/build/offline-permissions-verification`.

The production server stopped at 00:39:56, saved all dimensions and exited normally. Cleanup
confirmed all three owned server processes were absent and the loopback port was available.
All 1,020 added build paths were removed, restoring the exact 836 path baseline. The 26 preexisting
`.gradle` paths remained unchanged with no additions. The runtime and three metadata scratch
files were removed after their final consumer. The preexisting `run`, `run-248`, shared libraries,
dependency caches, source and candidate artifact were preserved. No cleanup resource remains.

This bounded evidence does not close BIN-REQ-012 or combined acceptance. The exact provider login
failure remains separate. Real provider and FTB lifecycle behavior, complete membership revision
guards, source expiry and suppression episodes, native cached permissions, real convergence time,
and matched client and browser acceptance remain open. The recording adapters and constructed
players do not substitute for those gates. No laptop or browser resource was created, and no
release or phase integration is claimed. The plan, immutable goal and cursor remain unchanged.

## Provider event regression

Source commit `55eae9800a0062611929dee4acd006a08d8d1850` connects provider user, node,
group, synchronization and configuration reload changes to the existing bounded reconciliation
queue. API 5.4 subscription handles are retained for deterministic cleanup. Closing disables
delivery before attempting every detachment; failures retain inactive handles for retry.
Subject changes invalidate only the affected calculator projection. Group and global changes
advance a projection generation and request a resumable rescan without enumerating players on
the callback thread. Callbacks neither access stage storage nor load provider users.

The [API event bus contract](https://raw.githubusercontent.com/LuckPerms/LuckPerms/v5.4/api/src/main/java/net/luckperms/api/event/EventBus.java)
defines subscription and detachment handles. Exact API 5.4 signatures were checked from the
recorded dependency artifact before implementation. Cache recalculation events are excluded
because bridge queries and context publication can themselves cause recalculation. Provider
context notifications remain in server reconciliation; this change does not prove immediate
invalidation of previously cached native permission answers.

On September 12, 2026, Java 21.0.11 `./gradlew test build --no-daemon --no-configuration-cache`
passed with Minecraft 1.21.1 and NeoForge 21.1.248. All 378 tests across 102 suites passed with
zero failures, errors or skips. Three new API boundary tests cover event routing, repeat
registration, late delivery after closure, retryable failed detachment and partial registration
cleanup. A calculator test verifies subject and global invalidation without provider queries or
context notifications, rejection of old publication tickets and recovery with a fresh ticket.
No separate formatter or static analysis task is configured. `git diff --check` and the final
source build passed; the postcommit rebuild passed with a clean source manifest.

The inspected `runServer` task graph starts a development dedicated server, with no client or
renderer. It reached readiness at 23:50:06 America/Chicago. The server executed the following
core fixtures through the actual GameTest dispatcher.

| Fixture | Started | Matching metadata and lime success marker observed |
|---|---|---|
| `projectioncontextsfollowconfirmedoutputandlifecycle` | 23:51:32 | 23:52:46, `provider_events_projection_pass` |
| `overflowingpermissioneventsreacheveryonlinesubject` | 23:52:46 | 23:53:14, `provider_events_budget_pass` |
| `permissionquerieswithdrawdeniedandunavailableoutput` | 23:53:14 | 23:53:34, `provider_events_query_pass` |

The template's actual structure block was at `0 180 3`, with its success glass at `-1 179 2`.
The first result inspection used a different height and was not accepted as evidence. The queue
fixture was rerun after locating the structure; each accepted result had matching metadata and
a fresh success marker. Released test chunks were force loaded before inspection. The queue
fixture includes the existing 300 subject overflow and reload checks, plus 1,200 subject callbacks
and a global callback through the installed adapter subscription. Callback delivery performs no
subject query; the queue stays at or below 256 entries. At most sixteen subjects reconcile per
simulated bridge tick, and all 300 subjects are visited within 64 iterations with no remaining
queue or rescan work. Constructed players and the recording adapter do not establish real provider
event delivery, offline convergence or the separate 60 second production acceptance gate.

The development server stopped at 23:53:45, saved all dimensions and exited normally at 23:53:48.
The packaged `progressivestages-3.0.5.jar` has SHA256
`d5486500d0fab140e6ec462acdcba8d8f284a2ee5891f56ef019e14bf741cdfe`.
Its manifest records the source commit above and `Build-Dirty: false`. All 741 project classes
match compiled output byte for byte; no `net/luckperms` API classes are bundled.

The same packaged artifact, without optional mods, reached production dedicated readiness at
23:55:06. At 23:55:18, `time query gametime` returned 4619. At 23:55:26,
`stage debug permissions status` reported stopped capture, zero records and an idle writer.
The server stopped at 23:55:38, saved every dimension and exited normally. Both launches used
the owned `node-1` runtime at
`/mnt/hermes/projects/ProgressiveStages/.phase-worktrees/phase-003/build/provider-events-verification`,
online authentication and `127.0.0.1:25589`. EULA true was read back before each launch.

Cleanup confirmed that both owned server processes exited and the loopback port was available.
All 979 paths created under `build` were removed, restoring its exact 836 path baseline; the
26 preexisting `.gradle` paths were preserved with no additions. The disposable runtime and
three metadata scratch files were removed. The preexisting `run`, `run-248`, library target,
shared dependency caches and candidate artifact were preserved. No cleanup resource remains.

This evidence does not close BIN-REQ-012. The exact provider dependency only login failure,
native cached permission behavior, offline contributor loads, stale completion guards across
all revisions, source episodes, real context transitions and combined client acceptance remain
separate gates. No laptop or browser resource was created, no release was published, and the
immutable goal, phase cursor and plan set remain unchanged.

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

Source commit `23d5733fc2b5a2d6b5018740f7af19e13e2d4c75` contains this repair and its regression
fixtures. The final packaged artifact below was built from that clean source revision.

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

The resulting `progressivestages-3.0.5.jar` SHA256 is
`9148c753e669740865fead68abda8c5e9e81ed9eedd37ea28a8c90148c994e37`. Its manifest identifies the
source commit above and `Build-Dirty: false`. All 726 project class entries matched the compiled
output byte for byte, and the JAR contains no `net/luckperms/` API classes. API 5.4 was added only
to the test dependencies so the interface fixtures can execute; it remains compile only for
production packaging.

On September 12, 2026, the exact packaged candidate ran in the disposable
`build/luckperms-node-verification` runtime inside the Phase 003 worktree on `node-1`. It used
Java 21.0.11, the existing read only NeoForge 21.1.248 library link, the verified `forgeserver`
launch target and `--nogui`, loopback port 25589, online authentication and a read back
`eula=true`. Only ProgressiveStages was installed. The server reached `Done` at 21:14:39
America/Chicago, initialized `neoforge:default_handler` and loaded 50 default stage definitions.
At 21:15:07, console `time query daytime` returned 562 and `stage debug permissions status`
reported stopped capture with zero records and an idle writer. This proves packaged common and
dedicated server classloading with LuckPerms absent, not provider mutations or player login.
No client, display, renderer or live LuckPerms bridge was launched.

The server stopped through standard input, saved every dimension and exited normally at 21:15:15.
The owned process and port listener were absent before cleanup. The disposable runtime, world,
logs, generated configuration, added test and build paths, and temporary ownership receipt were
removed after their final consumers. The build pruned the obsolete compiled
`LuckPermsBridge$OutboundEntry.class` because that nested record was removed from source; it was
not restored as stale bytecode. No other baseline paths were missing and no new disposable paths
remained. The intended candidate JAR, source, evidence, preexisting runtimes and shared dependency
caches were preserved. The goal and cursor hashes remained unchanged. The owned Gradle runs were
terminal and left no single use daemon running.

## Projection context regression

Source commit `b75c04e55364b0da477fb21165e46ab6bbf02807` supplies the reserved context needed
by the existing outbound nodes. Previously those nodes required `progressivestages_bridge=active`,
but the adapter registered no calculator to provide it. `LuckPermsProjectionContexts` now publishes
the marker for the current platform player object after confirmed node reconciliation. A ticket
captured before eligibility queries must still be valid at publication. Dirty subject processing,
reload, disconnect and shutdown invalidate the marker before reevaluation or node cleanup.
Unconfirmed writes and invalidated tickets cannot publish it. Replacement player objects do not
inherit old tickets. Context notification and calculator teardown failures remain visible and
retain the adapter for retry. No persistent permission mutation or new configuration field was
introduced.

The exact API 5.4 JAR was inspected with Java 21 `javap` for `ContextCalculator`, `ContextConsumer`
and `ContextManager` signatures. The upstream
[calculator contract](https://raw.githubusercontent.com/LuckPerms/LuckPerms/master/api/src/main/java/net/luckperms/api/context/ContextCalculator.java)
requires fast concurrent lookups without recursive context queries. The implementation reads only
its concurrent projection map in the calculator. The upstream
[NeoForge context manager](https://raw.githubusercontent.com/LuckPerms/LuckPerms/master/neoforge/src/main/java/me/lucko/luckperms/neoforge/context/NeoForgeContextManager.java)
identifies `ServerPlayer` as the platform target. These references and signature checks are static
evidence, not exact runtime registration, cache invalidation or player login proof. CodeGraph
returned mixed projects and historical worktrees; bounded active source inspection filled the
missing adapter and caller coverage.

On September 12, 2026, Java 21.0.11 `./gradlew test build --no-daemon --no-configuration-cache`
passed with Minecraft 1.21.1 and NeoForge 21.1.248. All 374 tests in 101 suites passed without
failures, errors or skips. Five calculator tests cover activation, unrelated targets, old tickets,
replacement sessions, concurrent invalidation, failed notifications, all marker withdrawal before
shutdown notifications, and retryable unregistration. A bridge shutdown regression proves that
successful node removal cannot discard an adapter whose calculator teardown failed. No separate
formatter or static analysis task is configured; `git diff --check` passed. The final source build
and postcommit clean rebuild both passed.

The development dedicated server reached readiness at 23:20:58 America/Chicago. Its inspected
`runServer` graph started no client or renderer. The actual GameTest dispatcher ran these fixtures
sequentially. Each result was checked against its structure metadata and fresh lime success marker;
the previous marker was cleared before the next test, and the released test chunks were force
loaded again before inspection.

| Fixture | Started | Matching metadata and success marker observed |
|---|---|---|
| `overflowingpermissioneventsreacheveryonlinesubject` | 23:21:49 | 23:22:28, `permission_reload_budget_pass` |
| `projectioncontextsfollowconfirmedoutputandlifecycle` | 23:22:49 | 23:23:49, `permission_projection_lifecycle_pass` |
| `permissionquerieswithdrawdeniedandunavailableoutput` | 23:24:03 | 23:24:57, `permission_query_after_projection_pass` |

The queue fixture retains its 300 synthetic online subjects and overflow assertions. It now also
requests a reload scan, proves the reload call performs no immediate subject queries, revisits all
300 subjects and asserts at most sixteen reconciliations on each simulated bridge tick. The
projection fixture uses the real bridge with a recording adapter to prove inactive output during
mutation and cleanup, failed write denial, confirmed publication, immediate reload invalidation,
stale query rejection, dirty subject invalidation, disconnect cleanup, unavailable permission
withdrawal, provider failure and recovery. The existing independent FALSE and unavailable query
regression passed afterward. The server stopped at 23:25:09 and saved every dimension before
normal exit. These are core fixtures and isolated API tests, not real provider event or client
acceptance.

The clean `progressivestages-3.0.5.jar` SHA256 is
`87addfcbde61ac5e0a8fabb0e2d1d4f00cadbe5a60aaf256ef320cf76f807f83`.
Its manifest identifies the source commit above with `Build-Dirty: false`; all 740 production
classes match compiled output, and no LuckPerms API classes are bundled. GitHub verified the
source commit's SSH signature. The production dedicated server with this JAR and no optional
integration mods reached readiness at 23:27:48. At 23:28:31 it returned daytime 5876 and stopped
permissions capture with zero records and an idle writer. It stopped at 23:28:51 and saved all
dimensions before normal exit. Neither inspected runtime log contained an error or fatal failure.

Both modes used `build/projection-context-verification` inside the existing Phase 003 worktree on
`node-1`, authenticated mode, loopback port 25589 and read back `eula=true`. Production used the
preexisting NeoForge 21.1.248 libraries through a read only link. No laptop, browser or live optional
provider was launched. The selected provider's dependency only login failure remains open.
Real provider calculator registration and cache behavior, complete user and group event coverage,
inherited exclusion, native permission use, bounded offline work, source expiry and suppression,
joined client behavior and full lifecycle convergence remain mandatory gates. This increment does
not close BIN-REQ-012 or combined acceptance.

Cleanup confirmed both owned server processes absent, every owned Gradle and server handle terminal,
and port 25589 available. It removed 1001 added build paths and no added local Gradle paths,
including the runtime, fixture JAR, world, logs, reports and compiled test output. The library
symlink was removed without following its target. Both path inventories matched their pretest
baselines exactly, with no missing preexisting path. The candidate retained its recorded hash;
source, evidence, preexisting runtimes and shared caches were preserved. The temporary launch
script, ownership receipt and scratch directory were removed. The plan, saved goal and active
phase cursor hashes remained unchanged.

## Saved source activation regression

Source commit `33373f85f233351c6181841b5b7bbbdad457bedd` separates stored permission sources
from effective access after data loading. Synchronized sources start inactive after codec decode;
independent and permanent sources remain effective. Activation is not serialized, while an in
process copy preserves the current activation state. The stage manager filters pending sources
from actor and legacy UUID access, effective lists, dependency views and source explanations.
Revalidation activates the existing contribution without manufacturing independent ownership or
repeating its acquisition event and timestamp. Explicit API, command and script bulk revocation
enumerate stored entitlements. The starter grant guard also uses stored progression, so pending
access does not make a returning player eligible for another first join starter grant.

On September 12, 2026, Java 21.0.11 `./gradlew test build --no-daemon --no-configuration-cache`
passed on Minecraft 1.21.1 and NeoForge 21.1.248. All 368 tests across 100 suites passed with no
failures, errors or skips. Four new unit tests cover all owner kinds, unchanged serialized records,
reactivation, retained independent and permanent sources, copy isolation, serialization of active
data, known legacy synchronized labels, highest stage filtering and bulk removal. No separate
formatter or static analysis task is configured; `git diff --check` passed. The postcommit clean
build passed with unchanged Java test inputs. No dependency or platform version changed.

The final development dedicated server reached readiness at 22:55:58 America/Chicago. The exact
configured task graph was inspected and started no client or renderer. Its GameTest dispatcher
ran these fixtures sequentially, clearing the success marker before each run and reloading the
test chunks before inspecting the structure metadata and fresh lime glass marker:

| Fixture | Started | Matching metadata and success marker observed |
|---|---|---|
| `loadedpermissionsourcesrequireauthoritativerevalidation` | 23:01:11 | 23:01:34, `permission_load_final_pass` |
| `pendingpermissionstagesdonotrepeatstartergrants` | 23:01:48 | 23:02:06, `permission_starter_final_pass` |
| `permissionsourcespreserveothersubjectsandindependentearnings` | 23:02:19 | 23:02:48, `permission_sources_after_load_pass` |

The load fixture decodes saved data and installs it as the actual overworld attachment. Through
the real stage manager and a controlled provider adapter, it asserts pending actor and legacy
access denial, retained independent and permanent source kinds, explicit removal of an inactive
entitlement, qualified reactivation with the original acquisition timestamp, and no mutation on
repeat reconciliation. The starter fixture temporarily changes cached starter settings, proves
that pending only progression prevents a repeated grant, then removes that progression and proves
the configured starter still grants to a player without stored stages. Both fixtures restore
attachments, stage definitions and timestamps; the starter fixture restores its cached settings.
The existing source ownership fixture passed afterward. The development server stopped at
23:03:00, saved all dimensions and exited normally.

These tests use constructed players without login connections. Codec decode and attachment
replacement are not a physical process restart or a real provider query. Native KubeJS bulk
revocation, joined beneficiary synchronization, actual provider restart and offline convergence,
context invalidation, legacy contributor migration, expiry, episode suppression and complete
acquisition effect behavior remain open. No complete BIN-REQ-011 or BIN-REQ-012 result is claimed.

The clean `progressivestages-3.0.5.jar` SHA256 is
`2c58f9ae3974dcfbd409e4b327399fc8c8ca679281850460c8f07df4a7e50413`.
Its manifest identifies the source commit above and `Build-Dirty: false`. All 738 production
classes match compiled bytes, and no LuckPerms API classes are bundled. GitHub verified the
pushed source commit's SSH signature. A production server with that exact JAR and no optional
integration mods reached readiness at 23:04:04. At 23:04:22 its console returned daytime 10849
and stopped permissions capture with zero records and an idle writer. It stopped at 23:04:38,
saved every dimension and exited normally. The inspected final development and production logs
contained no errors or fatal failures.

Both modes reused the owned `build/permission-startup-verification` directory inside the existing
Phase 003 worktree on `node-1`, with online authentication, loopback port 25589 and verified
`eula=true`. Production used the existing NeoForge 21.1.248 libraries through a read only link.
No laptop, browser or live optional provider resource was created. The selected provider's
dependency only login failure remains a separate prerequisite. The plan validation confirmed
DEC-006 already requires exactly 21.1.248 throughout; no plan, goal or phase cursor changed.

Cleanup confirmed every owned server process absent, every owned Gradle and server handle terminal,
and port 25589 available. It removed exactly 1120 added build paths and no added local Gradle paths,
including the runtime, worlds, logs, reports, compiled test output and fixture JAR. The libraries
link was removed without following its target. Final path inventories matched both pretest
baselines, with no preexisting path missing. The candidate JAR retained the recorded hash; source,
tracked evidence, preexisting runtimes and shared caches were preserved. The temporary launch
script, ownership receipt and metadata directory were removed. Plan validation scratch was also
removed, and the final plan, goal and cursor hashes matched their original values.

## Obsolete owner regression

Source commit `f476cbfdd85ee7f8ffcf5f61f76b1fbde8ba150d` adds a subject lookup over attributed
permission sources. The lookup is reconstructed from the existing persisted source records during
load and copy, and maintained during additions, individual revocation, stage removal, bulk
replacement and owner removal. It introduces no persistent schema field and returns immutable
snapshots. Subject reconciliation withdraws only that subject's synchronized contributions from
obsolete owners or deleted definitions before evaluating new grants. Independent, permanent and
other subject records remain. Committed invalidation identifies the original affected owners;
online beneficiaries receive refreshed views and a bulk event. The existing FTB membership detector
calls reconciliation before its team synchronization path.

On September 12, 2026, `./gradlew test build --no-daemon --no-configuration-cache` passed with
Java 21.0.11, Minecraft 1.21.1 and NeoForge 21.1.248. All 364 tests in 100 suites passed without
failures, errors or skips. Three added unit tests cover distinct subject and owner namespaces,
immutable lookup snapshots, codec and copy independence, bulk replacement, owner removal and
unattributed legacy labels. No separate formatter or static analysis task is configured;
`git diff --check` passed. The postcommit build passed with unchanged test inputs.

The dedicated development server reached readiness at 22:36:50 America/Chicago. Its actual
GameTest dispatcher ran `permissionownerchangeswithdrawstalecontributions`. The fixture seeded
obsolete personal and team contributions for a constructed subject, then reconciled a current
server scoped definition using a controlled adapter. It asserted preserved independent, permanent
and other subject contributions, last old owner removal, withdrawal before the new source grant,
correct affected owners, unchanged revision on repeated cleanup, deleted definition withdrawal and
no stale resurrection after restoring an ineligible definition. It restores subscriptions, data,
adapter state and definitions in teardown. This is a core owner transition fixture, not actual FTB
membership or joined client evidence.

At 22:37:34, the structure metadata identified the exact owner test and its fresh lime marker
reported `permission_owner_regression_pass`. The test chunks were force loaded again before marker
inspection because the framework had released them. The existing
`permissionsourcespreserveothersubjectsandindependentearnings` regression then ran; at 22:38:11 its
own metadata and cleared/fresh lime marker reported `permission_sources_after_owners_pass`.
The development server stopped through standard input at 22:38:28 and saved all dimensions before
normal exit. The task graph was inspected before launch and included no client or renderer.

The clean `progressivestages-3.0.5.jar` SHA256 is
`be8004e9148edb562f236280bdd9b085bba783770157bc25f6db186723b612c3`.
Its manifest identifies the source commit above and `Build-Dirty: false`. All 737 compiled production
classes match the packaged bytes and no LuckPerms API classes are bundled. GitHub verified the
pushed source commit's SSH signature. A production server with this exact JAR and no optional
integration mods reached readiness at 22:39:29. At 22:39:57 the console returned daytime 2507 and
permission capture status stopped, with zero records and an idle writer.

Both modes used the existing Phase 003 worktree's `build/permission-owner-verification` on
`node-1`, authenticated mode and loopback port 25589. Each launch read back EULA acceptance.
No client, browser, real LuckPerms or FTB Teams runtime was launched. The scoped audit found no
new provider API dependency, administrative permission mutation, persistent format change or
cross-subject deletion. Exact FTB event timing, joined beneficiary synchronization, offline
contributors, startup eligibility, legacy attribution, expiry/suppression and complete acquisition
effect behavior remain open. These results do not close the full BIN-REQ-011 or BIN-REQ-012 gates.

The production server stopped through standard input at 22:40:43, saved all dimensions and exited
normally. Cleanup confirmed both server processes absent, all owned Gradle/server handles terminal
and port 25589 closed. It removed 968 added build paths and 0 added local Gradle paths,
including the runtime, fixture JAR, logs, world, reports and compiled test output. The library
symlink was removed without following its target. Final inventories matched both pretest baselines
exactly. The intended candidate, source, preexisting runtimes and shared caches remain preserved.
The metadata scratch directory and launch script were removed. No laptop resource was created.
The saved goal and active phase cursor hashes remain unchanged.

## Inbound source regression

Source commit `dd7162b0c7628bce75273ac2fd0eb46be8c1baa6` separates inbound contributors by
player UUID, row ID and retention mode within the resolved owner. A first derived grant no longer
creates an independent grant. A preexisting stage with no source labels retains its legacy
independent meaning when a contribution is added. Source explanations recognize attributed and
older row labels as derived. Reconciliation removes synchronized rows that no longer exist on a
registered definition for the current owner, including a removed LuckPerms table. It preserves
other subjects, other rows, independent earnings and retained permanent history.

On September 12, 2026, Java 21.0.11 `./gradlew test build --no-daemon --no-configuration-cache`
passed on Minecraft 1.21.1 and NeoForge 21.1.248. All 361 tests in 100 suites passed without
failures, errors or skips. Seven added unit tests cover source identity and parsing, source kinds,
invalid empty contributions, personal/team/server legacy independence and codec roundtrips of
multiple subjects sharing an owner. The postcommit build passed with unchanged test inputs.
No separate formatter or static analysis task is configured; `git diff --check` passed.

The owned dedicated server ran `LuckPermsSourceGameTests` through the actual GameTest dispatcher:

```text
execute positioned 0 180 0 run test run permissionsourcespreserveothersubjectsandindependentearnings
```

The test uses two constructed server players, server scoped definitions and a controlled adapter.
It asserts distinct contributors, last source removal, independent earning preservation, removed
rows and tables, permanent retention, missing prerequisite denial, dependency loss, purchase denial
without an XP charge and unchanged mutation revision/grant time on repeated reconciliation. It
restores definitions, source and regression data, adapter state and subject queue membership.
An initial fixture failed because duplicate registration preserves the old definition; the corrected
fixture clears and installs each intended definition set. Production behavior was not weakened.

The corrected run reached readiness at 22:24:10 America/Chicago. At 22:24:43 the structure metadata
identified the exact source regression and its fresh lime result marker reported
`permission_sources_regression_pass`. The existing `permissionquerieswithdrawdeniedandunavailableoutput`
fixture then ran in the same server. At 22:25:09 its own metadata and cleared/fresh lime marker
reported `permission_queries_after_sources_pass`. The server stopped through its standard input
at 22:25:24, saved all dimensions and exited normally.

The clean `progressivestages-3.0.5.jar` SHA256 is
`d00b491aa20165de0d398a9c0191e0959232e68f7ebce9d51b308b18202bba24`.
Its manifest reports the source commit above and `Build-Dirty: false`; all 736 compiled production
classes match the packaged bytes. No LuckPerms API classes are bundled. A production server with
that exact JAR and no optional integration mods reached readiness at 22:26:35. At 22:26:56 the
console returned daytime 2510 and permission capture status stopped with zero records and an idle
writer. Shutdown at 22:27:07 saved all dimensions and exited normally. GitHub verified the pushed
source commit's SSH signature.

Both server modes ran without a GUI on `node-1`, in the existing Phase 003 worktree's
`build/permission-source-verification`, with authenticated mode and loopback port 25589. The task
graph was inspected before launch. EULA acceptance was read back before each launch. No laptop
client, browser, real LuckPerms provider or FTB Teams fixture was launched. This proves core source
mutation and persistence behavior, not actual provider login, team membership changes or network
synchronization. Legacy unattributed source migration, old owner and deleted definition cleanup,
offline contributors, expiry episodes, suppression, acquisition effects and the full real provider
matrix remain mandatory open work. No final phase merge, tag or release is claimed.

Cleanup confirmed both recorded server processes absent and loopback port 25589 closed. All owned
Gradle and server command handles were terminal. It removed 973 added build paths and
0 added local Gradle paths, including the runtime, test reports, compiled test output and
fixture JAR copy. The library symlink was removed without following its target. Both inventories
then matched their exact pretest baselines. The candidate JAR, source, preexisting runtimes and
shared caches remain. Temporary ownership and launch files were removed after their final consumer.
No laptop resource was created. The immutable goal and phase cursor remain unchanged.

## Online reconciliation queue regression

Source commit `f5bceb64c21e8a4294129282b7ccfd56cddb6a04` repairs the online overflow path
identified in the adapter audit. Previously, overflow cleared queued subjects, and the next drain
attempted to enqueue the entire online population. A population larger than the queue could
repeatedly clear earlier work. The new queue retains at most 256 pending subjects and resumes a
separate population cursor. Dirty work and scan work alternate within sixteen bridge
reconciliations per tick. New overflow requests a followup pass without restarting an active one.
Disconnect requests another pass to cover player list index changes, and shutdown clears all
queue and cursor state. No complete population copy or unbounded enqueue loop occurs in a poll.

Java 21 `./gradlew test build --no-daemon --no-configuration-cache` passed 354 tests across 99 suites,
with no failures, errors or skips. Eight queue regressions cover a 1,024 subject overflow,
duplicate events, sustained traffic during a scan, a followup scan, removal of an earlier list
member, sixteen indexed reads against a virtual million subject population, shutdown and empty
population behavior. The clean postcommit build passed with unchanged tests up to date.

On September 12, 2026, the isolated development dedicated server on `node-1` reached readiness at
22:03:12 America/Chicago using Java 21.0.11, Minecraft 1.21.1 and NeoForge 21.1.248. The inspected
`runServer` task graph and `forgeserverdev --nogui` target started no client or renderer.
`LuckPermsQueueGameTests.overflowingPermissionEventsReachEveryOnlineSubject` ran through the real
GameTest dispatcher at 22:03:23. It temporarily registers 300 synthetic server player identities
in the isolated server's player list and invokes the actual bridge tick method 64 times with a
counting adapter. It verifies all 300 subjects reach reconciliation, no call queries more than
sixteen subjects, the queue stays at or below 256, and the scan finishes. Its `finally` block
removes only fixture players and restores definitions, queue and adapter state. This is a core
queue integration fixture, not 300 networked clients, real provider events or elapsed tick latency.

At 22:03:33, the structure metadata identified the exact test and the lime success marker emitted
`permission_queue_regression_pass`. The marker was cleared before running
`permissionQueriesWithdrawDeniedAndUnavailableOutput` at 22:03:54. At 22:04:15 its exact metadata
and fresh lime marker emitted `permission_query_after_queue_pass`, verifying the existing
withdrawal path after fixture cleanup. The server stopped through its console at 22:04:29,
saved all dimensions and exited normally. No laptop resource or live LuckPerms provider was used.

The clean JAR SHA256 is `aa9621c30ddcb187b1066cb0c059501cca8f6e7c5eb9e3a88730a02db0ba4379`,
with `Build-Commit` bound to the source commit above and `Build-Dirty: false`. All 734 project
class entries match compiled output, and no LuckPerms API classes are bundled. No frontend or
packaged editor asset changed. GitHub verified the signed source commit on the Phase 003 branch.

This repair does not close BIN-AC-012F. Immediate full reconciliation on reload is still separate,
and persisted offline contributor rescans, eight concurrent loads, provider event invalidation,
epoch and membership guards, marker lifecycle and measured real provider convergence remain
required. The exact dependency only login failure is unchanged. The scoped review found no new
persistent provider writes, blocking loads, provider thread game mutation or optional API linkage
in the queue. Code index results mixed projects and old worktrees; bounded active source
inspection supplied the missing coverage.

The same clean JAR then ran through the production `forgeserver --nogui` launcher in
`build/reconcile-queue-verification` inside the Phase 003 worktree on `node-1`. Both launches
used verified `eula=true`, loopback port 25589 and online authentication. The production run
reused the preexisting 21.1.248 libraries through a fixture symlink and installed only the candidate
mod. It reached readiness at 22:05:48, loaded 50 stage definitions and selected the default
NeoForge permission handler. At 22:06:41 the console time query returned 2584 and permissions
capture status reported stopped, zero records and an idle writer. Console stop at 22:06:48 saved
every dimension and exited normally. This verifies packaged optional classloading and startup,
not real provider compatibility. Both owned server processes were absent and port 25589 was
closed before teardown.

Cleanup removed the 967 test created paths under `build`, including the runtime, world, reports,
test classes, three new compiled project classes, fixture JAR copy and library symlink without
following its target. No `.gradle` path was added. Both final path inventories exactly matched
their recorded baselines with no missing preexisting path. The requested clean JAR, source,
preexisting runtimes, shared libraries and dependency caches remain. All owned build and server
handles exited; the ownership receipt and init script were removed after their final consumer.
The loader plan validation passed without changing the plan, research, handoff, goal or cursor,
and its separate temporary intake was removed. No new laptop resource was created.

## Independent permission query regression

Source commit `5280889e4ee0cb4270d77a7be5a96206a72b0a34` replaces noncontextual input queries
and name based output exclusion with an API 5.4 query helper. It copies current provider query
options, removes only `progressivestages_bridge`, selects contextual evaluation and retains other
contexts and flags. A loaded offline user uses static provider options; an unloaded user remains
unavailable without a blocking load. Context snapshots retain every value per key and copy those
sets immutably. Configuration rejects reserved marker aliases and duplicate context keys without
case sensitivity while preserving the author's source casing.

Inbound eligibility uses independently queried groups and ready TRUE permissions. It no longer
rejects an independently held group merely because an output row names that group. An independent
FALSE permission result or unavailable query prevents positive outbound publication and withdraws
an existing owned contribution. An authoritative UNDEFINED result can receive configured positive
output. The distinction prevents a failed query from being interpreted as permission to grant.

Java 21 `./gradlew test build --no-daemon --no-configuration-cache` passed 346 tests across 98 suites,
with no failures, errors or skips. Eleven added tests cover marker removal, retained flags and
multiple contexts, TRUE/FALSE/UNDEFINED, loaded offline static contexts, unloaded users, immutable
snapshots, case insensitive identity, invalid context aliases, provider failure, independent equal
group membership and matching across all keys and any value within each key. The API fixtures use interface proxies and never
initialize a real provider. Existing node tests now inspect the fixture's explicit effective view,
so they still verify actual modeled output rather than its independently sourced input view.

On September 12, 2026, `LuckPermsQueryGameTests.permissionQueriesWithdrawDeniedAndUnavailableOutput`
ran twice through `/test run` on the owned NeoForge 21.1.248 development dedicated server. Both
runs used the source bytes committed above. The fixture constructs a server player, grants a
stage in server storage and calls the actual bridge reconciliation with a controlled adapter.
It asserts no publication under FALSE, publication under authoritative UNDEFINED, withdrawal on
FALSE and unavailable data, no repeated unavailable publication, recovery and withdrawal on stage
loss. It restores stage definitions, fixture state and adapter state in `finally`.

The first server configuration discovered no tests because it enabled only the `progressivestages`
namespace, while these tests use the `minecraft:igloo/top` template. Inspection of the exact
NeoForge `GameTestRegistry.register` showed that namespace filtering uses the template namespace.
The owned server stopped normally, then restarted with
`neoforge.enabledGameTestNamespaces=progressivestages,minecraft`. This corrected only the disposable
verification configuration. No test result was claimed from the failed discovery attempts.

The corrected server reached readiness at 21:43:29 America/Chicago. The focused test started at
21:43:47 and 21:44:08. Each run's structure metadata named
`permissionquerieswithdrawdeniedandunavailableoutput`; the observed lime glass success marker was
checked at 21:43:57 and 21:44:17. The marker was cleared before the repeat. The server saved all
dimensions and exited normally after `stop` at 21:44:53. These are core GameTest results with a
controlled adapter, not LuckPerms login, inherited graph exclusion or negative precedence proof.

The clean packaged `progressivestages-3.0.5.jar` has SHA256
`14ef6b65353e9e17938aa09fd66dda06ab74cd1f4d136311ac008fb38cf4f9d2`, a manifest bound to the source
commit above and `Build-Dirty: false`. All 731 project class entries matched compiled output,
and no LuckPerms API classes were bundled. The clean rebuild passed; unchanged Java tests were
up to date. No frontend source or packaged editor assets changed.

The packaged candidate then ran without optional mods in `build/luckperms-query-verification`
inside the Phase 003 worktree on `node-1`, using Java 21.0.11, the preexisting NeoForge 21.1.248
libraries through a read only fixture link, `forgeserver`, `--nogui`, loopback port 25589,
online authentication and verified `eula=true`. It reached readiness at 21:46:37, loaded 50 stage
definitions and selected `neoforge:default_handler`. At 21:47:10, `time query daytime` returned
10842 and `stage debug permissions status` reported stopped capture, zero records and an idle
writer. No client, renderer or live LuckPerms provider ran in this suite.

The scoped review checked query failure behavior, reserved context aliases, preservation of
independent input with the same group name and the optional classloading boundary. The code index again returned
mixed projects and historical worktrees; only bounded inspection of the active source was used.
No persistent user mutation, permission tree enumeration or blocking user load was introduced.
Real marker activation and invalidation, exact provider inherited exclusion and negative precedence,
provider events, bounded offline loads, stale completion guards and full lifecycle convergence
remain mandatory open gates. The exact provider's dependency only login failure is unchanged.

The production server stopped through standard input at 21:48:04, saved all dimensions and
exited normally. Its exact process was absent and loopback port 25589 was closed before cleanup.
The initial inventory identified 965 added paths under `build` and no added `.gradle` paths.
Only those added paths were removed, including the runtime, test reports, fixture JAR copy,
compiled test output and library symlink without following its target. The final cleanup inventory
matched both baselines exactly, with no missing preexisting path. The intended candidate JAR,
source, sanitized evidence, preexisting runtimes and shared libraries and dependency caches remain.
All owned Gradle and server handles were terminal. No laptop resource was created. The temporary
ownership receipt and init script were removed after their final consumer. The saved goal and
phase cursor were unchanged.

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


## Permission episode regression

Source commits `34f763b3cf7171e23dc1c0fe2f17f034e678b977` and
`1b5c90760f731a5315fa0eab4a77ecafe93b8499` add durable permission eligibility history and correct
first qualification, expiry and fixture isolation. Both commits are signed and pushed to the active
Phase 003 branch. This is core correction evidence for BIN-REQ-012 and BIN-REQ-014, not phase completion.

History survives source withdrawal, manual revocation, attachment encode/decode and copy. The
first qualified acquisition fixes its clock. Unavailable input and changed contexts do not prove
independent eligibility loss. The original conditions becoming false and then true can rearm the
episode; an administrative grant can clear suppression. Timed permanent sources expire before a
returning rank opens another episode. Expiry polling updates owner views without waiting for a
provider callback. Independent acquisition after derived access emits its normal event exactly once.

The exact NeoForge 21.1.248 `AttachmentHolder` catches deserialization exceptions and skips the
failed attachment. The registered stage serializer now retains the original unreadable NBT in a
state that rejects mutations. The actual attachment read, save and copy fixture verifies complete
payload equality for a future schema. Its deliberate error log is expected evidence, not a server failure.

### Automated and dedicated checks

Java 21 with the checked in wrapper completed `./gradlew test build`. The final reports contain
399 tests across 105 suites, with zero failures, errors or skips. `git diff --check` passed. No resource
provider changed, and no additional formatter task is configured in this checkout. The inspected
`runServer` graph launches only the dedicated server and no client or renderer.

The final dedicated run used `node-1`, Minecraft 1.21.1, NeoForge 21.1.248 and the active Phase 003
checkout. Its owned runtime was
`/mnt/hermes/projects/ProgressiveStages/.phase-worktrees/phase-003/build/permission-episode-verification`.
EULA readback was `eula=true`, authentication remained enabled and the listener stayed at
`127.0.0.1:25589`. PID `451256` reached readiness at 01:49:27 on September 13, 2026,
America/Chicago. Eleven actual dispatcher invocations covered ten distinct GameTests, including
one immediate repeat to verify fixture isolation. Every invocation had matching structure metadata
and a fresh lime success marker. The fixture area was cleared between invocations.

| GameTest | Passing server time | Contract checked |
|---|---|---|
| `permissionrevocationsurvivesreloadandonlyindependentlossrearms` | 01:50:23 | Initial world qualification, durable revoke, provider absence, context and retention changes, genuine loss/return and administrative grant |
| `permissionexpirysurvivescontextlossandofflinereconciliation` | 01:50:30 | Both retention modes, fixed clocks, polling, offline expiry/rearm, invalidated observation rollback and a returning permanent source |
| `independentearningafterpermissionaccessemitsoneacquisition` | 01:50:36 | Derived changes emit no ordinary acquisition/revocation events; independent earning emits one event and survives rank loss |
| `unreadableepisodeattachmentsremainintactaftersave` | 01:50:42 | Future schema rejects mutations and preserves the complete attachment through actual save and copy |
| `permissionsourcespreserveothersubjectsandindependentearnings` | 01:50:49 and 01:50:55 | Contributor separation, independent retention, dependencies, purchase denial and repeatable isolation |
| `permissionownerchangeswithdrawstalecontributions` | 01:51:02 | Obsolete owner withdrawal before new grants and preserved unrelated history |
| `offlinesourcesrevalidatewithoutaplayerandpreserveotherowners` | 01:51:08 | Static contexts, personal/shared contributors and retained sources without an online player |
| `offlineresultsrejectstaledefinitionsandunqualifiedgrants` | 01:51:15 | Stale owner/definition rejection and qualification guards |
| `offlinerescanconvergeswhilecontributorsareremoved` | 01:51:21 | All 300 seeded contributors, at most eight loads and sixteen subject operations per tick |
| `personalclocksdriveexpiryheldconditionsandslotage` | 01:51:27 | Owner clock isolation, held conditions, expiry and slot age |

The server saved all dimensions and exited normally after `stop` at 01:52:09. Earlier iterations
exposed private helper references in the unit tests, the distinction between a codec error result
and a thrown constructor validation error, and retained history in older GameTest fixtures.
The affected fixtures now isolate their attachment state and restore the original attachment;
no failing assertion was removed. The final source fixture repeat and complete 300-subject scan passed.

### Packaged candidate

The clean final JAR has SHA256
`59d5af457041c6100bcc2dcd413eea292f39f48a25e0ee403465dff97c70cc50`.
Its manifest names source commit `1b5c90760f731a5315fa0eab4a77ecafe93b8499` and `Build-Dirty: false`.
All 761 project class entries match compiled output, and no LuckPerms API classes are bundled.
The postcommit wrapper build passed with the same pinned platform and dependencies.

The same owned runtime then loaded only the packaged mod and the pinned loader. Production PID
`458202` reached readiness at 01:53:09. At 01:54:07, `time query gametime` returned `22885` and
`stage debug permissions status` reported stopped capture, zero records, zero bytes, zero queued
records and an idle writer. This proves common initialization and a dormant optional integration.
It does not prove a real LuckPerms player login or permission cache behavior.

### Remaining acceptance

The complete real provider, membership revision, row edit, administrative/reset, callback generation,
command provider and multiplayer lifecycle matrix remains open. These server fixtures use controlled
provider observations and do not replace actual LuckPerms, FTB Teams, Brave or laptop acceptance.
No client, browser, graphical process or laptop resource was started in this suite. No phase merge,
phase tag, wiki publication or release is claimed.


### Cleanup receipt

The final packaged server stopped at 01:55:08 and saved every dimension before normal exit at
01:55:09 on September 13, 2026, America/Chicago. All registered server PIDs were absent after the
suite, no Java process retained the owned runtime as its working directory, and the private port
could be bound again. Earlier development and packaged candidates were stopped before replacement.
The intermediate development session whose PID was not recorded also returned a normal terminal
exit; the final process and runtime checks found no leftover server.

Cleanup removed 1076 test-created build paths and 18 test-created local Gradle paths.
The original 836 build paths and 26 local Gradle paths were preserved. The owned runtime, its worlds,
configuration, logs, copied JAR and library symlink were removed. The symlink target, shared dependency
caches, preexisting `run` and `run-248` directories, current candidate artifact and all source/worktrees
were preserved. The three metadata-only scratch files and their unique temporary directory were
removed after their final consumer. No laptop, browser or audio resources required teardown.
The immutable goal and active phase cursor retain their previous digests.


## Administrative eligibility regression

Source commit `77fa1ac6500c7fd849c2acf1990941900ca8c63d` corrects administrative entry points
for BIN-AC-011B, BIN-AC-011C, BIN-AC-012C and BIN-AC-012D. Revoke APIs no longer skip eligibility
history after source withdrawal. Bulk and script reset visit registered definitions at their resolved
owners. Tag operations use the same API contract. Normal, bypass, single, bulk, tag and category
grants distinguish independent ownership from existing derived access. Mutation results and committed
listeners report source changes even when the effective stage set remains unchanged.

The final Java 21 wrapper `test build` passed 399 tests in 105 suites, without failures, errors or
skips. The postcommit `build` passed. No resource providers changed and this checkout has no separate
formatter task. `git diff --check` passed. The inspected server task graph contains no client launch.

Dedicated PID `492624` ran on `node-1` with Minecraft 1.21.1 and NeoForge 21.1.248 in the active
Phase 003 checkout's `build/admin-episode-verification` runtime. EULA readback was `eula=true`;
authentication remained enabled and the listener stayed on loopback port 25589. Readiness was
02:15:15 on September 13, 2026, America/Chicago. Tests entered through the normal dispatcher with
`execute positioned 0 180 0 run test run <method>`. Each invocation cleared its fixture area and
verified matching structure metadata at `0 180 3` plus a fresh lime success marker at `-1 179 2`.

| GameTest | Passing server time | Coverage |
|---|---|---|
| `administrativerevokessuppressunavailableandunqualifiedepisodes` | 02:15:29, repeated 02:16:08 | API, bulk, tag, category and script binding, both retention modes, provider outage, first qualification, reload, genuine rearm, no op counts, unknown stage and committed owner |
| `administrativegrantsrecordindependentownershipafterderivedaccess` | 02:16:05, repeated 02:16:10 | Normal and bypass API, bulk, tag, category, direct command and script binding, independent retention, no op counts and committed source change |
| `permissionrevocationsurvivesreloadandonlyindependentlossrearms` | 02:16:13 | Existing suppression, contexts, reload and administrative recovery |
| `permissionexpirysurvivescontextlossandofflinereconciliation` | 02:16:15 | Fixed expiry and guarded offline rearm |
| `independentearningafterpermissionaccessemitsoneacquisition` | 02:16:17 | One normal acquisition event and independent retention |
| `permissionsourcespreserveothersubjectsandindependentearnings` | 02:16:20 | Contributor separation and independent source preservation |
| `personalclocksdriveexpiryheldconditionsandslotage` | 02:16:22 | Personal clock isolation, held conditions and slot age |

All nine invocations passed. The two new tests exercise actual stage commands and the public script
binding methods; they do not claim a KubeJS engine run or real LuckPerms provider acceptance. The
initial category fixture failed because only StageOrder was populated. It now isolates and restores
the loader catalog as well, which the real category and script lookup paths query. No assertion was
removed. Launch setup also corrected closed console input and enabled the Minecraft template
namespace for these tests. Those setup attempts are not counted as passing tests.

The clean packaged JAR has SHA256
`fe8b52d2fe605cc49bdcc2eeae21bae5db3db2edc4cd9618a34c44736ce5af99`. Its manifest names the source
commit above with `Build-Dirty: false`; all 761 project classes match compiled output and no LuckPerms
API classes are bundled. Packaged production PID `498564` reached readiness at 02:18:14. At 02:18:40,
`time query gametime` returned `7343` and `stage debug permissions status` reported stopped capture,
zero records, bytes and queued records, and an idle writer. It saved all dimensions and exited
normally at 02:18:51. This proves common startup with optional providers absent.

Full real provider, membership revision, edited row, delayed callback, command provider, multiplayer,
Brave and laptop acceptance remain open. No phase integration, signed phase tag, wiki publication or
release is claimed by this bounded correction.

### Administrative suite cleanup

The final development server saved all dimensions and exited at 02:16:34; the packaged server did
so at 02:18:51. The earlier three launch setup processes terminated through server shutdown hooks,
saved their worlds and exited with the expected signal status. The later console setup run exited
normally; its PID was not retained. All six registered Java PIDs are absent, no Java process retains
the owned runtime working directory, and loopback port 25589 can be bound again.

Cleanup removed 1038 new build paths and 18 new local Gradle paths, while preserving the original
836 build paths and 26 local Gradle paths. The disposable runtime, worlds, configuration, logs and
copied JAR were removed. Its library symlink was removed without following the protected target.
All eleven scratch files and their unique directory were removed after evidence retention. The
current candidate JAR, source, worktrees, preexisting run directories and shared caches remain.
No laptop, browser, client, renderer or audio resource was started. The final read only diff and
receipt audit created no additional test resources. Goal and cursor digests remain unchanged.


## Membership generation verification

Commit `6eabe409d53b7ccfd1b50640fedb93232da6123b` adds membership revision checks to pending
offline permission observations and public actor context mutations. The
[membership regression](progression-ownership.md#membership-context-regression) records 399 passing
unit tests, ten dedicated GameTest invocations, packaged startup and exact cleanup evidence on
NeoForge 21.1.248. Stale observations with unchanged owner UUIDs cannot create synchronized or
permanent access; current observations still reconcile. Actual FTB events, real provider login,
online transactional reconciliation and laptop acceptance remain open.


## Online input collection verification

Source commit `2f858ee18191700f8eceb6a60ac73aafee5d9f70` collects all enabled inbound permission results before
changing stage sources or eligibility history. Each distinct permission is queried once. Provider
callbacks and replacement, membership changes, definition revisions or objects, concrete owners and
stage mutation revisions invalidate the complete collected input. A rejected observation queues a
fresh attempt. Query unavailability discards partial positive input without inventing authoritative
rank loss. Existing synchronized, permanent and independent ownership semantics remain in force.

On September 13, 2026, Java 21.0.11, Minecraft 1.21.1 and NeoForge 21.1.248 passed the final
`./gradlew test build --no-daemon --console=plain` in 12 seconds. All 400 unit tests in 105 suites
passed with zero failures, errors or skips. The new collection unit test verifies one read per distinct
permission, immutable observations and complete rejection after a later unavailable permission query.
The initial implementation build passed in 17 seconds and the clean postcommit build in four seconds.
No separate formatter is configured, no resource provider changed, and `git diff --check` passed.

Dedicated development PID `551738` ran on `node-1` in the Phase 003 checkout's
`build/online-input-verification` runtime, using the inspected `forgeserverdev` launch, `--nogui`,
Java 21 and the pinned compiled Minecraft artifact. Readiness occurred at 02:53:04 America/Chicago.
Authentication remained enabled, the listener stayed at loopback port 25589, and EULA readback was
`eula=true`. Each verified test reset the owned structure area, ran at `0 180 0`, confirmed the exact
method in structure metadata at `0 180 3`, and observed a fresh lime success marker at `-1 179 2`.
Earlier automated probes ran before the markers appeared and are excluded from the verified count.
The corrected harness waited for each test's success marker before starting the next scenario.

| GameTest | Verified time |
|---|---|
| `staleonlinequeriescannotpartiallygrantorwithdrawstages` | 02:53:29, 02:54:36 |
| `permissionrevocationsurvivesreloadandonlyindependentlossrearms` | 02:54:38 |
| `permissionexpirysurvivescontextlossandofflinereconciliation` | 02:54:41 |
| `independentearningafterpermissionaccessemitsoneacquisition` | 02:54:45 |
| `administrativerevokessuppressunavailableandunqualifiedepisodes` | 02:54:49 |
| `administrativegrantsrecordindependentownershipafterderivedaccess` | 02:54:52 |
| `offlinemembershipchangesrejectdelayedinputwithunchangedowners` | 02:54:55 |

All eight verified invocations passed. The new test covers both retention modes, membership and
definition changes, individual and population provider callbacks, stale positive and negative input,
unchanged ownership and history on rejection, no rejected mutation publication, and fresh recovery.
It uses a deterministic adapter and a fake player through the real bridge and manager; it is not
real provider, native FTB event, network login or client evidence.

The development server saved every dimension and exited normally at 02:55:46. The clean packaged JAR
has SHA256 `0a65d2475baf06715baf5eb4ff5640d31304be53b3e369f8e1b42e7df210a522` and reports the source
commit above with `Build-Dirty: false`. All 765 project classes match compiled output and no
LuckPerms API classes are bundled. Packaged production PID `560407` reached readiness at 02:57:13
with optional providers absent. Its console time query returned `3514` at 02:57:29; it saved every
dimension and exited normally at 02:57:30.

Cleanup verified both PIDs absent, no process working directory beneath the owned runtime, and port
25589 free. Exact ownership comparison removed 1029 new build paths and 13 new local Gradle
paths while preserving all 836 and 26 preexisting paths respectively. The runtime was removed without
following its libraries symlink. The preexisting `run-248/libraries`, current packaged JAR, source,
worktrees and shared caches remain. All nine owned scratch files and their unique directory were
removed after evidence extraction. No laptop, client, browser, renderer or audio resource was created.

This advances BIN-AC-012C, BIN-AC-012D and BIN-AC-012F for observation collection. Reentrant stage
callbacks during application, atomic outbound changes, real LuckPerms login, FTB lifecycle and the
full laptop and browser matrix remain open. No phase integration, tag, wiki update or release is claimed.

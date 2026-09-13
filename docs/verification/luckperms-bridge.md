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

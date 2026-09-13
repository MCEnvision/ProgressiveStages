# progression ownership verification

This page records the verification surface for actor-aware stage ownership in 3.0.5 development.
It is kept with the source so a release candidate can replace the pending entries with exact hashes
and runtime receipts.

## Owner clock regression

Source commit `5d9034528b799cf80a76a8a392f8504e69b9b46e` separates grant timestamps by owner kind,
UUID and stage. Personal, team and server clocks cannot overwrite one another. Legacy UUID APIs
remain team and server operations. Legacy timestamps remain in those namespaces and are never
copied into personal ownership. The regression save adds `clock_schema = 1` and
`owner_grant_times`, retaining unmatched legacy records in `grant_times`. Expiry, both held duration
condition paths, and online and offline slot age now receive the resolved owner identity.

The exact NeoForge 21.1.248 patched `DimensionDataStorage` source was inspected locally. It catches
a deserializer exception and can call the empty data constructor afterward. The regression factory
now refuses that fallback when the existing regression file remains present. Unsupported schema
versions and malformed map or timestamp types fail without replacing the original clock save.
This protects recovery data; it does not repair a corrupt file or authorize discarding it.

On September 13, 2026, Java 21.0.11 `./gradlew test build --no-daemon --no-configuration-cache`
passed on Minecraft 1.21.1 and NeoForge 21.1.248. All 390 tests across 104 suites passed with zero
failures, errors or skips. Four new unit tests cover all three owner kinds sharing one UUID,
save/load and targeted clearing, legacy migration and unrelated record preservation, the reserved
zero UUID, and rejection of unsupported or malformed clock data. No separate formatter or static
analysis task is configured; `git diff --check` passed. The final postcommit build also passed.

The inspected `runServer` task graph has no client or renderer. The first launch attempt stopped
at the working directory assertion before Minecraft started. Explicitly binding the task working
directory to the owned game directory corrected the harness. The first gameplay fixture then
failed at 00:52:30 because its constructed player lacked a packet connection. It now uses a directly
owned NeoForge fake player with its standard handler, preserving the actual grant, revoke and
condition assertions. That development server stopped normally at 00:53:38. The final development
server reached readiness at 00:54:20 America/Chicago and executed the following GameTests.

| Fixture | Started | Matching metadata and fresh lime success marker observed |
|---|---|---|
| `personalclocksdriveexpiryheldconditionsandslotage` | 00:54:43 | 00:55:02, `personal_clock_final_pass` |
| `unreadableclocksavecannotbecomeanemptyreplacement` | 00:55:02 | 00:55:31, `clock_save_preserved_pass` |
| `permissionsourcespreserveothersubjectsandindependentearnings` | 00:55:31 | 00:56:09, `clock_permission_sources_pass` |
| `offlineresultsrejectstaledefinitionsandunqualifiedgrants` | 00:56:09 | 00:56:39, `clock_offline_stale_pass` |

The actual structure block was at `0 181 3` and success glass at `-1 180 2`. Released chunks were
force loaded before inspection, and success glass was cleared before the next fixture. The clock
fixture uses actual grant and revoke handlers, verifies separate timestamps for colliding owners,
checks oldest slot selection and both held duration readers, and expires only the personal stage.
The save fixture evicts only the isolated regression cache entry, reads an actual unsupported
compressed NBT file through Minecraft storage, checks the refusal, saves other world data and
compares the rejected bytes. Its expected `Unsupported stage clock schema` error appeared at
00:55:03. The prior cache object and file bytes were restored in cleanup. The source and offline
fixtures passed afterward. These are server core checks with controlled actors and providers;
they do not establish a real player connection or third party lifecycle behavior.

The final development server stopped at 00:56:39, saved all dimensions and exited normally at
00:56:40. The clean packaged `progressivestages-3.0.5.jar` has SHA256
`6d08a10839092ffd4ed519ba7b84cc0baf253421e3207a330f1bf5fa836cc4f6`. Its manifest identifies the source
commit above with `Build-Dirty: false`. All 753 project classes match compiled output byte for
byte, and no `net/luckperms` API classes are bundled. The same JAR without optional mods reached
production dedicated readiness at 00:57:59. At 00:58:53, `time query gametime` returned 5907 and
`stage debug progression status` reported stopped capture, zero records and an idle writer.

The production server stopped at 00:59:44, saved all dimensions and exited normally. Cleanup
confirmed all three owned server processes were absent and the loopback port was available.
All 1,013 added build paths were removed, restoring its exact 836 path baseline. The 26 preexisting
`.gradle` paths had no additions or removals. The disposable runtime and three metadata scratch
files were removed after their final consumer. The preexisting `run`, `run-248`, shared libraries,
dependency caches, source and candidate artifact were preserved. No cleanup resource remains.

All launches used only `node-1`, online authentication and `127.0.0.1:25589`, with EULA true read
back before startup. The exact owned runtime was
`/mnt/hermes/projects/ProgressiveStages/.phase-worktrees/phase-003/build/stage-clock-verification`.
No laptop or browser resource was created. This P003-TASK-005 correction supports BIN-REQ-011 and
the owner-aware expiry prerequisite for BIN-REQ-012. Permission eligibility episodes, suppression,
rearming, full acquisition effects and combined provider and client gates remain open. No phase
merge, tag or release is claimed. The plan, immutable goal and phase cursor remain unchanged.

## static checks

The parser accepts omitted, `team_stage = false`, and `team_stage = true` values. It rejects a
non-Boolean value, `scope = "player"`, and an explicit override on a server stage. Schema 4 package
metadata and legacy compiler metadata preserve field presence. `TeamStageData` decodes legacy
team-only payloads as ownership schema 0 and writes schema 1 with distinct personal and team maps.

The effective snapshot is immutable and carries one actor revision plus source namespaces. Mutations
publish a committed result with affected owner and player sets. Existing UUID team APIs continue to
operate on team and server records.

## focused commands

Use a disposable dedicated server with a fixture containing omitted, personal, team, and server
stages, two teammates, an outsider, and a player without a team. Run:

```
/stage explain scope <player> <stage>
/stage debug progression on <player>
/stage debug progression status
```

Grant and revoke each stage through the normal command, trigger, purchase, quest, and script paths.
Compare each player's personal, team, and server namespaces before and after every operation. The
personal stage must remain absent from the teammate's snapshot, while a server stage must appear for
current and future players without an artificial team. Stop the capture and verify that a later
operation adds no new record.

## runtime evidence

The final phase packet must record the exact Minecraft, NeoForge, ProgressiveStages, FTB Library,
FTB Teams, FTB Quests, and KubeJS artifacts, hashes, configuration, fixture paths, server readiness,
and cleanup receipts. Client synchronization and gated input require the verified laptop and silent
client procedure. Server-only assertions do not close that client gate.

Until those runtime gates are complete, this page describes the required procedure and the focused
source tests only. It does not claim a published release or provider compatibility beyond the
candidate artifacts recorded in the phase packet.

## phase 001 artifact audit

The exact optional binaries were fetched from their publisher repositories on 2026-09-11 into a
disposable audit directory. Their hashes and declared licenses matched the planning records.

| artifact | sha256 | sha512 | license |
| --- | --- | --- | --- |
| `ftb-library-neoforge:2101.1.30` | `8d3ad0eaaae5f71cfbe9062bb9a03223a2db4aafa5243da486b65afa13fb24ad` | `1d3ccafda2b453ec95a703ce3fa5148db254ff2f7514c4c67fae5cd9698b196816e7b18a3bfafad60db2585069a9fcdf25953baef45cd24da685413b68a7b093` | All Rights Reserved |
| `ftb-teams-neoforge:2101.1.9` | `c0e4fcb2e349dd24dd3bddb1bcda6c61dad3c1db38e3bf1100e5da9c2753e571` | `f1542136eb2c857c9f278a2fe5b605186f2e1307c1b217812ed3f677b9ef5af01d86cb8269a5ce039325312f7d3f5d2ccb94c364a5aea52c28455de86e1cff9d` | All Rights Reserved |
| `ftb-quests-neoforge:2101.1.21` | `8559d32d03be276b156f843c8d56539668545c379462e557a9642370662cc294` | `0ed6f2325db700a790ae856cc5da29ffb07118da1e711edc1f93b6218890f2d6d5a81f8dde86d45c0278e93082ec040e4967733bc54a5b7244524307d5201a34` | All Rights Reserved |
| `kubejs-neoforge:2101.7.2-build.348` | `490b14231dbe0036037915e4863aab9895a441eade22e28cde6ebc1b31306d54` | `f94d7cb7f252667b73a0ec94a0ce7ca5ead4403c2b44392c1112c4f8cb2152b4032b3638fd5d04ceced27a583526eb74a90bdf4e5d29b88abb5523b680b1a72c` | GNU LGPLv3 |
| `architectury-neoforge:13.0.8` | `2eb06668281be9c57ed6ba8b2ca39b155567063c3096aa94a1c2140694f787df` | `25bd0de8219ee5cab11011262ac8e7ca276d40a72475392637c0f89c1a6af08f4d9e4cf9e5d183149af80c029476470392ad352b2685e06087f34a397bce5e6e` | GNU LGPLv3 |
| `rhino:2101.2.7-build.81` | `a02abde402b5cea16f31dea4026d630f95a3369134d891a196c014d1daf492c3` | `8394ec89c561df6b0db34b5f5abc7989a9b781fc202cbd511a782239ddfcd0586b90cfed8ad0c5abe47e191bf1a673ad0bcd0aae4d42f8c50e1a57f21662d60f` | MPL-2.0 |

The manifests were inspected for loader, Minecraft, Architectury, FTB, and Rhino ranges. A disposable
provider runtime then reached the `Done` state on port 25568 with FTB Teams, FTB Quests, KubeJS,
Architectury, and Rhino loaded alongside ProgressiveStages. FTB Teams detection, the FTB Quests
provider, and KubeJS compatibility initialization completed without errors. No player login or
LuckPerms callback evidence was claimed because this headless host has no client connection. The
provider runtime and audit directory were removed after inspection.

## phase 001 headless smoke

On 2026-09-11, a disposable NeoForge 21.1.219 dedicated server reached the `Done` state on port 25566 with Java 21 and no optional FTB providers installed. The server reported FTB Teams absent and loaded the current stage definitions without a mod loading error. No client or gameplay gate was claimed because this host is headless and no player connection was available. The exact runtime was `/mnt/hermes/projects/ProgressiveStages/.phase-worktrees/phase-001/run`; it was stopped and removed after log inspection.

The repository GameTest server also ran on the same pinned stack and shut down cleanly after
executing 21 tests. It reported 12 failures in the pre-existing inventory insertion scenarios.
Phase 001 does not change those inventory test sources, and no ownership GameTest was present in
the suite, so those failures remain an open baseline regression outside this phase's acceptance
surface.


## Membership context regression

Source commit `6eabe409d53b7ccfd1b50640fedb93232da6123b` replaces the fixed zero membership
revision in actor contexts with a monotonic server wide generation. Public context mutations
reject stale membership before grants or revokes, including after returning to the same owner.
They also reject a call outside the server thread before resolving the actor. Pending offline
permission observations carry the same generation through their initial and final commit checks.
The previous four argument offline context constructor remains available.

Provider initialization, login, logout and server shutdown invalidate captured contexts. The optional
FTB integration registers a native `TeamEvent.PLAYER_CHANGED` listener and unregisters both that
listener and its runtime event subscription at shutdown. Inspection of the pinned FTB Teams
2101.1.9 binary confirmed that `AbstractTeam.onPlayerChangeTeam` invokes this native event and that
Architectury 13.0.8 provides matching register and unregister operations. This is static linkage
and invocation evidence. Actual FTB membership event and listener lifecycle acceptance remains open.

On September 13, 2026, `./gradlew test build --no-daemon --console=plain` passed with Java 21.0.11,
Minecraft 1.21.1 and NeoForge 21.1.248. All 399 unit tests in 105 suites passed with no failures,
errors or skips. The final postcommit `build` passed in six seconds. No separate formatter task
is configured, no resource provider changed, and `git diff --check` passed.

Dedicated development PID `527287` ran on `node-1` in the active Phase 003 checkout's
`build/membership-verification` runtime. The inspected development launch uses `forgeserverdev`
and `--nogui`, with the pinned compiled Minecraft artifact on its classpath. No client or renderer
starts in that path. Both earlier launch setup processes exited with status 1 before readiness
because the launch classpath lacked Minecraft classes. Their PIDs were `525847` and `526480`.
Adding the exact pinned compiled artifact corrected the setup. A mistyped test name matched no
test and is excluded from the passing count.

The verified development server reached readiness at 02:36:50 America/Chicago. Authentication
remained enabled, its listener stayed at loopback port 25589, and EULA readback was `eula=true`.
Each test cleared its fixture area, ran through `execute positioned 0 180 0 run test run <method>`,
and verified matching structure metadata at `0 180 3` plus a fresh lime success marker at
`-1 179 2`. All ten invocations passed.

| GameTest | Passing server time | Scope |
|---|---|---|
| `stalemembershipcannotgrantorrevokewiththesameowner` | 02:37:24, 02:37:46 | Public API grant and revoke across personal, team fallback and server owners, stale membership and definitions, owner mismatch, offline actor, provider reset, fresh recovery, unchanged revision and no committed event on rejection |
| `offlinemembershipchangesrejectdelayedinputwithunchangedowners` | 02:37:43, 02:37:48 | Delayed input with unchanged shared owner, both retention modes, no stale eligibility episode, another contributor preserved, fresh observation recovery |
| `offlineresultsrejectstaledefinitionsandunqualifiedgrants` | 02:37:50 | Existing definition and qualification guards, invalidation during reconciliation |
| `offlinesourcesrevalidatewithoutaplayerandpreserveotherowners` | 02:37:52 | Contexts, source isolation, permanent and independent preservation |
| `offlinerescanconvergeswhilecontributorsareremoved` | 02:37:54 | Bounded offline scan and contributor removal |
| `permissionrevocationsurvivesreloadandonlyindependentlossrearms` | 02:37:57 | Existing suppression and recovery |
| `administrativerevokessuppressunavailableandunqualifiedepisodes` | 02:37:59 | Administrative entry point regression |
| `administrativegrantsrecordindependentownershipafterderivedaccess` | 02:38:01 | Independent grant and committed result regression |

The development server saved all dimensions and exited normally at 02:38:43. The packaged JAR
has SHA256 `d77c6de14082efca11d642c9b54b5eac1f615aec20673f5017c8e889f0445727` and names the
source commit above with `Build-Dirty: false`. All 762 project classes match compiled output;
no LuckPerms API classes are bundled. Production PID `533137` reached readiness at 02:40:00 with
optional providers absent. At 02:40:27, `time query gametime` returned `2730`, both progression
and permissions capture status reported stopped capture with zero records and an idle writer,
and the server saved every dimension and exited normally.

This closes the bounded context regression for BIN-AC-011B, BIN-AC-011C, BIN-AC-011E and
BIN-AC-012F. The new guard does not establish complete FTB membership synchronization, the real
LuckPerms provider, online reconciliation transactions, negative thread invocation coverage,
client synchronization or the remaining combined acceptance matrix. Those gates remain open.
No integration merge, phase tag, wiki update or release is claimed.

### Membership suite cleanup

After the final log and artifact consumers completed, all four recorded processes were absent,
no process retained the owned runtime as its working directory, and loopback port 25589 was free.
Exact comparison with the pretest ownership receipt removed 1034 new build paths and
13 new local Gradle paths. All 836 preexisting build paths and 26 preexisting local
Gradle paths remained. The disposable runtime was removed without following its libraries symlink;
the preexisting `run-248/libraries` target and the verified packaged JAR were preserved.
The 14 registered metadata and log files were removed after evidence extraction, and their
unique temporary directory was removed and checked absent. No laptop resource, client, browser,
renderer or audio stream was created by this suite.

## Native FTB membership verification on NeoForge 21.1.248

Source commit `f2e9b7cad10fe7e9c421bfb8b07e31ec281fb02d` adds `FtbMembershipGameTests` without changing production behavior.
On September 13, 2026, the actual FTB Teams 2101.1.9 runtime drove party creation, joining,
leaving and rejoining. The fixture uses the provider's own `playerLoggedIn`, `createParty`,
`PartyTeam.join` and `PartyTeam.leave` methods. It does not post a synthetic membership event
or manually increment the membership revision. Detached server test players supply the actors;
this proves native provider and server ownership behavior, not authenticated player login,
FTB command permissions, packets, client rendering or synchronization.

The test verifies that repeated integration registration still gives exactly one membership
invalidation per native event. Actual online actor queries and offline UUID owner queries agree
on the joined party and the solo owner after leaving. A personal profession belongs only to its
actor, while an explicitly shared stage is available to both members. Leaving removes the second
member's shared access and invalidates its captured offline owner context without altering the
first member's personal or shared stage. Rejoining restores shared access without copying the
personal profession.

The initial runtime test failed during teardown because `getKnownPlayerTeams()` returns an
unmodifiable map. That run is excluded from passing evidence. The corrected fixture uses bounded
reflection only to remove its two synthetic player records during teardown, then restores the
original stage attachment, definitions and regression clock in a separate `finally` block.
Native party disband handles the fixture party. Two consecutive runs assert that fixture team
and player records have been removed before reporting success. No production access bypass,
provider replacement or third party binary modification was introduced.

### Exact provider artifacts and bounded inspection

The three cached artifacts matched the official pinned hashes below. ZIP integrity passed;
none contains a nested JAR or native executable library. Metadata declares compatible Minecraft
and NeoForge ranges, and the selected Architectury and FTB Library versions meet the provider's
required dependencies. These binaries were copied only into the owned test runtime and are not
redistributed in the product or evidence.

- [ftb-teams-neoforge 2101.1.9](https://maven.ftb.dev/releases/dev/ftb/mods/ftb-teams-neoforge/2101.1.9/ftb-teams-neoforge-2101.1.9.jar). License: All Rights Reserved. SHA256 `c0e4fcb2e349dd24dd3bddb1bcda6c61dad3c1db38e3bf1100e5da9c2753e571`. SHA512 `f1542136eb2c857c9f278a2fe5b605186f2e1307c1b217812ed3f677b9ef5af01d86cb8269a5ce039325312f7d3f5d2ccb94c364a5aea52c28455de86e1cff9d`.
- [ftb-library-neoforge 2101.1.30](https://maven.ftb.dev/releases/dev/ftb/mods/ftb-library-neoforge/2101.1.30/ftb-library-neoforge-2101.1.30.jar). License: All Rights Reserved. SHA256 `8d3ad0eaaae5f71cfbe9062bb9a03223a2db4aafa5243da486b65afa13fb24ad`. SHA512 `1d3ccafda2b453ec95a703ce3fa5148db254ff2f7514c4c67fae5cd9698b196816e7b18a3bfafad60db2585069a9fcdf25953baef45cd24da685413b68a7b093`.
- [architectury-neoforge 13.0.8](https://maven.architectury.dev/dev/architectury/architectury-neoforge/13.0.8/architectury-neoforge-13.0.8.jar). License: GNU LGPLv3. SHA256 `2eb06668281be9c57ed6ba8b2ca39b155567063c3096aa94a1c2140694f787df`. SHA512 `25bd0de8219ee5cab11011262ac8e7ca276d40a72475392637c0f89c1a6af08f4d9e4cf9e5d183149af80c029476470392ad352b2685e06087f34a397bce5e6e`.

The scoped bytecode reference inspection found no direct Java network or process execution
references in FTB Teams. FTB Library network references occur in icon and client utilities;
Architectury has a URL reference in platform metadata. No `ProcessBuilder` reference was found
in the three artifacts. This bounded inventory is not a comprehensive third party security
audit and does not prove that indirect network or file activity is absent. The fixture uses
normal provider data storage inside its disposable world, exposes only the loopback game port,
and supplies no credentials or external services. Client image behavior was not exercised.

### Commands, results and artifact identity

`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon test build` passed after the
fixture correction in 12 seconds. All 407 unit tests in 106 suites passed with zero failures,
errors or skips. The postcommit `build` passed in four seconds. No separate formatter is
configured, no resource provider changed, and the source diff check passed.

The inspected development launch uses `forgeserverdev` and `--nogui`, Java 21, Minecraft 1.21.1
and NeoForge 21.1.248 on `node-1`, in the active Phase 003 checkout's
`build/ftb-membership-verification` directory. It starts no client or renderer. Each server
launch read back `eula=true`, retained authentication and bound only loopback port 25589.
The log confirmed all three exact provider versions above.

The initial server PID `657433` reached readiness at 04:00:33 America/Chicago, recorded the
teardown failure at 04:00:49 and saved all dimensions before exiting at 04:01:14. Its owned
world was removed after shutdown before retrying. The corrected server PID `661111` reached
readiness at 04:02:22. Each invocation cleared the fixture area, dispatched
`execute positioned 0 180 0 run test run nativeftbmembershippreservespersonalandsharedownership`,
and checked the matching structure metadata at `0 180 3` plus a fresh lime success marker at
`-1 179 2`. Both invocations passed at 04:02:37 and 04:02:48. The server saved all dimensions
and exited normally at 04:03:05.

The packaged `progressivestages-3.0.5.jar` has SHA256 `7a120279969f53e1021f8b4f127a491e8151b64bc803937ce457514bc0f73ed1` and SHA512 `d7f31d64bb2b79f183b16ce09a7692ba5b82005fb76dd44f95fb301f64e2c564264a900ce4888361284aba06f20025ddbd950d3fa8fb13639d68dcccef206953`.
Its manifest binds source commit `f2e9b7cad10fe7e9c421bfb8b07e31ec281fb02d` with `Build-Dirty: false`; all 769 project classes
match the compiled output. Neither FTB nor LuckPerms API classes are bundled. Packaged
production server PID `665889` reached readiness at 04:04:22 with all optional providers
absent, responded to `time query gametime` and reported progression capture stopped with zero
records and an idle writer. It saved all dimensions and exited normally at 04:04:45.

This supplies bounded native membership evidence for BIN-AC-009C, BIN-AC-011C and BIN-AC-011E.
It does not close complete team merge/disband, actual FTB Quests or KubeJS workflows, client
synchronization, the LuckPerms compatibility gate or final combined acceptance. The signed
source commit is pushed and verified by GitHub. No default branch integration, phase tag,
wiki update or release is claimed.


### Native membership suite cleanup

All three recorded server processes exited, no process retained the owned runtime as its
working directory, and loopback port 25589 was free. Cleanup removed 1049 new build paths
and 13 new local Gradle paths, preserving all 836 preexisting build paths and 26 preexisting
local Gradle paths. The runtime was removed without following its libraries symlink, and the
preexisting `run-248/libraries` target and packaged candidate remained intact. The 12 owned
scratch files and their unique temporary directory were removed after their final evidence
consumer, with absence verified. No laptop resource, browser, client, renderer, watcher or
audio stream was created by this suite. The laptop capability inspection was read only and
created no files or processes. The saved goal, phase cursor and authoritative plan stayed
unchanged.


## Native quest provider ownership regression on NeoForge 21.1.248

Source commit `6928148c0565940c37c47f8e786b84068aad0db1` fixes a mismatch in the registered
FTB Library stage provider. With `integration.ftbquests.team_mode = true`, grants and removals
already resolved stage ownership before delegating to `TeamStagesHelper`, but checks delegated
unconditionally. An actual per player quest reward could grant a personal profession that its
stage task then rejected. Conversely, a stale shared helper record could authorize a profession
that the player did not own. Checks now resolve ownership using the same boundary as grants and
removals. Personal and server stages use ProgressiveStages; team owned stages retain delegation.

### Actual provider fixture and scope

The fixture uses FTB Quests 2101.1.21 `StageReward.claim` and `StageTask.canSubmit` with the
actual registered `StageHelper` provider, a native FTB party and two detached server test players.
It explicitly configures `team_reward = false` and leaves `team_stage` false. Each player claims
the same per player reward separately. Assertions cover independent personal entitlements,
claimant only removal, a stale shared helper record that must not authorize the personal stage,
and server scope grant, check and removal across both actors. This is server provider evidence
for BIN-AC-011D, not authenticated reward claim packets, player UI, full quest completion,
client synchronization, script integration or the native FTB team storage route. Native rewards
and tasks configured to use FTB team storage bypass the provider and remain an open gate.

The fixture snapshots and restores the native quest team data map in addition to the membership
fixture's existing owner data, definitions and clocks. It restores the temporary quest team mode
setting in `finally`. Quest definitions are constructed only for the fixture and are not added to
the saved quest definition collection. Provider binaries are unmodified.

The FTB Teams, Library and Architectury versions, SHA256 and SHA512 values match the preceding
native membership artifact table. The additional artifact is
[ftb-quests-neoforge 2101.1.21](https://maven.ftb.dev/releases/dev/ftb/mods/ftb-quests-neoforge/2101.1.21/ftb-quests-neoforge-2101.1.21.jar),
licensed All Rights Reserved. SHA256 `8559d32d03be276b156f843c8d56539668545c379462e557a9642370662cc294`.
SHA512 `0ed6f2325db700a790ae856cc5da29ffb07118da1e711edc1f93b6218890f2d6d5a81f8dde86d45c0278e93082ec040e4967733bc54a5b7244524307d5201a34`.
Its required NeoForge, Minecraft, Architectury, Library and Teams ranges admit this exact matrix;
FTB XMod Compat is optional. ZIP integrity passed, with no nested JAR or native library. Bounded
bytecode inspection found a direct Java network reference in the client quest description field
and no `ProcessBuilder` reference. This is not a comprehensive third party security audit.
No provider binary is redistributed.

### Reproduction, correction and packaged smoke

All times below are September 13, 2026, America/Chicago. The owned headless runtime was
`build/quest-ownership-verification` in the active Phase 003 checkout on `node-1`, using Java 21,
Minecraft 1.21.1 and NeoForge 21.1.248. The inspected development launch used `forgeserverdev`
and `--nogui`. Each launch verified `eula=true`, preserved authentication and used only loopback
port 25589. No client, renderer or desktop was launched.

The baseline compiled the new regression before applying the production correction. Server PID
`679702` reached readiness at 04:11:50. The actual reward granted the personal stage, then the
quest check failed at 04:11:58 with
`Quest stage checks must use personal ownership even with quest team mode enabled.`
The provider log showed the incorrect `TeamStagesHelper=false` lookup. This baseline is failed
evidence. All dimensions saved at 04:12:27 and the process exited before its owned world was
removed for the corrected run.

Corrected server PID `686642` reached readiness at 04:14:48. Two invocations of
`execute positioned 0 180 0 run test run nativequestrewardsandchecksrespectpersonalownership`
passed at 04:18:57 and 04:19:25. The existing
`nativeftbmembershippreservespersonalandsharedownership` regression then passed at 04:19:54
with Quests installed. Each invocation cleared its fixture area and verified both a fresh lime
success marker at `-1 179 2` and matching structure metadata at `0 180 3`. All dimensions saved
at 04:20:06 and the process exited normally.

`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon test build` passed for the
corrected source, including 407 unit tests in 106 suites with zero failures, errors or skips.
The final precommit build took 13 seconds; the postcommit packaged `build` took five seconds.
No separate formatter is configured, no resource provider changed, and `git diff --check` passed.

The packaged JAR has SHA256 `3d93fd6e4cf325a386323f9589c63c4ad5e2045d043bdb7c36ed7593d46e070f` and SHA512 `fc128151ce38befac227b524ef868e72a8d3b52b70a08aeea95745498b5839d64f4af4e7183f5fa1a4d53a350184efbbe74b6f86575238a285a609a36ddc6e45`.
Its manifest records `Build-Commit: 6928148c0565940c37c47f8e786b84068aad0db1` and
`Build-Dirty: false`. All 770 project classes match compiled output, and no FTB or LuckPerms
API classes are bundled. Production server PID `696092` launched that JAR with all optional
providers absent and reached readiness at 04:21:00. It answered `time query gametime` at
04:21:09. An additional capture status command used an invalid command path and was rejected;
no capture status evidence is claimed from it. The signed source commit was pushed and GitHub
verified its signature. Full provider compatibility, final combined acceptance, default branch
integration, phase tag, wiki publication and release remain unclaimed.


### Quest provider suite cleanup

The packaged server saved all dimensions at 04:22:05 and exited normally. All three
recorded server processes were absent, no process retained the owned runtime as its working
directory, and loopback port 25589 was free. Cleanup removed 1227 new build paths and
13 new local Gradle paths, preserving all 836 preexisting build paths and 26 preexisting
local Gradle paths. The runtime libraries symlink was removed without following it, preserving
the shared libraries target and packaged candidate. All 12 registered scratch files were removed
after evidence extraction, and the unique temporary directory was verified absent. No laptop,
browser, client, renderer, watcher or audio resources were created. Plan validation passed with
plan set SHA256 `42ce0478a1e4c96021c72d0152224d5d883c9d7728eb92475ca380e05b862854`;
the validator intake was removed. The plan, immutable goal and phase cursor remained unchanged.


## Native quest team storage routing on NeoForge 21.1.248

Source commit `3974448403249feed3733bd5cfa5939a1166d731` extends the correction to native FTB team storage routes and
registered team stages. In FTB Quests 2101.1.21, `StageReward.claim` calls `isTeamReward` before
using `TeamStagesHelper`; `StageTask.canSubmit` reads `teamStage` before checking that same
helper. Those paths bypass the registered stage provider. The provider itself also delegated
registered team stages to the helper, leaving ProgressiveStages ownership unchanged.

Two optional mixins redirect those storage choices for defined stage IDs to the existing
provider. The reward hook targets all three native `isTeamReward` calls inside `claim`, including
its feedback selection, without replacing `isTeamReward` globally. The task hook targets the
single native team storage branch in `canSubmit`. Explicit injection counts protect the exact
inspected targets. The existing optional mixin plugin guards absent provider classes. The
provider uses ProgressiveStages ownership for every defined stage, including shared stages.
Undefined external stages retain the native FTB route, and disabled integration preserves it.
No third party binary, reward distribution setting, quest persistence format or stage schema
was modified. There is no event or provider callback before the two native helper shortcuts;
intercepting only the registered provider cannot fix them.

### Actual regression and passing scope

The fixture uses the same exact FTB Teams 2101.1.9, FTB Library 2101.1.30, Architectury 13.0.8
and FTB Quests 2101.1.21 artifacts recorded above. Before launch, each cached binary matched its
recorded SHA256 and SHA512, ZIP integrity passed, and the complete required four artifact matrix
was copied into the owned runtime. No dependency version or platform pin changed.

Both failure modes were reproduced before the production correction. On September 13, 2026,
America/Chicago, baseline server PID `706300` reached readiness at 04:26:49. At 04:27:12,
`nativeteamquestsettingsrespectstageownership` failed because the actual native team reward did
not grant the claimant's personal profession. At 04:27:58, the expanded per player reward test
failed with `Shared quest rewards must update authoritative stage ownership.` The preceding
personal and server assertions had passed, isolating the shared provider delegation mismatch.
These baseline results are failed evidence. The server exited before its owned world was removed.

Corrected server PID `712226` reached readiness at 04:30:15. Its log confirmed both new mixins
applied to the exact native classes. The native team settings regression passed twice, at
04:30:35 and 04:31:11. The expanded per player quest regression passed at 04:32:01, followed by
the native membership regression at 04:32:37. Each invocation cleared the fixture area and
verified a fresh lime success marker at `-1 179 2` together with the matching structure metadata
at `0 180 3`, after the actual server console test command completed.

Both native flag configurations cover actor only personal grants and removals, separate reward
invocations for two actors, stale helper denial, server scope and authoritative team grants and
removals. The native team settings case additionally proves undefined external stage grant,
check and removal through FTB storage, plus the disabled integration route. Native reward/task
NBT writes preserve the configured `team_reward` and `team_stage` values. Temporary cached config
values, team state, attachment data, definitions and regression clocks are restored in teardown.
The second actor's previous personal regression clock is now restored rather than cleared.

The fixtures invoke native reward bodies and task eligibility with detached server players and
real FTB teams. Invoking a team reward body for each actor does not prove that FTB's outer claim
workflow lets both actors claim that reward. Full reward claim tracking, authenticated packets,
quest team completion, client rendering and synchronization remain separate acceptance gates.
FTB reward distribution remains separate from stage ownership. This supplies bounded server
proof for the native storage and owner consistency portions of BIN-AC-011D and SHARED-003.

Existing defined stage records held only in the FTB helper are preserved but not imported into
ProgressiveStages by this correction. Their upgrade migration and source attribution remain an
open gate. This record does not claim legacy entitlement migration, final provider compatibility,
complete team lifecycle or final combined acceptance. Earlier records remain historical; their
open native storage branch is superseded only within the passing scope described here.

### Build and packaged absence verification

The owned runtime was `build/native-quest-storage-verification` in the active Phase 003 checkout
on `node-1`. The inspected development launch uses `forgeserverdev` and `--nogui`; the packaged
launch uses the production NeoForge server arguments. Both use Java 21, Minecraft 1.21.1 and
exactly NeoForge 21.1.248, verify `eula=true`, keep authentication and bind only loopback port
25589. No graphical process was launched.

`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon test build` passed after the
correction, including 407 unit tests in 106 suites with zero failures, errors or skips. The final
precommit build took 13 seconds, and the postcommit packaged `build` took four seconds. No
separate formatter is configured, no data provider changed, and `git diff --check` passed.

The packaged JAR has SHA256 `b73eaa979cb150b44463d01f821128b011295cf2aa5701c897355b34f021fbf1` and SHA512 `21b2493a2a7cf0cb47a55530a32562faab9247d1098bf59e24d5d96ee7af170115be6ffddf8770a78eee928ed37e47f1000d3f1983a66d33a36afe89223ba3f5`.
Its manifest records `Build-Commit: 3974448403249feed3733bd5cfa5939a1166d731` and `Build-Dirty: false`.
All 772 project classes match compiled output, both new mixins are registered in the packaged
configuration, and neither FTB nor LuckPerms API classes are bundled. With all optional providers
absent, packaged server PID `719503` reached readiness at 04:33:44 and answered
`time query gametime` at 04:34:05. GitHub verified the pushed source commit signature.
Default branch integration, final phase tagging, wiki publication and release remain unclaimed.


### Native storage suite cleanup

The baseline server saved all dimensions at 04:28:18, the corrected development server at
04:32:53, and the packaged server at 04:34:57. All three processes exited normally and were
confirmed absent. No process retained the runtime as its working directory, and loopback port
25589 was free. Cleanup removed 1233 new build paths and 13 new local Gradle paths,
preserving all 836 preexisting build paths and 26 preexisting Gradle paths. The runtime
libraries symlink was removed without following it; shared libraries and the packaged candidate
were preserved. All 12 registered scratch files and their unique temporary directory were
removed after evidence extraction, with absence verified. No laptop client, browser, renderer,
watcher or audio resource was created. The plan, immutable goal and phase cursor were unchanged.


## Legacy FTB helper import on NeoForge 21.1.248

Source commit `629738ab5973e3d0dc13b3f32a8400c9a904162b` imports existing defined helper stages
into their original team namespace. It replaces the old online player polling bridge, which
snapshotted existing helper records without importing them and routed later changes through
acquisition grants or revocations. That bridge could copy a shared profession into personal
ownership, replay rewards or remove independently earned progression.

After definitions are ready and both FTB integrations are enabled, the adapter reads all native
teams, including offline teams, and submits one import to StageManager. Defined IDs become
independent team grants. Current personal or server scope masks those preserved team records;
changing back to team scope can expose them again. Foreign undefined IDs remain native. The
original helper properties are unchanged, and subsequent helper edits are not an ongoing source.
New defined quest rewards use the native storage hooks described above.

The attachment stores `ftb_helper_import_schema = 1` and the imported team/stage map in
`ftb_helper_imports`. StageManager validates and copies data before installing grants and their
receipt together on the server thread. The receipt remains after revoke or team removal. An
empty import also persists completion. Migration publishes an owner change without executing
acquisition costs, prerequisites, rewards or grant clock initialization. Unsupported receipt
versions and invalid owner records use the existing protected loading boundary.

### Native provider assertions and actual restarts

The runtime was `build/legacy-ftb-import-verification` in the active Phase 003 checkout on
`node-1`. Minecraft 1.21.1, Java 21 and NeoForge 21.1.248 remained pinned. The development launch
used `forgeserverdev`, `--nogui`, loopback port 25589 and authentication. Each launch verified
`eula=true`. The exact FTB Teams 2101.1.9, FTB Library 2101.1.30, FTB Quests 2101.1.21 and
Architectury 13.0.8 binaries matched both recorded digests before copying. No provider binary
was modified. No client or renderer was launched.

Two initial fixture failures are excluded from passing evidence. On September 13, 2026,
America/Chicago, server PID `742969` failed the scope restoration assertion at 04:51:54 because
the fixture attempted to overwrite a definition with a duplicate registration that StageOrder
rejects. The fixture now rebuilds the definition map and explicitly verifies its configured
experience reward before checking reward suppression. Server PID `746390` then failed at
04:53:57 because earlier fixture player team files survived restart. Teardown now calls the
native team deletion operation after removing its two known player entries, rather than only
removing in memory map entries. Both failed servers exited normally before their disposable
world was removed. These changes strengthen the assertions and cleanup rather than relaxing them.

Corrected server PID `750299` reached readiness at 04:56:08. The actual helper import regression
passed at 04:56:41 and again at 04:57:09. It verifies shared access for two detached actors,
personal and server scope isolation, preserved masked team records, exclusion of foreign IDs,
independent provenance, no configured experience reward replay, unchanged grant clocks, one
committed owner notification, scope rollback, NBT receipt persistence and no resurrection after
revoke. The native team quest storage regression then passed at 04:57:56.

The three process restart fixture uses an explicitly enabled step property and an isolated stage
`progressivestages:legacy_restart` with `team_stage = true`. Its fixture TOML SHA256 was
`87bc198f779c7e1eb1e18c855d4dae7f00d60e94b4280fdc771c326d333e46d8`.
Preparation disabled only the cached integration flag for the remainder of that owned process,
created an offline native server team with a helper grant, and saved the native team files and
an unimported attachment. Preparation passed at 04:58:44. The same world was retained through
both restarts without rebuilding its data or copying state through a test codec.

Server PID `759940` reached readiness at 05:03:28 with normal integration configuration. Startup
imported the disk backed native grant as independent team ownership. The verification revoked
that grant, saved the world and checked that the original helper record remained. This step
passed at 05:03:54. Server PID `761839` reached readiness at 05:04:24 and confirmed that the
persisted receipt prevented the stale helper grant from restoring access, passing at 05:04:47.
Both restart assertions also confirmed the earlier fixture player identities were absent from
native provider storage. Every passing invocation checked a fresh lime block at `-1 179 2` and
matching test metadata at `0 180 3` after the server console command. Missing step properties
skip these specialized tests and must never count as restart proof.

This supplies bounded migration evidence for SHARED-003 and BIN-AC-011D. It supersedes the
previous native storage record's open helper import gap within this tested scope. Detached
actors do not prove authenticated client synchronization, quest reward claim tracking or all
team lifecycle operations. Final combined acceptance, default branch integration and the
remaining provider and graphical gates remain open.

### Build and packaged absence checks

`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon test build` passed with
410 tests in 106 suites and zero failures, errors or skips. The corrected fixture build took
11 seconds; the reviewed precommit build and postcommit packaged build each took four seconds.
No separate formatter is configured, no data provider changed, and `git diff --check` passed.

The packaged JAR has SHA256 `5b9f285e4b925343c32702a6dcf7ad5429485a749152e240581771bef04dbdd8` and SHA512 `b3e00158b1f333dab8c1e32f368b8cbffdacdfa0416f76b904de9be57fe0311801ac6c3c2a391b71f32055ecef6a399e31049cd3d9c2544efbbabc43419de336`.
Its manifest records `Build-Commit: 629738ab5973e3d0dc13b3f32a8400c9a904162b` and `Build-Dirty: false`.
All 776 project classes match compiled output, including the isolated restart fixture.
Neither FTB nor LuckPerms API classes are bundled. GitHub verified the pushed source signature.

After the FTB restart sequence finished, only the four hash verified runtime provider copies
were removed and the packaged candidate was installed. With all optional providers absent,
production server PID `765839` reached readiness at 05:06:18 and answered `time query gametime`
at 05:07:07. This verifies dedicated server classloading with the packaged artifact, not client
or full provider acceptance. The existing world remained disposable throughout the suite.

Plan validation passed with plan set SHA256
`42ce0478a1e4c96021c72d0152224d5d883c9d7728eb92475ca380e05b862854`.
DEC-006 already pins the build and both runtime hosts to NeoForge 21.1.248. No plan amendment,
goal change or cursor transition was needed. The validator intake and its directory were removed.


### Legacy import suite cleanup

The two failed fixture servers saved all dimensions at 04:53:05 and 04:54:40. The corrected
migration server saved at 05:03:13, followed by the import restart at 05:04:04 and the revoke
restart at 05:04:53. The packaged absence server saved at 05:07:30. All six server processes
exited normally and were confirmed absent before
cleanup. No process retained the owned runtime as its working directory, and loopback port
25589 was free. Cleanup removed 880 new build paths and 13 new local Gradle paths,
preserving all 836 preexisting build paths and 26 preexisting Gradle paths. The runtime libraries
symlink was removed without following it. Shared libraries, the packaged candidate, existing
runtime data outside this suite and unrelated checkouts were preserved.

All 17 registered scratch files and their unique temporary directory were removed after their
last evidence consumer, and absence was verified. No laptop client, browser, renderer, watcher
or audio resources were created. The immutable goal, authoritative plan and active cursor remain
unchanged. No default branch merge, phase tag, wiki publication or release occurred.

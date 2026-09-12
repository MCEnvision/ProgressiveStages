# progression ownership verification

This page records the verification surface for actor-aware stage ownership in 3.0.5 development.
It is kept with the source so a release candidate can replace the pending entries with exact hashes
and runtime receipts.

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

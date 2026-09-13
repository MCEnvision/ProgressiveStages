# Authenticated FTB Quests Profession Verification

On September 13, 2026, an authenticated nonoperator completed and claimed native FTB Quests
tasks and rewards through the laptop client. The personal `quest_chef` stage was granted and
removed through the actual quest reward controls. Repeated claims and server restart did not
restore the removed stage or pay duplicate stage rewards.

This is bounded evidence for BIN-AC-011D and the Phase 003 runtime matrix. Only one player and
their native FTB solo player team participated. It does not prove two player party distribution,
teammate isolation, LuckPerms, Selling Bin, KubeJS, NeoEssentials or the Easy Builder browser
journey. The separate [ownership verification](../progression-ownership.md) records native team
and script fixtures at their stated fidelity. The [final acceptance record](../3.0.5-acceptance.md)
retains the remaining integration gates.

## Candidate and hosts

The unchanged production JAR identifies source commit
`7b99df1d62bffe31c4262b498082ea5e31644a99` with `Build-Dirty: false`. Its SHA256 is
`fad646aa9bbc039d0c36b88d9ff79d929068960c8e32715bc5f1faaf5011deea`. Both hosts used Minecraft
1.21.1, NeoForge 21.1.248, FTB Quests 2101.1.21, FTB Teams 2101.1.9, FTB Library 2101.1.30 and
Architectury 13.0.8. All five mod JAR hashes matched across hosts. Exact SHA256 and SHA512 values,
runtime paths, process identities and observations are in [evidence.json](evidence.json).

The dedicated server ran without a GUI on `node-1`, using Java 21.0.11, online authentication
and a loopback listener. The laptop connected through the owned private SSH tunnel. The server
operator list was empty throughout the quest actions. Console commands only observed stage
access, levels and bread counts or saved the game; none completed quests, claimed rewards,
granted stages, revoked stages or elevated the player.

The laptop client used Java 21.0.7 and the verified RTX 5090 Laptop GPU with NVIDIA 610.57.04.
The isolated instance had master volume zero before launch. Its exact window and Java process
were correlated to the native PipeWire client and playback stream, muted and continually checked.
The same owned process and muted stream survived reconnect. [Renderer evidence](renderer.png)
comes from Minecraft's own screenshot action, as do the quest images below.

## Reproduction fixture

Use a disposable runtime with the exact providers above and the matching production candidate.
Write and read back `eula=true` before each dedicated server launch. Install these files before
starting the server:

| Source fixture | Runtime destination |
|---|---|
| [chef.toml](../../test/fixtures/ftb-quest-ownership/chef.toml) | `config/progressivestages/stages/chef.toml` |
| [data.snbt](../../test/fixtures/ftb-quest-ownership/data.snbt) | `config/ftbquests/quests/data.snbt` |
| [chef.snbt](../../test/fixtures/ftb-quest-ownership/chef.snbt) | `config/ftbquests/quests/chapters/chef.snbt` |

Set `general.team_mode = "ftb_teams"`, `integration.ftbquests.enabled = true`,
`integration.ftbquests.team_mode = true` and `integration.ftbquests.recheck_budget_per_tick = 10`.
The stage explicitly sets `team_stage = false`, with a reward of three levels and two bread.
Both quest rewards set `team_reward = false` and disable automatic claiming. The second quest's
native stage task deliberately sets `team_stage = true`. Its reward removes the stage.

FTB Quests normalized the input SNBT, added its defaults and moved the chapter and quest titles
into `lang/en_us.snbt`. Inspection confirmed that the quest IDs, types, stage IDs, task team flag,
reward distribution and disabled autoclaim values remained intact. The input fixtures are not
claimed to be byte identical to the provider's normalized output. Their hashes and the resulting
configuration hashes are recorded separately.

1. Join the ready dedicated server with the silent laptop client as a nonoperator. Open the
   quest book using `/ftbquests open_book`.
2. Open **Check and Remove the Profession** and click its stage task before earning the stage.
   It must remain incomplete. Confirm no stage, zero levels and zero bread through the console.
3. Open **Become a Chef** and click its checkmark task. Completion must make its reward available
   without granting the stage before the player claims it.
4. Click the stage reward. Confirm stage access, three levels, two bread and automatic completion
   of the second quest's native stage task.
5. Open the second quest and claim its removal reward. Confirm the stage is absent, with three
   levels and two bread still present.
6. Click both already claimed reward controls again. Confirm no additional stage mutation,
   reward payment or change to FTB's saved claim ledger.
7. Disconnect, stop and restart the same dedicated runtime with unchanged definitions, then
   reconnect. Reopen the first quest and click its claimed reward again. Confirm the removed
   stage remains absent, both claims remain recorded and levels and bread remain three and two.

For server observations, use `stage check <player> quest_chef`,
`data get entity <player> XpLevel`, `clear <player> minecraft:bread 0` and `save-all`.
The zero item count makes `clear` an observation without removing any item.

## Observed results

Times below use America/Chicago. The native quest ledger supplies completion and claim timestamps.

| Checkpoint | Stage | Levels | Bread | Native quest result |
|---|---|---|---|---|
| Initial, 15:35:07 | Absent | 0 | 0 | Stage task incomplete; no claims |
| Checkmark completed, 15:42:49 | Absent at 15:42:51 | Not sampled | Not sampled | First quest complete; reward unclaimed |
| Grant claimed, 15:43:07 | Present | 3 | 2 | Second stage task completed; one claim |
| Removal claimed, 15:43:58 | Absent | 3 | 2 | Both rewards claimed |
| Both controls clicked again, checked 15:44:34 | Absent | 3 | 2 | Ledger unchanged |
| Restart and repeated grant claim, checked 15:49:07 | Absent | 3 | 2 | Ledger unchanged |

The saved ledger immediately after removal, after repeat claims and after the final restart
was byte identical, with SHA256
`ad20b67b0728846df6d11bb2ad98df86744dc0cb63f6a2c055a9c8600bfdc678`.
The retained [initial](before.snbt), [checkmark](checkmark.snbt), [grant](granted.snbt) and
[removal](removed.snbt) snapshots replace the actual player name and UUID with stable pseudonyms.
Their sanitized hashes differ from the original observations and are recorded separately.

The images show the [incomplete stage task](task_denied.png),
[claimed grant and completion feedback](grant_claimed.png),
[claimed removal](removed.png) and [claimed reward after restart](final_claim.png).
Stage removal does not undo an already completed quest task or its claim history.

## Setup limits and cleanup

An intermediate restart inherited exhausted launcher standard input. Console text sent to that
process did not execute and is excluded from the accepted assertions. After player disconnect,
that owned process was terminated with exit code 143. The corrected launcher preserved terminal
standard input. The final restart reached readiness at 15:47:58, accepted console observations
and stopped normally at 15:49:33 with exit code zero. The first server also exited zero.
Earlier failed keyboard setup attempts and native Wayland cursor warnings are not passing
gameplay or an error free client console claim.

All owned server, client, launcher, audio watcher and tunnel processes exited. The client window,
playback stream and private listeners were absent after cleanup. The original laptop game
directory was restored with its original inode, configuration hash and instance entries.
The disposable game, dedicated runtime and both scratch directories were removed after evidence
extraction. All 836 preexisting build paths, the candidate JAR, shared libraries, caches and
unrelated worktrees were preserved. No production code change, new build, phase merge, tag or release
publication is claimed by this verification.

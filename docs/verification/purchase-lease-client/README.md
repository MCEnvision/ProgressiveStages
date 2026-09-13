# Purchase and lease laptop observations

On September 13, 2026, matching production artifacts on `node-1` and the `envision` laptop proved
independent purchase during an actual structure lease, lease withdrawal without losing that
purchase, restart persistence and exactly one configured refund. Live revocation also exposed a
stale open stage map. This record is partial acceptance with an explicit failing UI case.

## Identity and method

The clean candidate is source `c89c692ae20e21084798340b42e5f5be890b9f33`, with
`Build-Dirty: false`, Minecraft 1.21.1 and NeoForge 21.1.248. Both hosts used SHA-256
`ab216b42fcd6f863a3b4ea06a2e2bf9106fa9d7fb24402b5dddbec55f28453d1`.
The server alone also loaded the [documented fixture](../../test/fixtures/purchase-lease/README.md),
SHA-256 `829311023e85a4d827fb2e606a5dd34c554efdecac882680462931a680931828`.
Its sources and stage definitions are retained for reproduction. No optional permission provider
was loaded. The fixture provides assignment and observation; actual player movement drives the
normal structure session manager, and real GUI clicks drive the production purchase handler.

The dedicated server ran without a GUI on Java 21. The laptop used Java 21.0.7 and the NVIDIA
GeForce RTX 5090 Laptop GPU, OpenGL 4.6.0, driver 610.57.04. Both client launches had master
volume zero before startup, then exact window and process identity checks and muted playback
stream readback before input. A watcher continuously verified only those owned streams. The
server authenticated `EnVyOnMyMind` through the private loopback tunnel to port 25589 on both
connections. [Structured evidence](evidence.json) records runtime paths, process identities,
window tuples, audio readbacks, source and image hashes, server observations and cleanup.

## Observed results

All times below are CDT. The lease stage was personal, with a ten level and four bread price,
one diamond reward, and 50 percent refund. The hidden access stage qualified normal arena entry.

| Time | Stimulus and authoritative result | Client evidence |
|---|---|---|
| 12:49:44 | Outside the arena, no access, independent source or receipt. Twenty levels, eight bread, no diamond. | [Verified first renderer](renderer.png) |
| 12:49:57 | Actual forward movement enters the arena. Only temporary access appears; balances stay unchanged. | [Unlocked stage with Purchase offer](purchase_offer.png) |
| 12:51:22 | Five levels makes the offer unaffordable. Clicking it changes no source, receipt or balance. | [Disabled offer](unaffordable.png) |
| 12:51:51 | Restored affordability and actual purchase click add independent ownership beside the lease. Ten levels, four bread, one diamond and a receipt remain. | [Purchase offer removed](purchased.png) |
| 12:52:22 | Clicking the former button location does not repeat the charge or reward. | Same purchased state |
| 12:53:17 | Actual backward movement exits the arena. Only the independent source remains, with unchanged balances and receipt. | [Owned stage after exit](after_exit.png) |
| 13:00:31 | Server and client restart preserves independent ownership, balances and receipt. | [Restart renderer](restart_renderer.png), [owned stage after restart](after_restart.png) |
| 13:01:16 | Explicit revoke removes ownership and receipt, returning five levels and two bread. The one diamond is unchanged. | [Failing stale details](revoked.png) |
| 13:01:40 | Repeated revoke returns nothing more. Explicit GUI refresh displays Ready and the ordinary Unlock offer. | [Refreshed details](revoked_refreshed.png) |

The first backward input attempt occurred while the map was still open, and the measured position
remained inside. That attempt was not counted as a lease exit. After closing the complete map,
actual movement changed Z from approximately 7.812 to 16.659 and session participation fell to
zero. Only that second attempt supplies the lease withdrawal evidence.

## Open regression

After command revocation, the map header immediately changed from `1/1` to `0/1`, but its cached
node and selected details still showed **Unlocked** and no purchase offer. An explicit GUI data
refresh corrected both the status and offer. The server had already removed ownership and paid
exactly the expected refund. The screenshots preserve this disagreement, rather than treating
reopening as successful live synchronization.

Source inspection found that `handleStageSyncClient` and `handleStageUpdateClient` update
`ClientStageCache`, whose changed paths refresh recipe and quest integrations but do not refresh
the open map. `StageTreeScreen` caches owned state in `MapNode`, while `ClientTriggerProgress`
separately stores server supplied purchase offers. The repair must update both views, preserve the
existing response budget and explicit opening protocol, and never reopen a map the player closed.
Repeat this live revoke scenario, grant/source changes, duplicate updates and close before deferred
response against the corrected artifact. BIN-AC-011B and final mixed client synchronization remain
open to that extent.

Source `5d68f06d86ad5e1a5e297bb1caae6c235614392f` implements revision driven map and offer
refresh. Its build, server response GameTests and dedicated startup passed, as recorded in the
[repair verification](../3.0.5-security-review.md#open-map-synchronization-repair). Corrected laptop
acceptance remains pending; these screenshots continue to describe the earlier candidate.

The direct Java script binding opened the actual client map from a closed screen. This does not
prove KubeJS engine loading, an external provider implementation, LuckPerms behavior or the full
Brave Easy Builder journey. No broad phase or plan completion is claimed.

## Cleanup

Both server processes, both clients, their exact audio watchers and Prism launchers exited.
Owned playback streams and both tunnel listeners were absent. The laptop's original game directory
was restored with its original inode, and its original instance configuration hash matched.
The disposable server runtime was removed after unlinking its shared library reference. Cleanup
removed 139 additional build paths, 13 additional local Gradle paths and two exited daemon logs
bound to this checkout; all 862 preexisting paths and the clean candidate were preserved. Both
private scratch directories were removed. Only these minimal observations, screenshots and fixture
sources remain. No merge, release publication or historical branch or tag change occurred.

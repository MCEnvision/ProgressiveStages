# Live map synchronization verification

On September 13, 2026, the matching production client and server at source
`5d68f06d86ad5e1a5e297bb1caae6c235614392f` corrected the
[stale selected stage baseline](../purchase-lease-client/README.md#open-regression).
Normal command revocation updated the owned count, node, selected details and purchase offer
without reopening the map. A grant beside an active temporary lease also removed the purchase
offer while effective access stayed true. Definition reload, resize preservation and the
grant and revoke sequence after reconnect passed the observations below.

## Candidate and method

Both hosts used the clean 3.0.5 JAR with SHA-256
`56fd75b94aa5e0c657741ca4735d9ab985bde3ce50b151df084e50d31011aa02`, Minecraft 1.21.1,
NeoForge 21.1.248 and Java 21. The server alone loaded the existing
[purchase lease fixture](../../test/fixtures/purchase-lease/README.md), whose binary SHA-256 was
`f9f93c1d9574f900f02a4cf0cc63f8988a8a6e58e91179a467c0b61a88f96b6f`.
The fixture supplies an assigned structure and read only observations. Actual player movement
drives ordinary lease entry and withdrawal; the real purchase button uses the production handler.
Its opening command invokes the existing Java script binding.

The no GUI server on `node-1` authenticated the acceptance player over the private loopback
tunnel at port 25589. The `envision` laptop connected twice, using the NVIDIA GeForce RTX 5090
Laptop GPU, OpenGL 4.6.0 and NVIDIA driver 610.57.04. Each launch had master volume zero before
startup, followed by exact Hyprland window, process and muted application stream checks before
input. The owned watcher checked those streams every half second. Both
[initial](renderer.png) and [reconnected](reconnect_renderer.png) renderer observations are retained.
The [structured evidence](evidence.json) includes full artifact hashes, fixture source hashes,
runtime paths, process and window identities, mute readbacks, sanitized server observations,
image hashes and cleanup results.

## Observed sequence

Times are CDT. The profession is personal and costs ten levels plus four bread. It rewards one
diamond and refunds 50 percent of its purchase price on explicit revoke.

| Time | Server stimulus and state | Actual client observation |
|---|---|---|
| 13:30:53 to 13:31:48 | Actual entry adds only temporary access. A real purchase click followed by actual exit leaves independent ownership, ten levels, four bread, one diamond and a receipt. | The ordinary purchase path remains functional on the corrected candidate. |
| 13:32:13 | Revoke while the selected map remains open removes ownership and receipt. Fifteen levels, six bread and one diamond remain. | [Count becomes 0/1, node and details become Ready, and Unlock returns](live_revoked.png), with no explicit refresh. |
| 13:32:46 | Actual entry restores temporary access alone. Balances remain fifteen levels, six bread and one diamond. | [Unlocked with an independent Purchase offer](before_independent_grant.png). |
| 13:32:49 | A normal grant adds the independent source beside the lease. Effective access remains true. The ordinary grant awards one diamond, with no purchase charge or receipt. | [The same selected stage remains Unlocked and its offer disappears](source_only_change.png). |
| 13:33:43 to 13:33:44 | Actual exit removes the lease; revoke removes independent ownership. Reload changes the display name to Reloaded Profession and cost to twelve levels. | [Changed name and price appear in the selected details](definitions_reloaded.png). |
| 13:34:19 to 13:34:20 | Five full sync commands repeat the same unowned effective state. | The selected stage remains consistent. Processing within one client tick was not measured. |
| After window resize | The client returns to a smaller window with the same definition and ownership. | [Selection, Ready state and twelve level offer survive](after_resize.png). |
| 13:41:03 | Restore the exact tracked stage fixture and reload. | The open details return to Lease Profession and the ten level offer. |
| 13:41:24 to 13:41:57 | At two server ticks per second, explicitly open the map and grant the stage in one console batch. Close the details and map with Escape before the response interval expires. Server game time advances from 16444 through 16450 to 16510. | [The world is visible before](closed_before_response.png) and [after](closed_after_response.png) the deferred response interval. The map stays closed. Normal twenty tick rate is restored at 13:42:00. |
| 13:43:00 to 13:43:46 | Restart only the client and authenticate on the same server. Reopen and select the independently owned stage, then revoke it normally. | [Count, node, details and Unlock offer update after reconnect](reconnect_revoked.png). |
| 13:44:10 | Grant again while the selected map remains open. | [Count and selected details return to Unlocked and the offer disappears](reconnect_granted.png). |

The node position and selected stage survived these updates. There was no deliberate pan or
nondefault zoom test. The resize observation proves preservation across the actual resize; it
does not force a definition update inside the resize callback. The delayed interval uses actual
server game time, but transport was not instrumented to timestamp its individual automatic data
response. The separate [server response GameTests](../3.0.5-security-review.md#open-map-synchronization-repair)
prove queue delivery and opening intent. The fixture's keybind was unassigned, so this run does
not claim configured keybind acceptance.

This closes the reproduced live command revoke and source change presentation defect on this
candidate. It does not close the full mixed runtime matrix, real LuckPerms callbacks, Selling Bin,
KubeJS engine or Brave authoring acceptance. The earlier server restart persistence evidence
remains separate; this run reconnects to the same server.

## Cleanup

Both owned clients, watchers and Prism launchers exited, and both playback streams disappeared.
The original laptop game directory was restored with inode `15099132`; its instance configuration
matched the original SHA-256. The private tunnel listener was absent. The server stopped normally
after saving every dimension. Its exact disposable runtime was removed after unlinking the shared
library reference. Cleanup removed 136 new build paths, eight new local Gradle paths and one exited
daemon log belonging to this run, preserving all 862 baseline paths, the clean candidate and
preexisting `run-248` resources. Both private scratch directories were removed after preserving
the required evidence. No merge or release publication occurred.

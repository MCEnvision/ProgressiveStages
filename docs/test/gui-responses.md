# GUI response regression

`GuiResponseGameTests` exercises the actual server GUI request handler, rejected purchase handler,
public command dispatcher and server tick callback. A fixture connection captures outbound
`StageGuiDataPayload` packets before transport. It does not render a screen or establish client
acceptance.

The first test warms the GUI handler with 128 requests, then sends 1,000 requests alternating
between a GUI packet, purchase of a nonexistent stage and the public `stage gui` command at
permission level 0. All three entry points must share one immediate response. Thread allocation
for the measured burst must stay below 8 MiB, including reflection and command dispatch overhead.
This is a regression bound for redundant view reconstruction, not a general network benchmark.

The second test gives two independent players one immediate response each and queues another.
It adds a definition before the pending send, disconnects the second player, and waits for actual
server ticks. The first player must receive exactly one more response containing the new definition.
The disconnected player must receive none. Clearing the first player's connection state must allow
a fresh immediate response. Fixture players, queued requests and definitions are restored afterward.

Use a disposable no GUI development server on Java 21, Minecraft 1.21.1 and NeoForge 21.1.248,
with the `progressivestages` and `minecraft` GameTest namespaces enabled. The production launch
does not register these GameTests merely because `neoforge.enableGameTest` is true. Prepare the
repository's `forgeserverdev` launch and its complete runtime classpath. Bind only to the owned
private endpoint and verify readiness before executing each test separately:

```text
fill -2 180 -2 12 215 15 air
fill -2 179 -2 12 179 15 stone
execute positioned 0 180 0 run test run guipacketpurchaseandcommandburstsshareoneresponsebudget
```

Inspect the actual structure metadata and success marker. Clear and rebuild the same disposable
platform before running `queuedguiresponsesusecurrentdataandstopafterdisconnect`. The platform
fixes terrain dependent placement; the command's Y coordinate alone is not a result oracle.

Keep Minecraft client GUI presentation and reconnect acceptance separate. After the bounded
server suite, stop the exact owned process and remove its runtime and temporary build output.
Retain only the tested source and artifact identities, measured results, success evidence and
cleanup status in the [security review](../verification/3.0.5-security-review.md).

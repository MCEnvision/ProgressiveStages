# Snapshot Acknowledgement Tests

These dedicated server GameTests exercise `NetworkHandler.handleClientSnapshotAck` through its
payload context with controlled nonoperator server players. They use the actual snapshot sender
to establish offers. They do not establish client rendering, transport delivery or reconnect
behavior from a laptop.

`snapshotacknowledgementsrequireanofferedrevision` checks unsolicited packets, an incorrect
checksum, another player using the offer, an enforcement policy change without a revision
change, valid acceptance, a new compiled revision, a stale packet and disconnect cleanup.
The fixture restores the original compiled snapshot, enforcement policy and snapshot history,
then clears only its players' network state and discards those players in `finally`.

`snapshotacknowledgementburstsavoidsnapshotreconstruction` first offers the current snapshot,
warms the actual handler with 128 calls, then measures 1,000 repeated valid acknowledgements.
It requires less than 1 MiB of Java thread allocation for that burst and retains a valid
acknowledged base. This is a regression ceiling for redundant reconstruction, not a configurable
packet rate limit or a guarantee about other request handlers. The fixture records allocation
and elapsed time, restores the JVM allocation measurement setting and snapshot history, and
removes its player's state in `finally`.

Use Java 21, Minecraft 1.21.1 and NeoForge 21.1.248 with the `progressivestages` and `minecraft`
GameTest namespaces enabled. After the owned dedicated server is ready, run each test separately:

```text
fill -2 180 -2 12 215 15 air
fill -2 179 -2 12 179 15 stone
execute positioned 0 180 0 run test run snapshotacknowledgementsrequireanofferedrevision
```

After inspecting that test's actual structure metadata and result marker, clear the test area
and substitute `snapshotacknowledgementburstsavoidsnapshotreconstruction` in the final command.
The platform is only for a disposable world. The test framework chooses placement from the
terrain height, so a command's Y coordinate alone does not prove the result location.

Stop the owned server after the suite and remove its exact runtime, worlds, logs and temporary
build outputs after retaining sanitized evidence in the [acceptance record](../verification/3.0.5-acceptance.md).
Preserve shared dependency caches and unrelated processes. The
[security review](../verification/3.0.5-security-review.md) tracks other packet resource boundaries.

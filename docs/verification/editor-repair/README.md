# Editor and permission repair verification

Source commit `043fde3b4ca7a35a4ca73b2451da7a684573f251` repairs the eight editor defect groups
tracked in issue 47, the hidden window response timeout found during acceptance, and two LuckPerms
publication defects. This record does not close the final multiplayer acceptance tracked in issue 45.

The editor preserves untouched rule settings, keeps stage ownership separate from lifetime, writes
schema 4 attributes to the rules file, and rejects unsupported or destructive conversions. Runtime
consumers check the selected action. LuckPerms output remains valid when an unrelated player changes
their personal stage or permissions. Native context notifications reconcile the affected player
without reacting recursively to the bridge's own marker notifications.

## Automated and packaged verification

All 424 Java tests, 154 frontend tests and 119 required server GameTests passed. Type checking,
frontend packaging and the Gradle build passed. GameTests include the actual Selling Bin 1.6 and
LuckPerms 5.4.150 artifacts on Minecraft 1.21.1 and NeoForge 21.1.248. Native LuckPerms checks use
already loaded users and current player query options. They verify independent permissions and
synchronized access through survival, creative and survival transitions. The private review has
no unresolved actionable findings.

The source bound testing JAR has SHA256
`3267e54d635606e2696898d13f097dfebfb535dd2f28e1519efb3f784cea23fb`.
Its manifest records the source commit above and `Build-Dirty: false`. All 823 project class entries
match compiled output, packaged editor assets match the frontend build, and the JAR contains no
LuckPerms API classes. A Java 21 production dedicated server reached readiness, answered the time
query and diagnostic status command, and stopped normally. Exact dependency hashes are in
[observations.json](observations.json).

## Authenticated desktop observations

The silent NVIDIA laptop client and the dedicated server used the same earlier development JAR,
SHA256 `de502e926b0d756189d2670816efa5dc61a73d7907ef696629a6e99b1b4c44c3`.
This is separate evidence from the final source bound artifact. One authenticated player connected
with LuckPerms 5.4.150. Brave created and applied a personal profession stage, including a paired
bread restriction, a weather rule and a maximum health attribute. Saving and applying succeeded
while the Minecraft window was hidden. Export API content reimported through the editor with byte
identical stage and rule source. A downloaded export file was not verified.

With automatic selling enabled and the native food and emerald data packs active, unrestricted
carrots added 40 sale progress. Bread added 81 after the rank granted access. After synchronized
rank loss, the client retained all three bread, displayed the denial and left sale progress at 121.
Empty hand menu opening remained available for the selective restriction. Permanent retention
kept the stage after rank loss. [The denial image](bread-denied.png) records the retained bread and
required stage message.

The final candidate desktop matrix, complete provider lifecycle, native command matrix and
authenticated two player team lifecycle remain unverified. Detached and registered server player
fixtures do not establish authenticated multiplayer behavior. Gameplay input stopped when another
test needed the laptop desktop. No phase completion, final merge, phase tag or release is claimed.

## Cleanup

The owned editor tab, reverse tunnel, dedicated servers, client and mute watcher stopped. The client
required a forced stop after its normal shutdown did not exit. Exact laptop runtime and instance
link removal was verified, including the empty project parent created for this test. The complete
owned node 1 scratch directory was removed after retaining this sanitized evidence and the requested
JAR. Preexisting runtimes, shared caches, other clients and unrelated worktrees were preserved.

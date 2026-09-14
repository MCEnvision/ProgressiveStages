# Authenticated Diagnostic Capture Lifecycle

On September 13, 2026, the clean candidate from source
`7b99df1d62bffe31c4262b498082ea5e31644a99` passed the bounded capture checks below using an
authenticated laptop client and the normal production dedicated server. No implementation changed.

## Candidate and Method

Both hosts used ProgressiveStages 3.0.5 with SHA256
`fad646aa9bbc039d0c36b88d9ff79d929068960c8e32715bc5f1faaf5011deea`, Minecraft 1.21.1,
NeoForge 21.1.248 and Java 21. Optional providers were absent. The `node-1` server bound only
to loopback port 25589 with authentication enabled. The laptop connected through the existing
authorized private SSH route. Each server launch read back `eula=true`.

The original isolated Prism instance was preserved. Master volume was zero before every launch.
Hyprland window identity was correlated to the owned Java PID, its native PipeWire client,
playback node and PulseAudio stream serial. The application stream was explicitly muted and
read back before assertions. A watcher repeated identity and mute checks every half second.
The [first acceptance renderer](renderer.png) and [reconnected renderer](reconnect_renderer.png)
show the NVIDIA GeForce RTX 5090 Laptop GPU and OpenGL 4.6.0 with driver 610.57.04.

The operator entered complete start commands through targeted Hyprland keyboard events in the
owned game window. The console supplied operator setup, an ordinary stage grant, reload,
observations and shutdown. The sole fixture stage was personal with no rewards. Player names
are replaced with `player1` in the [structured evidence](evidence.json), which retains exact
capture IDs, observations, sanitized records, artifact hashes and cleanup.

## Observed Results

Times are America/Chicago.

| Scenario | Observed result |
|---|---|
| Operator start | Actual player input started progression capture at 15:13:05. A normal console grant at 15:13:41 produced one committed personal ownership record with one recipient. |
| Single capture | An editor start request during progression capture was rejected; the original capture remained active with its original ID. |
| Manual stop | Stop at 15:13:41 succeeded; a second stop reported already off. At 15:13:56, status showed `manual`, one record, 1,559 bytes, zero queued or dropped records, and `drained`. |
| Nonoperator denial | After removing operator permission, the full start command at 15:14:51 received `Incorrect argument for command`. Server status at 15:15:04 retained the prior stopped capture and record count. Restoring level 3 permitted subsequent starts. |
| Definition reload | Player input started editor capture at 15:15:09. Normal stage reload stopped it with reason `reload`, no queued or dropped records, and a drained 954 byte header. |
| Idle timeout | Player input started permissions capture at 15:15:31. At 15:15:49, 43 seconds remained. At 15:16:37 it reported `timeout`, no queued or dropped records, and a drained 959 byte header. This bounds the timeout without timestamping the exact expiry tick. |
| Disconnect | Player input started interaction capture at 15:17:10. Exiting the owned client closed its authenticated connection at 15:17:12. At 15:17:38, capture showed `target_removed`, no queued or dropped records, and a drained 961 byte header. This exercises connection loss, not the menu button. |
| Reconnect | The same player authenticated at 15:17:52. The previous capture stayed stopped with its original reason and ID. The replacement stream was separately verified muted. |
| Shutdown and restart | With that player online, the console started editor capture at 15:18:52 and observed it active before `stop`. The server saved and exited normally at 15:18:53, flushing its 955 byte header. The same runtime restarted at 15:20:01. Status at 15:20:23 reported off, empty target, zero records and bytes, and an idle writer. Another off was harmless; no old capture resumed. The final server stopped normally. |

All five capture headers contain valid JSON and the matching product, game and loader identities.
The sole decision record uses `selected` and `owner1` attribution without the player's name or
UUID and records the ordinary personal grant once.

An initial setup server lacked writable console input and exited with code 130 after being
stopped; it is not passing shutdown evidence. An invalid client simulation distance was corrected
before the acceptance launch. Early keyboard attempts did not execute the intended command,
including one argument forwarding attempt that opened the stage map. Only complete commands
correlated with the observations above count. Native Wayland cursor positioning warnings are
not treated as evidence of a clean client console.

This proves the stated authenticated controls, manual stop, reload, timeout, connection loss
and restart observations. Every category's gameplay producer, exact expiry tick, every limit
under real input, output failures, every permission level, provider events, browser authoring
and combined acceptance remain separate. The [performance procedure](../../test/diagnostics.md)
and its dedicated benchmark also remain separate.

## Cleanup

All three owned clients exited, including the initial setup attempt, and their playback streams
disappeared. All owned launchers, watchers and the tunnel exited; the laptop loopback port was
free. The original game directory was restored with inode `15099132`, and the instance config
again matched SHA256 `5c54429b91020f2db81d8228a86ddd95fb89aedb20868624a650f92566726ad2`.
No extra instance entries remained. All 19 laptop scratch files and their directory were removed.

Both acceptance server sessions exited with code zero; the final PID was `1989115`. No process
remained in the runtime and its port was free. After retaining the evidence, the shared library
symlink was unlinked and the exact runtime, world, captures and logs removed. All 836 preexisting
build paths, shared dependencies and the candidate remained. No build, dependency resolution,
Java source change, merge or release occurred in this check.

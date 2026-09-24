# 3.1.0 Phase 000 structure and diagnostics evidence

This record covers the bounded source and test evidence currently collected for Phase 000. It is
sanitized and contains no player identity, private endpoint, credential, or raw configuration.

## baseline

The phase workspace was created from the verified `origin/master` commit `c0a3ef2c32a5406a0bf12e43d2c475aa576fe9d6` with Minecraft 1.21.1, NeoForge 21.1.248, Java 21, and Gradle 8.8. The task graph for `test` contains no client launch. The root checkout remains dirty and is not used for phase edits.

The focused pre-change suite passed:

```text
./gradlew test --tests StageFileParserTest --tests Schema4StageCompilerTest --tests LegacyCompatibilityBaselineTest --tests DecisionResolverTest --tests CompiledRuleEngineTest --tests StructureSessionAccessPolicyTest --tests DiagnosticCaptureTest
```

## implemented source contracts

The accepted structure model now retains exact structure ID, owner stage, action, source key,
priority, entry padding, and accepted snapshot revision through the existing loader and registry
path. `entry_allowed = true` removes only that owner's entry contribution. Placement, breaking,
explosion, and spawn contributions remain independent. Actorless contributions are evaluated by
dimension and structure without a player. Exact IDs are required for structure targets.

The existing capture manager remains the one bounded recorder. It now accepts `structures` and
`abilities` categories, records player and actorless structure scopes, and records ability state
changes. The existing limits and asynchronous writer remain in force.

## focused verification

The following candidate suite passed after the source changes:

```text
./gradlew test --tests StageFileParserTest --tests Schema4StageCompilerTest --tests DiagnosticCaptureTest
./gradlew test
```

The focused aggregate and command registration checks also passed:

```text
./gradlew test --tests StructureRulesAggregateTest \\
  --tests StageCommandAliasTest.diagnosticCategoriesRemainUnderStageDebug
```

The required build also passed with `./gradlew build --no-daemon`. The candidate artifact was
`build/libs/progressivestages-3.0.5.jar`, SHA-256
`3a49528585e531feba26a0ec13b9c4ffc869d1b9c7ad56820d774d49769d8f6c`.

The server-only GameTest task was inspected as a no-client task and run with an isolated `run/`
directory whose `eula.txt` read back as `eula=true`. The new diagnostic capture command test and
all performance rounds, including `structures` and `abilities`, passed. The task still exited
nonzero because 15 pre-existing optional integration tests attempted to load the absent
`net.luckperms.api.LuckPermsProvider`. No structure or diagnostic test failed in that run.

## live fixture

The candidate jar was copied to a disposable client instance and connected to an isolated
dedicated server using the same Minecraft and NeoForge versions. The client audio option was set
to zero before launch. Its Minecraft playback stream was checked with `wpctl` and remained muted.
The server fixture generated a stronghold and loaded two stage files. The entry stage declared
`entry_allowed = false` with priority 10. The protection stage declared
`entry_allowed = true`, `prevent_block_place = true`, and priority 20.

Before the entry stage was granted, the player was repelled from the stronghold. The bounded
structure capture recorded an entry denial whose winner was the entry stage source, with priority
10. After the entry stage was granted, the player entered the same stronghold and a real client
placement attempt was denied while the protection stage remained ungranted. This confirms that
entry access and block placement protection are independent in the live path. The capture also
listed both stage contributions with their action and source keys.

The disposable client, server, RCON configuration, runtime world, capture files, and temporary
reports are test-owned resources and remain scheduled for removal after the final phase consumer.
The residual phase integration checks, release gates, and cleanup verification remain open until
the remaining Phase 000 tasks complete.

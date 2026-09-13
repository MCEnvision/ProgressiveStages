package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageCost;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.data.TeamStageData;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import com.enviouse.progressivestages.common.stage.PermissionStageSource;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.stage.StageOrder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.*;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class LuckPermsOfflineGameTests {
    private static final UUID FIRST = new UUID(0x5726L, 1);
    private static final UUID SECOND = new UUID(0x5726L, 2);
    private static final OwnerRef SHARED = new OwnerRef(OwnerKind.SERVER, StageManager.SERVER_TEAM);
    private static final StageId CHEF = StageId.parse("progressivestages:offline_chef");
    private static final StageId PERSONAL = StageId.parse("progressivestages:offline_personal");
    private static final StageId RETAINED = StageId.parse("progressivestages:offline_retained");
    private static final StageId WORLD = StageId.parse("progressivestages:offline_world");
    private static final StageId DEPENDENCY = StageId.parse("progressivestages:offline_dependency");
    private static final StageId PURCHASE = StageId.parse("progressivestages:offline_purchase");
    private static final List<StageId> STAGES = List.of(CHEF, PERSONAL, RETAINED, WORLD, DEPENDENCY, PURCHASE);

    private LuckPermsOfflineGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void offlineSourcesRevalidateWithoutAPlayerAndPreserveOtherOwners(GameTestHelper helper) throws Exception {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.order.registerStage(definition(CHEF, false, Map.of("server", List.of("professions"))));
            fixture.order.registerStage(StageDefinition.builder(PERSONAL).teamStage(false).luckPerms(options(false, Map.of())).build());
            fixture.order.registerStage(definition(RETAINED, true, Map.of()));
            fixture.order.registerStage(definition(WORLD, false, Map.of("world", List.of("overworld"))));
            fixture.data.grantStageFromSource(SHARED.id(), WORLD, source(FIRST, false));
            fixture.data.grantStageFromSource(SHARED.id(), CHEF, source(SECOND, false));
            fixture.adapter.changed.accept(FIRST);
            LuckPermsBridge.tick(fixture.server);
            helper.assertTrue(!fixture.data.hasEffectiveStage(SHARED, WORLD)
                && fixture.data.hasEffectiveStage(SHARED, CHEF), "Pending input must preserve another subject's effective source.");
            helper.assertTrue(fixture.adapter.jobs.size() == 1, "An offline request must not require a connected player.");
            fixture.adapter.finish(FIRST, true);
            LuckPermsBridge.tick(fixture.server);
            helper.assertTrue(fixture.data.getSources(SHARED, CHEF).equals(Set.of(source(FIRST, false), source(SECOND, false))),
                "Static server contexts must permit the qualified offline shared contribution.");
            helper.assertTrue(fixture.data.hasEffectiveStage(new OwnerRef(OwnerKind.PERSONAL, FIRST), PERSONAL),
                "Offline personal ownership must remain separate from shared storage.");
            helper.assertTrue(fixture.data.hasEffectiveStage(SHARED, RETAINED), "Qualified permanent input must be retained.");
            helper.assertTrue(!fixture.data.hasStage(SHARED.id(), WORLD), "An old runtime world must not satisfy an offline context.");
            fixture.adapter.changed.accept(FIRST);
            LuckPermsBridge.tick(fixture.server);
            fixture.adapter.finish(FIRST, false);
            LuckPermsBridge.tick(fixture.server);
            helper.assertTrue(fixture.data.getSources(SHARED, CHEF).equals(Set.of(source(SECOND, false))),
                "Offline rank loss must remove only that subject's synchronized contribution.");
            helper.assertTrue(!fixture.data.hasEffectiveStage(new OwnerRef(OwnerKind.PERSONAL, FIRST), PERSONAL),
                "Offline synchronized personal access must follow authoritative loss.");
            helper.assertTrue(fixture.data.hasEffectiveStage(SHARED, RETAINED), "Permanent access must survive offline loss.");
            fixture.data.grantStageFromSource(SHARED.id(), CHEF, "independent");
            fixture.adapter.changed.accept(SECOND);
            LuckPermsBridge.tick(fixture.server);
            fixture.adapter.finish(SECOND, false);
            LuckPermsBridge.tick(fixture.server);
            helper.assertTrue(fixture.data.getSources(SHARED, CHEF).equals(Set.of("independent")),
                "Removing the final synchronized contributor must preserve independently earned access.");
            helper.succeed();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void offlineResultsRejectStaleDefinitionsAndUnqualifiedGrants(GameTestHelper helper) throws Exception {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.order.registerStage(definition(CHEF, false, Map.of()));
            fixture.adapter.changed.accept(FIRST);
            LuckPermsBridge.tick(fixture.server);
            fixture.order.clear();
            fixture.order.registerStage(StageDefinition.builder(CHEF).teamStage(false).luckPerms(options(false, Map.of())).build());
            fixture.adapter.finish(FIRST, true);
            LuckPermsBridge.tick(fixture.server);
            helper.assertTrue(!fixture.data.hasStage(SHARED.id(), CHEF)
                && !fixture.data.hasPersonalStage(FIRST, CHEF), "An old owner observation must not grant into either namespace.");
            fixture.adapter.finish(FIRST, true);
            LuckPermsBridge.tick(fixture.server);
            helper.assertTrue(fixture.data.hasPersonalStage(FIRST, CHEF), "A new current observation must resolve the new owner.");
            fixture.order.registerStage(StageDefinition.builder(DEPENDENCY).scope("server").build());
            fixture.order.registerStage(StageDefinition.builder(WORLD).scope("server").dependencies(List.of(DEPENDENCY))
                .luckPerms(options(false, Map.of())).build());
            fixture.order.registerStage(StageDefinition.builder(PURCHASE).scope("server")
                .cost(new StageCost(5, List.of(), true, 0, 100)).luckPerms(options(false, Map.of())).build());
            fixture.adapter.changed.accept(FIRST);
            LuckPermsBridge.tick(fixture.server);
            fixture.adapter.finish(FIRST, true);
            LuckPermsBridge.tick(fixture.server);
            helper.assertTrue(!fixture.data.hasStage(SHARED.id(), WORLD) && !fixture.data.hasStage(SHARED.id(), DEPENDENCY)
                && !fixture.data.hasStage(SHARED.id(), PURCHASE), "Offline reconciliation must not grant prerequisites or purchases.");
            fixture.order.registerStage(definition(RETAINED, true, Map.of()));
            var context = fixture.manager.captureOfflinePermissionContext(FIRST);
            int[] checks = {0};
            helper.assertTrue(!fixture.manager.reconcileOfflinePermissionSources(context,
                Map.of(RETAINED, Set.of(source(FIRST, true))), () -> ++checks[0] == 1),
                "An observation invalidated during reconciliation must be rejected.");
            helper.assertTrue(!fixture.data.hasStage(SHARED.id(), RETAINED), "Stale input must not leave a permanent entitlement.");
            helper.succeed();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void offlineMembershipChangesRejectDelayedInputWithUnchangedOwners(GameTestHelper helper) throws Exception {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.order.registerStage(definition(CHEF, false, Map.of()));
            fixture.order.registerStage(definition(RETAINED, true, Map.of()));
            fixture.data.grantStageFromSource(SHARED.id(), CHEF, source(SECOND, false));
            fixture.adapter.changed.accept(FIRST);
            LuckPermsBridge.tick(fixture.server);
            com.enviouse.progressivestages.common.team.TeamProvider.getInstance().invalidateMembership();
            fixture.adapter.finish(FIRST, true);
            LuckPermsBridge.tick(fixture.server);
            helper.assertTrue(fixture.data.getSources(SHARED, CHEF).equals(Set.of(source(SECOND, false)))
                && !fixture.data.hasStage(SHARED.id(), RETAINED),
                "Stale membership must reject both retention modes without removing another contributor.");
            helper.assertTrue(fixture.data.getPermissionEpisodes(FIRST).isEmpty(),
                "Rejected membership must not create eligibility history.");
            fixture.adapter.finish(FIRST, true);
            LuckPermsBridge.tick(fixture.server);
            helper.assertTrue(fixture.data.getSources(SHARED, CHEF).contains(source(FIRST, false))
                && fixture.data.hasStage(SHARED.id(), RETAINED),
                "A newly loaded observation must reconcile under current membership.");
            helper.succeed();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void offlineRescanConvergesWhileContributorsAreRemoved(GameTestHelper helper) throws Exception {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.order.registerStage(definition(CHEF, false, Map.of()));
            for (int i = 1; i <= 300; i++) {
                fixture.data.grantStageFromSource(SHARED.id(), CHEF, source(new UUID(0x5726L, i), false));
            }
            fixture.adapter.automatic = true;
            LuckPermsBridge.reconcileAll();
            for (int tick = 0; tick < 128; tick++) {
                int before = fixture.adapter.requests;
                LuckPermsBridge.tick(fixture.server);
                helper.assertTrue(fixture.adapter.requests - before <= 16 && fixture.adapter.jobs.size() <= 8,
                    "Offline rescans must keep the shared tick budget and eight load limit.");
            }
            helper.assertTrue(!fixture.data.hasStage(SHARED.id(), CHEF) && fixture.data.nextPermissionSubject(null) == null,
                "Removing scanned contributors must not skip later persisted subjects.");
            helper.assertTrue(fixture.adapter.requests == 300 && fixture.adapter.jobs.isEmpty()
                && fixture.queue.size() == 0 && !fixture.queue.hasRescan(), "Every persisted offline subject must converge once.");
            helper.succeed();
        }
    }

    private static String source(UUID subject, boolean permanent) {
        return new PermissionStageSource(subject, "chef", permanent).label();
    }

    private static LuckPermsStageOptions options(boolean permanent, Map<String, List<String>> contexts) {
        return new LuckPermsStageOptions(true, true, permanent ? LuckPermsStageOptions.InboundMode.PERMANENT
            : LuckPermsStageOptions.InboundMode.SYNCHRONIZED,
            List.of(new LuckPermsStageOptions.InboundRule("chef", List.of(), List.of("professions.chef"),
                LuckPermsStageOptions.Match.ALL, contexts)), List.of(), List.of());
    }

    private static StageDefinition definition(StageId stage, boolean permanent, Map<String, List<String>> contexts) {
        return StageDefinition.builder(stage).scope("server").luckPerms(options(permanent, contexts)).build();
    }

    private static final class Fixture implements AutoCloseable {
        final MinecraftServer server;
        final StageOrder order = StageOrder.getInstance();
        final StageManager manager = StageManager.getInstance();
        final LuckPermsBridge bridge = LuckPermsBridge.getInstance();
        final List<StageDefinition> previous;
        final TeamStageData original;
        final TeamStageData data;
        final RecordingAdapter adapter = new RecordingAdapter();
        final SubjectReconciliationQueue queue;

        Fixture(GameTestHelper helper) throws Exception {
            server = helper.getLevel().getServer();
            helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "The offline fixture requires an isolated server.");
            previous = order.getOrderedStages().stream().map(stage -> order.getStageDefinition(stage).orElseThrow()).toList();
            helper.assertTrue(STAGES.stream().noneMatch(order::stageExists), "Fixture definitions must be unused.");
            original = server.overworld().getData(StageAttachments.TEAM_STAGES);
            data = new TeamStageData();
            server.overworld().setData(StageAttachments.TEAM_STAGES, data);
            var field = LuckPermsBridge.class.getDeclaredField("dirty");
            field.setAccessible(true);
            queue = (SubjectReconciliationQueue) field.get(bridge);
            queue.clear();
            bridge.setAdapterForTests(adapter);
            order.clear();
        }

        @Override public void close() {
            for (StageId stage : STAGES) {
                data.revokeStage(SHARED.id(), stage);
                for (int i = 1; i <= 300; i++) {
                    UUID subject = new UUID(0x5726L, i);
                    data.revokePersonalStage(subject, stage);
                    data.revokeStage(subject, stage);
                }
            }
            server.overworld().setData(StageAttachments.TEAM_STAGES, original);
            bridge.setAdapterForTests(null);
            queue.clear();
            order.clear();
            previous.forEach(order::registerStage);
        }
    }

    private static final class RecordingAdapter implements LuckPermsAdapter {
        final Map<UUID, Job> jobs = new HashMap<>();
        Consumer<UUID> changed;
        boolean automatic;
        int requests;
        @Override public State state() { return State.READY; }
        @Override public SubjectSnapshot snapshot(UUID subject) { throw new AssertionError("No online user query is expected"); }
        @Override public boolean groupExists(String group) { return false; }
        @Override public boolean subscribeChanges(Consumer<UUID> changed, Runnable allChanged) {
            this.changed = subject -> { invalidateOffline(subject); changed.accept(subject); }; return true;
        }
        @Override public boolean requestOffline(UUID subject, Set<String> permissions, Consumer<UUID> completed) {
            if (jobs.size() >= 8 || jobs.containsKey(subject)) return false;
            if (!permissions.equals(Set.of("professions.chef"))) throw new AssertionError("Only configured permissions may be queried");
            jobs.put(subject, new Job(completed));
            requests++;
            if (automatic) finish(subject, false);
            return true;
        }
        void finish(UUID subject, boolean allowed) {
            Job job = jobs.get(subject);
            if (job == null || job.result != null) throw new AssertionError("A matching pending load is required");
            job.result = new SubjectSnapshot(true, Set.of(), Map.of("professions.chef",
                allowed ? PermissionValue.TRUE : PermissionValue.FALSE), Map.of("server", Set.of("professions")));
            job.completed.accept(subject);
        }
        @Override public OfflineResult takeOffline(UUID subject) {
            Job job = jobs.get(subject); return job == null || job.result == null ? null : new OfflineResult(job.stale, job.result);
        }
        @Override public boolean isOfflineCurrent(UUID subject) { return jobs.containsKey(subject) && !jobs.get(subject).stale; }
        @Override public void completeOffline(UUID subject) { jobs.remove(subject); }
        @Override public void invalidateOffline(UUID subject) { if (jobs.containsKey(subject)) jobs.get(subject).stale = true; }
        @Override public void invalidateOffline() { jobs.values().forEach(job -> job.stale = true); }
        @Override public boolean shutdown() { jobs.clear(); return true; }
        @Override public MutationResult addTransient(UUID subject, NodeKind kind, String value, Map<String, String> contexts, String owner) {
            throw new AssertionError("Offline sources must not create player output nodes");
        }
        @Override public MutationResult removeTransient(UUID subject, NodeKind kind, String value, Map<String, String> contexts, String owner) {
            throw new AssertionError("Offline sources have no output node manifest");
        }
        private static final class Job {
            final Consumer<UUID> completed;
            SubjectSnapshot result;
            boolean stale;
            Job(Consumer<UUID> completed) { this.completed = completed; }
        }
    }
}

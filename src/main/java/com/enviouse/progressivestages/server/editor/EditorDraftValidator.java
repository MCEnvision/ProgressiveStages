package com.enviouse.progressivestages.server.editor;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.api.ProgressiveStagesAPI;
import com.enviouse.progressivestages.common.config.StageConfig;
import com.enviouse.progressivestages.common.stage.FieldDiagnostic;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.lock.LockDefinition;
import com.enviouse.progressivestages.common.rehaul.SelectorSpec;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.server.enforcement.InventoryTargetResolverRegistry;
import com.enviouse.progressivestages.server.loader.Schema4StageCompiler;
import com.enviouse.progressivestages.server.loader.StageFileParser;
import com.enviouse.progressivestages.server.loader.StagePackageDiscovery;
import com.enviouse.progressivestages.server.loader.StagePackageParser;
import net.minecraft.core.registries.BuiltInRegistries;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EditorDraftValidator {
    private EditorDraftValidator() {}

    static DraftValidation validate(Map<String, String> files, long revision) {
        Path temporary = null;
        try {
            temporary = Files.createTempDirectory("progressivestages-editor-validation-");
            List<String> errors = new ArrayList<>();
            for (Map.Entry<String, String> file : files.entrySet()) {
                String path = EditorPaths.normalize(file.getKey());
                if (path.equals("progressivestages.toml")) {
                    MainConfigValidation main = validateMainConfig(file.getValue());
                    errors.addAll(main.errors());
                    continue;
                }
                if (!path.startsWith("stages/")) throw new IllegalArgumentException("Draft TOML is outside the supported config paths");
                Path target = temporary.resolve(path.substring("stages/".length())).normalize();
                if (!target.startsWith(temporary)) throw new IllegalArgumentException("Draft path escapes validation root");
                Files.createDirectories(target.getParent());
                Files.writeString(target, file.getValue());
            }
            StagePackageDiscovery.DiscoveryResult discovery = StagePackageDiscovery.discover(temporary);
            errors.addAll(discovery.errors());
            List<DraftValidation.Diagnostic> diagnostics = new ArrayList<>();
            Map<StageId, StageDefinition> definitions = new LinkedHashMap<>();
            Map<StageId, String> sourceFiles = new LinkedHashMap<>();
            for (var source : discovery.packages()) {
                var parsed = StagePackageParser.parse(source);
                if (!parsed.isSuccess()) {
                    errors.add(source.root().getFileName() + ". " + parsed.getErrorMessage());
                    parsed.getFieldDiagnostic("stages/" + temporary.relativize(source.identityFile()).toString().replace('\\', '/'))
                        .filter(diagnostic -> !diagnostic.code().equals("invalid_stage"))
                        .map(DraftValidation.Diagnostic::from).ifPresent(diagnostics::add);
                    continue;
                }
                StageDefinition definition = parsed.getStageDefinition();
                try { Schema4StageCompiler.compile(definition, parsed.getSourceConfig(), source.sourceId(), 0); }
                catch (RuntimeException error) { errors.add(definition.getId() + ". " + error.getMessage()); }
                validateInventoryTargets(definition, errors);
                if (definitions.putIfAbsent(definition.getId(), definition) != null) errors.add("Duplicate stage id. " + definition.getId());
                sourceFiles.putIfAbsent(definition.getId(), "stages/" + temporary.relativize(source.identityFile()).toString().replace('\\', '/'));
            }
            for (Path source : discovery.legacyFiles()) {
                var parsed = StageFileParser.parseWithErrors(source);
                if (!parsed.isSuccess()) {
                    errors.add(source.getFileName() + ". " + parsed.getErrorMessage());
                    parsed.getFieldDiagnostic("stages/" + temporary.relativize(source).toString().replace('\\', '/'))
                        .map(DraftValidation.Diagnostic::from).ifPresent(diagnostics::add);
                    continue;
                }
                StageDefinition definition = parsed.getStageDefinition();
                if (definitions.putIfAbsent(definition.getId(), definition) != null) errors.add("Duplicate stage id. " + definition.getId());
                sourceFiles.putIfAbsent(definition.getId(), "stages/" + temporary.relativize(source).toString().replace('\\', '/'));
            }
            errors.addAll(StageOrder.validateDefinitions(definitions.values()));
            Path validationRoot = temporary;
            List<String> warnings = new ArrayList<>(discovery.ignoredFiles().stream()
                .map(path -> "Ignored helper TOML. " + validationRoot.relativize(path)).toList());
            var capabilities = ProgressiveStagesAPI.getStageCapabilities(definitions.values());
            for (var definition : definitions.values()) {
                for (var diagnostic : ProgressiveStagesAPI.validateStageOptions(definition, sourceFiles.get(definition.getId()), capabilities)) {
                    diagnostics.add(DraftValidation.Diagnostic.from(diagnostic));
                    String summary = diagnostic.file() + ". " + diagnostic.message();
                    if (diagnostic.severity() == FieldDiagnostic.Severity.ERROR) errors.add(summary);
                    else warnings.add(summary);
                }
            }
            return new DraftValidation(errors.isEmpty(), errors, warnings, definitions.size(), revision,
                diagnostics, capabilities, StageConfig.getTeamMode());
        } catch (IOException | RuntimeException error) {
            return new DraftValidation(false, List.of(error.getMessage()), List.of(), 0, revision);
        } finally {
            if (temporary != null) {
                try (var paths = Files.walk(temporary)) {
                    for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
                } catch (IOException ignored) {}
            }
        }
    }

    static MainConfigValidation validateMainConfig(String content) {
        try {
            CommentedConfig parsed = new TomlParser().parse(new StringReader(content));
            List<String> errors = new ArrayList<>();
            validateMainNode(StageConfig.SPEC.getSpec(), parsed, new ArrayList<>(), errors);
            return new MainConfigValidation(errors.isEmpty(), errors, parsed);
        } catch (RuntimeException error) {
            return new MainConfigValidation(false,
                List.of("progressivestages.toml. " + (error.getMessage() == null ? "invalid TOML" : error.getMessage())), null);
        }
    }

    private static void validateMainNode(UnmodifiableConfig spec, UnmodifiableConfig candidate,
                                         List<String> path, List<String> errors) {
        for (var entry : candidate.entrySet()) {
            if (!spec.contains(entry.getKey())) {
                errors.add("progressivestages.toml. " + dotted(path, entry.getKey()) + " is not a supported setting");
            }
        }
        for (var entry : spec.entrySet()) {
            path.add(entry.getKey());
            Object specValue = entry.getValue();
            if (specValue instanceof UnmodifiableConfig nested) {
                Object candidateValue = candidate.contains(entry.getKey()) ? candidate.getRaw(entry.getKey()) : null;
                if (candidateValue instanceof UnmodifiableConfig candidateSection) {
                    validateMainNode(nested, candidateSection, path, errors);
                } else if (candidateValue != null) {
                    errors.add("progressivestages.toml. " + String.join(".", path) + " must be a table");
                }
            } else if (candidate.contains(entry.getKey())) {
                var valueSpec = (net.neoforged.neoforge.common.ModConfigSpec.ValueSpec) specValue;
                Object candidateValue = candidate.getRaw(entry.getKey());
                if (!valueSpec.test(candidateValue)) {
                    String type = valueSpec.getClazz() == null ? "the configured type" : valueSpec.getClazz().getSimpleName();
                    errors.add("progressivestages.toml. " + String.join(".", path) + " must use a valid " + type + " within the configured limits");
                }
            }
            path.remove(path.size() - 1);
        }
    }

    private static String dotted(List<String> path, String leaf) {
        if (path.isEmpty()) return leaf;
        return String.join(".", path) + "." + leaf;
    }

    record MainConfigValidation(boolean valid, List<String> errors, CommentedConfig config) {
        MainConfigValidation {
            errors = errors == null ? List.of() : List.copyOf(errors);
        }
    }

    private static void validateInventoryTargets(StageDefinition definition, List<String> errors) {
        for (LockDefinition.InteractionLock lock : definition.getLocks().interactions()) {
            if (!"item_into_inventory".equals(lock.type())) continue;
            var selector = SelectorSpec.parse(lock.target());
            if (selector.isEmpty() || !selector.get().matcherId().equals(SelectorSpec.ID)) continue;
            var target = selector.get().resourceId();
            boolean valid = switch (lock.targetKind()) {
                case "block" -> BuiltInRegistries.BLOCK.containsKey(target);
                case "menu" -> BuiltInRegistries.MENU.containsKey(target);
                case "inventory" -> InventoryTargetResolverRegistry.get().catalogTargets().stream()
                    .anyMatch(descriptor -> descriptor.id().equals(target));
                default -> false;
            };
            if (!valid) {
                errors.add(definition.getId() + ". interactions.item_into_inventory.target is not a registered "
                    + lock.targetKind() + " target. " + target);
            }
        }
    }
}

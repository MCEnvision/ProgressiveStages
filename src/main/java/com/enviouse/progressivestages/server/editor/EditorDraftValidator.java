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

import java.io.IOException;
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
            for (Map.Entry<String, String> file : files.entrySet()) {
                String path = EditorPaths.normalize(file.getKey());
                if (path.equals("progressivestages.toml")) {
                    new com.electronwill.nightconfig.toml.TomlParser().parse(file.getValue());
                    continue;
                }
                if (!path.startsWith("stages/")) throw new IllegalArgumentException("Draft TOML is outside the supported config paths");
                Path target = temporary.resolve(path.substring("stages/".length())).normalize();
                if (!target.startsWith(temporary)) throw new IllegalArgumentException("Draft path escapes validation root");
                Files.createDirectories(target.getParent());
                Files.writeString(target, file.getValue());
            }
            StagePackageDiscovery.DiscoveryResult discovery = StagePackageDiscovery.discover(temporary);
            List<String> errors = new ArrayList<>(discovery.errors());
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

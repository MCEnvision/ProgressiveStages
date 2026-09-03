package com.enviouse.progressivestages.server.loader;

import com.enviouse.progressivestages.common.api.StageId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Migrates the exact generic recipe-rule shape written by the pre-3.0.4 visual editor.
 *
 * <p>The old editor represented an output-item recipe lock as a generic craft rule. That
 * representation is now rejected because a generic recipe selector cannot distinguish an
 * output item from a recipe identifier. Only the old editor's generated, single-target shape
 * is safe to identify here. Hand-authored generic rules remain untouched and receive the
 * normal field-specific compiler error.</p>
 */
public final class LegacyRecipeOutputRuleMigration {

    private static final Pattern RULE_BLOCK = Pattern.compile("(?ms)^[\\t ]*\\[\\[rules]]\\s*(?:\\R|$)(.*?)(?=^[\\t ]*\\[\\[|\\z)");
    private static final Pattern ID = Pattern.compile("(?m)^[\\t ]*id[\\t ]*=[\\t ]*\"([^\"]+)\"[\\t ]*(?:#.*)?$");
    private static final Pattern EFFECT = Pattern.compile("(?m)^[\\t ]*effect[\\t ]*=[\\t ]*\"lock\"[\\t ]*(?:#.*)?$");
    private static final Pattern ACTION = Pattern.compile("(?m)^[\\t ]*action[\\t ]*=[\\t ]*\"craft\"[\\t ]*(?:#.*)?$");
    private static final Pattern PRIORITY = Pattern.compile("(?m)^[\\t ]*priority[\\t ]*=[\\t ]*(-?\\d+)[\\t ]*(?:#.*)?$");
    private static final Pattern TARGET = Pattern.compile("(?m)^[\\t ]*targets\\.recipes[\\t ]*=[\\t ]*\\[[\\t ]*\"([^\"]+)\"[\\t ]*][\\t ]*(?:#.*)?$");
    private static final Pattern RECIPES_SECTION = Pattern.compile("(?m)^[\\t ]*\\[recipes]\\s*(?:#.*)?$");
    private static final Pattern GENERATED_EDITOR_ID = Pattern.compile("^[a-z0-9_.-]+:[a-z0-9_./-]+/rule_[a-z0-9]+$");
    private static final Pattern GENERATED_SHOWCASE_ID = Pattern.compile("^showcase:lock_recipes_\\d+$");

    private LegacyRecipeOutputRuleMigration() {}

    /**
     * Normalizes the one unambiguous recipe-output rule shape produced by the legacy
     * Easy Builder without touching a live stage file. Editor drafts use this before
     * validation so source view, review, and apply all expose the canonical field.
     */
    public static DraftNormalization normalizeDraftRules(String stageContent, String rulesContent) {
        if (hasIncludedRuleFiles(stageContent)) return DraftNormalization.unchanged(rulesContent);
        String migrated = rewrite(rulesContent);
        return migrated.equals(rulesContent) ? DraftNormalization.unchanged(rulesContent)
            : DraftNormalization.migrated(migrated);
    }

    static Result migrate(StagePackageSource source, StageId expectedStage) {
        if (source.rulesFile().isEmpty() || !source.additionalRuleFiles().isEmpty()
                || !source.additionalProgressionFiles().isEmpty()) {
            return Result.unchanged();
        }
        Path rulesFile = source.rulesFile().orElseThrow();
        String original;
        try {
            original = Files.readString(rulesFile);
        } catch (IOException error) {
            return Result.failure("Could not read rules.toml for recipe migration. " + error.getMessage());
        }
        String migrated = rewrite(original);
        if (migrated.equals(original)) return Result.unchanged();

        try {
            validate(source, expectedStage, migrated);
            Path backup = writeBackup(source, rulesFile, original);
            if (!Files.readString(rulesFile).equals(original)) {
                throw new IOException("rules.toml changed while its migration was being prepared");
            }
            replaceAtomically(rulesFile, migrated);
            return Result.migrated(backup);
        } catch (IOException | RuntimeException error) {
            return Result.failure("Could not migrate the legacy recipe output rule. " + error.getMessage());
        }
    }

    private static String rewrite(String original) {
        if (RECIPES_SECTION.matcher(original).find()) return original;
        Matcher matcher = RULE_BLOCK.matcher(original);
        StringBuilder rewritten = new StringBuilder();
        int migratedRules = 0;
        int last = 0;
        while (matcher.find()) {
            LegacyRule rule = LegacyRule.parse(matcher.group(1));
            if (rule == null || !rule.isRecognizableEditorOutputRule()) continue;
            migratedRules++;
            rewritten.append(original, last, matcher.start());
            rewritten.append("[recipes]\nlocked_items = [\"").append(rule.target()).append("\"]\n")
                .append("priority = ").append(rule.priority()).append("\n\n");
            last = matcher.end();
        }
        if (migratedRules != 1) return original;
        rewritten.append(original, last, original.length());
        return rewritten.toString();
    }

    private static boolean hasIncludedRuleFiles(String stageContent) {
        return Pattern.compile("(?m)^[\\t ]*(?:rules_includes|progression_includes)[\\t ]*=")
            .matcher(stageContent == null ? "" : stageContent).find();
    }

    private static void validate(StagePackageSource source, StageId expectedStage, String rulesContent) {
        try {
            String identity = Files.readString(source.identityFile());
            String progression = source.progressionFile().isPresent()
                ? Files.readString(source.progressionFile().orElseThrow()) : null;
            StageFileParser.ParseResult parsed = StagePackageParser.parseContents(source.sourceId(),
                source.identityFile().getFileName().toString(), identity,
                source.rulesFile().orElseThrow().getFileName().toString(), rulesContent,
                progression == null ? null : source.progressionFile().orElseThrow().getFileName().toString(), progression);
            if (!parsed.isSuccess()) throw new IllegalArgumentException(parsed.getErrorMessage());
            if (!parsed.getStageDefinition().getId().equals(expectedStage)) {
                throw new IllegalArgumentException("The migrated package changed the stage id");
            }
            Schema4StageCompiler.compile(parsed.getStageDefinition(), parsed.getSourceConfig(), source.sourceId(), 0);
        } catch (IOException error) {
            throw new IllegalArgumentException("Could not validate the migrated package. " + error.getMessage(), error);
        }
    }

    private static Path writeBackup(StagePackageSource source, Path rulesFile, String original) throws IOException {
        Path stagesRoot = source.root().getParent();
        if (stagesRoot == null) throw new IOException("The stage package has no stages root");
        Path relativePackage = stagesRoot.relativize(source.root());
        Path backup = stagesRoot.resolve(".migration-backups/recipe-output-v3")
            .resolve(relativePackage).resolve("rules.toml").normalize();
        if (!backup.startsWith(stagesRoot)) throw new IOException("Recipe migration backup escaped the stages directory");
        Files.createDirectories(backup.getParent());
        if (Files.exists(backup)) {
            if (!Files.readString(backup).equals(original)) {
                throw new IOException("A different recipe migration backup already exists");
            }
            return backup;
        }
        Path temporary = Files.createTempFile(backup.getParent(), ".rules.toml-", ".tmp");
        try {
            Files.writeString(temporary, original, StandardCharsets.UTF_8);
            move(temporary, backup);
        } finally {
            Files.deleteIfExists(temporary);
        }
        return backup;
    }

    private static void replaceAtomically(Path target, String content) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), ".rules.toml-", ".tmp");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            move(temporary, target);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record LegacyRule(String id, String target, int priority, int fieldCount) {
        static LegacyRule parse(String body) {
            Matcher id = ID.matcher(body);
            Matcher priority = PRIORITY.matcher(body);
            Matcher target = TARGET.matcher(body);
            if (!id.find() || !priority.find() || !target.find() || !EFFECT.matcher(body).find()
                    || !ACTION.matcher(body).find()) {
                return null;
            }
            int fields = countMatches(ID, body) + countMatches(EFFECT, body) + countMatches(ACTION, body)
                + countMatches(PRIORITY, body) + countMatches(TARGET, body);
            try {
                return new LegacyRule(id.group(1), target.group(1), Integer.parseInt(priority.group(1)), fields);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        boolean isRecognizableEditorOutputRule() {
            return fieldCount == 5 && (GENERATED_EDITOR_ID.matcher(id).matches()
                || GENERATED_SHOWCASE_ID.matcher(id).matches());
        }
    }

    private static int countMatches(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    record Result(boolean migrated, Path backup, String error) {
        static Result unchanged() { return new Result(false, null, ""); }
        static Result migrated(Path backup) { return new Result(true, backup, ""); }
        static Result failure(String error) { return new Result(false, null, error); }
        boolean failed() { return !error.isBlank(); }
    }

    public record DraftNormalization(String rulesContent, boolean migrated) {
        static DraftNormalization unchanged(String rulesContent) {
            return new DraftNormalization(rulesContent, false);
        }

        static DraftNormalization migrated(String rulesContent) {
            return new DraftNormalization(rulesContent, true);
        }
    }
}

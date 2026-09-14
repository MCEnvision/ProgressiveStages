package com.enviouse.progressivestages.server.loader;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlParser;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class StagePackageDiscovery {

    private StagePackageDiscovery() {}

    public static DiscoveryResult discover(Path stagesRoot) {
        if (stagesRoot == null || !Files.isDirectory(stagesRoot)) {
            return new DiscoveryResult(List.of(), List.of(), List.of(), List.of());
        }
        Path normalizedRoot = stagesRoot.toAbsolutePath().normalize();
        List<Path> allTomlFiles;
        try (var stream = Files.walk(normalizedRoot)) {
            allTomlFiles = stream.filter(Files::isRegularFile)
                .filter(StagePackageDiscovery::isToml)
                .sorted(Comparator.comparing(path -> normalizedRoot.relativize(path).toString()))
                .toList();
        } catch (IOException error) {
            return new DiscoveryResult(List.of(), List.of(), List.of(),
                List.of("Could not scan the stages directory. " + error.getMessage()));
        }
        List<Path> hiddenFiles = allTomlFiles.stream()
            .filter(path -> hasHiddenSegment(normalizedRoot.relativize(path))).toList();
        List<Path> tomlFiles = allTomlFiles.stream()
            .filter(path -> !hasHiddenSegment(normalizedRoot.relativize(path))).toList();

        List<Path> packageRoots = tomlFiles.stream()
            .filter(path -> path.getFileName().toString().equalsIgnoreCase("stage.toml"))
            .map(Path::getParent)
            .sorted(Comparator.comparing(path -> normalizedRoot.relativize(path).toString()))
            .toList();

        List<StagePackageSource> packages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (Path packageRoot : packageRoots) {
            try {
                packages.add(StagePackageParser.inspect(normalizedRoot, packageRoot));
            } catch (IOException | IllegalArgumentException error) {
                errors.add(normalizedRoot.relativize(packageRoot) + ". " + error.getMessage());
            }
        }

        List<Path> legacy = new ArrayList<>();
        List<Path> ignored = new ArrayList<>(hiddenFiles);
        for (Path file : tomlFiles) {
            if (file.getFileName().toString().equalsIgnoreCase("triggers.toml")) {
                ignored.add(file);
                continue;
            }
            boolean belongsToPackage = packageRoots.stream().anyMatch(root -> file.startsWith(root));
            if (belongsToPackage) continue;
            try {
                if (hasStageDefinition(Files.readString(file))) legacy.add(file);
                else ignored.add(file);
            } catch (IOException error) {
                errors.add(normalizedRoot.relativize(file) + ". " + error.getMessage());
            }
        }
        return new DiscoveryResult(packages, legacy, ignored, errors);
    }

    static boolean hasStageDefinition(String text) {
        Config parsed = Config.inMemory();
        try {
            new TomlParser().parse(new StringReader(text), parsed, ParsingMode.REPLACE);
            return parsed.contains("stage");
        } catch (ParsingException error) {
            return parsed.contains("stage") || text.lines().anyMatch(StagePackageDiscovery::declaresStage);
        }
    }

    private static boolean declaresStage(String line) {
        String candidate = line.stripLeading();
        if (candidate.isEmpty() || candidate.startsWith("#")) return false;
        if (!candidate.startsWith("[")) {
            int assignment = candidate.indexOf('=');
            if (assignment < 0) return false;
            candidate = candidate.substring(0, assignment + 1) + " { _ = true }\n";
        } else candidate += "\n_ = true\n";
        try {
            return new TomlParser().parse(candidate).contains("stage");
        } catch (ParsingException error) {
            return false;
        }
    }

    private static boolean isToml(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".toml");
    }

    private static boolean hasHiddenSegment(Path path) {
        for (Path segment : path) if (segment.toString().startsWith(".")) return true;
        return false;
    }

    public record DiscoveryResult(
            List<StagePackageSource> packages,
            List<Path> legacyFiles,
            List<Path> ignoredFiles,
            List<String> errors) {
        public DiscoveryResult {
            packages = List.copyOf(packages);
            legacyFiles = List.copyOf(legacyFiles);
            ignoredFiles = List.copyOf(ignoredFiles);
            errors = List.copyOf(errors);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }
    }
}

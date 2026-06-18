package com.glisterbyte.singingmonsters.sfsdocs;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateSfsApiDocs {

    private static final String resourcePath = "/sfs-api-docs/";
    private static final Path outputPath = Path.of("sfs-api-docs");

    private static Path getResourceDir(String path) {
        try {
            URI uri = Objects.requireNonNull(GenerateSfsApiDocs.class.getResource(path)).toURI();
            if (uri.getScheme().equals("jar")) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Map.of())) {
                    return fileSystem.getPath(path);
                }
            } else {
                return Path.of(uri);
            }
        }
        catch (URISyntaxException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<Path> getFilesInResourceDir(String path) {
        try (var stream = Files.list(getResourceDir(path))) {
            return stream.toList();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void writeIndexFile(List<Document> docs) throws IOException {

        List<String> indexLines = new ArrayList<>();

        indexLines.add("Unofficial My Singing Monsters API Docs");
        indexLines.add("=======================================");

        indexLines.add("""
                .. warning::
                   This documentation is currently largely incomplete.
                   For any particular command/event/data, the only documented parameters are those
                   that have been found relevant before. There are a lot of parameters that just
                   aren't documented here but do actually exist.
                   For example, :doc:`data/GenericUpdate` includes a ``coins`` field, but
                   it is unknown how that differs from ``coins_actual``, so it isn't documented.""");

        indexLines.add("");
        indexLines.add(".. toctree::");
        indexLines.add("   :maxdepth: 1");
        indexLines.add("   :caption: Topics");
        indexLines.add("");
        for (
                Path path : getFilesInResourceDir(resourcePath + "source/topics").stream()
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith(".md"))
                    .toList()
        ) {
            String fileName = path.getFileName().toString();
            fileName = fileName.substring(0, fileName.length() - 3);
            indexLines.add("   topics/" + fileName);
        }

        Set<String> categories = docs.stream().map(Document::getCategory).collect(Collectors.toSet());
        for (String category : categories.stream().sorted().toList()) {

            String title = Character.toUpperCase(category.charAt(0)) + category.substring(1);

            indexLines.add("");
            indexLines.add(".. toctree::");
            indexLines.add("   :maxdepth: 1");
            indexLines.add("   :caption: " + title);
            indexLines.add("");

            for (
                    Document doc : docs.stream()
                    .filter(d -> d.getCategory().equals(category))
                    .sorted(Comparator.comparing(Document::getTitle))
                    .toList()
            ) {
                indexLines.add("   " + category + "/" + doc.getFileName());
            }

        }

        Files.writeString(
                outputPath.resolve("source/index.rst"),
                String.join("\n", indexLines)
        );

    }

    public static void main(String[] args) throws IOException {

        FileUtils.copyDirectory(
                new File(getResourceDir(resourcePath).toUri()),
                new File(outputPath.toUri())
        );

        List<Document> docs = new DocumentMaster().documentEverything();
        for (Document doc : docs) {
            Path dir = outputPath.resolve("source").resolve(doc.getCategory());
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path path = dir.resolve(doc.getFileName() + ".md");
            Files.writeString(path, doc.toString());
        }

        writeIndexFile(docs);

    }

}
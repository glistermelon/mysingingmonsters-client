package com.glisterbyte.singingmonsters.sfsdocs;

import com.github.therapi.runtimejavadoc.*;
import com.glisterbyte.singingmonsters.sfsmodels.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateSfsApiDocs {

    private static final Path sourcePath = Path.of("sfs-api-docs/source");

    private static void writeIndexFile(List<Document> docs) throws IOException {

        List<String> indexLines = new ArrayList<>();

        indexLines.add("Unofficial My Singing Monsters API Docs");
        indexLines.add("=======================================");

        indexLines.add(".. warning::\n" +
                "   This documentation is currently largely incomplete.\n" +
                "   For any particular command/event/data, the only documented parameters are those\n" +
                "   that have been found relevant before. There are a lot of parameters that just\n" +
                "   aren't documented here but do actually exist.\n" +
                "   For example, :doc:`data/GenericUpdate` includes a ``coins`` field, but\n" +
                "   it is unknown how that differs from ``coins_actual``, so it isn't documented.");

        indexLines.add("");
        indexLines.add(".. toctree::");
        indexLines.add("   :maxdepth: 1");
        indexLines.add("   :caption: Topics");
        indexLines.add("");
        try (Stream<Path> topicsFilesStream = Files.list(sourcePath.resolve("topics"))) {
            for (
                    Path path : topicsFilesStream
                        .filter(Files::isRegularFile)
                        .filter(f -> f.getFileName().toString().endsWith(".md"))
                        .toList()
            ) {
                String fileName = path.getFileName().toString();
                fileName = fileName.substring(0, fileName.length() - 3);
                indexLines.add("   topics/" + fileName);
            }
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
                sourcePath.resolve("index.rst"),
                String.join("\n", indexLines)
        );

    }

    public static void main(String[] args) throws IOException {
        List<Document> docs = new DocumentMaster().documentEverything();
        for (Document doc : docs) {
            Path dir = sourcePath.resolve(doc.getCategory());
            if (!Files.exists(dir)) Files.createDirectory(dir);
            Path path = dir.resolve(doc.getFileName() + ".md");
            Files.writeString(path, doc.toString());
        }
        writeIndexFile(docs);
    }

}
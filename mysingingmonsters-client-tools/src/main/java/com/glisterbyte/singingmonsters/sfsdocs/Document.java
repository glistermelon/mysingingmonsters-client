package com.glisterbyte.singingmonsters.sfsdocs;

import com.github.therapi.runtimejavadoc.Comment;
import com.github.therapi.runtimejavadoc.CommentFormatter;
import com.glisterbyte.singingmonsters.sfsmodels.SfsModel;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.glisterbyte.singingmonsters.common.StringUtil.format;

public class Document {

    private static class SectionTree {

        private static class Node {

            String content = "";
            final Map<String, Node> children = new LinkedHashMap<>();
            final Node parent;

            private Node(Node parent) {
                this.parent = parent;
            }

            public Node() {
                this(null);
            }

            public Node createChild(String subsection) {
                Node child = new Node(this);
                children.put(subsection, child);
                return child;
            }

            public void toString(List<String> outLines, String title, int depth) {
                if (!outLines.isEmpty()) outLines.add("");
                outLines.add("#".repeat(depth + 1) + " " + title);
                outLines.add("");
                outLines.add(content);
                for (var entry : children.entrySet()) {
                    String childTitle = entry.getKey();
                    Node child = entry.getValue();
                    child.toString(outLines, childTitle, depth + 1);
                }
            }

        }

        private final Node rootNode = new Node();
        private Node currentNode = rootNode;

        public void pushSection(String section) {
            currentNode = currentNode.createChild(section);
        }

        public void popSection() {
            if (currentNode.parent == null) throw new RuntimeException("No section to pop");
            currentNode = currentNode.parent;
        }

        public void popAllSections() {
            currentNode = rootNode;
        }

        public boolean isAtRoot() {
            return currentNode == rootNode;
        }

        public void addContent(String line) {
            String content = currentNode.content;
            content = content.strip();
            if (!content.isEmpty()) content += "\n";
            content += line;
            currentNode.content = content;
        }

        public String toString(String title) {
            List<String> lines = new ArrayList<>();
            rootNode.toString(lines, title, 0);
            return String.join("\n", lines);
        }

    }

    final DocumentMaster master;

    private final String category;
    private final String fileName;

    private String title;

    private final SectionTree sectionTree = new SectionTree();

    public Document(DocumentMaster master, String category, String fileName) {
        this.master = master;
        this.category = category;
        this.fileName = fileName;
    }

    private static final CommentFormatter formatter = new CommentFormatter();

    public String getCategory() {
        return category;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRelativePath() {
        return category + "/" + fileName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void editTitleSection() {
        sectionTree.popAllSections();
    }

    public void newSection(String section) {
        if (!sectionTree.isAtRoot()) sectionTree.popSection();
        sectionTree.pushSection(section);
    }

    public void newSubSection(String subsection) {
        sectionTree.pushSection(subsection);
    }

    public void popSection() {
        sectionTree.popSection();
    }

    public void add(String line) {
        sectionTree.addContent(line);
    }

    public void add(Comment comment) {
        add(formatter.format(comment));
    }

    public void add(Table table) {
        add(table.toString());
    }

    private String formatClassSubstitutions(String string) {

        Pattern pattern = Pattern.compile("\\b\\w+\\b");
        Matcher matcher = pattern.matcher(string);

        StringBuilder formatted = new StringBuilder();

        while (matcher.find()) {
            String word = matcher.group();
            Class<? extends SfsModel> targetClass = master.getClassForSimpleName(word);
            if (targetClass == null) continue;
            Document targetDoc = master.getDocument(targetClass);
            if (targetDoc == null) continue;
            String replacement = format("[{}](/{})", targetDoc.getTitle(), targetDoc.getRelativePath());
            matcher.appendReplacement(formatted, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(formatted);

        return formatted.toString();

    }

    @Override
    public String toString() {
        return formatClassSubstitutions(sectionTree.toString(title));
    }

}
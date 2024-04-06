package com.probejs.document;

import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.document.comment.CommentUtil;
import com.probejs.document.comment.special.CommentAssign;
import com.probejs.document.comment.special.CommentTarget;
import com.probejs.document.parser.processor.Document;
import com.probejs.document.type.IType;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import me.shedaniel.architectury.platform.Mod;
import me.shedaniel.architectury.platform.Platform;

public class DocManager {

    public static final Map<String, List<DocumentClass>> classDocuments = new HashMap<>();
    public static final Map<String, List<IType>> typesAssignable = new HashMap<>();
    public static final Map<String, List<DocumentClass>> classAdditions = new HashMap<>();
    public static final List<String> rawTSDoc = new ArrayList<>();
    public static final List<DocumentType> typeDocuments = new ArrayList<>();

    public static final void addAssignable(String className, IType type) {
        DocManager.typesAssignable.computeIfAbsent(className, k -> new ArrayList<>()).add(type);
    }

    public static final void addAdditions(String className, DocumentClass addition) {
        DocManager.classAdditions.computeIfAbsent(className, k -> new ArrayList<>()).add(addition);
    }

    public static void fromPath(Document document) throws IOException {
        File[] files = ProbePaths.DOCS.toFile().listFiles();
        if (files == null) {
            return;
        }
        List<File> filesSorted = Arrays
            .stream(files)
            .sorted(Comparator.comparing(File::getName))
            .collect(Collectors.toList());
        for (File f : filesSorted) {
            if (!f.getName().endsWith(".d.ts") || f.isDirectory()) {
                continue;
                //return?
            }
            BufferedReader reader = Files.newBufferedReader(f.toPath());
            if (!f.getName().startsWith("!")) {
                reader.lines().forEach(document::step);
            } else {
                reader.lines().forEach(rawTSDoc::add);
            }
            reader.close();
        }
    }

    public static void fromFiles(Document document) throws IOException {
        for (Mod mod : Platform.getMods()) {
            Path filePath = mod.getFilePath();
            if (
                // doc appearently should be readable regular file
                !Files.isRegularFile(filePath) ||
                (
                    // let's assume docs are only inside jar/zip
                    !filePath.getFileName().toString().endsWith(".jar") &&
                    !filePath.getFileName().toString().endsWith(".zip")
                )
            ) {
                continue;
            }
            ZipFile file = new ZipFile(filePath.toFile());
            ZipEntry entry = file.getEntry("probejs.documents.txt");
            if (entry == null) {
                continue;
            }
            ProbeJS.LOGGER.info("Found documents list from {}", mod.getName());
            InputStream stream = file.getInputStream(entry);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new BufferedInputStream(stream), StandardCharsets.UTF_8)
            );
            List<String> docNames = reader.lines().collect(Collectors.toList());
            for (String docName : docNames) {
                if (docName.startsWith("!")) {
                    docName = docName.substring(1);
                    int i = docName.indexOf(" ");
                    if (i != -1) {
                        if (!Platform.isModLoaded(docName.substring(0, i))) {
                            continue;
                        }
                        docName = docName.substring(i + 1);
                    }
                    ZipEntry docEntry = file.getEntry(docName);
                    if (docEntry == null) {
                        ProbeJS.LOGGER.warn("Document from file not found - {}", docName);
                        continue;
                    }
                    ProbeJS.LOGGER.info("Loading document inside jar - {}", docName);
                    InputStream docStream = file.getInputStream(docEntry);
                    BufferedReader docReader = new BufferedReader(
                        new InputStreamReader(new BufferedInputStream(docStream), StandardCharsets.UTF_8)
                    );
                    rawTSDoc.addAll(docReader.lines().collect(Collectors.toList()));
                } else {
                    ZipEntry docEntry = file.getEntry(docName);
                    if (docEntry == null) {
                        ProbeJS.LOGGER.warn("Document from file not found - {}", docName);
                        continue;
                    }
                    ProbeJS.LOGGER.info("Loading document inside jar - {}", docName);
                    InputStream docStream = file.getInputStream(docEntry);
                    BufferedReader docReader = new BufferedReader(
                        new InputStreamReader(new BufferedInputStream(docStream), StandardCharsets.UTF_8)
                    );
                    docReader.lines().forEach(document::step);
                }
            }
            file.close();
        }
    }

    public static void init() {
        Document documentState = new Document();

        rawTSDoc.clear();
        classDocuments.clear();
        classAdditions.clear();
        typeDocuments.clear();
        typesAssignable.clear();

        try {
            fromFiles(documentState);
            fromPath(documentState);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (IDocument doc : documentState.getDocument().getDocuments()) {
            if (doc instanceof DocumentClass) {
                DocumentClass classDoc = (DocumentClass) doc;
                if (!CommentUtil.isLoaded(classDoc.getComment())) {
                    continue;
                }
                DocumentComment comment = classDoc.getComment();
                if (comment != null) {
                    CommentTarget target = comment.getSpecialComment(CommentTarget.class);
                    if (target != null) {
                        classDocuments
                            .computeIfAbsent(target.getTargetName(), s -> new ArrayList<>())
                            .add(classDoc);
                        comment
                            .getSpecialComments(CommentAssign.class)
                            .stream()
                            .map(CommentAssign::getType)
                            .forEach(type -> {
                                addAssignable(target.getTargetName(), type);
                            });
                        continue;
                    }
                }
                addAdditions(classDoc.getName(), classDoc);
            } else if (doc instanceof DocumentType) {
                DocumentType typeDoc = (DocumentType) doc;
                if (CommentUtil.isLoaded(typeDoc.getComment())) {
                    typeDocuments.add(typeDoc);
                }
            } else {
                //maybe we can add more doc type
            }
        }
    }
}

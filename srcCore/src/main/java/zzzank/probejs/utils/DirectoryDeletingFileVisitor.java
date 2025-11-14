package zzzank.probejs.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author ZZZank
 */
public class DirectoryDeletingFileVisitor extends SimpleFileVisitor<Path> {
    @NotNull
    @Override
    public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    @NotNull
    @Override
    public FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc) throws IOException {
        if (exc != null) {
            throw exc;
        }
        Files.delete(dir); // delete dir AFTER deleting all files
        return FileVisitResult.CONTINUE;
    }
}

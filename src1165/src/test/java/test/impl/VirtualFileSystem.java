package test.impl;

import org.jetbrains.annotations.NotNull;

import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/// An in-memory [java.nio.file.FileSystem] for testing ConfigIO and TSFileWriter
/// without touching the real filesystem.
///
/// All files are stored in memory as byte arrays. Directories are tracked by path.
///
/// Usage:
/// ```java
/// var vfs = new VirtualFileSystem();
/// Path p = vfs.getPath("/test/file.txt");
/// Files.createDirectories(p.getParent());
/// Files.writeString(p, "hello");
/// String content = Files.readString(p);
/// ```
///
/// All [Path] objects obtained from this filesystem must come from [VirtualFileSystem.getPath].
/// Using default filesystem [Path] objects with VFS methods will throw [IllegalArgumentException].
///
/// @author ZZZank
public class VirtualFileSystem extends FileSystem {

    // region static singleton provider

    private static final VfsProvider SHARED_PROVIDER = new VfsProvider();

    // endregion

    // region instance fields & constructor

    private final VfsProvider vfsProvider;
    private final String separator = "/";
    private boolean open = true;

    public VirtualFileSystem() {
        this.vfsProvider = SHARED_PROVIDER;
    }

    // endregion
    // region FileSystem implementation

    @Override
    public FileSystemProvider provider() {
        return vfsProvider;
    }

    @Override
    public @NotNull Path getPath(@NotNull String first, String... more) {
        var sb = new StringBuilder(first);
        for (var segment : more) {
            sb.append(separator).append(segment);
        }
        return new VfsPath(this, normalize(sb.toString()));
    }

    @Override
    public String getSeparator() {
        return separator;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return List.of(new VfsPath(this, "/"));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return List.of();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Set.of("basic");
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        open = false;
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException("VFS does not support user principal lookup");
    }

    @Override
    public WatchService newWatchService() {
        throw new UnsupportedOperationException("VFS does not support watch services");
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        var colon = syntaxAndPattern.indexOf(':');
        if (colon == -1) {
            throw new IllegalArgumentException("syntax and pattern must be separated by ':'");
        }
        var syntax = syntaxAndPattern.substring(0, colon);
        var pattern = syntaxAndPattern.substring(colon + 1);
        return switch (syntax) {
            case "glob" -> {
                var regex = globToRegex(pattern);
                yield path -> path.toString().matches(regex);
            }
            case "regex" -> path -> path.toString().matches(pattern);
            default -> throw new UnsupportedOperationException("unsupported syntax: " + syntax);
        };
    }

    private static String globToRegex(String glob) {
        var sb = new StringBuilder();
        for (int i = 0; i < glob.length(); i++) {
            var c = glob.charAt(i);
            switch (c) {
                case '*', '?' -> sb.append('.');
                default -> {
                    if (Character.isLetterOrDigit(c) || c == '/' || c == '-' || c == '_') {
                        sb.append(c);
                    } else {
                        sb.append('\\').append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    // endregion
    // region storage helpers

    /** Internal storage map: normalized path string → file content bytes. */
    ConcurrentHashMap<String, byte[]> files = new ConcurrentHashMap<>();
    /** Directory tracking set. */
    Set<String> dirs = ConcurrentHashMap.newKeySet();

    static String normalize(String path) {
        // collapse repeated slashes, resolve . and ..
        var parts = new ArrayDeque<String>();
        for (var part : path.split("/")) {
            switch (part) {
                case "", "." -> {
                }
                case ".." -> {
                    if (!parts.isEmpty() && !"..".equals(parts.peekLast())) {
                        parts.removeLast();
                    } else {
                        parts.add("..");
                    }
                }
                default -> parts.add(part);
            }
        }
        var abs = path.startsWith("/");
        var joined = String.join("/", parts);
        return abs ? "/" + joined : joined;
    }

    static String key(Path path) {
        if (!(path instanceof VfsPath vp)) {
            throw new IllegalArgumentException("not a VFS path: " + path);
        }
        return vp.pathString;
    }

    // endregion
    // region test helpers

    /// Convenience: writes the given string content to a virtual file at `path`.
    public void write(Path path, String content) {
        files.put(key(path), content.getBytes());
    }

    /// Returns the text content of a virtual file, or `null` if it doesn't exist.
    public String getContent(Path path) {
        var data = files.get(key(path));
        return data != null ? new String(data) : null;
    }

    /// Returns all stored file paths (normalized strings).
    public Set<String> viewFilePaths() {
        return Collections.unmodifiableSet(files.keySet());
    }

    /// Clears all stored files and directories.
    public void clear() {
        files.clear();
        dirs.clear();
    }

    // endregion
}

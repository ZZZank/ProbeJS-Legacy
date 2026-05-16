package test.impl;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

class VfsProvider extends FileSystemProvider {

    static final String SCHEME = "memory";

    @Override
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
        return new VirtualFileSystem();
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return new VirtualFileSystem();
    }

    @Override
    public @NotNull Path getPath(URI uri) {
        if (!SCHEME.equals(uri.getScheme())) {
            throw new IllegalArgumentException("unexpected scheme: " + uri.getScheme());
        }
        // memory:///foo/bar -> /foo/bar
        return new VirtualFileSystem().getPath(uri.getPath());
    }

    @Override
    public SeekableByteChannel newByteChannel(
        Path path,
        Set<? extends OpenOption> options,
        FileAttribute<?>... attrs
    ) throws IOException {
        var vfs = vfs(path);
        var key = VirtualFileSystem.key(path);
        var read = !options.contains(StandardOpenOption.WRITE)
                   && !options.contains(StandardOpenOption.APPEND);

        if (read) {
            var data = vfs.files.get(key);
            if (data == null) {
                throw new NoSuchFileException(path.toString());
            }
            return new VfsByteChannel(data);
        }

        // writing/truncating/appending
        byte[] initial;
        if (options.contains(StandardOpenOption.APPEND)) {
            initial = vfs.files.getOrDefault(key, new byte[0]);
        } else {
            initial = new byte[0];
        }
        return new VfsByteChannel(initial, saved -> vfs.files.put(key, saved));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
        throws IOException {
        var vfs = vfs(dir);
        var prefix = VirtualFileSystem.key(dir);
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }
        var results = new ArrayList<Path>();
        for (var f : vfs.files.keySet()) {
            if (f.startsWith(prefix) && f.indexOf("/", prefix.length()) == -1) {
                var p = new VfsPath(vfs, f);
                if (filter.accept(p)) {
                    results.add(p);
                }
            }
        }
        return new DirectoryStream<>() {
            @Override
            public @NotNull Iterator<Path> iterator() {
                return results.iterator();
            }

            @Override
            public void close() {
            }
        };
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) {
        var vfs = vfs(dir);
        vfs.dirs.add(VirtualFileSystem.key(dir));
    }

    @Override
    public void delete(Path path) throws IOException {
        var vfs = vfs(path);
        var key = VirtualFileSystem.key(path);
        var removedFile = vfs.files.remove(key) != null;
        var removedDir = vfs.dirs.remove(key);
        if (!removedFile && !removedDir) {
            throw new NoSuchFileException(path.toString());
        }
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        var vfs = vfs(source);
        ensureSameVfs(vfs, target);
        var srcKey = VirtualFileSystem.key(source);
        var dstKey = VirtualFileSystem.key(target);
        var data = vfs.files.get(srcKey);
        if (data == null) {
            throw new NoSuchFileException(source.toString());
        }
        vfs.files.put(dstKey, data.clone());
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        var vfs = vfs(source);
        ensureSameVfs(vfs, target);
        var srcKey = VirtualFileSystem.key(source);
        var dstKey = VirtualFileSystem.key(target);
        var data = vfs.files.remove(srcKey);
        if (data == null) {
            throw new NoSuchFileException(source.toString());
        }
        vfs.files.put(dstKey, data);
    }

    @Override
    public boolean isSameFile(Path path1, Path path2) {
        return VirtualFileSystem.key(path1).equals(VirtualFileSystem.key(path2));
    }

    @Override
    public boolean isHidden(Path path) {
        return path.getFileName() != null && path.getFileName().toString().startsWith(".");
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        var vfs = vfs(path);
        var key = VirtualFileSystem.key(path);
        if (!vfs.files.containsKey(key) && !vfs.dirs.contains(key)) {
            throw new NoSuchFileException(path.toString());
        }
    }

    @Override
    public FileStore getFileStore(Path path) {
        throw new UnsupportedOperationException("VFS does not support FileStore");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) {
        if (type != BasicFileAttributes.class) {
            throw new UnsupportedOperationException("only BasicFileAttributes is supported, got " + type);
        }
        var vfs = vfs(path);
        var key = VirtualFileSystem.key(path);
        var exists = vfs.files.containsKey(key) || vfs.dirs.contains(key);
        var isDir = vfs.dirs.contains(key);
        return (A) new VfsFileAttributes(exists, isDir);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) {
        // minimal implementation: support "exists", "size", "isDirectory"
        var vfs = vfs(path);
        var key = VirtualFileSystem.key(path);
        var result = new LinkedHashMap<String, Object>();
        var data = vfs.files.get(key);
        result.put("exists", data != null || vfs.dirs.contains(key));
        result.put("size", data != null ? (long) data.length : 0L);
        result.put("isDirectory", vfs.dirs.contains(key));
        result.put("isRegularFile", data != null);
        return result;
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) {
        // no-op: VFS does not support persistent attributes
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return null;
    }

    // region helpers

    static VirtualFileSystem vfs(Path path) {
        var fs = path.getFileSystem();
        if (!(fs instanceof VirtualFileSystem vfs)) {
            throw new IllegalArgumentException("not a VFS path: " + path);
        }
        return vfs;
    }

    private static void ensureSameVfs(VirtualFileSystem vfs, Path p) {
        if (p.getFileSystem() != vfs) {
            throw new IllegalArgumentException("paths from different VFS: " + p);
        }
    }

    // endregion
}

package test.impl;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

class VfsPath implements Path {

    final VirtualFileSystem vfs;
    final String pathString;
    final boolean absolute;
    final String[] parts; // empty array for root "/"

    VfsPath(VirtualFileSystem vfs, String pathString) {
        this.vfs = vfs;
        this.absolute = pathString.startsWith("/");

        // split and normalize
        var raw = pathString.split("/", -1);
        var list = new ArrayList<String>();
        for (var part : raw) {
            if (!part.isEmpty()) {
                list.add(part);
            }
        }
        this.parts = list.toArray(new String[0]);
        this.pathString = absolute ? "/" + String.join("/", parts) : String.join("/", parts);
    }

    String pathString() {
        return pathString;
    }

    @Override
    public @NotNull FileSystem getFileSystem() {
        return vfs;
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public Path getRoot() {
        return absolute ? new VfsPath(vfs, "/") : null;
    }

    @Override
    public Path getFileName() {
        if (parts.length == 0) {
            return null;
        }
        return new VfsPath(vfs, parts[parts.length - 1]);
    }

    @Override
    public Path getParent() {
        if (parts.length == 0) {
            return null;
        }
        var parentParts = Arrays.copyOf(parts, parts.length - 1);

        var parentPathString = String.join("/", parentParts);
        if (absolute) {
            parentPathString = "/" + parentPathString;
        }
        return new VfsPath(vfs, parentPathString);
    }

    @Override
    public int getNameCount() {
        return parts.length;
    }

    @Override
    public @NotNull Path getName(int index) {
        return new VfsPath(vfs, parts[index]);
    }

    @Override
    public @NotNull Path subpath(int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > parts.length || beginIndex >= endIndex) {
            throw new IllegalArgumentException();
        }
        var sub = Arrays.copyOfRange(parts, beginIndex, endIndex);
        return new VfsPath(vfs, String.join("/", sub));
    }

    @Override
    public boolean startsWith(@NotNull Path other) {
        var otherParts = partsOf(other);
        if (otherParts.length > parts.length) {
            return false;
        }
        for (int i = 0; i < otherParts.length; i++) {
            if (!parts[i].equals(otherParts[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean startsWith(@NotNull String other) {
        return startsWith(new VfsPath(vfs, other));
    }

    @Override
    public boolean endsWith(@NotNull Path other) {
        var otherParts = partsOf(other);
        if (otherParts.length > parts.length) {
            return false;
        }
        var offset = parts.length - otherParts.length;
        for (int i = 0; i < otherParts.length; i++) {
            if (!parts[offset + i].equals(otherParts[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean endsWith(@NotNull String other) {
        return endsWith(new VfsPath(vfs, other));
    }

    @Override
    public @NotNull Path normalize() {
        return new VfsPath(vfs, VirtualFileSystem.normalize(pathString));
    }

    @Override
    public @NotNull Path resolve(Path other) {
        if (other.isAbsolute()) {
            return other;
        }
        if (pathString.isEmpty()) {
            // `"" + "/" + other` will create an absolute path
            return other;
        }
        return new VfsPath(vfs, pathString + "/" + other);
    }

    @Override
    public @NotNull Path resolve(@NotNull String other) {
        return resolve(new VfsPath(vfs, other));
    }

    @Override
    public @NotNull Path resolveSibling(@NotNull Path other) {
        var parent = getParent();
        return parent != null ? parent.resolve(other) : other;
    }

    @Override
    public @NotNull Path resolveSibling(@NotNull String other) {
        return resolveSibling(new VfsPath(vfs, other));
    }

    @Override
    public @NotNull Path relativize(@NotNull Path other) {
        var otherParts = partsOf(other);
        var common = 0;
        while (common < parts.length && common < otherParts.length
               && parts[common].equals(otherParts[common])) {
            common++;
        }
        var result = new ArrayList<String>();
        for (int i = common; i < parts.length; i++) {
            result.add("..");
        }
        result.addAll(Arrays.asList(otherParts).subList(common, otherParts.length));
        return new VfsPath(vfs, String.join("/", result));
    }

    @Override
    public @NotNull URI toUri() {
        try {
            return new URI(VfsProvider.SCHEME, null, "/" + pathString, null);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public @NotNull Path toAbsolutePath() {
        if (absolute) {
            return this;
        }
        return new VfsPath(vfs, "/" + pathString);
    }

    @Override
    public @NotNull Path toRealPath(LinkOption @NotNull ... options) {
        return toAbsolutePath().normalize();
    }

    @Override
    public @NotNull File toFile() {
        throw new UnsupportedOperationException("VFS path cannot be converted to File");
    }

    @Override
    public @NotNull WatchKey register(@NotNull WatchService watcher, WatchEvent.Kind<?> @NotNull [] events, WatchEvent.Modifier @NotNull ... modifiers) {
        throw new UnsupportedOperationException("VFS does not support watch services");
    }

    @Override
    public @NotNull WatchKey register(@NotNull WatchService watcher, WatchEvent.Kind<?> @NotNull ... events) {
        throw new UnsupportedOperationException("VFS does not support watch services");
    }

    @Override
    public @NotNull Iterator<Path> iterator() {
        var list = new ArrayList<Path>(parts.length);
        for (var part : parts) {
            list.add(new VfsPath(vfs, part));
        }
        return list.iterator();
    }

    @Override
    public int compareTo(Path other) {
        return pathString.compareTo(other.toString());
    }

    @Override
    public int hashCode() {
        return pathString.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VfsPath other)) {
            return false;
        }
        return vfs == other.vfs && pathString.equals(other.pathString);
    }

    @Override
    public @NotNull String toString() {
        return pathString;
    }

    private static String[] partsOf(Path p) {
        if (p instanceof VfsPath vp) {
            return vp.parts;
        }
        return p.toString().split("/");
    }
}

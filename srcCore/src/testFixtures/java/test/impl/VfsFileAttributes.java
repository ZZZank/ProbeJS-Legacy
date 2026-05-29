package test.impl;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

class VfsFileAttributes implements BasicFileAttributes {

    private final boolean exists;
    private final boolean directory;

    VfsFileAttributes(boolean exists, boolean directory) {
        this.exists = exists;
        this.directory = directory;
    }

    @Override
    public FileTime lastModifiedTime() {
        return FileTime.fromMillis(0);
    }

    @Override
    public FileTime lastAccessTime() {
        return FileTime.fromMillis(0);
    }

    @Override
    public FileTime creationTime() {
        return FileTime.fromMillis(0);
    }

    @Override
    public boolean isRegularFile() {
        return exists && !directory;
    }

    @Override
    public boolean isDirectory() {
        return directory;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Object fileKey() {
        return null;
    }
}

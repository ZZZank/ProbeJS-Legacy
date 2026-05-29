package test.impl;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

class VfsByteChannel implements SeekableByteChannel {

    private byte[] data;
    private int position;
    private boolean open = true;
    private final java.util.function.Consumer<byte[]> onClose;

    VfsByteChannel(byte[] data) {
        this(data, null);
    }

    VfsByteChannel(byte[] data, java.util.function.Consumer<byte[]> onClose) {
        this.data = data != null ? data : new byte[0];
        this.onClose = onClose;
    }

    @Override
    public synchronized int read(ByteBuffer dst) {
        if (position >= data.length) {
            return -1;
        }
        var len = Math.min(dst.remaining(), data.length - position);
        dst.put(data, position, len);
        position += len;
        return len;
    }

    @Override
    public synchronized int write(ByteBuffer src) {
        var len = src.remaining();
        ensureCapacity(position + len);
        src.get(data, position, len);
        position += len;
        return len;
    }

    private void ensureCapacity(int minCapacity) {
        if (data.length < minCapacity) {
            data = Arrays.copyOf(data, Math.max(minCapacity, data.length * 2));
        }
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
        if (newPosition < 0) {
            throw new IllegalArgumentException("negative position: " + newPosition);
        }
        this.position = (int) newPosition;
        return this;
    }

    @Override
    public long size() {
        return data.length;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
        if (size < data.length) {
            data = Arrays.copyOf(data, (int) size);
        }
        if (position > size) {
            position = (int) size;
        }
        return this;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        if (!open) {
            return;
        }
        open = false;
        if (onClose != null) {
            // data array may be larger than actual written content due to ensureCapacity growth strategy;
            // trim to position to avoid trailing null bytes corrupting the stored content
            onClose.accept(position < data.length ? Arrays.copyOf(data, position) : data);
        }
    }
}

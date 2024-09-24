package me.chriss99.worldmanagement;

import java.nio.ByteBuffer;

public interface BufferInterpreter<T> {
    T getFromByteBuffer(ByteBuffer buffer);
    void putInByteBuffer(ByteBuffer buffer, T value);
    Class<T> typeClass();
    int byteSize();
}

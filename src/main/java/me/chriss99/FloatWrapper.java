package me.chriss99;

import me.chriss99.worldmanagement.BufferInterpreter;

import java.nio.ByteBuffer;

public class FloatWrapper implements BufferInterpreter<Float> {
    @Override
    public Float getFromByteBuffer(ByteBuffer buffer) {
        return buffer.getFloat();
    }

    @Override
    public void putInByteBuffer(ByteBuffer buffer, Float value) {
        buffer.putFloat(value);
    }

    @Override
    public Class<Float> typeClass() {
        return Float.class;
    }

    @Override
    public int byteSize() {
        return Float.BYTES;
    }
}

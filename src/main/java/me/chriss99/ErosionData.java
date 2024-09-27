package me.chriss99;

import org.joml.Vector4f;

import java.nio.ByteBuffer;

public class ErosionData {
    public float terrain;
    public float water;
    public float sediment;
    public float hardness;

    public Vector4f waterOutflowPipes;
    public Vector4f sedimentOutflowPipes;

    public Vector4f thermalOutflowPipes1;
    public Vector4f thermalOutflowPipes2;

    public ErosionData(float terrain, float water, float sediment, float hardness, Vector4f waterOutflowPipes, Vector4f sedimentOutflowPipes, Vector4f thermalOutflowPipes1, Vector4f thermalOutflowPipes2) {
        this.terrain = terrain;
        this.water = water;
        this.sediment = sediment;
        this.hardness = hardness;

        this.waterOutflowPipes = waterOutflowPipes;
        this.sedimentOutflowPipes = sedimentOutflowPipes;

        this.thermalOutflowPipes1 = thermalOutflowPipes1;
        this.thermalOutflowPipes2 = thermalOutflowPipes2;
    }



    public static final BufferInterpreter bufferInterpreter = new BufferInterpreter();

    public static class BufferInterpreter implements me.chriss99.worldmanagement.BufferInterpreter<ErosionData> {
        @Override
        public ErosionData getFromByteBuffer(ByteBuffer buffer) {
            return new ErosionData(
                    buffer.getFloat(),
                    buffer.getFloat(),
                    buffer.getFloat(),
                    buffer.getFloat(),

                    getVector(buffer),
                    getVector(buffer),

                    getVector(buffer),
                    getVector(buffer)
            );
        }

        private static Vector4f getVector(ByteBuffer buffer) {
            return new Vector4f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
        }

        @Override
        public void putInByteBuffer(ByteBuffer buffer, ErosionData value) {
            buffer.putFloat(value.terrain);
            buffer.putFloat(value.water);
            buffer.putFloat(value.sediment);
            buffer.putFloat(value.hardness);

            putVector(buffer, value.waterOutflowPipes);
            putVector(buffer, value.sedimentOutflowPipes);

            putVector(buffer, value.thermalOutflowPipes1);
            putVector(buffer, value.thermalOutflowPipes2);
        }

        private static void putVector(ByteBuffer buffer, Vector4f vector) {
            buffer.putFloat(vector.x);
            buffer.putFloat(vector.y);
            buffer.putFloat(vector.z);
            buffer.putFloat(vector.w);
        }

        @Override
        public Class<ErosionData> typeClass() {
            return ErosionData.class;
        }

        @Override
        public int byteSize() {
            return 80; //4*(4 + 4*4)
        }
    }
}

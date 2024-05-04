package me.chriss99;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CameraMatrix {
    float aspectRatio = 1;
    float FOV = 90;
    float zNear = 0;
    float zFar = 20000;

    Vector3f position = new Vector3f();
    float yaw = 0;
    float pitch = 0;
    float roll = 0;

    public Matrix4f generateMatrix() {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.m30(-position.x);
        viewMatrix.m31(-position.y);
        viewMatrix.m32(-position.z);

        Matrix4f yawMatrix = new Matrix4f();
        yawMatrix.m00((float) Math.cos(yaw));
        yawMatrix.m02((float) -Math.sin(yaw));
        yawMatrix.m20((float) Math.sin(yaw));
        yawMatrix.m22((float) Math.cos(yaw));

        Matrix4f pitchMatrix = new Matrix4f();
        pitchMatrix.m11((float) Math.cos(pitch));
        pitchMatrix.m12((float) Math.sin(pitch));
        pitchMatrix.m21((float) -Math.sin(pitch));
        pitchMatrix.m22((float) Math.cos(pitch));

        Matrix4f rollMatrix = new Matrix4f();
        rollMatrix.m00((float) Math.cos(roll));
        rollMatrix.m01((float) Math.sin(roll));
        rollMatrix.m10((float) -Math.sin(roll));
        rollMatrix.m11((float) Math.cos(roll));

        float f = (float) (1 / Math.tan(Math.toRadians(FOV)/2));
        float DNF = 1/(zNear-zFar);

        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.m00(aspectRatio * f);
        projectionMatrix.m11(f);
        projectionMatrix.m22(-2 * DNF);
        projectionMatrix.m32(1 + (2*zFar) * DNF);
        projectionMatrix.m23(1);
        projectionMatrix.m33(0);

        return projectionMatrix.mul(rollMatrix.mul(pitchMatrix.mul(yawMatrix.mul(viewMatrix))));
    }
}

package me.chriss99

import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class CameraMatrix {
    var aspectRatio: Float = 1f
    var FOV: Float = 90f
    var zNear: Float = 0f
    var zFar: Float = 20000f

    var position: Vector3f = Vector3f()
    var yaw: Float = 0f
    var pitch: Float = 0f
    var roll: Float = 0f

    fun generateMatrix(): Matrix4f {
        val viewMatrix: Matrix4f = Matrix4f()
        viewMatrix.m30(-position.x)
        viewMatrix.m31(-position.y)
        viewMatrix.m32(-position.z)

        val yawMatrix: Matrix4f = Matrix4f()
        yawMatrix.m00(cos(yaw.toDouble()).toFloat())
        yawMatrix.m02(-sin(yaw.toDouble()).toFloat())
        yawMatrix.m20(sin(yaw.toDouble()).toFloat())
        yawMatrix.m22(cos(yaw.toDouble()).toFloat())

        val pitchMatrix: Matrix4f = Matrix4f()
        pitchMatrix.m11(cos(pitch.toDouble()).toFloat())
        pitchMatrix.m12(sin(pitch.toDouble()).toFloat())
        pitchMatrix.m21(-sin(pitch.toDouble()).toFloat())
        pitchMatrix.m22(cos(pitch.toDouble()).toFloat())

        val rollMatrix: Matrix4f = Matrix4f()
        rollMatrix.m00(cos(roll.toDouble()).toFloat())
        rollMatrix.m01(sin(roll.toDouble()).toFloat())
        rollMatrix.m10(-sin(roll.toDouble()).toFloat())
        rollMatrix.m11(cos(roll.toDouble()).toFloat())

        val f = (1 / tan(Math.toRadians(FOV.toDouble()) / 2)).toFloat()
        val DNF = 1 / (zNear - zFar)

        val projectionMatrix: Matrix4f = Matrix4f()
        projectionMatrix.m00(aspectRatio * f)
        projectionMatrix.m11(f)
        projectionMatrix.m22(-2 * DNF)
        projectionMatrix.m32(1 + (2 * zFar) * DNF)
        projectionMatrix.m23(1f)
        projectionMatrix.m33(0f)

        return projectionMatrix.mul(rollMatrix.mul(pitchMatrix.mul(yawMatrix.mul(viewMatrix))))
    }
}
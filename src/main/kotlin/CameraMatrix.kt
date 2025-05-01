package me.chriss99

import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class CameraMatrix(
    var aspectRatio: Float = 1f,
    var FOV: Float = 90f,
    var zNear: Float = 0f,
    var zFar: Float = 20000f,

    var position: Vec3 = Vec3(),
    var yaw: Float = 0f,
    var pitch: Float = 0f,
    var roll: Float = 0f
) {
    val viewMatrix: Mat4
        get() = Mat4().let {
            it.d0 = -position.x
            it.d1 = -position.y
            it.d2 = -position.z
            it
        }
    val yawMatrix: Mat4
        get() = Mat4().let {
            it.a0 = cos(yaw)
            it.a2 = -sin(yaw)
            it.c0 = sin(yaw)
            it.c2 = cos(yaw)
            it
        }
    val pitchMatrix: Mat4
        get() = Mat4().let {
            it.b1 = cos(pitch)
            it.b2 = sin(pitch)
            it.c1 = -sin(pitch)
            it.c2 = cos(pitch)
            it
        }
    val rollMatrix: Mat4
        get() = Mat4().let {
            it.a0 = cos(roll)
            it.a1 = sin(roll)
            it.b0 = -sin(roll)
            it.b1 = cos(roll)
            it
        }
    val projectionMatrix: Mat4
        get() = Mat4().let {
            val f = (1 / tan(Math.toRadians(FOV.toDouble()) / 2)).toFloat()
            val DNF = 1 / (zNear - zFar)

            it.a0 = aspectRatio * f
            it.b1 = f
            it.c2 = -2 * DNF
            it.d2 = 1 + (2 * zFar) * DNF
            it.c3 = 1f
            it.d3 = 0f
            it
        }

    fun generateMatrix(): Mat4 = projectionMatrix * rollMatrix * pitchMatrix * yawMatrix * viewMatrix
}
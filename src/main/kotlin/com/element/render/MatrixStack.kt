package com.element.render

import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.ArrayDeque

/**
 * A simple matrix stack implementation.
 */
class
MatrixStack {

    /**
     * Represents a single pose on the stack.
     */
    data class Pose(
        val model: Matrix4f,
        val normal: Matrix3f
    )

    private val stack = ArrayDeque<Pose>()
    private var current = Pose(Matrix4f(), Matrix3f())

    /**
     * Pushes a copy of the current pose onto the stack.
     */
    fun push() = apply {
        stack.push(Pose(Matrix4f(current.model), Matrix3f(current.normal)))
    }

    /**
     * Pops the last pose off the stack.
     */
    fun pop() = apply {
        if (stack.isEmpty()) {
            error("MatrixStack underflow: pop() called without a matching push()")
        }
        current = stack.pop()
    }

    /**
     * Translates the current model matrix by (x, y, z).
     */
    fun translate(x: Float, y: Float, z: Float) = apply {
        current.model.translate(x, y, z)
    }

    /**
     * Translates the current model matrix by a vector.
     */
    fun translate(vec: Vector3f) = translate(vec.x, vec.y, vec.z)

    /**
     * Scales the current matrix by (x, y, z).
     * Updates the normal matrix using the inverse transpose of the upper-left 3x3.
     */
    fun scale(x: Float, y: Float, z: Float) = apply {
        current.model.scale(x, y, z)
        updateNormalMatrix()
    }

    /**
     * Uniformly scales the current matrix.
     */
    fun scale(s: Float) = scale(s, s, s)

    /**
     * Rotates the current matrix using a quaternion.
     */
    fun rotate(quat: Quaternionf) = apply {
        current.model.rotate(quat)
        updateNormalMatrix()
    }

    /**
     * Rotates the current matrix around the X axis.
     */
    fun rotateX(radians: Float) = apply {
        current.model.rotateX(radians)
        updateNormalMatrix()
    }

    /**
     * Rotates the current matrix around the Y axis.
     */
    fun rotateY(radians: Float) = apply {
        current.model.rotateY(radians)
        updateNormalMatrix()
    }

    /**
     * Rotates the current matrix around the Z axis.
     */
    fun rotateZ(radians: Float) = apply {
        current.model.rotateZ(radians)
        updateNormalMatrix()
    }

    /**
     * Rotates the current matrix around an arbitrary axis.
     */
    fun rotate(angle: Float, x: Float, y: Float, z: Float) = apply {
        val quat = Quaternionf().fromAxisAngleRad(x, y, z, angle)
        rotate(quat)
    }

    /**
     * Resets the current pose to identity.
     */
    fun identity() = apply {
        current.model.identity()
        current.normal.identity()
    }

    /**
     * Returns the current model matrix (mutable reference).
     */
    fun model(): Matrix4f = current.model

    /**
     * Returns the current normal matrix (mutable reference).
     */
    fun normal(): Matrix3f = current.normal

    /**
     * Returns a copy of the current pose.
     */
    fun pose(): Pose = Pose(Matrix4f(current.model), Matrix3f(current.normal))

    /**
     * Returns the current stack depth.
     */
    fun size(): Int = stack.size

    /**
     * Updates the normal matrix from the current model matrix.
     */
    private fun updateNormalMatrix() {
        current.model.normal(current.normal)
    }
}

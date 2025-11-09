package com.element.render

import org.joml.Matrix4f
import org.joml.Vector3f

object RenderSystem {
    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f()
    private val viewProjectionMatrix = Matrix4f()
    private var dirty = true

    fun setProjection(matrix: Matrix4f) {
        projectionMatrix.set(matrix)
        dirty = true
    }

    /**
     * Sets a perspective projection.
     *
     * @param fov Field of view in radians
     * @param aspect Aspect ratio (width / height)
     * @param near Near plane distance
     * @param far Far plane distance
     */
    fun perspective(fov: Float, aspect: Float, near: Float, far: Float) {
        projectionMatrix.identity().perspective(fov, aspect, near, far)
        dirty = true
    }

    /**
     * Sets an orthographic projection.
     *
     * @param left Left plane
     * @param right Right plane
     * @param bottom Bottom plane
     * @param top Top plane
     * @param near Near plane
     * @param far Far plane
     */
    fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        projectionMatrix.identity().ortho(left, right, bottom, top, near, far)
        dirty = true
    }

    /**
     * Sets an orthographic projection.
     *
     * @param left Left plane
     * @param right Right plane
     * @param bottom Bottom plane
     * @param top Top plane
     * @param near Near plane
     * @param far Far plane
     */
    fun ortho(left: Double, right: Double, bottom: Double, top: Double, near: Double, far: Double) {
        projectionMatrix.identity().ortho(left.toFloat(), right.toFloat(), bottom.toFloat(), top.toFloat(), near.toFloat(), far.toFloat())
        dirty = true
    }

    fun setView(matrix: Matrix4f) {
        viewMatrix.set(matrix)
        dirty = true
    }

    fun lookAt(eye: Vector3f, center: Vector3f, up: Vector3f = Vector3f(0f, 1f, 0f)) {
        viewMatrix.identity().lookAt(eye, center, up)
        dirty = true
    }

    fun reset() {
        projectionMatrix.identity()
        viewMatrix.identity()
        dirty = true
    }

    fun projection(): Matrix4f = projectionMatrix

    fun view(): Matrix4f = viewMatrix

    fun viewProjection(): Matrix4f {
        if (dirty) {
            viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix)
            dirty = false
        }
        return viewProjectionMatrix
    }
}

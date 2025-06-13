package org.lwjgl

import org.lwjgl.glfw.GLFW

object Sys {
    const val TIMER_RESOLUTION: Long = 1000L

    @JvmStatic
    fun getTime(): Long {
        return (GLFW.glfwGetTime() * TIMER_RESOLUTION).toLong()
    }

    @JvmStatic
    fun getTimerResolution(): Long = TIMER_RESOLUTION

    @JvmStatic
    fun getVersion(): String {
        return "3.3.6"
    }
}

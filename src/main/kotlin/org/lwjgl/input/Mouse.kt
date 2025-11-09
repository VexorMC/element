package org.lwjgl.input

import org.lwjgl.opengl.Display
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWMouseButtonCallback
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.system.MemoryUtil
import java.util.LinkedList
import java.util.Queue

object Mouse {
    const val BUTTON_LEFT = 0
    const val BUTTON_RIGHT = 1
    const val BUTTON_MIDDLE = 2

    private var mouseButtonCallback: GLFWMouseButtonCallback? = null
    private var cursorPosCallback: GLFWCursorPosCallback? = null

    private val eventQueue: Queue<MouseEvent> = LinkedList()
    private var currentEvent: MouseEvent? = null

    private var lastX: Double = 0.0
    private var lastY: Double = 0.0
    private var firstMouseInput = true

    private var xDelta = 0
    private var yDelta = 0

    private val buttonStates: MutableMap<Int, Boolean> = mutableMapOf()

    private data class MouseEvent(
        val button: Int,
        val state: Boolean,
        val x: Int,
        val y: Int,
        val dx: Int,
        val dy: Int,
        val dwheel: Int,
        val timestamp: Long
    )

    fun create() {
        if (!isCreated()) {
            val window = Display.getWindowHandle()
            if (window != MemoryUtil.NULL) {
                mouseButtonCallback = object : GLFWMouseButtonCallback() {
                    override fun invoke(window: Long, button: Int, action: Int, mods: Int) {
                        val lwjgl2Button = when (button) {
                            GLFW.GLFW_MOUSE_BUTTON_LEFT -> BUTTON_LEFT
                            GLFW.GLFW_MOUSE_BUTTON_RIGHT -> BUTTON_RIGHT
                            GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> BUTTON_MIDDLE
                            else -> -1 // Not mapped
                        }
                        if (lwjgl2Button != -1) {
                            val isPressed = action != GLFW.GLFW_RELEASE
                            buttonStates[button] = isPressed

                            val x = getX()
                            val y = getY()
                            // For click events, dx/dy might be 0 or small, as it's the state at click time
                            eventQueue.offer(MouseEvent(lwjgl2Button, isPressed, x, y, 0, 0, 0, System.currentTimeMillis()))
                        }
                    }
                }
                GLFW.glfwSetMouseButtonCallback(window, mouseButtonCallback)

                cursorPosCallback = object : GLFWCursorPosCallback() {
                    override fun invoke(window: Long, xpos: Double, ypos: Double) {
                        if (firstMouseInput) {
                            lastX = xpos
                            lastY = ypos
                            firstMouseInput = false
                        }

                        val dx = (xpos - lastX).toInt()
                        val dy = (ypos - lastY).toInt()


                        xDelta += dx
                        yDelta += dy

                        val currentButton = -1
                        eventQueue.offer(MouseEvent(currentButton, false, xpos.toInt(), Display.getHeight() - ypos.toInt(), dx, dy, 0, System.currentTimeMillis()))

                        lastX = xpos
                        lastY = ypos
                    }
                }
                GLFW.glfwSetCursorPosCallback(window, cursorPosCallback)

                // Get initial cursor position
                val xBuffer = MemoryUtil.memAllocDouble(1)
                val yBuffer = MemoryUtil.memAllocDouble(1)
                GLFW.glfwGetCursorPos(window, xBuffer, yBuffer)
                lastX = xBuffer.get(0)
                lastY = yBuffer.get(0)
                MemoryUtil.memFree(xBuffer)
                MemoryUtil.memFree(yBuffer)

            } else {
                System.err.println("Mouse.create() called before Display.create().")
            }
        }
    }

    fun destroy() {
        if (isCreated()) {
            val window = Display.getWindowHandle()
            if (window != MemoryUtil.NULL) {
                GLFW.glfwSetMouseButtonCallback(window, null)
                GLFW.glfwSetCursorPosCallback(window, null)
            }
            mouseButtonCallback?.free()
            mouseButtonCallback = null
            cursorPosCallback?.free()
            cursorPosCallback = null
            eventQueue.clear()
            buttonStates.clear()
            currentEvent = null
        }
    }

    @JvmStatic
    fun isCreated(): Boolean = mouseButtonCallback != null && cursorPosCallback != null && Display.isCreated()

    @JvmStatic
    fun next(): Boolean {
        currentEvent = eventQueue.poll()
        return currentEvent != null
    }

    @JvmStatic
    fun getEventButton(): Int = currentEvent?.button ?: -1

    @JvmStatic
    fun getEventButtonState(): Boolean = currentEvent?.state ?: false

    @JvmStatic
    fun getEventX(): Int = currentEvent?.x ?: 0

    @JvmStatic
    fun getEventY(): Int = currentEvent?.y ?: 0

    @JvmStatic
    fun getEventDWheel(): Int = currentEvent?.dwheel ?: 0

    @JvmStatic
    fun getX(): Int {
        val window = Display.getWindowHandle()
        if (window == MemoryUtil.NULL) return 0
        val xBuffer = MemoryUtil.memAllocDouble(1)
        GLFW.glfwGetCursorPos(window, xBuffer, null)
        val x = xBuffer.get(0).toInt()
        MemoryUtil.memFree(xBuffer)
        return x
    }

    @JvmStatic
    fun getY(): Int {
        val window = Display.getWindowHandle()
        if (window == MemoryUtil.NULL) return 0
        val yBuffer = MemoryUtil.memAllocDouble(1)
        GLFW.glfwGetCursorPos(window, null, yBuffer)
        val y = Display.getHeight() - yBuffer.get(0).toInt()
        MemoryUtil.memFree(yBuffer)
        return y
    }

    @JvmStatic
    fun isButtonDown(button: Int): Boolean {
        val glfwButton = when (button) {
            BUTTON_LEFT -> GLFW.GLFW_MOUSE_BUTTON_LEFT
            BUTTON_RIGHT -> GLFW.GLFW_MOUSE_BUTTON_RIGHT
            BUTTON_MIDDLE -> GLFW.GLFW_MOUSE_BUTTON_MIDDLE
            else -> return false
        }
        val window = Display.getWindowHandle()
        if (window == MemoryUtil.NULL) return false
        return GLFW.glfwGetMouseButton(window, glfwButton) == GLFW.GLFW_PRESS
    }

    @JvmStatic
    fun setGrabbed(grabbed: Boolean) {
        val window = Display.getWindowHandle()
        if (window != MemoryUtil.NULL) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                if (grabbed) GLFW.GLFW_CURSOR_DISABLED else GLFW.GLFW_CURSOR_NORMAL)
        }
    }

    internal fun handleScrollInput(xoffset: Double, yoffset: Double) {
        val dwheel = (yoffset * 120).toInt()
        if (dwheel != 0) {
            eventQueue.offer(MouseEvent(-1, false, getX(), getY(), 0, 0, dwheel, System.currentTimeMillis()))
        }
    }

    @JvmStatic
    fun setCursorPosition(newX: Int, newY: Int) {
        val window = Display.getWindowHandle()
        if (window != MemoryUtil.NULL) {
            GLFW.glfwSetCursorPos(window, newX.toDouble(), (Display.getHeight() - newY).toDouble())
            lastX = newX.toDouble()
            lastY = (Display.getHeight() - newY).toDouble()
        }
    }

    @JvmStatic
    fun isInsideWindow(): Boolean {
        val window = Display.getWindowHandle()
        if (window != MemoryUtil.NULL) {
            val x = getX()
            val y = getY()
            return x >= 0 && x < Display.getWidth() && y >= 0 && y < Display.getHeight()
        }
        return false
    }

    @JvmStatic
    fun getDX(): Int {
        val dx = xDelta
        xDelta = 0
        return dx
    }

    @JvmStatic
    fun getDY(): Int {
        val dy = yDelta
        yDelta = 0
        return -dy
    }
}
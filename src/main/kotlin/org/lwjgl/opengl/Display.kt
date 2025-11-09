package org.lwjgl.opengl

import org.lwjgl.BufferUtils
import org.lwjgl.LWJGLException
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwSetWindowTitle
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.glfw.GLFWWindowSizeCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.nio.Buffer
import java.nio.ByteBuffer
import kotlin.math.sqrt

object Display {
    private var window: Long = MemoryUtil.NULL

    private var currentMode: DisplayMode? = null
    private var wasResizedFlag: Boolean = false
    private var windowSizeCallback: GLFWWindowSizeCallback? = null
    private var errorCallback: GLFWErrorCallback? = null
    private var cachedIcons: Array<ByteBuffer>? = null

    private var title: String = "Application"

    @JvmStatic
    @Throws(LWJGLException::class)
    fun create() {
        if (isCreated()) {
            System.err.println("Display is already created.")
            return
        }

        errorCallback = GLFWErrorCallback.createPrint(System.err).set()

        if (!GLFW.glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        val vidMode: GLFWVidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
            ?: throw IllegalStateException("Failed to get primary monitor video mode.")

        val initialWidth = currentMode?.getWidth() ?: -1
        val initialHeight = currentMode?.getHeight() ?: -1

        window = GLFW.glfwCreateWindow(initialWidth, initialHeight, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        GLFW.glfwSetWindowPos(
            window,
            (vidMode.width() - initialWidth) / 2,
            (vidMode.height() - initialHeight) / 2
        )

        windowSizeCallback = object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                wasResizedFlag = true
                currentMode = DisplayMode(width, height, getActualDisplayMode()?.getBitsPerPixel() ?: 32, getActualDisplayMode()?.getFrequency() ?: 60)
            }
        }
        GLFW.glfwSetWindowSizeCallback(window, windowSizeCallback)

        GLFW.glfwSetScrollCallback(window) { _, xoffset, yoffset ->
            Mouse.handleScrollInput(xoffset, yoffset)
        }

        GLFW.glfwSetCharCallback(window) { _, codepoint ->
            Keyboard.handleCharInput(codepoint)
        }

        GLFW.glfwMakeContextCurrent(window)

        GLFW.glfwShowWindow(window)

        cachedIcons?.let { setIcon(it) }

        GL.createCapabilities()

        Keyboard.create()
        Mouse.create()

        currentMode = DisplayMode(initialWidth, initialHeight, 32, vidMode.refreshRate())
    }

    @JvmStatic
    fun destroy() {
        if (!isCreated()) return

        Keyboard.destroy()
        Mouse.destroy()

        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)

        GLFW.glfwTerminate()
        window = MemoryUtil.NULL
        currentMode = null
        wasResizedFlag = false
    }

    @JvmStatic
    fun isCreated(): Boolean = window != MemoryUtil.NULL

    @JvmStatic
    fun update() {
        if (!isCreated()) return
        GLFW.glfwSwapBuffers(window)
        GLFW.glfwPollEvents()
    }

    @JvmStatic
    fun setTitle(title: String) {
        this.title = title

        if (!isCreated()) return
        glfwSetWindowTitle(window, title)
    }

    @JvmStatic
    fun setResizable(resizable: Boolean) {
        if (!isCreated()) return
        GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_RESIZABLE, if (resizable) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
    }

    @JvmStatic
    fun wasResized(): Boolean {
        val temp = wasResizedFlag
        wasResizedFlag = false
        return temp
    }

    @JvmStatic
    fun getWidth(): Int {
        if (!isCreated()) return 0
        val widthBuffer = MemoryStack.stackPush().mallocInt(1)
        val heightBuffer = MemoryStack.stackPush().mallocInt(1)
        GLFW.glfwGetWindowSize(window, widthBuffer, heightBuffer)
        val width = widthBuffer.get(0)
        MemoryStack.stackPop()
        MemoryStack.stackPop()
        return width
    }

    @JvmStatic
    fun getHeight(): Int {
        if (!isCreated()) return 0
        val widthBuffer = MemoryStack.stackPush().mallocInt(1)
        val heightBuffer = MemoryStack.stackPush().mallocInt(1)
        GLFW.glfwGetWindowSize(window, widthBuffer, heightBuffer)
        val height = heightBuffer.get(0)
        MemoryStack.stackPop()
        MemoryStack.stackPop()
        return height
    }

    @JvmStatic
    fun getActualDisplayMode(): DisplayMode? {
        if (!isCreated()) return null
        currentMode = DisplayMode(getWidth(), getHeight(), currentMode?.getBitsPerPixel() ?: 32, currentMode?.getFrequency() ?: 60)
        return currentMode
    }

    @JvmStatic
    fun setVSyncEnabled(vsync: Boolean) {
        if (!isCreated()) return
        GLFW.glfwSwapInterval(if (vsync) 1 else 0)
    }

    @JvmStatic
    fun isCloseRequested(): Boolean {
        if (!isCreated()) return false
        return GLFW.glfwWindowShouldClose(window)
    }

    private fun getCurrentVideoMode(): GLFWVidMode? {
        return GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
    }

    @JvmStatic
    fun setFullscreen(fullscreen: Boolean) {
        if (!isCreated()) return

        val monitor = if (fullscreen) GLFW.glfwGetPrimaryMonitor() else MemoryUtil.NULL
        val mode = getCurrentVideoMode() ?: return
        val currentWidth = getWidth()
        val currentHeight = getHeight()

        GLFW.glfwSetWindowMonitor(
            window,
            monitor,
            0, 0,
            currentWidth,
            currentHeight,
            mode.refreshRate()
        )
    }

    @JvmStatic
    @Throws(LWJGLException::class)
    fun setDisplayMode(mode: DisplayMode) {
        if (!isCreated()) {
            currentMode = mode
            return
        }

        GLFW.glfwSetWindowSize(window, mode.getWidth(), mode.getHeight())
        currentMode = mode
    }

    @JvmStatic
    fun getDisplayMode(): DisplayMode = currentMode ?: getDesktopDisplayMode()

    @JvmStatic
    fun setIcon(icons: Array<ByteBuffer>) {
        if (!isCreated()) {
            this.cachedIcons = icons.map { cloneByteBuffer(it) }.toTypedArray()
            return
        }

        MemoryStack.stackPush().use { stack ->
            val images = GLFWImage.malloc(icons.size, stack)
            icons.forEachIndexed { index, iconData ->
                val dimension = sqrt((iconData.limit() / 4).toDouble()).toInt()
                val image = GLFWImage.malloc(stack)
                image.set(dimension, dimension, iconData)
                images.put(index, image)
            }
            images.flip()
            GLFW.glfwSetWindowIcon(window, images)
        }
    }

    @JvmStatic
    fun getAvailableDisplayModes(): Array<DisplayMode> {
        val monitor = GLFW.glfwGetPrimaryMonitor()
        val modes = GLFW.glfwGetVideoModes(monitor) ?: return emptyArray()

        return modes.map { mode ->
            DisplayMode(
                mode.width(),
                mode.height(),
                mode.redBits() + mode.greenBits() + mode.blueBits(),
                mode.refreshRate()
            )
        }.toTypedArray()
    }

    /**
     * Gets the desktop display mode
     */
    @JvmStatic
    fun getDesktopDisplayMode(): DisplayMode {
        val monitor = GLFW.glfwGetPrimaryMonitor()
        val mode = GLFW.glfwGetVideoMode(monitor) ?: throw IllegalStateException("Could not get desktop display mode")
        return DisplayMode(
            mode.width(),
            mode.height(),
            mode.redBits() + mode.greenBits() + mode.blueBits(),
            mode.refreshRate()
        )
    }

    private fun cloneByteBuffer(original: ByteBuffer): ByteBuffer {
        val clone = BufferUtils.createByteBuffer(original.capacity())
        val oldPosition = original.position()
        clone.put(original)
        (original as Buffer).position(oldPosition)
        (clone as Buffer).flip()
        return clone
    }

    /**
     * Synchronizes the display to run at a specified frames per second
     */
    @JvmStatic
    fun sync(fps: Int) {
        if (fps <= 0) return
        GLFW.glfwSwapInterval(if (fps == 60) 1 else 0)
    }

    /**
     * Returns true if the window is active (focused)
     */
    @JvmStatic
    fun isActive(): Boolean {
        return if (!isCreated()) false else GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE
    }

    /**
     * Returns the current GLFW window handle.
     */
    @JvmStatic
    fun getWindowHandle() = window
}

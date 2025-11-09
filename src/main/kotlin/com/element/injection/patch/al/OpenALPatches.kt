package com.element.injection.patch.al

import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.alcCloseDevice
import org.lwjgl.openal.ALC10.alcCreateContext
import org.lwjgl.openal.ALC10.alcDestroyContext
import org.lwjgl.openal.ALC10.alcMakeContextCurrent
import org.lwjgl.openal.ALC10.alcOpenDevice
import org.lwjgl.system.MemoryUtil
import java.nio.IntBuffer

@Suppress("unused")
object OpenALPatches {
    private var openDevice: Long = MemoryUtil.NULL
    private var openContext: Long = MemoryUtil.NULL

    @JvmStatic
    fun create() {
        val device = alcOpenDevice(null as String?)
        if (device == MemoryUtil.NULL) throw IllegalStateException("Unable to open audio device")

        openDevice = device
        val capabilities = ALC.createCapabilities(device)

        openContext = alcCreateContext(device, null as IntBuffer?)

        if (openContext == MemoryUtil.NULL) throw IllegalStateException("Failed to create audio context")

        alcMakeContextCurrent(openContext)
        AL.createCapabilities(capabilities)
    }

    @JvmStatic
    fun isCreated() = openContext != MemoryUtil.NULL

    @JvmStatic
    fun destroy() {
        if (openContext != MemoryUtil.NULL) {
            alcMakeContextCurrent(MemoryUtil.NULL)
            alcDestroyContext(openContext)
            openContext = MemoryUtil.NULL
        }

        if (openDevice != MemoryUtil.NULL) {
            alcCloseDevice(openDevice)
            openDevice = MemoryUtil.NULL
        }
    }
}
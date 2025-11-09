package com.element.utility

object Utility {
    @JvmStatic
    fun getGLErrorString(error: Int): String {
        return when (error) {
            0x0000 -> "GL_NO_ERROR"
            0x0500 -> "GL_INVALID_ENUM"
            0x0501 -> "GL_INVALID_VALUE"
            0x0502 -> "GL_INVALID_OPERATION"
            0x0503 -> "GL_STACK_OVERFLOW"
            0x0504 -> "GL_STACK_UNDERFLOW"
            0x0505 -> "GL_OUT_OF_MEMORY"
            0x0506 -> "GL_INVALID_FRAMEBUFFER_OPERATION"
            0x0507 -> "GL_CONTEXT_LOST"
            0x8031 -> "GL_TABLE_TOO_LARGE"
            else -> "Unknown OpenGL Error: 0x${error.toString(16)}"
        }
    }
}
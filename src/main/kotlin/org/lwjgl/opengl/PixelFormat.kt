package org.lwjgl.opengl

/**
 * This class describes pixel format properties for an OpenGL context.
 * Instances of this class are used to indicate minimum required properties.
 *
 * Example:
 * ```kotlin
 * val pf = PixelFormat().withDepthBits(24).withSamples(4).withSRGB(true)
 * ```
 *
 * WARNING: Some pixel formats may cause issues on buggy drivers.
 * For example, on Windows, setting `samples != 0` may enable ARB pixel format selection, which can trigger crashes.
 */
data class PixelFormat(
    val bpp: Int = 0,
    val alpha: Int = 0,
    val depth: Int = 8,
    val stencil: Int = 0,
    val samples: Int = 0,
    val colorSamples: Int = 0,
    val numAuxBuffers: Int = 0,
    val accumBpp: Int = 0,
    val accumAlpha: Int = 0,
    val stereo: Boolean = false,
    val floatingPoint: Boolean = false,
    val floatingPointPacked: Boolean = false,
    val sRGB: Boolean = false
) {

    fun withBitsPerPixel(bpp: Int): PixelFormat {
        require(bpp >= 0) { "Invalid number of bits per pixel specified: $bpp" }
        return copy(bpp = bpp)
    }

    fun withAlphaBits(alpha: Int): PixelFormat {
        require(alpha >= 0) { "Invalid number of alpha bits specified: $alpha" }
        return copy(alpha = alpha)
    }

    fun withDepthBits(depth: Int): PixelFormat {
        require(depth >= 0) { "Invalid number of depth bits specified: $depth" }
        return copy(depth = depth)
    }

    fun withStencilBits(stencil: Int): PixelFormat {
        require(stencil >= 0) { "Invalid number of stencil bits specified: $stencil" }
        return copy(stencil = stencil)
    }

    fun withSamples(samples: Int): PixelFormat {
        require(samples >= 0) { "Invalid number of samples specified: $samples" }
        return copy(samples = samples)
    }

    fun withCoverageSamples(colorSamples: Int): PixelFormat = withCoverageSamples(colorSamples, samples)

    fun withCoverageSamples(colorSamples: Int, coverageSamples: Int): PixelFormat {
        require(coverageSamples >= 0 && colorSamples >= 0 && (coverageSamples != 0 || colorSamples == 0) && coverageSamples >= colorSamples) {
            "Invalid number of coverage samples specified: $coverageSamples - $colorSamples"
        }
        return copy(samples = coverageSamples, colorSamples = colorSamples)
    }

    fun withAuxBuffers(numAuxBuffers: Int): PixelFormat {
        require(numAuxBuffers >= 0) { "Invalid number of auxiliary buffers specified: $numAuxBuffers" }
        return copy(numAuxBuffers = numAuxBuffers)
    }

    fun withAccumulationBitsPerPixel(accumBpp: Int): PixelFormat {
        require(accumBpp >= 0) { "Invalid number of bits per pixel in the accumulation buffer specified: $accumBpp" }
        return copy(accumBpp = accumBpp)
    }

    fun withAccumulationAlpha(accumAlpha: Int): PixelFormat {
        require(accumAlpha >= 0) { "Invalid number of alpha bits in the accumulation buffer specified: $accumAlpha" }
        return copy(accumAlpha = accumAlpha)
    }

    fun withStereo(stereo: Boolean): PixelFormat = copy(stereo = stereo)

    fun withFloatingPoint(floatingPoint: Boolean): PixelFormat =
        if (floatingPoint) copy(floatingPoint = true, floatingPointPacked = false)
        else copy(floatingPoint = false)

    fun withFloatingPointPacked(floatingPointPacked: Boolean): PixelFormat =
        if (floatingPointPacked) copy(floatingPointPacked = true, floatingPoint = false)
        else copy(floatingPointPacked = false)

    fun withSRGB(sRGB: Boolean): PixelFormat = copy(sRGB = sRGB)
}

package org.lwjgl.opengl

/**
 * Represents a display mode with width, height, bits per pixel, and frequency.
 *
 * @property width The width of the display mode in pixels.
 * @property height The height of the display mode in pixels.
 * @property bitsPerPixel The bits per pixel of the display mode.
 * @property frequency The refresh rate (frequency) of the display mode in Hz.
 */
data class DisplayMode(
    private val width: Int,
    private val height: Int,
    private val bitsPerPixel: Int,
    private val frequency: Int
) {
    constructor(width: Int, height: Int) : this(width, height, 32, 60)

    /**
     * Gets the width of the display mode.
     * @return The width in pixels.
     */
    fun getWidth(): Int = width

    /**
     * Gets the height of the display mode.
     * @return The height in pixels.
     */
    fun getHeight(): Int = height

    /**
     * Gets the bits per pixel of the display mode.
     * @return The bits per pixel.
     */
    fun getBitsPerPixel(): Int = bitsPerPixel

    /**
     * Gets the frequency (refresh rate) of the display mode.
     * @return The frequency in Hz.
     */
    fun getFrequency(): Int = frequency

    override fun toString(): String {
        return "DisplayMode(width=$width, height=$height, bpp=$bitsPerPixel, freq=$frequency)"
    }
}
package org.lwjgl.input

import org.lwjgl.opengl.Display
import org.lwjgl.Sys
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.system.MemoryUtil

object Keyboard {
    const val KEY_NONE = 0x00
    const val KEY_ESCAPE = 0x01
    const val KEY_1 = 0x02
    const val KEY_2 = 0x03
    const val KEY_3 = 0x04
    const val KEY_4 = 0x05
    const val KEY_5 = 0x06
    const val KEY_6 = 0x07
    const val KEY_7 = 0x08
    const val KEY_8 = 0x09
    const val KEY_9 = 0x0A
    const val KEY_0 = 0x0B
    const val KEY_MINUS = 0x0C /* - on main keyboard */
    const val KEY_EQUALS = 0x0D
    const val KEY_BACK = 0x0E /* backspace */
    const val KEY_TAB = 0x0F
    const val KEY_Q = 0x10
    const val KEY_W = 0x11
    const val KEY_E = 0x12
    const val KEY_R = 0x13
    const val KEY_T = 0x14
    const val KEY_Y = 0x15
    const val KEY_U = 0x16
    const val KEY_I = 0x17
    const val KEY_O = 0x18
    const val KEY_P = 0x19
    const val KEY_LBRACKET = 0x1A
    const val KEY_RBRACKET = 0x1B
    const val KEY_RETURN = 0x1C /* Enter on main keyboard */
    const val KEY_LCONTROL = 0x1D
    const val KEY_A = 0x1E
    const val KEY_S = 0x1F
    const val KEY_D = 0x20
    const val KEY_F = 0x21
    const val KEY_G = 0x22
    const val KEY_H = 0x23
    const val KEY_J = 0x24
    const val KEY_K = 0x25
    const val KEY_L = 0x26
    const val KEY_SEMICOLON = 0x27
    const val KEY_APOSTROPHE = 0x28
    const val KEY_GRAVE = 0x29 /* accent grave */
    const val KEY_LSHIFT = 0x2A
    const val KEY_BACKSLASH = 0x2B
    const val KEY_Z = 0x2C
    const val KEY_X = 0x2D
    const val KEY_C = 0x2E
    const val KEY_V = 0x2F
    const val KEY_B = 0x30
    const val KEY_N = 0x31
    const val KEY_M = 0x32
    const val KEY_COMMA = 0x33
    const val KEY_PERIOD = 0x34 /* . on main keyboard */
    const val KEY_SLASH = 0x35 /* / on main keyboard */
    const val KEY_RSHIFT = 0x36
    const val KEY_MULTIPLY = 0x37 /* * on numeric keypad */
    const val KEY_LMENU = 0x38 /* left Alt */
    const val KEY_SPACE = 0x39
    const val KEY_CAPITAL = 0x3A
    const val KEY_F1 = 0x3B
    const val KEY_F2 = 0x3C
    const val KEY_F3 = 0x3D
    const val KEY_F4 = 0x3E
    const val KEY_F5 = 0x3F
    const val KEY_F6 = 0x40
    const val KEY_F7 = 0x41
    const val KEY_F8 = 0x42
    const val KEY_F9 = 0x43
    const val KEY_F10 = 0x44
    const val KEY_NUMLOCK = 0x45
    const val KEY_SCROLL = 0x46
    const val KEY_NUMPAD7 = 0x47
    const val KEY_NUMPAD8 = 0x48
    const val KEY_NUMPAD9 = 0x49
    const val KEY_SUBTRACT = 0x4A /* - on numeric keypad */
    const val KEY_NUMPAD4 = 0x4B
    const val KEY_NUMPAD5 = 0x4C
    const val KEY_NUMPAD6 = 0x4D
    const val KEY_ADD = 0x4E /* + on numeric keypad */
    const val KEY_NUMPAD1 = 0x4F
    const val KEY_NUMPAD2 = 0x50
    const val KEY_NUMPAD3 = 0x51
    const val KEY_NUMPAD0 = 0x52
    const val KEY_DECIMAL = 0x53 /* . on numeric keypad */
    const val KEY_F11 = 0x57
    const val KEY_F12 = 0x58
    const val KEY_F13 = 0x64 /* (NEC PC98) */
    const val KEY_F14 = 0x65 /* (NEC PC98) */
    const val KEY_F15 = 0x66 /* (NEC PC98) */
    const val KEY_KANA = 0x70 /* (Japanese keyboard) */
    const val KEY_ABNT_C1 = 0x73 /* / on Transmeta Crap keyboard. */
    const val KEY_CONVERT = 0x79 /* (Japanese keyboard) */
    const val KEY_NOCONVERT = 0x7B /* (Japanese keyboard) */
    const val KEY_YEN = 0x7D /* (Japanese keyboard) */
    const val KEY_ABNT_C2 = 0x7E /* Numpad . on Transmeta Crap keyboard. */
    const val KEY_NUMPADEQUALS = 0x8D /* = on numeric keypad (NEC PC98) */
    const val KEY_CIRCUMFLEX = 0x90 /* (Japanese keyboard) */
    const val KEY_AT = 0x91 /* (NEC PC98) */
    const val KEY_COLON = 0x92 /* (NEC PC98) */
    const val KEY_UNDERLINE = 0x93 /* (NEC PC98) */
    const val KEY_KANJI = 0x94 /* (Japanese keyboard) */
    const val KEY_STOP = 0x95 /* (NEC PC98) */
    const val KEY_AX = 0x96 /* (Japan AX) */
    const val KEY_UNLABELED = 0x97 /* (J3100) */
    const val KEY_NUMPADENTER = 0x9C /* Enter on numeric keypad */
    const val KEY_RCONTROL = 0x9D
    const val KEY_NUMPADCOMMA = 0xB3 /* , on numeric keypad (NEC PC98) */
    const val KEY_DIVIDE = 0xB5 /* / on numeric keypad */
    const val KEY_SYSRQ = 0xB7
    const val KEY_RMENU = 0xB8 /* right Alt */
    const val KEY_PAUSE = 0xC5 /* Pause */
    const val KEY_HOME = 0xC7 /* Home on arrow keypad */
    const val KEY_UP = 0xC8 /* UpArrow on arrow keypad */
    const val KEY_PRIOR = 0xC9 /* PgUp on arrow keypad */
    const val KEY_LEFT = 0xCB /* LeftArrow on arrow keypad */
    const val KEY_RIGHT = 0xCD /* RightArrow on arrow keypad */
    const val KEY_END = 0xCF /* End on arrow keypad */
    const val KEY_DOWN = 0xD0 /* DownArrow on arrow keypad */
    const val KEY_NEXT = 0xD1 /* PgDn on arrow keypad */
    const val KEY_INSERT = 0xD2 /* Insert on arrow keypad */
    const val KEY_DELETE = 0xD3 /* Delete on arrow keypad */
    const val KEY_LWIN = 0xDB /* Left Windows key */
    const val KEY_RWIN = 0xDC /* Right Windows key */
    const val KEY_APPS = 0xDD /* AppMenu key */
    const val KEY_POWER = 0xDE
    const val KEY_SLEEP = 0xDF

    private var keyCallback: org.lwjgl.glfw.GLFWKeyCallback? = null
    private val eventQueue: java.util.Queue<KeyboardEvent> = java.util.LinkedList()
    private var currentEvent: KeyboardEvent? = null

    private val keyStates: MutableMap<Int, Boolean> = mutableMapOf()

    private val glfwKeyToLwjgl2Key = mapOf(
        GLFW.GLFW_KEY_ESCAPE to KEY_ESCAPE,
        GLFW.GLFW_KEY_1 to KEY_1,
        GLFW.GLFW_KEY_2 to KEY_2,
        GLFW.GLFW_KEY_3 to KEY_3,
        GLFW.GLFW_KEY_4 to KEY_4,
        GLFW.GLFW_KEY_5 to KEY_5,
        GLFW.GLFW_KEY_6 to KEY_6,
        GLFW.GLFW_KEY_7 to KEY_7,
        GLFW.GLFW_KEY_8 to KEY_8,
        GLFW.GLFW_KEY_9 to KEY_9,
        GLFW.GLFW_KEY_0 to KEY_0,
        GLFW.GLFW_KEY_MINUS to KEY_MINUS,
        GLFW.GLFW_KEY_EQUAL to KEY_EQUALS,
        GLFW.GLFW_KEY_BACKSPACE to KEY_BACK,
        GLFW.GLFW_KEY_TAB to KEY_TAB,
        GLFW.GLFW_KEY_Q to KEY_Q,
        GLFW.GLFW_KEY_W to KEY_W,
        GLFW.GLFW_KEY_E to KEY_E,
        GLFW.GLFW_KEY_R to KEY_R,
        GLFW.GLFW_KEY_T to KEY_T,
        GLFW.GLFW_KEY_Y to KEY_Y,
        GLFW.GLFW_KEY_U to KEY_U,
        GLFW.GLFW_KEY_I to KEY_I,
        GLFW.GLFW_KEY_O to KEY_O,
        GLFW.GLFW_KEY_P to KEY_P,
        GLFW.GLFW_KEY_LEFT_BRACKET to KEY_LBRACKET,
        GLFW.GLFW_KEY_RIGHT_BRACKET to KEY_RBRACKET,
        GLFW.GLFW_KEY_ENTER to KEY_RETURN,
        GLFW.GLFW_KEY_LEFT_CONTROL to KEY_LCONTROL,
        GLFW.GLFW_KEY_A to KEY_A,
        GLFW.GLFW_KEY_S to KEY_S,
        GLFW.GLFW_KEY_D to KEY_D,
        GLFW.GLFW_KEY_F to KEY_F,
        GLFW.GLFW_KEY_G to KEY_G,
        GLFW.GLFW_KEY_H to KEY_H,
        GLFW.GLFW_KEY_J to KEY_J,
        GLFW.GLFW_KEY_K to KEY_K,
        GLFW.GLFW_KEY_L to KEY_L,
        GLFW.GLFW_KEY_SEMICOLON to KEY_SEMICOLON,
        GLFW.GLFW_KEY_APOSTROPHE to KEY_APOSTROPHE,
        GLFW.GLFW_KEY_GRAVE_ACCENT to KEY_GRAVE,
        GLFW.GLFW_KEY_LEFT_SHIFT to KEY_LSHIFT,
        GLFW.GLFW_KEY_BACKSLASH to KEY_BACKSLASH,
        GLFW.GLFW_KEY_Z to KEY_Z,
        GLFW.GLFW_KEY_X to KEY_X,
        GLFW.GLFW_KEY_C to KEY_C,
        GLFW.GLFW_KEY_V to KEY_V,
        GLFW.GLFW_KEY_B to KEY_B,
        GLFW.GLFW_KEY_N to KEY_N,
        GLFW.GLFW_KEY_M to KEY_M,
        GLFW.GLFW_KEY_COMMA to KEY_COMMA,
        GLFW.GLFW_KEY_PERIOD to KEY_PERIOD,
        GLFW.GLFW_KEY_SLASH to KEY_SLASH,
        GLFW.GLFW_KEY_RIGHT_SHIFT to KEY_RSHIFT,
        GLFW.GLFW_KEY_KP_MULTIPLY to KEY_MULTIPLY,
        GLFW.GLFW_KEY_LEFT_ALT to KEY_LMENU,
        GLFW.GLFW_KEY_SPACE to KEY_SPACE,
        GLFW.GLFW_KEY_CAPS_LOCK to KEY_CAPITAL,
        GLFW.GLFW_KEY_F1 to KEY_F1,
        GLFW.GLFW_KEY_F2 to KEY_F2,
        GLFW.GLFW_KEY_F3 to KEY_F3,
        GLFW.GLFW_KEY_F4 to KEY_F4,
        GLFW.GLFW_KEY_F5 to KEY_F5,
        GLFW.GLFW_KEY_F6 to KEY_F6,
        GLFW.GLFW_KEY_F7 to KEY_F7,
        GLFW.GLFW_KEY_F8 to KEY_F8,
        GLFW.GLFW_KEY_F9 to KEY_F9,
        GLFW.GLFW_KEY_F10 to KEY_F10,
        GLFW.GLFW_KEY_NUM_LOCK to KEY_NUMLOCK,
        GLFW.GLFW_KEY_SCROLL_LOCK to KEY_SCROLL,
        GLFW.GLFW_KEY_KP_7 to KEY_NUMPAD7,
        GLFW.GLFW_KEY_KP_8 to KEY_NUMPAD8,
        GLFW.GLFW_KEY_KP_9 to KEY_NUMPAD9,
        GLFW.GLFW_KEY_KP_SUBTRACT to KEY_SUBTRACT,
        GLFW.GLFW_KEY_KP_4 to KEY_NUMPAD4,
        GLFW.GLFW_KEY_KP_5 to KEY_NUMPAD5,
        GLFW.GLFW_KEY_KP_6 to KEY_NUMPAD6,
        GLFW.GLFW_KEY_KP_ADD to KEY_ADD,
        GLFW.GLFW_KEY_KP_1 to KEY_NUMPAD1,
        GLFW.GLFW_KEY_KP_2 to KEY_NUMPAD2,
        GLFW.GLFW_KEY_KP_3 to KEY_NUMPAD3,
        GLFW.GLFW_KEY_KP_0 to KEY_NUMPAD0,
        GLFW.GLFW_KEY_KP_DECIMAL to KEY_DECIMAL,
        GLFW.GLFW_KEY_F11 to KEY_F11,
        GLFW.GLFW_KEY_F12 to KEY_F12,
        GLFW.GLFW_KEY_KP_ENTER to KEY_NUMPADENTER,
        GLFW.GLFW_KEY_RIGHT_CONTROL to KEY_RCONTROL,
        GLFW.GLFW_KEY_KP_DIVIDE to KEY_DIVIDE,
        GLFW.GLFW_KEY_PRINT_SCREEN to KEY_SYSRQ,
        GLFW.GLFW_KEY_RIGHT_ALT to KEY_RMENU,
        GLFW.GLFW_KEY_PAUSE to KEY_PAUSE,
        GLFW.GLFW_KEY_HOME to KEY_HOME,
        GLFW.GLFW_KEY_UP to KEY_UP,
        GLFW.GLFW_KEY_PAGE_UP to KEY_PRIOR,
        GLFW.GLFW_KEY_LEFT to KEY_LEFT,
        GLFW.GLFW_KEY_RIGHT to KEY_RIGHT,
        GLFW.GLFW_KEY_END to KEY_END,
        GLFW.GLFW_KEY_DOWN to KEY_DOWN,
        GLFW.GLFW_KEY_PAGE_DOWN to KEY_NEXT,
        GLFW.GLFW_KEY_INSERT to KEY_INSERT,
        GLFW.GLFW_KEY_DELETE to KEY_DELETE,
        GLFW.GLFW_KEY_LEFT_SUPER to KEY_LWIN,
        GLFW.GLFW_KEY_RIGHT_SUPER to KEY_RWIN,
        GLFW.GLFW_KEY_MENU to KEY_APPS
    )

    private data class KeyboardEvent(
        val key: Int,
        val state: Boolean,
        val character: Char,
        val timestamp: Long
    )

    @JvmStatic
    fun create() {
        if (!isCreated()) {
            val window = Display.getWindowHandle()
            if (window != MemoryUtil.NULL) {
                keyCallback = object : GLFWKeyCallback() {
                    override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
                        val lwjgl2Key = glfwKeyToLwjgl2Key[key] ?: KEY_NONE
                        val isPressed = action != GLFW.GLFW_RELEASE

                        keyStates[key] = isPressed

                        val char = if (isPressed) getCharFromKey(key, mods) else '\u0000'

                        eventQueue.offer(KeyboardEvent(lwjgl2Key, isPressed, char, Sys.getTime()))
                    }
                }
                GLFW.glfwSetKeyCallback(window, keyCallback)
            } else {
                System.err.println("Keyboard.create() called before Display.create().")
            }
        }
    }

    @JvmStatic
    fun destroy() {
        if (isCreated()) {
            val window = Display.getWindowHandle()
            if (window != MemoryUtil.NULL) {
                GLFW.glfwSetKeyCallback(window, null)
            }
            keyCallback?.free()
            keyCallback = null
            eventQueue.clear()
            keyStates.clear()
            currentEvent = null
        }
    }

    @JvmStatic
    fun isCreated(): Boolean = keyCallback != null && Display.isCreated()

    @JvmStatic
    fun next(): Boolean {
        currentEvent = eventQueue.poll()
        return currentEvent != null
    }

    @JvmStatic
    fun getEventKeyState(): Boolean = currentEvent?.state ?: false

    @JvmStatic
    fun getEventKey(): Int = currentEvent?.key ?: KEY_NONE

    @JvmStatic
    fun getEventCharacter(): Char = currentEvent?.character ?: '\u0000'

    @JvmStatic
    fun isKeyDown(key: Int): Boolean {
        val glfwKey = lwjgl2KeyToGlfwKey[key] ?: return false
        return keyStates[glfwKey] == true
    }

    @JvmStatic
    private fun getCharFromKey(glfwKey: Int, mods: Int): Char {
        return when (glfwKey) {
            GLFW.GLFW_KEY_A -> 'a'
            GLFW.GLFW_KEY_B -> 'b'
            GLFW.GLFW_KEY_C -> 'c'
            GLFW.GLFW_KEY_D -> 'd'
            GLFW.GLFW_KEY_E -> 'e'
            GLFW.GLFW_KEY_F -> 'f'
            GLFW.GLFW_KEY_G -> 'g'
            GLFW.GLFW_KEY_H -> 'h'
            GLFW.GLFW_KEY_I -> 'i'
            GLFW.GLFW_KEY_J -> 'j'
            GLFW.GLFW_KEY_K -> 'k'
            GLFW.GLFW_KEY_L -> 'l'
            GLFW.GLFW_KEY_M -> 'm'
            GLFW.GLFW_KEY_N -> 'n'
            GLFW.GLFW_KEY_O -> 'o'
            GLFW.GLFW_KEY_P -> 'p'
            GLFW.GLFW_KEY_Q -> 'q'
            GLFW.GLFW_KEY_R -> 'r'
            GLFW.GLFW_KEY_S -> 's'
            GLFW.GLFW_KEY_T -> 't'
            GLFW.GLFW_KEY_U -> 'u'
            GLFW.GLFW_KEY_V -> 'v'
            GLFW.GLFW_KEY_W -> 'w'
            GLFW.GLFW_KEY_X -> 'x'
            GLFW.GLFW_KEY_Y -> 'y'
            GLFW.GLFW_KEY_Z -> 'z'
            GLFW.GLFW_KEY_0 -> '0'
            GLFW.GLFW_KEY_1 -> '1'
            GLFW.GLFW_KEY_2 -> '2'
            GLFW.GLFW_KEY_3 -> '3'
            GLFW.GLFW_KEY_4 -> '4'
            GLFW.GLFW_KEY_5 -> '5'
            GLFW.GLFW_KEY_6 -> '6'
            GLFW.GLFW_KEY_7 -> '7'
            GLFW.GLFW_KEY_8 -> '8'
            GLFW.GLFW_KEY_9 -> '9'
            GLFW.GLFW_KEY_SPACE -> ' '
            GLFW.GLFW_KEY_APOSTROPHE -> '\''
            GLFW.GLFW_KEY_COMMA -> ','
            GLFW.GLFW_KEY_MINUS -> '-'
            GLFW.GLFW_KEY_PERIOD -> '.'
            GLFW.GLFW_KEY_SLASH -> '/'
            GLFW.GLFW_KEY_SEMICOLON -> ';'
            GLFW.GLFW_KEY_EQUAL -> '='
            GLFW.GLFW_KEY_LEFT_BRACKET -> '['
            GLFW.GLFW_KEY_BACKSLASH -> '\\'
            GLFW.GLFW_KEY_RIGHT_BRACKET -> ']'
            GLFW.GLFW_KEY_GRAVE_ACCENT -> '`'
            // Handle shift for uppercase letters and symbols
            else -> '\u0000'
        }
    }

    private val lwjgl2KeyToGlfwKey = glfwKeyToLwjgl2Key.entries.associate { (k, v) -> v to k }

    @JvmStatic
    fun getKeyName(key: Int): String {
        val glfwKey = lwjgl2KeyToGlfwKey[key] ?: return "Unknown Key"
        return GLFW.glfwGetKeyName(glfwKey, 0) ?: "Unknown Key"
    }

    fun handleCharInput(codepoint: Int) {}

    @JvmStatic
    fun enableRepeatEvents(b: Boolean) {
    }
}
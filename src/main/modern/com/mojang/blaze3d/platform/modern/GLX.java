package com.mojang.blaze3d.platform.modern;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import org.apache.logging.log4j.LogManager;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;

@Environment(EnvType.CLIENT)
@DontObfuscate
public class GLX {
	private static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private static String cpuInfo;

	public static String _getLWJGLVersion() {
		return Version.getVersion();
	}

	public static void _setGlfwErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
		GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(gLFWErrorCallbackI);
		if (gLFWErrorCallback != null) {
			gLFWErrorCallback.free();
		}
	}

	public static <T> T make(Supplier<T> supplier) {
		return (T)supplier.get();
	}

	public static <T> T make(T object, Consumer<T> consumer) {
		consumer.accept(object);
		return object;
	}
}

package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.util.Identifier;
import net.minecraft.util.StrictJsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ShaderManager implements ResourceReloadListener, AutoCloseable {
	static final Logger LOGGER = LogManager.getLogger();
	public static final int MAX_LOG_LENGTH = 32768;
	public static final String SHADER_PATH = "shaders";
	private static final String SHADER_INCLUDE_PATH = "shaders/include/";
	private static final FileToIdConverter POST_CHAIN_ID_CONVERTER = FileToIdConverter.json("post_effect");
	final TextureManager textureManager;
	private final Consumer<Exception> recoveryHandler;
	private ShaderManager.CompilationCache compilationCache = new ShaderManager.CompilationCache(ShaderManager.Configs.EMPTY);
	final CachedOrthoProjectionMatrixBuffer postChainProjectionMatrixBuffer = new CachedOrthoProjectionMatrixBuffer("post", 0.1F, 1000.0F, false);

	public ShaderManager(TextureManager textureManager, Consumer<Exception> consumer) {
		this.textureManager = textureManager;
		this.recoveryHandler = consumer;
	}

	@Override
	public void reload(ResourceManager resourceManager) {
		// clear current cache

		if (this.compilationCache != null) {
			this.compilationCache.close();
		}

		this.compilationCache = new CompilationCache(Configs.EMPTY);
	}

	private static void loadShader(
		Identifier resourceLocation,
		Resource resource,
		ShaderType shaderType,
		Map<Identifier, Resource> map,
		Builder<ShaderManager.ShaderSourceKey, String> builder
	) {
		Identifier resourceLocation2 = shaderType.idConverter().fileToId(resourceLocation);
		GlslPreprocessor glslPreprocessor = createPreprocessor(map, resourceLocation);

		try {
			Reader reader = resource.openAsReader();

			try {
				String string = IOUtils.toString(reader);
				builder.put(new ShaderManager.ShaderSourceKey(resourceLocation2, shaderType), String.join("", glslPreprocessor.process(string)));
			} catch (Throwable var11) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable var10) {
						var11.addSuppressed(var10);
					}
				}

				throw var11;
			}

			if (reader != null) {
				reader.close();
			}
		} catch (IOException var12) {
			LOGGER.error("Failed to load shader source at {}", resourceLocation, var12);
		}
	}

	private static GlslPreprocessor createPreprocessor(Map<Identifier, Resource> map, Identifier resourceLocation) {
		final Identifier resourceLocation2 = resourceLocation.withPath(FileUtil::getFullResourcePath);
		return new GlslPreprocessor() {
			private final Set<Identifier> importedLocations = new ObjectArraySet<>();

			@Override
			public String applyImport(boolean bl, String string) {
				Identifier resourceLocationx;
				try {
					if (bl) {
						resourceLocationx = resourceLocation2.withPath((UnaryOperator<String>)(string2 -> FileUtil.normalizeResourcePath(string2 + string)));
					} else {
						resourceLocationx = Identifier.parse(string).withPrefix("shaders/include/");
					}
				} catch (Exception var8) {
					ShaderManager.LOGGER.error("Malformed GLSL import {}: {}", string, var8.getMessage());
					return "#error " + var8.getMessage();
				}

				if (!this.importedLocations.add(resourceLocationx)) {
					return null;
				} else {
					try {
						Reader reader = ((Resource)map.get(resourceLocationx)).openAsReader();

						String var5;
						try {
							var5 = IOUtils.toString(reader);
						} catch (Throwable var9) {
							if (reader != null) {
								try {
									reader.close();
								} catch (Throwable var7) {
									var9.addSuppressed(var7);
								}
							}

							throw var9;
						}

						if (reader != null) {
							reader.close();
						}

						return var5;
					} catch (IOException var10) {
						ShaderManager.LOGGER.error("Could not open GLSL import {}: {}", resourceLocationx, var10.getMessage());
						return "#error " + var10.getMessage();
					}
				}
			}
		};
	}

	private static void loadPostChain(Identifier resourceLocation, Resource resource, Builder<Identifier, PostChainConfig> builder) {
		Identifier resourceLocation2 = POST_CHAIN_ID_CONVERTER.fileToId(resourceLocation);

		try {
			Reader reader = resource.openAsReader();

			try {
				JsonElement jsonElement = StrictJsonParser.parse(reader);
				builder.put(resourceLocation2, PostChainConfig.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonSyntaxException::new));
			} catch (Throwable var8) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (reader != null) {
				reader.close();
			}
		} catch (JsonParseException | IOException var9) {
			LOGGER.error("Failed to parse post chain at {}", resourceLocation, var9);
		}
	}

	private static boolean isShader(Identifier resourceLocation) {
		return ShaderType.byLocation(resourceLocation) != null || resourceLocation.getPath().endsWith(".glsl");
	}

	private void tryTriggerRecovery(Exception exception) {
		if (!this.compilationCache.triggeredRecovery) {
			this.recoveryHandler.accept(exception);
			this.compilationCache.triggeredRecovery = true;
		}
	}

	@Nullable
	public PostChain getPostChain(Identifier resourceLocation, Set<Identifier> set) {
		try {
			return this.compilationCache.getOrLoadPostChain(resourceLocation, set);
		} catch (ShaderManager.CompilationException var4) {
			LOGGER.error("Failed to load post chain: {}", resourceLocation, var4);
			this.compilationCache.postChains.put(resourceLocation, Optional.empty());
			this.tryTriggerRecovery(var4);
			return null;
		}
	}

	public void close() {
		this.compilationCache.close();
		this.postChainProjectionMatrixBuffer.close();
	}

	public String getShader(Identifier resourceLocation, ShaderType shaderType) {
		return this.compilationCache.getShaderSource(resourceLocation, shaderType);
	}

	@Environment(EnvType.CLIENT)
	class CompilationCache implements AutoCloseable {
		private final ShaderManager.Configs configs;
		final Map<Identifier, Optional<PostChain>> postChains = new HashMap();
		boolean triggeredRecovery;

		CompilationCache(final ShaderManager.Configs configs) {
			this.configs = configs;
		}

		@Nullable
		public PostChain getOrLoadPostChain(Identifier resourceLocation, Set<Identifier> set) throws ShaderManager.CompilationException {
			Optional<PostChain> optional = (Optional<PostChain>)this.postChains.get(resourceLocation);
			if (optional != null) {
				return (PostChain)optional.orElse(null);
			} else {
				PostChain postChain = this.loadPostChain(resourceLocation, set);
				this.postChains.put(resourceLocation, Optional.of(postChain));
				return postChain;
			}
		}

		private PostChain loadPostChain(Identifier resourceLocation, Set<Identifier> set) throws CompilationException {
			PostChainConfig postChainConfig = this.configs.postChains.get(resourceLocation);
			if (postChainConfig == null) {
				try {
					Identifier fileId = POST_CHAIN_ID_CONVERTER.idToFile(resourceLocation);
					Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(fileId);
					try (Reader reader = resource.openAsReader()) {
						JsonElement jsonElement = StrictJsonParser.parse(reader);
						postChainConfig = PostChainConfig.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonSyntaxException::new);
						// Optionally cache it
						((Map<Identifier, PostChainConfig>) configs.postChains).put(resourceLocation, postChainConfig);
					}
				} catch (IOException | JsonParseException e) {
					throw new CompilationException("Failed to load post chain config: " + resourceLocation);
				}
			}

			return PostChain.load(postChainConfig, ShaderManager.this.textureManager, set, resourceLocation, ShaderManager.this.postChainProjectionMatrixBuffer);
		}

		public void close() {
			this.postChains.values().forEach(optional -> optional.ifPresent(PostChain::close));
			this.postChains.clear();
		}

		public String getShaderSource(Identifier resourceLocation, ShaderType shaderType) {
			ShaderSourceKey key = new ShaderSourceKey(resourceLocation, shaderType);
			String source = this.configs.shaderSources.get(key);
			if (source != null) {
				return source;
			}

			try {
				Identifier fileId = shaderType.idConverter().idToFile(resourceLocation);
				System.out.printf("Loading %s%n", fileId.toString());
				Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(fileId);
				GlslPreprocessor preprocessor = createPreprocessor(MinecraftClient.getInstance().getResourceManager().listResources(ShaderManager::isShader), fileId);
				try (Reader reader = resource.openAsReader()) {
					String raw = IOUtils.toString(reader);
					String processed = String.join("", preprocessor.process(raw));
					configs.shaderSources.put(key, processed);
					return processed;
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to load shader source: " + resourceLocation, e);
			}
		}

	}

	@Environment(EnvType.CLIENT)
	public static class CompilationException extends Exception {
		public CompilationException(String string) {
			super(string);
		}
	}

	@Environment(EnvType.CLIENT)
	public record Configs(Map<ShaderManager.ShaderSourceKey, String> shaderSources, Map<Identifier, PostChainConfig> postChains) {
		public static final ShaderManager.Configs EMPTY = new ShaderManager.Configs(Map.of(), Map.of());
	}

	@Environment(EnvType.CLIENT)
	record ShaderSourceKey(Identifier id, ShaderType type) {
		public String toString() {
			return this.id + " (" + this.type + ")";
		}
	}
}

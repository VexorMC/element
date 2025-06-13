package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

@Environment(EnvType.CLIENT)
public abstract class RenderStateShard {
	public static final double MAX_ENCHANTMENT_GLINT_SPEED_MILLIS = 8.0;
	protected final String name;
	private final Runnable setupState;
	private final Runnable clearState;
	protected static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = new RenderStateShard.TextureStateShard(SpriteAtlasTexture.BLOCK_ATLAS_TEX, true);
	protected static final RenderStateShard.TextureStateShard BLOCK_SHEET = new RenderStateShard.TextureStateShard(SpriteAtlasTexture.BLOCK_ATLAS_TEX, false);
	protected static final RenderStateShard.EmptyTextureStateShard NO_TEXTURE = new RenderStateShard.EmptyTextureStateShard();
	protected static final RenderStateShard.TexturingStateShard DEFAULT_TEXTURING = new RenderStateShard.TexturingStateShard(
		"default_texturing", () -> {}, () -> {}
	);
	protected static final RenderStateShard.TexturingStateShard GLINT_TEXTURING = new RenderStateShard.TexturingStateShard(
		"glint_texturing", () -> setupGlintTexturing(8.0F), RenderSystem::resetTextureMatrix
	);
	protected static final RenderStateShard.TexturingStateShard ENTITY_GLINT_TEXTURING = new RenderStateShard.TexturingStateShard(
		"entity_glint_texturing", () -> setupGlintTexturing(0.5F), RenderSystem::resetTextureMatrix
	);
	protected static final RenderStateShard.TexturingStateShard ARMOR_ENTITY_GLINT_TEXTURING = new RenderStateShard.TexturingStateShard(
		"armor_entity_glint_texturing", () -> setupGlintTexturing(0.16F), RenderSystem::resetTextureMatrix
	);
	protected static final RenderStateShard.LightmapStateShard LIGHTMAP = new RenderStateShard.LightmapStateShard(true);
	protected static final RenderStateShard.LightmapStateShard NO_LIGHTMAP = new RenderStateShard.LightmapStateShard(false);
	protected static final RenderStateShard.OverlayStateShard OVERLAY = new RenderStateShard.OverlayStateShard(true);
	protected static final RenderStateShard.OverlayStateShard NO_OVERLAY = new RenderStateShard.OverlayStateShard(false);
	protected static final RenderStateShard.LayeringStateShard NO_LAYERING = new RenderStateShard.LayeringStateShard("no_layering", () -> {}, () -> {});
	protected static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING = new RenderStateShard.LayeringStateShard("view_offset_z_layering", () -> {
		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();
		RenderSystem.getProjectionType().applyLayeringTransform(matrix4fStack, 1.0F);
	}, () -> {
		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.popMatrix();
	});
	protected static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING_FORWARD = new RenderStateShard.LayeringStateShard(
		"view_offset_z_layering_forward", () -> {
			Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
			matrix4fStack.pushMatrix();
			RenderSystem.getProjectionType().applyLayeringTransform(matrix4fStack, -1.0F);
		}, () -> {
			Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
			matrix4fStack.popMatrix();
		}
	);
	protected static final RenderStateShard.OutputStateShard MAIN_TARGET = new RenderStateShard.OutputStateShard(
		"main_target", () -> MinecraftClient.getInstance().getMainRenderTarget()
	);
	protected static final RenderStateShard.OutputStateShard OUTLINE_TARGET = new RenderStateShard.OutputStateShard("outline_target", () -> {
		RenderTarget renderTarget = /*MinecraftClient.getInstance().worldRenderer.entityOutlineTarget()*/ null;
		return renderTarget != null ? renderTarget : MinecraftClient.getInstance().getMainRenderTarget();
	});
	protected static final RenderStateShard.OutputStateShard TRANSLUCENT_TARGET = new RenderStateShard.OutputStateShard("translucent_target", () -> {
		RenderTarget renderTarget = /*MinecraftClient.getInstance().worldRenderer.getTranslucentTarget()*/ null;
		return renderTarget != null ? renderTarget : MinecraftClient.getInstance().getMainRenderTarget();
	});
	protected static final RenderStateShard.OutputStateShard PARTICLES_TARGET = new RenderStateShard.OutputStateShard("particles_target", () -> {
		RenderTarget renderTarget = /*MinecraftClient.getInstance().worldRenderer.getParticlesTarget()*/ null;
		return renderTarget != null ? renderTarget : MinecraftClient.getInstance().getMainRenderTarget();
	});
	protected static final RenderStateShard.OutputStateShard WEATHER_TARGET = new RenderStateShard.OutputStateShard("weather_target", () -> {
		RenderTarget renderTarget = /*MinecraftClient.getInstance().worldRenderer.getWeatherTarget()*/ null;
		return renderTarget != null ? renderTarget : MinecraftClient.getInstance().getMainRenderTarget();
	});
	protected static final RenderStateShard.OutputStateShard ITEM_ENTITY_TARGET = new RenderStateShard.OutputStateShard("item_entity_target", () -> {
		RenderTarget renderTarget = /*MinecraftClient.getInstance().worldRenderer.getItemEntityTarget()*/ null;
		return renderTarget != null ? renderTarget : MinecraftClient.getInstance().getMainRenderTarget();
	});
	protected static final RenderStateShard.LineStateShard DEFAULT_LINE = new RenderStateShard.LineStateShard(OptionalDouble.of(1.0));

	public RenderStateShard(String string, Runnable runnable, Runnable runnable2) {
		this.name = string;
		this.setupState = runnable;
		this.clearState = runnable2;
	}

	public void setupRenderState() {
		this.setupState.run();
	}

	public void clearRenderState() {
		this.clearState.run();
	}

	public String toString() {
		return this.name;
	}

	public String getName() {
		return this.name;
	}

	private static void setupGlintTexturing(float f) {
		long l = (long)(System.currentTimeMillis() * 2 * 8.0);
		float g = (float)(l % 110000L) / 110000.0F;
		float h = (float)(l % 30000L) / 30000.0F;
		Matrix4f matrix4f = new Matrix4f().translation(-g, h, 0.0F);
		matrix4f.rotateZ((float) (Math.PI / 18)).scale(f);
		RenderSystem.setTextureMatrix(matrix4f);
	}

	@Environment(EnvType.CLIENT)
	static class BooleanStateShard extends RenderStateShard {
		private final boolean enabled;

		public BooleanStateShard(String string, Runnable runnable, Runnable runnable2, boolean bl) {
			super(string, runnable, runnable2);
			this.enabled = bl;
		}

		@Override
		public String toString() {
			return this.name + "[" + this.enabled + "]";
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class EmptyTextureStateShard extends RenderStateShard {
		public EmptyTextureStateShard(Runnable runnable, Runnable runnable2) {
			super("texture", runnable, runnable2);
		}

		EmptyTextureStateShard() {
			super("texture", () -> {}, () -> {});
		}

		protected Optional<Identifier> cutoutTexture() {
			return Optional.empty();
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class LayeringStateShard extends RenderStateShard {
		public LayeringStateShard(String string, Runnable runnable, Runnable runnable2) {
			super(string, runnable, runnable2);
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class LightmapStateShard extends RenderStateShard.BooleanStateShard {
		public LightmapStateShard(boolean bl) {
			super("lightmap", () -> {
				if (bl) {
					MinecraftClient.getInstance().gameRenderer.enableLightmap();
				}
			}, () -> {
				if (bl) {
					MinecraftClient.getInstance().gameRenderer.disableLightmap();
				}
			}, bl);
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class LineStateShard extends RenderStateShard {
		private final OptionalDouble width;

		public LineStateShard(OptionalDouble optionalDouble) {
			super("line_width", () -> {
				if (!Objects.equals(optionalDouble, OptionalDouble.of(1.0))) {
					if (optionalDouble.isPresent()) {
						RenderSystem.lineWidth((float)optionalDouble.getAsDouble());
					} else {
						RenderSystem.lineWidth(Math.max(2.5F, MinecraftClient.getInstance().width / 1920.0F * 2.5F));
					}
				}
			}, () -> {
				if (!Objects.equals(optionalDouble, OptionalDouble.of(1.0))) {
					RenderSystem.lineWidth(1.0F);
				}
			});
			this.width = optionalDouble;
		}

		@Override
		public String toString() {
			return this.name + "[" + (this.width.isPresent() ? this.width.getAsDouble() : "window_scale") + "]";
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class MultiTextureStateShard extends RenderStateShard.EmptyTextureStateShard {
		private final Optional<Identifier> cutoutTexture;

		MultiTextureStateShard(List<RenderStateShard.MultiTextureStateShard.Entry> list) {
			super(() -> {
				for (int i = 0; i < list.size(); i++) {
					RenderStateShard.MultiTextureStateShard.Entry entry = (RenderStateShard.MultiTextureStateShard.Entry)list.get(i);
					TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
					AbstractTexture abstractTexture = (AbstractTexture) textureManager.getTexture(entry.id);
					abstractTexture.setUseMipmaps(entry.mipmap);
					RenderSystem.setShaderTexture(i, abstractTexture.getTextureView());
				}
			}, () -> {});
			this.cutoutTexture = list.isEmpty() ? Optional.empty() : Optional.of(((RenderStateShard.MultiTextureStateShard.Entry)list.stream().findFirst().get()).id);
		}

		@Override
		protected Optional<Identifier> cutoutTexture() {
			return this.cutoutTexture;
		}

		public static RenderStateShard.MultiTextureStateShard.Builder builder() {
			return new RenderStateShard.MultiTextureStateShard.Builder();
		}

		@Environment(EnvType.CLIENT)
		public static final class Builder {
			private final ImmutableList.Builder<RenderStateShard.MultiTextureStateShard.Entry> builder = new ImmutableList.Builder<>();

			public RenderStateShard.MultiTextureStateShard.Builder add(Identifier resourceLocation, boolean bl) {
				this.builder.add(new RenderStateShard.MultiTextureStateShard.Entry(resourceLocation, bl));
				return this;
			}

			public RenderStateShard.MultiTextureStateShard build() {
				return new RenderStateShard.MultiTextureStateShard(this.builder.build());
			}
		}

		@Environment(EnvType.CLIENT)
		record Entry(Identifier id, boolean mipmap) {
		}
	}

	@Environment(EnvType.CLIENT)
	protected static final class OffsetTexturingStateShard extends RenderStateShard.TexturingStateShard {
		public OffsetTexturingStateShard(float f, float g) {
			super("offset_texturing", () -> RenderSystem.setTextureMatrix(new Matrix4f().translation(f, g, 0.0F)), () -> RenderSystem.resetTextureMatrix());
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class OutputStateShard extends RenderStateShard {
		private final Supplier<RenderTarget> renderTargetSupplier;

		public OutputStateShard(String string, Supplier<RenderTarget> supplier) {
			super(string, () -> {}, () -> {});
			this.renderTargetSupplier = supplier;
		}

		public RenderTarget getRenderTarget() {
			return (RenderTarget)this.renderTargetSupplier.get();
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class OverlayStateShard extends RenderStateShard.BooleanStateShard {
		public OverlayStateShard(boolean bl) {
			super("overlay", () -> {
				if (bl) {
//					Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();
				}
			}, () -> {
				if (bl) {
//					Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor();
				}
			}, bl);
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class TextureStateShard extends RenderStateShard.EmptyTextureStateShard {
		private final Optional<Identifier> texture;
		private final boolean mipmap;

		public TextureStateShard(Identifier resourceLocation, boolean bl) {
			super(() -> {
				TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
				AbstractTexture abstractTexture = (AbstractTexture) textureManager.getTexture(resourceLocation);
				abstractTexture.setUseMipmaps(bl);
				RenderSystem.setShaderTexture(0, abstractTexture.getTextureView());
			}, () -> {});
			this.texture = Optional.of(resourceLocation);
			this.mipmap = bl;
		}

		@Override
		public String toString() {
			return this.name + "[" + this.texture + "(mipmap=" + this.mipmap + ")]";
		}

		@Override
		protected Optional<Identifier> cutoutTexture() {
			return this.texture;
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class TexturingStateShard extends RenderStateShard {
		public TexturingStateShard(String string, Runnable runnable, Runnable runnable2) {
			super(string, runnable, runnable2);
		}
	}
}

package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TextureTarget extends RenderTarget {
	public TextureTarget(@Nullable String string, int i, int j, boolean bl) {
		super(string, bl);
		RenderSystem.assertOnRenderThread();
		this.resize(i, j);
	}
}

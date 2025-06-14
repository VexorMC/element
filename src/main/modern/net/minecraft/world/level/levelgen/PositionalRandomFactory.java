package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.math.BlockPos;

public interface PositionalRandomFactory {
	default RandomSource at(BlockPos blockPos) {
		return this.at(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	default RandomSource fromHashOf(Identifier resourceLocation) {
		return this.fromHashOf(resourceLocation.toString());
	}

	RandomSource fromHashOf(String string);

	RandomSource fromSeed(long l);

	RandomSource at(int i, int j, int k);

	@VisibleForTesting
	void parityConfigString(StringBuilder stringBuilder);
}

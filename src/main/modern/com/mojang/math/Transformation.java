package com.mojang.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class Transformation {
	private final Matrix4fc matrix;
	public static final Codec<Transformation> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				ExtraCodecs.VECTOR3F.fieldOf("translation").forGetter(transformation -> transformation.translation),
				ExtraCodecs.QUATERNIONF.fieldOf("left_rotation").forGetter(transformation -> transformation.leftRotation),
				ExtraCodecs.VECTOR3F.fieldOf("scale").forGetter(transformation -> transformation.scale),
				ExtraCodecs.QUATERNIONF.fieldOf("right_rotation").forGetter(transformation -> transformation.rightRotation)
			)
			.apply(instance, Transformation::new)
	);
	public static final Codec<Transformation> EXTENDED_CODEC = Codec.withAlternative(
		CODEC, ExtraCodecs.MATRIX4F.xmap(Transformation::new, Transformation::getMatrix)
	);
	private boolean decomposed;
	@Nullable
	private Vector3f translation;
	@Nullable
	private Quaternionf leftRotation;
	@Nullable
	private Vector3f scale;
	@Nullable
	private Quaternionf rightRotation;
	private static final Transformation IDENTITY = Util.make(() -> {
		Transformation transformation = new Transformation(new Matrix4f());
		transformation.translation = new Vector3f();
		transformation.leftRotation = new Quaternionf();
		transformation.scale = new Vector3f(1.0F, 1.0F, 1.0F);
		transformation.rightRotation = new Quaternionf();
		transformation.decomposed = true;
		return transformation;
	});

	public Transformation(@Nullable Matrix4fc matrix4fc) {
		if (matrix4fc == null) {
			this.matrix = new Matrix4f();
		} else {
			this.matrix = matrix4fc;
		}
	}

	public Transformation(@Nullable Vector3f vector3f, @Nullable Quaternionf quaternionf, @Nullable Vector3f vector3f2, @Nullable Quaternionf quaternionf2) {
		this.matrix = compose(vector3f, quaternionf, vector3f2, quaternionf2);
		this.translation = vector3f != null ? vector3f : new Vector3f();
		this.leftRotation = quaternionf != null ? quaternionf : new Quaternionf();
		this.scale = vector3f2 != null ? vector3f2 : new Vector3f(1.0F, 1.0F, 1.0F);
		this.rightRotation = quaternionf2 != null ? quaternionf2 : new Quaternionf();
		this.decomposed = true;
	}

	public static Transformation identity() {
		return IDENTITY;
	}

	public Transformation compose(Transformation transformation) {
		Matrix4f matrix4f = this.getMatrixCopy();
		matrix4f.mul(transformation.getMatrix());
		return new Transformation(matrix4f);
	}

	@Nullable
	public Transformation inverse() {
		if (this == IDENTITY) {
			return this;
		} else {
			Matrix4f matrix4f = this.getMatrixCopy().invertAffine();
			return matrix4f.isFinite() ? new Transformation(matrix4f) : null;
		}
	}

	private void ensureDecomposed() {
		if (!this.decomposed) {
			float f = 1.0F / this.matrix.m33();
			Triple<Quaternionf, Vector3f, Quaternionf> triple = MatrixUtil.svdDecompose(new Matrix3f(this.matrix).scale(f));
			this.translation = this.matrix.getTranslation(new Vector3f()).mul(f);
			this.leftRotation = new Quaternionf(triple.getLeft());
			this.scale = new Vector3f(triple.getMiddle());
			this.rightRotation = new Quaternionf(triple.getRight());
			this.decomposed = true;
		}
	}

	private static Matrix4f compose(
		@Nullable Vector3f vector3f, @Nullable Quaternionf quaternionf, @Nullable Vector3f vector3f2, @Nullable Quaternionf quaternionf2
	) {
		Matrix4f matrix4f = new Matrix4f();
		if (vector3f != null) {
			matrix4f.translation(vector3f);
		}

		if (quaternionf != null) {
			matrix4f.rotate(quaternionf);
		}

		if (vector3f2 != null) {
			matrix4f.scale(vector3f2);
		}

		if (quaternionf2 != null) {
			matrix4f.rotate(quaternionf2);
		}

		return matrix4f;
	}

	public Matrix4fc getMatrix() {
		return this.matrix;
	}

	public Matrix4f getMatrixCopy() {
		return new Matrix4f(this.matrix);
	}

	public Vector3f getTranslation() {
		this.ensureDecomposed();
		return new Vector3f(this.translation);
	}

	public Quaternionf getLeftRotation() {
		this.ensureDecomposed();
		return new Quaternionf(this.leftRotation);
	}

	public Vector3f getScale() {
		this.ensureDecomposed();
		return new Vector3f(this.scale);
	}

	public Quaternionf getRightRotation() {
		this.ensureDecomposed();
		return new Quaternionf(this.rightRotation);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Transformation transformation = (Transformation)object;
			return Objects.equals(this.matrix, transformation.matrix);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.matrix});
	}

	public Transformation slerp(Transformation transformation, float f) {
		Vector3f vector3f = this.getTranslation();
		Quaternionf quaternionf = this.getLeftRotation();
		Vector3f vector3f2 = this.getScale();
		Quaternionf quaternionf2 = this.getRightRotation();
		vector3f.lerp(transformation.getTranslation(), f);
		quaternionf.slerp(transformation.getLeftRotation(), f);
		vector3f2.lerp(transformation.getScale(), f);
		quaternionf2.slerp(transformation.getRightRotation(), f);
		return new Transformation(vector3f, quaternionf, vector3f2, quaternionf2);
	}
}

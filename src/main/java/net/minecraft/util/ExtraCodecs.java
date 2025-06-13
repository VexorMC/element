package net.minecraft.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.primitives.UnsignedBytes;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.Codec.ResultFunction;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

public class ExtraCodecs {
	public static final Codec<JsonElement> JSON = converter(JsonOps.INSTANCE);
	public static final Codec<Object> JAVA = converter(JavaOps.INSTANCE);
	public static final Codec<Integer> UNSIGNED_BYTE = Codec.BYTE
		.flatComapMap(
			UnsignedBytes::toInt,
			integer -> integer > 255 ? DataResult.error(() -> "Unsigned byte was too large: " + integer + " > 255") : DataResult.success(integer.byteValue())
		);
	public static final Codec<Integer> NON_NEGATIVE_INT = intRangeWithMessage(0, Integer.MAX_VALUE, integer -> "Value must be non-negative: " + integer);
	public static final Codec<Integer> POSITIVE_INT = intRangeWithMessage(1, Integer.MAX_VALUE, integer -> "Value must be positive: " + integer);
	public static final Codec<Float> NON_NEGATIVE_FLOAT = floatRangeMinInclusiveWithMessage(
		0.0F, Float.MAX_VALUE, float_ -> "Value must be non-negative: " + float_
	);
	public static final Codec<Float> POSITIVE_FLOAT = floatRangeMinExclusiveWithMessage(0.0F, Float.MAX_VALUE, float_ -> "Value must be positive: " + float_);
	public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap(string -> {
		try {
			return DataResult.success(Pattern.compile(string));
		} catch (PatternSyntaxException var2) {
			return DataResult.error(() -> "Invalid regex pattern '" + string + "': " + var2.getMessage());
		}
	}, Pattern::pattern);
	public static final Codec<Instant> INSTANT_ISO8601 = temporalCodec(DateTimeFormatter.ISO_INSTANT).xmap(Instant::from, Function.identity());
	public static final Codec<byte[]> BASE64_STRING = Codec.STRING.comapFlatMap(string -> {
		try {
			return DataResult.success(Base64.getDecoder().decode(string));
		} catch (IllegalArgumentException var2) {
			return DataResult.error(() -> "Malformed base64 string");
		}
	}, bs -> Base64.getEncoder().encodeToString(bs));
	public static final Codec<String> ESCAPED_STRING = Codec.STRING
		.comapFlatMap(string -> DataResult.success(StringEscapeUtils.unescapeJava(string)), StringEscapeUtils::escapeJava);
	public static final Function<Optional<Long>, OptionalLong> toOptionalLong = optional -> (OptionalLong)optional.map(OptionalLong::of)
		.orElseGet(OptionalLong::empty);
	public static final Function<OptionalLong, Optional<Long>> fromOptionalLong = optionalLong -> optionalLong.isPresent()
		? Optional.of(optionalLong.getAsLong())
		: Optional.empty();
	public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM
		.xmap(longStream -> BitSet.valueOf(longStream.toArray()), bitSet -> Arrays.stream(bitSet.toLongArray()));

	public static final Codec<String> NON_EMPTY_STRING = Codec.STRING
		.validate(string -> string.isEmpty() ? DataResult.error(() -> "Expected non-empty string") : DataResult.success(string));
	public static final Codec<Integer> CODEPOINT = Codec.STRING.comapFlatMap(string -> {
		int[] is = string.codePoints().toArray();
		return is.length != 1 ? DataResult.error(() -> "Expected one codepoint, got: " + string) : DataResult.success(is[0]);
	}, Character::toString);
	public static <T> Codec<T> converter(DynamicOps<T> dynamicOps) {
		return Codec.PASSTHROUGH.xmap(dynamic -> dynamic.convert(dynamicOps).getValue(), object -> new Dynamic<>(dynamicOps, (T)object));
	}


	public static <E> Codec<E> idResolverCodec(ToIntFunction<E> toIntFunction, IntFunction<E> intFunction, int i) {
		return Codec.INT
			.flatXmap(
				integer -> (DataResult)Optional.ofNullable(intFunction.apply(integer))
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error(() -> "Unknown element id: " + integer)),
				object -> {
					int j = toIntFunction.applyAsInt(object);
					return j == i ? DataResult.error(() -> "Element with unknown id: " + object) : DataResult.success(j);
				}
			);
	}

	public static <I, E> Codec<E> idResolverCodec(Codec<I> codec, Function<I, E> function, Function<E, I> function2) {
		return codec.flatXmap(object -> {
			E object2 = (E)function.apply(object);
			return object2 == null ? DataResult.error(() -> "Unknown element id: " + object) : DataResult.success(object2);
		}, object -> {
			I object2 = (I)function2.apply(object);
			return object2 == null ? DataResult.error(() -> "Element with unknown id: " + object) : DataResult.success(object2);
		});
	}

	public static <E> Codec<E> orCompressed(Codec<E> codec, Codec<E> codec2) {
		return new Codec<E>() {
			@Override
			public <T> DataResult<T> encode(E object, DynamicOps<T> dynamicOps, T object2) {
				return dynamicOps.compressMaps() ? codec2.encode(object, dynamicOps, object2) : codec.encode(object, dynamicOps, object2);
			}

			@Override
			public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicOps, T object) {
				return dynamicOps.compressMaps() ? codec2.decode(dynamicOps, object) : codec.decode(dynamicOps, object);
			}

			public String toString() {
				return codec + " orCompressed " + codec2;
			}
		};
	}

	public static <E> MapCodec<E> orCompressed(MapCodec<E> mapCodec, MapCodec<E> mapCodec2) {
		return new MapCodec<E>() {
			@Override
			public <T> RecordBuilder<T> encode(E object, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
				return dynamicOps.compressMaps() ? mapCodec2.encode(object, dynamicOps, recordBuilder) : mapCodec.encode(object, dynamicOps, recordBuilder);
			}

			@Override
			public <T> DataResult<E> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
				return dynamicOps.compressMaps() ? mapCodec2.decode(dynamicOps, mapLike) : mapCodec.decode(dynamicOps, mapLike);
			}

			@Override
			public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
				return mapCodec2.keys(dynamicOps);
			}

			public String toString() {
				return mapCodec + " orCompressed " + mapCodec2;
			}
		};
	}

	public static <E> Codec<E> overrideLifecycle(Codec<E> codec, Function<E, Lifecycle> function, Function<E, Lifecycle> function2) {
		return codec.mapResult(new ResultFunction<E>() {
			@Override
			public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> dynamicOps, T object, DataResult<Pair<E, T>> dataResult) {
				return (DataResult<Pair<E, T>>)dataResult.result().map(pair -> dataResult.setLifecycle((Lifecycle)function.apply(pair.getFirst()))).orElse(dataResult);
			}

			@Override
			public <T> DataResult<T> coApply(DynamicOps<T> dynamicOps, E object, DataResult<T> dataResult) {
				return dataResult.setLifecycle((Lifecycle)function2.apply(object));
			}

			public String toString() {
				return "WithLifecycle[" + function + " " + function2 + "]";
			}
		});
	}

	public static <E> Codec<E> overrideLifecycle(Codec<E> codec, Function<E, Lifecycle> function) {
		return overrideLifecycle(codec, function, function);
	}

	public static <K, V> ExtraCodecs.StrictUnboundedMapCodec<K, V> strictUnboundedMap(Codec<K> codec, Codec<V> codec2) {
		return new ExtraCodecs.StrictUnboundedMapCodec<>(codec, codec2);
	}

	public static <E> Codec<List<E>> compactListCodec(Codec<E> codec) {
		return compactListCodec(codec, codec.listOf());
	}

	public static <E> Codec<List<E>> compactListCodec(Codec<E> codec, Codec<List<E>> codec2) {
		return Codec.either(codec2, codec)
			.xmap(either -> either.map(list -> list, List::of), list -> list.size() == 1 ? Either.right(list.stream().findFirst().get()) : Either.left(list));
	}

	private static Codec<Integer> intRangeWithMessage(int i, int j, Function<Integer, String> function) {
		return Codec.INT
			.validate(
				integer -> integer.compareTo(i) >= 0 && integer.compareTo(j) <= 0 ? DataResult.success(integer) : DataResult.error(() -> (String)function.apply(integer))
			);
	}

	public static Codec<Integer> intRange(int i, int j) {
		return intRangeWithMessage(i, j, integer -> "Value must be within range [" + i + ";" + j + "]: " + integer);
	}

	private static Codec<Float> floatRangeMinInclusiveWithMessage(float f, float g, Function<Float, String> function) {
		return Codec.FLOAT
			.validate(
				float_ -> float_.compareTo(f) >= 0 && float_.compareTo(g) <= 0 ? DataResult.success(float_) : DataResult.error(() -> (String)function.apply(float_))
			);
	}

	private static Codec<Float> floatRangeMinExclusiveWithMessage(float f, float g, Function<Float, String> function) {
		return Codec.FLOAT
			.validate(
				float_ -> float_.compareTo(f) > 0 && float_.compareTo(g) <= 0 ? DataResult.success(float_) : DataResult.error(() -> (String)function.apply(float_))
			);
	}

	public static Codec<Float> floatRange(float f, float g) {
		return floatRangeMinInclusiveWithMessage(f, g, float_ -> "Value must be within range [" + f + ";" + g + "]: " + float_);
	}

	public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> codec) {
		return codec.validate(list -> list.isEmpty() ? DataResult.error(() -> "List must have contents") : DataResult.success(list));
	}



	public static <M extends Map<?, ?>> Codec<M> nonEmptyMap(Codec<M> codec) {
		return codec.validate(map -> map.isEmpty() ? DataResult.error(() -> "Map must have contents") : DataResult.success(map));
	}

	public static <E> MapCodec<E> retrieveContext(Function<DynamicOps<?>, DataResult<E>> function) {
		class ContextRetrievalCodec extends MapCodec<E> {
			@Override
			public <T> RecordBuilder<T> encode(E object, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
				return recordBuilder;
			}

			@Override
			public <T> DataResult<E> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
				return (DataResult<E>)function.apply(dynamicOps);
			}

			public String toString() {
				return "ContextRetrievalCodec[" + function + "]";
			}

			@Override
			public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
				return Stream.empty();
			}
		}

		return new ContextRetrievalCodec();
	}

	public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> function) {
		return collection -> {
			Iterator<E> iterator = collection.iterator();
			if (iterator.hasNext()) {
				T object = (T)function.apply(iterator.next());

				while (iterator.hasNext()) {
					E object2 = (E)iterator.next();
					T object3 = (T)function.apply(object2);
					if (object3 != object) {
						return DataResult.error(() -> "Mixed type list: element " + object2 + " had type " + object3 + ", but list is of type " + object);
					}
				}
			}

			return DataResult.success(collection, Lifecycle.stable());
		};
	}

	public static <A> Codec<A> catchDecoderException(Codec<A> codec) {
		return Codec.of(codec, new Decoder<A>() {
			@Override
			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicOps, T object) {
				try {
					return codec.decode(dynamicOps, object);
				} catch (Exception var4) {
					return DataResult.error(() -> "Caught exception decoding " + object + ": " + var4.getMessage());
				}
			}
		});
	}

	public static Codec<TemporalAccessor> temporalCodec(DateTimeFormatter dateTimeFormatter) {
		return Codec.STRING.comapFlatMap(string -> {
			try {
				return DataResult.success(dateTimeFormatter.parse(string));
			} catch (Exception var3) {
				return DataResult.error(var3::getMessage);
			}
		}, dateTimeFormatter::format);
	}

	public static MapCodec<OptionalLong> asOptionalLong(MapCodec<Optional<Long>> mapCodec) {
		return mapCodec.xmap(toOptionalLong, fromOptionalLong);
	}

	public static <K, V> Codec<Map<K, V>> sizeLimitedMap(Codec<Map<K, V>> codec, int i) {
		return codec.validate(
			map -> map.size() > i ? DataResult.error(() -> "Map is too long: " + map.size() + ", expected range [0-" + i + "]") : DataResult.success(map)
		);
	}

	public static <T> Codec<Object2BooleanMap<T>> object2BooleanMap(Codec<T> codec) {
		return Codec.unboundedMap(codec, Codec.BOOL).xmap(Object2BooleanOpenHashMap::new, Object2ObjectOpenHashMap::new);
	}

	@Deprecated
	public static <K, V> MapCodec<V> dispatchOptionalValue(
		String string, String string2, Codec<K> codec, Function<? super V, ? extends K> function, Function<? super K, ? extends Codec<? extends V>> function2
	) {
		return new MapCodec<V>() {
			@Override
			public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
				return Stream.of(dynamicOps.createString(string), dynamicOps.createString(string2));
			}

			@Override
			public <T> DataResult<V> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
				T object = mapLike.get(string);
				return object == null ? DataResult.error(() -> "Missing \"" + string + "\" in: " + mapLike) : codec.decode(dynamicOps, object).flatMap(pair -> {
					T objectx = (T)Objects.requireNonNullElseGet(mapLike.get(string2), dynamicOps::emptyMap);
					return (function2.apply(pair.getFirst())).decode(dynamicOps, objectx).map(Pair::getFirst);
				});
			}

			@Override
			public <T> RecordBuilder<T> encode(V object, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
				K object2 = (K)function.apply(object);
				recordBuilder.add(string, codec.encodeStart(dynamicOps, object2));
				DataResult<T> dataResult = this.encode((Codec)function2.apply(object2), object, dynamicOps);
				if (dataResult.result().isEmpty() || !Objects.equals(dataResult.result().get(), dynamicOps.emptyMap())) {
					recordBuilder.add(string2, dataResult);
				}

				return recordBuilder;
			}

			private <T, V2 extends V> DataResult<T> encode(Codec<V2> codec, V object, DynamicOps<T> dynamicOps) {
				return codec.encodeStart(dynamicOps, (V2)object);
			}
		};
	}

	public static <A> Codec<Optional<A>> optionalEmptyMap(Codec<A> codec) {
		return new Codec<Optional<A>>() {
			@Override
			public <T> DataResult<Pair<Optional<A>, T>> decode(DynamicOps<T> dynamicOps, T object) {
				return isEmptyMap(dynamicOps, object)
					? DataResult.success(Pair.of(Optional.empty(), object))
					: codec.decode(dynamicOps, object).map(pair -> pair.mapFirst(Optional::of));
			}

			private static <T> boolean isEmptyMap(DynamicOps<T> dynamicOps, T object) {
				Optional<MapLike<T>> optional = dynamicOps.getMap(object).result();
				return optional.isPresent() && ((MapLike)optional.get()).entries().findAny().isEmpty();
			}

			public <T> DataResult<T> encode(Optional<A> optional, DynamicOps<T> dynamicOps, T object) {
				return optional.isEmpty() ? DataResult.success(dynamicOps.emptyMap()) : codec.encode((A)optional.get(), dynamicOps, object);
			}
		};
	}

	@Deprecated
	public static <E extends Enum<E>> Codec<E> legacyEnum(Function<String, E> function) {
		return Codec.STRING.comapFlatMap(string -> {
			try {
				return DataResult.success(function.apply(string));
			} catch (IllegalArgumentException var3) {
				return DataResult.error(() -> "No value with id: " + string);
			}
		}, Enum::toString);
	}

	public static class LateBoundIdMapper<I, V> {
		private final BiMap<I, V> idToValue = HashBiMap.create();

		public Codec<V> codec(Codec<I> codec) {
			BiMap<V, I> biMap = this.idToValue.inverse();
			return ExtraCodecs.idResolverCodec(codec, this.idToValue::get, biMap::get);
		}

		public ExtraCodecs.LateBoundIdMapper<I, V> put(I object, V object2) {
			Objects.requireNonNull(object2, () -> "Value for " + object + " is null");
			this.idToValue.put(object, object2);
			return this;
		}
	}

	public record StrictUnboundedMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements Codec<Map<K, V>>, BaseMapCodec<K, V> {
		@Override
		public <T> DataResult<Map<K, V>> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
			Builder<K, V> builder = ImmutableMap.builder();

			for (Pair<T, T> pair : mapLike.entries().toList()) {
				DataResult<K> dataResult = this.keyCodec().parse(dynamicOps, pair.getFirst());
				DataResult<V> dataResult2 = this.elementCodec().parse(dynamicOps, pair.getSecond());
				DataResult<Pair<K, V>> dataResult3 = dataResult.apply2stable(Pair::of, dataResult2);
				Optional<Error<Pair<K, V>>> optional = dataResult3.error();
				if (optional.isPresent()) {
					String string = ((Error)optional.get()).message();
					return DataResult.error(() -> dataResult.result().isPresent() ? "Map entry '" + dataResult.result().get() + "' : " + string : string);
				}

				if (!dataResult3.result().isPresent()) {
					return DataResult.error(() -> "Empty or invalid map contents are not allowed");
				}

				Pair<K, V> pair2 = (Pair<K, V>)dataResult3.result().get();
				builder.put(pair2.getFirst(), pair2.getSecond());
			}

			Map<K, V> map = builder.build();
			return DataResult.success(map);
		}

		@Override
		public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> dynamicOps, T object) {
			return dynamicOps.getMap(object).setLifecycle(Lifecycle.stable()).flatMap(mapLike -> this.decode(dynamicOps, mapLike)).map(map -> Pair.of(map, object));
		}

		public <T> DataResult<T> encode(Map<K, V> map, DynamicOps<T> dynamicOps, T object) {
			return this.encode(map, dynamicOps, dynamicOps.mapBuilder()).build(object);
		}

		public String toString() {
			return "StrictUnboundedMapCodec[" + this.keyCodec + " -> " + this.elementCodec + "]";
		}
	}

	public record TagOrElementLocation(Identifier id, boolean tag) {
		public String toString() {
			return this.decoratedId();
		}

		private String decoratedId() {
			return this.tag ? "#" + this.id : this.id.toString();
		}
	}
}

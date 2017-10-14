package squidpony;

import java.util.HashMap;
import java.util.Map;

import squidpony.panel.IColoredString;
import squidpony.squidmath.RNG;

/**
 * How to manage colors, making sure that a color is allocated at most once.
 * 
 * <p>
 * If you aren't using squidlib's gdx part, you should use this interface (and
 * the {@link Skeleton} implementation), because it caches instances.
 * </p>
 * 
 * <p>
 * If you are using squidlib's gdx part, you should use this interface (and the
 * {@code SquidColorCenter} implementation) if:
 * 
 * <ul>
 * <li>You don't want to use preallocated instances (if you do, check out
 * {@code squidpony.squidgrid.gui.Colors})</li>
 * <li>You don't want to use named colors (if you do, check out
 * {@code com.badlogic.gdx.graphics.Colors})</li>
 * <li>You don't like libgdx's Color representation (components as floats
 * in-between 0 and 1) but prefer components within 0 (inclusive) and 256
 * (exclusive); and don't mind the overhead of switching the representations. My
 * personal opinion is that the overhead doesn't matter w.r.t other intensive
 * operations that we have in roguelikes (path finding).</li>
 * </ul>
 * 
 * @author smelC
 * 
 * @param <T>
 *            The concrete type of colors
 */
public interface IColorCenter<T> {

	/**
	 * @param red
	 *            The red component. For screen colors, in-between 0 (inclusive) and
	 *            256 (exclusive).
	 * @param green
	 *            The green component. For screen colors, in-between 0 (inclusive)
	 *            and 256 (exclusive).
	 * @param blue
	 *            The blue component. For screen colors, in-between 0 (inclusive)
	 *            and 256 (exclusive).
	 * @param opacity
	 *            The alpha component. In-between 0 (inclusive) and 256 (exclusive).
	 *            Larger values mean more opacity; 0 is clear.
	 * @return A possibly transparent color.
	 */
	T get(int red, int green, int blue, int opacity);

	/**
	 * @param red
	 *            The red component. For screen colors, in-between 0 (inclusive) and
	 *            256 (exclusive).
	 * @param green
	 *            The green component. For screen colors, in-between 0 (inclusive)
	 *            and 256 (exclusive).
	 * @param blue
	 *            The blue component. For screen colors, in-between 0 (inclusive)
	 *            and 256 (exclusive).
	 * @return An opaque color.
	 */
	T get(int red, int green, int blue);

	/**
	 * @return Opaque white.
	 */
	T getWhite();

	/**
	 * @return Opaque black.
	 */
	T getBlack();

	/**
	 * @return The fully transparent color.
	 */
	T getTransparent();

	/**
	 * @param rng
	 *            an RNG from SquidLib.
	 * @param opacity
	 *            The alpha component. In-between 0 (inclusive) and 256 (exclusive).
	 *            Larger values mean more opacity; 0 is clear.
	 * @return A random color, except for the alpha component.
	 */
	T getRandom(RNG rng, int opacity);

	/**
	 * @param c
	 *            a concrete color
	 * @return The red component. For screen colors, in-between 0 (inclusive) and
	 *         256 (exclusive).
	 */
	int getRed(T c);

	/**
	 * @param c
	 *            a concrete color
	 * @return The green component. For screen colors, in-between 0 (inclusive) and
	 *         256 (exclusive).
	 */
	int getGreen(T c);

	/**
	 * @param c
	 *            a concrete color
	 * @return The blue component. For screen colors, in-between 0 (inclusive) and
	 *         256 (exclusive).
	 */
	int getBlue(T c);

	/**
	 * @param c
	 *            a concrete color
	 * @return The alpha component. In-between 0 (inclusive) and 256 (exclusive).
	 */
	int getAlpha(T c);

	/**
	 * @param c
	 * @return The color that {@code this} shows when {@code c} is requested. May be
	 *         {@code c} itself.
	 */
	T filter(T c);

	/**
	 * @param ics
	 * @return {@code ics} filtered according to {@link #filter(Object)}. May be
	 *         {@code ics} itself if unchanged.
	 */
	IColoredString<T> filter(IColoredString<T> ics);

	/**
	 * Gets a copy of t and modifies it to make a shade of gray with the same
	 * brightness. The doAlpha parameter causes the alpha to be considered in the
	 * calculation of brightness and also changes the returned alpha of the color.
	 * Not related to reified types or any usage of "reify."
	 * 
	 * @param t
	 *            a T to copy; only the copy will be modified
	 * @param doAlpha
	 *            Whether to include (and hereby change) the alpha component.
	 * @return A monochromatic variation of {@code t}.
	 */
	T greify(/* @Nullable */ T t, boolean doAlpha);

	/**
	 * A skeletal implementation of {@link IColorCenter}.
	 * 
	 * @author smelC
	 * 
	 * @param <T>
	 *            a concrete color type
	 */
	abstract class Skeleton<T> implements IColorCenter<T> {

		private final Map<Long, T> cache = new HashMap<>(256);

		protected /* Nullable */ IFilter<T> filter;

		/**
		 * @param filter
		 *            The filter to use, or {@code null} for no filter.
		 */
		protected Skeleton(/* Nullable */ IFilter<T> filter) {
			this.filter = filter;
		}

		@Override
		public T get(int red, int green, int blue, int opacity) {
			final Long value = getUniqueIdentifier((short) red, (short) green, (short) blue, (short) opacity);
			T t = cache.get(value);
			if (t == null) {
				/* Miss */
				t = create(red, green, blue, opacity);
				/* Put in cache */
				cache.put(value, t);
			}
			return t;
		}

		@Override
		public T get(int red, int green, int blue) {
			return get(red, green, blue, 255);
		}

		@Override
		public final T getWhite() {
			return get(255, 255, 255, 255);
		}

		@Override
		public final T getBlack() {
			return get(0, 0, 0, 255);
		}

		@Override
		public final T getTransparent() {
			return get(0, 0, 0, 0);
		}

		@Override
		public final T getRandom(RNG rng, int opacity) {
			return get(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256), opacity);
		}

		@Override
		public T filter(T c) {
			return c == null ? c : get(getRed(c), getGreen(c), getBlue(c), getAlpha(c));
		}

		@Override
		public IColoredString<T> filter(IColoredString<T> ics) {
			/*
			 * It is common not to have a filter or to have the identity one. To avoid
			 * always copying strings in this case, we first roll over the string to see if
			 * there'll be a change.
			 * 
			 * This is clearly a subjective design choice but my industry experience is that
			 * minimizing allocations is the thing to do for performances, hence I prefer
			 * iterating twice to do that.
			 */
			boolean change = false;
			for (IColoredString.Bucket<T> bucket : ics) {
				final T in = bucket.getColor();
				if (in == null)
					continue;
				final T out = filter(in);
				if (in != out) {
					change = true;
					break;
				}
			}

			if (change) {
				final IColoredString<T> result = IColoredString.Impl.create();
				for (IColoredString.Bucket<T> bucket : ics)
					result.append(bucket.getText(), filter(bucket.getColor()));
				return result;
			} else
				/* Only one allocation: the iterator, yay \o/ */
				return ics;
		}

		/**
		 * Gets a copy of t and modifies it to make a shade of gray with the same
		 * brightness. The doAlpha parameter causes the alpha to be considered in the
		 * calculation of brightness and also changes the returned alpha of the color.
		 * Not related to reified types or any usage of "reify."
		 * 
		 * @param t
		 *            a T to copy; only the copy will be modified
		 * @param doAlpha
		 *            Whether to include (and hereby change) the alpha component.
		 * @return A monochromatic variation of {@code t}.
		 */
		@Override
		public T greify(T t, boolean doAlpha) {
			if (t == null)
				/* Cannot do */
				return null;
			final int red = getRed(t);
			final int green = getGreen(t);
			final int blue = getBlue(t);
			final int alpha = getAlpha(t);
			final int rgb = red + green + blue;
			final int mean;
			final int newAlpha;
			if (doAlpha) {
				mean = (rgb + alpha) / 4;
				newAlpha = mean;
			} else {
				mean = rgb / 3;
				/* No change */
				newAlpha = alpha;
			}
			return get(mean, mean, mean, newAlpha);
		}

		/**
		 * Create a concrete instance of the color type given as a type parameter.
		 * That's the place to use the {@link #filter}.
		 * 
		 * @param red
		 *            the red component of the desired color
		 * @param green
		 *            the green component of the desired color
		 * @param blue
		 *            the blue component of the desired color
		 * @param opacity
		 *            the alpha component or opacity of the desired color
		 * @return a fresh instance of the concrete color type
		 */
		protected abstract T create(int red, int green, int blue, int opacity);

		private long getUniqueIdentifier(short r, short g, short b, short a) {
			return ((a & 0xffL) << 48) | ((r & 0xffffL) << 32) | ((g & 0xffffL) << 16) | (b & 0xffffL);
		}

	}
}

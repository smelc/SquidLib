package squidpony.squidgrid.gui.gdx;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;

import squidpony.IColorCenter;
import squidpony.IFilter;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.StatefulRNG;

/**
 * A concrete implementation of {@link IColorCenter} for libgdx's
 * {@link com.badlogic.gdx.graphics.Color}. Supports filtering any colors that
 * this creates using a {@code Filter}, such as one from {@link Filters}.
 *
 * @author smelC
 * @author Tommy Ettinger
 * @see SColor Another way to obtain colors by using pre-allocated (and named)
 *      instances.
 */
public class SquidColorCenter extends IColorCenter.Skeleton<Color> {

	/**
	 * A fresh filter-less color center.
	 */
	public SquidColorCenter() {
		this(null);
	}

	/**
	 * A fresh filtered color center.
	 * 
	 * @param filterEffect
	 *            The filter to use.
	 */
	public SquidColorCenter(/* Nullable */IFilter<Color> filterEffect) {
		super(filterEffect);
	}

	@Override
	protected Color create(int red, int green, int blue, int opacity) {
		if (filter == null)
			/* No filtering */
			return new Color(red / 255f, green / 255f, blue / 255f, opacity / 255f);
		else
			/* Some filtering */
			return filter.alter(red / 255f, green / 255f, blue / 255f, opacity / 255f);
	}

	@Override
	public Color filter(Color c) {
		if (c == null)
			return Color.CLEAR;
		else
			return super.filter(c);
	}

	public Color get(long c) {
		return get((int) ((c >> 24) & 0xff), (int) ((c >> 16) & 0xff), (int) ((c >> 8) & 0xff), (int) (c & 0xff));
	}

	public Color get(float r, float g, float b, float a) {
		return get(Math.round(255 * r), Math.round(255 * g), Math.round(255 * b), Math.round(255 * a));
	}

	@Override
	public int getRed(Color c) {
		return Math.round(c.r * 255f);
	}

	@Override
	public int getGreen(Color c) {
		return Math.round(c.g * 255f);
	}

	@Override
	public int getBlue(Color c) {
		return Math.round(c.b * 255f);
	}

	@Override
	public int getAlpha(Color c) {
		return Math.round(c.a * 255f);
	}

	public static int encode(Color color) {
		if (color == null)
			return 0;
		return (Math.round(color.r * 255.0f) << 24) | (Math.round(color.g * 255.0f) << 16)
				| (Math.round(color.b * 255.0f) << 8) | Math.round(color.a * 255.0f);
	}

	/**
	 * Gets a modified copy of color as if it is lit with a colored light source.
	 * 
	 * @param color
	 *            the color to shine the light on
	 * @param light
	 *            the color of the light source
	 * @return a copy of the Color color that factors in the lighting of the Color
	 *         light.
	 */
	public Color lightWith(Color color, Color light) {
		return filter(color.cpy().mul(light));
	}

	/**
	 * Gets a fully random color that is only required to be opaque.
	 * 
	 * @return a random Color
	 */
	public Color random() {
		StatefulRNG rng = DefaultResources.getGuiRandom();
		return get(rng.nextFloat(), rng.nextFloat(), rng.nextFloat(), 1f);
	}

	/**
	 * Finds a gradient with the specified number of steps going from fromColor to
	 * toColor, both included in the gradient. This does not typically take a direct
	 * path on its way between fromColor and toColor, and is useful to generate a
	 * wide variety of colors that can be confined to a rough amount of maximum
	 * difference by choosing values for fromColor and toColor that are more
	 * similar. <br>
	 * Try using colors for fromColor and toColor that have different r, g, and b
	 * values, such as gray and white, then compare to colors that don't differ on,
	 * for example, r, such as bright red and pink. In the first case, red, green,
	 * blue, and many other colors will be generated if there are enough steps; in
	 * the second case, red will be at the same level in all generated colors (very
	 * high, so no pure blue or pure green, but purple and yellow are possible).
	 * This should help illustrate how this chooses how far to "zig-zag" off the
	 * straight-line path.
	 * 
	 * @param fromColor
	 *            the color to start with, included in the gradient
	 * @param toColor
	 *            the color to end on, included in the gradient
	 * @param steps
	 *            the number of elements to use in the gradient; ideally no greater
	 *            than 345 to avoid duplicates
	 * @return an ArrayList composed of the zig-zag steps from fromColor to toColor,
	 *         with length equal to steps
	 */
	public ArrayList<Color> zigzagGradient(Color fromColor, Color toColor, int steps) {
		ArrayList<Color> colors = new ArrayList<>((steps > 1) ? steps : 1);
		colors.add(filter(fromColor));
		if (steps < 2)
			return colors;
		float dr = toColor.r - fromColor.r, dg = toColor.g - fromColor.g, db = toColor.b - fromColor.b, a = fromColor.a,
				cr, cg, cb;
		int decoded;
		for (float i = 1; i < steps; i++) {
			// 345 happens to be the distance on our 3D Hilbert curve that corresponds to
			// (7,7,7).
			decoded = Math.round(345 * (i / (steps - 1)));
			cr = (CoordPacker.hilbert3X[decoded] / 7f) * dr + fromColor.r;
			cg = (CoordPacker.hilbert3Y[decoded] / 7f) * dg + fromColor.g;
			cb = (CoordPacker.hilbert3Z[decoded] / 7f) * db + fromColor.b;
			colors.add(get(cr, cg, cb, a));
		}
		return colors;
	}

	@Override
	public String toString() {
		return "SquidColorCenter{" + "filter=" + (filter == null ? "null" : filter.getClass().getSimpleName()) + '}';
	}
}

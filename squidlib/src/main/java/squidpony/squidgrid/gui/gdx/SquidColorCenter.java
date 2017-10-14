package squidpony.squidgrid.gui.gdx;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;

import squidpony.IColorCenter;
import squidpony.IFilter;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.StatefulRNG;

/**
 * A concrete implementation of {@link IColorCenter} for libgdx's
 * {@link com.badlogic.gdx.graphics.Color}. Supports filtering any colors that
 * this creates using a {@link Filter}, such as one from {@link Filters}.
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

	/**
	 * Gets the linear interpolation from Color start to Color end, changing by the
	 * fraction given by change.
	 * 
	 * @param start
	 *            the initial Color
	 * @param end
	 *            the "target" color
	 * @param change
	 *            the degree to change closer to end; a change of 0.0f produces
	 *            start, 1.0f produces end
	 * @return a new Color
	 */
	@Override
	public Color lerp(Color start, Color end, float change) {
		if (start == null || end == null)
			return Color.CLEAR;
		return get(start.r + change * (end.r - start.r), start.g + change * (end.g - start.g),
				start.b + change * (end.b - start.b), start.a + change * (end.a - start.a));
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
	 * Lightens a color by degree and returns the new color (mixed with white).
	 * 
	 * @param color
	 *            the color to lighten
	 * @param degree
	 *            a float between 0.0f and 1.0f; more makes it lighter
	 * @return the lightened (and if a filter is used, also filtered) new color
	 */
	public Color light(Color color, float degree) {
		return lerp(color, Color.WHITE, degree);
	}

	/**
	 * Lightens a color slightly and returns the new color (10% mix with white).
	 * 
	 * @param color
	 *            the color to lighten
	 * @return the lightened (and if a filter is used, also filtered) new color
	 */
	public Color light(Color color) {
		return lerp(color, Color.WHITE, 0.1f);
	}

	/**
	 * Lightens a color significantly and returns the new color (30% mix with
	 * white).
	 * 
	 * @param color
	 *            the color to lighten
	 * @return the lightened (and if a filter is used, also filtered) new color
	 */
	public Color lighter(Color color) {
		return lerp(color, Color.WHITE, 0.3f);
	}

	/**
	 * Lightens a color massively and returns the new color (70% mix with white).
	 * 
	 * @param color
	 *            the color to lighten
	 * @return the lightened (and if a filter is used, also filtered) new color
	 */
	public Color lightest(Color color) {
		return lerp(color, Color.WHITE, 0.7f);
	}

	/**
	 * Darkens a color by the specified degree and returns the new color (mixed with
	 * black).
	 * 
	 * @param color
	 *            the color to darken
	 * @param degree
	 *            a float between 0.0f and 1.0f; more makes it darker
	 * @return the darkened (and if a filter is used, also filtered) new color
	 */
	public Color dim(Color color, float degree) {
		return lerp(color, Color.BLACK, degree);
	}

	/**
	 * Darkens a color slightly and returns the new color (10% mix with black).
	 * 
	 * @param color
	 *            the color to darken
	 * @return the darkened (and if a filter is used, also filtered) new color
	 */
	public Color dim(Color color) {
		return lerp(color, Color.BLACK, 0.1f);
	}

	/**
	 * Darkens a color significantly and returns the new color (30% mix with black).
	 * 
	 * @param color
	 *            the color to darken
	 * @return the darkened (and if a filter is used, also filtered) new color
	 */
	public Color dimmer(Color color) {
		return lerp(color, Color.BLACK, 0.3f);
	}

	/**
	 * Darkens a color massively and returns the new color (70% mix with black).
	 * 
	 * @param color
	 *            the color to darken
	 * @return the darkened (and if a filter is used, also filtered) new color
	 */
	public Color dimmest(Color color) {
		return lerp(color, Color.BLACK, 0.7f);
	}

	/**
	 * Gets a fully-desaturated version of the given color (keeping its brightness,
	 * but making it grayscale).
	 * 
	 * @param color
	 *            the color to desaturate (will not be modified)
	 * @return the grayscale version of color
	 */
	@Override
	public Color desaturated(Color color) {
		float f = color.r * 0.299f + color.g * 0.587f + color.b * 0.114f;
		return get(f, f, f, color.a);
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
	 * Blends a color with a random (opaque) color by a factor of 10% random.
	 * 
	 * @param color
	 *            the color to randomize
	 * @return the randomized (and if a filter is used, also filtered) new color
	 */
	public Color randomize(Color color) {
		return lerp(color, random(), 0.1f);
	}

	/**
	 * Blends a color with a random (opaque) color by a factor of 30% random.
	 * 
	 * @param color
	 *            the color to randomize
	 * @return the randomized (and if a filter is used, also filtered) new color
	 */
	public Color randomizeMore(Color color) {
		return lerp(color, random(), 0.3f);
	}

	/**
	 * Blends a color with a random (opaque) color by a factor of 70% random.
	 * 
	 * @param color
	 *            the color to randomize
	 * @return the randomized (and if a filter is used, also filtered) new color
	 */
	public Color randomizeMost(Color color) {
		return lerp(color, random(), 0.7f);
	}

	/**
	 * Blends the colors A and B by a random degree.
	 * 
	 * @param a
	 *            a color to mix in
	 * @param b
	 *            another color to mix in
	 * @return a random blend of a and b.
	 */
	public Color randomBlend(Color a, Color b) {
		return lerp(a, b, DefaultResources.getGuiRandom().nextFloat());
	}

	/**
	 * Finds a 16-step gradient going from fromColor to toColor, both included in
	 * the gradient.
	 * 
	 * @param fromColor
	 *            the color to start with, included in the gradient
	 * @param toColor
	 *            the color to end on, included in the gradient
	 * @return a 16-element ArrayList composed of the blending steps from fromColor
	 *         to toColor
	 */
	public ArrayList<Color> gradient(Color fromColor, Color toColor) {
		ArrayList<Color> colors = new ArrayList<>(16);
		for (int i = 0; i < 16; i++) {
			colors.add(lerp(fromColor, toColor, i / 15f));
		}
		return colors;
	}

	/**
	 * Finds a gradient with the specified number of steps going from fromColor to
	 * toColor, both included in the gradient.
	 * 
	 * @param fromColor
	 *            the color to start with, included in the gradient
	 * @param toColor
	 *            the color to end on, included in the gradient
	 * @param steps
	 *            the number of elements to use in the gradient
	 * @return an ArrayList composed of the blending steps from fromColor to
	 *         toColor, with length equal to steps
	 */
	public ArrayList<Color> gradient(Color fromColor, Color toColor, int steps) {
		return gradient(fromColor, toColor, steps, Interpolation.linear);
	}

	/**
	 * Finds a gradient with the specified number of steps going from fromColor to
	 * midColor, then midColor to (possibly) fromColor, with both included in the
	 * gradient but fromColor only repeated at the end if the number of steps is
	 * odd.
	 * 
	 * @param fromColor
	 *            the color to start with (and end with, if steps is an odd number),
	 *            included in the gradient
	 * @param midColor
	 *            the color to use in the middle of the loop, included in the
	 *            gradient
	 * @param steps
	 *            the number of elements to use in the gradient, will be at least 3
	 * @return an ArrayList composed of the blending steps from fromColor to
	 *         midColor to fromColor again, with length equal to steps
	 */
	public ArrayList<Color> loopingGradient(Color fromColor, Color midColor, int steps) {
		return loopingGradient(fromColor, midColor, steps, Interpolation.linear);
	}

	/**
	 * Finds a gradient with the specified number of steps going from fromColor to
	 * toColor, both included in the gradient. The interpolation argument can be
	 * used to make the color stay close to fromColor and/or toColor longer than it
	 * would normally, or shorter if the middle colors are desirable.
	 * 
	 * @param fromColor
	 *            the color to start with, included in the gradient
	 * @param toColor
	 *            the color to end on, included in the gradient
	 * @param steps
	 *            the number of elements to use in the gradient
	 * @param interpolation
	 *            a libGDX Interpolation that defines how quickly the color changes
	 *            during the transition
	 * @return an ArrayList composed of the blending steps from fromColor to
	 *         toColor, with length equal to steps
	 */
	public ArrayList<Color> gradient(Color fromColor, Color toColor, int steps, Interpolation interpolation) {
		ArrayList<Color> colors = new ArrayList<>((steps > 1) ? steps : 1);
		colors.add(filter(fromColor));
		if (steps < 2)
			return colors;
		for (float i = 1; i < steps; i++) {
			colors.add(lerp(fromColor, toColor, interpolation.apply(i / (steps - 1))));
		}
		return colors;
	}

	/**
	 * Finds a gradient with the specified number of steps going from fromColor to
	 * midColor, then midColor to (possibly) fromColor, with both included in the
	 * gradient but fromColor only repeated at the end if the number of steps is
	 * odd. The interpolation argument can be used to make the color linger for a
	 * while with colors close to fromColor or midColor, or to do the opposite and
	 * quickly change from one and spend more time in the middle.
	 * 
	 * @param fromColor
	 *            the color to start with (and end with, if steps is an odd number),
	 *            included in the gradient
	 * @param midColor
	 *            the color to use in the middle of the loop, included in the
	 *            gradient
	 * @param steps
	 *            the number of elements to use in the gradient, will be at least 3
	 * @param interpolation
	 *            a libGDX Interpolation that defines how quickly the color changes
	 *            at the start and end of each transition, both from fromColor to
	 *            midColor as well as back to fromColor
	 * @return an ArrayList composed of the blending steps from fromColor to
	 *         midColor to fromColor again, with length equal to steps
	 */
	public ArrayList<Color> loopingGradient(Color fromColor, Color midColor, int steps, Interpolation interpolation) {
		ArrayList<Color> colors = new ArrayList<>((steps > 3) ? steps : 3);
		colors.add(filter(fromColor));
		for (float i = 1; i < steps / 2; i++) {
			colors.add(lerp(fromColor, midColor, interpolation.apply(i / (steps / 2))));
		}
		for (float i = 0, c = steps / 2; c < steps; i++, c++) {
			colors.add(lerp(midColor, fromColor, interpolation.apply(i / (steps / 2))));
		}
		return colors;
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

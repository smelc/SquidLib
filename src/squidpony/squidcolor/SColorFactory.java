package squidpony.squidcolor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import squidpony.squidmath.Bresenham;
import squidpony.squidmath.Point3D;
import squidpony.squidmath.RNG;

/**
 * Provides utilities for working with colors as well as caching operations for
 * color creation.
 *
 * All returned SColor objects are cached so multiple requests for the same
 * SColor will not create duplicate long term objects.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class SColorFactory {

    private static RNG rng = new RNG();
    private static Map<Integer, SColor> colorBag = new HashMap<Integer, SColor>();
    private static Map<String, ArrayList<SColor>> pallets = new HashMap<String, ArrayList<SColor>>();

    /**
     * Prevents any instances from being created.
     */
    private SColorFactory() {
    }

    /**
     * Utility method to blend the two colors by the amount passed in as the
     * coefficient.
     *
     * @param a
     * @param b
     * @param coef
     * @return
     */
    private static int blend(int a, int b, double coef) {
        coef = Math.min(1, coef);
        coef = Math.max(0, coef);
        return (int) (a + (b - a) * coef);
    }

    /**
     * Returns an SColor that is the given distance from the first color to the
     * second color.
     *
     * @param color1 The first color
     * @param color2 The second color
     * @param coef The percent towards the second color, as 0.0 to 1.0
     * @return
     */
    public static SColor blend(SColor color1, SColor color2, double coef) {
        return getSColor(blend(color1.getRed(), color2.getRed(), coef),
                blend(color1.getGreen(), color2.getGreen(), coef),
                blend(color1.getBlue(), color2.getBlue(), coef));
    }

    /**
     * Returns an SColor that is randomly chosen from the color line between the
     * two provided colors from the two provided points.
     *
     * @param color1
     * @param color2
     * @param min The minimum percent towards the second color, as 0.0 to 1.0
     * @param max The maximum percent towards the second color, as 0.0 to 1.0
     * @return
     */
    public static SColor randomBlend(SColor color1, SColor color2, double min, double max) {
        return blend(color1, color2, rng.between(min, max));
    }

    /**
     * Clears the backing cache.
     *
     * Should only be used if an extreme number of colors are being created and
     * then not reused, such as when blending different colors in different
     * areas that will not be revisited.
     */
    public static void emptyCache() {
        colorBag = new HashMap<Integer, SColor>();
    }

    /**
     * Returns the cached color that matches the desired rgb value.
     *
     * If the color is not already in the cache, it is created and added to the
     * cache.
     *
     * @param rgb
     * @return
     */
    public static SColor getSColor(int rgb) {
        if (colorBag.containsKey(rgb)) {
            return colorBag.get(rgb);
        } else {
            SColor color = new SColor(rgb);
            colorBag.put(rgb, color);
            return color;
        }
    }

    /**
     * Returns an SColor with the given values, with those values clamped
     * between 0 and 255.
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static SColor getSColor(int r, int g, int b) {
        r = Math.min(r, 255);
        r = Math.max(r, 0);
        g = Math.min(g, 255);
        g = Math.max(g, 0);
        b = Math.min(b, 255);
        b = Math.max(b, 0);
        return getSColor(r * 256 * 256 + g * 256 + b);
    }

    /**
     * Returns an SColor that is a slightly dimmer version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public static SColor dim(SColor color) {
        return blend(color, SColor.BLACK, 0.1);
    }

    /**
     * Returns an SColor that is a somewhat dimmer version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public static SColor dimmer(SColor color) {
        return blend(color, SColor.BLACK, 0.3);
    }

    /**
     * Returns an SColor that is a lot darker version of the provided color.
     *
     * @param color
     * @return
     */
    public static SColor dimmest(SColor color) {
        return blend(color, SColor.BLACK, 0.7);
    }

    /**
     * Returns an SColor that is a slightly lighter version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public static SColor light(SColor color) {
        return blend(color, SColor.WHITE, 0.1);
    }

    /**
     * Returns an SColor that is a somewhat lighter version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public static SColor lighter(SColor color) {
        return blend(color, SColor.WHITE, 0.3);
    }

    /**
     * Returns an SColor that is a lot lighter version of the provided color.
     *
     * @param color
     * @return
     */
    public static SColor lightest(SColor color) {
        return blend(color, SColor.WHITE, 0.6);
    }

    /**
     * Returns an SColor that is the fully desaturated (greyscale) version of
     * the provided color.
     *
     * @param color
     * @return
     */
    public static SColor desaturated(SColor color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int average = (int) (r * 0.299 + g * 0.587 + b * 0.114);

        return getSColor(average, average, average);
    }

    /**
     * Returns an SColor that is the version of the provided color desaturated
     * the given amount.
     *
     * @param color
     * @param percent The percent to desaturate, from 0.0 for none to 1.0 for
     * fully desaturated
     * @return
     */
    public static SColor desaturate(SColor color, double percent) {
        return blend(color, desaturated(color), percent);
    }

    /**
     * Returns a list of colors starting at the first color and moving to the
     * second color. The end point colors are included in the list.
     *
     * @param color1
     * @param color2
     * @return
     */
    public static ArrayList<SColor> getGradient(SColor color1, SColor color2) {
        String name = palletNamer(color2, color2);
        if (pallets.containsKey(name)) {
            return pallets.get(name);
        }

        //get the gradient
        Queue<Point3D> gradient = Bresenham.line3D(scolorToCoord3D(color1), scolorToCoord3D(color2));
        ArrayList<SColor> ret = new ArrayList<SColor>();
        for (Point3D coord : gradient) {
            ret.add(coord3DToSColor(coord));
        }

        pallets.put(name, ret);
        return ret;
    }

    /**
     * Returns the pallet associate with the provided name, or null if there is
     * no such pallet.
     *
     * @param name
     * @return
     */
    public static ArrayList<SColor> getPallet(String name) {
        return pallets.get(name);
    }

    /**
     * Returns the SColor that is the provided percent towards the end of the
     * gradient.
     *
     * @param name
     * @param percent
     * @return
     */
    public static SColor getFromGradient(String name, float percent) {
        ArrayList<SColor> list = pallets.get(name);
        if (list == null) {
            return null;
        }

        int index = (int) (list.size() * percent);//find the index that's the given percent into the gradient

        return list.get(index);
    }

    /**
     * Places the pallet into the cache, along with each of the member colors.
     *
     * @param name
     * @param pallet
     */
    public static void addPallet(String name, ArrayList<SColor> pallet) {
        ArrayList<SColor> temp = new ArrayList<SColor>();

        //make sure all the colors in the pallet are also in the general color cache
        for (SColor sc : pallet) {
            temp.add(getSColor(sc.getRGB()));
        }

        pallets.put(name, temp);
    }

    /**
     * Converts the provided color into a three dimensional coordinate point for
     * use in the Bresenham algorithms.
     *
     * @param color
     * @return
     */
    private static Point3D scolorToCoord3D(SColor color) {
        return new Point3D(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Converts the provided three dimensional coordinate into a color for use
     * in the Bresenham algorithms.
     *
     * @param coord
     * @return
     */
    private static SColor coord3DToSColor(Point3D coord) {
        return getSColor(coord.x, coord.y, coord.z);
    }

    private static String palletNamer(SColor color1, SColor color2) {
        return color1.getName() + " to " + color2.getName();
    }
}

package squidpony;

import squidpony.squidmath.Coord;

/**
 * Static methods useful to be GWT-compatible, and also methods useful for filling gaps in Java's support for arrays.
 * You can think of the purpose of this class as "GWT, and Compatibility". There's a replacement for a Math method that
 * isn't available on GWT, a quick way to get the first element in an Iterable, copying, inserting, and filling methods
 * for 2D arrays of primitive types (char, int, double, and boolean), and also a method to easily clone a Coord array.
 * 
 * @author smelC
 * @author Tommy Ettinger
 */
public class GwtCompatibility {

	/**
     * Gets an exact copy of an array of Coord. References are shared, which should be the case for all usage of Coord
     * since they are immutable and thus don't need multiple variants on a Coord from the pool.
	 * @param input an array of Coord to copy
	 * @return A clone of {@code input}.
	 */
	public static Coord[] cloneCoords(Coord[] input) {
		final Coord[] result = new Coord[input.length];
        //System.arraycopy, despite being cumbersome, is the fastest way to copy an array on the JVM.
        System.arraycopy(input, 0, result, 0, input.length);
		return result;
	}

	/**
     * A replacement for Math.IEEEremainder, just because Math.IEEEremainder isn't GWT-compatible.
     * Gets the remainder of op / d, which can be negative if any parameter is negative.
	 * @param op the operand/dividend
	 * @param d the divisor
	 * @return The remainder of {@code op / d}, as a double; can be negative
	 */
	/* smelC: because Math.IEEEremainder isn't GWT compatible */
	public static double IEEEremainder(double op, double d) {
		final double div = Math.round(op / d);
		return op - (div * d);
	}

    /**
     * Stupidly simple convenience method that produces a range from 0 to end, not including end, as an int array.
     * @param end the exclusive upper bound on the range
     * @return the range of ints as an int array
     */
    public static int[] range(int end)
    {
        if(end <= 0)
            return new int[0];
        int[] r = new int[end];
        for (int i = 0; i < end; i++) {
            r[i] = i;
        }
        return r;
    }

}

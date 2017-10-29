package squidpony.squidmath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import squidpony.GwtCompatibility;
import squidpony.annotation.Beta;
import squidpony.squidgrid.Radius;

/**
 * Region encoding of 64x64 areas as a number of long arrays; uncompressed
 * (fatty), but fast (greased lightning). Created by Tommy Ettinger on
 * 6/24/2016.
 */
@Beta
public class GreasedRegion implements Serializable {
	private static final long serialVersionUID = 0;
	private static final SobolQRNG sobol = new SobolQRNG(2);

	public long[] data;
	public int height;
	public int width;
	private int ySections;
	private long yEndMask;

	public GreasedRegion() {
		width = 64;
		height = 64;
		ySections = 1;
		yEndMask = -1L;
		data = new long[64];
	}

	public GreasedRegion(boolean[][] bits) {
		width = bits.length;
		height = bits[0].length;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (bits[x][y])
					data[x * ySections + (y >> 6)] |= 1L << (y & 63);
			}
		}
	}

	public GreasedRegion refill(boolean[][] map) {
		if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					data[x * ySections + (y >> 6)] |= (map[x][y] ? 1L : 0L) << (y & 63);
				}
			}
			return this;
		} else {
			width = (map == null) ? 0 : map.length;
			height = (map == null || map.length <= 0) ? 0 : map[0].length;
			ySections = (height + 63) >> 6;
			yEndMask = -1L >>> (64 - (height & 63));
			data = new long[width * ySections];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (map[x][y])
						data[x * ySections + (y >> 6)] |= 1L << (y & 63);
				}
			}
			return this;
		}
	}

	public GreasedRegion(char[][] map, char yes) {
		width = map.length;
		height = map[0].length;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (map[x][y] == yes)
					data[x * ySections + (y >> 6)] |= 1L << (y & 63);
			}
		}
	}

	public GreasedRegion refill(char[][] map, char yes) {
		if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					data[x * ySections + (y >> 6)] |= ((map[x][y] == yes) ? 1L : 0L) << (y & 63);
				}
			}
			return this;
		} else {
			width = (map == null) ? 0 : map.length;
			height = (map == null || map.length <= 0) ? 0 : map[0].length;
			ySections = (height + 63) >> 6;
			yEndMask = -1L >>> (64 - (height & 63));
			data = new long[width * ySections];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (map[x][y] == yes)
						data[x * ySections + (y >> 6)] |= 1L << (y & 63);
				}
			}
			return this;
		}
	}

	public GreasedRegion(int[][] map, int yes) {
		width = map.length;
		height = map[0].length;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (map[x][y] == yes)
					data[x * ySections + (y >> 6)] |= 1L << (y & 63);
			}
		}
	}

	public GreasedRegion refill(int[][] map, int yes) {
		if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					data[x * ySections + (y >> 6)] |= ((map[x][y] == yes) ? 1L : 0L) << (y & 63);
				}
			}
			return this;
		} else {
			width = (map == null) ? 0 : map.length;
			height = (map == null || map.length <= 0) ? 0 : map[0].length;
			ySections = (height + 63) >> 6;
			yEndMask = -1L >>> (64 - (height & 63));
			data = new long[width * ySections];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (map[x][y] == yes)
						data[x * ySections + (y >> 6)] |= 1L << (y & 63);
				}
			}
			return this;
		}
	}

	/**
	 * Constructs this GreasedRegion using an int[][], treating cells as on if they
	 * are greater than or equal to lower and less than upper, or off otherwise.
	 * 
	 * @param map
	 *            an int[][] that should have some ints between lower and upper
	 * @param lower
	 *            lower bound, inclusive; all on cells will have values in map that
	 *            are at least equal to lower
	 * @param upper
	 *            upper bound, exclusive; all on cells will have values in map that
	 *            are less than upper
	 */
	public GreasedRegion(int[][] map, int lower, int upper) {
		width = map.length;
		height = map[0].length;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		int[] column;
		for (int x = 0; x < width; x++) {
			column = map[x];
			for (int y = 0; y < height; y++) {
				if (column[y] >= lower && column[y] < upper)
					data[x * ySections + (y >> 6)] |= 1L << (y & 63);
			}
		}
	}

	public GreasedRegion refill(int[][] map, int lower, int upper) {
		if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
			int[] column;
			for (int x = 0; x < width; x++) {
				column = map[x];
				for (int y = 0; y < height; y++) {
					data[x * ySections + (y >> 6)] |= ((column[y] >= lower && column[y] < upper) ? 1L : 0L) << (y & 63);
				}
			}
			return this;
		} else {
			width = (map == null) ? 0 : map.length;
			height = (map == null || map.length <= 0) ? 0 : map[0].length;
			ySections = (height + 63) >> 6;
			yEndMask = -1L >>> (64 - (height & 63));
			data = new long[width * ySections];
			int[] column;
			for (int x = 0; x < width; x++) {
				column = map[x];
				for (int y = 0; y < height; y++) {
					if (column[y] >= lower && column[y] < upper)
						data[x * ySections + (y >> 6)] |= 1L << (y & 63);
				}
			}
			return this;
		}
	}

	/**
	 * Constructs this GreasedRegion using a short[][], treating cells as on if they
	 * are greater than or equal to lower and less than upper, or off otherwise.
	 * 
	 * @param map
	 *            a short[][] that should have some shorts between lower and upper
	 * @param lower
	 *            lower bound, inclusive; all on cells will have values in map that
	 *            are at least equal to lower
	 * @param upper
	 *            upper bound, exclusive; all on cells will have values in map that
	 *            are less than upper
	 */
	public GreasedRegion(short[][] map, int lower, int upper) {
		width = map.length;
		height = map[0].length;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		short[] column;
		for (int x = 0; x < width; x++) {
			column = map[x];
			for (int y = 0; y < height; y++) {
				if (column[y] >= lower && column[y] < upper)
					data[x * ySections + (y >> 6)] |= 1L << (y & 63);
			}
		}
	}

	public GreasedRegion refill(short[][] map, int lower, int upper) {
		if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
			short[] column;
			for (int x = 0; x < width; x++) {
				column = map[x];
				for (int y = 0; y < height; y++) {
					data[x * ySections + (y >> 6)] |= ((column[y] >= lower && column[y] < upper) ? 1L : 0L) << (y & 63);
				}
			}
			return this;
		} else {
			width = (map == null) ? 0 : map.length;
			height = (map == null || map.length <= 0) ? 0 : map[0].length;
			ySections = (height + 63) >> 6;
			yEndMask = -1L >>> (64 - (height & 63));
			data = new long[width * ySections];
			short[] column;
			for (int x = 0; x < width; x++) {
				column = map[x];
				for (int y = 0; y < height; y++) {
					if (column[y] >= lower && column[y] < upper)
						data[x * ySections + (y >> 6)] |= 1L << (y & 63);
				}
			}
			return this;
		}
	}

	/**
	 * Constructs this GreasedRegion using a double[][] (typically one generated by
	 * {@link squidpony.squidai.DijkstraMap}) that only stores two relevant states:
	 * an "on" state for values less than or equal to upperBound (inclusive), and an
	 * "off" state for anything else.
	 * 
	 * @param map
	 *            a double[][] that probably relates in some way to DijkstraMap.
	 * @param upperBound
	 *            upper inclusive; any double greater than this will be off, any
	 *            others will be on
	 */
	public GreasedRegion(double[][] map, double upperBound) {
		width = map.length;
		height = map[0].length;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (map[x][y] <= upperBound)
					data[x * ySections + (y >> 6)] |= 1L << (y & 63);
			}
		}
	}

	public GreasedRegion refill(double[][] map, double upperBound) {
		if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (map[x][y] <= upperBound)
						data[x * ySections + (y >> 6)] |= 1L << (y & 63);
				}
			}
			return this;
		} else {
			width = (map == null) ? 0 : map.length;
			height = (map == null || map.length <= 0) ? 0 : map[0].length;
			ySections = (height + 63) >> 6;
			yEndMask = -1L >>> (64 - (height & 63));
			data = new long[width * ySections];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (map[x][y] <= upperBound)
						data[x * ySections + (y >> 6)] |= 1L << (y & 63);
				}
			}
			return this;
		}
	}

	/**
	 * Constructs this GreasedRegion using a double[][] (typically one generated by
	 * {@link squidpony.squidai.DijkstraMap}) that only stores two relevant states:
	 * an "on" state for values between lowerBound (inclusive) and upperBound
	 * (exclusive), and an "off" state for anything else.
	 * 
	 * @param map
	 *            a double[][] that probably relates in some way to DijkstraMap.
	 * @param lowerBound
	 *            lower inclusive; any double lower than this will be off, any equal
	 *            to or greater than this, but less than upper, will be on
	 * @param upperBound
	 *            upper exclusive; any double greater than or equal to this this
	 *            will be off, any doubles both less than this and equal to or
	 *            greater than lower will be on
	 */
	public GreasedRegion(double[][] map, double lowerBound, double upperBound) {
		width = map.length;
		height = map[0].length;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (map[x][y] >= lowerBound && map[x][y] < upperBound)
					data[x * ySections + (y >> 6)] |= 1L << (y & 63);
			}
		}
	}

	public GreasedRegion refill(double[][] map, double lower, double upper) {
		if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
			double[] column;
			for (int x = 0; x < width; x++) {
				column = map[x];
				for (int y = 0; y < height; y++) {
					data[x * ySections + (y >> 6)] |= ((column[y] >= lower && column[y] < upper) ? 1L : 0L) << (y & 63);
				}
			}
			return this;
		} else {
			width = (map == null) ? 0 : map.length;
			height = (map == null || map.length <= 0) ? 0 : map[0].length;
			ySections = (height + 63) >> 6;
			yEndMask = -1L >>> (64 - (height & 63));
			data = new long[width * ySections];
			double[] column;
			for (int x = 0; x < width; x++) {
				column = map[x];
				for (int y = 0; y < height; y++) {
					if (column[y] >= lower && column[y] < upper)
						data[x * ySections + (y >> 6)] |= 1L << (y & 63);
				}
			}
			return this;
		}
	}

	public GreasedRegion(boolean[] bits, int width, int height) {
		this.width = width;
		this.height = height;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / height, y = a % height) {
			if (bits[a])
				data[x * ySections + (y >> 6)] |= 1L << (y & 63);
		}
	}

	public GreasedRegion refill(boolean[] bits, int width, int height) {
		if (bits != null && bits.length > 0 && this.width == width && this.height == height) {
			for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / height, y = a % height) {
				data[x * ySections + (y >> 6)] |= (bits[a] ? 1L : 0L) << (y & 63);
			}
			return this;
		} else {
			this.width = (bits == null || width < 0) ? 0 : width;
			this.height = (bits == null || bits.length <= 0 || height < 0) ? 0 : height;
			ySections = (height + 63) >> 6;
			yEndMask = -1L >>> (64 - (height & 63));
			data = new long[width * ySections];
			for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / height, y = a % height) {
				if (bits[a])
					data[x * ySections + (y >> 6)] |= 1L << (y & 63);
			}
			return this;
		}
	}

	/**
	 * Constructor for an empty GreasedRegion of the given width and height.
	 * GreasedRegions are mutable, so you can add to this with insert() or
	 * insertSeveral(), among others.
	 * 
	 * @param width
	 *            the maximum width for the GreasedRegion
	 * @param height
	 *            the maximum height for the GreasedRegion
	 */
	public GreasedRegion(int width, int height) {
		this.width = width;
		this.height = height;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
	}

	/**
	 * Constructor for a GreasedRegion that contains a single "on" cell, and has the
	 * given width and height. Note that to avoid confusion with the constructor
	 * that takes multiple Coord values, this takes the single "on" Coord first,
	 * while the multiple-Coord constructor takes its vararg or array of Coords
	 * last.
	 * 
	 * @param single
	 *            the one (x,y) point to store as "on" in this GreasedRegion
	 * @param width
	 *            the maximum width for the GreasedRegion
	 * @param height
	 *            the maximum height for the GreasedRegion
	 */
	public GreasedRegion(Coord single, int width, int height) {
		this.width = width;
		this.height = height;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];

		if (single.x < width && single.y < height && single.x >= 0 && single.y >= 0)
			data[single.x * ySections + (single.y >> 6)] |= 1L << (single.y & 63);
	}

	/**
	 * Constructor for a GreasedRegion that can have several "on" cells specified,
	 * and has the given width and height. Note that to avoid confusion with the
	 * constructor that takes one Coord value, this takes the vararg or array of
	 * Coords last, while the single-Coord constructor takes its one Coord first.
	 * 
	 * @param width
	 *            the maximum width for the GreasedRegion
	 * @param height
	 *            the maximum height for the GreasedRegion
	 * @param points
	 *            an array or vararg of Coord to store as "on" in this GreasedRegion
	 */
	public GreasedRegion(int width, int height, Coord... points) {
		this.width = width;
		this.height = height;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		if (points != null) {
			for (int i = 0, x, y; i < points.length; i++) {
				x = points[i].x;
				y = points[i].y;
				if (x < width && y < height && x >= 0 && y >= 0)
					data[x * ySections + (y >> 6)] |= 1L << (y & 63);
			}
		}
	}

	/**
	 * Constructor for an random GreasedRegion of the given width and height.
	 * GreasedRegions are mutable, so you can add to this with insert() or
	 * insertSeveral(), among others.
	 * 
	 * @param random
	 *            a RandomnessSource (such as LightRNG or ThunderRNG) that this will
	 *            use to generate its contents
	 * @param width
	 *            the maximum width for the GreasedRegion
	 * @param height
	 *            the maximum height for the GreasedRegion
	 */
	public GreasedRegion(RandomnessSource random, int width, int height) {
		this.width = width;
		this.height = height;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		for (int i = 0; i < width * ySections; i++) {
			data[i] = random.nextLong();
		}
		if (ySections > 0 && yEndMask != -1) {
			for (int a = ySections - 1; a < data.length; a += ySections) {
				data[a] &= yEndMask;
			}
		}
	}

	/**
	 * Copy constructor that takes another GreasedRegion and copies all of its data
	 * into this new one. If you find yourself frequently using this constructor and
	 * assigning it to the same variable, consider using the
	 * {@link #remake(GreasedRegion)} method on the variable instead, which will, if
	 * it has the same width and height as the other GreasedRegion, avoid creating
	 * garbage and quickly fill the variable with the other's contents.
	 * 
	 * @see #copy() for a convenience method that just uses this constructor
	 * @param other
	 *            another GreasedRegion that will be copied into this new
	 *            GreasedRegion
	 */
	public GreasedRegion(GreasedRegion other) {
		width = other.width;
		height = other.height;
		ySections = other.ySections;
		yEndMask = other.yEndMask;
		data = new long[width * ySections];
		System.arraycopy(other.data, 0, data, 0, width * ySections);
	}

	public GreasedRegion(final long[] data2, final int width, final int height) {
		this.width = width;
		this.height = height;
		ySections = (height + 63) >> 6;
		yEndMask = -1L >>> (64 - (height & 63));
		data = new long[width * ySections];
		System.arraycopy(data2, 0, data, 0, width * ySections);
	}

	public GreasedRegion remake(GreasedRegion other) {
		if (width == other.width && height == other.height) {
			System.arraycopy(other.data, 0, data, 0, width * ySections);
			return this;
		} else {
			width = other.width;
			height = other.height;
			ySections = other.ySections;
			yEndMask = other.yEndMask;
			data = new long[width * ySections];
			System.arraycopy(other.data, 0, data, 0, width * ySections);
			return this;
		}
	}

	public GreasedRegion insert(int x, int y) {
		if (x < width && y < height && x >= 0 && y >= 0)
			data[x * ySections + (y >> 6)] |= 1L << (y & 63);
		return this;
	}

	public GreasedRegion insert(Coord point) {
		return insert(point.x, point.y);
	}

	public GreasedRegion insertSeveral(Coord... points) {
		for (int i = 0, x, y; i < points.length; i++) {
			x = points[i].x;
			y = points[i].y;
			if (x < width && y < height && x >= 0 && y >= 0)
				data[x * ySections + (y >> 6)] |= 1L << (y & 63);
		}
		return this;
	}

	public GreasedRegion insertSeveral(Iterable<Coord> points) {
		int x, y;
		for (Coord pt : points) {
			x = pt.x;
			y = pt.y;
			if (x < width && y < height && x >= 0 && y >= 0)
				data[x * ySections + (y >> 6)] |= 1L << (y & 63);
		}
		return this;
	}

	public GreasedRegion insertRectangle(int startX, int startY, int rectangleWidth, int rectangleHeight) {
		if (rectangleWidth < 1 || rectangleHeight < 1 || ySections <= 0)
			return this;
		if (startX < 0)
			startX = 0;
		else if (startX >= width)
			startX = width - 1;
		if (startY < 0)
			startY = 0;
		else if (startY >= height)
			startY = height - 1;
		int endX = Math.min(width, startX + rectangleWidth) - 1, endY = Math.min(height, startY + rectangleHeight) - 1,
				startSection = startY >> 6, endSection = endY >> 6;
		if (startSection < endSection) {
			long startMask = -1L << (startY & 63), endMask = -1L >>> (~endY & 63);
			for (int a = startX * ySections + startSection; a <= endX * ySections; a += ySections) {
				data[a] |= startMask;
			}
			if (endSection - startSection > 1) {
				for (int b = 1; b < endSection - startSection; b++) {
					for (int a = startX * ySections + startSection + b; a < endX * ySections
							+ ySections; a += ySections) {
						data[a] = -1;
					}
				}
			}
			for (int a = startX * ySections + endSection; a <= endX * ySections + ySections; a += ySections) {
				data[a] |= endMask;
			}
		} else {
			long mask = (-1L << (startY & 63)) & (-1L >>> (~endY & 63));
			for (int a = startX * ySections + startSection; a <= endX * ySections + startSection; a += ySections) {
				data[a] |= mask;
			}
		}

		if (ySections > 0 && yEndMask != -1) {
			for (int a = ySections - 1; a < data.length; a += ySections) {
				data[a] &= yEndMask;
			}
		}
		return this;
	}

	public GreasedRegion insertCircle(Coord center, int radius) {
		return insertSeveral(Radius.CIRCLE.pointsInside(center, radius, false, width, height));
	}

	public GreasedRegion remove(int x, int y) {
		if (x < width && y < height && x >= 0 && y >= 0)
			data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
		return this;
	}

	public GreasedRegion remove(Coord point) {
		return remove(point.x, point.y);
	}

	public GreasedRegion removeSeveral(Coord... points) {
		for (int i = 0, x, y; i < points.length; i++) {
			x = points[i].x;
			y = points[i].y;
			if (x < width && y < height && x >= 0 && y >= 0)
				data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
		}
		return this;
	}

	public GreasedRegion removeSeveral(Iterable<Coord> points) {
		int x, y;
		for (Coord pt : points) {
			x = pt.x;
			y = pt.y;
			if (x < width && y < height && x >= 0 && y >= 0)
				data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
		}
		return this;
	}

	public GreasedRegion removeRectangle(int startX, int startY, int rectangleWidth, int rectangleHeight) {
		if (rectangleWidth < 1 || rectangleHeight < 1 || ySections <= 0)
			return this;
		if (startX < 0)
			startX = 0;
		else if (startX >= width)
			startX = width - 1;
		if (startY < 0)
			startY = 0;
		else if (startY >= height)
			startY = height - 1;
		int endX = Math.min(width, startX + rectangleWidth) - 1, endY = Math.min(height, startY + rectangleHeight) - 1,
				startSection = startY >> 6, endSection = endY >> 6;
		if (startSection < endSection) {
			long startMask = ~(-1L << (startY & 63)), endMask = ~(-1L >>> (~endY & 63));
			for (int a = startX * ySections + startSection; a <= endX * ySections; a += ySections) {
				data[a] &= startMask;
			}
			if (endSection - startSection > 1) {
				for (int b = 1; b < endSection - startSection; b++) {
					for (int a = startX * ySections + startSection + b; a < endX * ySections
							+ ySections; a += ySections) {
						data[a] = 0;
					}
				}
			}
			for (int a = startX * ySections + endSection; a <= endX * ySections + ySections; a += ySections) {
				data[a] &= endMask;
			}
		} else {
			long mask = ~((-1L << (startY & 63)) & (-1L >>> (~endY & 63)));
			for (int a = startX * ySections + startSection; a <= endX * ySections + startSection; a += ySections) {
				data[a] &= mask;
			}
		}
		return this;
	}

	public GreasedRegion removeCircle(Coord center, int radius) {
		return removeSeveral(Radius.CIRCLE.pointsInside(center, radius, false, width, height));
	}

	public GreasedRegion clear() {
		for (int i = 0; i < data.length; i++) {
			data[i] = 0;
		}
		return this;
	}

	public GreasedRegion copy() {
		return new GreasedRegion(this);
	}

	public boolean[][] decode() {
		boolean[][] bools = new boolean[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				bools[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0;
			}
		}
		return bools;
	}

	public char[][] toChars(char on, char off) {
		char[][] chars = new char[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? on : off;
			}
		}
		return chars;
	}

	public char[][] toChars() {
		return toChars('.', '#');
	}

	/**
	 * Returns a copy of map where if a cell is "on" in this GreasedRegion, this
	 * keeps the value in map intact, and where a cell is "off", it instead writes
	 * the char filler.
	 * 
	 * @param map
	 *            a 2D char array that will not be modified
	 * @param filler
	 *            the char to use where this GreasedRegion stores an "off" cell
	 * @return a masked copy of map
	 */
	public char[][] mask(char[][] map, char filler) {
		if (map == null || map.length == 0)
			return new char[0][0];
		int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
		char[][] chars = new char[width2][height2];
		for (int x = 0; x < width2; x++) {
			for (int y = 0; y < height2; y++) {
				chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? map[x][y] : filler;
			}
		}
		return chars;
	}

	/**
	 * Returns a copy of map where if a cell is "on" in this GreasedRegion, this
	 * keeps the value in map intact, and where a cell is "off", it instead writes
	 * the short filler. Meant for use with MultiSpill, but may be used anywhere you
	 * have a 2D short array. {@link #mask(char[][], char)} is more likely to be
	 * useful.
	 * 
	 * @param map
	 *            a 2D short array that will not be modified
	 * @param filler
	 *            the short to use where this GreasedRegion stores an "off" cell
	 * @return a masked copy of map
	 */
	public short[][] mask(short[][] map, short filler) {
		if (map == null || map.length == 0)
			return new short[0][0];
		int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
		short[][] shorts = new short[width2][height2];
		for (int x = 0; x < width2; x++) {
			for (int y = 0; y < height2; y++) {
				shorts[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? map[x][y] : filler;
			}
		}
		return shorts;
	}

	/**
	 * "Inverse mask for ints;" returns a copy of map where if a cell is "off" in
	 * this GreasedRegion, this keeps the value in map intact, and where a cell is
	 * "on", it instead writes the int toWrite.
	 * 
	 * @param map
	 *            a 2D int array that will not be modified
	 * @param toWrite
	 *            the int to use where this GreasedRegion stores an "on" cell
	 * @return an altered copy of map
	 */
	public int[][] writeInts(int[][] map, int toWrite) {
		if (map == null || map.length == 0)
			return new int[0][0];
		int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
		int[][] ints = new int[width2][height2];
		for (int x = 0; x < width2; x++) {
			for (int y = 0; y < height2; y++) {
				ints[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? toWrite : map[x][y];
			}
		}
		return ints;
	}

	public GreasedRegion or(GreasedRegion other) {
		for (int x = 0; x < width && x < other.width; x++) {
			for (int y = 0; y < ySections && y < other.ySections; y++) {
				data[x * ySections + y] |= other.data[x * ySections + y];
			}
			/*
			 * for (int y = 0; y < height && y < other.height; y++) { data[x * ySections +
			 * (y >> 6)] &= other.data[x * ySections + (y >> 6)]; }
			 * 
			 */
		}

		if (ySections > 0 && yEndMask != -1) {
			for (int a = ySections - 1; a < data.length; a += ySections) {
				data[a] &= yEndMask;
			}
		}

		return this;
	}

	public GreasedRegion and(GreasedRegion other) {
		for (int x = 0; x < width && x < other.width; x++) {
			for (int y = 0; y < ySections && y < other.ySections; y++) {
				data[x * ySections + y] &= other.data[x * ySections + y];
			}
		}
		return this;
	}

	public GreasedRegion andNot(GreasedRegion other) {
		for (int x = 0; x < width && x < other.width; x++) {
			for (int y = 0; y < ySections && y < other.ySections; y++) {
				data[x * ySections + y] &= ~other.data[x * ySections + y];
			}
		}
		return this;
	}

	/**
	 * Like andNot, but subtracts this GreasedRegion from other and stores the
	 * result in this GreasedRegion, without mutating other.
	 * 
	 * @param other
	 * @return
	 */
	public GreasedRegion notAnd(GreasedRegion other) {
		for (int x = 0; x < width && x < other.width; x++) {
			for (int y = 0; y < ySections && y < other.ySections; y++) {
				data[x * ySections + y] = other.data[x * ySections + y] & ~data[x * ySections + y];
			}
		}
		return this;
	}

	public GreasedRegion xor(GreasedRegion other) {
		for (int x = 0; x < width && x < other.width; x++) {
			for (int y = 0; y < ySections && y < other.ySections; y++) {
				data[x * ySections + y] ^= other.data[x * ySections + y];
			}
		}

		if (ySections > 0 && yEndMask != -1) {
			for (int a = ySections - 1; a < data.length; a += ySections) {
				data[a] &= yEndMask;
			}
		}
		return this;
	}

	public GreasedRegion not() {
		for (int a = 0; a < data.length; a++) {
			data[a] = ~data[a];
		}

		if (ySections > 0 && yEndMask != -1) {
			for (int a = ySections - 1; a < data.length; a += ySections) {
				data[a] &= yEndMask;
			}
		}
		return this;
	}

	public GreasedRegion translate(int x, int y) {
		if (width < 2 || ySections <= 0 || (x == 0 && y == 0))
			return this;

		long[] data2 = new long[width * ySections];
		int start = Math.max(0, x), len = Math.min(width, width + x) - start;
		long prev, tmp;
		if (x < 0) {
			System.arraycopy(data, Math.max(0, -x) * ySections, data2, 0, len * ySections);
		} else if (x > 0) {
			System.arraycopy(data, 0, data2, start * ySections, len * ySections);
		} else {
			System.arraycopy(data, 0, data2, 0, len * ySections);
		}
		if (y < 0) {
			for (int i = start; i < len; i++) {
				prev = 0L;
				for (int j = 0; j < ySections; j++) {
					tmp = prev;
					prev = (data2[i * ySections + j] & ~(-1L << -y)) << (64 + y);
					data2[i * ySections + j] >>>= -y;
					data2[i * ySections + j] |= tmp;
				}
			}
		} else if (y > 0) {
			for (int i = start; i < start + len; i++) {
				prev = 0L;
				for (int j = ySections - 1; j >= 0; j--) {
					tmp = prev;
					prev = (data2[i * ySections + j] & ~(-1L >>> y)) >>> (64 - y);
					data2[i * ySections + j] <<= y;
					data2[i * ySections + j] |= tmp;
				}
			}
		}

		if (ySections > 0 && yEndMask != -1) {
			for (int a = ySections - 1; a < data.length; a += ySections) {
				data2[a] &= yEndMask;
			}
		}
		data = data2;
		return this;
	}

	public GreasedRegion expand() {
		if (width < 2 || ySections == 0)
			return this;

		long[] next = new long[width * ySections];
		System.arraycopy(data, 0, next, 0, width * ySections);
		for (int a = 0; a < ySections; a++) {
			next[a] |= (data[a] << 1) | (data[a] >>> 1) | data[a + ySections];
			next[(width - 1) * ySections + a] |= (data[(width - 1) * ySections + a] << 1)
					| (data[(width - 1) * ySections + a] >>> 1) | data[(width - 2) * ySections + a];

			for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
				next[i] |= (data[i] << 1) | (data[i] >>> 1) | data[i - ySections] | data[i + ySections];
			}

			if (a > 0) {
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] |= (data[i - 1] & 0x8000000000000000L) >>> 63;
				}
			}

			if (a < ySections - 1) {
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] |= (data[i + 1] & 1L) << 63;
				}
			}
		}

		if (ySections > 0 && yEndMask != -1) {
			for (int a = ySections - 1; a < next.length; a += ySections) {
				next[a] &= yEndMask;
			}
		}
		data = next;
		return this;
	}

	public GreasedRegion expand(int amount) {
		for (int i = 0; i < amount; i++) {
			expand();
		}
		return this;
	}

	public GreasedRegion[] expandSeries(int amount) {
		if (amount <= 0)
			return new GreasedRegion[0];
		GreasedRegion[] regions = new GreasedRegion[amount];
		GreasedRegion temp = new GreasedRegion(this);
		for (int i = 0; i < amount; i++) {
			regions[i] = new GreasedRegion(temp.expand());
		}
		return regions;
	}

	public ArrayList<GreasedRegion> expandSeriesToLimit() {
		ArrayList<GreasedRegion> regions = new ArrayList<>();
		GreasedRegion temp = new GreasedRegion(this);
		while (temp.size() != temp.expand().size()) {
			regions.add(new GreasedRegion(temp));
		}
		return regions;
	}

	public GreasedRegion fringe() {
		GreasedRegion cpy = new GreasedRegion(this);
		expand();
		return andNot(cpy);
	}

	public GreasedRegion fringe(int amount) {
		GreasedRegion cpy = new GreasedRegion(this);
		expand(amount);
		return andNot(cpy);
	}

	public GreasedRegion[] fringeSeries(int amount) {
		if (amount <= 0)
			return new GreasedRegion[0];
		GreasedRegion[] regions = new GreasedRegion[amount];
		GreasedRegion temp = new GreasedRegion(this);
		regions[0] = new GreasedRegion(temp);
		for (int i = 1; i < amount; i++) {
			regions[i] = new GreasedRegion(temp.expand());
		}
		for (int i = 0; i < amount - 1; i++) {
			regions[i].xor(regions[i + 1]);
		}
		regions[amount - 1].fringe();
		return regions;
	}

	public ArrayList<GreasedRegion> fringeSeriesToLimit() {
		ArrayList<GreasedRegion> regions = expandSeriesToLimit();
		for (int i = regions.size() - 1; i > 0; i--) {
			regions.get(i).xor(regions.get(i - 1));
		}
		regions.get(0).xor(this);
		return regions;
	}

	public GreasedRegion retract() {
		if (width <= 2 || ySections <= 0)
			return this;

		long[] next = new long[width * ySections];
		System.arraycopy(data, ySections, next, ySections, (width - 2) * ySections);
		for (int a = 0; a < ySections; a++) {
			if (a > 0 && a < ySections - 1) {
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
							& ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63)) & data[i - ySections]
							& data[i + ySections];
				}
			} else if (a > 0) {
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63)) & (data[i] >>> 1)
							& data[i - ySections] & data[i + ySections];
				}
			} else if (a < ySections - 1) {
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] &= (data[i] << 1) & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63)) & data[i - ySections]
							& data[i + ySections];
				}
			} else // only the case when ySections == 1
			{
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] &= (data[i] << 1) & (data[i] >>> 1) & data[i - ySections] & data[i + ySections];
				}
			}
		}

		if (yEndMask != -1) {
			for (int a = ySections - 1; a < next.length; a += ySections) {
				next[a] &= yEndMask;
			}
		}
		data = next;
		return this;
	}

	public GreasedRegion retract(int amount) {
		for (int i = 0; i < amount; i++) {
			retract();
		}
		return this;
	}

	public GreasedRegion[] retractSeries(int amount) {
		if (amount <= 0)
			return new GreasedRegion[0];
		GreasedRegion[] regions = new GreasedRegion[amount];
		GreasedRegion temp = new GreasedRegion(this);
		for (int i = 0; i < amount; i++) {
			regions[i] = new GreasedRegion(temp.retract());
		}
		return regions;
	}

	public GreasedRegion surface() {
		GreasedRegion cpy = new GreasedRegion(this).retract();
		return xor(cpy);
	}

	public GreasedRegion surface(int amount) {
		GreasedRegion cpy = new GreasedRegion(this).retract(amount);
		return xor(cpy);
	}

	public GreasedRegion[] surfaceSeries(int amount) {
		if (amount <= 0)
			return new GreasedRegion[0];
		GreasedRegion[] regions = new GreasedRegion[amount];
		GreasedRegion temp = new GreasedRegion(this);
		regions[0] = new GreasedRegion(temp);
		for (int i = 1; i < amount; i++) {
			regions[i] = new GreasedRegion(temp.retract());
		}
		for (int i = 0; i < amount - 1; i++) {
			regions[i].xor(regions[i + 1]);
		}
		regions[amount - 1].surface();
		return regions;
	}

	public GreasedRegion expand8way() {
		if (width < 2 || ySections <= 0)
			return this;

		long[] next = new long[width * ySections];
		System.arraycopy(data, 0, next, 0, width * ySections);
		for (int a = 0; a < ySections; a++) {
			next[a] |= (data[a] << 1) | (data[a] >>> 1) | data[a + ySections] | (data[a + ySections] << 1)
					| (data[a + ySections] >>> 1);
			next[(width - 1) * ySections + a] |= (data[(width - 1) * ySections + a] << 1)
					| (data[(width - 1) * ySections + a] >>> 1) | data[(width - 2) * ySections + a]
					| (data[(width - 2) * ySections + a] << 1) | (data[(width - 2) * ySections + a] >>> 1);

			for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
				next[i] |= (data[i] << 1) | (data[i] >>> 1) | data[i - ySections] | (data[i - ySections] << 1)
						| (data[i - ySections] >>> 1) | data[i + ySections] | (data[i + ySections] << 1)
						| (data[i + ySections] >>> 1);
			}

			if (a > 0) {
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] |= ((data[i - 1] & 0x8000000000000000L) >>> 63)
							| ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63)
							| ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63);
				}
			}

			if (a < ySections - 1) {
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] |= ((data[i + 1] & 1L) << 63) | ((data[i - ySections + 1] & 1L) << 63)
							| ((data[i + ySections + 1] & 1L) << 63);
				}
			}
		}

		if (ySections > 0 && yEndMask != -1) {
			for (int a = ySections - 1; a < next.length; a += ySections) {
				next[a] &= yEndMask;
			}
		}
		data = next;
		return this;
	}

	public GreasedRegion expand8way(int amount) {
		for (int i = 0; i < amount; i++) {
			expand8way();
		}
		return this;
	}

	public GreasedRegion[] expandSeries8way(int amount) {
		if (amount <= 0)
			return new GreasedRegion[0];
		GreasedRegion[] regions = new GreasedRegion[amount];
		GreasedRegion temp = new GreasedRegion(this);
		for (int i = 0; i < amount; i++) {
			regions[i] = new GreasedRegion(temp.expand8way());
		}
		return regions;
	}

	public ArrayList<GreasedRegion> expandSeriesToLimit8way() {
		ArrayList<GreasedRegion> regions = new ArrayList<>();
		GreasedRegion temp = new GreasedRegion(this);
		while (temp.size() != temp.expand8way().size()) {
			regions.add(new GreasedRegion(temp));
		}
		return regions;
	}

	public GreasedRegion fringe8way() {
		GreasedRegion cpy = new GreasedRegion(this);
		expand8way();
		return andNot(cpy);
	}

	public GreasedRegion fringe8way(int amount) {
		GreasedRegion cpy = new GreasedRegion(this);
		expand8way(amount);
		return andNot(cpy);
	}

	public GreasedRegion[] fringeSeries8way(int amount) {
		if (amount <= 0)
			return new GreasedRegion[0];
		GreasedRegion[] regions = new GreasedRegion[amount];
		GreasedRegion temp = new GreasedRegion(this);
		regions[0] = new GreasedRegion(temp);
		for (int i = 1; i < amount; i++) {
			regions[i] = new GreasedRegion(temp.expand8way());
		}
		for (int i = 0; i < amount - 1; i++) {
			regions[i].xor(regions[i + 1]);
		}
		regions[amount - 1].fringe8way();
		return regions;
	}

	public ArrayList<GreasedRegion> fringeSeriesToLimit8way() {
		ArrayList<GreasedRegion> regions = expandSeriesToLimit8way();
		for (int i = regions.size() - 1; i > 0; i--) {
			regions.get(i).xor(regions.get(i - 1));
		}
		regions.get(0).xor(this);
		return regions;
	}

	public GreasedRegion retract8way() {
		if (width <= 2 || ySections <= 0)
			return this;

		long[] next = new long[width * ySections];
		System.arraycopy(data, ySections, next, ySections, (width - 2) * ySections);
		for (int a = 0; a < ySections; a++) {
			if (a > 0 && a < ySections - 1) {
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
							& ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63)) & data[i - ySections] & data[i + ySections]
							& ((data[i - ySections] << 1) | ((data[i - 1 - ySections] & 0x8000000000000000L) >>> 63))
							& ((data[i + ySections] << 1) | ((data[i - 1 + ySections] & 0x8000000000000000L) >>> 63))
							& ((data[i - ySections] >>> 1) | ((data[i + 1 - ySections] & 1L) << 63))
							& ((data[i + ySections] >>> 1) | ((data[i + 1 + ySections] & 1L) << 63));
				}
			} else if (a > 0) {
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63)) & (data[i] >>> 1)
							& data[i - ySections] & data[i + ySections]
							& ((data[i - ySections] << 1) | ((data[i - 1 - ySections] & 0x8000000000000000L) >>> 63))
							& ((data[i + ySections] << 1) | ((data[i - 1 + ySections] & 0x8000000000000000L) >>> 63))
							& (data[i - ySections] >>> 1) & (data[i + ySections] >>> 1);
				}
			} else if (a < ySections - 1) {
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] &= (data[i] << 1) & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63)) & data[i - ySections]
							& data[i + ySections] & (data[i - ySections] << 1) & (data[i + ySections] << 1)
							& ((data[i - ySections] >>> 1) | ((data[i + 1 - ySections] & 1L) << 63))
							& ((data[i + ySections] >>> 1) | ((data[i + 1 + ySections] & 1L) << 63));
				}
			} else // only the case when ySections == 1
			{
				for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
					next[i] &= (data[i] << 1) & (data[i] >>> 1) & data[i - ySections] & data[i + ySections]
							& (data[i - ySections] << 1) & (data[i + ySections] << 1) & (data[i - ySections] >>> 1)
							& (data[i + ySections] >>> 1);
				}
			}
		}

		if (yEndMask != -1) {
			for (int a = ySections - 1; a < next.length; a += ySections) {
				next[a] &= yEndMask;
			}
		}
		data = next;
		return this;
	}

	public GreasedRegion retract8way(int amount) {
		for (int i = 0; i < amount; i++) {
			retract8way();
		}
		return this;
	}

	public GreasedRegion[] retractSeries8way(int amount) {
		if (amount <= 0)
			return new GreasedRegion[0];
		GreasedRegion[] regions = new GreasedRegion[amount];
		GreasedRegion temp = new GreasedRegion(this);
		for (int i = 0; i < amount; i++) {
			regions[i] = new GreasedRegion(temp.retract8way());
		}
		return regions;
	}

	public GreasedRegion surface8way() {
		GreasedRegion cpy = new GreasedRegion(this).retract8way();
		return xor(cpy);
	}

	public GreasedRegion surface8way(int amount) {
		GreasedRegion cpy = new GreasedRegion(this).retract8way(amount);
		return xor(cpy);
	}

	public GreasedRegion[] surfaceSeries8way(int amount) {
		if (amount <= 0)
			return new GreasedRegion[0];
		GreasedRegion[] regions = new GreasedRegion[amount];
		GreasedRegion temp = new GreasedRegion(this);
		regions[0] = new GreasedRegion(temp);
		for (int i = 1; i < amount; i++) {
			regions[i] = new GreasedRegion(temp.retract8way());
		}
		for (int i = 0; i < amount - 1; i++) {
			regions[i].xor(regions[i + 1]);
		}
		regions[amount - 1].surface8way();
		return regions;
	}

	public GreasedRegion flood(GreasedRegion bounds) {
		if (width < 2 || ySections <= 0 || bounds == null || bounds.width < 2 || bounds.ySections <= 0)
			return this;

		long[] next = new long[width * ySections];
		for (int a = 0; a < ySections && a < bounds.ySections; a++) {
			next[a] |= (data[a] | (data[a] << 1) | (data[a] >>> 1) | data[a + ySections]) & bounds.data[a];
			next[(width - 1) * ySections
					+ a] |= (data[(width - 1) * ySections + a] | (data[(width - 1) * ySections + a] << 1)
							| (data[(width - 1) * ySections + a] >>> 1) | data[(width - 2) * ySections + a])
							& bounds.data[(width - 1) * bounds.ySections + a];

			for (int i = ySections + a, j = bounds.ySections + a; i < (width - 1) * ySections
					&& j < (bounds.width - 1) * bounds.ySections; i += ySections, j += bounds.ySections) {
				next[i] |= (data[i] | (data[i] << 1) | (data[i] >>> 1) | data[i - ySections] | data[i + ySections])
						& bounds.data[j];
			}

			if (a > 0) {
				for (int i = ySections + a, j = bounds.ySections + a; i < (width - 1) * ySections
						&& j < (bounds.width - 1) * bounds.ySections; i += ySections, j += bounds.ySections) {
					next[i] |= (data[i] | ((data[i - 1] & 0x8000000000000000L) >>> 63)) & bounds.data[j];
				}
			}

			if (a < ySections - 1 && a < bounds.ySections - 1) {
				for (int i = ySections + a, j = bounds.ySections + a; i < (width - 1) * ySections
						&& j < (bounds.width - 1) * bounds.ySections; i += ySections, j += bounds.ySections) {
					next[i] |= (data[i] | ((data[i + 1] & 1L) << 63)) & bounds.data[j];
				}
			}
		}

		if (yEndMask != -1 && bounds.yEndMask != -1) {
			if (ySections == bounds.ySections) {
				long mask = ((yEndMask >>> 1) <= (bounds.yEndMask >>> 1)) ? yEndMask : bounds.yEndMask;
				for (int a = ySections - 1; a < next.length; a += ySections) {
					next[a] &= mask;
				}
			} else if (ySections < bounds.ySections) {
				for (int a = ySections - 1; a < next.length; a += ySections) {
					next[a] &= yEndMask;
				}
			} else {
				for (int a = bounds.ySections - 1; a < next.length; a += ySections) {
					next[a] &= bounds.yEndMask;
				}
			}
		}
		data = next;
		return this;
	}

	public GreasedRegion flood(GreasedRegion bounds, int amount) {
		int ct = size(), ct2;
		for (int i = 0; i < amount; i++) {
			flood(bounds);
			if (ct == (ct2 = size()))
				break;
			else
				ct = ct2;

		}
		return this;
	}

	public GreasedRegion[] floodSeries(GreasedRegion bounds, int amount) {
		if (amount <= 0)
			return new GreasedRegion[0];
		int ct = size(), ct2;
		GreasedRegion[] regions = new GreasedRegion[amount];
		boolean done = false;
		GreasedRegion temp = new GreasedRegion(this);
		for (int i = 0; i < amount; i++) {
			if (done) {
				regions[i] = new GreasedRegion(temp);
			} else {
				regions[i] = new GreasedRegion(temp.flood(bounds));
				if (ct == (ct2 = temp.size()))
					done = true;
				else
					ct = ct2;
			}
		}
		return regions;
	}

	public ArrayList<GreasedRegion> floodSeriesToLimit(GreasedRegion bounds) {
		int ct = size(), ct2;
		ArrayList<GreasedRegion> regions = new ArrayList<>();
		GreasedRegion temp = new GreasedRegion(this);
		while (true) {
			temp.flood(bounds);
			if (ct == (ct2 = temp.size()))
				return regions;
			else {
				ct = ct2;
				regions.add(new GreasedRegion(temp));
			}
		}
	}

	public GreasedRegion flood8way(GreasedRegion bounds) {
		if (width < 2 || ySections <= 0 || bounds == null || bounds.width < 2 || bounds.ySections <= 0)
			return this;

		long[] next = new long[width * ySections];
		for (int a = 0; a < ySections && a < bounds.ySections; a++) {
			next[a] |= (data[a] | (data[a] << 1) | (data[a] >>> 1) | data[a + ySections] | (data[a + ySections] << 1)
					| (data[a + ySections] >>> 1)) & bounds.data[a];
			next[(width - 1) * ySections
					+ a] |= (data[(width - 1) * ySections + a] | (data[(width - 1) * ySections + a] << 1)
							| (data[(width - 1) * ySections + a] >>> 1) | data[(width - 2) * ySections + a]
							| (data[(width - 2) * ySections + a] << 1) | (data[(width - 2) * ySections + a] >>> 1))
							& bounds.data[(width - 1) * bounds.ySections + a];

			for (int i = ySections + a, j = bounds.ySections + a; i < (width - 1) * ySections
					&& j < (bounds.width - 1) * bounds.ySections; i += ySections, j += bounds.ySections) {
				next[i] |= (data[i] | (data[i] << 1) | (data[i] >>> 1) | data[i - ySections]
						| (data[i - ySections] << 1) | (data[i - ySections] >>> 1) | data[i + ySections]
						| (data[i + ySections] << 1) | (data[i + ySections] >>> 1)) & bounds.data[j];
			}

			if (a > 0) {
				for (int i = ySections + a, j = bounds.ySections + a; i < (width - 1) * ySections
						&& j < (bounds.width - 1) * bounds.ySections; i += ySections, j += bounds.ySections) {
					next[i] |= (data[i] | ((data[i - 1] & 0x8000000000000000L) >>> 63)
							| ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63)
							| ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63)) & bounds.data[j];
				}
			}

			if (a < ySections - 1 && a < bounds.ySections - 1) {
				for (int i = ySections + a, j = bounds.ySections + a; i < (width - 1) * ySections
						&& j < (bounds.width - 1) * bounds.ySections; i += ySections, j += bounds.ySections) {
					next[i] |= (data[i] | ((data[i + 1] & 1L) << 63) | ((data[i - ySections + 1] & 1L) << 63)
							| ((data[i + ySections + 1] & 1L) << 63)) & bounds.data[j];
				}
			}
		}

		if (yEndMask != -1 && bounds.yEndMask != -1) {
			if (ySections == bounds.ySections) {
				long mask = ((yEndMask >>> 1) <= (bounds.yEndMask >>> 1)) ? yEndMask : bounds.yEndMask;
				for (int a = ySections - 1; a < next.length; a += ySections) {
					next[a] &= mask;
				}
			} else if (ySections < bounds.ySections) {
				for (int a = ySections - 1; a < next.length; a += ySections) {
					next[a] &= yEndMask;
				}
			} else {
				for (int a = bounds.ySections - 1; a < next.length; a += ySections) {
					next[a] &= bounds.yEndMask;
				}
			}
		}
		data = next;
		return this;
	}

	public GreasedRegion flood8way(GreasedRegion bounds, int amount) {
		int ct = size(), ct2;
		for (int i = 0; i < amount; i++) {
			flood8way(bounds);
			if (ct == (ct2 = size()))
				break;
			else
				ct = ct2;
		}
		return this;
	}

	public GreasedRegion[] floodSeries8way(GreasedRegion bounds, int amount) {
		if (amount <= 0)
			return new GreasedRegion[0];
		int ct = size(), ct2;
		GreasedRegion[] regions = new GreasedRegion[amount];
		boolean done = false;
		GreasedRegion temp = new GreasedRegion(this);
		for (int i = 0; i < amount; i++) {
			if (done) {
				regions[i] = new GreasedRegion(temp);
			} else {
				regions[i] = new GreasedRegion(temp.flood8way(bounds));
				if (ct == (ct2 = temp.size()))
					done = true;
				else
					ct = ct2;
			}
		}
		return regions;
	}

	public ArrayList<GreasedRegion> floodSeriesToLimit8way(GreasedRegion bounds) {
		int ct = size(), ct2;
		ArrayList<GreasedRegion> regions = new ArrayList<>();
		GreasedRegion temp = new GreasedRegion(this);
		while (true) {
			temp.flood8way(bounds);
			if (ct == (ct2 = temp.size()))
				return regions;
			else {
				ct = ct2;
				regions.add(new GreasedRegion(temp));
			}
		}
	}

	public GreasedRegion spill(GreasedRegion bounds, int volume, RNG rng) {
		if (width < 2 || ySections <= 0 || bounds == null || bounds.width < 2 || bounds.ySections <= 0)
			return this;
		int current = size();
		if (current >= volume)
			return this;
		GreasedRegion t = new GreasedRegion(this);
		Coord c = Coord.get(-1, -1);
		for (int i = current; i < volume; i++) {
			insert(t.remake(this).fringe().and(bounds).singleRandom(rng));
		}
		return this;
	}

	/**
	 * If this GreasedRegion stores multiple unconnected "on" areas, this finds each
	 * isolated area (areas that are only adjacent diagonally are considered
	 * separate from each other) and returns it as an element in an ArrayList of
	 * GreasedRegion, with one GreasedRegion per isolated area. Useful when you
	 * have, for example, all the rooms in a dungeon with their connecting corridors
	 * removed, but want to separate the rooms. You can get the aforementioned data
	 * assuming a bare dungeon called map using: <br>
	 * {@code GreasedRegion floors = new GreasedRegion(map, '.'),
	 * rooms = floors.copy().retract8way().flood(floors, 2),
	 * corridors = floors.copy().andNot(rooms),
	 * doors = rooms.copy().and(corridors.copy().fringe());} <br>
	 * You can then get all rooms as separate regions with
	 * {@code List<GreasedRegion> apart = split(rooms);}, or substitute
	 * {@code split(corridors)} to get the corridors. The room-finding technique
	 * works by shrinking floors by a radius of 1 (8-way), which causes thin areas
	 * like corridors of 2 or less width to be removed, then flood-filling the
	 * floors out from the area that produces by 2 cells (4-way this time) to
	 * restore the original size of non-corridor areas (plus some extra to ensure
	 * odd shapes are kept). Corridors are obtained by removing the rooms from
	 * floors. The example code also gets the doors (which overlap with rooms, not
	 * corridors) by finding where the a room and a corridor are adjacent. This
	 * technique is used with some enhancements in the RoomFinder class.
	 * 
	 * @see squidpony.squidgrid.mapping.RoomFinder for a class that uses this
	 *      technique without exposing GreasedRegion
	 * @return an ArrayList containing each unconnected area from packed as a
	 *         GreasedRegion element
	 */
	public ArrayList<GreasedRegion> split() {
		ArrayList<GreasedRegion> scattered = new ArrayList<>(32);
		Coord fst = first();
		GreasedRegion remaining = new GreasedRegion(this);
		while (fst.x >= 0) {
			GreasedRegion filled = new GreasedRegion(fst, width, height).flood(remaining, width * height);
			scattered.add(filled);
			remaining.andNot(filled);
			fst = remaining.first();
		}
		return scattered;
	}

	public GreasedRegion removeIsolated() {
		Coord fst = first();
		GreasedRegion remaining = new GreasedRegion(this), filled = new GreasedRegion(this);
		while (fst.x >= 0) {
			filled.clear().insert(fst).flood(remaining, 8);
			if (filled.size() <= 4)
				andNot(filled);
			remaining.andNot(filled);
			fst = remaining.first();
		}
		return this;
	}

	public boolean intersects(GreasedRegion other) {
		for (int x = 0; x < width && x < other.width; x++) {
			for (int y = 0; y < ySections && y < other.ySections; y++) {
				if ((data[x * ySections + y] & other.data[x * ySections + y]) != 0)
					return true;
			}
		}
		return false;
	}

	public int size() {
		int c = 0;
		for (int i = 0; i < width * ySections; i++) {
			c += Long.bitCount(data[i]);
		}
		return c;
	}

	public Coord fit(double xFraction, double yFraction) {
		int tmp, xTotal = 0, yTotal = 0, xTarget, yTarget, bestX = -1;
		long t;
		int[] xCounts = new int[width];
		for (int x = 0; x < width; x++) {
			for (int s = 0; s < ySections; s++) {
				t = data[x * ySections + s];
				if (t != 0) {
					tmp = Long.bitCount(t);
					xCounts[x] += tmp;
					xTotal += tmp;
				}
			}
		}
		xTarget = (int) (xTotal * xFraction);
		for (int x = 0; x < width; x++) {
			if ((xTarget -= xCounts[x]) < 0) {
				bestX = x;
				yTotal = xCounts[x];
				break;
			}
		}
		if (bestX < 0) {
			return Coord.get(-1, -1);
		}
		yTarget = (int) (yTotal * yFraction);

		for (int s = 0, y = 0; s < ySections; s++) {
			t = data[bestX * ySections + s];
			for (long cy = 1; cy != 0 && y < height; y++, cy <<= 1) {
				if ((t & cy) != 0 && --yTarget < 0) {
					return Coord.get(bestX, y);
				}
			}
		}

		return new Coord(-1, -1);
	}

	public int[][] fit(int[][] basis, int defaultValue) {
		int[][] next = GwtCompatibility.fill2D(defaultValue, width, height);
		if (basis == null || basis.length <= 0 || basis[0] == null || basis[0].length <= 0)
			return next;
		int tmp, xTotal = 0, yTotal = 0, xTarget, yTarget, bestX = -1, oX = basis.length, oY = basis[0].length, ao;
		long t;
		int[] xCounts = new int[width];
		for (int x = 0; x < width; x++) {
			for (int s = 0; s < ySections; s++) {
				t = data[x * ySections + s];
				if (t != 0) {
					tmp = Long.bitCount(t);
					xCounts[x] += tmp;
					xTotal += tmp;
				}
			}
		}
		if (xTotal <= 0)
			return next;
		for (int aX = 0; aX < oX; aX++) {
			CELL_WISE: for (int aY = 0; aY < oY; aY++) {
				if ((ao = basis[aX][aY]) == defaultValue)
					continue;
				xTarget = xTotal * aX / oX;
				for (int x = 0; x < width; x++) {
					if ((xTarget -= xCounts[x]) < 0) {
						bestX = x;
						yTotal = xCounts[x];
						yTarget = yTotal * aY / oY;
						for (int s = 0, y = 0; s < ySections; s++) {
							t = data[bestX * ySections + s];
							for (long cy = 1; cy != 0 && y < height; y++, cy <<= 1) {
								if ((t & cy) != 0 && --yTarget < 0) {
									next[bestX][y] = ao;
									continue CELL_WISE;
								}
							}
						}
						continue CELL_WISE;
					}
				}

			}
		}

		return next;
	}

	/*
	 * public int[][] edgeFit(int[][] basis, int defaultValue) { int[][] next =
	 * GwtCompatibility.fill2D(defaultValue, width, height); if(basis == null ||
	 * basis.length <= 0 || basis[0] == null || basis[0].length <= 0) return next;
	 * 
	 * return next; }
	 */
	public Coord[] separatedPortion(double fraction) {
		if (fraction < 0)
			return new Coord[0];
		if (fraction > 1)
			fraction = 1;
		int ct, tmp, xTotal = 0, yTotal = 0, xTarget, yTarget, bestX = -1;
		long t;
		int[] xCounts = new int[width];
		for (int x = 0; x < width; x++) {
			for (int s = 0; s < ySections; s++) {
				t = data[x * ySections + s];
				if (t != 0) {
					tmp = Long.bitCount(t);
					xCounts[x] += tmp;
					xTotal += tmp;
				}
			}
		}
		Coord[] vl = new Coord[ct = (int) (fraction * xTotal)];
		double[] vec = new double[2];
		sobol.skipTo(1337);
		EACH_SOBOL: for (int i = 0; i < ct; i++) {
			sobol.fillVector(vec);
			xTarget = (int) (xTotal * vec[0]);
			for (int x = 0; x < width; x++) {
				if ((xTarget -= xCounts[x]) < 0) {
					bestX = x;
					yTotal = xCounts[x];
					break;
				}
			}
			yTarget = (int) (yTotal * vec[1]);

			for (int s = 0, y = 0; s < ySections; s++) {
				t = data[bestX * ySections + s];
				for (long cy = 1; cy != 0 && y < height; y++, cy <<= 1) {
					if ((t & cy) != 0 && --yTarget < 0) {
						vl[i] = Coord.get(bestX, y);
						continue EACH_SOBOL;
					}
				}
			}
		}
		return vl;

	}

	public Coord[] randomSeparated(double fraction, RNG rng) {
		return randomSeparated(fraction, rng, -1);
	}

	public Coord[] randomSeparated(double fraction, RNG rng, int limit) {
		if (fraction < 0)
			return new Coord[0];
		if (fraction > 1)
			fraction = 1;
		int ct, tmp, xTotal = 0, yTotal = 0, xTarget, yTarget, bestX = -1;
		long t;
		int[] xCounts = new int[width];
		for (int x = 0; x < width; x++) {
			for (int s = 0; s < ySections; s++) {
				t = data[x * ySections + s];
				if (t != 0) {
					tmp = Long.bitCount(t);
					xCounts[x] += tmp;
					xTotal += tmp;
				}
			}
		}
		ct = (int) (fraction * xTotal);
		if (limit >= 0 && limit < ct)
			ct = limit;
		Coord[] vl = new Coord[ct];
		double[] vec = new double[2];
		sobol.skipTo(rng.between(1000, 65000));
		EACH_SOBOL: for (int i = 0; i < ct; i++) {
			sobol.fillVector(vec);
			xTarget = (int) (xTotal * vec[0]);
			for (int x = 0; x < width; x++) {
				if ((xTarget -= xCounts[x]) < 0) {
					bestX = x;
					yTotal = xCounts[x];
					break;
				}
			}
			yTarget = (int) (yTotal * vec[1]);

			for (int s = 0, y = 0; s < ySections; s++) {
				t = data[bestX * ySections + s];
				for (long cy = 1; cy != 0 && y < height; y++, cy <<= 1) {
					if ((t & cy) != 0 && --yTarget < 0) {
						vl[i] = Coord.get(bestX, y);
						continue EACH_SOBOL;
					}
				}
			}
		}
		return vl;

	}

	public double rateDensity() {
		double sz = height * width;
		if (sz == 0)
			return 0;
		double onAmount = sz - size(), retractedOn = sz - copy().retract().size();
		return (onAmount + retractedOn) / (sz * 2.0);
	}

	public double rateRegularity() {
		GreasedRegion me2 = copy().surface8way();
		double irregularCount = me2.size();
		if (irregularCount == 0)
			return 0;
		return me2.remake(this).surface().size() / irregularCount;
	}

	/*
	 * // This showed a strong x-y correlation because it didn't have a way to use a
	 * non-base-2 van der Corput sequence. // It also produced very close-together
	 * points, unfortunately. public static double quasiRandomX(int idx) { return
	 * atVDCSequence(23L + idx * 255L); } public static double quasiRandomY(int idx)
	 * { return atVDCSequence(20L + idx); }
	 * 
	 * private static double atVDCSequence(long idx) { long leading =
	 * Long.numberOfLeadingZeros(idx); double t = (Long.reverse(idx) >>> leading) /
	 * (1.0 * (1L << (64L - leading))); return t; }
	 */

	public Coord[] asCoords() {
		int ct = 0, idx = 0;
		for (int i = 0; i < width * ySections; i++) {
			ct += Long.bitCount(data[i]);
		}
		Coord[] points = new Coord[ct];
		long t, w;
		for (int x = 0; x < width; x++) {
			for (int s = 0; s < ySections; s++) {
				if ((t = data[x * ySections + s]) != 0) {
					w = Long.lowestOneBit(t);
					while (w != 0) {
						points[idx++] = Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
						t ^= w;
						w = Long.lowestOneBit(t);
					}
				}
			}
		}
		return points;
	}

	public Coord first() {
		long t, w;
		for (int x = 0; x < width; x++) {
			for (int s = 0; s < ySections; s++) {
				if ((w = Long.lowestOneBit(data[x * ySections + s])) != 0) {
					return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
				}
			}
		}
		return new Coord(-1, -1);
	}

	public Coord singleRandom(RNG rng) {
		int ct = 0, tmp;
		int[] counts = new int[width * ySections];
		for (int i = 0; i < width * ySections; i++) {
			tmp = Long.bitCount(data[i]);
			counts[i] = tmp == 0 ? -1 : (ct += tmp);
		}
		tmp = rng.nextInt(ct);
		long t, w;
		for (int x = 0; x < width; x++) {
			for (int s = 0; s < ySections; s++) {
				if ((ct = counts[x * ySections + s]) > tmp) {
					t = data[x * ySections + s];
					w = Long.lowestOneBit(t);
					for (--ct; w != 0; ct--) {
						if (ct == tmp)
							return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
						t ^= w;
						w = Long.lowestOneBit(t);
					}
				}
			}
		}

		return new Coord(-1, -1);
	}

	public Coord[] randomPortion(RNG rng, int size) {
		int ct = 0, idx = 0, run = 0;
		for (int i = 0; i < width * ySections; i++) {
			ct += Long.bitCount(data[i]);
		}
		if (ct <= 0 || size <= 0)
			return new Coord[0];
		if (ct <= size)
			return asCoords();
		Coord[] points = new Coord[size];
		int[] order = rng.randomOrdering(ct);
		Arrays.sort(order, 0, size);
		long t, w;
		ALL: for (int x = 0; x < width; x++) {
			for (int s = 0; s < ySections; s++) {
				if ((t = data[x * ySections + s]) != 0) {
					w = Long.lowestOneBit(t);
					while (w != 0) {
						if (run++ == order[idx]) {
							points[idx++] = Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
							if (idx >= size)
								break ALL;
						}
						t ^= w;
						w = Long.lowestOneBit(t);
					}
				}
			}
		}
		return points;
	}

	/**
	 * Generates a 2D int array from an array or vararg of GreasedRegions, starting
	 * at all 0 and adding 1 to the int at a position once for every GreasedRegion
	 * that has that cell as "on." This means if you give 8 GreasedRegions to this
	 * method, it can produce any number between 0 and 8 in a cell; if you give 16
	 * GreasedRegions, then it can produce number between 0 and 16 in a cell.
	 * 
	 * @param regions
	 *            an array or vararg of GreasedRegions; must all have the same width
	 *            and height
	 * @return a 2D int array with the same width and height as the regions, where
	 *         an int cell equals the number of given GreasedRegions that had an
	 *         "on" cell at that position
	 */
	public static int[][] sum(GreasedRegion... regions) {
		if (regions == null || regions.length <= 0)
			return new int[0][0];
		int w = regions[0].width, h = regions[0].height, l = Math.min(32, regions.length), ys = regions[0].ySections;
		int[][] numbers = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				for (int i = 0; i < l; i++) {
					numbers[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0;
				}
			}
		}
		return numbers;
	}

	/**
	 * Generates a 2D int array from an array or vararg of GreasedRegions, treating
	 * each cell in the nth region as the nth bit of the int at the corresponding
	 * x,y cell in the int array. This means if you give 8 GreasedRegions to this
	 * method, it can produce any 8-bit number in a cell (0-255); if you give 16
	 * GreasedRegions, then it can produce any 16-bit number (0-65535).
	 * 
	 * @param regions
	 *            an array or vararg of GreasedRegions; must all have the same width
	 *            and height
	 * @return a 2D int array with the same width and height as the regions, with
	 *         bits per int taken from the regions
	 */
	public static int[][] bitSum(GreasedRegion... regions) {
		if (regions == null || regions.length <= 0)
			return new int[0][0];
		int w = regions[0].width, h = regions[0].height, l = Math.min(32, regions.length), ys = regions[0].ySections;
		int[][] numbers = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				for (int i = 0; i < l; i++) {
					numbers[x][y] |= (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 << i : 0;
				}
			}
		}
		return numbers;
	}

	/*
	 * public static int[][] selectiveNegate(int[][] numbers, GreasedRegion region,
	 * int mask) { if(region == null) return numbers; int w = region.width, h =
	 * region.height, ys = region.ySections; for (int x = 0; x < w; x++) { for (int
	 * y = 0; y < h; y++) { if((region.data[x * ys + (y >> 6)] & (1L << (y & 63)))
	 * != 0) numbers[x][y] = (~numbers[x][y] & mask); } } return numbers; }
	 */

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		GreasedRegion that = (GreasedRegion) o;

		if (height != that.height)
			return false;
		if (width != that.width)
			return false;
		if (ySections != that.ySections)
			return false;
		if (yEndMask != that.yEndMask)
			return false;
		return Arrays.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		int result = CrossHash.hash(data);
		result = 31 * result + height;
		result = 31 * result + width;
		result = 31 * result + ySections;
		result = 31 * result + (int) (yEndMask ^ (yEndMask >>> 32));
		return result;
	}
}

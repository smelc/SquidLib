package squidpony.squidgrid.zone;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Abstraction over a list of {@link Coord}. This allows to use the short arrays
 * coming from {@link squidpony.squidmath.CoordPacker}, which are compressed for
 * better memory usage, regular {@link List lists of Coord}, which are often the
 * simplest option, or {@link squidpony.squidmath.GreasedRegion GreasedRegions},
 * which are "greasy" in the fatty-food sense (they are heavier objects, and are
 * uncompressed) but also "greased" like greased lightning (they are very fast at
 * spatial transformations on their region).
 * <p>
 * Zones are {@link Serializable}, but serialization doesn't change the internal
 * representation (some would want to pack {@link ListZone} into
 * {@link CoordPackerZone}s when serializing). I find that overzealous for a
 * simple interface. If you want your zones to be be packed when serialized,
 * create {@link CoordPackerZone} yourself. In squidlib-extra, GreasedRegions are
 * given slightly special treatment during that JSON-like serialization so they
 * avoid repeating certain information, but they are still going to be larger than
 * compressed short arrays from CoordPacker.
 * </p>
 * <p>
 * While CoordPacker produces short arrays that can be wrapped in CoordPackerZone
 * objects, and a List of Coord can be similarly wrapped in a ListZone object,
 * GreasedRegion extends {@link Zone.Skeleton} and so implements Zone itself.
 * Unlike CoordPackerZone, which is immutable in practice (changing the short
 * array reference is impossible and changing the elements rarely works as
 * planned), GreasedRegion is mutable for performance reasons, and may need copies
 * to be created if you want to keep around older GreasedRegions.
 * </p>
 * @author smelC
 * @see squidpony.squidmath.CoordPacker
 * @see squidpony.squidmath.GreasedRegion
 */
public interface Zone extends Serializable, Iterable<Coord> {

    /**
     * @return Whether this zone is empty.
     */
    boolean isEmpty();

    /**
     * @return The number of cells that this zone contains (the size
     * {@link #getAll()}).
     */
    int size();

    /**
     * @param x
     * @param y
     * @return Whether this zone contains the coordinate (x,y).
     */
    boolean contains(int x, int y);

    /**
     * @param c
     * @return Whether this zone contains {@code c}.
     */
    boolean contains(Coord c);

	/**
	 * @param other
	 * @return true if all cells of {@code other} are in {@code this}.
	 */
	boolean contains(Zone other);

	/**
	 * @param other
	 * @return true if {@code this} and {@code other} have a common cell.
	 */
	boolean intersectsWith(Zone other);

	/**
	 * @return The approximate center of this zone, or null if this zone
	 *         is empty.
	 */
	/* @Nullable */ Coord getCenter();

	/**
	 * @return The distance between the leftmost cell and the rightmost cell, or
	 *         anything negative if {@code this} zone is empty.
	 */
	int getWidth();

	/**
	 * @return The distance between the topmost cell and the lowest cell, or
	 *         anything negative if {@code this} zone is empty.
	 */
	int getHeight();

	/**
	 * @return The approximation of the zone's diagonal, using
	 *         {@link #getWidth()} and {@link #getHeight()}.
	 */
	double getDiagonal();

	/**
	 * @param smallestOrBiggest
	 * @return The x-coordinate of the Coord within {@code this} that has the
	 *         smallest (or biggest) x-coordinate. Or -1 if the zone is empty.
	 */
	int x(boolean smallestOrBiggest);

	/**
	 * @param smallestOrBiggest
	 * @return The y-coordinate of the Coord within {@code this} that has the
	 *         smallest (or biggest) y-coordinate. Or -1 if the zone is empty.
	 */
	int y(boolean smallestOrBiggest);

    /**
     * @return All cells in this zone.
     */
    List<Coord> getAll();

	/** @return {@code this} shifted by {@code c} */
	Zone translate(Coord c);

	/** @return {@code this} shifted by {@code (x,y)} */
	Zone translate(int x, int y);

	/** @return Cells adjacent to {@code this} that aren't in {@code this} */
	Collection<Coord> getExternalBorder();

	/**
	 * @return A variant of {@code this} where cells adjacent to {@code this}
	 *         have been added (i.e. it's {@code this} plus
	 *         {@link #getExternalBorder()}).
	 */
	Zone extend();

    /**
     * A convenience partial implementation. Please try for all new
     * implementations of {@link Zone} to be subtypes of this class. It usually
     * prove handy at some point to have a common superclass.
     *
     * @author smelC
     */
    abstract class Skeleton implements Zone {

		private transient Coord center = null;
		private transient int width = -2;
		private transient int height = -2;

		private static final long serialVersionUID = 4436698111716212256L;

		@Override
		/* Convenience implementation, feel free to override */
		public boolean contains(Coord c) {
			return contains(c.x, c.y);
		}

		@Override
		/* Convenience implementation, feel free to override */
		public boolean contains(Zone other) {
			for (Coord c : other) {
				if (!contains(c))
					return false;
			}
			return true;
		}

		@Override
		public boolean intersectsWith(Zone other) {
			final int tsz = size();
			final int osz = other.size();
			final Iterable<Coord> iteratedOver = tsz < osz ? this : other;
			final Zone other_ = tsz < osz ? other : this;
			for (Coord c : iteratedOver) {
				if (other_.contains(c))
					return true;
			}
			return false;
		}

		@Override
		/*
		 * Convenience implementation, feel free to override, in particular if
		 * you can avoid allocating the list usually allocated by getAll().
		 */
		public Iterator<Coord> iterator() {
			return getAll().iterator();
		}

		@Override
		/* Convenience implementation, feel free to override. */
		public int getWidth() {
			if (width == -2)
				width = isEmpty() ? -1 : x(false) - x(true);
			return width;
		}

		@Override
		/* Convenience implementation, feel free to override. */
		public int getHeight() {
			if (height == -2)
				height = isEmpty() ? -1 : y(false) - y(true);
			return height;
		}

		@Override
		public double getDiagonal() {
			final int w = getWidth();
			final int h = getHeight();
			return Math.sqrt((w * w) + (h * h));
		}

		@Override
		/* Convenience implementation, feel free to override. */
		public int x(boolean smallestOrBiggest) {
			return smallestOrBiggest ? smallest(true) : biggest(true);
		}

		@Override
		/* Convenience implementation, feel free to override. */
		public int y(boolean smallestOrBiggest) {
			return smallestOrBiggest ? smallest(false) : biggest(false);
		}

		@Override
		/* Convenience implementation, feel free to override. */
		/*
		 * A possible enhancement would be to check that the center is within
		 * the zone, and if not to return the coord closest to the center, that
		 * is in the zone .
		 */
		public /* @Nullable */ Coord getCenter() {
			if (center == null) {
				/* Need to compute it */
				if (isEmpty())
					return null;
				int x = 0, y = 0;
				float nb = 0;
				for (Coord c : this) {
					x += c.x;
					y += c.y;
					nb++;
				}
				/* Remember it */
				center = Coord.get(Math.round(x / nb), Math.round(y / nb));
			}
			return center;
		}

		@Override
		/* Convenience implementation, feel free to override. */
		public Zone translate(Coord c) {
			return translate(c.x, c.y);
		}

		@Override
		/* Convenience implementation, feel free to override. */
		public Zone translate(int x, int y) {
			final List<Coord> initial = getAll();
			final List<Coord> shifted = new ArrayList<Coord>(initial);
			final int sz = initial.size();
			for (int i = 0; i < sz; i++) {
				final Coord c = initial.get(i);
				shifted.add(Coord.get(c.x + x, c.y + y));
			}
			return new ListZone(shifted);
		}

		@Override
		/* Convenience implementation, feel free to override. */
		public Collection<Coord> getExternalBorder() {
			return DungeonUtility.border(getAll(), null);
		}

		@Override
		/* Convenience implementation, feel free to override. */
		public Zone extend() {
			final List<Coord> list = new ArrayList<Coord>(getAll());
			list.addAll(getExternalBorder());
			return new ListZone(list);
		}

		private int smallest(boolean xOrY) {
			if (isEmpty())
				return -1;
			int min = Integer.MAX_VALUE;
			for (Coord c : this) {
				final int val = xOrY ? c.x : c.y;
				if (val < min)
					min = val;
			}
			return min;
		}

		private int biggest(boolean xOrY) {
			int max = -1;
			for (Coord c : this) {
				final int val = xOrY ? c.x : c.y;
				if (max < val)
					max = val;
			}
			return max;
		}
	}
}

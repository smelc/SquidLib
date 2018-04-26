package squidpony.panel;

/**
 * The abstraction of {@code SquidPanel}s, to abstract from the UI
 * implementation (i.e. whether it's awt or libgdx doesn't matter here).
 * 
 * @author smelC - Introduction of this interface, but methods were in
 *         SquidPanel already.
 * 
 * @param <T>
 *            The type of colors
 */
public interface ISquidPanel<T> {

	/**
	 * Puts {@code color} at {@code (x, y)} (in the cell's entirety, i.e. in the
	 * background).
	 * 
	 * @param x
	 * @param y
	 * @param color
	 */
	void put(int x, int y, T color);

	/**
	 * Puts the character {@code c} at {@code (x, y)} with some {@code color}.
	 * 
	 * @param x
	 * @param y
	 * @param c
	 * @param color
	 */
	void put(int x, int y, char c, T color);

	/**
	 * @param foregrounds
	 *            Can be {@code null}, indicating that only colors must be put.
	 * @param colors
	 */
	void put(/* @Nullable */ char[][] foregrounds, T[][] colors);

	/**
	 * Removes the contents of this cell, leaving a transparent space.
	 *
	 * @param x
	 * @param y
	 */
	void clear(int x, int y);

	/**
	 * @return The number of cells that this panel spans, horizontally.
	 */
	int gridWidth();

	/**
	 * @return The number of cells that this panel spans, vertically.
	 */
	int gridHeight();

	/**
	 * @return The width of a cell, in number of pixels.
	 */
	int cellWidth();

	/**
	 * @return The height of a cell, in number of pixels.
	 */
	int cellHeight();

	/**
	 * @return Returns true if there are animations running when this method is
	 *         called.
	 */
	boolean hasActiveAnimations();

}

package squidpony.squidgrid.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.PerlinNoise;
import squidpony.squidmath.RNG;

/**
 * A static class that can be used to modify the char[][] dungeons that other
 * generators produce. Includes various utilities for random floor-finding, but
 * also provides ways to take dungeons that use '#' for walls and make a copy
 * that uses unicode box drawing characters.
 *
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class DungeonUtility {

	/**
	 * Takes a char[][] dungeon map that uses '#' to represent walls, and returns a
	 * new char[][] that uses unicode box drawing characters to draw straight,
	 * continuous lines for walls, filling regions between walls (that were filled
	 * with more walls before) with space characters, ' '. If the lines "point the
	 * wrong way," such as having multiple horizontally adjacent vertical lines
	 * where there should be horizontal lines, call transposeLines() on the returned
	 * map, which will keep the dimensions of the map the same and only change the
	 * line chars. You will also need to call transposeLines if you call
	 * hashesToLines on a map that already has "correct" line-drawing characters,
	 * which means hashesToLines should only be called on maps that use '#' for
	 * walls. If you have a jumbled map that contains two or more of the following:
	 * "correct" line-drawing characters, "incorrect" line-drawing characters, and
	 * '#' characters for walls, you can reset by calling linesToHashes() and then
	 * potentially calling hashesToLines() again.
	 *
	 * @param map
	 *            a 2D char array indexed with x,y that uses '#' for walls
	 * @return a copy of the map passed as an argument with box-drawing characters
	 *         replacing '#' walls
	 */
	public static char[][] hashesToLines(char[][] map) {
		return hashesToLines(map, false);
	}

	private static final char[] wallLookup = new char[] { '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴',
			'┐', '┤', '┬', '┼', '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼', '#',
			'│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼', '#', '│', '─', '└', '│', '│',
			'┌', '│', '─', '┘', '─', '┴', '┐', '┤', '┬', '┤', '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─',
			'┴', '┐', '┤', '┬', '┼', '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
			'#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '─', '┴', '#', '│', '─', '└', '│',
			'│', '┌', '│', '─', '┘', '─', '┴', '┐', '┤', '─', '┘', '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘',
			'─', '┴', '┐', '┤', '┬', '┼', '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '─', '┐', '┤', '┬',
			'┬', '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼', '#', '│', '─', '└',
			'│', '│', '┌', '│', '─', '┘', '─', '─', '┐', '┤', '┬', '┐', '#', '│', '─', '└', '│', '│', '┌', '├', '─',
			'┘', '─', '┴', '┐', '│', '┬', '├', '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '─', '┐', '│',
			'┬', '┌', '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '│', '─', '└', '#', '│', '─',
			'└', '│', '│', '┌', '│', '─', '┘', '─', '─', '┐', '│', '─', '\1' };

	/**
	 * Takes a char[][] dungeon map that uses '#' to represent walls, and returns a
	 * new char[][] that uses unicode box drawing characters to draw straight,
	 * continuous lines for walls, filling regions between walls (that were filled
	 * with more walls before) with space characters, ' '. If keepSingleHashes is
	 * true, then '#' will be used if a wall has no orthogonal wall neighbors; if it
	 * is false, then a horizontal line will be used for stand-alone wall cells. If
	 * the lines "point the wrong way," such as having multiple horizontally
	 * adjacent vertical lines where there should be horizontal lines, call
	 * transposeLines() on the returned map, which will keep the dimensions of the
	 * map the same and only change the line chars. You will also need to call
	 * transposeLines if you call hashesToLines on a map that already has "correct"
	 * line-drawing characters, which means hashesToLines should only be called on
	 * maps that use '#' for walls. If you have a jumbled map that contains two or
	 * more of the following: "correct" line-drawing characters, "incorrect"
	 * line-drawing characters, and '#' characters for walls, you can reset by
	 * calling linesToHashes() and then potentially calling hashesToLines() again.
	 *
	 * @param map
	 *            a 2D char array indexed with x,y that uses '#' for walls
	 * @param keepSingleHashes
	 *            true if walls that are not orthogonally adjacent to other walls
	 *            should stay as '#'
	 * @return a copy of the map passed as an argument with box-drawing characters
	 *         replacing '#' walls
	 */
	public static char[][] hashesToLines(char[][] map, boolean keepSingleHashes) {
		int width = map.length + 2;
		int height = map[0].length + 2;

		char[][] dungeon = new char[width][height];
		for (int i = 1; i < width - 1; i++) {
			System.arraycopy(map[i - 1], 0, dungeon[i], 1, height - 2);
		}
		for (int i = 0; i < width; i++) {
			dungeon[i][0] = '\1';
			dungeon[i][height - 1] = '\1';
		}
		for (int i = 0; i < height; i++) {
			dungeon[0][i] = '\1';
			dungeon[width - 1][i] = '\1';
		}
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				if (map[x - 1][y - 1] == '#') {
					int q = 0;
					q |= (y <= 1 || map[x - 1][y - 2] == '#' || map[x - 1][y - 2] == '+' || map[x - 1][y - 2] == '/')
							? 1
							: 0;
					q |= (x >= width - 2 || map[x][y - 1] == '#' || map[x][y - 1] == '+' || map[x][y - 1] == '/') ? 2
							: 0;
					q |= (y >= height - 2 || map[x - 1][y] == '#' || map[x - 1][y] == '+' || map[x - 1][y] == '/') ? 4
							: 0;
					q |= (x <= 1 || map[x - 2][y - 1] == '#' || map[x - 2][y - 1] == '+' || map[x - 2][y - 1] == '/')
							? 8
							: 0;

					q |= (y <= 1 || x >= width - 2 || map[x][y - 2] == '#' || map[x][y - 2] == '+'
							|| map[x][y - 2] == '/') ? 16 : 0;
					q |= (y >= height - 2 || x >= width - 2 || map[x][y] == '#' || map[x][y] == '+' || map[x][y] == '/')
							? 32
							: 0;
					q |= (y >= height - 2 || x <= 1 || map[x - 2][y] == '#' || map[x - 2][y] == '+'
							|| map[x - 2][y] == '/') ? 64 : 0;
					q |= (y <= 1 || x <= 1 || map[x - 2][y - 2] == '#' || map[x - 2][y - 2] == '+'
							|| map[x - 2][y - 2] == '/') ? 128 : 0;
					if (!keepSingleHashes && wallLookup[q] == '#') {
						dungeon[x][y] = '─';
					} else {
						dungeon[x][y] = wallLookup[q];
					}
				}
			}
		}
		char[][] portion = new char[width - 2][height - 2];
		for (int i = 1; i < width - 1; i++) {
			for (int j = 1; j < height - 1; j++) {
				switch (dungeon[i][j]) {
				case '\1':
					portion[i - 1][j - 1] = ' ';
					break;
				default: // ┼┌┘
					portion[i - 1][j - 1] = dungeon[i][j];
				}
			}
		}
		return portion;
	}

	/**
	 * Reverses most of the effects of hashesToLines(). The only things that will
	 * not be reversed are the placement of space characters in unreachable
	 * wall-cells-behind-wall-cells, which remain as spaces. This is useful if you
	 * have a modified map that contains wall characters of conflicting varieties,
	 * as described in hashesToLines().
	 *
	 * @param map
	 *            a 2D char array indexed with x,y that uses box-drawing characters
	 *            for walls
	 * @return a copy of the map passed as an argument with '#' replacing
	 *         box-drawing characters for walls
	 */
	public static char[][] linesToHashes(char[][] map) {

		int width = map.length;
		int height = map[0].length;
		char[][] portion = new char[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				switch (map[i][j]) {
				case '\1':
				case '├':
				case '┤':
				case '┴':
				case '┬':
				case '┌':
				case '┐':
				case '└':
				case '┘':
				case '│':
				case '─':
				case '┼':
					portion[i][j] = '#';
					break;
				default:
					portion[i][j] = map[i][j];
				}
			}
		}
		return portion;
	}

	/**
	 * If you call hashesToLines() on a map that uses [y][x] conventions instead of
	 * [x][y], it will have the lines not connect as you expect. Use this function
	 * to change the directions of the box-drawing characters only, without altering
	 * the dimensions in any way. This returns a new char[][], instead of modifying
	 * the parameter in place. transposeLines is also needed if the lines in a map
	 * have become transposed when they were already correct; calling this method on
	 * an incorrectly transposed map will change the directions on all of its lines.
	 *
	 * @param map
	 *            a 2D char array indexed with y,x that uses box-drawing characters
	 *            for walls
	 * @return a copy of map that uses box-drawing characters for walls that will be
	 *         correct when indexed with x,y
	 */
	public static char[][] transposeLines(char[][] map) {

		int width = map[0].length;
		int height = map.length;
		char[][] portion = new char[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				switch (map[i][j]) {
				case '\1':
					portion[i][j] = ' ';
					break;
				case '├':
					portion[i][j] = '┬';
					break;
				case '┤':
					portion[i][j] = '┴';
					break;
				case '┴':
					portion[i][j] = '┤';
					break;
				case '┬':
					portion[i][j] = '├';
					break;
				case '┐':
					portion[i][j] = '└';
					break;
				case '└':
					portion[i][j] = '┐';
					break;
				case '│':
					portion[i][j] = '─';
					break;
				case '─':
					portion[i][j] = '│';
					break;
				// case '├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─':
				default: // ┼┌┘
					portion[i][j] = map[i][j];
				}
			}
		}
		return portion;
	}

	/**
	 * Takes a char[][] dungeon map and returns a copy with all box drawing chars,
	 * special placeholder chars, or '#' chars changed to '#' and everything else
	 * changed to '.' .
	 *
	 * @param map
	 *            a char[][] with different characters that can be simplified to
	 *            "wall" or "floor"
	 * @return a copy of map with all box-drawing, placeholder, wall or space
	 *         characters as '#' and everything else '.'
	 */
	public static char[][] simplifyDungeon(char[][] map) {

		int width = map.length;
		int height = map[0].length;
		char[][] portion = new char[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				switch (map[i][j]) {
				case '\1':
				case '├':
				case '┤':
				case '┴':
				case '┬':
				case '┌':
				case '┐':
				case '└':
				case '┘':
				case '│':
				case '─':
				case '┼':
				case ' ':
				case '#':
					portion[i][j] = '#';
					break;
				default:
					portion[i][j] = '.';
				}
			}
		}
		return portion;
	}

	/**
	 * Takes a dungeon map that uses two characters per cell, and condenses it to
	 * use only the left (lower index) character in each cell. This should
	 * (probably) only be called on the result of doubleWidth(), and will throw an
	 * exception if called on a map with an odd number of characters for width, such
	 * as "#...#" .
	 *
	 * @param map
	 *            a char[][] that has been widened by doubleWidth()
	 * @return a copy of map that uses only one char per cell
	 */
	public static char[][] unDoubleWidth(char[][] map) {
		int width = map.length;
		int height = map[0].length;
		if (width % 2 != 0)
			throw new IllegalArgumentException("Argument must be a char[width][height] with an even width.");
		char[][] unpaired = new char[width / 2][height];
		for (int y = 0; y < height; y++) {
			for (int x = 0, px = 0; px < width; x++, px += 2) {
				unpaired[x][y] = map[px][y];
			}
		}
		return unpaired;
	}

	/**
	 * @param level
	 *            dungeon/map level as 2D char array. x,y indexed
	 * @param c
	 *            Coord to check
	 * @return {@code true} if {@code c} is valid in {@code level}, {@code false}
	 *         otherwise.
	 */
	public static boolean inLevel(char[][] level, Coord c) {
		return inLevel(level, c.x, c.y);
	}

	/**
	 * @param level
	 *            dungeon/map level as 2D char array. x,y indexed
	 * @param x
	 *            x coordinate to check
	 * @param y
	 *            y coordinate to check
	 * @return {@code true} if {@code c} is valid in {@code level}, {@code false}
	 *         otherwise.
	 */
	public static boolean inLevel(char[][] level, int x, int y) {
		return 0 <= x && x < level.length && 0 <= y && y < level[x].length;
	}

	/**
	 * @param level
	 *            dungeon/map level as 2D double array. x,y indexed
	 * @param c
	 *            Coord to check
	 * @return {@code true} if {@code c} is valid in {@code level}, {@code false}
	 *         otherwise.
	 */
	public static boolean inLevel(double[][] level, Coord c) {
		return inLevel(level, c.x, c.y);
	}

	/**
	 * @param level
	 *            dungeon/map level as 2D double array. x,y indexed
	 * @param x
	 *            x coordinate to check
	 * @param y
	 *            y coordinate to check
	 * @return {@code true} if {@code c} is valid in {@code level}, {@code false}
	 *         otherwise.
	 */
	public static boolean inLevel(double[][] level, int x, int y) {
		return 0 <= x && x < level.length && 0 <= y && y < level[x].length;
	}

	/**
	 * @param level
	 *            a dungeon/map level as 2D array. x,y indexed
	 * @param c
	 *            Coord to check
	 * @return {@code true} if {@code c} is valid in {@code level}, {@code false}
	 *         otherwise.
	 */
	public static <T> boolean inLevel(T[][] level, Coord c) {
		return inLevel(level, c.x, c.y);
	}

	/**
	 * @param level
	 *            a dungeon/map level as 2D array. x,y indexed
	 * @param x
	 *            x coordinate to check
	 * @param y
	 *            y coordinate to check
	 * @return {@code true} if {@code c} is valid in {@code level}, {@code false}
	 *         otherwise.
	 */
	public static <T> boolean inLevel(T[][] level, int x, int y) {
		return 0 <= x && x < level.length && 0 <= y && y < level[x].length;
	}

	/**
	 * Fills {@code array2d} with {@code value}.
	 * 
	 * @param array2d
	 * @param value
	 */
	public static void fill(boolean[][] array2d, boolean value) {
		final int width = array2d.length;
		final int height = width == 0 ? 0 : array2d[0].length;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++)
				array2d[x][y] = value;
		}
	}

	/**
	 * Fills {@code array2d} with {@code value}.
	 * 
	 * @param array2d
	 * @param value
	 */
	public static void fill(double[][] array2d, double value) {
		final int width = array2d.length;
		final int height = width == 0 ? 0 : array2d[0].length;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++)
				array2d[x][y] = value;
		}
	}

	/**
	 * Fills {@code array2d} with {@code value}.
	 * 
	 * @param array2d
	 * @param value
	 */
	public static void fill(int[][] array2d, int value) {
		final int width = array2d.length;
		final int height = width == 0 ? 0 : array2d[0].length;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++)
				array2d[x][y] = value;
		}
	}

	/**
	 * An easy way to get the Coord items in a List of Coord that are at the edge of
	 * the region.
	 * 
	 * @param zone
	 *            a List of Coord representing a region
	 * @param buffer
	 *            The list to fill if non null (i.e. if non-null, it is returned).
	 *            If null, a fresh list will be allocated and returned.
	 * @return Elements in {@code zone} that are neighbors to an element not in
	 *         {@code zone}.
	 */
	public static List<Coord> border(final List<Coord> zone, /* @Nullable */ List<Coord> buffer) {
		final int zsz = zone.size();
		final List<Coord> border = buffer == null ? new ArrayList<Coord>(zsz / 4) : buffer;
		for (int i = 0; i < zsz; i++) {
			final Coord c = zone.get(i);
			if (hasANeighborNotIn(c, zone))
				border.add(c);
		}
		return border;
	}

	/**
	 * Quickly counts the number of char elements in level that are equal to match.
	 *
	 * @param level
	 *            the 2D char array to count cells in
	 * @param match
	 *            the char to search for
	 * @return the number of cells that matched
	 */
	public static int countCells(char[][] level, char match) {
		if (level == null || level.length == 0)
			return 0;
		int counter = 0;
		for (int x = 0; x < level.length; x++) {
			for (int y = 0; y < level[x].length; y++) {
				if (level[x][y] == match)
					counter++;
			}
		}
		return counter;
	}

	/**
	 * For when you want to print a 2D char array. Prints on multiple lines, with a
	 * trailing newline.
	 *
	 * @param level
	 *            a 2D char array to print with a trailing newline
	 */
	public static void debugPrint(char[][] level) {
		if (level == null || level.length == 0 || level[0].length == 0)
			System.out.println("INVALID DUNGEON LEVEL");
		else {
			for (int y = 0; y < level[0].length; y++) {
				for (int x = 0; x < level.length; x++) {
					System.out.print(level[x][y]);
				}
				System.out.println();

			}
		}
	}

	/**
	 * Changes the outer edge of a char[][] to the wall char, '#'.
	 *
	 * @param map
	 *            A char[][] that stores map data; will be modified in place
	 * @return the modified-in-place map with its edge replaced with '#'
	 */
	public static char[][] wallWrap(char[][] map) {
		int upperY = map[0].length - 1;
		int upperX = map.length - 1;
		for (int i = 0; i < map.length; i++) {
			map[i][0] = '#';
			map[i][upperY] = '#';
		}
		for (int i = 0; i < map[0].length; i++) {
			map[0][i] = '#';
			map[upperX][i] = '#';
		}
		return map;
	}

	public static ArrayList<Coord> allMatching(char[][] map, char... matching) {
		if (map == null || map.length <= 0 || matching == null || matching.length <= 0)
			return new ArrayList<Coord>(0);
		int width = map.length, height = map[0].length;
		char[] matches = new char[matching.length];
		System.arraycopy(matching, 0, matches, 0, matching.length);
		Arrays.sort(matches);
		ArrayList<Coord> points = new ArrayList<Coord>(map.length * 4);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (Arrays.binarySearch(matches, map[x][y]) >= 0)
					points.add(Coord.get(x, y));
			}
		}
		return points;
	}

	/**
	 * Gets a List of Coord that are within radius distance of (x,y), and appends
	 * them to buf if it is non-null or makes a fresh List to append to otherwise.
	 * Returns buf if non-null, else the fresh List of Coord. May produce Coord
	 * values that are not within the boundaries of a map, such as (-5,-4), if the
	 * center is too close to the edge or radius is too high. You can use
	 * {@link squidpony.squidgrid.Radius#inCircle(int, int, int, boolean, int, int, List)}
	 * with surpassEdges as false if you want to limit Coords to within the map, or
	 * the more general
	 * {@link squidpony.squidgrid.Radius#pointsInside(int, int, int, boolean, int, int, List)}
	 * on a Radius.SQUARE or Radius.DIAMOND enum value if you want a square or
	 * diamond shape.
	 *
	 * @param x
	 *            center x of the circle
	 * @param y
	 *            center y of the circle
	 * @param radius
	 *            inclusive radius to extend from the center; radius 0 gives just
	 *            the center
	 * @param buf
	 *            Where to add the coordinates, or null for this method to allocate
	 *            a fresh list.
	 * @return The coordinates of a circle centered {@code (x, y)}, whose diameter
	 *         is {@code (radius * 2) + 1}.
	 * @see squidpony.squidgrid.Radius#inCircle(int, int, int, boolean, int, int,
	 *      List) if you want to keep the Coords within the bounds of the map
	 */
	public static List<Coord> circle(int x, int y, int radius, /* @Nullable */ List<Coord> buf) {
		final List<Coord> result = buf == null ? new ArrayList<Coord>() : buf;
		radius = Math.max(0, radius);
		for (int dx = -radius; dx <= radius; ++dx) {
			final int high = (int) Math.floor(Math.sqrt(radius * radius - dx * dx));
			for (int dy = -high; dy <= high; ++dy) {
				result.add(Coord.get(x + dx, y + dy));
			}
		}
		return result;
	}

	// FIXME CH Use Grids instead
	private static boolean hasANeighborNotIn(Coord c, Collection<Coord> others) {
		for (Direction dir : Direction.OUTWARDS) {
			if (!others.contains(c.translate(dir)))
				return true;
		}
		return false;
	}
}
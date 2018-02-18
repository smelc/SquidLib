package squidpony.squidmath;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Provides a means to generate Bresenham lines in 2D and 3D.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Lewis Potter
 * @author Tommy Ettinger
 * @author smelC
 */
public class Bresenham {

    /**
     * Prevents any instances from being created
     */
    private Bresenham() {
    }

    /**
     * Generates a 2D Bresenham line between two points. If you don't need
     * the {@link Queue} interface for the returned reference, consider
     * using {@link #line2D_(Coord, Coord)} to save some memory.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    public static Queue<Coord> line2D(Coord a, Coord b) {
        return line2D(a.x, a.y, b.x, b.y);
    }

    /**
     * Generates a 2D Bresenham line between two points.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    public static Coord[] line2D_(Coord a, Coord b) {
        return line2D_(a.x, a.y, b.x, b.y);
    }

    /**
     * Generates a 2D Bresenham line between two points. If you don't need
     * the {@link Queue} interface for the returned reference, consider
     * using {@link #line2D_(int, int, int, int)} to save some memory.
     *
     * Uses ordinary Coord values for points, and these can be pooled
     * if they aren't beyond what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @return a Queue (internally, a LinkedList) of Coord points along the line
     */
    public static Queue<Coord> line2D(int startx, int starty, int endx, int endy) {
        Queue<Coord> result = new LinkedList<>();

        int dx = endx - startx;
        int dy = endy - starty;

        int ax = Math.abs(dx) << 1;
        int ay = Math.abs(dy) << 1;

        int signx = (int) Math.signum(dx);
        int signy = (int) Math.signum(dy);

        int x = startx;
        int y = starty;

        int deltax, deltay;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            while (true) {
                result.offer(Coord.get(x, y));
                if (x == endx) {
                    return result;
                }

                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                x += signx;
                deltay += ay;
            }
        } else /* y dominant */ {
            deltax = ax - (ay >> 1);
            while (true) {
                result.offer(Coord.get(x, y));
                if (y == endy) {
                    return result;
                }

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }


                y += signy;
                deltax += ax;
            }
        }
    }


    /**
     * Generates a 2D Bresenham line between two points. Returns an array
     * of Coord instead of a Queue.
     *
     * Uses ordinary Coord values for points, and these can be pooled
     * if they aren't beyond what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @return an array of Coord points along the line
     */
    public static Coord[] line2D_(int startx, int starty, int endx, int endy) {
        int dx = endx - startx;
        int dy = endy - starty;

        int signx = (int) Math.signum(dx);
        int signy = (int) Math.signum(dy);

        int ax = (dx = Math.abs(dx)) << 1;
        int ay = (dy = Math.abs(dy)) << 1;

        int x = startx;
        int y = starty;

        int deltax, deltay;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            Coord[] result = new Coord[dx+1];
            for (int i = 0; i <= dx; i++) {
                result[i] = Coord.get(x, y);

                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                x += signx;
                deltay += ay;
            }
            return result;
        } else /* y dominant */ {
            deltax = ax - (ay >> 1);
            Coord[] result = new Coord[dy+1];
            for (int i = 0; i <= dy; i++) {
                result[i] = Coord.get(x, y);

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }


                y += signy;
                deltax += ax;
            }
            return result;
        }
    }

}

package squidpony.squidmath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Region encoding of 64x64 areas as a number of long arrays; uncompressed (fatty), but fast (greased lightning).
 * Created by Tommy Ettinger on 6/24/2016.
 */
public class GreasedRegion implements Serializable {
    private static final long serialVersionUID = 0;

    public long[] data;
    public int height;
    public int width;
    private int ySections;
    private long yEndMask;

    public GreasedRegion()
    {
        width = 64;
        height = 64;
        ySections = 1;
        yEndMask = -1L;
        data = new long[64];
    }

    public GreasedRegion(boolean[][] bits)
    {
        width = bits.length;
        height = bits[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = (-1L >>> (64 - (height & 63)));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(bits[x][y]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }

    public GreasedRegion(char[][] map, char yes)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = (-1L >>> (64 - (height & 63)));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }

    public GreasedRegion(boolean[] bits, int width, int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = (-1L >>> (64 - (height & 63)));
        data = new long[width * ySections];
        for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / height, y = a % height) {
            if(bits[a]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
        }
    }

    public GreasedRegion(Coord single, int width, int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = (-1L >>> (64 - (height & 63)));
        data = new long[width * ySections];

        if(single.x < width && single.y < height && single.x >= 0 && single.y >= 0)
            data[single.x * ySections + (single.y >> 6)] |= 1L << (single.y & 63);
    }

    public GreasedRegion(int width, int height, Coord... points)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = (-1L >>> (64 - (height & 63)));
        data = new long[width * ySections];
        if(points != null)
        {
            for (int i = 0, x, y; i < points.length; i++) {
                x = points[i].x;
                y = points[i].y;
                if(x < width && y < height && x >= 0 && y >= 0)
                    data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }

    public GreasedRegion(GreasedRegion other)
    {
        width = other.width;
        height = other.height;
        ySections = other.ySections;
        yEndMask = other.yEndMask;
        data = new long[width * ySections];
        System.arraycopy(other.data, 0, data, 0, width * ySections);
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

    public GreasedRegion insert(Coord point)
    {
            int x = point.x,
                    y = point.y;
            if(x < width && y < height && x >= 0 && y >= 0)
                data[x * ySections + (y >> 6)] |= 1L << (y & 63);
        return this;
    }

    public GreasedRegion insertSeveral(Coord... points)
    {
        for (int i = 0, x, y; i < points.length; i++) {
            x = points[i].x;
            y = points[i].y;
            if(x < width && y < height && x >= 0 && y >= 0)
                data[x * ySections + (y >> 6)] |= 1L << (y & 63);
        }
        return this;
    }
    public GreasedRegion clear()
    {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
        return this;
    }

    public GreasedRegion copy()
    {
        return new GreasedRegion(this);
    }

    public boolean[][] decode()
    {
        boolean[][] bools = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bools[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0;
            }
        }
        return bools;
    }

    public char[][] toChars(char on, char off)
    {
        char[][] chars = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? on : off;
            }
        }
        return chars;
    }

    public char[][] toChars()
    {
        return toChars('.', '#');
    }

    public GreasedRegion or(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] |= other.data[x * ySections + y];
            }
            /*
            for (int y = 0; y < height && y < other.height; y++) {
                data[x * ySections + (y >> 6)] &= other.data[x * ySections + (y >> 6)];
            }

             */
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }

        return this;
    }

    public GreasedRegion and(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] &= other.data[x * ySections + y];
            }
        }
        return this;
    }

    public GreasedRegion andNot(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] &= ~other.data[x * ySections + y];
            }
        }
        return this;
    }

    public GreasedRegion xor(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] ^= other.data[x * ySections + y];
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        return this;
    }

    public GreasedRegion not()
    {
        for (int a = 0; a < data.length; a++)
        {
            data[a] = ~ data[a];
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        return this;
    }

    public GreasedRegion translate(int x, int y)
    {
        if(width < 2 || ySections <= 0 || (x == 0 && y == 0))
            return this;

        long[] data2 = new long[width * ySections];
        int start = Math.max(0, x), len = Math.min(width, width + x) - start;
        long prev, tmp;
        if(x < 0)
        {
            System.arraycopy(data, Math.max(0, -x) * ySections, data2, 0, len * ySections);
        }
        else if(x > 0)
        {
            System.arraycopy(data, 0, data2, start * ySections, len * ySections);
        }
        else
        {
            System.arraycopy(data, 0, data2, 0, len * ySections);
        }
        if(y < 0) {
            for (int i = start; i < len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L << -y)) << (64 + y);
                    data2[i * ySections + j] >>>= -y;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        else if(y > 0) {
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

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data2[a] &= yEndMask;
            }
        }
        data = data2;
        return this;
    }
    public GreasedRegion expand()
    {
        if(width < 2 || ySections == 0)
            return this;

        long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            next[a] |= (data[a] << 1) | (data[a] >>> 1) | (data[a+ySections]);
            next[(width-1)*ySections+a] |= (data[(width-1)*ySections+a] << 1) | (data[(width-1)*ySections+a] >>> 1) | (data[(width-2)*ySections+a]);

            for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                next[i] |= (data[i] << 1) | (data[i] >>> 1) | (data[i - ySections]) | (data[i + ySections]);
            }

            if(a > 0) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i - 1] & 0x8000000000000000L) >>> 63;
                }
            }

            if(a < ySections - 1) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i + 1] & 1L) << 63;
                }
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion expand(int amount)
    {
        for (int i = 0; i < amount; i++) {
            expand();
        }
        return this;
    }

    public GreasedRegion[] expandSeries(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.expand());
        }
        return regions;
    }

    public GreasedRegion fringe()
    {
        GreasedRegion cpy = new GreasedRegion(this);
        expand();
        return andNot(cpy);
    }
    public GreasedRegion fringe(int amount)
    {
        GreasedRegion cpy = new GreasedRegion(this);
        expand(amount);
        return andNot(cpy);
    }

    public GreasedRegion[] fringeSeries(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
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

    public GreasedRegion retract()
    {
        if(width <= 2 || ySections <= 0)
            return this;

        long[] next = new long[width * ySections];
        System.arraycopy(data, ySections, next, ySections, (width - 2) * ySections);
        for (int a = 0; a < ySections; a++) {
            if(a > 0 && a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & (data[i - ySections])
                            & (data[i + ySections]);
                }
            }
            else if(a > 0) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & (data[i] >>> 1)
                            & (data[i - ySections])
                            & (data[i + ySections]);
                }
            }
            else if(a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1)
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & (data[i - ySections])
                            & (data[i + ySections]);
                }
            }
            else // only the case when ySections == 1
            {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1) & (data[i] >>> 1) & (data[i - ySections]) & (data[i + ySections]);
                }
            }
        }

        if(yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }
    public GreasedRegion retract(int amount)
    {
        for (int i = 0; i < amount; i++) {
            retract();
        }
        return this;
    }

    public GreasedRegion[] retractSeries(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.retract());
        }
        return regions;
    }

    public GreasedRegion surface()
    {
        GreasedRegion cpy = new GreasedRegion(this).retract();
        return xor(cpy);
    }
    public GreasedRegion surface(int amount)
    {
        GreasedRegion cpy = new GreasedRegion(this).retract(amount);
        return xor(cpy);
    }

    public GreasedRegion[] surfaceSeries(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
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

    public GreasedRegion expand8way()
    {
        if(width < 2 || ySections <= 0)
            return this;

        long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            next[a] |= (data[a] << 1) | (data[a] >>> 1)
                    | (data[a+ySections]) | (data[a+ySections] << 1) | (data[a+ySections] >>> 1);
            next[(width-1)*ySections+a] |= (data[(width-1)*ySections+a] << 1) | (data[(width-1)*ySections+a] >>> 1)
                    | (data[(width-2)*ySections+a]) | (data[(width-2)*ySections+a] << 1) | (data[(width-2)*ySections+a] >>> 1);

            for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                next[i] |= (data[i] << 1) | (data[i] >>> 1)
                        | (data[i - ySections]) | (data[i - ySections] << 1) | (data[i - ySections] >>> 1)
                        | (data[i + ySections]) | (data[i + ySections] << 1) | (data[i + ySections] >>> 1);
            }

            if(a > 0) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= ((data[i - 1] & 0x8000000000000000L) >>> 63) |
                            ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63) |
                            ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63);
                }
            }

            if(a < ySections - 1) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= ((data[i + 1] & 1L) << 63) |
                            ((data[i - ySections + 1] & 1L) << 63) |
                            ((data[i + ySections+ 1] & 1L) << 63);
                }
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion expand8way(int amount)
    {
        for (int i = 0; i < amount; i++) {
            expand8way();
        }
        return this;
    }

    public GreasedRegion[] expandSeries8way(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.expand8way());
        }
        return regions;
    }

    public GreasedRegion fringe8way()
    {
        GreasedRegion cpy = new GreasedRegion(this);
        expand8way();
        return andNot(cpy);
    }
    public GreasedRegion fringe8way(int amount)
    {
        GreasedRegion cpy = new GreasedRegion(this);
        expand8way(amount);
        return andNot(cpy);
    }

    public GreasedRegion[] fringeSeries8way(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
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

    public GreasedRegion retract8way()
    {
        if(width <= 2 || ySections <= 0)
            return this;

        long[] next = new long[width * ySections];
        System.arraycopy(data, ySections, next, ySections, (width - 2) * ySections);
        for (int a = 0; a < ySections; a++) {
            if(a > 0 && a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & (data[i - ySections])
                            & (data[i + ySections])
                            & ((data[i - ySections] << 1) | ((data[i - 1 - ySections] & 0x8000000000000000L) >>> 63))
                            & ((data[i + ySections] << 1) | ((data[i - 1 + ySections] & 0x8000000000000000L) >>> 63))
                            & ((data[i - ySections] >>> 1) | ((data[i + 1 - ySections] & 1L) << 63))
                            & ((data[i + ySections] >>> 1) | ((data[i + 1 + ySections] & 1L) << 63));
                }
            }
            else if(a > 0) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & (data[i] >>> 1)
                            & (data[i - ySections])
                            & (data[i + ySections])
                            & ((data[i - ySections] << 1) | ((data[i - 1 - ySections] & 0x8000000000000000L) >>> 63))
                            & ((data[i + ySections] << 1) | ((data[i - 1 + ySections] & 0x8000000000000000L) >>> 63))
                            & (data[i - ySections] >>> 1)
                            & (data[i + ySections] >>> 1);
                }
            }
            else if(a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1)
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & (data[i - ySections])
                            & (data[i + ySections])
                            & (data[i - ySections] << 1)
                            & (data[i + ySections] << 1)
                            & ((data[i - ySections] >>> 1) | ((data[i + 1 - ySections] & 1L) << 63))
                            & ((data[i + ySections] >>> 1) | ((data[i + 1 + ySections] & 1L) << 63));
                }
            }
            else // only the case when ySections == 1
            {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1)
                            & (data[i] >>> 1)
                            & (data[i - ySections])
                            & (data[i + ySections])
                            & (data[i - ySections] << 1)
                            & (data[i + ySections] << 1)
                            & (data[i - ySections] >>> 1)
                            & (data[i + ySections] >>> 1);
                }
            }
        }

        if(yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion retract8way(int amount)
    {
        for (int i = 0; i < amount; i++) {
            retract8way();
        }
        return this;
    }

    public GreasedRegion[] retractSeries8way(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.retract8way());
        }
        return regions;
    }

    public GreasedRegion surface8way()
    {
        GreasedRegion cpy = new GreasedRegion(this).retract8way();
        return xor(cpy);
    }

    public GreasedRegion surface8way(int amount)
    {
        GreasedRegion cpy = new GreasedRegion(this).retract8way(amount);
        return xor(cpy);
    }

    public GreasedRegion[] surfaceSeries8way(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
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

    public GreasedRegion flood(GreasedRegion bounds)
    {
        if(width < 2 || ySections <= 0 || bounds == null || bounds.width < 2 || bounds.ySections <= 0)
            return this;

        long[] next = new long[width * ySections];
        for (int a = 0; a < ySections && a < bounds.ySections; a++) {
            next[a] |= (data[a] |(data[a] << 1) | (data[a] >>> 1) | (data[a+ySections])) & bounds.data[a];
            next[(width-1)*ySections+a] = (data[(width-1)*ySections+a] | (data[(width-1)*ySections+a] << 1)
                    | (data[(width-1)*ySections+a] >>> 1) | (data[(width-2)*ySections+a])) & bounds.data[(width-1)*bounds.ySections+a];

            for (int i = ySections+a, j = bounds.ySections+a; i < (width - 1) * ySections &&
                    j < (bounds.width - 1) * bounds.ySections; i+= ySections, j+= bounds.ySections) {
                next[i] = (data[i] | (data[i] << 1) | (data[i] >>> 1) | (data[i - ySections]) | (data[i + ySections])) & bounds.data[j];
            }

            if(a > 0) {
                for (int i = ySections+a, j = bounds.ySections+a; i < (width-1) * ySections && j < (bounds.width-1) * bounds.ySections;
                     i+= ySections, j += bounds.ySections) {
                    next[i] = (data[i] | ((data[i - 1] & 0x8000000000000000L) >>> 63)) & bounds.data[j];
                }
            }

            if(a < ySections - 1 && a < bounds.ySections - 1) {
                for (int i = ySections+a, j = bounds.ySections+a;
                     i < (width-1) * ySections && j < (bounds.width-1) * bounds.ySections; i+= ySections, j += bounds.ySections) {
                    next[i] = (data[i] | ((data[i + 1] & 1L) << 63)) & bounds.data[j];
                }
            }
        }

        if(yEndMask != -1 && bounds.yEndMask != -1) {
            if(ySections == bounds.ySections) {
                long mask = ((yEndMask >>> 1) <= (bounds.yEndMask >>> 1))
                        ? yEndMask : bounds.yEndMask;
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= mask;
                }
            }
            else if(ySections < bounds.ySections) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            else {
                for (int a = bounds.ySections - 1; a < next.length; a += ySections) {
                    next[a] &= bounds.yEndMask;
                }
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion flood(GreasedRegion bounds, int amount)
    {
        int ct = count(), ct2;
        for (int i = 0; i < amount; i++) {
            flood(bounds);
            if(ct == (ct2 = count()))
                break;
            else
                ct = ct2;

        }
        return this;
    }


    public GreasedRegion[] floodSeries(GreasedRegion bounds, int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        int ct = count(), ct2;
        GreasedRegion[] regions = new GreasedRegion[amount];
        boolean done = false;
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            if(done) {
                regions[i] = new GreasedRegion(temp);
            }
            else {
                regions[i] = new GreasedRegion(temp.flood(bounds));
                if (ct == (ct2 = temp.count()))
                    done = true;
                else
                    ct = ct2;
            }
        }
        return regions;
    }

    public GreasedRegion flood8way(GreasedRegion bounds)
    {
        if(width < 2 || ySections <= 0 || bounds == null || bounds.width < 2 || bounds.ySections <= 0)
            return this;

        long[] next = new long[width * ySections];
        for (int a = 0; a < ySections && a < bounds.ySections; a++) {
            next[a] = (data[a] | (data[a] << 1) | (data[a] >>> 1)
                    | (data[a+ySections]) | (data[a+ySections] << 1) | (data[a+ySections] >>> 1)) & bounds.data[a];
            next[(width-1)*ySections+a] = (data[(width-1)*ySections+a]
                    | (data[(width-1)*ySections+a] << 1) | (data[(width-1)*ySections+a] >>> 1)
                    | (data[(width-2)*ySections+a]) | (data[(width-2)*ySections+a] << 1) | (data[(width-2)*ySections+a] >>> 1))
                    & bounds.data[(width-1)*bounds.ySections+a];

            for (int i = ySections+a, j = bounds.ySections+a; i < (width - 1) * ySections &&
                    j < (bounds.width - 1) * bounds.ySections; i+= ySections, j+= bounds.ySections) {
                next[i] = (data[i] | (data[i] << 1) | (data[i] >>> 1)
                        | (data[i - ySections]) | (data[i - ySections] << 1) | (data[i - ySections] >>> 1)
                        | (data[i + ySections]) | (data[i + ySections] << 1) | (data[i + ySections] >>> 1))
                        & bounds.data[j];
            }

            if(a > 0) {
                for (int i = ySections+a, j = bounds.ySections+a; i < (width-1) * ySections && j < (bounds.width-1) * bounds.ySections;
                     i+= ySections, j += bounds.ySections) {
                    next[i] = (data[i] | ((data[i - 1] & 0x8000000000000000L) >>> 63) |
                            ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63) |
                            ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63)) & bounds.data[j];
                }
            }

            if(a < ySections - 1 && a < bounds.ySections - 1) {
                for (int i = ySections+a, j = bounds.ySections+a;
                     i < (width-1) * ySections && j < (bounds.width-1) * bounds.ySections; i+= ySections, j += bounds.ySections) {
                    next[i] = (data[i] | ((data[i + 1] & 1L) << 63) |
                            ((data[i - ySections + 1] & 1L) << 63) |
                            ((data[i + ySections+ 1] & 1L) << 63)) & bounds.data[j];
                }
            }
        }

        if(yEndMask != -1 && bounds.yEndMask != -1) {
            if(ySections == bounds.ySections) {
                long mask = ((yEndMask >>> 1) <= (bounds.yEndMask >>> 1))
                        ? yEndMask : bounds.yEndMask;
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= mask;
                }
            }
            else if(ySections < bounds.ySections) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            else {
                for (int a = bounds.ySections - 1; a < next.length; a += ySections) {
                    next[a] &= bounds.yEndMask;
                }
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion flood8way(GreasedRegion bounds, int amount)
    {
        int ct = count(), ct2;
        for (int i = 0; i < amount; i++) {
            flood8way(bounds);
            if(ct == (ct2 = count()))
                break;
            else
                ct = ct2;
        }
        return this;
    }

    public GreasedRegion[] floodSeries8way(GreasedRegion bounds, int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        int ct = count(), ct2;
        GreasedRegion[] regions = new GreasedRegion[amount];
        boolean done = false;
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            if(done) {
                regions[i] = new GreasedRegion(temp);
            }
            else {
                regions[i] = new GreasedRegion(temp.flood8way(bounds));
                if (ct == (ct2 = temp.count()))
                    done = true;
                else
                    ct = ct2;
            }
        }
        return regions;
    }

    /**
     * If this GreasedRegion stores multiple unconnected "on" areas, this finds each isolated area (areas that
     * are only adjacent diagonally are considered separate from each other) and returns it as an element in an
     * ArrayList of GreasedRegion, with one GreasedRegion per isolated area. Useful when you have, for example, all the
     * rooms in a dungeon with their connecting corridors removed, but want to separate the rooms. You can get the
     * aforementioned data assuming a bare dungeon called map using:
     * <br>
     * {@code GreasedRegion floors = new GreasedRegion(map, '.'),
     * rooms = floors.copy().retract8way().flood(floors, 2),
     * corridors = floors.copy().andNot(rooms),
     * doors = rooms.copy().and(corridors.copy().fringe());}
     * <br>
     * You can then get all rooms as separate regions with {@code List<GreasedRegion> apart = split(rooms);}, or
     * substitute {@code split(corridors)} to get the corridors. The room-finding technique works by shrinking floors
     * by a radius of 1 (8-way), which causes thin areas like corridors of 2 or less width to be removed, then
     * flood-filling the floors out from the area that produces by 2 cells (4-way this time) to restore the original
     * size of non-corridor areas (plus some extra to ensure odd shapes are kept). Corridors are obtained by removing
     * the rooms from floors. The example code also gets the doors (which overlap with rooms, not corridors) by finding
     * where the a room and a corridor are adjacent. This technique is used with some enhancements in the RoomFinder
     * class.
     * @see squidpony.squidgrid.mapping.RoomFinder for a class that uses this technique without exposing GreasedRegion
     * @return an ArrayList containing each unconnected area from packed as a GreasedRegion element
     */
    public ArrayList<GreasedRegion> split()
    {
        ArrayList<GreasedRegion> scattered = new ArrayList<>(32);
        Coord fst = first();
        GreasedRegion remaining = new GreasedRegion(this);
        while (fst.x >= 0) {
            GreasedRegion filled  = new GreasedRegion(fst, width, height).flood(remaining, width * height);
            scattered.add(filled);
            remaining.andNot(filled);
            fst = remaining.first();
        }
        return scattered;
    }

    public boolean intersects(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                if((data[x * ySections + y] & other.data[x * ySections + y]) != 0)
                    return true;
            }
        }
        return false;
    }

    public static OrderedSet<GreasedRegion> whichContain(int x, int y, GreasedRegion ... packed)
    {
        OrderedSet<GreasedRegion> found = new OrderedSet<>(packed.length);
        GreasedRegion tmp;
        for (int i = 0; i < packed.length; i++) {
            if((tmp = packed[i]) != null && tmp.test(x, y))
                found.add(tmp);
        }
        return found;
    }


    public int count()
    {
        int c = 0;
        for (int i = 0; i < width * ySections; i++) {
            c += Long.bitCount(data[i]);
        }
        return c;
    }

    public Coord fit(double xFraction, double yFraction)
    {
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
        xTarget = (int)(xTotal * xFraction);
        for (int x = 0; x < width; x++) {
            if((xTarget -= xCounts[x]) < 0)
            {
                bestX = x;
                yTotal = xCounts[x];
                break;
            }
        }
        if(bestX < 0)
        {
            return Coord.get(-1, -1);
        }
        yTarget = (int)(yTotal * yFraction);

        for (int s = 0, y = 0; s < ySections; s++) {
            t = data[bestX * ySections + s];
            for (long cy = 1; cy != 0 && y < height; y++, cy <<= 1) {
                if((t & cy) != 0 && --yTarget < 0)
                {
                    return Coord.get(bestX, y);
                }
            }
        }

        return new Coord(-1, -1);

    }

    public Coord[] separatedPortion(double fraction)
    {
        if(fraction < 0)
            return new Coord[0];
        if(fraction > 1)
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
        Coord[] vl = new Coord[ct = (int)(fraction * xTotal)];
        SobolQRNG sobol = new SobolQRNG(2);
        double[] vec;
        sobol.skipTo(1337);
        EACH_SOBOL:
        for (int i = 0; i < ct; i++)
        {
            vec = sobol.nextVector();
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


    /*
    // This showed a strong x-y correlation because it didn't have a way to use a non-base-2 van der Corput sequence.
    // It also produced very close-together points, unfortunately.
    public static double quasiRandomX(int idx)
    {
        return atVDCSequence(23L + idx * 255L);
    }
    public static double quasiRandomY(int idx)
    {
        return atVDCSequence(20L + idx);
    }

    private static double atVDCSequence(long idx)
    {
        long leading = Long.numberOfLeadingZeros(idx);
        double t = (Long.reverse(idx) >>> leading) / (1.0 * (1L << (64L - leading)));
        return t;
    }
    */

    public Coord[] asCoords()
    {
        int ct = 0, idx = 0;
        for (int i = 0; i < width * ySections; i++) {
            ct += Long.bitCount(data[i]);
        }
        Coord[] points = new Coord[ct];
        long t, w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = Long.lowestOneBit(t);
                    while (w != 0) {
                        t ^= w;
                        points[idx++] = Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        w = Long.lowestOneBit(t);
                    }
                }
            }
        }
        return points;
    }

    public Coord first()
    {
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

    public Coord singleRandom(RNG rng)
    {
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
                        t ^= w;
                        if (ct == tmp)
                            return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        w = Long.lowestOneBit(t);
                    }
                }
            }
        }

        return new Coord(-1, -1);
    }

    public boolean test(int x, int y)
    {
        return x >= 0 && y >= 0 && x < width && y < height && ySections > 0 &&
                ((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GreasedRegion that = (GreasedRegion) o;

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (ySections != that.ySections) return false;
        if (yEndMask != that.yEndMask) return false;
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

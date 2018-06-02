package squidpony.squidmath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import squidpony.annotation.GwtIncompatible;

/**
 * Interface of a RNG. It's a stripped down version of the original RNG class
 * from SquidLib It's an interface instead of a class, to be able to implement
 * with libgdx's RNG.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 * @author smelC
 */
public interface IRNG {

	/**
	 * Returns a value between min (inclusive) and max (exclusive).
	 *
	 * <p>
	 * The inclusive and exclusive behavior is to match the behavior of the similar
	 * method that deals with floating point values.
	 * </p>
	 * 
	 * <p>
	 * If {@code min} and {@code max} happen to be the same, {@code min} is returned
	 * (breaking the exclusive behavior, but it's convenient to do so).
	 * </p>
	 *
	 * @param min
	 *            the minimum bound on the return value (inclusive)
	 * @param max
	 *            the maximum bound on the return value (exclusive)
	 * @return the found value
	 */
	public int between(int min, int max);

	/**
	 * Returns a random element from the provided Collection, which should have
	 * predictable iteration order if you want predictable behavior for identical
	 * RNG seeds, though it will get a random element just fine for any Collection
	 * (just not predictably in all cases). If you give this a Set, it should be a
	 * LinkedHashSet or some form of sorted Set like TreeSet if you want predictable
	 * results. Any List or Queue should be fine. Map does not implement Collection,
	 * thank you very much Java library designers, so you can't actually pass a Map
	 * to this, though you can pass the keys or values. If coll is empty, returns
	 * null.
	 *
	 * <p>
	 * Requires iterating through a random amount of coll's elements, so performance
	 * depends on the size of coll but is likely to be decent, as long as iteration
	 * isn't unusually slow. This replaces {@code getRandomElement(Queue)}, since
	 * Queue implements Collection and the older Queue-using implementation was
	 * probably less efficient.
	 * </p>
	 * 
	 * <p>
	 * This method is deprecated, because it is often better to call
	 * {@link #getRandomElement(List)} instead. Nevertheless, it is not planned to
	 * remove it.
	 * </p>
	 * 
	 * @param <T>
	 *            the type of the returned object
	 * @param coll
	 *            the Collection to get an element from; remember, Map does not
	 *            implement Collection
	 * @return the randomly selected element
	 */
	@Deprecated
	public <T> T getRandomElement(Collection<T> coll);

	/**
	 * Returns a random element from the provided list. If the list is empty then
	 * null is returned.
	 *
	 * @param <T>
	 *            the type of the returned object
	 * @param list
	 *            the list to get an element from
	 * @return the randomly selected element
	 */
	public <T> T getRandomElement(List<T> list);

	/**
	 * Returns a random element from the provided array and maintains object type.
	 *
	 * @param <T>
	 *            the type of the returned object
	 * @param array
	 *            the array to get an element from
	 * @return the randomly selected element
	 */
	public <T> T getRandomElement(T[] array);

	/**
	 * Get an Iterable that starts at a random location in list and continues on
	 * through list in its current order. Loops around to the beginning after it
	 * gets to the end, stops when it returns to the starting location. <br>
	 * You should not modify {@code list} while you use the returned reference. And
	 * there'll be no ConcurrentModificationException to detect such erroneous uses.
	 * 
	 * @param list
	 *            A list <b>with a constant-time {@link List#get(int)} method</b>
	 *            (otherwise performance degrades).
	 * @return An {@link Iterable} that iterates over {@code list} but start at a
	 *         random index. If the chosen index is {@code i}, the iterator will
	 *         return:
	 *         {@code list[i]; list[i+1]; ...; list[list.length() - 1]; list[0]; list[i-1]}
	 *
	 */
	public <T> Iterable<T> getRandomStartIterable(final List<T> list);

	/**
	 * Get a random bit of state, interpreted as true or false with approximately
	 * equal likelihood.
	 * 
	 * @return a random boolean.
	 */
	public boolean nextBoolean();

	/**
	 * This returns a maximum of 0.99999994 because that is the largest Float value
	 * that is less than 1.0f
	 *
	 * @return a value between 0 (inclusive) and 0.99999994 (inclusive)
	 */
	public float nextFloat();

	/**
	 * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both
	 * inclusive).
	 * 
	 * @return a 32-bit random int.
	 */
	public int nextInt();

	/**
	 * Returns a random non-negative integer below the given bound, or 0 if the
	 * bound is 0 or negative.
	 *
	 * @param bound
	 *            the upper bound (exclusive)
	 * @return the found number
	 */
	public int nextInt(final int bound);

	/**
	 * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
	 * 
	 * @return a 64-bit random long.
	 */
	public long nextLong();

	/**
	 * Returns a random non-negative short below the given bound, or 0 if the bound
	 * is 0 or negative.
	 * 
	 * @param bound
	 *            the upper bound (exclusive)
	 * @return a 64-bit random long.
	 */
	public short nextShort(short bound);

	/**
	 * Shuffle an array using the Fisher-Yates algorithm and returns a shuffled
	 * copy. Not GWT-compatible; use the overload that takes two arrays if you use
	 * GWT. <br>
	 * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
	 * 
	 * @param elements
	 *            an array of T; will not be modified
	 * @param <T>
	 *            can be any non-primitive type.
	 * @return a shuffled copy of elements
	 */
	@GwtIncompatible
	public <T> T[] shuffle(T[] elements);

	/**
	 * Shuffles an array in place using the Fisher-Yates algorithm. If you don't
	 * want the array modified, use {@link #shuffle(Object[], Object[])}. Unlike
	 * {@link #shuffle(Object[])}, this is GWT-compatible. <br>
	 * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
	 * 
	 * @param elements
	 *            an array of T; <b>will</b> be modified
	 * @param <T>
	 *            can be any non-primitive type.
	 */
	public <T> void shuffleInPlace(T[] elements);

	public <T> void shuffleInPlace(List<T> elements);

	/**
	 * Shuffle an array using the "inside-out" Fisher-Yates algorithm. DO NOT give
	 * the same array for both elements and dest, since the prior contents of dest
	 * are rearranged before elements is used, and if they refer to the same array,
	 * then you can end up with bizarre bugs where one previously-unique item shows
	 * up dozens of times. If possible, create a new array with the same length as
	 * elements and pass it in as dest; the returned value can be assigned to
	 * whatever you want and will have the same items as the newly-formed array.
	 * <br>
	 * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_.22inside-
	 * out.22_algorithm
	 * 
	 * @param elements
	 *            an array of T; will not be modified
	 * @param <T>
	 *            can be any non-primitive type.
	 * @param dest
	 *            Where to put the shuffle. If it does not have the same length as
	 *            {@code elements}, this will use the randomPortion method of this
	 *            class to fill the smaller dest. MUST NOT be the same array as
	 *            elements!
	 * @return {@code dest} after modifications
	 */
	/* This method has this prototype to be compatible with GWT. */
	public <T> T[] shuffle(T[] elements, T[] dest);

	/**
	 * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm. The
	 * result is allocated if {@code buf} is null or if {@code buf} isn't empty,
	 * otherwise {@code elements} is poured into {@code buf}.
	 * 
	 * @param elements
	 *            a Collection of T; will not be modified
	 * @param <T>
	 *            can be any non-primitive type.
	 * @return a shuffled ArrayList containing the whole of elements in
	 *         pseudo-random order.
	 */
	public <T> ArrayList<T> shuffle(Collection<T> elements, /* @Nullable */ ArrayList<T> buf);

	/**
	 * @return The state of {@code this}. May be {@code this} itself, maybe not.
	 */
	public Serializable toSerializable();
}

package squidpony;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import squidpony.panel.IColoredString;

/**
 * An helper class for code that deals with lists of {@link IColoredString}s. It
 * does nothing smart, its only purpose is to save you some typing for frequent
 * calls. It is particularly useful when feeding large pieces of text to classes
 * like TextPanel in the display module.
 * 
 * TODO CH Deprecate me
 * 
 * @author smelC
 */
public class ColoredStringList<T> extends ArrayList<IColoredString<T>> {

	private static final long serialVersionUID = -5111205714079762803L;

	/**
	 * Character used by libgdx's Label and friends to determine when to output a
	 * new line when wrapping text.
	 */
	public static final String GDX_NEWLINE = "\n";

	public ColoredStringList() {
		super();
	}

	public ColoredStringList(int expectedSize) {
		super(expectedSize);
	}

	/**
	 * @return A fresh empty instance.
	 */
	public static <T> ColoredStringList<T> create() {
		return new ColoredStringList<T>();
	}

	/**
	 * @param expectedSize
	 * @return A fresh empty instance.
	 */
	public static <T> ColoredStringList<T> create(int expectedSize) {
		return new ColoredStringList<T>(expectedSize);
	}

	/**
	 * Appends {@code text} to {@code this}, without specifying its color.
	 * 
	 * @param text
	 *            the text to append
	 */
	public void addText(String text) {
		addColoredText(text, null);
	}

	/**
	 * Appends {@code text} to {@code this}.
	 * 
	 * @param text
	 *            the text to append
	 */
	public void addText(IColoredString<T> text) {
		final int sz = size();
		if (sz == 0)
			add(text);
		else {
			get(sz - 1).append(text);
		}
	}

	/**
	 * Appends colored text to {@code this}.
	 * 
	 * @param text
	 *            the text to append
	 */
	public void addText(String text, T c) {
		addColoredText(text, c);
	}

	/**
	 * Appends colored text to {@code this}.
	 * 
	 * @param text
	 *            the text to append
	 */
	public void addColoredText(String text, T c) {
		if (isEmpty())
			add(IColoredString.Impl.create(text, c));
		else {
			final IColoredString<T> last = get(size() - 1);
			last.append(text, c);
		}
	}

	/**
	 * Appends text to {@code this}, on a new line; without specifying its color.
	 * 
	 * @param text
	 *            the text to append
	 */
	public void addTextOnNewLine(String text) {
		addColoredTextOnNewLine(text, null);
	}

	public void addTextOnNewLine(IColoredString<T> text) {
		add(text);
	}

	/**
	 * Appends colored text to {@code this}.
	 * 
	 * @param text
	 *            the text to append
	 */
	public void addColoredTextOnNewLine(String text, /* @Nullable */ T color) {
		this.add(IColoredString.Impl.<T>create(GDX_NEWLINE + text, color));
	}

	/**
	 * Adds {@code texts} to {@code this}, starting a new line for the first one.
	 * 
	 * @param texts
	 *            the Collection of objects extending IColoredString to append
	 */
	public void addOnNewLine(Collection<? extends IColoredString<T>> texts) {
		final Iterator<? extends IColoredString<T>> it = texts.iterator();
		boolean first = true;
		while (it.hasNext()) {
			if (first) {
				addTextOnNewLine(it.next());
				first = false;
			} else
				addText(it.next());
		}
	}

	/**
	 * Contrary to {@link Collection#addAll(Collection)}, this method appends text
	 * to the current text, without inserting new lines.
	 *
	 * @param texts
	 *            the Collection of objects extending IColoredString to append
	 */
	public void addAllText(Collection<? extends IColoredString<T>> texts) {
		for (IColoredString<T> text : texts)
			addText(text);
	}

	/**
	 * Jumps a line.
	 */
	public void addEmptyLine() {
		this.add(IColoredString.Impl.<T>create(GDX_NEWLINE, null));
	}

	/**
	 * Changes a color in members of {@code this}.
	 * 
	 * @param old
	 *            The color to replace. Can be {@code null}.
	 */
	public void replaceColor(T old, T new_) {
		final int sz = size();
		for (int i = 0; i < sz; i++) {
			final IColoredString<T> ics = get(i);
			if (ics != null)
				ics.replaceColor(old, new_);
		}
	}

}

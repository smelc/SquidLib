package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import squidpony.squidmath.OrderedMap;

/**
 * Class for creating text blocks.
 *
 * This class defaults to having no padding and having no font set. You can use
 * a default square or narrow font by calling the appropriate method, or set the
 * font to any AngelCode bitmap font on the classpath (typically in libGDX, this
 * would be in the assets folder; these fonts can be created by Hiero in the
 * libGDX tools, see https://github.com/libgdx/libgdx/wiki/Hiero for more)
 *
 * After all settings are set, one of the initialization methods must be called
 * before the factory can be used.
 *
 * In order to easily support Unicode, strings are treated as a series of code
 * points.
 *
 * All images have transparent backgrounds.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 */
public class TextCellFactory implements Disposable {

	private final BitmapFont bmpFont;

	protected Texture block = null;
	protected String fitting = SQUID_FITTING;
	protected int width = 1, height = 1;
	protected float actualCellWidth = 1, actualCellHeight = 1;
	private boolean initialized = false;
	protected boolean distanceField = false;
	protected ShaderProgram shader;
	protected float descent;
	private Label.LabelStyle style;
	protected OrderedMap<String, String> swap = new OrderedMap<>(32);
	/**
	 * The commonly used symbols in roguelike games.
	 */
	public static final String DEFAULT_FITTING = "@!#$%^&*()_+1234567890-=~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz;:,'\"{}?/\\ ",
			LINE_FITTING = "┼├┤┴┬┌┐└┘│─", SQUID_FITTING = DEFAULT_FITTING + LINE_FITTING;

	/**
	 * @param font
	 *            The font to use
	 */
	public TextCellFactory(BitmapFont font) {
		this.bmpFont= font;
		swap.put("\u0006", " ");
	}

	/**
	 * If using a distance field font, you MUST call this at some point while the
	 * batch has begun, or use code that calls it for you (which is now much of
	 * SquidLib). A typical point to call it is in the "void draw(Batch batch, float
	 * parentAlpha)" method or an overriding method for a Scene2D class. You should
	 * call configureShader rarely, typically only a few times per frame if there
	 * are no images to render, and this means the logical place to call it is in
	 * the outermost Group that contains any SquidPanel objects or other widgets. If
	 * you have multipleTextCellFactory objects, each one needs to have
	 * configureShader called before it is used to draw. <br>
	 * SquidLayers and SquidPanel already call this method in their draw overrides,
	 * so you don't need to call this manually if you use SquidLayers or SquidPanel.
	 * <br>
	 * If you don't use a distance field font, you don't need to call this, but
	 * calling it won't cause problems.
	 *
	 * @param batch
	 *            the Batch, such as a SpriteBatch, to configure to render distance
	 *            field fonts if necessary.
	 */
	public void configureShader(Batch batch) {
		if (initialized && distanceField) {
			batch.setShader(shader);
			shader.setUniformf("u_smoothing", 3.5f * bmpFont.getData().scaleX);

		}
	}

	/**
	 * Releases all resources of this object.
	 */
	@Override
	public void dispose() {
		if (bmpFont != null)
			bmpFont.dispose();
		if (block != null)
			block.dispose();
	}

	/**
	 * Use the specified Batch to draw a String (often just one char long) in the
	 * specified LibGDX Color, with x and y determining the world-space coordinates
	 * for the upper-left corner.
	 *
	 * @param batch
	 *            the LibGDX Batch to do the drawing
	 * @param s
	 *            the string to draw, often but not necessarily one char. Can be
	 *            null to draw a solid block instead.
	 * @param color
	 *            the LibGDX Color to draw the char(s) with, all the same color
	 * @param x
	 *            x of the upper-left corner of the region of text in world
	 *            coordinates.
	 * @param y
	 *            y of the upper-left corner of the region of text in world
	 *            coordinates.
	 */
	public void draw(Batch batch, String s, Color color, float x, float y) {
		if (!initialized) {
			throw new IllegalStateException("This factory has not yet been initialized!");
		}

		if (s == null) {
			Color orig = batch.getColor();
			batch.setColor(color);
			batch.draw(block, x, y - actualCellHeight, actualCellWidth, actualCellHeight); // descent * 1 / 3f
			batch.setColor(orig);
		} else if (s.length() > 0 && s.charAt(0) == '\0') {
			Color orig = batch.getColor();
			batch.setColor(color);
			batch.draw(block, x, y - actualCellHeight, actualCellWidth * s.length(), actualCellHeight); // descent * 1 /
																										// 3f
			batch.setColor(orig);
		} else {
			bmpFont.setColor(color);
			if (swap.containsKey(s))
				bmpFont.draw(batch, swap.get(s), x,
						y - descent + 1/* * 1.5f *//* - lineHeight * 0.2f */ /* + descent */, width * s.length(),
						Align.center, false);
			else
				bmpFont.draw(batch, s, x, y - descent + 1/* * 1.5f *//* - lineHeight * 0.2f */ /* + descent */,
						width * s.length(), Align.center, false);
		}
	}

	/**
	 * Use the specified Batch to draw a String (often just one char long) with the
	 * default color (white), with x and y determining the world-space coordinates
	 * for the upper-left corner.
	 *
	 * @param batch
	 *            the LibGDX Batch to do the drawing
	 * @param s
	 *            the string to draw, often but not necessarily one char. Can be
	 *            null to draw a solid block instead.
	 * @param x
	 *            x of the upper-left corner of the region of text in world
	 *            coordinates.
	 * @param y
	 *            y of the upper-left corner of the region of text in world
	 *            coordinates.
	 */
	public void draw(Batch batch, String s, float x, float y) {
		if (!initialized) {
			throw new IllegalStateException("This factory has not yet been initialized!");
		}

		// + descent * 3 / 2f
		// - distanceFieldScaleY / 12f

		// height - lineTweak * 2f
		if (s == null) {
			batch.setColor(1f, 1f, 1f, 1f);
			batch.draw(block, x, y - actualCellHeight, actualCellWidth, actualCellHeight); // + descent * 1 / 3f
		} else if (s.length() > 0 && s.charAt(0) == '\0') {
			batch.setColor(1f, 1f, 1f, 1f);
			batch.draw(block, x, y - actualCellHeight, actualCellWidth * s.length(), actualCellHeight); // descent * 1 /
																										// 3f
		} else {
			bmpFont.setColor(1f, 1f, 1f, 1f);
			if (swap.containsKey(s))
				bmpFont.draw(batch, swap.get(s), x,
						y - descent + 1/* * 1.5f *//* - lineHeight * 0.2f */ /* + descent */, width * s.length(),
						Align.center, false);
			else
				bmpFont.draw(batch, s, x, y - descent + 1/* * 1.5f *//* - lineHeight * 0.2f */ /* + descent */,
						width * s.length(), Align.center, false);
		}
	}

	/**
	 * Returns the font used by this factory.
	 *
	 * @return the font
	 */
	public BitmapFont font() {
		return bmpFont;
	}

	/**
	 * Returns the height of a single cell.
	 *
	 * @return the height of a single cell
	 */
	public int height() {
		return height;
	}

	/**
	 * Sets the factory's cell height to the provided value. Clamps at 1 on the
	 * lower bound to ensure valid calculations.
	 *
	 * @param height
	 *            the desired width
	 * @return this factory for method chaining
	 */
	public TextCellFactory height(int height) {
		this.height = Math.max(1, height);
		actualCellHeight = this.height;
		return this;
	}

	/**
	 * Initializes the factory to then be able to create text cells on demand.
	 *
	 * Will match the width and height to 12 and 12, scaling the font to fit.
	 *
	 * Calling this after the factory has already been initialized will
	 * re-initialize it.
	 *
	 * @return this for method chaining
	 */
	public TextCellFactory initByFont() {
		bmpFont.setFixedWidthGlyphs(fitting);
		width = (int) bmpFont.getSpaceWidth();
		height = (int) (bmpFont.getLineHeight());
		descent = bmpFont.getDescent();

		actualCellWidth = width;
		actualCellHeight = height;
		// modifiedHeight = height;
		Pixmap temp = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		temp.setColor(Color.WHITE);
		temp.fill();
		block = new Texture(1, 1, Pixmap.Format.RGBA8888);
		block.draw(temp, 0, 0);
		temp.dispose();
		style = new Label.LabelStyle(bmpFont, null);
		initialized = true;
		return this;
	}

	/**
	 * Returns true if this factory is fully initialized and ready to build text
	 * cells.
	 *
	 * @return true if initialized
	 */
	public boolean initialized() {
		return initialized;
	}

	/**
	 * Converts a String into a Label, or if the argument s is null, creates an
	 * Image of a solid block. Can be used for preparing glyphs for animation
	 * effects, and is used internally for this purpose.
	 * 
	 * @param s
	 *            a String to make into an Actor, which can be null for a solid
	 *            block.
	 * @param color
	 *            a Color to tint s with.
	 * @return the Actor, with no position set.
	 */
	public Actor makeActor(String s, Color color) {
		if (!initialized) {
			throw new IllegalStateException("This factory has not yet been initialized!");
		}
		if (s == null) {
			Image im = new Image(block);
			im.setColor(color);
			// im.setSize(width, height - MathUtils.ceil(bmpFont.getDescent() / 2f));
			im.setSize(actualCellWidth, actualCellHeight + (distanceField ? 1 : 0)); // - lineHeight / actualCellHeight
																						// //+ lineTweak * 1f
			// im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
			return im;
		} else if (s.length() > 0 && s.charAt(0) == '\0') {
			Image im = new Image(block);
			im.setColor(color);
			// im.setSize(width * s.length(), height - MathUtils.ceil(bmpFont.getDescent() /
			// 2f));
			im.setSize(actualCellWidth * s.length(), actualCellHeight + (distanceField ? 1 : 0)); // - lineHeight /
																									// actualCellHeight
																									// //+ lineTweak *
																									// 1f
			// im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
			return im;
		} else {
			Label lb;
			if (swap.containsKey(s))
				lb = new Label(swap.get(s), style);
			else
				lb = new Label(s, style);
			// lb.setFontScale(bmpFont.getData().scaleX, bmpFont.getData().scaleY);
			lb.setSize(width * s.length(), height - descent); // + lineTweak * 1f
			lb.setColor(color);
			// lb.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
			return lb;
		}
	}

	/**
	 * Returns the width of a single cell.
	 *
	 * @return the width
	 */
	public int width() {
		return width;
	}

	/**
	 * Sets the factory's cell width to the provided value. Clamps at 1 on the lower
	 * bound to ensure valid calculations.
	 *
	 * @param width
	 *            the desired width
	 * @return this factory for method chaining
	 */
	public TextCellFactory width(int width) {
		this.width = Math.max(1, width);
		actualCellWidth = this.width;
		return this;
	}
}

package squidpony.squidgrid.gui.gdx;

import java.util.Arrays;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;

import squidpony.IColorCenter;
import squidpony.panel.ISquidPanel;

/**
 * Displays text and images in a grid pattern. Supports basic animations.
 * 
 * Grid width and height settings are in terms of number of cells. Cell width
 * and height are in terms of number of pixels.
 *
 * When text is placed, the background color is set separately from the
 * foreground character. When moved, only the foreground character is moved.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public final class SquidPanel extends Group implements ISquidPanel<Color> {

	protected int animationCount = 0;
	protected final int cellWidth, cellHeight;
	protected int gridWidth, gridHeight, gridOffsetX = 0, gridOffsetY = 0;
	protected final String[][] contents;
	protected final Color[][] colors;
	protected final TextCellFactory textFactory;
	protected float xOffset, yOffset;
	public static final float DEFAULT_ANIMATION_DURATION = 0.12F;

	/**
	 * Builds a panel with the given grid size and all other parameters determined
	 * by the factory. Even if sprite images are being used, a TextCellFactory is
	 * still needed to perform sizing and other utility functions.
	 *
	 * If the TextCellFactory has not yet been initialized, then it will be sized at
	 * 12x12 px per cell. If it is null then a default one will be created and
	 * initialized.
	 *
	 * @param gridWidth
	 *            the number of cells horizontally
	 * @param gridHeight
	 *            the number of cells vertically
	 * @param factory
	 *            the factory to use for cell rendering
	 * @param assetManager
	 * @param center
	 *            The color center to use. Can be {@code null}, but then must be set
	 *            later on with {@link #setColorCenter(IColorCenter)}.
	 */
	public SquidPanel(int gridWidth, int gridHeight, TextCellFactory factory, float xOffset,
			float yOffset) {
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		assert factory != null;
		this.textFactory = factory;
		if (!textFactory.initialized())
			textFactory.initByFont();

		cellWidth = MathUtils.round(textFactory.actualCellWidth);
		cellHeight = MathUtils.round(textFactory.actualCellHeight);

		contents = new String[gridWidth][gridHeight];
		colors = new Color[gridWidth][gridHeight];
		for (int i = 0; i < gridWidth; i++) {
			Arrays.fill(colors[i], Color.CLEAR);
		}

		int w = gridWidth * cellWidth;
		int h = gridHeight * cellHeight;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		setSize(w, h);
	}

	@Override
	public int cellHeight() {
		return cellHeight;
	}

	@Override
	public int cellWidth() {
		return cellWidth;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		textFactory.configureShader(batch);
		Color tmp;
		for (int x = gridOffsetX; x < gridWidth; x++) {
			for (int y = gridOffsetY; y < gridHeight; y++) {
				tmp = colors[x][y];
				textFactory.draw(batch, contents[x][y], tmp, xOffset + /*- getX() + 1f * */ x * cellWidth,
						yOffset + /*- getY() + 1f * */ (gridHeight - y) * cellHeight + 1f);
			}
		}
		super.draw(batch, parentAlpha);
	}

	/**
	 * @return The {@link TextCellFactory} backing {@code this}.
	 */
	public TextCellFactory getTextCellFactory() {
		return textFactory;
	}

	@Override
	public int gridHeight() {
		return gridHeight;
	}

	@Override
	public int gridWidth() {
		return gridWidth;
	}

	@Override
	public boolean hasActiveAnimations() {
		// return animationCount != 0;
		if (0 < animationCount)
			return true;
		else
			return 0 < getActions().size;
	}

	@Override
	public void put(/* @Nullable */char[][] chars, Color[][] foregrounds) {
		if (chars == null) {
			/* Only colors to put */
			final int width = foregrounds.length;
			final int height = width == 0 ? 0 : foregrounds[0].length;
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++)
					put(x, y, foregrounds[x][y]);
			}
		} else
			put(0, 0, chars, foregrounds);
	}

	/**
	 * Takes a unicode char for input.
	 *
	 * @param x
	 * @param y
	 * @param c
	 * @param color
	 */
	@Override
	public void put(int x, int y, char c, Color color) {
		if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
			return;// skip if out of bounds
		}
		contents[x][y] = String.valueOf(c);
		colors[x][y] = color; // scc.filter(color);
	}

	public void put(int xOffset, int yOffset, char[][] chars, Color[][] foregrounds) {
		for (int x = xOffset; x < xOffset + chars.length; x++) {
			for (int y = yOffset; y < yOffset + chars[0].length; y++) {
				if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {// check for valid input
					put(x, y, chars[x - xOffset][y - yOffset], foregrounds[x - xOffset][y - yOffset]);
				}
			}
		}
	}

	@Override
	public void put(int x, int y, Color color) {
		put(x, y, '\0', color);
	}

	/**
	 * Sets the position of the actor's bottom left corner.
	 *
	 * @param x
	 * @param y
	 */
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		setBounds(x, y, getWidth(), getHeight());
	}

	/**
	 * Slides {@code name} from {@code (x,y)} to {@code (newx, newy)}. If
	 * {@code name} or {@code color} is {@code null}, it is picked from this panel
	 * (thereby removing the current name, if any). This also allows a Runnable to
	 * be given as {@code postRunnable} to be run after the slide completes.
	 *
	 * @param x
	 *            Where to start the slide, horizontally.
	 * @param y
	 *            Where to start the slide, vertically.
	 * @param name
	 *            The name to slide, or {@code null} to pick it from this panel's
	 *            {@code (x,y)} cell.
	 * @param color
	 *            The color to use, or {@code null} to pick it from this panel's
	 *            {@code (x,y)} cell.
	 * @param newX
	 *            Where to end the slide, horizontally.
	 * @param newY
	 *            Where to end the slide, vertically.
	 * @param duration
	 *            The animation's duration.
	 * @param postRunnable
	 *            a Runnable to execute after the slide completes; may be null to do
	 *            nothing.
	 */
	public void slide(int x, int y, final /* @Nullable */ String name, /* @Nullable */ Color color, int newX, int newY,
			float duration, /* @Nullable */ Runnable postRunnable) {
		final Actor a = createActor(x, y, name == null ? contents[x][y] : name, color == null ? colors[x][y] : color,
				false);
		if (a == null)
			return;

		duration = clampDuration(duration);
		animationCount++;

		final int nbActions = 2 + (postRunnable == null ? 0 : 1);

		int index = 0;
		final Action[] sequence = new Action[nbActions];
		final float nextX = adjustX(newX, false);
		final float nextY = adjustY(newY);
		sequence[index++] = Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration);
		if (postRunnable != null) {
			sequence[index++] = Actions.run(postRunnable);
		}
		/*
		 * Do this one last, so that hasActiveAnimations() returns true during
		 * 'postRunnables'
		 */
		sequence[index] = Actions.delay(duration, Actions.run(new Runnable() {
			@Override
			public void run() {
				recallActor(a, name == null);
			}
		}));

		a.addAction(Actions.sequence(sequence));
	}

	/**
	 * Like {@link #tint(int, int, Color, float)}, but waits for {@code delay} (in
	 * seconds) before performing it. Additionally, enqueue {@code postRunnable} for
	 * running after the created action ends.
	 * 
	 * @param delay
	 *            how long to wait in milliseconds before starting the effect
	 * @param x
	 *            the x-coordinate of the cell to tint
	 * @param y
	 *            the y-coordinate of the cell to tint
	 * @param color
	 *            what to transition ae's color towards, and then transition back
	 *            from
	 * @param duration
	 *            how long the total "round-trip" transition should take in
	 *            milliseconds
	 * @param postRunnable
	 *            a Runnable to execute after the tint completes; may be null to do
	 *            nothing.
	 */

	public void tint(float delay, int x, int y, Color color, float duration, Runnable postRunnable) {
		final Actor a = cellToActor(x, y);
		if (a == null)
			return;
		duration = clampDuration(duration);
		animationCount++;

		Color ac = a.getColor();

		final int nbActions = 3 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
		final Action[] sequence = new Action[nbActions];
		int index = 0;
		if (0 < delay)
			sequence[index++] = Actions.delay(delay);
		sequence[index++] = Actions.color(color, duration * 0.3f);
		sequence[index++] = Actions.color(ac, duration * 0.7f);
		if (postRunnable != null) {
			sequence[index++] = Actions.run(postRunnable);
		}
		/*
		 * Do this one last, so that hasActiveAnimations() returns true during
		 * 'postRunnable'
		 */
		sequence[index] = Actions.run(new Runnable() {
			@Override
			public void run() {
				recallActor(a, true);
			}
		});

		a.addAction(Actions.sequence(sequence));
	}

	protected float clampDuration(float duration) {
		if (duration < 0.02f)
			return 0.02f;
		else
			return duration;
	}

	protected /* @Nullable */ Actor createActor(int x, int y, String name, Color color, boolean doubleWidth) {
		if (name == null || name.isEmpty())
			return null;
		else {
			final Actor a = textFactory.makeActor(name, color);
			a.setName(name);
			a.setPosition(adjustX(x, doubleWidth) - getX() * 2, adjustY(y) - getY() * 2);
			addActor(a);
			return a;
		}
	}

	private float adjustX(float x, boolean doubleWidth) {
		if (doubleWidth)
			return x * 2 * cellWidth + getX();
		else
			return x * cellWidth + getX();
	}

	private float adjustY(float y) {
		return (gridHeight - y - 1) * cellHeight + getY() + 1;
	}

	/**
	 * Created an Actor from the contents of the given x,y position on the grid.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private Actor cellToActor(int x, int y) {
		return cellToActor(x, y, false);
	}

	/**
	 * Created an Actor from the contents of the given x,y position on the grid;
	 * deleting the grid's String content at this cell.
	 * 
	 * @param x
	 * @param y
	 * @param doubleWidth
	 * @return A fresh {@link Actor}, that has just been added to {@code this}.
	 */
	private Actor cellToActor(int x, int y, boolean doubleWidth) {
		return createActor(x, y, contents[x][y], colors[x][y], doubleWidth);
	}

	private void recallActor(Actor a, boolean restoreSym) {
		animationCount--;
		int x = Math.round((a.getX() - getX()) / cellWidth), y = gridHeight - (int) (a.getY() / cellHeight) - 1;
		// y = gridHeight - (int)((a.getY() - getY()) / cellHeight) - 1;
		if (x < 0 || y < 0 || x >= contents.length || y >= contents[x].length)
			return;
		if (restoreSym)
			contents[x][y] = a.getName();
		removeActor(a);
	}
}

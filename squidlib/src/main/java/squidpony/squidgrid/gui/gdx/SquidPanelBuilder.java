package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import squidpony.IColorCenter;

/**
 * An almost-concrete implementation of {@link IPanelBuilder}. This class makes
 * the assumption that font files are only available for square and even sizes.
 * The only method to implement is {@link #fontfile(int)} that must return the
 * name of the file where a font of a given size lives.
 * 
 * <p>
 * I think this class should be generalized so that it supports cell width and
 * cell height that are proportional (for example: a height that is the double
 * of the width), but not to arbitrary combinations. In that case
 * {@link #adjustCellSize(int)} would become impossible to implement.
 * </p>
 * 
 * @author smelC
 */
public abstract class SquidPanelBuilder extends IPanelBuilder.Skeleton {

	protected final /* @Nullable */ IColorCenter<Color> icc;
	public final /* @Nullable */ AssetManager assetManager;

	protected final int smallestFont;
	protected final int largestFont;

	protected final int fontOffset;

	/**
	 * The color passed to {@link SquidPanel#setDefaultForeground(Color)} when
	 * building a new panel, if non-{@code null}.
	 */
	protected /* @Nullable */ Color defaultForegroundColor;

	/**
	 * @param smallestFont
	 *            The smallest font size available.
	 * @param largestFont
	 *            The largest font size available.
	 * @param fontOffset
	 *            This offset is added to the cell size when computing the font size
	 *            for a given cell size.
	 * @param icc
	 *            The color center to give to
	 *            {@link SquidPanel#setColorCenter(IColorCenter)}, or {@code null}
	 *            not to call this method.
	 * @param assetManager
	 */
	public SquidPanelBuilder(int smallestFont, int largestFont, int fontOffset, /* @Nullable */IColorCenter<Color> icc,
			/* @Nullable */ AssetManager assetManager) {
		this.icc = icc;
		this.assetManager = assetManager;

		this.smallestFont = smallestFont;
		this.largestFont = largestFont;
		this.fontOffset = fontOffset;
	}

	@Override
	public SquidPanel buildScreenWide(int screenWidth, int screenHeight, int desiredCellSize,
			/* @Nullable */ TextCellFactory tcf) {
		final int sz = adjustCellSize(desiredCellSize);
		final int hcells = screenWidth / sz;
		final int vcells = screenHeight / sz;
		final SquidPanel result = buildByCells(hcells, vcells, sz, sz, tcf);
		final int vmargin = screenHeight - (vcells * sz);
		final int hmargin = screenWidth - (hcells * sz);
		result.setPosition(hmargin / 2, vmargin / 2);
		/* TODO smelC Draw margins ? */
		return result;
	}

	@Override
	public SquidPanel buildByCells(int hCells, int vCells, int cellWidth, int cellHeight,
			/* @Nullable */ TextCellFactory tcf_) {
		if (cellWidth != cellHeight)
			throw new IllegalStateException("Non square cells aren't supported");

		final TextCellFactory tcf;
		if (tcf_ != null && tcf_.width() == cellWidth && tcf_.height() == cellHeight) {
			/* Can reuse */
			tcf = tcf_;
		} else {
			final int fontSize = fontSizeForCellSize(cellWidth);
			if (!hasFontOfSize(fontSize))
				throw new IllegalStateException("No font of size " + fontSize);

			final String fontpath = fontfile(fontSize);

			assetManager.load(new AssetDescriptor<BitmapFont>(fontpath, BitmapFont.class));
			/*
			 * We're using the AssetManager not be asynchronous, but to avoid loading a file
			 * twice (because that takes some time (tens of milliseconds)). Hence this KISS
			 * code to avoid having to handle a not-yet-loaded font:
			 */
			assetManager.finishLoading();
			final BitmapFont bmpFont = assetManager.get(fontpath, BitmapFont.class);
			tcf = new TextCellFactory(bmpFont);
			for (TextureRegion region : tcf.font().getRegions())
				region.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
			tcf.initByFont();
			tcf.width(cellWidth).height(cellHeight);
		}

		assert tcf != null;

		final SquidPanel result = new SquidPanel(hCells, vCells, tcf, 0f, 0f);
		if (defaultForegroundColor != null)
			result.setDefaultForeground(defaultForegroundColor);
		return result;
	}

	/**
	 * @param sz
	 * @return A valid cell size, as close as possible to {@code sz}.
	 */
	@Override
	public int adjustCellSize(int sz) {
		int result = sz % 2 == 0 ? sz : sz - 1;
		if (hasFontForCellOfSize(result))
			return result;
		if (cellSizeTooBig(sz)) {
			final int offset = -2;
			while (0 < result) {
				result += offset;
				if (hasFontForCellOfSize(result))
					return result;
			}
			throw new IllegalStateException("There's a bug in the computation of the cell size");
		} else if (cellSizeTooSmall(result)) {
			final int offset;
			offset = 2;
			while (/* It's better to stop one day */ result < 512) {
				result += offset;
				if (hasFontForCellOfSize(result))
					return result;
			}
			throw new IllegalStateException("There's a bug in the computation of the cell size");
		} else
			throw new IllegalStateException("There's a bug in the computation of the cell size");

	}

	@Override
	public boolean cellSizeTooBig(int cellSize) {
		return largestFont < fontSizeForCellSize(cellSize);
	}

	@Override
	public boolean cellSizeTooSmall(int cellSize) {
		return fontSizeForCellSize(cellSize) < smallestFont;
	}

	@Override
	public boolean hasFontForCellOfSize(int cellSize) {
		return hasFontOfSize(fontSizeForCellSize(cellSize));
	}

	@Override
	public int fontSizeForCellSize(int cellSize) {
		return cellSize + fontOffset;
	}

	@Override
	public boolean hasFontOfSize(int sz) {
		return sz % 2 == 0 && smallestFont <= sz && sz <= largestFont;
	}

	/**
	 * @param c
	 *            The default foreground color that freshly created panels will
	 *            have. Can be {@code null} to use {@link SquidPanel}'s default.
	 */
	public void setDefaultForegroundColor(/* @Nullable */Color c) {
		this.defaultForegroundColor = c;
	}

	/**
	 * @param sz
	 * @return The name of the file where the font of size {@code sz} lives.
	 */
	protected abstract String fontfile(int sz);
}

package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;

import squidpony.IFilter;

/**
 * A Filter that converts all colors passed to it to grayscale, like a black and white film.
 */
public class GrayscaleFilter implements IFilter<Color>
{
    public GrayscaleFilter()
    {
    	/* Nothing to do */
    }

    @Override
    public Color alter(float r, float g, float b, float a) {
        float v = (r + g + b) / 3f;
        return new Color(v, v, v, a);
    }
}
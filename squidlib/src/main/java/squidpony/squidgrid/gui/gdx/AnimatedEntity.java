package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * A simple class that wraps an Actor with its grid position, animating state,
 * and if it is a double-width Actor. Created by Tommy Ettinger on 7/22/2015.
 */
public final class AnimatedEntity {

	final Actor actor;
	int gridX, gridY;
	boolean animating = false;
	boolean doubleWidth = false;

	public AnimatedEntity(Actor actor, int x, int y) {
		this.actor = actor;
		gridX = x;
		gridY = y;
	}

	public AnimatedEntity(Actor actor, int x, int y, boolean doubleWidth) {
		this.actor = actor;
		gridX = x;
		gridY = y;
		this.doubleWidth = doubleWidth;
	}
}

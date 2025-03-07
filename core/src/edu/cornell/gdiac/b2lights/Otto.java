package edu.cornell.gdiac.b2lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.physics.obstacle.ObstacleCanvas;
import edu.cornell.gdiac.util.FilmStrip;

/**
 * Player avatar for the plaform game.
 *
 * Note that the constructor does very little. The true initialization happens
 * by reading the JSON value.
 */
public class Otto extends DudeModel{
    /// Whether or not this Otto instance has triggered the blind action
    private boolean inked;
    private float flipScale = 1.0f;

    public Otto() {
        super("Otto");
    }

    /**
     * Gets the current value of <code>inked</code>.
     *
     * @return Whether this Otto instance has inked
     */
    public boolean getInked() {
        return inked;
    }

    /**
     * Update the value of <code>inked</code>.
     *
     * @param value What to set the new value of <code>inked</code> to
     */
    public void setInked(boolean value) {
        inked = value;
    }

    public void setFlipScale(float scale) {
        flipScale = scale;
    }

    public void draw(ObstacleCanvas canvas) {
        float angle = (getPlayerType() != DudeType.OTTO) ? getAngle() : 0;
        FilmStrip filmstrip = getFilmstrip();
        Vector2 center = getCenter();
        if (filmstrip != null) {
            canvas.draw(filmstrip, Color.WHITE, center.x, center.y, getX() * drawScale.x, getY() * drawScale.y, angle, flipScale * getWidthScale(), getHeightScale());
        }
    }
}

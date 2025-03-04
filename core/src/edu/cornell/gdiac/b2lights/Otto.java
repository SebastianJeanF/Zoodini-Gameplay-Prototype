package edu.cornell.gdiac.b2lights;

/**
 * Player avatar for the plaform game.
 *
 * Note that the constructor does very little. The true initialization happens
 * by reading the JSON value.
 */
public class Otto extends DudeModel{
    /// Whether or not this Otto instance has triggered the blind action
    private boolean inked;

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
}

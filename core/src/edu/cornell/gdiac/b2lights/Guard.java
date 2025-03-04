package edu.cornell.gdiac.b2lights;

import com.badlogic.gdx.math.Vector2;

public class Guard extends DudeModel {
    public static final int MAX_CHASE_TIME = 420;
    public static final float FOV_DISTANCE = 7.0f; // Maximum detection distance.
    public static final float FOV_ANGLE = 45.0f; // Total cone angle in degrees.

    private boolean isChasing;
    private boolean meowed;
    private int chaseTimer;

    /**
     * Creates a new dude with degenerate settings
     * <p>
     * The main purpose of this constructor is to set the initial capsule
     * orientation.
     *
     * @param type
     */
    public Guard(String type) {
        super(type);

        isChasing = false;
        meowed = false;
        chaseTimer = 0;
    }

    /** The position that this guard should move to */
    Vector2 target = null;

    public boolean isAgroed() {
        return isChasing;
    }

    public Vector2 getTarget() {
        return target;
    }

    public void setAgroed(Vector2 target, boolean meowed) {
        isChasing = true;
        this.target = target;
        this.meowed = meowed;
    }

    public void deAgro() {
        isChasing = false;
        target = null;
        meowed = false;
    }

    public boolean isMeowed() {
        return meowed;
    }

    public int getChaseTimer() {
        return chaseTimer;
    }

    public void setChaseTimer(int value) {
        chaseTimer = value;
    }
}

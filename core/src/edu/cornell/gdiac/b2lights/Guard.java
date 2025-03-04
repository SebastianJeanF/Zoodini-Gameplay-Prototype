package edu.cornell.gdiac.b2lights;

public class Guard extends DudeModel {

    /**
     * Creates a new dude with degenerate settings
     * <p>
     * The main purpose of this constructor is to set the initial capsule orientation.
     *
     * @param type
     */
    public Guard(String type) {
        super(type);
    }

    boolean isChasing = false;

    DudeModel target = null;

    public boolean isAgroed() {
        return isChasing;
    }

    public DudeModel getTarget() {
        return target;
    }

    public void setAgroed(DudeModel target) {
        isChasing = true;
        this.target = target;
    }

    public void deAgro() {
        isChasing = false;
        target = null;
    }
}

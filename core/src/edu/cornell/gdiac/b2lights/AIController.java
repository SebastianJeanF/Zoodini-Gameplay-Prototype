package edu.cornell.gdiac.b2lights;

public class AIController {
    /**
     * Enumeration to encode the finite state machine.
     */
    private static enum FSMState {
        /** Guard is patrolling without target*/
        NEUTRAL,
        /** Guard is chasing target*/
        CHASE,
        /** Guard is returning to patrol (after being distracted or player
         * goes out of range)*/
        RETURN,
        /** Guard is distracted by meow*/
        DISTRACTED
    }

    /** Guard identifier for this AI controller */
    private int id;
    /** Guard controlled by this AI controller */
    private Guard guard;
    /** Target of the guard (to chase) */
    private DudeModel target; // Maybe add player class (so guard can detect both Otto and Gar)

    /** Current state of the finite state machine */
    private FSMState state;

    public AIController(int id) {
        this.id = id;
        this.state = FSMState.NEUTRAL;
        this.target = null;
    }
}

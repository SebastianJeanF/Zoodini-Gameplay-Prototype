package edu.cornell.gdiac.b2lights;

import com.badlogic.gdx.utils.Queue;

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

    private static enum Movement {
        /** Guard is moving right */
        RIGHT,
        /** Guard is moving up */
        UP,
        /** Guard is moving left */
        LEFT,
        /** Guard is moving down */
        DOWN,
        /** Guard is not moving */
        NO_ACTION
    }

    /** Guard identifier for this AI controller */
    private int id;
    /** Guard controlled by this AI controller */
    private Guard guard;
    /** Target of the guard (to chase) */
    private DudeModel target; // Maybe add player class (so guard can detect both Otto and Gar)
    /** Current state of the finite state machine */
    private FSMState state;
    /** The state of the game (needed by the AI) */
    private GameSession session;


    /**
     * Creates an AIController for the guard with the given id.
     *
     * @param id The unique ship identifier
     * @param session The current game session
     */
    public AIController(int id, GameSession session) {
        this.id = id;
        this.state = FSMState.NEUTRAL;
        this.target = null;
        this.session = session;
    }

    /**
     * Marks on the grid the goal tile for the BFS search
     *
     * @param targetX x-coordinate of the goal tile (in screen coordinates)
     * @param targetY y-coordinate of the goal tile (in screen coordinates)
     */
    private void selectTarget(float targetX, float targetY) {
        // Convert screen coordinates to grid coordinates
        Grid grid = session.getGrid();
        int targetGridX = grid.screenToGridX(targetX);
        int targetGridY = grid.screenToGridY(targetY);
        // Mark the goal tile on the grid
        grid.setGoal(targetGridX, targetGridY);
    }

    /**
     * Returns a movement direction that moves towards a goal tile.
     *
     * Use BFS to find the first step (not the entire path) in the shortest path to the goal tile.
     * The value returned should be a control code.
     *
     * @return a movement direction that moves towards a goal tile
     */
    private int getMoveAlongPathToGoalTile() {
        Grid grid = session.getGrid();

        Queue<int[]> queue = new Queue<>();
        int startX = grid.screenToGridX(guard.getX());
        int startY = grid.screenToGridY(guard.getY());

        grid.setVisited(startX, startY);
        queue.addLast(new int[]{startX, startY, -1});

        /** Direction vectors for 4-way movement (right, up, left, down) */
        int[][] DIRECTIONS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        Movement[] startingSteps = {Movement.DOWN, Movement.RIGHT, Movement.UP, Movement.LEFT};

        // Add the first steps into the queue
        for (int i = 0; i < DIRECTIONS.length; i++) {
            int dx = DIRECTIONS[i][0]; int dy = DIRECTIONS[i][1];
            int newX = startX + dx; int newY = startY + dy;
            if (grid.inBounds(newX, newY) && !grid.isWall(newX, newY)) {
                    grid.setVisited(newX, newY);
                    queue.addLast(new int[]{newX, newY, startingSteps[i].ordinal()});
            }
        }

        // BFS search
        while (!queue.isEmpty()) {
            int[] cords = queue.removeFirst();
            int currX = cords[0]; int currY = cords[1]; int firstStep = cords[2];
            if (grid.isGoal(currX, currY)) {
                if (firstStep == -1) { // goal is starting node
                    return Movement.NO_ACTION.ordinal();
                }
                return firstStep;
            }
            for (int[] dir : DIRECTIONS) { // add neighbors to the queue
                int dx = dir[0]; int dy = dir[1];
                int newX = currX + dx; int newY = currY + dy;
                if (grid.inBounds(newX, newY) && !grid.isWall(newX, newY) && grid.isVisited(newX, newY)) {
                    int[] neiCords = {newX, newY, firstStep};
                    queue.addLast(neiCords);
                    grid.setVisited(newX, newY);
                }
            }
        }
        return Movement.NO_ACTION.ordinal();
    }


}

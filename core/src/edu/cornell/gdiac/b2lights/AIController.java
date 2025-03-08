package edu.cornell.gdiac.b2lights;

import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.math.Vector2;

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

    public static enum Movement {
        /** Guard is moving down */
        DOWN,
        /** Guard is moving up */
        UP,
        /** Guard is moving left */
        LEFT,
        /** Guard is moving right */
        RIGHT,
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
    // private GameSession session;
    /** The state of the game (needed by the AI) */
    private Grid grid;


    /**
     * Creates an AIController for the guard with the given id.
     *
     * @param id The unique ship identifier
     * @param session The current game session
     * @param grid The current game session grid
     */
    public AIController(Guard guard, Grid grid) {
        this.id = id;
        this.state = FSMState.NEUTRAL;
        this.target = null;
        this.grid = grid;
        this.guard = guard;
    }

    /**
     * Marks on the grid the goal tile for the BFS search
     *
     * @param targetX x-coordinate of the goal tile (in screen coordinates)
     * @param targetY y-coordinate of the goal tile (in screen coordinates)
     */
    public void selectTarget(float targetX, float targetY) {
        // Convert screen coordinates to grid coordinates
        // Grid grid = session.getGrid();
//        int targetGridX = grid.screenToGridX(targetX);
//        int targetGridY = grid.screenToGridY(targetY);
        int targetGridX = grid.physicsToGridX(targetX);
        int targetGridY = grid.physicsToGridY(targetY);
        System.out.println("targetX: " + targetX + " targetY: " + targetY);
        System.out.println("targetGridX: " + targetGridX + " targetGridY: " + targetGridY);
        // Mark the goal tile on the grid
        grid.setGoal(targetGridX, targetGridY);
    }


    /**
     * Converts the discrete Movement enum to a continuous movement vector
     */
    public Vector2 movementToVector(Movement movement) {
        switch (movement) {
            case RIGHT: return new Vector2(1, 0);
            case UP: return new Vector2(0, 1);
            case LEFT: return new Vector2(-1, 0);
            case DOWN: return new Vector2(0, -1);
            default: return new Vector2(0, 0); // NO_ACTION
        }
    }

    public Movement ordinalToMovement(int ordinal) {
        switch (ordinal) {
            case 0: return Movement.DOWN;
            case 1: return Movement.UP;
            case 2: return Movement.LEFT;
            case 3: return Movement.RIGHT;
            default: return Movement.NO_ACTION;
        }
    }


    /**
     * Returns a movement direction that moves towards a goal tile.
     *
     * Use BFS to find the first step (not the entire path) in the shortest path to the goal tile.
     * The value returned should be a control code.
     *
     * @return a movement direction that moves towards a goal tile
     */
    public Vector2 getMoveAlongPathToGoalTile(float guardX, float guardY) {

        Queue<int[]> queue = new Queue<>();
        int startX = grid.physicsToGridX(guardX);
        int startY = grid.physicsToGridY(guardY);

        System.out.println("in BFS");
        System.out.println("Is Goal Set: " + grid.isGoalSet());
        System.out.println("startX: " + startX + " startY: " + startY);
        grid.printGoalCoords();



        grid.setVisited(startX, startY);
        queue.addLast(new int[]{startX, startY, -1});

        int[][] DIRECTIONS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        Movement[] startingSteps = {Movement.DOWN, Movement.UP, Movement.LEFT, Movement.RIGHT};

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
            System.out.println("currX: " + currX + " currY: " + currY);
            if (grid.isGoal(currX, currY)) {
                if (firstStep == -1) { // goal is starting node
                    System.out.println("no good 1");
                    return movementToVector(Movement.NO_ACTION);
                }
                return movementToVector(ordinalToMovement(firstStep));
            }
            for (int[] dir : DIRECTIONS) { // add neighbors to the queue
                int dx = dir[0]; int dy = dir[1];
                int newX = currX + dx; int newY = currY + dy;
                if (grid.inBounds(newX, newY) && !grid.isWall(newX, newY) && !grid.isVisited(newX, newY)) {
                    System.out.println("adding neighbor");
                    int[] neiCords = {newX, newY, firstStep};
                    queue.addLast(neiCords);
                    grid.setVisited(newX, newY);
                }
            }
        }
        System.out.println("no action returned");
        return movementToVector(Movement.NO_ACTION);
    }


}

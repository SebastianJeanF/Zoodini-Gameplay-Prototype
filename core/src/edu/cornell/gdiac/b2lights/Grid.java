package edu.cornell.gdiac.b2lights;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * A class representing the 2D grid of the game
 */
public class Grid {

    private static class TileState {
        /** Whether the tile has been visited (for pathfinding) */
        public boolean visited = false;
        /** Whether the tile is a wall */
        public boolean wall = false;
        /** Whether the tile is a goal (for pathfinding) */
        public boolean goal = false;
    }

    /** The board width (in number of tiles) */
    private int width;
    /** The board height (in number of tiles) */
    private int height;
    /** The tile size (px)*/
    private float tileSize;
    /** The grid of booleans representing the map (True if wall, False otherwise) */
    private TileState[][] grid;
    /** The world bounds in physics units */
    private Rectangle bounds;
    /** The scaling factor between physics and screen coordinates */
    private Vector2 scale;

    public Grid(LevelModel levelModel, float cellsPerUnit) {
        this.bounds = levelModel.getBounds();
        this.scale = levelModel.getScale();

        // Determine tile size in physics units
        this.tileSize = 1.0f / cellsPerUnit;

        // Calculate grid dimensions
        this.width = (int)(bounds.width * cellsPerUnit);
        this.height = (int)(bounds.height * cellsPerUnit);

        // Initialize grid
        this.grid = new TileState[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = new TileState();
            }
        }

        initializeFromLevel(levelModel);
    }

    /**
     * Initialize the grid with walls from the level model
     *
     * @param levelModel The level model containing obstacles
     */
    private void initializeFromLevel(LevelModel levelModel) {
        for (Obstacle obj : levelModel.objects) {
            // Skip non-wall objects
            if (!(obj instanceof InteriorModel) && !(obj instanceof ExteriorModel)) {
                continue;
            }

            // For box obstacles, mark the covered cells as walls
            if (obj instanceof InteriorModel) {
                InteriorModel wall = (InteriorModel) obj;
                Vector2 position = new Vector2(wall.getX(), wall.getY());
                Vector2 dimension = new Vector2(wall.getWidth(), wall.getHeight());

                // Convert to grid coordinates
                int startX = physicsToGridX(position.x - dimension.x/2);
                int startY = physicsToGridY(position.y - dimension.y/2);
                int endX = physicsToGridX(position.x + dimension.x/2);
                int endY = physicsToGridY(position.y + dimension.y/2);

                // Mark all covered cells as walls
                for (int x = startX; x <= endX; x++) {
                    for (int y = startY; y <= endY; y++) {
                        if (inBounds(x, y)) {
                            grid[x][y].wall = true;
                        }
                    }
                }
            }

        }
    }

    /**
     * Resets the values of the grid to false
     */
    public void resetGrid() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = new TileState();
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private TileState getTileState(int x, int y) {
        if (!inBounds(x, y)) {
            return null;
        }
        return grid[x][y];
    }

    public boolean isWall(int x, int y) {
        return grid[x][y].wall;
    }

    public boolean isVisited(int x, int y) {
        return grid[x][y].visited;
    }

    public int screenToBoard(float f) {
        return (int)(f / (tileSize));
    }

    public float boardToScreen(int n) {
        return (float) (n + 0.5f) * (tileSize);
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public boolean isGoal(int x, int y) {
        return grid[x][y].goal;
    }


    public void clearMarks() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j].visited = false;
                grid[i][j].goal = false;
            }
        }
    }

    /**
     * Converts physics coordinates to grid coordinates
     *
     * @param physicsX The x coordinate in physics space
     * @return The corresponding x coordinate in grid space
     */
    public int physicsToGridX(float physicsX) {
        return (int)((physicsX - bounds.x) / tileSize);
    }

    /**
     * Converts physics coordinates to grid coordinates
     *
     * @param physicsY The y coordinate in physics space
     * @return The corresponding y coordinate in grid space
     */
    public int physicsToGridY(float physicsY) {
        return (int)((physicsY - bounds.y) / tileSize);
    }

    public void printGrid() {
        System.out.println(width);
        System.out.println(height);
        for (int i = 0; i < width; i++) {
            String[] row = new String[height];
            for (int j = 0; j < height; j++) {
                row[j] = grid[i][j].wall ? "X" : "O";
            }
            System.out.println(String.join(" ", row));
        }
    }

}

package edu.cornell.gdiac.b2lights;

/**
 * A class representing the 2D grid of the game
 */
public class Grid {
    /** The board width (in number of tiles) */
    private int width;
    /** The board height (in number of tiles) */
    private int height;
    /** The tile size (px)*/
    private float cellSize;
    /** The grid of booleans representing the map (True if wall, False otherwise) */
    private boolean[][] grid;

    public Grid(int width, int height, float cellSize) {
        this.width = width;
        this.height = height;
        this.cellSize = cellSize;
        this.grid = new boolean[width][height];
    }

    /**
     * Resets the values of the grid to false
     */
    public void resetGrid() {
        this.grid = new boolean[width][height];
    }

}

package edu.cornell.gdiac.b2lights;

import edu.cornell.gdiac.assets.AssetDirectory;

public class GameSession {
    /** Grid of tiles representing current level */
    private Grid grid;

    /**
     * Creates a new game session
     *
     * This method will call reset() to set up the board.
     *
     * @params assets   The associated asset directory
     */
    public GameSession() {
        reset();
    }

    /**
     * Generates all of the game model objects.
     *
     * This method generates the board and all of the ships. It will use the
     * JSON files in the asset directory to generate these models.
     */
    public void reset() {

    }

    /**
     * Returns the game board
     *
     * @return the game board
     */
    public Grid getGrid() {
        return grid;
    }
}

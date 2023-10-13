package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for its display.
 *
 * @author Jesse Hardy
 */
public class Grid {
    /**
     * Creates an instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Grid.class);
    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     *
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     *
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Check whether a piece can be played in the grid at the given x,y
     *
     * @param piece the piece to play
     * @param placeX the x-coordinate
     * @param placeY the y-coordinate
     * @return whether the piece can be played
     */
    public boolean canPlayPiece(GamePiece piece, int placeX, int placeY){
        logger.info("Checking if we can play the piece {} at {}, {}", piece, placeX, placeY);
        int[][] blocks = piece.getBlocks();
        for(int x=0; x<blocks.length; x++){
            for(int y=0; y< blocks.length; y++){
                if (blocks[x][y] > 0) {
                    var gridValue = get(placeX + x-1, placeY + y-1);
                    if(gridValue !=0){
                        logger.info("Unable to place piece, conflict at {}, {}", placeX+x-1, placeY+y-1);
                        return false;
                    }

                }
            }

        }
        return true;
    }

    /**
     * Play the piece by updating the grid with the piece blocks
     *
     * @param  piece the piece to play
     * @param placeX the x-coordinate
     * @param placeY the y-coordinate
     */
    public void playPiece(GamePiece piece, int placeX, int placeY){
        logger.info("Playing the piece {} at {}, {}", piece, placeX, placeY);
        int colour = piece.getValue();
        int[][] blocks = piece.getBlocks();

        for(int x=0; x<blocks.length; x++){
            for(int y=0; y<blocks.length; y++){
                if (blocks[x][y] > 0) {
                    set(placeX + x-1, placeY + y-1, colour);

                }
            }

        }
    }

    /**
     * For the instructions file
     *
     * @param piece a {@link uk.ac.soton.comp1206.game.GamePiece} object
     */
    public void insertPiece(GamePiece piece){
        var blocks = piece.getBlocks();
        for(int x=0; x<piece.getBlocks().length; x++){
            for(int y=0; y<piece.getBlocks().length; y++){
                int value = blocks[x][y];
                this.grid[x][y].set(value);
            }
        }
    }

    /**
     * Get the value represented at the given x and y index within the grid
     *
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }


}

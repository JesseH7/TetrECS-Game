package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.scene.MenuScene;

/**
 * PieceBoard is a visual component to represent the grid of each piece.
 * It extends GameBoard so that it has its own gridPane and grid.
 *
 * @author Jesse Hardy
 */
public class PieceBoard extends GameBoard{
    /**
     * Creates a new instance of the Logger
     */
    private static final Logger logger = LogManager.getLogger(PieceBoard.class);
    /**
     * Whether a dot should be placed on the center square
     */
    private boolean withDot;

    /**
     *Constructor for PieceBoard
     *
     * @param grid a {@link uk.ac.soton.comp1206.game.Grid} object
     * @param width a double
     * @param height a double
     */
    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);
        this.withDot = false;
    }

    /**
     * Sets the grid with the correct piece values
     *
     * @param piece a {@link uk.ac.soton.comp1206.game.GamePiece} object
     */
    public void pieceToDisplay(GamePiece piece){
        logger.info("Setting the PieceBoard values");
        clear();
        for (int col = 0; col <= 2; col++){
            for (int row = 0; row <= 2; row++){
                if (piece.getBlocks()[col][row] != 0){
                    this.grid.set(col, row, piece.getValue());
                }
            }
        }

    }

    /**
     * Clears everything in the PieceBoard grid
     */
    private void clear(){
        for (int col = 0; col <= 2; col++){
            for (int row = 0; row <= 2; row++){
                this.grid.set(col, row, 0);
            }
        }
    }

    /**
     * Setter for the field withDot
     *
     * @param withDot a boolean
     */
    public void setWithDot(boolean withDot){
        this.withDot = withDot;
    }

    /**
     * Getter for the field withDot
     *
     * @return a boolean
     */
    public boolean getWithDot(){
        return this.withDot;
    }

}

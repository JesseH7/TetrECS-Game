package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Next Piece listener is used to handle the event when a new piece is provided
 * by the game it can update the PieceBoard with that piece.
 *
 * @author Jesse Hardy
 */
public interface NextPieceListener {

    /**
     * Handle a next piece event
     *
     * @param piece the current piece
     * @param followingPiece the next piece
     */
    public void nextPiece(GamePiece piece, GamePiece followingPiece);
}

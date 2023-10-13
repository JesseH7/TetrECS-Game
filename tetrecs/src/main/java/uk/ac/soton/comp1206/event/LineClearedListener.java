package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.Set;

/**
 * LineClearedListener interface
 *
 * @author Jesse Hardy
 */
public interface LineClearedListener {
    /**
     * lineCleared
     *
     * @param gameBlockCoordinateSet a {@link java.util.Set} object
     */
    public void lineCleared(Set<GameBlockCoordinate> gameBlockCoordinateSet);
}

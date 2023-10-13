package uk.ac.soton.comp1206.event;

/**
 * GameLoopListener interface to detect when a new game loop happens
 *
 * @author Jesse Hardy
 */
public interface GameLoopListener {
    /**
     * setOnGameLoop
     *
     * @param lives an int
     */
    void setOnGameLoop(int lives);
}

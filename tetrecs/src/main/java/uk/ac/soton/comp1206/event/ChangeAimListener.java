package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * ChangeAimListener interface to handle when the aim is changed using the mouse of the keyboard.
 *
 * @author Jesse Hardy
 */
public interface ChangeAimListener {
    /**
     * changeAim
     *
     * @param block a {@link uk.ac.soton.comp1206.component.GameBlock} object
     * @param entered a {@link java.lang.Boolean} object
     */
    public void changeAim(GameBlock block, Boolean entered);
}

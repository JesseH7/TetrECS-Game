package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.*;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 *
 * @author Jesse Hardy
 */
public class GameBlock extends Canvas {
    /**
     * Creating an instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    /**
     * Creating an instance of the GameBoard
     */
    private final GameBoard gameBoard;

    /**
     * The width of a block
     */
    private final double width;
    /**
     * The height of a block
     */
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    /**
     * DropShadow for the graphics of the blocks
     */
    private final DropShadow shadow;
    /**
     * Whether the block is being hovered over or not
     */
    public boolean hovered;
    /**
     * Opacity of the blocks before they are cleared (for the animation)
     */
    private double opacity = 1.0;
    /**
     * Animation that fades out the blocks when they are cleared
     */
    private AnimationTimer fadeOutAnimation;

    /**
     * Create a new single Game Block
     *
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Creating a drop shadow
        shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setOffsetX(5);
        shadow.setOffsetY(5);
        shadow.setRadius(5);
        setEffect(shadow);


        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Fades out the block when it is cleared by the user
     */
    public void fadeOut() {
        var gc = getGraphicsContext2D();
        if (fadeOutAnimation != null) {
            fadeOutAnimation.stop();
        }
        fadeOutAnimation = new AnimationTimer() {
            private long startTime = -1;
            private boolean isRunning = true;

            @Override
            public void handle(long now) {
                if (startTime == -1) {
                    startTime = now;
                }
                double elapsed = (now - startTime) / 500_000_000.0; // seconds
                opacity = Math.max(0, 1 - elapsed);
                gc.setFill(Color.LIME);
                gc.fillRect(0, 0, width, height);
                gc.setFill(Color.color(0, 0, 0, opacity));
                gc.fillRect(0, 0, width, height);
                if (opacity == 0 && isRunning) {
                    isRunning = false;
                    paintEmpty();
                    fadeOutAnimation.stop();
                }
            }
        };
        fadeOutAnimation.start();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(this.isHovered()){
            paintForHover(COLOURS[value.get()]);
        }
        else if(value.get() == 0) {
            paintEmpty();
        }

        else if(this.gameBoard instanceof PieceBoard && getX()==1 && getY()==1 && ((PieceBoard) this.gameBoard).getWithDot()){
           paintWithCircle(COLOURS[value.get()]);
        }


        else{
            paintColor(COLOURS[value.get()]);
        }
    }

    /**
     * Paint this canvas empty
     */
    public void paintEmpty() {
        var gc = getGraphicsContext2D();

        // Remove any effects
        setEffect(null);
        gc.setEffect(null);

        // Remove the border by setting the stroke color to transparent
        gc.setStroke(Color.TRANSPARENT);
        gc.strokeRect(0, 0, width, height);

        // Clear
        gc.clearRect(0,0,width,height);

        gc.setGlobalAlpha(0.6);
        //Fill
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.GHOSTWHITE);
        gc.setLineWidth(2);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        // Remove any effects
        setEffect(null);

        // Clear
        gc.clearRect(0, 0, width, height);
        gc.setGlobalAlpha(1);

        // Colour fill
        gc.setFill(colour);
        gc.fillRect(0, 0, width, height);

        // Create a triangle inside the rectangle using fillPolygon method
        double[] xPoints = {0, width, width};
        double[] yPoints = {0, 0, height};
        Color color = (Color) colour;
        Color darkerColor = color.deriveColor(0, 1, 0.8, 1);
        gc.setFill(darkerColor);
        gc.fillPolygon(xPoints, yPoints, 3); // fill the polygon with three points

        // Border with gradient stroke and shadow effect
        gc.setLineWidth(2);

        // Add a shadow effect to one side of the rectangle
        setEffect(shadow);
        gc.setEffect(shadow);

        // Draw the border with the gradient stroke
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * Paints the block so that it has the effect of being hovered over which has a
     * grey overlay.
     *
     * @param colour The original colour of the block
     */
    public void paintForHover(Paint colour){
        var gc = getGraphicsContext2D();

        // Remove any effects
        setEffect(null);

        // Clear
        gc.clearRect(0, 0, width, height);
        gc.setGlobalAlpha(1);

        // Colour fill
        gc.setFill(colour);
        gc.fillRect(0, 0, width, height);

        // Create a triangle inside the rectangle using fillPolygon method
        double[] xPoints = {0, width, width};
        double[] yPoints = {0, 0, height};
        Color color = (Color) colour;
        Color darkerColor = color.deriveColor(0, 1, 0.8, 1);
        gc.setFill(darkerColor);
        gc.fillPolygon(xPoints, yPoints, 3); // fill the polygon with three points

        // Border with gradient stroke and shadow effect
        gc.setLineWidth(2);

        // Add a shadow effect to one side of the rectangle
        setEffect(shadow);
        gc.setEffect(shadow);

        // Draw the border with the gradient stroke
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        gc.strokeRect(0, 0, width, height);

        // Create a light grey highlight overlay
        gc.setFill(Color.rgb(192, 192, 192, 0.5)); // Set the fill color to a partially transparent light grey
        gc.fillRect(0, 0, width, height);
    }

    /**
     * Paints the block with a circle in the middle, which is used for the incoming pieceBoard.
     * @param colour The original colour of the block
     */
    private void paintWithCircle(Paint colour){
        logger.info(colour);
        var gc = getGraphicsContext2D();

        // Remove any effects
        setEffect(null);

        // Clear
        gc.clearRect(0, 0, width, height);
        gc.setGlobalAlpha(1);

        // Colour fill
        gc.setFill(colour);
        gc.fillRect(0, 0, width, height);

        // Create a triangle inside the rectangle using fillPolygon method
        double[] xPoints = {0, width, width};
        double[] yPoints = {0, 0, height};
        Color color = (Color) colour;
        Color darkerColor = color.deriveColor(0, 1, 0.8, 1);
        gc.setFill(darkerColor);
        gc.fillPolygon(xPoints, yPoints, 3); // fill the polygon with three points

        // Border with gradient stroke and shadow effect
        gc.setLineWidth(2);

        // Add a shadow effect to one side of the rectangle
        setEffect(shadow);
        gc.setEffect(shadow);

        // Create a linear gradient for the border
        LinearGradient gradient = new LinearGradient(0, 0, 0, height, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE), new Stop(0.5, Color.GRAY), new Stop(1, Color.WHITE));
        gc.setStroke(gradient);

        // Draw the border with the gradient stroke
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0, 0, width, height);

        gc.setFill(Color.LIGHTGRAY);
        gc.fillOval((height/2)-12.5, (width/2)-12.5, 25, 25);
    }

    /**
     * Get the column of this block
     *
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     *
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     *
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     *
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * {@inheritDoc}
     *
     * Converts the block to a string for logging
     */
    @Override
    public String toString() {
        return "GameBlock{" +
                "x=" + x +
                ", y=" + y +
                ", value=" + value.get() +
                '}';
    }

    /**
     * Sets the hovered field variable to a given value
     *
     * @param hovered Whether the block is being hovered over or not
     */
    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    /**
     * Getter method for hovered
     *
     * @return hovered
     */
    public boolean isHovered() {
        return hovered;
    }
}

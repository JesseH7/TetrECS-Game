package uk.ac.soton.comp1206.game;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.event.*;
import uk.ac.soton.comp1206.scene.ChallengeScene;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 *
 * @author Silen
 * @version $Id: $Id
 */
public class Game {
    /**
     * Instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Game.class);
    /**
     * Random for creating a new piece
     */
    private Random random = new Random();
    /**
     * Keep track of the currentPiece
     */
    private GamePiece currentPiece;
    /**
     * Keep track of the followingPiece
     */
    private GamePiece followingPiece;
    /**
     * ArrayList containing the currentPiece and the followingPiece
     */
    ArrayList<GamePiece> piecesToPlay = new ArrayList<>(2);
    /**
     * Keep track of the currentLives
     */
    private IntegerProperty currentLives = new SimpleIntegerProperty();
    /**
     * Keep track of the currentLevel
     */
    private IntegerProperty currentLevel = new SimpleIntegerProperty();
    /**
     * Keep track of the currentMultiplier
     */
    private IntegerProperty currentMultiplier = new SimpleIntegerProperty();
    /**
     * Keep track of the currentScore
     */
    private IntegerProperty currentScore = new SimpleIntegerProperty();
    /**
     * Instance of the NextPieceListener
     */
    public NextPieceListener nextPieceListener;
    /**
     * Instance of the LineClearedListener
     */
    public LineClearedListener lineClearedListener;
    /**
     * Creates a new timer
     */
    private ScheduledExecutorService timer;
    /**
     * Instance of the GameLoopListener
     */
    private GameLoopListener gameLoopListener;
    /**
     * Instance of the GameOverListener
     */
    private GameOverListener gameOverListener;

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Generates the next pieces that are to be played
     */
    public void nextPiece(){
        spawnPiece();
        currentPiece = piecesToPlay.get(0);
        logger.info("The next piece is: {}", currentPiece);
        followingPiece = piecesToPlay.get(1);
        logger.info("The following piece is: {}", followingPiece);
        if (this.nextPieceListener != null){
            nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
        }
    }

    /**
     * Spawns a new piece and fills the array piecesToPlay when a piece is played
     */
    public void spawnPiece(){
        var maxPieces = GamePiece.PIECES;
        while(piecesToPlay.size()<2){
            var randomPiece = random.nextInt(maxPieces);
            logger.info("Picking random piece: {}", randomPiece);
            var piece = GamePiece.createPiece(randomPiece);
            piecesToPlay.add(piece);
        }

    }

    /**
     * Controls what needs to be updated after a piece is played by the user
     */
    public void afterPiece(){
        logger.info("Checking if any lines need to be cleared");
        HashSet<GameBlockCoordinate> coordinatesToClear = new HashSet<>();
        int blocksCleared = 0;
        int linesCleared = 0;
        //Check if there are any vertical lines
        for(int x=0; x<grid.getRows(); x++){
            HashSet<GameBlockCoordinate> tempCoordinates = new HashSet<>();
            for(int y=0; y<grid.getCols(); y++){
                if(grid.get(x,y) > 0){
                    var block = new GameBlockCoordinate(x,y);
                    tempCoordinates.add(block);
                    if(tempCoordinates.size()==grid.getCols()){
                        coordinatesToClear.addAll(tempCoordinates);
                        logger.info("Vertical line at x={} is cleared",x);
                        linesCleared++;
                    }
                }
                else{
                    break;
                }
            }
        }
        //Check if there are any horizontal lines
        for(int y=0; y<grid.getRows(); y++){
            HashSet<GameBlockCoordinate> tempCoordinates = new HashSet<>();
            for(int x=0; x<grid.getCols(); x++){
                if(grid.get(x,y) > 0){
                    var block = new GameBlockCoordinate(x,y);
                    tempCoordinates.add(block);
                    if(tempCoordinates.size()==grid.getRows()){
                        coordinatesToClear.addAll(tempCoordinates);
                        logger.info("Horizontal line at y={} is cleared",y);
                        linesCleared++;
                    }
                }
                else{
                    break;
                }
            }
        }
        if(coordinatesToClear.size()>0){
            Multimedia.playAudio("clear.wav");
        }
        if(lineClearedListener != null){
            this.lineClearedListener.lineCleared(coordinatesToClear);
        }
        for(GameBlockCoordinate block: coordinatesToClear){
            grid.set(block.getX(), block.getY(), 0);
        }
        blocksCleared = coordinatesToClear.size();
        score(linesCleared, blocksCleared);
        multiplier(linesCleared);
        level(getCurrentScore());
    }

    /**
     * Rotates the current piece given a number of rotations
     *
     * @param rotations an int
     */
    public void rotateCurrentPiece(int rotations){
        logger.info("Rotating piece {}", currentPiece.toString());
        currentPiece.rotate(rotations);
        if (this.nextPieceListener != null){
            nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
        }
        Multimedia.playAudio("rotate.wav");
    }

    /**
     * Swaps the current piece with the following piece
     */
    public void swapCurrentPiece(){
        var tempPiece = piecesToPlay.get(1);
        piecesToPlay.set(1, piecesToPlay.get(0));
        piecesToPlay.set(0, tempPiece);
        currentPiece = piecesToPlay.get(0);
        logger.info("The next piece is: {}", currentPiece);
        followingPiece = piecesToPlay.get(1);
        logger.info("The following piece is: {}", followingPiece);
        if (this.nextPieceListener != null){
            nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
        }
        Multimedia.playAudio("rotate.wav");
    }


    /**
     * Calculates what the score should be after a line is cleared
     *
     * @param linesCleared a {@link java.lang.Integer} object
     * @param blocksCleared a {@link java.lang.Integer} object
     */
    public void score(Integer linesCleared, Integer blocksCleared){
        Integer newScore = linesCleared*blocksCleared*10*getCurrentMultiplier();
        setCurrentScore(getCurrentScore()+newScore);
    }

    /**
     * Calculates what the multiplier should be after a block is placed
     *
     * @param linesCleared a {@link java.lang.Integer} object
     */
    public void multiplier(Integer linesCleared){
        if(linesCleared>0){
            setCurrentMultiplier(getCurrentMultiplier()+1);
        }
        else{
            setCurrentMultiplier(1);
        }
    }
    /**
     * Calculates what the level should be given the score
     *
     * @param score a {@link java.lang.Integer} object
     */
    public void level(Integer score){
        setCurrentLevel(score/1000);
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(this::gameLoop, getTimerDelay(), getTimerDelay(), TimeUnit.MILLISECONDS);
        nextPiece();
        setCurrentLives(3);
        setCurrentLevel(0);
        setCurrentMultiplier(1);
        setCurrentScore(0);
    }

    /**
     * Handle what should happen when a particular block is clicked
     *
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        if(grid.canPlayPiece(currentPiece, x, y)){
            //Can play the piece
            logger.info("CURRENT LIVES: "+getCurrentLives());
            restartTimer();
            grid.playPiece(currentPiece, x, y);
            Multimedia.playAudio("place.wav");
            piecesToPlay.set(0, piecesToPlay.get(1));
            piecesToPlay.remove(1);
            nextPiece();
            afterPiece();

        }
        else{
            //Cannot play piece
            Multimedia.playAudio("fail.wav");
        }
    }

    /**
     * Handle what should happen when the next piece should be called upon
     *
     * @param listener a {@link uk.ac.soton.comp1206.event.NextPieceListener} object
     */
    public void setNextPieceListener(NextPieceListener listener){
        this.nextPieceListener = listener;
    }
    /**
     * Setter for the field lineClearedListener
     *
     * @param listener a {@link uk.ac.soton.comp1206.event.LineClearedListener} object
     */
    public void setLineClearedListener(LineClearedListener listener){this.lineClearedListener = listener;}

    /**
     * Setter for the field gameOverListener
     *
     * @param listener a {@link uk.ac.soton.comp1206.event.GameOverListener} object
     */
    public void setGameOverListener(GameOverListener listener){
        this.gameOverListener = listener;
    }

    /**
     * Game ends when all lives are lost and the timer runs out
     */
    public void gameOver(){
        timer.shutdown();
        if (gameOverListener != null){
            gameOverListener.gameOver();
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     *
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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

    /**
     * Gets the timer delay
     *
     * @return an int
     */
    public int getTimerDelay(){
        logger.info("This is what the time should be "+Math.max(2500, 12000-(500*getCurrentLevel())));
        return Math.max(2500, 12000-(500*getCurrentLevel()));
    }
    /**
     * Gets the timer delay in seconds
     *
     * @return a float
     */
    public float getTimerDelayInSeconds() {
        return (float) Math.max(2500, 12000 - (500 * getCurrentLevel()))/1000;
    }

    /**
     * Restarts the timer when a piece is played or a life is lost
     */
    private void restartTimer(){
        shutDownTimer();
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(this::gameLoop, getTimerDelay(), getTimerDelay(), TimeUnit.MILLISECONDS);
        if (this.gameLoopListener != null){
            gameLoopListener.setOnGameLoop(getCurrentLives());
        }
    }

    /**
     * Shuts down the timer to close its thread
     */
    public void shutDownTimer(){
        timer.shutdown();
    }

    /**
     * Discards the current piece and generates a new one when a life is lost
     */
    private void discardCurrentPiece(){
        logger.info("Discarding current piece {}", currentPiece);
        var maxPieces = GamePiece.PIECES;
        var randomPiece = random.nextInt(maxPieces);
        logger.info("Picking random piece: {}", randomPiece);
        var piece = GamePiece.createPiece(randomPiece);
        piecesToPlay.remove(0);
        piecesToPlay.add(0, piece);
        currentPiece = piecesToPlay.get(0);
        if (this.nextPieceListener != null){
            nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
        }
    }

    /**
     * The gameLoop which reduces the lives when the timer reaches 0
     */
    private void gameLoop(){
        if (getCurrentLives()-1 < 0){
            gameOver();
        }
        discardCurrentPiece();
        setCurrentLives(getCurrentLives()-1);
        logger.info("Reducing lives from {} to {}", getCurrentLives(), getCurrentLives()-1);
        setCurrentMultiplier(1);
        if (this.gameLoopListener != null){
            gameLoopListener.setOnGameLoop(getCurrentLives());
        }
    }

    /**
     * Setter for the field gameLoopListener
     *
     * @param listener a {@link uk.ac.soton.comp1206.event.GameLoopListener} object
     */
    public void setGameLoopListener(GameLoopListener listener){
        this.gameLoopListener = listener;
    }

    /**
     * getCurrentLivesProperty
     *
     * @return a {@link javafx.beans.property.IntegerProperty} object
     */
    public IntegerProperty getCurrentLivesProperty(){
        return currentLives;
    }
    /**
     * Getter for the field currentLives
     *
     * @return a {@link java.lang.Integer} object
     */
    public Integer getCurrentLives(){return currentLives.get();}
    /**
     * Setter for the field currentLives
     *
     * @param lives a {@link java.lang.Integer} object
     */
    public void setCurrentLives(Integer lives){currentLives.set(lives);}
    /**
     * getCurrentLevelProperty
     *
     * @return a {@link javafx.beans.property.IntegerProperty} object
     */
    public IntegerProperty getCurrentLevelProperty(){
        return currentLevel;
    }
    /**
     * Getter for the field currentLevel
     *
     * @return a {@link java.lang.Integer} object
     */
    public Integer getCurrentLevel(){return currentLevel.get();}
    /**
     * Setter for the field currentLevel
     *
     * @param level a {@link java.lang.Integer} object
     */
    public void setCurrentLevel(Integer level){
        currentLevel.set(level);
    }
    /**
     * getCurrentMultiplierProperty
     *
     * @return a {@link javafx.beans.property.IntegerProperty} object
     */
    public IntegerProperty getCurrentMultiplierProperty(){
        return currentMultiplier;
    }
    /**
     * Getter for the field currentMultiplier
     *
     * @return a {@link java.lang.Integer} object
     */
    public Integer getCurrentMultiplier(){
        return currentMultiplier.get();
    }
    /**
     * Setter for the field currentMultiplier
     *
     * @param multiplier a {@link java.lang.Integer} object
     */
    public void setCurrentMultiplier(Integer multiplier){
        currentMultiplier.set(multiplier);
    }
    /**
     * getCurrentScoreProperty
     *
     * @return a {@link javafx.beans.property.IntegerProperty} object
     */
    public IntegerProperty getCurrentScoreProperty(){
        return currentScore;
    }
    /**
     * Getter for the field currentScore
     *
     * @return a {@link java.lang.Integer} object
     */
    public Integer getCurrentScore(){
        return currentScore.get();
    }
    /**
     * Setter for the field currentScore
     *
     * @param score a {@link java.lang.Integer} object
     */
    public void setCurrentScore(Integer score){
        currentScore.set(score);
    }


}

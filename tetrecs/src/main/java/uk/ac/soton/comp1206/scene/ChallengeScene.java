package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.ChangeAimListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 *
 * @author Jesse Hardy
 */
public class ChallengeScene extends BaseScene {

    /**
     * Creates an instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    /**
     * Creates an instance of the Game
     */
    protected Game game;
    /**
     * Creates an instance of the PieceBoard
     */
    public PieceBoard pieceBoard;
    /**
     * The following piece
     */
    public PieceBoard pieceBoardFollowing;
    /**
     * The coordinates of the aim
     */
    private Point2D aim;
    /**
     * A new instance of the GameBoard
     */
    private GameBoard board;
    /**
     * The previous block that was highlighted either by the mouse of keyboard
     */
    public GameBlock previousBlockHighlighted;
    /**
     * The UI component of the timer bar
     */
    private Rectangle timerRect = new Rectangle(0, 0, gameWindow.getWidth(), 25);
    /**
     * A new timeline for the animation of the timer bar
     */
    private Timeline timeline = new Timeline();

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        this.aim = new Point2D(2,2);
        this.previousBlockHighlighted = null;
        logger.info("Creating Challenge Scene");
    }

    /**
     * {@inheritDoc}
     *
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        var title = new Text("Challenge Mode");
        title.getStyleClass().add("title");
        VBox centreBox = new VBox();
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        centreBox.getChildren().addAll(title, board);
        centreBox.setAlignment(Pos.TOP_CENTER);
        centreBox.setSpacing(50);
        mainPane.setCenter(centreBox);

        //Current piece
        pieceBoard = new PieceBoard(new Grid(3,3), gameWindow.getWidth()*0.2, gameWindow.getWidth()*0.2);
        pieceBoard.setWithDot(true);
        //Following piece
        pieceBoardFollowing = new PieceBoard(new Grid(3,3), gameWindow.getWidth()*0.15, gameWindow.getWidth()*0.15);

        //Game information
        VBox leftBox = new VBox();
        VBox scoreBox = new VBox();
        var scoreText = new Text("Score");
        var score = new Text();
        score.getStyleClass().add("score");
        scoreText.getStyleClass().add("score");
        score.textProperty().bind(game.getCurrentScoreProperty().asString());
        scoreBox.getChildren().addAll(scoreText, score);
        scoreBox.setAlignment(Pos.TOP_CENTER);
        leftBox.getChildren().addAll(scoreBox, pieceBoard, pieceBoardFollowing);
        leftBox.setSpacing(40);
        leftBox.setAlignment(Pos.TOP_CENTER);
        mainPane.setLeft(leftBox);

        VBox livesBox = new VBox();
        var livesText = new Text("Lives");
        var lives = new Text();
        lives.getStyleClass().add("lives");
        livesText.getStyleClass().add("lives");
        lives.textProperty().bind(game.getCurrentLivesProperty().asString());
        livesBox.getChildren().addAll(livesText, lives);
        livesBox.setAlignment(Pos.CENTER);

        VBox multiplierBox = new VBox();
        var multiplierText = new Text("Multiplier");
        var theX = new Text("x");
        var multiplier = new Text();
        HBox hBox = new HBox(theX, multiplier);
        hBox.setAlignment(Pos.CENTER);
        multiplier.getStyleClass().add("multiplier");
        theX.getStyleClass().add("multiplier");
        multiplierText.getStyleClass().add("multiplier");
        multiplier.textProperty().bind(game.getCurrentMultiplierProperty().asString());
        multiplierBox.getChildren().addAll(multiplierText, hBox);
        multiplierBox.setAlignment(Pos.CENTER);

        VBox levelBox = new VBox();
        var levelText = new Text("Level");
        var level = new Text();
        level.getStyleClass().add("level");
        levelText.getStyleClass().add("level");
        level.textProperty().bind(game.getCurrentLevelProperty().asString());
        levelBox.getChildren().addAll(levelText, level);
        levelBox.setAlignment(Pos.CENTER);

        VBox highScoreBox = new VBox();
        var highScoreText = new Text("High Score");
        var highScore = new Text(getHighScore().toString());
        highScoreText.getStyleClass().add("hiscore");
        highScore.getStyleClass().add("hiscore");
        highScoreBox.getChildren().addAll(highScoreText, highScore);
        highScoreBox.setAlignment(Pos.CENTER);

        VBox rightSide = new VBox(highScoreBox, livesBox, multiplierBox, levelBox);
        rightSide.setSpacing(40);

        mainPane.setRight(rightSide);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        board.setOnRightClicked(this::rightClicked);
        logger.info("BLOCK:"+board.getBlock((int) aim.getX(), (int) aim.getY()).toString());
        board.setOnHover(this::hovered);
        pieceBoard.setOnBlockClick(this::rightClicked);
        this.game.setNextPieceListener(this::nextPiece);
        this.game.setLineClearedListener(this::lineCleared);

        timerRect.setFill(Color.GREEN);
        timerRect.setStroke(Color.BLACK);

        mainPane.setBottom(timerRect);

        // Create the timer animation timeline
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.seconds(0), new KeyValue(timerRect.widthProperty(), gameWindow.getWidth())),
                new KeyFrame(Duration.seconds(game.getTimerDelayInSeconds()/3), new KeyValue(timerRect.widthProperty(), (gameWindow.getWidth()*2)/3)),
                new KeyFrame(Duration.seconds(game.getTimerDelayInSeconds()), new KeyValue(timerRect.widthProperty(), 0)),
                new KeyFrame(Duration.seconds(0), new KeyValue(timerRect.fillProperty(), Color.GREEN)),
                new KeyFrame(Duration.seconds(game.getTimerDelayInSeconds()/2), new KeyValue(timerRect.fillProperty(), Color.YELLOW)),
                new KeyFrame(Duration.seconds(game.getTimerDelayInSeconds()), new KeyValue(timerRect.fillProperty(), Color.RED))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        this.game.setGameLoopListener(this::resetBar);
        this.game.setGameOverListener(this::gameOver);


    }

    /**
     * Is called when the user runs out of lives and runs the scoreScreen window
     */
    private void gameOver(){
        Platform.runLater(() -> {
            Multimedia.stopMusic();
            timeline.stop();
            gameWindow.startScores(game);
        });
    }

    /**
     * This method resets the bar
     * @param lives
     */
    private void resetBar(int lives){
        logger.info("THIS IS THE NUMBER OF LIVES => " + lives);
        timeline.stop();

        timeline = new Timeline();
        logger.info("This is the time being sent " + game.getTimerDelayInSeconds());

        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.seconds(0), new KeyValue(timerRect.widthProperty(), gameWindow.getWidth())),
                new KeyFrame(Duration.seconds(game.getTimerDelayInSeconds()/3), new KeyValue(timerRect.widthProperty(), (gameWindow.getWidth()*2)/3)),
                new KeyFrame(Duration.seconds(game.getTimerDelayInSeconds()), new KeyValue(timerRect.widthProperty(), 0)),
                new KeyFrame(Duration.seconds(0), new KeyValue(timerRect.fillProperty(), Color.GREEN)),
                new KeyFrame(Duration.seconds(game.getTimerDelayInSeconds()/2), new KeyValue(timerRect.fillProperty(), Color.YELLOW)),
                new KeyFrame(Duration.seconds(game.getTimerDelayInSeconds()), new KeyValue(timerRect.fillProperty(), Color.RED))
        );

        timerRect.setWidth(gameWindow.getWidth());
        timerRect.setFill(Color.GREEN);
        timeline.play();
    }


    /**
     * This method is used to adjust the coordinates if the aim
     *
     * @param x The amount the aim needs to be moves on the x-axis
     * @param y The amount the aim needs to be moves on the y-axis
     */
    public void moveAim(int x, int y){
        if(aim.getX()>=4 && x>0){
            x=0;
        }
        if(aim.getX()<=0 && x<0){
            x=0;
        }
        if(aim.getY()>=4 && y>0){
            y=0;
        }
        if(aim.getY()<=0 && y<0){
            y=0;
        }
        if(board.getChangeAimListener() != null) {
            board.getChangeAimListener().changeAim(this.board.getBlock((int) aim.getX(), (int) aim.getY()), false);
        }
        logger.info("Moving aim from ({},{}) to ({},{})", aim.getX(), aim.getY(), aim.getX()+x, aim.getY()+y);
        aim = aim.add(x, y);
        if(board.getChangeAimListener() != null) {
            board.getChangeAimListener().changeAim(this.board.getBlock((int) aim.getX(), (int) aim.getY()), true);
        }

    }

    /**
     * Is called when the listener is activated when the mouse is hovered over a square or the keyboard is used.
     * @param gameBlock The block being hovered over
     * @param entered Whether the mouse has entered the block or not
     */
    private void hovered(GameBlock gameBlock, Boolean entered){
        if(previousBlockHighlighted != null) {
            previousBlockHighlighted.setHovered(false);
            previousBlockHighlighted.paint();
        }
        previousBlockHighlighted = gameBlock;
        gameBlock.setHovered(entered);
        gameBlock.paint();
    }

    /**
     * Is called when the line cleared listener is activated and fades out the blocks given.
     * @param gameBlockCoordinateSet The blocks that were cleared by the user
     */
    private void lineCleared(Set<GameBlockCoordinate> gameBlockCoordinateSet) {
        this.board.fadeOut(gameBlockCoordinateSet);
    }

    /**
     * Handle when the piece board is left-clicked
     * @param gameBlock The block that was left-clicked
     */
    private void rightClicked(GameBlock gameBlock) {
        game.rotateCurrentPiece(1);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
}

    /**
     * Handle when the user presses ENTER or X
     */
    private void blockEntered(){
        game.blockClicked(board.getBlock((int) aim.getX(), (int) aim.getY()));
    }

    /**
     * Handle when the board is right-clicked
     */
    private void rightClicked(){
        game.rotateCurrentPiece(1);
    }

    /**
     * Handle when a new pieces are added to the currentPiece and followingPiece
     * @param piece currentPiece
     * @param followingPiece followingPiece
     */
    private void nextPiece(GamePiece piece, GamePiece followingPiece){
        logger.info("Piece being displayed {}", piece.toString());
        this.pieceBoard.pieceToDisplay(piece);
        this.pieceBoardFollowing.pieceToDisplay(followingPiece);
    }

    /**
     * This retrieves the local high score from the scores.txt file by
     * getting the score of the first line.
     * @return An Integer for the highest
     */
    public Integer getHighScore(){
        String filePath = "data/scores.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if(line == null || !line.contains(":")){
                return 0;
            }
            String[] currentLine = line.split(":");
            return Integer.valueOf(currentLine[1]);

        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
        }
        return null;
    }

    /**
     * Set up the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     *
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        //Handle key presses in the scene
        gameWindow.getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                Platform.runLater(() -> {
                    Multimedia.stopMusic();
                    game.shutDownTimer();
                    timeline.stop();
                    gameWindow.startMenu();
                });
            }
            if(keyEvent.getCode() == KeyCode.E){
                game.rotateCurrentPiece(1);
            }
            if(keyEvent.getCode() == KeyCode.C){
                game.rotateCurrentPiece(1);
            }
            if(keyEvent.getCode() == KeyCode.CLOSE_BRACKET){
                game.rotateCurrentPiece(1);
            }

            if(keyEvent.getCode() == KeyCode.Q){
                game.rotateCurrentPiece(3);
            }
            if(keyEvent.getCode() == KeyCode.Z){
                game.rotateCurrentPiece(3);
            }
            if(keyEvent.getCode() == KeyCode.OPEN_BRACKET){
                game.rotateCurrentPiece(3);
            }
            if(keyEvent.getCode() == KeyCode.SPACE){
                game.swapCurrentPiece();
            }
            if(keyEvent.getCode() == KeyCode.R){
                game.swapCurrentPiece();
            }
            if(keyEvent.getCode() == KeyCode.W){
                moveAim(0, -1);
            }
            if(keyEvent.getCode() == KeyCode.UP){
                moveAim(0, -1);
            }
            if(keyEvent.getCode() == KeyCode.S){
                moveAim(0, 1);
            }
            if(keyEvent.getCode() == KeyCode.DOWN){
                moveAim(0, 1);
            }
            if(keyEvent.getCode() == KeyCode.A){
                moveAim(-1, 0);
            }
            if(keyEvent.getCode() == KeyCode.LEFT){
                moveAim(-1, 0);
            }
            if(keyEvent.getCode() == KeyCode.D){
                moveAim(1, 0);
            }
            if(keyEvent.getCode() == KeyCode.RIGHT){
                moveAim(1, 0);
            }
            if(keyEvent.getCode() == KeyCode.ENTER){
                blockEntered();
            }
            if(keyEvent.getCode() == KeyCode.X){
                blockEntered();
            }
            if(keyEvent.getCode() == KeyCode.BACK_SPACE){
                gameOver();
            }
        });
        game.start();
    }

}

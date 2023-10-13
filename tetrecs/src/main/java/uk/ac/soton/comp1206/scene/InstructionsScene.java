package uk.ac.soton.comp1206.scene;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import java.io.File;
import java.net.MalformedURLException;

/**
 * Creates a new scene to show the user how to play the game
 *
 * @author Jesse Hardy
 */
public class InstructionsScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    /**
     * Constructor for InstructionsScene
     *
     * @param gameWindow a {@link uk.ac.soton.comp1206.ui.GameWindow} object
     */
    public InstructionsScene(GameWindow gameWindow){
        super(gameWindow);
        logger.info("Creating Instructions Scene");
    }

    /**
     * Initialise the scene
     */
    @Override
    public void initialise() {
        gameWindow.getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                gameWindow.startMenuWithoutMusic();
            }
        });
    }

    /**
     * Build the instructions window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var instructionsPane = new StackPane();
        instructionsPane.setMaxWidth(gameWindow.getWidth());
        instructionsPane.setMaxHeight(gameWindow.getHeight());
        instructionsPane.getStyleClass().add("menu-background");
        root.getChildren().add(instructionsPane);

        var mainPane = new BorderPane();
        instructionsPane.getChildren().add(mainPane);

        VBox middleBox = new VBox();
        var instructionsTitle = new Text("Instructions");
        instructionsTitle.getStyleClass().add("title");
        middleBox.getChildren().add(instructionsTitle);
        //Instructions image
        try {
            var instructions = new ImageView(new Image(new File("src/main/resources/images/Instructions.png").toURL().toExternalForm()));
            instructions.setFitWidth(500);
            instructions.setPreserveRatio(true);
            middleBox.getChildren().add(instructions);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        var piecesTitle = new Text("Pieces");
        piecesTitle.getStyleClass().add("title");
        middleBox.getChildren().add(piecesTitle);

        //Grid pane of pieces
        var piecePane = new GridPane();
        int column = 0;
        int row = 0;
        for(int i=0; i<15; i++){
            var pieceGrid = new Grid(3, 3);
            pieceGrid.insertPiece(GamePiece.createPiece(i));
            var piece = new PieceBoard(pieceGrid, 60, 60);
            piecePane.add(piece, row, column);
            if(column==0 && row<4){
                row++;
            }
            else {
                if((row+1)%5 == 0){
                    column = (column+1)%3;
                }
                row = (row+1)%5;
            }
        }
        piecePane.setHgap(5);
        piecePane.setVgap(5);
        middleBox.getChildren().add(piecePane);
        piecePane.setAlignment(Pos.BOTTOM_CENTER);

        mainPane.setCenter(middleBox);
        middleBox.setAlignment(Pos.CENTER);

    }
}

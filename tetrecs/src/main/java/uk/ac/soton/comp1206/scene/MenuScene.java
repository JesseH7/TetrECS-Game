package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 *
 * @author Jesse Hardy
 */
public class MenuScene extends BaseScene {
    /**
     * Instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * {@inheritDoc}
     *
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Title
        try {
            // Create ImageView
            ImageView title = new ImageView(new Image(new File("src/main/resources/images/TetrECS.png").toURI().toString()));
            title.setFitWidth(600);
            title.setPreserveRatio(true);

            // Add ImageView to an HBox
            HBox titleBox = new HBox(title);
            mainPane.setTop(titleBox);
            titleBox.setAlignment(Pos.CENTER);
            titleBox.setPadding(new Insets(25, 0, 100, 0));

            // Create ScaleTransition to grow and shrink ImageView
            ScaleTransition transition = new ScaleTransition(Duration.seconds(1), title);
            transition.setAutoReverse(true);
            transition.setCycleCount(Animation.INDEFINITE);
            transition.setToX(1.2);
            transition.setToY(1.2);

            // Start ScaleTransition
            transition.play();

            mainPane.setTop(titleBox);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        VBox buttons = new VBox();
        var buttonSinglePlayer = new Button("Single Player");
        buttonSinglePlayer.getStyleClass().add("menuItem");
        buttons.getChildren().add(buttonSinglePlayer);
        //Bind the button action to the startGame method in the menu
        buttonSinglePlayer.setOnAction(this::startGame);

        //Instructions scene button
        var buttonInstructions = new Button("Instructions");
        buttonInstructions.getStyleClass().add("menuItem");
        buttons.getChildren().add(buttonInstructions);
        buttonInstructions.setOnAction(this::startInstructions);

        //Exit button
        var buttonExit = new Button("Exit");
        buttonExit.getStyleClass().add("menuItemSmall");
        buttons.getChildren().add(buttonExit);
        buttonExit.setOnAction((event) -> {
            System.exit(0);
        });

        buttons.setSpacing(50);
        mainPane.setCenter(buttons);
        buttons.setAlignment(Pos.CENTER);
    }

    /**
     * {@inheritDoc}
     *
     * Initialise the menu
     */
    @Override
    public void initialise() {
    }
    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

    /**
     * Start the instructions scene from the GameWindow
     * @param event
     */
    private void startInstructions(ActionEvent event){
        gameWindow.startInstructions();

    }

}

package uk.ac.soton.comp1206.scene;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

/**
 * ScoreScene class extends the BaseScene to create a scene to show the local scores
 * and the online scores after the game is over.
 *
 * @author Jesse Hardy
 */
public class ScoresScene extends BaseScene {
    /**
     * Creates an instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    /**
     * Creates an ArrayList to store the local scores
     */
    public ArrayList<Pair<String, Integer>> scorePairs = new ArrayList<>();
    /**
     * Converts the ArrayList into an observable list
     */
    public ObservableList<Pair<String, Integer>> scorePairsObservable = FXCollections.observableArrayList(scorePairs);
    /**
     * Creates a wrapper around the observable list
     */
    public SimpleListProperty<Pair<String, Integer>> wrapper = new SimpleListProperty<>(scorePairsObservable);

    /**
     * Creates an instance of the Game
     */
    public Game game;
    /**
     * A list of all the scores retrieved from the server
     */
    private final SimpleListProperty<Pair<String, Integer>> remoteScores;
    /**
     * The message that will be sent to the server
     */
    public String message;
    /**
     * The main Border Pane
     */
    public BorderPane mainPane = new BorderPane();
    /**
     * Whether the user beat any of the local scores
     */
    public boolean userBeatLocalScore;
    /**
     * Whether the user beat any of the online scores
     */
    public boolean userBeatOnlineScore;

    /**
     * Constructor for ScoresScene
     *
     * @param gameWindow a {@link uk.ac.soton.comp1206.ui.GameWindow} object
     * @param game a {@link uk.ac.soton.comp1206.game.Game} object
     */
    public ScoresScene(GameWindow gameWindow, Game game){
        super(gameWindow);
        this.game = game;
        logger.info("Creating the Scores Scene");
        this.userBeatLocalScore = false;
        this.userBeatOnlineScore = false;
        this.remoteScores = new SimpleListProperty<>();
    }

    /**
     * Initialises the score scene
     */
    @Override
    public void initialise() {
        gameWindow.getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                Multimedia.stopMusic();
                gameWindow.startMenu();
            }
        });
    }

    /**
     * Builds the game window and loads the UI components
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var scoresPane = new StackPane();
        scoresPane.setMaxWidth(gameWindow.getWidth());
        scoresPane.setMaxHeight(gameWindow.getHeight());
        scoresPane.getStyleClass().add("menu-background");
        root.getChildren().add(scoresPane);

        scoresPane.getChildren().add(mainPane);

        try {
            var title = new ImageView(new Image(new File("src/main/resources/images/TetrECS.png").toURL().toExternalForm()));
            title.setFitWidth(500);
            title.setPreserveRatio(true);
            var gameOverTitle = new Text("Game Over");
            gameOverTitle.getStyleClass().add("bigtitle");
            var highScoresTitle = new Text("High Scores");
            highScoresTitle.getStyleClass().add("title");
            VBox titleBox = new VBox(title, gameOverTitle, highScoresTitle);
            mainPane.setTop(titleBox);
            titleBox.setAlignment(Pos.CENTER);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        loadAllScores();

    }
    /**
     * Loads the onlineScores first and then loads the local scores and updates
     * all the UI.
     */
    public void loadAllScores(){
        logger.info("Loading All Scores");
        // Load online scores first
        CompletableFuture<Void> onlineScoresFuture = loadOnlineScores();

        // When online scores are loaded, load local scores and update UI
        onlineScoresFuture.thenRunAsync(() -> {
            loadLocalScores();
            updateUI();
        }, Platform::runLater);

    }
    /**
     * Loads the online scores from the server using the communicator
     *
     * @return a {@link java.util.concurrent.CompletableFuture} object
     */
    public CompletableFuture<Void> loadOnlineScores() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        Communicator communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");
        message = "HISCORES";
        communicator.send(message);
        CommunicationsListener listener = new CommunicationsListener() {
            @Override
            public void receiveCommunication(String communication) {
                String receivedScores = communication.replace("HISCORES ", "");
                String[] receivedScoresArray = receivedScores.split("\n");
                ArrayList<Pair<String, Integer>> onlineScores = new ArrayList<>();
                for (String pair : receivedScoresArray) {
                    String[] newPairArray = pair.split(":");
                    Pair<String, Integer> newPair = new Pair<>(newPairArray[0], Integer.parseInt(newPairArray[1]));
                    onlineScores.add(newPair);
                }
                Platform.runLater(() -> {
                    ObservableList<Pair<String, Integer>> newRemoteScores = FXCollections.observableArrayList();
                    newRemoteScores.addAll(onlineScores);
                    remoteScores.set(newRemoteScores);
                });
                future.complete(null);
            }
        };
        communicator.addListener(listener);
        return future;
    }

    /**
     * Updates the UI to add the appropriate online and local score lists and if the user
     * needs to enter their name or not.
     */
    public void updateUI(){
        VBox middleBox = new VBox();
        if(scorePairsObservable.size() == 0){
            this.userBeatLocalScore = true;
        }
        else if (game.getCurrentScore() > scorePairsObservable.get(scorePairsObservable.size() - 1).getValue()) {
            this.userBeatLocalScore = true;
        }
        if(remoteScores.size() == 0){
            this.userBeatOnlineScore = true;
        }
        else if (game.getCurrentScore() > remoteScores.get(remoteScores.getSize() - 1).getValue()) {
            this.userBeatOnlineScore = true;
        }
        logger.info(scorePairsObservable.toString());
        logger.info(userBeatLocalScore);
        if (this.userBeatOnlineScore || this.userBeatLocalScore) {
            logger.info("New high score");
            Text text = new Text("Enter your name");
            text.getStyleClass().add("title");
            var enterName = new TextField();
            var submit = new Button("Submit");
            middleBox.getChildren().addAll(text, enterName, submit);
            mainPane.setCenter(middleBox);
            submit.setOnAction((actionEvent -> {
                if (this.userBeatLocalScore) {
                    scorePairsObservable.add(new Pair<>(enterName.getText(), game.getCurrentScore()));
                }
                if (this.userBeatOnlineScore) {
                    remoteScores.add(new Pair<>(enterName.getText(), game.getCurrentScore()));
                    writeOnlineScores(new Pair<>(enterName.getText(), game.getCurrentScore()));
                }
                mainPane.getChildren().remove(middleBox);
                loadScoresUI();
                loadOnlineScoresUI();
            }));
        } else {
            loadScoresUI();
            loadOnlineScoresUI();
        }
    }


    /**
     * Sends a message to the server if a users score beats one of the online scores
     *
     * @param score a {@link javafx.util.Pair} object
     */
    public void writeOnlineScores(Pair<String, Integer> score){
        Communicator communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");
        communicator.send("HISCORE "+score.getKey()+":"+score.getValue());
    }
    /**
     * Loads the online scores list UI
     */
    public void loadOnlineScoresUI(){
        sortByValueDescending(remoteScores);
        ScoresList scoresList = new ScoresList("Online Scores");
        scoresList.getScoresListProperty().bindBidirectional(remoteScores);
        for (Pair<String, Integer> scorePair : scoresList.getScoresListProperty()) {
            scoresList.addScores(scorePair);
        }
        mainPane.setRight(scoresList);
        scoresList.reveal();
    }
    /**
     * Loads the local scores list UI
     */
    public void loadScoresUI(){
        sortByValueDescending(scorePairsObservable);
        ScoresList scoresList = new ScoresList("Local Scores");
        scoresList.getScoresListProperty().bindBidirectional(wrapper);
        for (Pair<String, Integer> scorePair : scoresList.getScoresListProperty()) {
            scoresList.addScores(scorePair);
        }
        mainPane.setLeft(scoresList);
        scoresList.reveal();
        writeScores();
    }
    /**
     * Sorts a given observable list by descending order by the score
     *
     * @param list a {@link javafx.collections.ObservableList} object
     */
    public static void sortByValueDescending(ObservableList<Pair<String,Integer>> list) {
        list.sort(new Comparator<Pair<String,Integer>>() {
            @Override
            public int compare(Pair<String,Integer> o1, Pair<String,Integer> o2) {
                return o2.getValue().compareTo(o1.getValue()); // Reverse order
            }
        });
    }
    /**
     * Reads the local scores.txt file
     */
    public void loadLocalScores(){
        String filePath = "data/scores.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains(":")) {
                    String[] currentLine = line.split(":");
                    String name = currentLine[0];
                    Integer score = Integer.valueOf(currentLine[1]);
                    scorePairsObservable.add(new Pair<>(name, score));
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
        }
        logger.info("Local scores read {}", scorePairsObservable.toString());
    }

    /**
     * Write the updates local scores to the scores.txt file
     */
    public void writeScores(){
        StringBuilder data = new StringBuilder();
        for(Pair<String, Integer> pairs: scorePairsObservable){
            data.append(pairs.getKey()+":"+pairs.getValue().toString()+"\n");
        }
        String filename = "data/scores.txt";

        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(data.toString());
            writer.close();
            logger.info("Data written to file successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

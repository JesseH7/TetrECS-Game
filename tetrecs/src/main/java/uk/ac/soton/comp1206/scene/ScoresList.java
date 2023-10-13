package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 * ScoresList is a custom UI component which extends VBox to create an animated list of the online and
 * local scores.
 *
 * @author Jesse Hardy
 */
public class ScoresList extends VBox {
    /**
     * The Vbox of the scores
     */
    private final VBox scores;
    /**
     * The list of the scores
     */
    private final SimpleListProperty<Pair<String, Integer>> scoresListProperty;
    /**
     * Constructor for ScoresList
     *
     * @param title a {@link java.lang.String} object
     */
    public ScoresList(String title){
        scoresListProperty = new SimpleListProperty<>();
        setPrefWidth(200);
        setSpacing(26);
        setPadding(new Insets(10,10,10,10));

        //Title
        Text scoresTitle = new Text(title);
        scoresTitle.getStyleClass().add("heading");

        //Scores list
        scores = new VBox();
        getChildren().addAll(scoresTitle, scores);
    }

    /**
     * Getter for the field scoresListProperty
     *
     * @return a {@link javafx.beans.property.SimpleListProperty} object
     */
    public SimpleListProperty<Pair<String, Integer>> getScoresListProperty(){
        return this.scoresListProperty;
    }

    /**
     * Adds a given score to the custom UI component ScoreList
     *
     * @param score a {@link javafx.util.Pair} object
     */
    public void addScores(Pair<String, Integer> score){
        var newScore = new Text(score.getKey()+": "+score.getValue());
        newScore.getStyleClass().add("scoreitem");
        newScore.setOpacity(0);
        scores.getChildren().add(newScore);
    }
    /**
     * Animates the custom UI component to show the scores one by one
     */
    public void reveal() {
        // Loop through each score and animate its appearance
        for (int i = 0; i < scores.getChildren().size(); i++) {
            Text scoreText = (Text) scores.getChildren().get(i);
            // Calculate the delay for this score based on its position
            Duration delay = Duration.millis(i * 200);
            // Fade the score in
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), scoreText);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            // Set the initial opacity to 0 to make it invisible
            scoreText.setOpacity(0);
            // Play the animation with a delay
            fadeTransition.setDelay(delay);
            fadeTransition.play();
        }
    }
}

package uk.ac.soton.comp1206.game;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Responsible for playing music and audio effects in the game
 *
 * @author Jesse Hardy
 */
public class Multimedia {
    /**
     * Instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    /**
     * For playing sound effects
     */
    private static MediaPlayer audioPlayer;
    /**
     * For playing music
     */
    private static MediaPlayer musicPlayer;

    /**
     * Whether there is music currently playing
     */
    private static boolean musicPlaying;

    /**
     * Play the sound effect
     *
     * @param file a {@link java.lang.String} object
     */
    public static void playAudio(String file){
        String toPlay = Objects.requireNonNull(Multimedia.class.getResource("/sounds/" + file)).toExternalForm();
        logger.info("Playing audio "+toPlay);

        try{
            Media play = new Media(toPlay);
            audioPlayer = new MediaPlayer(play);
            audioPlayer.play();
        } catch(Exception e){
            e.printStackTrace();
            logger.error("Unable to play audio file");
        }
    }

    /**
     * Play the music
     *
     * @param file a {@link java.lang.String} object
     */
    public static void playMusic(String file){
        String toPlay = Objects.requireNonNull(Multimedia.class.getResource("/music/"+file).toExternalForm());
        logger.info("Playing background music "+toPlay);

        try{
            Media play = new Media(toPlay);
            musicPlayer = new MediaPlayer(play);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(0.2);
            musicPlayer.play();

            musicPlaying = true;

        } catch(Exception e){
            e.printStackTrace();
            logger.error("Unable to play music file");
        }
    }

    /**
     * stopMusic
     */
    public static void stopMusic(){
        if(musicPlaying) {
            logger.info("Stopping the music");
            musicPlayer.stop();
            musicPlaying = false;
        }
    }
}

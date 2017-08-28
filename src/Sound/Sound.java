package Sound;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

/**
 * Created by anatolyi on 6/21/2017.
 */
public class Sound {

    static Media sound;
    private static boolean isSongPlaying = false;
    public static MediaPlayer mediaPlayer;

    public static void startPlayingMusic() {
        stopMusic();
        isSongPlaying = true;
        String musicFile = "resources/Music/ninja_turtle.mp3";
        sound = new Media(new File(musicFile).toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }

    public static void startWinningMusic() {
        stopMusic();
        String musicFile = "resources/Music/winning_Music.mp3";
        sound = new Media(new File(musicFile).toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }

    public static void stopMusic() {
        if (isSongPlaying == true)
            mediaPlayer.stop();
    }

}

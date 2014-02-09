package video_storm;

import net.indiespot.media.Movie;
import net.indiespot.media.impl.OpenALAudioRenderer;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

import java.io.File;
import java.io.IOException;

public class MoviePlayer {
    public Movie movie;
    private boolean paused;

    OpenALAudioRenderer audioRenderer;

    public MoviePlayer(String moviePath) throws IOException, LWJGLException {
        File movieFile = new File(moviePath);
        movie = Movie.open(movieFile);

        audioRenderer = new OpenALAudioRenderer();
        audioRenderer.init(movie.audioStream(), movie.framerate());

        AL.create();
    }

    public void pause() {
        paused = true;
        audioRenderer.pause();
    }

    public void resume() {
        paused = false;
        audioRenderer.resume();
    }

    public boolean tick( MovieCanvas movieCanvas ) {
        audioRenderer.tick(movie);

        if (movie.isTimeForNextFrame()) {
            if ( !movieCanvas.readMovieFrame(movie) )
                return false;

            // signal the AV-sync that we processed a frame
            movie.onUpdatedVideoFrame();
        }

        return true;
    }

    public void close() {
        try {
            movie.close();
            audioRenderer.close();

            AL.destroy();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMovieWidth() {
        return movie.width();
    }

    public int getMovieHeight() {
        return movie.height();
    }

    public void togglePause() {
        if (paused)
            resume();
        else
            pause();
    }
}

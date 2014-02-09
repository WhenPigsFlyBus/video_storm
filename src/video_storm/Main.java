package video_storm;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import video_storm.input.KeyInput;

public class Main
{
    static KeyInput keyinput;
    static MoviePlayer player = null;

	public static void main(String[] args) {
        Window window = null;

        try {
            player = new MoviePlayer("C:\\indispot\\indiespot-media-0.8.04-all\\sample_h264.mp4");

            window = new Window(1400, 800);

            Canvas canvas = new Canvas();
            canvas.set(new MovieCanvas(player.getMovieWidth(), player.getMovieHeight()));

            keyinput = new KeyInput();

            while (!Display.isCloseRequested()) {
                processInput();

                player.tick(canvas.movieCanvas);

                canvas.setView(Display.getWidth(), Display.getHeight());
                canvas.render();

                Display.update();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            if (keyinput != null)
                keyinput.close();

            if (player != null)
                player.close();

            if (window != null)
                window.close();
        }

		System.exit(0);    	    
	}

    private static void processInput() {
        keyinput.get();

        if (keyinput.consume(Keyboard.KEY_SPACE)) {
            player.togglePause();
        }
        else if (keyinput.consume(Keyboard.KEY_TAB)) {
            State.menuMode = !State.menuMode;
            System.out.println( "*** menu mode" );
        }
    }
}
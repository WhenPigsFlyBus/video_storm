package video_storm;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import video_storm.input.KeyInput;
import video_storm.input.MouseInput;

public class MainTest {
    public static void main( String[] args ) throws LWJGLException {
        new Window(640, 480);

        KeyInput k = new KeyInput();
        MouseInput m = new MouseInput();

        System.out.println( "*** " + Keyboard.KEY_SPACE );

        while(true) {
            k.get();
            m.get();
            Display.update();
        }
    }
}

package video_storm.input;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;

public class MouseInput {

    public MouseInput() throws LWJGLException {
        Mouse.create();
    }

    public void get() {
        while (Mouse.next()) {
            if (Mouse.getEventButton()>-1)
                System.out.println( "*** " + Mouse.getEventButton() + ":" + Mouse.getEventButtonState() );
            else
                System.out.println( "*** " + Mouse.getEventX() + ", " + Mouse.getEventY() );
        }
    }

    public void finalize() {
        System.out.println( "*** finalized" );
        Mouse.destroy();
    }
}

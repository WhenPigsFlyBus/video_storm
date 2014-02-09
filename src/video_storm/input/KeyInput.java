package video_storm.input;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;

public class KeyInput {
    public boolean[] keys = new boolean[65536];

    public KeyInput() throws LWJGLException {
        Keyboard.create();
    }

    public void get() {
        while(Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                keys[Keyboard.getEventKey()] = true;
                System.out.println( "*** " + Keyboard.getEventKey());
            }
            else {
                keys[Keyboard.getEventKey()] = false;
            }
        }
    }

    public void close() {
        Keyboard.destroy();
    }

    public boolean consume( int keySpace ) {
        boolean ans = keys[keySpace];
        keys[keySpace] = false;
        return ans;
    }
}

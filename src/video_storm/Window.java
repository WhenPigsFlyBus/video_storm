package video_storm;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

public class Window {

    public Window(int width, int height) throws LWJGLException {
        Display.setDisplayMode( new DisplayMode( width, height ) );
        Display.setResizable(true);
        Display.setTitle("VideoStorm v1.0 (by Ace)");
        Display.setVSyncEnabled(false);

        Display.create(new PixelFormat(/* bpp */32, /* alpha */8, /* depth */24,/* stencil */8,/* samples */4));
    }

    public void close() {
        Display.destroy();
    }
}

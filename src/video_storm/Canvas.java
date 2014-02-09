package video_storm;

import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glViewport;

public class Canvas {
    MovieCanvas movieCanvas;

    public void set(MovieCanvas movieCanvas) {
        this.movieCanvas = movieCanvas;
    }

    public void setView( int width, int height ) {
        glDisable( GL_CULL_FACE );

        glViewport( 0, 0, width, height );
    }

    public void render() {
        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glPushMatrix();
            movieCanvas.render(Display.getWidth(), Display.getHeight());
        glPopMatrix();

        glFlush();
    }
}

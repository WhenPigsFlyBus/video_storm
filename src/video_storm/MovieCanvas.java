package video_storm;

import craterstudio.math.EasyMath;
import net.indiespot.media.Movie;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.glu.GLU;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class MovieCanvas {
    int textureHandle;
    float texWidthUsedRatio, texHeightUsedRatio;

    int movieWidth, movieHeight;

    ByteBuffer textureBuffer;

    public MovieCanvas(int movieWidth, int movieHeight) {
        this.movieWidth = movieWidth;
        this.movieHeight = movieHeight;

        setupTexture();
    }

    public void render(int screenWidth, int screenHeight) {
        setProjection(screenWidth, screenHeight);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        float finalWidth = 1, finalHeight = 1;

        float movieRatio = (float)movieHeight / (float) movieWidth;

        if ( movieRatio < (float)screenHeight / (float)screenWidth) {
            finalWidth = screenWidth;
            finalHeight = movieRatio * screenWidth;
        }
        else {
            finalWidth = (float)movieWidth / (float) movieHeight * screenHeight;
            finalHeight = screenHeight;
        }

        renderMovieScreen(finalWidth, finalHeight, State.menuMode );
    }

    private void setProjection(int screenWidth, int screenHeight) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        if (State.menuMode ) {
            GLU.gluPerspective( 60.0f, screenWidth / ( float ) screenHeight, 0.01f, 10000.0f );
            GLU.gluLookAt( 400, -210, -1600f, -400, 0, 0, 0, -1, 0 );
        }
        else {
            GLU.gluOrtho2D( -screenWidth / 2, screenWidth / 2, screenHeight / 2, -screenHeight / 2 );
        }
    }

    private void renderMovieScreen(float width, float height, boolean renderBlurr) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureHandle);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBegin(GL_QUADS);
        {
            glColor4f(1, 1, 1, 1);

            // render movie screen
            float z = 1;

            glTexCoord2f(0 * texWidthUsedRatio, 1 * texHeightUsedRatio);
            glVertex3f(-width/2, height/2, z);

            glTexCoord2f(1 * texWidthUsedRatio, 1 * texHeightUsedRatio);
            glVertex3f(width/2, height/2, z);

            glTexCoord2f(1 * texWidthUsedRatio, 0 * texHeightUsedRatio);
            glVertex3f(width/2, -height/2, z);

            glTexCoord2f(0 * texWidthUsedRatio, 0 * texHeightUsedRatio);
            glVertex3f(-width/2, -height/2, z);

            if (renderBlurr)
                blurrOnFloor(width, height);
        }

        glEnd();

        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
    }

    private void blurrOnFloor(float screenW, float screenH) {

        for (int i = -15; i <= 15; i++) {
            glColor4f(1, 1, 1, 0.015f);

            glTexCoord2f(0 * texWidthUsedRatio,
                         1 * texHeightUsedRatio);
                                                        glVertex3f( -screenW/2,
                                                                    screenH/2,
                                                                    0);

            glTexCoord2f(1 * texWidthUsedRatio,
                         1 * texHeightUsedRatio);

                                                        glVertex3f( screenW/2,
                                                                    screenH/2,
                                                                0);

//            glColor4f(1, 1, 1, 0.0f);
            glTexCoord2f(1 * texWidthUsedRatio,
                         0 * texHeightUsedRatio);

                                                        glVertex3f( screenW/2 + i * 5,
                                                                    screenH/2,
                                                                    -screenH);

            glTexCoord2f(   0 * texWidthUsedRatio,
                            0 * texHeightUsedRatio);

                                                        glVertex3f( -screenW/2 + i * 5,
                                                                    screenH/2,
                                                                    -screenH);
        }
    }

    private void setupTexture() {
        textureHandle = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureHandle);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        int wPot = EasyMath.fitInPowerOfTwo( movieWidth );
        int hPot = EasyMath.fitInPowerOfTwo( movieHeight );
        texWidthUsedRatio = (float) movieWidth / wPot;
        texHeightUsedRatio = (float) movieHeight / hPot;

        // 'tmpbuf' should be null, but some drivers are too buggy
        ByteBuffer tmpbuf = BufferUtils.createByteBuffer( wPot * hPot * 3 );
        glTexImage2D(GL_TEXTURE_2D, 0/* level */, GL_RGB, wPot, hPot, 0/* border */, GL_RGB, GL_UNSIGNED_BYTE, tmpbuf);
    }

    public boolean readMovieFrame(Movie movie) {
        textureBuffer = movie.videoStream().readFrameInto(textureBuffer);

        if (textureBuffer == null)
            return false;

        glTexSubImage2D(GL_TEXTURE_2D, 0/* level */, 0, 0, movie.width(), movie.height(), GL_RGB, GL_UNSIGNED_BYTE, textureBuffer);

        return true;
    }
}

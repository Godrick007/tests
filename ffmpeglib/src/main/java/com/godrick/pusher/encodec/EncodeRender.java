package com.godrick.pusher.encodec;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.godrick.ffmpeglib.R;
import com.godrick.ffmpeglib.opengl.ShaderUtil;
import com.godrick.ffmpeglib.surface.EGLSurfaceView;
import com.godrick.ffmpeglib.util.ImageTextureUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class EncodeRender implements EGLSurfaceView.EGLRender {

    private final float[] vertex_data = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,

            0f,0f,
            0f,0f,
            0f,0f,
            0f,0f
    };

    private final float[] texture_data = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };


    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private Context context;


    private int program;
    private int avPosition;
    private int afPosition;

    private int VBOId;

    private int textureId;

    private Bitmap bitmap;

    private int imgTextureId;

    public EncodeRender(Context context, int textureId) {

        bitmap = ImageTextureUtil.createTextImage("godrick",22,"#ff0000","#00000000",0);

        float r = 1.0f * bitmap.getWidth() / bitmap.getHeight();

        float w = r * 0.1f;

        vertex_data[8] = -w /2;
        vertex_data[9] = -0.1f /2;

        vertex_data[10] = w/2;
        vertex_data[11] = -0.1f /2;

        vertex_data[12] = -w/2;
        vertex_data[13] = 0.1f /2;

        vertex_data[14] = w/2;
        vertex_data[15] = 0.1f /2;


        this.context = context;
        this.textureId = textureId;
        this.context = context;
        vertexBuffer = ByteBuffer.allocateDirect(vertex_data.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertex_data);
        vertexBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(texture_data.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(texture_data);
        textureBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated() {

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);


        String vertexSource = ShaderUtil.readRawText(context, R.raw.vertex2);
        String textureSource = ShaderUtil.readRawText(context, R.raw.fragment2);

        program = ShaderUtil.createProgram(vertexSource, textureSource);

        avPosition = GLES20.glGetAttribLocation(program, "v_Position");
        afPosition = GLES20.glGetAttribLocation(program, "f_Position");
//        u_matrix = GLES20.glGetUniformLocation(program,"u_Matrix");

        int[] vbos = new int[1];
        GLES20.glGenBuffers(1,vbos,0);
        VBOId = vbos[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBOId);

        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                vertex_data.length * 4 + texture_data.length * 4,
                null,
                GLES20.GL_STATIC_DRAW
        );

        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                0,
                vertex_data.length * 4,
                vertexBuffer
        );

        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                vertex_data.length * 4,
                texture_data.length * 4,
                textureBuffer
        );

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

        imgTextureId = ImageTextureUtil.loadBitmapTexture2D(bitmap);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame() {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 0f, 0f, 1f);

        GLES20.glUseProgram(program);

//        GLES20.glUniformMatrix4fv(u_matrix,1,false,matrix,0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBOId);


        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, vertex_data.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //img
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,imgTextureId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBOId);

        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, 32);
        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, vertex_data.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

    }
}

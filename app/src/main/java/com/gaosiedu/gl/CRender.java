package com.gaosiedu.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.gaosiedu.myapplication.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CRender implements GLSurfaceView.Renderer {


    private Context context;

    private int program;

    private int avPosition;
    private int afPosition;
    private int textureId;


    private final float[] vertexDate = {

            -1f,-1f,
            1f,-1f,
            -1f,1f,
            1f,1f

    };

    private final float[] textureData = {

//            0f,1f,
//            1f,1f,
//            0f,0f,
//            1f,0f

            1f,0f,
            0f,0f,
            1f,1f,
            0f,1f

    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    public CRender(Context context){

        this.context = context;

        vertexBuffer = ByteBuffer.allocateDirect(vertexDate.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexDate);

        vertexBuffer.position(0);


        textureBuffer = ByteBuffer.allocateDirect(textureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData);

        textureBuffer.position(0);

    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        String vertexShaderSource = ShaderUtil.readRawText(context, R.raw.vertex_shader);
        String fragmentShaderSource = ShaderUtil.readRawText(context,R.raw.fragment_shader);

        program = ShaderUtil.createProgram(vertexShaderSource,fragmentShaderSource);

        if(program > 0){
            avPosition = GLES20.glGetAttribLocation(program,"av_Position");
            afPosition = GLES20.glGetAttribLocation(program,"af_Position");
//            sTexture = GLES20.glGetUniformLocation(program,"sTexture");

            int[] textureId = new int[1];

            GLES20.glGenTextures(1,textureId,0);
            if(textureId[0] != GLES20.GL_TRUE){
                return;
            }

            this.textureId = textureId[0];

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,this.textureId);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);


//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inScaled = false;
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher);

            if(bitmap == null){
                return;
            }


            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);

            bitmap.recycle();

        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0,0,width,height);


    }

    @Override
    public void onDrawFrame(GL10 gl) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f,1.0f,1.0f,1.0f);

        GLES20.glUseProgram(program);

//        GLES20.glUniform4f(afColor,1f,1f,0f,1f);

        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition,2,GLES20.GL_FLOAT,false,8,vertexBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);


        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition,2,GLES20.GL_FLOAT,false,8,textureBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);


    }
}

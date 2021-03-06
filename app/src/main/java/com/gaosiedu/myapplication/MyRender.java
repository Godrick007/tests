package com.gaosiedu.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.godrick.ffmpeglib.opengl.ShaderUtil;
import com.godrick.ffmpeglib.surface.EGLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class MyRender implements EGLSurfaceView.EGLRender {


    private final float[] vertex_data = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,

            -0.5f,-0.5f,
             0.5f,-0.5f,
            -0.5f,0.5f,
             0.5f,0.5f

    };

    private final float[] texture_data = {
//            0f, 0f,
//            1f, 0f,
//            0f, 1f,
//            1f, 1f

            0f,1f,
            1f,1f,
            0f,0f,
            1f,0f

    };


    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private Context context;


    private int program;
    private int avPosition;
    private int afPosition;
    private int textureId;
    private int sampler;

    private int VBOId;
    private int FBOId;

    private FBORender fboRender;

    private int imgTexture;
    private int imgTexture2;


    private int u_matrix;
    private float[] matrix = new float[16];


    private OnRenderCreateListener onRenderCreateListener;

    public MyRender(Context context) {
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

        fboRender = new FBORender(context);

    }


    @Override
    public void onSurfaceCreated() {
        Log.e("render", "onSurfaceCreated");

        fboRender.onCreate();

        String vertexSource = ShaderUtil.readRawText(context, R.raw.vertex2_m);
        String textureSource = ShaderUtil.readRawText(context, R.raw.fragment2);

        program = ShaderUtil.createProgram(vertexSource, textureSource);

        avPosition = GLES20.glGetAttribLocation(program, "v_Position");
        afPosition = GLES20.glGetAttribLocation(program, "f_Position");
        sampler = GLES20.glGetUniformLocation(program,"sTexture");
        u_matrix = GLES20.glGetUniformLocation(program,"u_Matrix");


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


        int[] fbos = new int[1];
        GLES20.glGenBuffers(1,fbos,0);
        FBOId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,FBOId);





        int[] textureIds = new int[1];

        GLES20.glGenTextures(1, textureIds, 0);
        textureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(sampler,0);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);


        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                1080,
                1920,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
        );

        //bind texture 2 fbo
        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                textureId,
                0
        );


        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE){
            //failed
            Log.e("opengl","fbo NOT cool");
        }else{
            //success
            Log.e("opengl","fbo cool");
        }



//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.mipmap.aa);
//
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);
//
//        bitmap.recycle();
//        bitmap = null;




        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);


        imgTexture = loadTexture(R.mipmap.aa);
        imgTexture2 = loadTexture(R.mipmap.map);


        if(onRenderCreateListener != null){
            onRenderCreateListener.onCreate(textureId);
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.e("render", "onSurfaceChanged");
        fboRender.onChange(width,height);
        GLES20.glViewport(0, 0, width, height);

        if(width > height){

            Matrix.orthoM(
                    matrix,
                    0,
                    -width / (height / 424f * 578f),
                    width / (height / 424f * 578f),
                    -1f,
                    1f,
                    -1f,
                    1f
            );

        }else{

            Matrix.orthoM(
                    matrix,
                    0,
                    -1f,
                    1f,
                    -height / (width / 578f * 424f),
                    height / (width / 578f * 424f),
                    -1f,
                    1f
            );

        }

//        Matrix.rotateM(matrix,0,180,0,0,1);
//        Matrix.rotateM(matrix,0,180,0,1,0);
        Matrix.rotateM(matrix,0,180,1,0,0);


    }

    @Override
    public void onDrawFrame() {
        Log.e("render", "onDrawFrame");

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,FBOId);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 0f, 0f, 1f);


        GLES20.glUseProgram(program);

        GLES20.glUniformMatrix4fv(u_matrix,1,false,matrix,0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBOId);


        //绘制第一张图片
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,imgTexture);

        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, vertex_data.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


        //绘制第二张
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,imgTexture2);

        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, 32);
        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, vertex_data.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        fboRender.onDrawFrame(textureId);

    }


    private int loadTexture(int src){

        int[] textureIds = new int[1];

        GLES20.glGenTextures(1, textureIds, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),src);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

        return textureIds[0];
    }


    public void setOnRenderCreateListener(OnRenderCreateListener onRenderCreateListener) {
        this.onRenderCreateListener = onRenderCreateListener;
    }

    public interface OnRenderCreateListener {
        void onCreate(int textureId);
    }

}

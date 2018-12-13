package com.godrick.ffmpeglib.opengl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;


import com.godrick.ffmpeglib.R;
import com.godrick.ffmpeglib.listeners.OnRenderCallback;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CRender implements GLSurfaceView.Renderer ,SurfaceTexture.OnFrameAvailableListener {


    public enum RENDER_TYPE{
        YUV,CODEC
    }

    private RENDER_TYPE render_type;

    private Context context;

    private int program_yuv;

    private int avPosition_yuv;
    private int afPosition_yuv;
    private int textureId;


    private int sampler_y;
    private int sampler_u;
    private int sampler_v;

    private int[] textureId_yuv;

    private int width_yuv;
    private int height_yuv;

    private Buffer y;
    private Buffer u;
    private Buffer v;


    private OnRenderCallback onRenderCallback;


    private final float[] vertexDate = {

            -1f,-1f,
            1f,-1f,
            -1f,1f,
            1f,1f

    };

    private final float[] textureData = {

            0f,1f,
            1f,1f,
            0f,0f,
            1f,0f

//            1f,0f,
//            0f,0f,
//            1f,1f,
//            0f,1f

    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureYuvBuffer;

    //media codec
    private int program_codec;
    private int avPosition_codec;
    private int afPosition_codec;
    private int sampler_codec;
    private int textureId_codec;
    private SurfaceTexture surfaceTexture;
    private Surface surface;

    private OnSurfaceCreatedListener onSurfaceCreatedListener;
    private OnRenderListener onRenderListener;



    public CRender(Context context){

        this.context = context;

        vertexBuffer = ByteBuffer.allocateDirect(vertexDate.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexDate);

        vertexBuffer.position(0);


        textureYuvBuffer = ByteBuffer.allocateDirect(textureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData);

        textureYuvBuffer.position(0);

    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initRender();
        initRenderCodec();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0,0,width,height);


    }

    @Override
    public void onDrawFrame(GL10 gl) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f,0.0f,0.0f,1.0f);
        if(render_type == RENDER_TYPE.YUV){
            Log.e("java","type is yuv");
            renderYUV();
        }else if(render_type == RENDER_TYPE.CODEC){
            Log.e("java","type is codec");
            renderCodec();
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

    }

    public void setRender_type(RENDER_TYPE render_type) {
        this.render_type = render_type;
    }

    public void setOnSurfaceCreatedListener(OnSurfaceCreatedListener onSurfaceCreatedListener) {
        this.onSurfaceCreatedListener = onSurfaceCreatedListener;
    }

    public void setOnRenderListener(OnRenderListener onRenderListener) {
        this.onRenderListener = onRenderListener;
    }

    private void initRender(){

        String vertexShaderSource = ShaderUtil.readRawText(context, R.raw.vertex_shader);
        String fragmentShaderSource = ShaderUtil.readRawText(context,R.raw.fragment_shader);

        program_yuv = ShaderUtil.createProgram(vertexShaderSource,fragmentShaderSource);
        avPosition_yuv = GLES20.glGetAttribLocation(program_yuv,"av_Position");
        afPosition_yuv = GLES20.glGetAttribLocation(program_yuv,"af_Position");

        sampler_y = GLES20.glGetUniformLocation(program_yuv,"sampler_y");
        sampler_u = GLES20.glGetUniformLocation(program_yuv,"sampler_u");
        sampler_v = GLES20.glGetUniformLocation(program_yuv,"sampler_v");

        textureId_yuv = new int[3];

        GLES20.glGenTextures(3,textureId_yuv,0);

        for(int i = 0; i < 3 ; i ++){

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId_yuv[i]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

        }

    }


    public void setYUVRenderData(int width_yuv,int height_yuv,byte[] y,byte[] u, byte[] v){
        this.width_yuv = width_yuv;
        this.height_yuv = height_yuv;
        this.y = ByteBuffer.wrap(y);
        this.u = ByteBuffer.wrap(u);
        this.v = ByteBuffer.wrap(v);
    }

    private void renderYUV(){


        if(width_yuv > 0 && height_yuv > 0 && y != null && u != null && v != null){
            GLES20.glUseProgram(program_yuv);


            GLES20.glEnableVertexAttribArray(avPosition_yuv);
            GLES20.glVertexAttribPointer(avPosition_yuv,2,GLES20.GL_FLOAT,false,8,vertexBuffer);



            GLES20.glEnableVertexAttribArray(afPosition_yuv);
            GLES20.glVertexAttribPointer(afPosition_yuv,2,GLES20.GL_FLOAT,false,8, textureYuvBuffer);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId_yuv[0]);
            GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_LUMINANCE,
                    width_yuv,
                    height_yuv,
                    0,
                    GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE,
                    y
            );

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId_yuv[1]);
            GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_LUMINANCE,
                    width_yuv / 2,
                    height_yuv / 2,
                    0,
                    GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE,
                    u
            );

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId_yuv[2]);
            GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_LUMINANCE,
                    width_yuv / 2,
                    height_yuv / 2,
                    0,
                    GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE,
                    v
            );

            GLES20.glUniform1i(sampler_y,0);
            GLES20.glUniform1i(sampler_u,1);
            GLES20.glUniform1i(sampler_v,2);

            y.clear();
            u.clear();
            v.clear();

            y = null;
            u = null;
            v = null;
        }

    }


    private void initRenderCodec(){

        String vertexShaderSource = ShaderUtil.readRawText(context, R.raw.vertex_shader);
        String fragmentShaderSource = ShaderUtil.readRawText(context,R.raw.fragment_mediacodec);

        program_codec = ShaderUtil.createProgram(vertexShaderSource,fragmentShaderSource);

        avPosition_codec = GLES20.glGetAttribLocation(program_codec,"av_Position");
        afPosition_codec = GLES20.glGetAttribLocation(program_codec,"af_Position");

        sampler_codec = GLES20.glGetUniformLocation(program_codec,"sTexture");

        int[] textureIds = new int[1];

        GLES20.glGenTextures(1,textureIds,0);
        textureId_codec = textureIds[0];

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(textureId_codec);
        surface = new Surface(surfaceTexture);
        surfaceTexture.setOnFrameAvailableListener(this);

        if(onSurfaceCreatedListener != null){
            onSurfaceCreatedListener.onSurfaceCreated(surface);
        }

    }

    private void renderCodec(){
        surfaceTexture.updateTexImage();
        GLES20.glUseProgram(program_codec);

        GLES20.glEnableVertexAttribArray(avPosition_codec);
        GLES20.glVertexAttribPointer(avPosition_codec,2,GLES20.GL_FLOAT,false,8,vertexBuffer);



        GLES20.glEnableVertexAttribArray(afPosition_codec);
        GLES20.glVertexAttribPointer(afPosition_codec,2,GLES20.GL_FLOAT,false,8, textureYuvBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId_codec);

        GLES20.glUniform1i(sampler_codec,0);

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

        if(onRenderListener != null){
            onRenderListener.onRender();
        }


    }



    public interface OnSurfaceCreatedListener{
        void onSurfaceCreated(Surface surface);
    }

    public interface OnRenderListener{
        void onRender();
    }

}

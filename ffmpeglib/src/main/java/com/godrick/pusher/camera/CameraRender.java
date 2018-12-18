package com.godrick.pusher.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.godrick.ffmpeglib.R;
import com.godrick.ffmpeglib.opengl.ShaderUtil;
import com.godrick.ffmpeglib.surface.EGLSurfaceView;
import com.godrick.ffmpeglib.util.DisplayUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CameraRender implements EGLSurfaceView.EGLRender ,SurfaceTexture.OnFrameAvailableListener{


    private final float[] vertex_data = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };

    private final float[] texture_data = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };


    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;



    private int program;
    private int avPosition;
    private int afPosition;
    private int fboTextureId;
    private int sampler;

    private int VBOId;
    private int FBOId;

    private int u_matrix;
    private float[] matrix = new float[16];

    private int imgTexture;
    private int imgTexture2;


    int cameraTextureId;

    private SurfaceTexture surfaceTexture;

    private Context context;

    private OnSurfaceCreatedListener onSurfaceCreatedListener;

    private CameraFBORender fboRender;


    private int screen_width;
    private int screen_height;
    private int width;
    private int height;


    public CameraRender(Context context) {
        this.context = context;

        screen_width = DisplayUtil.getScreenWidth(context);
        screen_height = DisplayUtil.getScreenHeight(context);

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

        fboRender = new CameraFBORender(context);

        resetMatrix();
    }

    @Override
    public void onSurfaceCreated() {

        fboRender.onCreate();

        String vertexSource = ShaderUtil.readRawText(context, R.raw.vertex_shader2);
        String textureSource = ShaderUtil.readRawText(context, R.raw.fragment_mediacodec);

        program = ShaderUtil.createProgram(vertexSource, textureSource);

        avPosition = GLES20.glGetAttribLocation(program, "av_Position");
        afPosition = GLES20.glGetAttribLocation(program, "af_Position");
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


        int[] ms = new int[1];



        int[] textureIds = new int[1];

        GLES20.glGenTextures(1, textureIds, 0);
        fboTextureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId);

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
                screen_width,
                screen_height,
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
                fboTextureId,
                0
        );


        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE){
            //failed
            Log.e("opengl","fbo NOT cool");
        }else{
            //success
            Log.e("opengl","fbo cool");
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);



        int[] textureIdsEos = new int[1];

        GLES20.glGenTextures(1,textureIdsEos,0);


        cameraTextureId = textureIdsEos[0];

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,cameraTextureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(cameraTextureId);

        surfaceTexture.setOnFrameAvailableListener(this);

        if(onSurfaceCreatedListener != null){
            onSurfaceCreatedListener.onSurfaceCreated(surfaceTexture,fboTextureId);
        }

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,0);

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
//        GLES20.glViewport(0,0,width,height);
//        fboRender.onChange(width,height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame() {

        surfaceTexture.updateTexImage();



        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 0f, 0f, 1f);


        GLES20.glUseProgram(program);

        GLES20.glViewport(0,0,screen_width,screen_height);



        GLES20.glUniformMatrix4fv(u_matrix,1,false,matrix,0);


        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,FBOId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBOId);
        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, vertex_data.length * 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,fboTextureId);



        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);



        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);


        fboRender.onChange(width,height);
        fboRender.onDrawFrame(fboTextureId);

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    public void resetMatrix(){
        Matrix.setIdentityM(matrix,0);
    }

    public void setAngle(float angle,float x,float y,float z){
        Matrix.rotateM(matrix,0,angle,x,y,z);
    }


    public int getFBOTextureId(){
        return fboTextureId;
    }


    public void setOnSurfaceCreatedListener(OnSurfaceCreatedListener onSurfaceCreatedListener) {
        this.onSurfaceCreatedListener = onSurfaceCreatedListener;
    }

    public interface OnSurfaceCreatedListener{
        void onSurfaceCreated(SurfaceTexture surfaceTexture,int textureId);
    }
}

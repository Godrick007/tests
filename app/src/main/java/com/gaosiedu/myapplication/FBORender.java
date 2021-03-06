package com.gaosiedu.myapplication;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.godrick.ffmpeglib.opengl.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class FBORender {

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

    private Context context;


    private int program;
    private int avPosition;
    private int afPosition;

    private int VBOId;

//    private int u_matrix;
//    private float[] matrix = new float[16];


    public FBORender(Context context){

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

    public void onCreate(){

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

    }


    public void onChange(int width,int height){
        GLES20.glViewport(0,0,width,height);
//        if(width > height){
//
//            Matrix.orthoM(
//                    matrix,
//                    0,
//                    -width / (height / 424f * 578f),
//                    width / (height / 424f * 578f),
//                    -1f,
//                    1f,
//                    -1f,
//                    1f
//            );
//
//        }else{
//
//            Matrix.orthoM(
//                    matrix,
//                    0,
//                    -1f,
//                    1f,
//                    -height / (width / 578f * 424f),
//                    height / (width / 578f * 424f),
//                    -1f,
//                    1f
//            );
//
//        }
    }

    public void onDrawFrame(int imgTexture){

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 0f, 0f, 1f);

        GLES20.glUseProgram(program);

//        GLES20.glUniformMatrix4fv(u_matrix,1,false,matrix,0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,imgTexture);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBOId);


        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, vertex_data.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
    }

}

package com.godrick.ffmpeglib.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderUtil {

    public static String readRawText(Context context, int rawId) {

        InputStream is = null;
        BufferedReader reader = null;

        try {


            is = context.getResources().openRawResource(rawId);
            reader = new BufferedReader(new InputStreamReader(is));

            StringBuffer sb = new StringBuffer();

            String line;

            while (true) {

                line = reader.readLine();

                if (line != null) {
                    sb.append(line).append("\n");
                }else{
                    break;
                }

            }
            return sb.toString();
        }catch(Exception e){
            e.printStackTrace();

        }finally{

            try {
                if(reader != null)
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if(is != null)
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



    private static int loadShader(int shaderType,String source){

        int shader = GLES20.glCreateShader(shaderType);
        if(shader != 0){

            GLES20.glShaderSource(shader,source);

            GLES20.glCompileShader(shader);

            int[] compile = new int[1];

            GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,compile,0);

            if(compile[0] != GLES20.GL_TRUE){
                Log.e("gl","shader compile error");
                GLES20.glDeleteShader(shader);
                shader = 0;
            }

        }

        return shader;

    }


    public static int createProgram(String vertexSource,String fragmentSource){

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexSource);
        if(vertexShader == 0){
            return 0;
        }


        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentSource);
        if(fragmentShader == 0){
            return 0;
        }

        int program = GLES20.glCreateProgram();

        if(program != 0) {

            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);

//            int[] linkStatus = new int[1];
//
//            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
//            if (linkStatus[0] != GLES20.GL_TRUE) {
//                Log.e("gl", "program link error");
//                GLES20.glDeleteProgram(program);
//                return 0;
//            }
        }

        return program;
    }

}
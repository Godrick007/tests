package com.godrick.ffmpeglib.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;

import java.nio.ByteBuffer;

public class ImageTextureUtil {

    public static Bitmap createTextImage(String text,int textSize,String textColor,String backgroundColor,int padding){

        Paint paint = new Paint();
        paint.setColor(Color.parseColor(textColor));
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);


        float width = paint.measureText(text,0,text.length());
        float top = paint.getFontMetrics().top;
        float bottom = paint.getFontMetrics().bottom;

        Bitmap bitmap = Bitmap.createBitmap(
                (int)(width + padding * 2),
                (int) (bottom - top + padding *2),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor(backgroundColor));
        canvas.drawText(text,padding,- top + padding,paint);
        return bitmap;

    }


    public static int loadBitmapTexture2D(Bitmap bitmap){

        int[] textureIds  = new int[1];

        GLES20.glGenTextures(1,textureIds,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

        ByteBuffer bitmapBuffer = ByteBuffer.allocate(bitmap.getWidth() * bitmap.getHeight() * 4);
        bitmap.copyPixelsToBuffer(bitmapBuffer);
        bitmapBuffer.flip();

        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                bitmap.getWidth(),
                bitmap.getHeight(),
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                bitmapBuffer
                );

        return textureIds[0];
    }

}

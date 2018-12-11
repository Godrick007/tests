package com.godrick.ffmpeglib.listeners;

public interface OnRenderCallback {

    void onCallback(int width,int height,byte[] y, byte[] u, byte[] v);

}

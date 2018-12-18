package com.gaosiedu.myapplication;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.godrick.ffmpeglib.opengl.CGLSurfaceView2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity3 extends AppCompatActivity {

    MyEGLSurfaceView surfaceView;
    LinearLayout llContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        surfaceView = findViewById(R.id.surface_view);
        llContent = findViewById(R.id.ll_content);


        surfaceView.getRender().setOnRenderCreateListener(new MyRender.OnRenderCreateListener() {
            @Override
            public void onCreate(int textureId) {

                runOnUiThread(()->{


                    if(llContent.getChildCount() > 0){
                        llContent.removeAllViews();
                    }



                    for(int i = 0 ; i < 3 ; i++){

                        int id = 0;
                        switch (i){
                            case 0:
                                id = R.raw.fragment_shader1;
                                break;
                            case 1:
                                id = R.raw.fragment_shader2;
                                break;
                            case 2:
                                id = R.raw.fragment_shader3;
                                break;
                        }


                        MutiSurfaceView mutiSurfaceView = new MutiSurfaceView(MainActivity3.this);
                        MutiRender render = new MutiRender(MainActivity3.this);
                        render.setTextureId(textureId,id);
                        mutiSurfaceView.setRender(render);
                        mutiSurfaceView.setSurfaceAndEglSurface(null,surfaceView.getEglContext());

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

                        layoutParams.width = 200;
                        layoutParams.height = 300;


                        mutiSurfaceView.setLayoutParams(layoutParams);

                        llContent.addView(mutiSurfaceView);

                    }



                });

            }
        });


    }



}

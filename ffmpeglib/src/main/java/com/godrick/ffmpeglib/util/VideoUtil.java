package com.godrick.ffmpeglib.util;

import android.media.MediaCodecList;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

public class VideoUtil {

    private static Map<String,String> codeMapping = new HashMap<>();


    static{
        codeMapping.put("h264","video/avc");
    }


    public static String findVideoCodecName(String codeName){

        if(codeMapping.containsKey(codeName)){
            return codeMapping.get(codeName);
        }

        return "";
    }


    public static boolean isSupportCodec(String codecName){

        boolean supportCode = false;

        int count = MediaCodecList.getCodecCount();

        for(int i = 0; i < count ; i ++){

            String[] types = MediaCodecList.getCodecInfoAt(i).getSupportedTypes();

            for(int j = 0; j < types.length; j++){

                if(types[j].equals(findVideoCodecName(codecName))){
                    supportCode = true;
                    break;
                }

            }

            if(supportCode){
                break;
            }

        }


        return supportCode;
    }

}

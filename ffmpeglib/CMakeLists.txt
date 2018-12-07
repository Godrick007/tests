# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# cmakelists 设置c++11
include(CheckCXXCompilerFlag)
CHECK_CXX_COMPILER_FLAG("-std=c++11" COMPILER_SUPPORTS_CXX11)
CHECK_CXX_COMPILER_FLAG("-std=c++0x" COMPILER_SUPPORTS_CXX0X)
if(COMPILER_SUPPORTS_CXX11)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
elseif(COMPILER_SUPPORTS_CXX0X)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++0x")
else()
    message(STATUS "The compiler ${CMAKE_CXX_COMPILER} has no C++11 support. Please use a different C++ compiler.")

endif()
# cmakelists 设置c++11


SET(CMAKE_BUILD_TYPE "Debug")
SET(CMAKE_CXX_FLAGS_DEBUG "$ENV{CXXFLAGS} -O0 -Wall -g2 -ggdb")
SET(CMAKE_CXX_FLAGS_RELEASE "$ENV{CXXFLAGS} -O3 -Wall")
include_directories(../ffmpeglib/src/main/cpp/include)
include_directories(../ffmpeglib/src/main/cpp/include/libsoundtouch)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ../ffmpeglib/src/main/cpp/Queue.cpp
        ../ffmpeglib/src/main/cpp/PlayStatus.cpp
        ../ffmpeglib/src/main/cpp/enter.cpp
        ../ffmpeglib/src/main/cpp/Ffmpeg.cpp
        ../ffmpeglib/src/main/cpp/Audio.cpp
        ../ffmpeglib/src/main/cpp/Video.cpp
        ../ffmpeglib/src/main/cpp/CallJava.cpp
        )

add_library(

        soundtouch

        SHARED

        ../ffmpeglib/src/main/cpp/soundtouch/AAFilter.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/FIFOSampleBuffer.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/FIRFilter.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/cpu_detect_x86.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/sse_optimized.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/WavFile.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/RateTransposer.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/SoundTouch.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/InterpolateCubic.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/InterpolateLinear.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/InterpolateShannon.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/TDStretch.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/BPMDetect.cpp
        ../ffmpeglib/src/main/cpp/soundtouch/PeakFinder.cpp
)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)


set(SO_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)


add_library(avcodec-57 SHARED IMPORTED)
set_target_properties(avcodec-57
        PROPERTIES IMPORTED_LOCATION
        ${SO_DIR}/${ANDROID_ABI}/libavcodec-57.so)


add_library(avdevice-57 SHARED IMPORTED)
set_target_properties(avdevice-57
        PROPERTIES IMPORTED_LOCATION
        ${SO_DIR}/${ANDROID_ABI}/libavdevice-57.so)


add_library(avfilter-6 SHARED IMPORTED)
set_target_properties(avfilter-6
        PROPERTIES IMPORTED_LOCATION
        ${SO_DIR}/${ANDROID_ABI}/libavfilter-6.so)


add_library(avformat-57 SHARED IMPORTED)
set_target_properties(avformat-57
        PROPERTIES IMPORTED_LOCATION
        ${SO_DIR}/${ANDROID_ABI}/libavformat-57.so)


add_library(avutil-55 SHARED IMPORTED)
set_target_properties(avutil-55
        PROPERTIES IMPORTED_LOCATION
        ${SO_DIR}/${ANDROID_ABI}/libavutil-55.so)


add_library(postproc-54 SHARED IMPORTED)
set_target_properties(postproc-54
        PROPERTIES IMPORTED_LOCATION
        ${SO_DIR}/${ANDROID_ABI}/libpostproc-54.so)


add_library(swresample-2 SHARED IMPORTED)
set_target_properties(swresample-2
        PROPERTIES IMPORTED_LOCATION
        ${SO_DIR}/${ANDROID_ABI}/libswresample-2.so)


add_library(swscale-4 SHARED IMPORTED)
set_target_properties(swscale-4
        PROPERTIES IMPORTED_LOCATION
        ${SO_DIR}/${ANDROID_ABI}/libswscale-4.so)


add_library(x264 SHARED IMPORTED)
set_target_properties(x264
        PROPERTIES IMPORTED_LOCATION
        ${SO_DIR}/${ANDROID_ABI}/libx264.so)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.



        native-lib
        OpenSLES
        x264
        avutil-55
        avdevice-57
        avfilter-6
        avformat-57
        avcodec-57
        postproc-54
        swresample-2
        swscale-4

        android
        soundtouch

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
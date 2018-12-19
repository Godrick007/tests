//
// Created by Godrick Crown on 2018/12/19.
//

#ifndef TESTS_RECORDBUFFER_H
#define TESTS_RECORDBUFFER_H


class RecordBuffer {

public:

    short **buffer;
    int index = -1;

public:

    RecordBuffer(int bufferSize);

    ~RecordBuffer();

    short *getRecordBuffer();

    short *getCacheBuffer();

};


#endif //TESTS_RECORDBUFFER_H

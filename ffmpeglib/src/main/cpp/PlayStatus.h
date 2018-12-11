//
// Created by Godrick Crown on 2018/11/28.
//

#ifndef TESTS_PLAYERSTATUS_H
#define TESTS_PLAYERSTATUS_H


class PlayStatus {

public:

    bool exit;

    bool load = true;

    bool seek = false;

    bool pause = false;


public:

    PlayStatus();

    ~PlayStatus();

};


#endif //TESTS_PLAYERSTATUS_H

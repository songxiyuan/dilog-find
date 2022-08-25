package com.github.songxiyuan.dilogfind;

public class Position {
    String time;
    String tag;
    String file;
    int row;
    int column;

    Position(String f, int r, int c) {
        file = f;
        row=r;
        column=c;
        time=f;
        tag="haha";

    }
}
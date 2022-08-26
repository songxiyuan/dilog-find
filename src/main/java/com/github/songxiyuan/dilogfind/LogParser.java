package com.github.songxiyuan.dilogfind;

import org.bouncycastle.util.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {
    String Origin;
    String level;
    String time;
    String tag;
    String file;
    String[] params;
    int row;

    LogParser(String lineLog) {
        try {
            Origin = lineLog;
            String[] infos = lineLog.split("\\|\\|");
            String[] levelTimePathRowTag = infos[0].split("]\\[");
            params = infos;
            level = levelTimePathRowTag[0].trim().substring(1);
            time = levelTimePathRowTag[1].split("\\+")[0];
            String[] pathRowTag = levelTimePathRowTag[2].split("/");
            String[] fileRowTag = pathRowTag[pathRowTag.length - 1].split(":");
            file = fileRowTag[0];
            String[] rowTag = fileRowTag[1].split("]");
            row = Integer.parseInt(rowTag[0]);
            tag = rowTag[1].trim();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
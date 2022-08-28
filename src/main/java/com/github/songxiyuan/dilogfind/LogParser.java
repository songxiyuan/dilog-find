package com.github.songxiyuan.dilogfind;

import com.intellij.openapi.util.text.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.taskdefs.Get;

import java.util.List;
import java.util.Vector;

public class LogParser {
    String Origin;
    String level;
    String time;
    String tag;
    String[] paths;
    String fileName;
    String[] params;
    int row;

    LogParser(String lineLog) {
        try {
            Origin = lineLog;
            String[] infos = lineLog.split("\\|\\|");
            String[] level_Time_PathRowTag = infos[0].split("]\\[");
            params = infos;
            level = level_Time_PathRowTag[0].trim().substring(1);
            time = level_Time_PathRowTag[1].split("\\+")[0].substring(11);
            String[] pathRow_Tag = level_Time_PathRowTag[2].split("]");
            tag = pathRow_Tag[1].trim();
            String[] path_Row = pathRow_Tag[0].split(":");
            row = Integer.parseInt(path_Row[1]) - 1;

            //获取path 和 name
            String[] pathsTemp = path_Row[0].split("/");
            paths = new String[pathsTemp.length - 1];
            for (int i = 1; i < pathsTemp.length - 1; i++) {
                String[] path = pathsTemp[i].split("\\.");
                paths[i - 1] = path[0];
            }
            paths[paths.length - 1] = pathsTemp[pathsTemp.length - 1];
            fileName = pathsTemp[pathsTemp.length - 1];

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /*
    ...xiaojukeji.com/guarana/dive-honors-g/middleware.traceRequestOut/trace.go
    -> guarana/dive-honors-g/middleware/trace.go

    ..eji.com/guarana/dive-honors-g/middleware.TraceWithConfig.func1.1/trace.go
    -> guarana/dive-honors-g/middleware/trace.go

    ..e-honors-g/models/public.(*RightRecordCacheService).RightRecords/right_record_cache_service.go
    -> models/public/right_record_cache_service.go
     */
}
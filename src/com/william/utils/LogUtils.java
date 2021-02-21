package com.william.utils;

import java.time.LocalDateTime;

public class LogUtils {
    public static void log(Object... objs) {
        StringBuffer content = new StringBuffer();
        content.append("[ ");
        content.append(LocalDateTime.now());
        content.append(" ]");
        content.append(" --- ");
        for (Object obj : objs) {
            content.append(obj.toString());
            content.append(" ");
        }
        System.out.println(content.toString());
    }
}

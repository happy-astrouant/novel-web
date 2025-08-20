package io.github.xzy.novel.core.util;

public class DesensitizedUtil {

    // 对userID脱密，前后各保留2位
    public static String userId(String input) {
        String start = input.substring(0, 2);
        String end = input.substring(input.length() - 2);
        return start + "****" + end;
    }

    public static String chineseName(String s) {
        return s.charAt(0) + (s.length() > 2 ? "**" : "*");
    }
}

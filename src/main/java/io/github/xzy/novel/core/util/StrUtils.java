package io.github.xzy.novel.core.util;

public class StrUtils {

    public static String hide(String input, int startInclude, int endExclude) {
        String result = input.substring(0, startInclude);
        String replace = "*".repeat(endExclude - startInclude);
        result += replace;
        result += input.substring(endExclude);
        return result;
    }
}

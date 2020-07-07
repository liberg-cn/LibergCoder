package cn.liberg.coder.tool.util;

public class Paths {

    public static String unifyDirectory(String path) {
        String unified = Strings.replaceAll(path, '\\', '/');
        if (!unified.endsWith("/")) {
            unified += "/";
        }
        return unified;
    }
}

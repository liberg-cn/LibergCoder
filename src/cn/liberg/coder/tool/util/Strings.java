package cn.liberg.coder.tool.util;

public class Strings {


    public static String strip(String src, String what) {
        return strip(src, what, what);
    }

    public static String strip(String src, String begin, String end) {
        int len = begin.length();
        if (src.length() >= len) {
            int p1 = 0, p2 = src.length();
            if (src.startsWith(begin)) p1 += len;
            if (p1 < p2 && src.endsWith(end)) p2 -= end.length();
            return src.substring(p1, p2);
        } else {
            return src;
        }
    }

    public static String replaceAll(String src, char fromChar, char toChar) {
        char[] arr = src.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == fromChar) {
                arr[i] = toChar;
            }
        }
        return new String(arr);
    }

    public static String findFirst(String src, int pos, String starter, String ends) {
        String rt = null;
        int len = src.length();
        int p1 = src.indexOf(starter, pos);
        if (p1 >= 0) {
            int mid = p1 + starter.length();
            if (mid < len) {
                int p2 = src.indexOf(ends, mid);
                if (p2 >= 0) {
                    rt = src.substring(mid, p2);
                }
            }
        }
        return rt;
    }

    public static String findFirst(String src, int pos, char starter, char ends) {
        String rt = null;
        int len = src.length();
        int p1 = src.indexOf(starter, pos);
        if (p1 >= 0) {
            int mid = p1 + 1;
            if (mid < len) {
                int p2 = src.indexOf(ends, mid);
                if (p2 >= 0) {
                    rt = src.substring(mid, p2);
                }
            }
        }
        return rt;
    }

    public static void main(String[] args) {
        String test = "*abcd@1111@";

    }

    /**
     * 从尾巴开始往前找被[starter,ends]包裹的部分
     */
    public static String findLast(String src, char starter, char ends) {
        String rt = null;
        int p2 = src.lastIndexOf(ends);
        if (p2 >= 1) {
            int p1 = src.lastIndexOf(starter, p2);
            if (p1 >= 0) {
                rt = src.substring(p1 + 1, p2);
            }
        }
        return rt;
    }

}

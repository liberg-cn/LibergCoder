package cn.liberg.coder.tool.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static BufferedWriter bufferedWriter(File file, String path, boolean append) throws FileNotFoundException, UnsupportedEncodingException {
        FileOutputStream fos = file != null ? new FileOutputStream(file, append) : new FileOutputStream(path, append);
        return new BufferedWriter(new OutputStreamWriter(fos, UTF_8));
    }
    public static BufferedWriter bufferedWriter(String path, boolean append) throws FileNotFoundException, UnsupportedEncodingException {
        return bufferedWriter(null, path, append);
    }
    public static BufferedWriter bufferedWriter(String path) throws FileNotFoundException, UnsupportedEncodingException {
        return bufferedWriter(null, path, false);
    }
    public static BufferedWriter bufferedWriter(File file, boolean append) throws FileNotFoundException, UnsupportedEncodingException {
        return bufferedWriter(file, null, append);
    }
    public static BufferedWriter bufferedWriter(File file) throws FileNotFoundException, UnsupportedEncodingException {
        return bufferedWriter(file, null, false);
    }

    public static BufferedReader bufferedReader(String path) throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(path), UTF_8));
    }

    public static BufferedReader bufferedReader(File file) throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8));
    }

}

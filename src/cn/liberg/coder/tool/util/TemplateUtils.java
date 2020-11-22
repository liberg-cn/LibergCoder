package cn.liberg.coder.tool.util;

import java.io.BufferedWriter;
import java.io.IOException;

public class TemplateUtils {

    public static void writeImportsLogger(BufferedWriter bw) throws IOException {
        bw.write("import org.slf4j.Logger;\r\n");
        bw.write("import org.slf4j.LoggerFactory;\r\n");
    }

    public static void writeDefineLogger(BufferedWriter bw, String className) throws IOException {
        bw.write("    private static final Logger logger = LoggerFactory.getLogger(" + className + ".class);\r\n");
    }

}

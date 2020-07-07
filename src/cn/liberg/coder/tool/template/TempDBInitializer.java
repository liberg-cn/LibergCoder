package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.LibergToolContext;

import java.io.*;

public class TempDBInitializer {
    public static final String name = "DBInitializer";

    public static void createFileIfAbsent(LibergToolContext context) {
        File file = new File(context.getDataPath() +name+".java");
        if(!file.exists()) {
            try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(generateContent(context));
                System.out.println("> " + context.getDataPackage() + "." + name + "  created.");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("> " + context.getDataPackage() + "." + name + "  already exists.");
        }
    }

    private static String generateContent(LibergToolContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ");
        sb.append(context.getDataPackage());
        sb.append(";");
        sb.append(Formats.NL2);
        sb.append("public class DBInitializer {");
        sb.append(Formats.NL2);
        sb.append("    public void initData() {");
        sb.append(Formats.NL);
        sb.append("    }");
        sb.append(Formats.NL);
        sb.append("}");
        sb.append(Formats.NL);
        return sb.toString();
    }
}

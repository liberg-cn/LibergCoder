package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.util.FileUtils;

import java.io.BufferedWriter;
import java.io.File;

public final class TempDBConfig {
    public static final String selfName = "DBConfig";

    public static void createFileIfAbsent(LibergToolContext ctx) throws Exception {
        File file = new File(ctx.getDataPath() + selfName + ".java");
        if (!file.exists()) {
            try (BufferedWriter bw = FileUtils.bufferedWriter(file)) {
                writeTo(bw, ctx);
                System.out.println("> " + ctx.getDataPackage() + "." + selfName + "  created.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeTo(BufferedWriter bw, LibergToolContext ctx) throws Exception {
        bw.write("package " + ctx.getDataPackage() + ";\r\n");
        bw.write("\r\n");
        bw.write("import cn.liberg.database.IDataBaseConf;\r\n");
        bw.write("import org.springframework.beans.factory.annotation.Value;\r\n");
        bw.write("import org.springframework.stereotype.Component;\r\n");
        bw.write("\r\n");
        bw.write("@Component\r\n");
        bw.write("public class DBConfig implements IDataBaseConf {\r\n");
        bw.write("    private String driverName = \"com.mysql.cj.jdbc.Driver\";\r\n");
        bw.write("    @Value(value = \"${spring.datasource.url}\")\r\n");
        bw.write("    private String url = \"\";\r\n");
        bw.write("    @Value(value = \"${spring.datasource.name}\")\r\n");
        bw.write("    private String dbName = \"\";\r\n");
        bw.write("    @Value(value = \"${spring.datasource.username}\")\r\n");
        bw.write("    private String userName = \"root\";\r\n");
        bw.write("    @Value(value = \"${spring.datasource.password}\")\r\n");
        bw.write("    private String password = \"\";\r\n");
        bw.write("    private String charset = \"utf8\";\r\n");
        bw.write("    private String collation = \"utf8_general_ci\";\r\n");
        bw.write("\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public String getDriverName() {\r\n");
        bw.write("        return driverName;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public String getDbName() {\r\n");
        bw.write("        return dbName;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public String getUrl() {\r\n");
        bw.write("        return url;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public String getUserName() {\r\n");
        bw.write("        return userName;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public String getPassword() {\r\n");
        bw.write("        return password;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public String getCharset() {\r\n");
        bw.write("        return charset;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public String getCollation() {\r\n");
        bw.write("        return collation;\r\n");
        bw.write("    }\r\n");
        bw.write("}\r\n");
        bw.write("\r\n");
    }
}

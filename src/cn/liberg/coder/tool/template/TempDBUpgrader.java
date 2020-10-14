package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.LibergToolException;
import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.java.JClass;
import cn.liberg.coder.tool.java.JMethod;
import cn.liberg.coder.tool.mysql.TableUpgrader;
import cn.liberg.coder.tool.util.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TempDBUpgrader {
    public static final String NAME = "DBUpgrader";
    public static int currentVer;
    private static final List<String> tableAdded = new ArrayList<>();
    private static final List<TableUpgrader> tableUpgraders = new ArrayList<>();

    public static void addCreateTableLine(String line) {
        tableAdded.add(line);
    }
    public static void addTableUpgrader(TableUpgrader upgrader) {
        tableUpgraders.add(upgrader);
    }

    public static void createFileIfAbsent(LibergToolContext context) {
        File file = new File(context.getDataPath() + NAME + ".java");
        if (!file.exists()) {
            try (BufferedWriter bw = FileUtils.bufferedWriter(file)) {
                writeInitContent(context, bw);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void save(LibergToolContext ctx) throws LibergToolException {
        String path = ctx.getDataPath() + NAME + ".java";
        JClass parser = new JClass(path);
        if (parser.loadedFromFile) {
            if (tableAdded.size() > 0 || tableUpgraders.size() > 0) {
                if(upgrade(parser)) {
                    parser.writeToFile(path);
                    System.out.println("> " + ctx.getDataPackage() + "." + NAME + " updated.");
                }
            }
        }
    }

    private static boolean upgrade(JClass parser) throws LibergToolException {
        JMethod upgradeMethod = null;
        int upgradeMethodIndex = -1;
        for(int i=0;i<parser.methods.size();i++) {
            JMethod jm = parser.methods.get(i);
            if("upgrade".equals(jm.name)) {
                upgradeMethodIndex = i;
                upgradeMethod = jm;
                break;
            }
        }
        if(upgradeMethod==null) {
            System.out.println("> WARNING: DBUpgrader.upgrade() is not found...");
            return false;
        }

        String methodName;
        do {
            methodName = "upgradeTo" + (++currentVer);
        } while (parser.hasMethod(methodName));

        JMethod jm = new JMethod("private void " + methodName + "(Statement stat) throws SQLException {");
        for (TableUpgrader upgrader : tableUpgraders) {
            for (String op : upgrader.build()) {
                jm.appendBodyLine("        " + op);
            }
        }
        for (String toAdd : tableAdded) {
            jm.appendBodyLine("    " + toAdd);
        }
        parser.addMethod(jm, upgradeMethodIndex+1);
        parser.writeToFile(parser.mCodeFilePath);
        return true;
    }

    private static void writeInitContent(LibergToolContext ctx, BufferedWriter bw) throws IOException {
        bw.write("package " + ctx.getDataPackage() + ";\r\n");
        bw.write("\r\n");
        bw.write("import cn.liberg.database.IDataBaseConf;\r\n");
        bw.write("import cn.liberg.database.TableAlteration;\r\n");
        bw.write("import org.apache.commons.logging.Log;\r\n");
        bw.write("import org.apache.commons.logging.LogFactory;\r\n");
        bw.write("\r\n");
        bw.write("import java.lang.reflect.Method;\r\n");
        bw.write("import java.sql.SQLException;\r\n");
        bw.write("import java.sql.Statement;\r\n");
        bw.write("\r\n");
        bw.write("public class " + NAME + " extends DBImpl {\r\n");
        bw.write("    private Log logger = LogFactory.getLog(getClass());\r\n");
        bw.write("\r\n");
        bw.write("    public " + NAME + "(IDataBaseConf dbConf) {\r\n");
        bw.write("        super(dbConf);\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    public int upgrade(Statement stat, int dbVersion, int newVersion) {\r\n");
        bw.write("        Class clazz = this.getClass();\r\n");
        bw.write("        int version = dbVersion;\r\n");
        bw.write("        try {\r\n");
        bw.write("            while(version<newVersion) {\r\n");
        bw.write("                version++;\r\n");
        bw.write("                Method method = clazz.getDeclaredMethod(\"upgradeTo\" + version, Statement.class);\r\n");
        bw.write("                if(method != null) {\r\n");
        bw.write("                    method.invoke(this, stat);\r\n");
        bw.write("                }\r\n");
        bw.write("            }\r\n");
        bw.write("        } catch (Exception e) {\r\n");
        bw.write("            version--;\r\n");
        bw.write("            logger.error(\"DBUpgrader failed:\" + super.getName() +\r\n");
        bw.write("                    \". version=\" + version + \", expectedVersion=\" + newVersion, e);\r\n");
        bw.write("        }\r\n");
        bw.write("        return version;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    private TableAlteration alter(String tableName) {\r\n");
        bw.write("        return new TableAlteration(tableName);\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("}\r\n");
    }

}

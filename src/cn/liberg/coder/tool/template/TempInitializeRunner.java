package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.util.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class TempInitializeRunner {
    public static final String selfName = "InitializeRunner";

    public static void createFileIfAbsent(LibergToolContext ctx) throws Exception {
        File file = new File(ctx.getMiscPath() +selfName+".java");
        if(!file.exists()) {
            try(BufferedWriter bw = FileUtils.bufferedWriter(file)) {
                writeTo(bw, ctx);
                System.out.println(ctx.getMiscPackage() + "." + selfName + "  created.");
            }
        }
    }

    private static void writeTo(BufferedWriter bw, LibergToolContext ctx) throws Exception {
        String dataPackage = ctx.getDataPackage();
        bw.write("package "+ctx.getMiscPackage()+";\r\n");
        bw.write("\r\n");
        bw.write("import "+dataPackage+".DBConfig;\r\n");
        bw.write("import "+dataPackage+".DBImpl;\r\n");
        bw.write("import cn.liberg.database.DBHelper;\r\n");
        bw.write("import org.springframework.beans.factory.annotation.Autowired;\r\n");
        bw.write("import org.springframework.boot.ApplicationArguments;\r\n");
        bw.write("import org.springframework.boot.ApplicationRunner;\r\n");
        bw.write("import org.springframework.stereotype.Component;\r\n");
        bw.write("\r\n");
        bw.write("@Component\r\n");
        bw.write("public class "+selfName+" implements ApplicationRunner {\r\n");
        bw.write("    private DBConfig dbConf;\r\n");
        bw.write("\r\n");
        bw.write("    public InitializeRunner(@Autowired DBConfig dbConf) {\r\n");
        bw.write("        this.dbConf = dbConf;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public void run(ApplicationArguments args) throws Exception {\r\n");
        bw.write("        DBHelper.self().init(new DBImpl(dbConf));\r\n");
        bw.write("    }\r\n");
        bw.write("}\r\n");
    }
}

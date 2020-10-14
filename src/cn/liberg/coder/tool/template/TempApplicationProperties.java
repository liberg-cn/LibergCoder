package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.core.Apair;
import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.util.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Initialize操作的时候，修改application.properties
 * 增加server.port/spring.application.name，以及DataSource相关配置
 */
public class TempApplicationProperties {
    public static final String selfName = "application.properties";
    private static final List<Apair<String, String>> defaultConfigs = new ArrayList<>(16);

    public static void addMissingConfigs(LibergToolContext ctx) throws Exception {
        File file = new File(ctx.getResourcesPath() + selfName);
        if (!file.exists()) return;

        initConfigs(ctx);

        Properties props = new Properties();
        BufferedReader fr = FileUtils.bufferedReader(file);
        props.load(fr);
        fr.close();
        List<String> missingLines = buildMissingConfigLines(props);
        if(missingLines.size()>0) {
            BufferedWriter bw = FileUtils.bufferedWriter(file, true);
            for(String line : missingLines) {
                bw.write(line);
                bw.write("\r\n");
            }
            bw.close();
        }
    }

    private static void initConfigs(LibergToolContext ctx) {
        String projectName = ctx.getProjectName();
        String dbName = Formats.toDataBaseName(projectName);
        defaultConfigs.add(new Apair<>("server.port", "8080"));
        defaultConfigs.add(new Apair<>("spring.application.name", projectName));
        defaultConfigs.add(new Apair<>("spring.datasource.url", "jdbc:mysql://127.0.0.1:3306/"));
        defaultConfigs.add(new Apair<>("spring.datasource.name", dbName));
        defaultConfigs.add(new Apair<>("spring.datasource.username", "root"));
        defaultConfigs.add(new Apair<>("spring.datasource.password", ""));
    }

    private static List<String> buildMissingConfigLines(Properties props) {
        List<String> list = new ArrayList<>();
        for(Apair<String, String> item : defaultConfigs) {
            if(props.getProperty(item.k) == null) {
                list.add(item.k + "=" + item.v);
            }
        }
        return list;
    }
}

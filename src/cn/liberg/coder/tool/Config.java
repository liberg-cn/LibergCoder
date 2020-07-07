package cn.liberg.coder.tool;

import cn.liberg.coder.tool.util.Paths;

import java.io.*;
import java.util.Properties;

public class Config {
    public static final String configFileName = "LibergConfig.properties";
    public static final String keyEnableLog = "enableLog";
    public static final String keyUpgradeData = "upgradeData";
    public static final String keyCreateApiDocument = "createApiDocument";
    public static final String keyDataPackage = "dataPackage";
    public static final String keyEntityPackage = "entityPackage";
    public static final String keyTypePackage = "typePackage";
    public static final String keyDaoPackage = "daoPackage";
    public static final String keyAccessPackage = "accessPackage";
    public static final String keyServicePackage = "servicePackage";
    public static final String keyInterfacesPackage = "interfacesPackage";
    public static final String keyControllerApiPackage = "controllerApiPackage";
    public static final String keyMiscPackage = "miscPackage";
    public static final String keyApiDocumentPath = "apiDocumentPath";

    private Properties props = null;
    private static volatile Config selfInstance;

    private Config() {
        props = new Properties();
    }

    public static Config self() {
        if (selfInstance == null) {
            synchronized (Config.class) {
                if (selfInstance == null) {
                    selfInstance = new Config();
                }
            }
        }
        return selfInstance;
    }

    public boolean isEnableLog() {
        return "true".equals(get(keyEnableLog));
    }

    public boolean isUpgradeData() {
        return "true".equals(get(keyUpgradeData));
    }

    public boolean isCreateApiDocument() {
        return "true".equals(get(keyCreateApiDocument));
    }

    public static Config loadFrom(String directory){
        String root = Paths.unifyDirectory(directory);
        File configFile = new File(root + configFileName);
        Config config = null;
        if (configFile.exists()) {
            config = new Config();
            try {
                FileReader fileReader = new FileReader(configFile);
                config.props.load(fileReader);
                config.redirectLogFile(root);
                fileReader.close();
                return config;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Config createOrLoad(String directory) throws Exception {
        directory = Paths.unifyDirectory(directory);
        Config config = new Config();
        Properties props = config.props;
        File configFile = new File(directory + configFileName);
        if (!configFile.exists()) {
            /**
             * 是否记录代码生成日志
             * 日志文件，位于project根目录下
             * 如果代码生成插件运行失败，可以开启日志来跟踪错误
             */
            props.put(keyEnableLog, "true");
            props.put(keyUpgradeData, "true");
            props.put(keyCreateApiDocument, "true");

            props.put(keyDataPackage, "data");
            props.put(keyEntityPackage, "data.entity");
            /**
             * 存放entity字段类型定义的（枚举）类
             */
            props.put(keyTypePackage, "data.type");
            props.put(keyDaoPackage, "data.dao");
            props.put(keyAccessPackage, "data.access");
            props.put(keyServicePackage, "service");
            props.put(keyInterfacesPackage, "service.interfaces");
            props.put(keyControllerApiPackage, "controller.api");
            props.put(keyMiscPackage, "misc");
            /**
             * 接口描述文档位置
             * 默认：src/main/resources/static/api-doc
             *
             *  -可以指定基于resources的相对位置，如"static/my-api-doc"
             *  -可以指定绝对路径，如"D:/dev/nginx/web/myProject/api-doc"
             */
            props.put(keyApiDocumentPath, "static/api-doc");
            configFile.createNewFile();
            FileWriter fw = new FileWriter(configFile);
            props.store(fw, "LibergCoder Plugin Configs");
            fw.close();
        } else {
            FileReader fileReader = new FileReader(configFile);
            props.load(fileReader);
            fileReader.close();
        }
        config.redirectLogFile(directory);
        return config;
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    private void redirectLogFile(String storeDirectory) {
        if (isEnableLog()) {
            try {
                System.setOut(new PrintStream(new FileOutputStream(new File(storeDirectory + "LibergCoder.log"), true)));
                System.setErr(new PrintStream(new FileOutputStream(new File(storeDirectory + "LibergCoder.err"), true)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

package cn.liberg.coder.tool;

import cn.liberg.coder.tool.util.Paths;
import cn.liberg.coder.tool.util.Strings;

import java.io.*;
import java.util.Properties;

public class LibergToolContext {
    private String projectName;
    private String projectRootPath;//
    private String basePath;//absolute path of 'src/main/'
    private String javaCodePath;//src/main/java/[basePackage]/
    private String resourcesPath;//src/main/resources/
    private String basePackage;
    private Config config;

    public LibergToolContext(String projectName, String projectRootPath, String basePackge, Config config) throws Exception {
        this.projectName = projectName;
        String root = Paths.unifyDirectory(projectRootPath);
        this.projectRootPath = root;
        this.basePackage = basePackge;
        this.config = config;
        basePath = root + "src/main/";
        javaCodePath = root + "src/main/java/" + Strings.replaceAll(basePackge, '.', '/') + "/";
        resourcesPath = root + "src/main/resources/";
    }

    public static Properties getProperties(String projectRootPath) throws Exception {
        Properties props = null;
        File configFile = new File(Paths.unifyDirectory(projectRootPath)
                + Config.configFileName);
        if (configFile.exists()) {
            props = new Properties();
            FileReader fileReader = new FileReader(configFile);
            props.load(fileReader);
            fileReader.close();
        }
        return props;
    }

    public String getProjectName() {
        return projectName;
    }

    public boolean isUpgradeData() {
        return config.isUpgradeData();
    }

    public boolean isCreateApiDocument() {
        return config.isCreateApiDocument();
    }

    public String getProjectRootPath() {
        return projectRootPath;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public String getResourcesPath() {
        return resourcesPath;
    }

    public boolean getEnableLog() {
        return "true".equals(config.get(Config.keyEnableLog));
    }

    public String getDataPackage() {
        return mkPackage(config.get(Config.keyDataPackage));
    }

    public String getDataPath() {
        return mkCodePath(config.get(Config.keyDataPackage));
    }

    public String getEntityPath() {
        return mkCodePath(config.get(Config.keyEntityPackage));
    }

    public String getEntityPackage() {
        return mkPackage(config.get(Config.keyEntityPackage));
    }

    public String getTypePath() {
        return mkCodePath(config.get(Config.keyTypePackage));
    }

    public String getTypePackage() {
        return mkPackage(config.get(Config.keyTypePackage));
    }

    public String getDaoPath() {
        return mkCodePath(config.get(Config.keyDaoPackage));
    }

    public String getAccessPath() {
        return mkCodePath(config.get(Config.keyAccessPackage));
    }

    public String getDaoPackage() {
        return mkPackage(config.get(Config.keyDaoPackage));
    }

    public String getAccessPackage() {
        return mkPackage(config.get(Config.keyAccessPackage));
    }

    public String getServicePath() {
        return mkCodePath(config.get(Config.keyServicePackage));
    }

    public String getServicePackage() {
        return mkPackage(config.get(Config.keyServicePackage));
    }

    public String getInterfacesPath() {
        return mkCodePath(config.get(Config.keyInterfacesPackage));
    }

    public String getInterfacesPackage() {
        return mkPackage(config.get(Config.keyInterfacesPackage));
    }

    public String getControllerApiPath() {
        return mkCodePath(config.get(Config.keyControllerApiPackage));
    }

    public String getControllerApiPackage() {
        return mkPackage(config.get(Config.keyControllerApiPackage));
    }

    public String getMiscPath() {
        return mkCodePath(config.get(Config.keyMiscPackage));
    }

    public String getMiscPackage() {
        return mkPackage(config.get(Config.keyMiscPackage));
    }

    public String getApiDocumentPath() {
        String realPath;
        String docPath = config.get(Config.keyApiDocumentPath);
        if (docPath == null || docPath.length() < 2) {
            realPath = basePath + "resources/static/api-doc/";
        } else {
            if (docPath.startsWith("/") || docPath.charAt(1) == ':') {
                realPath = docPath;
            } else {
                realPath = basePath + "resources/" + docPath;
            }
            if (!realPath.endsWith("/")) {
                realPath += "/";
            }
        }
        mkDirs(realPath);
        return realPath;
    }

    private String mkDirs(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        return path;
    }

    private String mkPackage(String sub) {
        return basePackage + "." + sub;
    }

    private String mkCodePath(String pkg) {
        String path = Strings.replaceAll(pkg, '.', '/') + "/";
        return mkDirs(javaCodePath + path);
    }
}
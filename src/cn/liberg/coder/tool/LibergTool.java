package cn.liberg.coder.tool;

import cn.liberg.coder.tool.java.JClassEntity;
import cn.liberg.coder.tool.java.JInterface;
import cn.liberg.coder.tool.template.*;

import java.util.ArrayList;
import java.util.List;

public class LibergTool {
    public static final String PROJECT_NAME = "LibergCoder";
    public static final String VERSION = "1.2.0";
    public static final String MARK = PROJECT_NAME + "@" + VERSION;

    LibergToolContext context;
    String lineDividerBegin = ">------------------------------------------------>";
    String lineDivider = ">>";
    String lineDividerEnd = "<------------------------------------------------<";
    long startTime;
    ArrayList<JClassEntity> entityList;
    TempDBImpl tempDBCreatorImpl;
    ArrayList<TempDao> tempDaoList;
    ArrayList<TempAccess> tempAccessList;
    ArrayList<TempController> tempControllerList;
    ArrayList<TempInterfaceDocument> tempInterfaceDocumentList;
    ArrayList<TempService> tempServiceList;
    ILibergToolCallback callback = ILibergToolCallback.DefalutImpl;

    public LibergTool(LibergToolContext ctx) throws LibergToolException {
        System.out.println(lineDividerBegin);
        System.out.println("> " + MARK + " preparing ...");
        startTime = System.currentTimeMillis();
        context = ctx;
        entityList = new ArrayList<>();
        tempDBCreatorImpl = new TempDBImpl(ctx);
        tempDaoList = new ArrayList<>();
        tempAccessList = new ArrayList<>();
        tempControllerList = new ArrayList<>();
        if (ctx.isCreateApiDocument()) {
            tempInterfaceDocumentList = new ArrayList<>();
        }
        tempServiceList = new ArrayList<>();
    }

    public static void doInitialize(LibergToolContext ctx) throws LibergToolException {
        try {
            //data.DBConfig.java
            TempDBConfig.createFileIfAbsent(ctx);
            //data.DBInitializer.java
            TempDBInitializer.createFileIfAbsent(ctx);
            //data.DBImpl.java
            TempDBImpl.createFileIfAbsent(ctx);
            //data.DBUpgrader.java
            TempDBUpgrader.createFileIfAbsent(ctx);

            //mkdirs
            ctx.getEntityPath();
            ctx.getTypePath();
            ctx.getDaoPath();
//            ctx.getAccessPath();
            ctx.getServicePath();
            ctx.getInterfacesPath();
            ctx.getControllerApiPath();

            //misc.ResponseBodyProcessor.java
            TempResponseBodyProcessor.createFileIfAbsent(ctx);
            //misc.InitializerRunner.java
            TempInitializeRunner.createFileIfAbsent(ctx);

            //添加mysql/fastjson依赖
            TempApplicationPOM pom = new TempApplicationPOM(ctx);
            pom.addMysqlIfAbsent();
            pom.addFastJsonIfAbsent();
            pom.save();

            //添加默认配置到application.properties
            TempApplicationProperties.addMissingConfigs(ctx);
        } catch (Exception e) {
            throw new LibergToolException(e);
        }
    }

    public static void doBuild(LibergToolContext ctx, List<String> entityList, List<String> interfaceList) throws LibergToolException {
        LibergTool tool = new LibergTool(ctx);
        for(String entity : entityList) {
            tool.addEntity(entity);
        }
        for(String anInterface : interfaceList) {
            tool.addInterface(anInterface);
        }
        tool.save();
    }


    public void setCallback(ILibergToolCallback callback) {
        this.callback = callback;
    }

    public static String getVersion() {
        return VERSION;
    }

    public void addEntity(String... entityNames) throws LibergToolException {
        for (int i = 0; i < entityNames.length; i++) {
            addEntity(entityNames[i]);
        }
    }

    public void addEntity(List<String> entityNames) throws LibergToolException {
        for (String entityName : entityNames) {
            addEntity(entityName);
        }
    }

    public void addEntity(String entityName) throws LibergToolException {
        JClassEntity entity = new JClassEntity(context, context.getEntityPath() + entityName + ".java");
        if (entity.name == null) {
            entity.name = entityName;
        }
        entityList.add(entity);
        tempDBCreatorImpl.update(entity);
        tempDaoList.add(new TempDao(context, entity));
//        tempAccessList.add(new TempAccess(context, entity));
        callback.onAddEntity(entity);
    }

    public void addInterface(String interfaceName) throws LibergToolException {
        JInterface jInterface = new JInterface(context.getInterfacesPath() + interfaceName + ".java");
        tempServiceList.add(new TempService(context, jInterface));
        tempControllerList.add(new TempController(context, jInterface));
        if (context.isCreateApiDocument()) {
            tempInterfaceDocumentList.add(new TempInterfaceDocument(context, jInterface));
        }
        callback.onAddInterface(jInterface);
    }


    public void save() throws LibergToolException {
        tempDBCreatorImpl.save();
        System.out.println(lineDivider);
        for (JClassEntity jEntity : entityList) {
            jEntity.save();
        }
        System.out.println(lineDivider);
        for (TempDao dao : tempDaoList) {
            dao.save();
        }
//        for (TempAccess access : tempAccessList) {
//            access.save();
//        }
        System.out.println(lineDivider);
        for (TempService service : tempServiceList) {
            service.save();
        }
        System.out.println(lineDivider);
        for (TempController controller : tempControllerList) {
            controller.save();
        }
        if (context.isCreateApiDocument()) {
            System.out.println(lineDivider);
            for (TempInterfaceDocument tempDoc : tempInterfaceDocumentList) {
                tempDoc.save();
                callback.onApiDocumentCreated(tempDoc.getSelfPath());
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("> "+ MARK + " finished, timeTaken: " + (endTime - startTime) + "ms.");
        System.out.println(lineDividerEnd);
        callback.onStart();
    }

}

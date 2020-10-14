package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.LibergToolException;
import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.java.*;
import cn.liberg.coder.tool.util.Strings;

import java.util.ArrayList;
import java.util.List;

public class TempService {
    LibergToolContext context;
    String keyName;
    String interfaceName;
    JInterface jInterface;

    String selfName;
    String selfPath;
    JClass parser;

    public TempService(LibergToolContext ctx, JInterface jInterface) throws LibergToolException {
        interfaceName = jInterface.name;
        this.context = ctx;
        this.jInterface = jInterface;

        keyName = Strings.strip(interfaceName, "I", "Service");
        selfName = keyName + "Service";
        selfPath = context.getServicePath() + selfName + ".java";

        parser = new JClass(selfPath);
        if(!parser.loadedFromFile) {
            initTemplate();
        } else {
            updateTemplate();
        }
    }


    public void save() throws LibergToolException {
        parser.writeToFile(selfPath);
        String tip = "  created.";
        if(parser.loadedFromFile) {
            tip = "  updated.";
        }
        System.out.println("> " + context.getServicePackage() + "." + selfName + tip);
    }

    private void initTemplate() {
        parser.mPackage = context.getServicePackage();

        parser.mImports.add("cn.liberg.core.OperatorException");
        parser.mImports.add("cn.liberg.core.Response");
        parser.mImports.add(context.getEntityPackage() + "." + keyName);
        parser.mImports.add(context.getInterfacesPackage() + "." + interfaceName);
        parser.mImports.add("org.springframework.stereotype.Service");

        parser.name = selfName;
        parser.defLine = "public class "+selfName+" implements "+interfaceName+" {";
        parser.classAnnos.add("@Service");

        updateInterfacesMethod();
    }

    private void updateTemplate() {
        updateInterfacesMethod();
    }

    private void updateInterfacesMethod() {
        ArrayList<JInterfaceMethod> list = jInterface.getMethods();
        for(JInterfaceMethod method : list) {
//            if(!parser.hasMethod(method.name)) {
//                parser.addMethod(makeControllerMethod(method));
//            }
            parser.addOrUpdateMethodSignature(makeControllerMethod(method));
        }
    }

    private JMethod makeControllerMethod(JInterfaceMethod method) {
        JMethod jm = new JMethod(method.name, method.returnType);
        ArrayList<String> annoLines = new ArrayList<>();
        annoLines.add("@Override");
        jm.annoLines = annoLines;
        jm.restPart = method.restPart.replaceFirst(";", " {");


        List<MetaArg> argList = method.getArgList();
        if(argList != null) {
            try {
                for(MetaArg arg : argList) {
                    jm.addArg(arg.copy());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JType type = new JType(jm.returnType);
        jm.appendBodyLine("        //TODO...");
        if(!type.isVoid) {
            String rt = "null";
            if(type.isNumber) {
                rt = "0";
            } else if(type.isBoolean) {
                rt = "false";
            }
            jm.appendBodyLine("        return "+rt+";");
        }
        return jm;
    }

}

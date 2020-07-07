package cn.liberg.coder.tool.java;

/**
 * 成员变量、方法参数、方法返回值的类型
 */
public class JType {
    public String type;
    public boolean isNumber;
    public boolean isBoolean;
    public boolean isVoid;
    public boolean isRef;//引用类型

    public boolean isArray;
    public boolean isList;
    public boolean isMap;
    public boolean isResponse;

    public JType(String type) {
        this.type = type;
        if(type.endsWith("[]")) {
            isArray = true;
            isRef = true;
        } else if(isVoid(type)) {
            isVoid = true;
        } else if(isNumber(type)) {
            isNumber = true;
        } else if(isBoolean(type)) {
            isBoolean = true;
        } else {
            isRef = true;
            if(type.startsWith("List") || type.startsWith("ArrayList")) {
                isList = true;
            } else if(type.startsWith("Map") || type.startsWith("HashMap")) {
                isMap = true;
            } else if("Response".equals(type)) {
                isResponse = true;
            }
        }
    }

    public static boolean isVoid(String type) {
        return "void".equals(type);
    }

    public static boolean isBoolean(String type) {
        return "boolean".equals(type);
    }

    public static boolean isNumber(String type) {
        boolean rt = false;
        switch (type) {
            case "byte":
            case "int":
            case "short":
            case "long":
                rt = true;
                break;
            default:
                break;
        }
        return rt;
    }
}

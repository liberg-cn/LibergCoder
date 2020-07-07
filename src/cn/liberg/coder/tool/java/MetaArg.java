package cn.liberg.coder.tool.java;

import cn.liberg.coder.tool.util.RegExpr;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class MetaArg {
    public String name;//参数名称
    public String type;//参数类型
    public MetaAnno anno;//参数上的注解

    public static final RegExpr RE = new RegExpr("((@\\w+(\\(.*\\))? +)*)((\\w+(<[, <>\\[\\]\\w]+>)?(\\[\\])?) +(\\w+)) *");

    public static ArrayList<MetaArg> parseArgs(String args) {
        ArrayList<MetaArg> argList = null;
        if(args.length()>0) {
            argList = new ArrayList<>();
            Matcher matcher = RE.matcher(args);
            String annos;
            while(matcher.find()){
                MetaArg arg = new MetaArg();
                arg.type = matcher.group(5);
                arg.name = matcher.group(8);
                annos = matcher.group(1);
                if(annos!=null) {
                    annos = annos.trim();
                    if(annos.length()>0) {
                        arg.anno = new MetaAnno(annos);
                    }
                }
                argList.add(arg);
            }
        }
        return argList;
    }

    public MetaArg copy() {
        MetaArg rt = new MetaArg();
        rt.name = name;//字符串，作为不可变的对象，无须复制
        rt.type = type;
        if(anno!=null) {
            rt.anno = anno.copy();
        }
        return rt;
    }

    public void writeTo(BufferedWriter bw) throws IOException {
        if(anno != null) {
            anno.writeTo(bw);
        }
        bw.write(type);
        bw.write(" ");
        bw.write(name);
    }
}

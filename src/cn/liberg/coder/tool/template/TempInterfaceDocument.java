package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.LibergToolException;
import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.java.JInterface;
import cn.liberg.coder.tool.java.JInterfaceMethod;
import cn.liberg.coder.tool.java.MetaArg;
import cn.liberg.coder.tool.util.FileUtils;
import cn.liberg.coder.tool.util.Strings;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 * 生成接口文档.md
 * 抽取方法上面的注释
 * url和参数
 * 默认放到resources/static/doc目录下，支持放到本地其他目录，
 */
public class TempInterfaceDocument {
    LibergToolContext context;
    String keyName;
    String interfaceName;
    JInterface jInterface;

    String selfName;
    String selfPath;
    String localUrl = "";
    String testEnvUrl = "";
    String onlineEnvUrl = "";
    String version = "V1.0";

    public TempInterfaceDocument(LibergToolContext ctx, JInterface jInterface) throws LibergToolException {
        interfaceName = jInterface.name;
        this.context = ctx;
        this.jInterface = jInterface;

        keyName = Strings.strip(interfaceName, "I", "Service");
        selfName = keyName + "Service";
        selfPath = context.getApiDocumentPath() + selfName + "-api.md";
    }

    public String getSelfPath() {
        return selfPath;
    }

    public void save() throws LibergToolException {
        boolean isUpdate = false;

        BufferedWriter bw = null;
        try {
            File file = new File(selfPath);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                isUpdate = true;
                parseDocMeta(file);
            }
            bw = FileUtils.bufferedWriter(file);
            bw.write("## " + selfName +"接口文档" + version);
            bw.write(Formats.NL2);
            bw.write("> **基础URL**"+Formats.NL);
            bw.write("开发本机："+localUrl+Formats.NL);
            bw.write("测试环境："+testEnvUrl+Formats.NL);
            bw.write("正式环境："+onlineEnvUrl);
            bw.write(Formats.NL2);
            writeInterfaceMethodDocument(bw);
        } catch (IOException e) {
            e.printStackTrace();
            throw new LibergToolException(e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String tip = isUpdate ? "updated." : "created.";
        System.out.println("> " + context.getApiDocumentPath() + selfName + ".md api-document " + tip);
    }

    private void parseDocMeta(File file) {
        try(BufferedReader bw = new BufferedReader(new FileReader(file))) {


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeInterfaceMethodDocument(BufferedWriter bw) throws IOException {
        ArrayList<JInterfaceMethod> list = jInterface.getMethods();
        for (int i = 0; i < list.size(); i++) {
            writeMethodDocument(i, list.get(i), bw);
        }
    }

    private void writeMethodDocument(int index, JInterfaceMethod method, BufferedWriter bw) throws IOException {
        bw.write("### "+(++index)+". POST " + "`/api/" + keyName.toLowerCase()+"/" + method.name+"`");
        bw.write(Formats.NL);

        if(method.descTop != null) {
            method.descTop.wirteTo(bw);
        }
        bw.write(Formats.NL);
        bw.write("参数："+Formats.NL);

        List<MetaArg> argList = method.getArgList();
        if (argList != null) {
            try {
                for (MetaArg arg : argList) {
                    bw.write("        `"+Formats.forShort(arg.name)+"`--"+arg.name+"，"+arg.type.toLowerCase()+"类型");
                    bw.write(Formats.NL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bw.write("返回值："+method.returnType);
        bw.write(Formats.NL2);
    }

}

package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.util.FileUtils;

import java.io.*;

public final class TempResponseBodyProcessor {
    public static final String selfName = "ResponseBodyProcessor";

    public static void createFileIfAbsent(LibergToolContext ctx) throws Exception {
        File file = new File(ctx.getMiscPath() +selfName+".java");
        if(!file.exists()) {
            try(BufferedWriter bw = FileUtils.bufferedWriter(file)) {
                writeTo(bw, ctx);
                System.out.println("> " + ctx.getMiscPackage() + "." + selfName + "  created.");
            }
        }
    }

    public static void writeTo(BufferedWriter bw, LibergToolContext ctx) throws IOException {
        bw.write("package "+ctx.getMiscPackage()+";\r\n");
        bw.write("\r\n");
        bw.write("import com.alibaba.fastjson.JSON;\r\n");
        bw.write("import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;\r\n");
        bw.write("import org.springframework.core.MethodParameter;\r\n");
        bw.write("import org.springframework.http.MediaType;\r\n");
        bw.write("import org.springframework.http.server.ServerHttpRequest;\r\n");
        bw.write("import org.springframework.http.server.ServerHttpResponse;\r\n");
        bw.write("import org.springframework.http.server.ServletServerHttpRequest;\r\n");
        bw.write("import org.springframework.http.server.ServletServerHttpResponse;\r\n");
        bw.write("import org.springframework.util.StringUtils;\r\n");
        bw.write("import org.springframework.web.bind.annotation.ControllerAdvice;\r\n");
        bw.write("import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;\r\n");
        bw.write("\r\n");
        bw.write("import javax.servlet.http.HttpServletRequest;\r\n");
        bw.write("import javax.servlet.http.HttpServletResponse;\r\n");
        bw.write("import java.io.IOException;\r\n");
        bw.write("import java.io.OutputStream;\r\n");
        bw.write("import java.nio.charset.Charset;\r\n");
        bw.write("import java.nio.charset.StandardCharsets;\r\n");
        bw.write("\r\n");
        bw.write("\r\n");
        bw.write("@ControllerAdvice\r\n");
        bw.write("public class ResponseBodyProcessor extends FastJsonHttpMessageConverter implements ResponseBodyAdvice {\r\n");
        bw.write("    public static final String utf8 = \"utf-8\";\r\n");
        bw.write("    public static final Charset utf8Charset = utf8Charset = StandardCharsets.UTF_8;\r\n");
        bw.write("    public static final String jsonpCallback = \"callback\";\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, " +
                "Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {\r\n");
        bw.write("        HttpServletRequest request = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest();\r\n");
        bw.write("        String callback = request.getParameter(jsonpCallback);\r\n");
        bw.write("\r\n");
        bw.write("        HttpServletResponse response = ((ServletServerHttpResponse) serverHttpResponse).getServletResponse();\r\n");
        bw.write("        response.setCharacterEncoding(utf8);\r\n");
        bw.write("        response.setHeader(\"Access-Control-Allow-Origin\", \"*\");\r\n");
        bw.write("\r\n");
        bw.write("        String json = JSON.toJSONString(o);\r\n");
        bw.write("        if (!StringUtils.isEmpty(callback)) {\r\n");
        bw.write("            json = new StringBuilder(callback).append(\"(\").append(json).append(\")\").toString();\r\n");
        bw.write("        }\r\n");
        bw.write("        try(OutputStream out = response.getOutputStream()) {\r\n");
        bw.write("            out.write(json.getBytes(utf8Charset));\r\n");
        bw.write("            out.flush();\r\n");
        bw.write("        } catch (IOException e) {\r\n");
        bw.write("            e.printStackTrace();\r\n");
        bw.write("        }\r\n");
        bw.write("        return o;\r\n");
        bw.write("    }\r\n");
        bw.write("\r\n");
        bw.write("    @Override\r\n");
        bw.write("    public boolean supports(MethodParameter methodParameter, Class aClass) {\r\n");
        bw.write("        return true;\r\n");
        bw.write("    }\r\n");
        bw.write("}\r\n");
    }

}

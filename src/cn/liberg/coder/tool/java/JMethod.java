package cn.liberg.coder.tool.java;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.util.RegExpr;
import cn.liberg.coder.tool.core.ILineReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class JMethod {
    public String name;
    public ArrayList<String> annoLines;//方法上面可能存在的多行注解
    public String defLine = "";//方法名称定义的行，多行会合并成一行
    public JDesc descTop;//方法上面的注释

    public String visitor = "public";//访问修饰符，默认public
    public boolean isStatic;
    public boolean isFinal;
    public String returnType;//返回值类型
    public String args;
    public boolean isConstructor;
    public String restPart;//方法参数之后，其余的部分
    public static final RegExpr RE = new RegExpr("^(public|protected|private) +(static +)?(final +)?(\\w+(<.+>)?(\\[\\])? +)?(\\w+) *\\(([@=<>\\[\\]\\.(\"), \\w]*)\\)([^)]*)$");

    //private member
    private MetaAnno anno = null;
    private List<MetaArg> argList;
    private ArrayList<String> body = new ArrayList<String>();

    public MetaAnno getAnno() {
        if (anno == null) {
            anno = new MetaAnno(annoLines);
        }
        return anno;
    }

    public void addAnnoLine(String line) {
        if (annoLines == null) {
            annoLines = new ArrayList<>();
        }
        annoLines.add(line);
        if (anno != null) {
            anno.addLine(line);
        }
    }

    public JMethod(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public JMethod(String line) {
        defLine = line;
        parse(line);
    }

    public JMethod(ILineReader reader, String firstLine) throws IOException {
        String line = firstLine;
        int[] arr = Formats.charCount(line, '(', ')');
        int leftBracket = arr[0];
        int rightBracket = arr[1];

        if (leftBracket > rightBracket) {
            StringBuilder sb = new StringBuilder(256);
            sb.append(line);
            do {
                line = reader.next(true);
                sb.append(" ");
                sb.append(line);
                arr = Formats.charCount(line, '(', ')');
                leftBracket += arr[0];
                rightBracket += arr[1];
            } while (leftBracket > rightBracket);
            defLine = sb.toString();
        } else {
            defLine = line;
        }
        parse(defLine);
    }

    public void appendBodyLine(String line) {
        body.add(line);
    }

    public String popBodyLine() {
        String line = null;
        if (body.size() > 0) {
            line = body.remove(body.size() - 1);
        }
        return line;
    }

    public ArrayList<String> getBody() {
        return body;
    }

    public void setBody(ArrayList<String> newBody) {
        this.body = newBody;
    }

    public void addArg(MetaArg arg) {
        if (argList == null) {
            argList = new ArrayList<>();
        }
        argList.add(arg);
    }

    /**
     * To parse method line, like: "public String method1(String arg1) {"
     */
    private void parse(String line) {
        defLine = line;
        Matcher matcher = RE.findMatcher(line);
        if (matcher != null) {
            visitor = matcher.group(1);
            name = matcher.group(7);
            if (matcher.group(2) != null) {
                isStatic = true;
            }
            if (matcher.group(3) != null) {
                isFinal = true;
            }
            if (matcher.group(4) != null) {
                returnType = matcher.group(4).trim();
            } else {
                isConstructor = true;
                returnType = name;
            }
            args = matcher.group(8).trim();
            if (args.length() > 0) {
                argList = MetaArg.parseArgs(args);
            }
            restPart = matcher.group(9);
        }
    }

    public void writeTo(BufferedWriter bw) throws IOException {
        if (descTop != null) {
            descTop.wirteTo(bw);
        }
        if (annoLines != null) {
            for (String it : annoLines) {
                bw.write(Formats.IN);
                bw.write(it);
                bw.write(Formats.NL);
            }
        }
        bw.write(Formats.IN);
        //----
//		bw.write(defLine);
        writeDefLine(bw);
        //----
        bw.write(Formats.NL);
        for (String it : body) {
            bw.write(it);
            bw.write(Formats.NL);
        }
        bw.write(Formats.IN);
        bw.write("}");
        bw.write(Formats.NL2);
    }

    private void writeDefLine(BufferedWriter bw) throws IOException {
        int indent = 4;
        bw.write(visitor);
        bw.write(" ");
        indent += visitor.length() + 1;
        if (isStatic) {
            bw.write("static ");
            indent += 7;
        }
        if (isFinal) {
            bw.write("final ");
            indent += 6;
        }
        if (!isConstructor) {
            bw.write(returnType);
            indent += returnType.length();
            bw.write(" ");
            indent++;
        }
        bw.write(name);
        indent += name.length();
        bw.write("(");
        indent++;

        writeArgs(bw, indent);
        bw.write(")");
        bw.write(restPart);
    }

    private void writeArgs(BufferedWriter bw, int indent) throws IOException {
        if (argList != null) {
            MetaArg arg;
            for (int i = 0; i < argList.size(); i++) {
                arg = argList.get(i);
                arg.writeTo(bw);
                if (i < argList.size() - 1) {
                    bw.write(", ");
                    if (arg.anno != null) {//参数带有注解时，下一个参数另起一行
                        bw.write(Formats.NL);
                        writeIndent(bw, indent);
                    }
                }
            }
        }
    }

    private void writeIndent(BufferedWriter bw, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            bw.write(" ");
        }
    }
}
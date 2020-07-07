package cn.liberg.coder.tool.java;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.util.RegExpr;
import cn.liberg.coder.tool.core.ILineReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class JInterfaceMethod {
	public String name;
	public JDesc descTop;//方法上面的注释
	public ArrayList<String> annoLines;//方法上面可能存在的多行注解
	public String defLine = "";//方法名称定义的行，多行合并成一行

	public String visitor = "public";//访问修饰符，接口只能是public
	public boolean isStatic;
	public boolean isDefault;
	public String returnType;//返回值类型
	public String args;
	public String restPart;//方法参数之后，其余的部分

	public static final RegExpr RE = new RegExpr("^(public +)?(static +)?(default +)?(\\w+(<.+>)?(\\[\\])?) +(\\w+) *\\(([@=<>\\[\\]\\.(\"), \\w]*)\\)([^)]*)$");

	//private member
	private MetaAnno anno = null;
	private List<MetaArg> argList;
	private ArrayList<String> body;//default方法，或static方法才有方法实现

	public MetaAnno getAnno() {
		if(anno == null) {
			anno = new MetaAnno(annoLines);
		}
		return anno;
	}

	JInterfaceMethod() {

	}

	public List<MetaArg> getArgList() {
		return argList;
	}

	public void appendBodyLine(String line) {
		if(isDefault || isStatic) {
			body.add(line);
		}
	}

	public String popBodyLine() {
		String line = null;
		if(body!=null && body.size()>0) {
			line = body.remove(body.size()-1);
		}
		return line;
	}


	/**
	 * To parse method lines, like:
	 * public long save(@json("ec") String entityClass,
	 *                  @json("do") String dataObj) throws OperatorException;
	 */
	public JInterfaceMethod(ILineReader reader, String firstLine) throws IOException {
		String line = firstLine;
		int[] arr = Formats.charCount(line, '(', ')');
		int leftBracket = arr[0];
		int rightBracket = arr[1];

		if(leftBracket > rightBracket) {
			StringBuilder sb = new StringBuilder(256);
			sb.append(line);
			do {
				line = reader.next(true);
				sb.append(" ");
				sb.append(line);
				arr = Formats.charCount(line, '(', ')');
				leftBracket += arr[0];
				rightBracket += arr[1];
			} while(leftBracket>rightBracket);
			defLine = sb.toString();
		} else {
			defLine = line;
		}
		parse(defLine);
	}

	private void parse(String line) {
		Matcher matcher = RE.findMatcher(line);
		if(matcher != null) {
			if(matcher.group(1) != null) {
				visitor = matcher.group(1).trim();
			}
			name = matcher.group(7);
			if(matcher.group(2) != null) {
				isStatic = true;
			}
			if(matcher.group(3) != null) {
				isDefault = true;
			}
			if(matcher.group(4) != null) {
				returnType = matcher.group(4).trim();
			}
			args = matcher.group(8).trim();
			if(args.length()>0) {
				argList = MetaArg.parseArgs(args);
			}
			restPart = matcher.group(9);
		}
	}
	
	public void writeTo(BufferedWriter bw) throws IOException {
		if(descTop != null) {
			descTop.wirteTo(bw);
		}
		if(annoLines != null) {
			for(String it : annoLines) {
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
		if(isDefault || isStatic) {
			for(String it : body) {
				bw.write(it);
				bw.write(Formats.NL);
			}
			bw.write(Formats.IN);
			bw.write("}");
		}
		bw.write(Formats.NL2);
	}

	private void writeDefLine(BufferedWriter bw) throws IOException {
		int indent = 4;
		bw.write(visitor);
		bw.write(" ");
		indent += visitor.length()+1;
		if(isStatic) {
			bw.write("static ");
			indent += 7;
		}
		if(isDefault) {
			bw.write("default ");
			indent += 8;
		}
		bw.write(returnType);
		indent += returnType.length();
		bw.write(" ");
		bw.write(name);
		bw.write("(");
		indent += name.length();
		indent += 2;

		writeArgs(bw, indent);
		bw.write(")");
		bw.write(restPart);
	}

	private void writeArgs(BufferedWriter bw, int indent) throws IOException {
		if(argList!=null) {
			MetaArg arg;
			for (int i = 0; i <argList.size(); i++) {
				arg = argList.get(i);
				arg.writeTo(bw);
				if(i<argList.size()-1) {
					bw.write(", ");
					if(arg.anno != null) {//参数带有注解时，下一个参数另起一行
						bw.write(Formats.NL);
						writeIndent(bw, indent);
					}
				}
			}
		}
	}

	private void writeIndent(BufferedWriter bw, int len) throws IOException {
		for (int i = 0; i <len; i++) {
			bw.write(" ");
		}
	}
}
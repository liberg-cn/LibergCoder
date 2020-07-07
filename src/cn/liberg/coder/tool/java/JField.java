package cn.liberg.coder.tool.java;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.util.RegExpr;
import cn.liberg.coder.tool.util.Strings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class JField {
	public String name;
	public ArrayList<String> annoLines;
	public String defLine;//成员变量定义的整行，不支持跨多行定义一个成员变量
	public JDesc descTop;//行上面的注释

	public String visitor;//访问修饰符
	public boolean isStatic;
	public boolean isFinal;
	public boolean isVolatile;
	public boolean isTransient;
	public String type;
	public String defaultValue;//=号后面的值
	public String desc;//成员后面的单行注释

	public static final RegExpr RE = new RegExpr("^(public|protected|private) +(static +)?(final +)?(volatile +)?(transient +)?(\\w+(<.+>)?(\\[\\])?) +(\\w+) *(= *([-\\. \\[\\]\\(\\)\\w\\\"]+))?");

	//private member
	private MetaAnno anno = null;

	public MetaAnno getAnno() {
		if(anno == null) {
			anno = new MetaAnno(annoLines);
		}
		return anno;
	}

	public JField(String line) {
		defLine = line;
		Matcher matcher = RE.findMatcher(line);
		if(matcher != null) {
			visitor = matcher.group(1);
			if(matcher.group(2) != null) {
				isStatic = true;
			}
			if(matcher.group(3) != null) {
				isFinal = true;
			}
			if(matcher.group(4) != null) {
				isVolatile = true;
			}
			if(matcher.group(5) != null) {
				isTransient = true;
			}
			type = matcher.group(6);
			name = matcher.group(9);
			String dv = matcher.group(11);
			if(dv != null) {
				defaultValue = dv;
			}
		}
		int idx = line.indexOf(";");
		if(idx>=0) {
			String mark = line.substring(idx+1).trim();
			if(mark.length()>2) {
				char c = mark.charAt(1);
				if(c == '/') {
					desc = mark.substring(2).trim();
				} else if(c == '*') {
					desc = Strings.strip(mark, "/*", "*/").trim();
				}
			}
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
		bw.write(defLine);
		bw.write(Formats.NL);
	}
}
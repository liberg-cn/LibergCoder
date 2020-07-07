package cn.liberg.coder.tool.java;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.util.RegExpr;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class JInterfaceField {
	public String name;
	public String type;
	public String value;
	public ArrayList<String> annoLines;
	public String defLine;//成员变量定义的整行
	public JDesc descTop;//行上面的注释
	public String desc;//成员后面的单行注释

	//接口成员变量必须是public static final的
	public static final RegExpr RE = new RegExpr("");

	//private member
	private MetaAnno anno = null;

	public MetaAnno getAnno() {
		if(anno == null) {
			anno = new MetaAnno(annoLines);
		}
		return anno;
	}

	public JInterfaceField(String line) {
		defLine = line;
		//TODO
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
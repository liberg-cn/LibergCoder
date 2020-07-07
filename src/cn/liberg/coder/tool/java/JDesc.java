package cn.liberg.coder.tool.java;

import cn.liberg.coder.tool.LibergTool;
import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.core.ILineReader;

import java.io.BufferedWriter;
import java.io.IOException;

public class JDesc {
	public boolean mIsDoubleSlash;
	public String mText;//注释文本

	public static JDesc fileDesc() {
		StringBuilder sb = new StringBuilder();
		sb.append("/*");
		sb.append(Formats.NL);
		sb.append(" * First Created by "+ LibergTool.PROJECT_NAME +"@" + LibergTool.getVersion());
		sb.append(Formats.NL);
		sb.append(" */");
		sb.append(Formats.NL);
		return new JDesc(sb.toString());
	}

	private JDesc() {

	}

	public JDesc(String text) {
		mText = text;
		mIsDoubleSlash = mText.startsWith("//");
	}

	static JDesc parse(ILineReader reader, String firstLine) throws Exception {
		JDesc jd = new JDesc();
		String line = firstLine;
		String trimed = line.trim();
		if(trimed.startsWith("//")) {
			jd.mIsDoubleSlash = true;
			jd.mText = line + Formats.NL;
		} else if(trimed.startsWith("/*")) {
			StringBuilder sb = new StringBuilder();
			do {
				sb.append(line);
				sb.append(Formats.NL);
				if(line.trim().endsWith("*/")) {
					break;
				}
			}
			while((line=reader.next(false)) != null);
			jd.mText = sb.toString();
		}
		return jd;
	}
	
	public void wirteTo(BufferedWriter bw) throws IOException {
		bw.write(mText);
	}
}
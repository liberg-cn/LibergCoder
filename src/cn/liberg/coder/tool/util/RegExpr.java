package cn.liberg.coder.tool.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpr {
	Pattern mPattern;

	public RegExpr(String pattern) {
		mPattern = Pattern.compile(pattern);
	}

	public String find(String src, int groupIndex) {
		Matcher m = mPattern.matcher(src);
		if(m.find()) {
			return m.group(groupIndex);
		} else {
			return null;
		}
	}

	public Matcher matcher(String src) {
		return mPattern.matcher(src);
	}

	public Matcher findMatcher(String src) {
		Matcher m = mPattern.matcher(src);
		if(m.find()) {
			return m;
		} else {
			return null;
		}
	}

	public static String matcher(String src, String pattern, int groupIndex) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(src);
		if(m.find()) {
			return m.group(groupIndex);
		} else {
			return "";
		}
	}

}
